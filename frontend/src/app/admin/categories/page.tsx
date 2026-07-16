"use client"

import { useState, useEffect } from "react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Badge } from "@/components/ui/badge"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Skeleton } from "@/components/ui/skeleton"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog"
import { Label } from "@/components/ui/label"
import { Textarea } from "@/components/ui/textarea"
import { Switch } from "@/components/ui/switch"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Plus, Search, Edit, Trash2, Eye, FolderOpen, Package, AlertCircle } from "lucide-react"
import { ImageUpload } from "@/components/image-upload"
import { SafeImage } from "@/components/safe-image"
import { useToast } from "@/hooks/use-toast"
import { getAllCategories, createCategory, updateCategory, deleteCategory } from "@/lib/services/categories"

interface Category {
  id: number
  name: string
  slug: string
  description: string
  image: string
  parent?: number | null
  parent_id?: number | null
  is_active: boolean
  product_count: number
  sort_order: number
  created_at: string
  updated_at: string
}

interface NewCategoryForm {
  name: string
  slug: string
  description: string
  image: string | File | null
  parentId: string
  isActive: boolean
  sortOrder: string
}

function CategoriesSkeleton() {
  return (
    <div className="space-y-6">
      <div>
        <Skeleton className="h-8 w-32 mb-2" />
        <Skeleton className="h-4 w-64" />
      </div>

      <div className="grid grid-cols-2 md:grid-cols-4 gap-3 sm:gap-4">
        {Array.from({ length: 4 }).map((_, i) => (
          <div key={i} className="p-4 bg-white rounded-2xl border border-slate-100 shadow-sm space-y-2">
            <Skeleton className="h-4 w-20" />
            <Skeleton className="h-8 w-12" />
          </div>
        ))}
      </div>

      <div className="p-4 bg-white rounded-2xl border border-slate-100 shadow-sm">
        <div className="flex gap-4">
          <Skeleton className="h-10 flex-1" />
          <Skeleton className="h-10 w-48" />
        </div>
      </div>

      <div className="p-5 bg-white rounded-2xl border border-slate-100 shadow-sm space-y-4">
        {Array.from({ length: 5 }).map((_, i) => (
          <div key={i} className="flex items-center gap-4 py-3 border-b border-slate-100 last:border-0">
            <Skeleton className="h-4 w-24" />
            <Skeleton className="h-4 w-32" />
            <Skeleton className="h-4 flex-1" />
            <Skeleton className="h-6 w-20 rounded-full" />
            <Skeleton className="h-8 w-8 rounded-lg" />
          </div>
        ))}
      </div>
    </div>
  )
}

export default function AdminCategoriesPage() {
  const { toast } = useToast()
  const [categories, setCategories] = useState<Category[]>([])
  const [loading, setLoading] = useState(true)
  const [searchTerm, setSearchTerm] = useState("")
  const [statusFilter, setStatusFilter] = useState("all")
  const [isAddDialogOpen, setIsAddDialogOpen] = useState(false)
  const [editingCategory, setEditingCategory] = useState<Category | null>(null)
  const [viewingCategory, setViewingCategory] = useState<Category | null>(null)
  const [isViewDialogOpen, setIsViewDialogOpen] = useState(false)
  const [isEditDialogOpen, setIsEditDialogOpen] = useState(false)

  const [newCategory, setNewCategory] = useState<NewCategoryForm>({
    name: "",
    slug: "",
    description: "",
    image: "",
    parentId: "",
    isActive: true,
    sortOrder: "",
  })

  useEffect(() => {
    const fetchCategories = async () => {
      try {
        setLoading(true)
        const data = await getAllCategories()
        console.log('Categories data:', data)

        if (data && typeof data === 'object' && 'results' in data && Array.isArray(data.results)) {
          setCategories(data.results)
        } else if (Array.isArray(data)) {
          setCategories(data)
        } else {
          console.error('Categories data is not in expected format:', data)
          setCategories([])
          toast({
            title: "Error",
            description: "Invalid categories data format",
            variant: "destructive",
          })
        }
      } catch (error) {
        console.error('Error fetching categories:', error)
        setCategories([])
        toast({
          title: "Error",
          description: "Failed to fetch categories",
          variant: "destructive",
        })
      } finally {
        setLoading(false)
      }
    }

    fetchCategories()
  }, [toast])

  const filteredCategories = (categories || []).filter((category) => {
    const matchesSearch = category.name.toLowerCase().includes(searchTerm.toLowerCase())
    const matchesStatus = statusFilter === "all" || (statusFilter === "active" ? category.is_active : !category.is_active)
    return matchesSearch && matchesStatus
  })

  const generateSlug = (name: string) => {
    return name
      .toLowerCase()
      .replace(/[^a-z0-9]+/g, "-")
      .replace(/(^-|-$)/g, "")
  }

  const handleAddCategory = async () => {
    if (!newCategory.name) {
      toast({
        title: "Error",
        description: "Category name is required",
        variant: "destructive",
      })
      return
    }

    try {
      const categoryData = {
        name: newCategory.name,
        slug: newCategory.slug || generateSlug(newCategory.name),
        description: newCategory.description,
        imageFile: newCategory.image instanceof File ? newCategory.image : undefined,
        image: typeof newCategory.image === 'string' ? newCategory.image : undefined,
        parent_id: newCategory.parentId ? Number.parseInt(newCategory.parentId) : undefined,
        is_active: newCategory.isActive,
        sort_order: newCategory.sortOrder ? Number.parseInt(newCategory.sortOrder) : undefined,
      }

      const createdCategory = await createCategory(categoryData)
      setCategories([...(categories || []), createdCategory])

      setNewCategory({
        name: "",
        slug: "",
        description: "",
        image: "",
        parentId: "",
        isActive: true,
        sortOrder: "",
      })
      setIsAddDialogOpen(false)

      toast({
        title: "Success",
        description: "Category created successfully",
      })
    } catch (error) {
      console.error('Error creating category:', error)
      toast({
        title: "Error",
        description: "Failed to create category",
        variant: "destructive",
      })
    }
  }

  const handleEditCategory = async () => {
    if (!editingCategory || !newCategory.name) {
      toast({
        title: "Error",
        description: "Category name is required",
        variant: "destructive",
      })
      return
    }

    try {
      const categoryData = {
        name: newCategory.name,
        slug: newCategory.slug || generateSlug(newCategory.name),
        description: newCategory.description,
        imageFile: newCategory.image instanceof File ? newCategory.image : undefined,
        image: typeof newCategory.image === 'string' ? newCategory.image : undefined,
        parent_id: newCategory.parentId ? Number.parseInt(newCategory.parentId) : undefined,
        is_active: newCategory.isActive,
        sort_order: newCategory.sortOrder ? Number.parseInt(newCategory.sortOrder) : undefined,
      }

      const updatedCategory = await updateCategory(editingCategory.id.toString(), categoryData)
      setCategories((categories || []).map((c) => (c.id === editingCategory.id ? updatedCategory : c)))

      setEditingCategory(null)
      setNewCategory({
        name: "",
        slug: "",
        description: "",
        image: "",
        parentId: "",
        isActive: true,
        sortOrder: "",
      })
      setIsEditDialogOpen(false)

      toast({
        title: "Success",
        description: "Category updated successfully",
      })
    } catch (error) {
      console.error('Error updating category:', error)
      toast({
        title: "Error",
        description: "Failed to update category",
        variant: "destructive",
      })
    }
  }

  const handleDeleteCategory = async (id: number) => {
    const categoryToDelete = (categories || []).find((c) => c.id === id)
    const hasChildren = (categories || []).some((c) => c.parent_id === id)

    if (hasChildren) {
      toast({
        title: "Error",
        description: "Cannot delete category with subcategories. Please delete subcategories first.",
        variant: "destructive",
      })
      return
    }

    try {
      await deleteCategory(id.toString())
      setCategories((categories || []).filter((c) => c.id !== id))
      toast({
        title: "Success",
        description: `Category "${categoryToDelete?.name}" deleted successfully`,
      })
    } catch (error) {
      console.error('Error deleting category:', error)
      toast({
        title: "Error",
        description: "Failed to delete category",
        variant: "destructive",
      })
    }
  }

  const toggleCategoryStatus = async (id: number) => {
    const category = (categories || []).find((c) => c.id === id)
    if (!category) return

    try {
      const updatedCategory = await updateCategory(id.toString(), {
        ...category,
        is_active: !category.is_active,
      })
      setCategories(
        (categories || []).map((c) => (c.id === id ? updatedCategory : c))
      )
    } catch (error) {
      console.error('Error toggling category status:', error)
      toast({
        title: "Error",
        description: "Failed to update category status",
        variant: "destructive",
      })
    }
  }

  const getParentCategories = () => {
    return (categories || []).filter((c) => !c.parent_id)
  }

  const getCategoryHierarchy = (category: Category) => {
    if (!category.parent_id) return category.name

    const parent = (categories || []).find((c) => c.id === category.parent_id)
    return parent ? `${parent.name} > ${category.name}` : category.name
  }

  if (loading && categories.length === 0) {
    return <CategoriesSkeleton />
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-2xl sm:text-3xl font-bold tracking-tight text-slate-900">Categories</h1>
          <p className="text-slate-500 text-sm sm:text-base">Organize your products with categories and subcategories</p>
        </div>
        <Dialog open={isAddDialogOpen} onOpenChange={(open) => {
          setIsAddDialogOpen(open)
          if (open) {
            setNewCategory({
              name: "",
              slug: "",
              description: "",
              image: "",
              parentId: "",
              isActive: true,
              sortOrder: "",
            })
          }
        }}>
          <DialogTrigger asChild>
            <Button className="rounded-xl">
              <Plus className="mr-2 h-4 w-4" />
              Add Category
            </Button>
          </DialogTrigger>
          <DialogContent className="max-w-2xl">
            <DialogHeader>
              <DialogTitle>Add New Category</DialogTitle>
              <DialogDescription>Create a new category to organize your products</DialogDescription>
            </DialogHeader>
            <div className="grid gap-4 py-4 max-h-[70vh] overflow-y-auto scrollbar-hidden px-2">
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="name">Category Name *</Label>
                  <Input
                    id="name"
                    value={newCategory.name}
                    onChange={(e) => {
                      const name = e.target.value
                      setNewCategory((prev) => ({
                        ...prev,
                        name,
                        slug: generateSlug(name),
                      }))
                    }}
                    placeholder="e.g., Smartphones"
                    className="rounded-xl"
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="slug">URL Slug</Label>
                  <Input
                    id="slug"
                    value={newCategory.slug}
                    onChange={(e) => setNewCategory((prev) => ({ ...prev, slug: e.target.value }))}
                    placeholder="e.g., smartphones"
                    className="rounded-xl"
                  />
                </div>
              </div>

              <div className="space-y-2">
                <Label htmlFor="description">Description</Label>
                <Textarea
                  id="description"
                  value={newCategory.description}
                  onChange={(e) => setNewCategory((prev) => ({ ...prev, description: e.target.value }))}
                  placeholder="Brief description of the category"
                />
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="parentId">Parent Category (Optional)</Label>
                  <Select
                    value={newCategory.parentId}
                    onValueChange={(value) => setNewCategory((prev) => ({ ...prev, parentId: value }))}
                  >
                    <SelectTrigger className="rounded-xl">
                      <SelectValue placeholder="Select parent category" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="none">None (Top Level)</SelectItem>
                      {getParentCategories().map((category) => (
                        <SelectItem key={category.id} value={category.id.toString()}>
                          {category.name}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>
                <div className="space-y-2">
                  <Label htmlFor="sortOrder">Sort Order</Label>
                  <Input
                    id="sortOrder"
                    type="number"
                    value={newCategory.sortOrder}
                    onChange={(e) => setNewCategory((prev) => ({ ...prev, sortOrder: e.target.value }))}
                    placeholder="1"
                    className="rounded-xl"
                  />
                </div>
              </div>

              <div className="space-y-2">
                <ImageUpload
                  value={newCategory.image || undefined}
                  onChange={(value) => setNewCategory((prev) => ({ ...prev, image: value }))}
                  label="Category Image"
                />
              </div>

              <div className="flex items-center space-x-2">
                <Switch
                  id="isActive"
                  checked={newCategory.isActive}
                  onCheckedChange={(checked) => setNewCategory((prev) => ({ ...prev, isActive: checked }))}
                />
                <Label htmlFor="isActive">Active Category</Label>
              </div>
            </div>
            <DialogFooter>
              <Button variant="outline" onClick={() => setIsAddDialogOpen(false)} className="rounded-xl">
                Cancel
              </Button>
              <Button onClick={handleAddCategory} className="rounded-xl">Add Category</Button>
            </DialogFooter>
          </DialogContent>
        </Dialog>
      </div>

      {/* Stats Cards */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-3 sm:gap-4">
        {[
          { label: "Total Categories", value: (categories || []).length, icon: FolderOpen, color: "from-blue-500 to-indigo-500" },
          { label: "Active Categories", value: (categories || []).filter((c) => c.is_active).length, icon: FolderOpen, color: "from-green-500 to-emerald-500" },
          { label: "Parent Categories", value: (categories || []).filter((c) => !c.parent_id).length, icon: FolderOpen, color: "from-purple-500 to-pink-500" },
          { label: "Total Products", value: (categories || []).reduce((sum, c) => sum + (c.product_count || 0), 0), icon: Package, color: "from-amber-500 to-orange-500" },
        ].map((stat) => (
          <Card key={stat.label} className="border-0 shadow-sm bg-white rounded-2xl overflow-hidden hover:shadow-md transition-shadow">
            <CardContent className="p-4 sm:p-5">
              <div className="flex items-start justify-between mb-3">
                <p className="text-xs sm:text-sm font-medium text-slate-500">{stat.label}</p>
                <div className={`w-9 h-9 sm:w-10 sm:h-10 bg-gradient-to-br ${stat.color} rounded-xl flex items-center justify-center shadow-lg`}>
                  <stat.icon className="h-4 w-4 sm:h-5 sm:w-5 text-white" />
                </div>
              </div>
              <div className="text-xl sm:text-2xl font-bold text-slate-900">{stat.value}</div>
            </CardContent>
          </Card>
        ))}
      </div>

      {/* Filters */}
      <Card className="border-0 shadow-sm bg-white rounded-2xl overflow-hidden">
        <CardContent className="p-4 sm:p-5">
          <div className="flex flex-col sm:flex-row gap-3">
            <div className="flex-1 relative">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-slate-400 w-4 h-4" />
              <Input
                placeholder="Search categories..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="pl-10 h-11 bg-slate-50 border-slate-200 rounded-xl focus:bg-white"
              />
            </div>
            <Select value={statusFilter} onValueChange={setStatusFilter}>
              <SelectTrigger className="w-full sm:w-48 h-11 bg-slate-50 border-slate-200 rounded-xl">
                <SelectValue placeholder="Status" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">All Status</SelectItem>
                <SelectItem value="active">Active</SelectItem>
                <SelectItem value="inactive">Inactive</SelectItem>
              </SelectContent>
            </Select>
          </div>
        </CardContent>
      </Card>

      {/* Categories List */}
      <Card className="border-0 shadow-sm bg-white rounded-2xl overflow-hidden">
        <CardHeader className="p-4 sm:p-5 pb-0">
          <CardTitle className="text-lg font-bold text-slate-900">
            Categories ({filteredCategories.length})
          </CardTitle>
        </CardHeader>
        <CardContent className="p-4 sm:p-5">
          {filteredCategories.length === 0 ? (
            <div className="text-center py-12">
              <div className="w-16 h-16 bg-slate-100 rounded-full flex items-center justify-center mx-auto mb-4">
                <AlertCircle className="w-8 h-8 text-slate-300" />
              </div>
              <p className="text-slate-500 font-medium">
                {searchTerm || statusFilter !== "all" ? "No categories match your filters." : "No categories found."}
              </p>
            </div>
          ) : (
            <>
              {/* Desktop Table */}
              <div className="hidden md:block space-y-0">
                {/* Header */}
                <div className="grid grid-cols-6 gap-x-6 px-4 py-3 border-b">
                  <span className="text-xs font-medium text-slate-500 uppercase tracking-wider">Category</span>
                  <span className="text-xs font-medium text-slate-500 uppercase tracking-wider">Hierarchy</span>
                  <span className="text-xs font-medium text-slate-500 uppercase tracking-wider">Products</span>
                  <span className="text-xs font-medium text-slate-500 uppercase tracking-wider">Status</span>
                  <span className="text-xs font-medium text-slate-500 uppercase tracking-wider">Sort Order</span>
                  <span className="text-xs font-medium text-slate-500 uppercase tracking-wider">Actions</span>
                </div>
                {/* Rows */}
                {filteredCategories
                  .sort((a, b) => (a.sort_order || 0) - (b.sort_order || 0))
                  .map((category) => (
                    <div key={category.id} className="grid grid-cols-6 gap-x-6 items-center px-4 py-4 border-b hover:bg-slate-50/50 transition-colors">
                      <div className="flex items-center space-x-3 min-w-0">
                        <div className="w-10 h-10 bg-slate-100 rounded-lg flex items-center justify-center overflow-hidden flex-shrink-0">
                          <SafeImage
                            src={category.image || "/placeholder.svg"}
                            alt={category.name}
                            width={40}
                            height={40}
                            className="w-full h-full object-cover"
                          />
                        </div>
                        <div className="min-w-0">
                          <p className="font-medium text-slate-900 text-sm truncate">{category.name}</p>
                          <p className="text-xs text-slate-500 truncate">/{category.slug}</p>
                        </div>
                      </div>
                      <div className="min-w-0">
                        <div className="flex items-center">
                          {category.parent_id && <span className="text-slate-400 mr-1">└</span>}
                          <span className="text-sm text-slate-600 truncate">{getCategoryHierarchy(category)}</span>
                        </div>
                      </div>
                      <div>
                        <Badge variant="outline" className="text-xs rounded-full">{category.product_count || 0} products</Badge>
                      </div>
                      <div className="flex items-center space-x-2">
                        <Badge
                          variant={category.is_active ? "default" : "secondary"}
                          className={category.is_active ? "bg-green-100 text-green-700 border-0" : "bg-slate-100 text-slate-700 border-0"}
                        >
                          {category.is_active ? "Active" : "Inactive"}
                        </Badge>
                        <Switch
                          checked={category.is_active}
                          onCheckedChange={() => toggleCategoryStatus(category.id)}
                        />
                      </div>
                      <span className="text-sm text-slate-600">{category.sort_order || 0}</span>
                      <div className="flex items-center space-x-1">
                        <Button
                          variant="ghost"
                          size="icon"
                          className="h-9 w-9 rounded-xl hover:bg-slate-100"
                          onClick={() => {
                            setViewingCategory(category)
                            setIsViewDialogOpen(true)
                          }}
                        >
                          <Eye className="h-4 w-4 text-slate-500" />
                        </Button>
                        <Button
                          variant="ghost"
                          size="icon"
                          className="h-9 w-9 rounded-xl hover:bg-slate-100"
                          onClick={() => {
                            setEditingCategory(category)
                            setNewCategory({
                              name: category.name,
                              slug: category.slug,
                              description: category.description,
                              image: category.image,
                              parentId: category.parent_id?.toString() || "",
                              isActive: category.is_active,
                              sortOrder: category.sort_order?.toString() || "",
                            })
                            setIsEditDialogOpen(true)
                          }}
                        >
                          <Edit className="h-4 w-4 text-slate-500" />
                        </Button>
                        <Button
                          variant="ghost"
                          size="icon"
                          className="h-9 w-9 rounded-xl hover:bg-slate-100"
                          onClick={() => handleDeleteCategory(category.id)}
                        >
                          <Trash2 className="h-4 w-4 text-red-500" />
                        </Button>
                      </div>
                    </div>
                  ))}
              </div>

              {/* Mobile Cards */}
              <div className="md:hidden space-y-3">
                {filteredCategories
                  .sort((a, b) => (a.sort_order || 0) - (b.sort_order || 0))
                  .map((category) => (
                    <div key={category.id} className="p-4 bg-slate-50 rounded-xl space-y-3">
                      <div className="flex items-center justify-between">
                        <div className="flex items-center space-x-3 min-w-0">
                          <div className="w-9 h-9 bg-white rounded-lg flex items-center justify-center overflow-hidden flex-shrink-0 border border-slate-200">
                            <SafeImage
                              src={category.image || "/placeholder.svg"}
                              alt={category.name}
                              width={36}
                              height={36}
                              className="w-full h-full object-cover"
                            />
                          </div>
                          <div className="min-w-0">
                            <p className="font-medium text-slate-900 text-sm truncate">{category.name}</p>
                            <p className="text-xs text-slate-500 truncate">{getCategoryHierarchy(category)}</p>
                          </div>
                        </div>
                        <Badge
                          variant={category.is_active ? "default" : "secondary"}
                          className={`text-xs rounded-full px-2.5 py-1 flex-shrink-0 ml-2 ${category.is_active ? "bg-green-100 text-green-700 border-0" : "bg-slate-100 text-slate-700 border-0"}`}
                        >
                          {category.is_active ? "Active" : "Inactive"}
                        </Badge>
                      </div>
                      <div className="flex items-center justify-between">
                        <div className="flex items-center gap-2">
                          <span className="text-xs bg-white text-slate-600 px-2 py-1 rounded-lg border border-slate-200">
                            {category.product_count || 0} products
                          </span>
                          <span className="text-xs text-slate-400">Sort: {category.sort_order || 0}</span>
                        </div>
                        <div className="flex items-center space-x-1">
                          <Switch
                            checked={category.is_active}
                            onCheckedChange={() => toggleCategoryStatus(category.id)}
                          />
                          <Button
                            variant="ghost"
                            size="icon"
                            className="h-8 w-8 rounded-xl hover:bg-white"
                            onClick={() => {
                              setViewingCategory(category)
                              setIsViewDialogOpen(true)
                            }}
                          >
                            <Eye className="h-3.5 w-3.5 text-slate-500" />
                          </Button>
                          <Button
                            variant="ghost"
                            size="icon"
                            className="h-8 w-8 rounded-xl hover:bg-white"
                            onClick={() => {
                              setEditingCategory(category)
                              setNewCategory({
                                name: category.name,
                                slug: category.slug,
                                description: category.description,
                                image: category.image,
                                parentId: category.parent_id?.toString() || "",
                                isActive: category.is_active,
                                sortOrder: category.sort_order?.toString() || "",
                              })
                              setIsEditDialogOpen(true)
                            }}
                          >
                            <Edit className="h-3.5 w-3.5 text-slate-500" />
                          </Button>
                          <Button
                            variant="ghost"
                            size="icon"
                            className="h-8 w-8 rounded-xl hover:bg-white"
                            onClick={() => handleDeleteCategory(category.id)}
                          >
                            <Trash2 className="h-3.5 w-3.5 text-red-500" />
                          </Button>
                        </div>
                      </div>
                    </div>
                  ))}
              </div>
            </>
          )}
        </CardContent>
      </Card>

      {/* Category Detail View Dialog */}
      <Dialog open={isViewDialogOpen} onOpenChange={setIsViewDialogOpen}>
        <DialogContent className="max-w-2xl">
          <DialogHeader>
            <DialogTitle>Category Details</DialogTitle>
            <DialogDescription>Complete category information</DialogDescription>
          </DialogHeader>
          {viewingCategory && (
            <div className="space-y-6">
              <div className="grid md:grid-cols-2 gap-6">
                <div className="space-y-4">
                  <div className="aspect-square bg-slate-100 rounded-lg overflow-hidden">
                    <SafeImage
                      src={viewingCategory.image || "/placeholder.svg"}
                      alt={viewingCategory.name}
                      width={320}
                      height={320}
                      className="w-full h-full object-cover"
                    />
                  </div>
                </div>
                <div className="space-y-4">
                  <div>
                    <h2 className="text-2xl font-bold">{viewingCategory.name}</h2>
                    <p className="text-slate-600">/{viewingCategory.slug}</p>
                  </div>
                  <div>
                    <h3 className="font-medium mb-2">Description</h3>
                    <p className="text-slate-600">{viewingCategory.description || "No description provided"}</p>
                  </div>
                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <h3 className="font-medium">Status</h3>
                      <Badge variant={viewingCategory.is_active ? "default" : "secondary"}>
                        {viewingCategory.is_active ? "Active" : "Inactive"}
                      </Badge>
                    </div>
                    <div>
                      <h3 className="font-medium">Products</h3>
                      <p className="text-slate-600">{viewingCategory.product_count || 0} products</p>
                    </div>
                  </div>
                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <h3 className="font-medium">Sort Order</h3>
                      <p className="text-slate-600">{viewingCategory.sort_order || 0}</p>
                    </div>
                    <div>
                      <h3 className="font-medium">Category ID</h3>
                      <p className="text-slate-600">#{viewingCategory.id}</p>
                    </div>
                  </div>
                  {viewingCategory.parent_id && (
                    <div>
                      <h3 className="font-medium">Parent Category</h3>
                      <p className="text-slate-600">
                        {(categories || []).find((c) => c.id === viewingCategory.parent_id)?.name || "Unknown"}
                      </p>
                    </div>
                  )}
                </div>
              </div>
              <div className="grid grid-cols-2 gap-4 pt-4 border-t">
                <div>
                  <h3 className="font-medium">Created</h3>
                  <p className="text-slate-600">{new Date(viewingCategory.created_at).toLocaleDateString()}</p>
                </div>
                <div>
                  <h3 className="font-medium">Last Updated</h3>
                  <p className="text-slate-600">{new Date(viewingCategory.updated_at).toLocaleDateString()}</p>
                </div>
              </div>
            </div>
          )}
          <DialogFooter>
            <Button variant="outline" onClick={() => setIsViewDialogOpen(false)} className="rounded-xl">
              Close
            </Button>
            <Button
              onClick={() => {
                setIsViewDialogOpen(false)
                setEditingCategory(viewingCategory)
                setNewCategory({
                  name: viewingCategory!.name,
                  slug: viewingCategory!.slug,
                  description: viewingCategory!.description,
                  image: viewingCategory!.image,
                  parentId: viewingCategory!.parent_id?.toString() || "",
                  isActive: viewingCategory!.is_active,
                  sortOrder: viewingCategory!.sort_order?.toString() || "",
                })
                setIsEditDialogOpen(true)
              }}
              className="rounded-xl"
            >
              Edit Category
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Edit Category Dialog */}
      <Dialog open={isEditDialogOpen} onOpenChange={(open) => {
        setIsEditDialogOpen(open)
        if (!open) {
          setEditingCategory(null)
          setNewCategory({
            name: "",
            slug: "",
            description: "",
            image: "",
            parentId: "",
            isActive: true,
            sortOrder: "",
          })
        }
      }}>
        <DialogContent className="max-w-2xl">
          <DialogHeader>
            <DialogTitle>Edit Category</DialogTitle>
            <DialogDescription>Update category information</DialogDescription>
          </DialogHeader>
          <div className="grid gap-4 py-4 max-h-[70vh] overflow-y-auto scrollbar-hidden px-2">
            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="edit-name">Category Name *</Label>
                <Input
                  id="edit-name"
                  value={newCategory.name}
                  onChange={(e) => {
                    const name = e.target.value
                    setNewCategory((prev) => ({
                      ...prev,
                      name,
                      slug: generateSlug(name),
                    }))
                  }}
                  className="rounded-xl"
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="edit-slug">URL Slug</Label>
                <Input
                  id="edit-slug"
                  value={newCategory.slug}
                  onChange={(e) => setNewCategory((prev) => ({ ...prev, slug: e.target.value }))}
                  className="rounded-xl"
                />
              </div>
            </div>

            <div className="space-y-2">
              <Label htmlFor="edit-description">Description</Label>
              <Textarea
                id="edit-description"
                value={newCategory.description}
                onChange={(e) => setNewCategory((prev) => ({ ...prev, description: e.target.value }))}
              />
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="edit-parentId">Parent Category</Label>
                <Select
                  value={newCategory.parentId}
                  onValueChange={(value) => setNewCategory((prev) => ({ ...prev, parentId: value }))}
                >
                  <SelectTrigger className="rounded-xl">
                    <SelectValue placeholder="Select parent category" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="none">None (Top Level)</SelectItem>
                    {getParentCategories()
                      .filter((c) => c.id !== editingCategory?.id)
                      .map((category) => (
                        <SelectItem key={category.id} value={category.id.toString()}>
                          {category.name}
                        </SelectItem>
                      ))}
                  </SelectContent>
                </Select>
              </div>
              <div className="space-y-2">
                <Label htmlFor="edit-sortOrder">Sort Order</Label>
                <Input
                  id="edit-sortOrder"
                  type="number"
                  value={newCategory.sortOrder}
                  onChange={(e) => setNewCategory((prev) => ({ ...prev, sortOrder: e.target.value }))}
                  className="rounded-xl"
                />
              </div>
            </div>

            <div className="space-y-2">
              <ImageUpload
                value={newCategory.image || undefined}
                onChange={(value) => setNewCategory((prev) => ({ ...prev, image: value }))}
                label="Category Image"
              />
            </div>

            <div className="flex items-center space-x-2">
              <Switch
                id="edit-isActive"
                checked={newCategory.isActive}
                onCheckedChange={(checked) => setNewCategory((prev) => ({ ...prev, isActive: checked }))}
              />
              <Label htmlFor="edit-isActive">Active Category</Label>
            </div>
          </div>
          <DialogFooter>
            <Button
              variant="outline"
              onClick={() => {
                setIsEditDialogOpen(false)
                setEditingCategory(null)
                setNewCategory({
                  name: "",
                  slug: "",
                  description: "",
                  image: "",
                  parentId: "",
                  isActive: true,
                  sortOrder: "",
                })
              }}
              className="rounded-xl"
            >
              Cancel
            </Button>
            <Button onClick={handleEditCategory} className="rounded-xl">Update Category</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  )
}
