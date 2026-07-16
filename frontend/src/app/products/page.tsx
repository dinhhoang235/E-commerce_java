"use client"

import { useState, useEffect } from "react"
import { useSearchParams } from "next/navigation"
import Link from "next/link"
import Image from "next/image"
import { Button } from "@/components/ui/button"
import { Card, CardContent } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Input } from "@/components/ui/input"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { ShoppingCart, X, Search, SlidersHorizontal } from "lucide-react"
import { Skeleton } from "@/components/ui/skeleton"
import { useCart } from "@/components/cart-provider"
import { WishlistButton } from "@/components/wishlist-button"
import { getAllProducts, getProductsByCategory } from "@/lib/services/products"
import { getAllCategories } from "@/lib/services/categories"
import { StarRating } from "@/components/star-rating"
import { formatImageUrl, isExternalImage } from "@/lib/utils/image"

interface Category {
  id: string | number
  name: string
  slug: string
  description?: string
  image?: string
  is_active?: boolean
  product_count?: number
  parent?: Category | null
  parent_id?: number | null
}

interface ProductColor {
  id: number
  name: string
  hex_code: string
}

interface ProductVariant {
  id: number
  color: ProductColor
  storage: string
  price: string
  stock: number
  sold: number
  is_in_stock: boolean
  total_stock: number
}

interface Product {
  id: string | number
  name: string
  description: string
  min_price: number
  max_price: number
  image?: string
  category: Category
  rating: number
  reviews: number
  badge?: string
  variants: ProductVariant[]
  available_colors: ProductColor[]
  available_storages: string[]
  total_stock: number
  features?: string[]
  full_description?: string
}

export default function ProductsPage() {
  const searchParams = useSearchParams()
  const { addItem } = useCart()
  const [allProducts, setAllProducts] = useState<Product[]>([])
  const [allCategories, setAllCategories] = useState<Category[]>([])
  const [filteredProducts, setFilteredProducts] = useState<Product[]>([])
  const [totalProductCount, setTotalProductCount] = useState(0)
  const [searchTerm, setSearchTerm] = useState("")
  const [selectedCategory, setSelectedCategory] = useState("all")
  const [sortBy, setSortBy] = useState("name")
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState("")

  useEffect(() => {
    const fetchData = async () => {
      try {
        setIsLoading(true)
        
        const categoriesResponse = await getAllCategories()
        let categoriesData: Category[] = []
        if (Array.isArray(categoriesResponse)) {
          categoriesData = categoriesResponse
        } else if (categoriesResponse && Array.isArray(categoriesResponse.results)) {
          categoriesData = categoriesResponse.results
        }
        
        const activeCategories = categoriesData.filter((cat: Category) => cat.is_active !== false)
        setAllCategories(activeCategories)
        
        const urlCategory = searchParams.get("category")
        let response
        
        if (urlCategory && urlCategory !== 'all') {
          response = await getProductsByCategory(urlCategory)
        } else {
          response = await getAllProducts()
        }
        
        if (response && response.results && Array.isArray(response.results)) {
          setAllProducts(response.results)
          setTotalProductCount(response.count || response.results.length)
        } else if (Array.isArray(response)) {
          setAllProducts(response)
          setTotalProductCount(response.length)
        } else {
          setAllProducts([])
          setError("Data format error. Please try again later.")
        }
        setIsLoading(false)
      } catch (err) {
        console.error("Failed to fetch data:", err)
        setError("Failed to load data. Please try again later.")
        setIsLoading(false)
      }
    }

    fetchData()
  }, [searchParams])

  useEffect(() => {
    const urlSearch = searchParams.get("search")
    const urlCategory = searchParams.get("category")

    if (!urlSearch && !urlCategory) {
      setSearchTerm("")
      setSelectedCategory("all")
      return
    }

    if (urlSearch) {
      setSearchTerm(urlSearch)
    } else {
      setSearchTerm("")
    }

    if (urlCategory) {
      setSelectedCategory(urlCategory)
    } else {
      setSelectedCategory("all")
    }
  }, [searchParams])

  useEffect(() => {
    if (!Array.isArray(allProducts) || allProducts.length === 0) {
      setFilteredProducts([]);
      return;
    }

    try {
      let filtered = [...allProducts];

      if (searchTerm) {
        const searchLower = searchTerm.toLowerCase();
        filtered = filtered.filter(
          (product: Product) => {
            if (!product) return false;
            
            let categoryName = "";
            if (product.category) {
              categoryName = product.category.name.toLowerCase();
            }
            
            return (
              (product.name && product.name.toLowerCase().includes(searchLower)) ||
              (product.description && product.description.toLowerCase().includes(searchLower)) ||
              categoryName.includes(searchLower)
            );
          }
        );
      }
      
      filtered.sort((a: Product, b: Product) => {
        try {
          switch (sortBy) {
            case "price-low":
              return a.min_price - b.min_price
            case "price-high":
              return b.max_price - a.max_price
            case "rating":
              return b.rating - a.rating
            default:
              return a.name.localeCompare(b.name)
          }
        } catch (err) {
          return 0;
        }
      });
      
      setFilteredProducts(filtered);
    } catch (err) {
      setFilteredProducts([]);
    }
  }, [allProducts, searchTerm, sortBy])

  const handleCategoryChange = async (newCategory: string) => {
    setSelectedCategory(newCategory)
    setIsLoading(true)
    setError("")
    
    try {
      let response
      if (newCategory === 'all') {
        response = await getAllProducts()
      } else {
        response = await getProductsByCategory(newCategory)
      }
      
      if (response && response.results && Array.isArray(response.results)) {
        setAllProducts(response.results)
        setTotalProductCount(response.count || response.results.length)
      } else if (Array.isArray(response)) {
        setAllProducts(response)
        setTotalProductCount(response.length)
      } else {
        setAllProducts([])
        setError("Data format error. Please try again later.")
      }
    } catch (err) {
      setError("Failed to load products. Please try again later.")
      setAllProducts([])
    } finally {
      setIsLoading(false)
    }
  }

  const clearSearch = () => setSearchTerm("")
  const clearCategory = () => setSelectedCategory("all")
  const hasActiveFilters = searchTerm || selectedCategory !== "all"

  if (isLoading) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-slate-50 via-white to-slate-100">
        <div className="container mx-auto px-4 sm:px-6 py-6 sm:py-8">
          <Skeleton className="h-8 sm:h-9 w-48 mb-6" />
          
          <div className="flex flex-wrap gap-2 sm:gap-3 mb-6">
            {Array.from({ length: 5 }).map((_, i) => (
              <Skeleton key={i} className="h-10 w-20 rounded-xl" />
            ))}
          </div>
          
          <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4 sm:gap-6">
            {Array.from({ length: 8 }).map((_, i) => (
              <div key={i} className="rounded-2xl overflow-hidden border border-slate-100 bg-white">
                <Skeleton className="h-48 sm:h-56 w-full" />
                <div className="p-4 space-y-3">
                  <Skeleton className="h-4 w-3/4" />
                  <Skeleton className="h-3 w-1/2" />
                  <div className="flex items-center gap-2">
                    <Skeleton className="h-5 w-16 rounded-full" />
                    <Skeleton className="h-5 w-12 rounded-full" />
                  </div>
                  <div className="flex items-center justify-between pt-2">
                    <Skeleton className="h-6 w-20" />
                    <Skeleton className="h-9 w-9 rounded-xl" />
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
    )
  }

  if (error) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center p-8 bg-white rounded-3xl shadow-xl max-w-md mx-4">
          <div className="w-16 h-16 bg-red-100 rounded-full flex items-center justify-center mx-auto mb-4">
            <span className="text-2xl">⚠️</span>
          </div>
          <h3 className="text-lg font-semibold mb-2 text-slate-900">Oops! Something went wrong</h3>
          <p className="text-slate-500 mb-6">{error}</p>
          <Button onClick={() => window.location.reload()} className="bg-gradient-to-r from-blue-600 to-purple-600 rounded-xl px-6 py-2.5 font-semibold shadow-lg shadow-blue-500/25">
            Try Again
          </Button>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50/50 via-white to-slate-100/50">
      <div className="container mx-auto px-4 py-8">
        {/* Header */}
        <div className="mb-8">
          <Badge variant="secondary" className="bg-blue-100 text-blue-700 mb-3 px-4 py-1.5 rounded-full">
            <SlidersHorizontal className="w-3 h-3 mr-1.5" />
            Products
          </Badge>
          <h1 className="text-3xl lg:text-4xl font-bold tracking-tight">
            {selectedCategory === "all"
              ? "All Products"
              : allCategories.find(cat => cat.slug === selectedCategory)?.name || "Products"}
          </h1>
          <p className="text-slate-500 mt-2">
            {searchTerm ? `Search results for "${searchTerm}"` : "Discover our complete collection"}
          </p>
        </div>

        {/* Active Filters */}
        {hasActiveFilters && (
          <div className="mb-6 flex items-center gap-2 flex-wrap">
            <span className="text-sm font-medium text-slate-600">Active filters:</span>
            {searchTerm && (
              <Badge variant="secondary" className="flex items-center gap-1.5 bg-white rounded-full px-3 py-1 shadow-sm">
                <Search className="w-3 h-3" />
                {searchTerm}
                <Button variant="ghost" size="sm" className="h-4 w-4 p-0 hover:bg-transparent" onClick={clearSearch}>
                  <X className="h-3 w-3" />
                </Button>
              </Badge>
            )}
            {selectedCategory !== "all" && (
              <Badge variant="secondary" className="flex items-center gap-1.5 bg-white rounded-full px-3 py-1 shadow-sm">
                {selectedCategory}
                <Button variant="ghost" size="sm" className="h-4 w-4 p-0 hover:bg-transparent" onClick={clearCategory}>
                  <X className="h-3 w-3" />
                </Button>
              </Badge>
            )}
          </div>
        )}

        {/* Filters */}
        <div className="flex flex-col lg:flex-row gap-4 mb-8 p-5 bg-white/80 backdrop-blur-sm rounded-2xl shadow-sm border border-slate-100">
          <div className="flex-1 relative">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
            <Input
              placeholder="Search products..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="w-full pl-10 h-11 rounded-xl bg-slate-50 border-slate-200/60 focus:bg-white focus:border-blue-300 focus:ring-2 focus:ring-blue-500/20"
            />
          </div>
          <Select value={selectedCategory} onValueChange={handleCategoryChange}>
            <SelectTrigger className="w-full lg:w-48 h-11 rounded-xl bg-slate-50">
              <SelectValue placeholder="Category" />
            </SelectTrigger>
            <SelectContent className="rounded-xl">
              <SelectItem value="all">All Categories</SelectItem>
              {allCategories.map((category) => (
                <SelectItem key={category.id} value={category.slug}>
                  {category.name}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
          <Select value={sortBy} onValueChange={setSortBy}>
            <SelectTrigger className="w-full lg:w-48 h-11 rounded-xl bg-slate-50">
              <SelectValue placeholder="Sort by" />
            </SelectTrigger>
            <SelectContent className="rounded-xl">
              <SelectItem value="name">Name</SelectItem>
              <SelectItem value="price-low">Price: Low to High</SelectItem>
              <SelectItem value="price-high">Price: High to Low</SelectItem>
              <SelectItem value="rating">Rating</SelectItem>
            </SelectContent>
          </Select>
        </div>

        {/* Results Count */}
        <div className="mb-6">
          <p className="text-sm text-slate-500 font-medium">
            Showing {filteredProducts.length} of {totalProductCount} products
          </p>
        </div>

        {/* Products Grid */}
        {filteredProducts.length > 0 && (
          <div className="grid md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
            {filteredProducts.map((product: Product) => (
              <Card key={product.id} className="group glass-card hover:shadow-card-hover transition-all duration-500 hover:-translate-y-2 rounded-3xl overflow-hidden border-0">
                <CardContent className="p-0">
                  <div className="relative bg-gradient-to-br from-slate-50 to-slate-100 p-4">
                    {product.badge && (
                      <Badge className="absolute top-3 left-3 z-10 bg-gradient-to-r from-red-500 to-rose-500 text-white font-bold px-3 py-1 rounded-full shadow-lg shadow-red-500/25">
                        {product.badge}
                      </Badge>
                    )}
                    <div className="absolute top-3 right-3 z-10">
                      <WishlistButton 
                        productId={typeof product.id === 'string' ? parseInt(product.id) : product.id}
                        variant="icon"
                        className="bg-white/80 backdrop-blur-sm hover:bg-white shadow-md rounded-full h-9 w-9"
                      />
                    </div>
                    <Link href={`/products/${product.id}`}>
                      <div className="relative aspect-square overflow-hidden rounded-2xl">
                        <Image
                          src={formatImageUrl(product.image)}
                          alt={product.name}
                          fill
                          unoptimized={isExternalImage(product.image)}
                          className="object-contain p-4 group-hover:scale-110 transition-transform duration-500"
                        />
                      </div>
                    </Link>
                  </div>
                  <div className="p-5 bg-white space-y-3">
                    <Link href={`/products/${product.id}`} className="block">
                      <h3 className="font-semibold text-slate-900 line-clamp-2 hover:text-blue-600 transition-colors leading-tight text-sm">
                        {product.name}
                      </h3>
                    </Link>
                    <div className="flex items-center gap-1.5">
                      <StarRating rating={product.rating} size="sm" />
                      <span className="text-xs text-slate-500 font-medium">({product.reviews})</span>
                    </div>
                    <div className="flex items-center justify-between pt-1">
                      <span className="text-lg font-bold bg-gradient-to-r from-slate-900 to-slate-700 bg-clip-text text-transparent">
                        {product.min_price === product.max_price 
                          ? `$${product.min_price}` 
                          : `$${product.min_price} - $${product.max_price}`}
                      </span>
                      <Link href={`/products/${product.id}`}>
                        <Button size="sm" className="h-8 px-4 rounded-xl bg-gradient-to-r from-blue-500 to-purple-500 hover:from-blue-600 hover:to-purple-600 text-white font-semibold shadow-md shadow-blue-500/25 hover:shadow-lg hover:shadow-blue-500/30 transition-all duration-300">
                          <ShoppingCart className="h-3.5 w-3.5 mr-1.5" />
                          Buy
                        </Button>
                      </Link>
                    </div>
                    <WishlistButton 
                      productId={typeof product.id === 'string' ? parseInt(product.id) : product.id}
                      variant="text"
                      className="w-full justify-center h-8 rounded-xl text-xs"
                      showText={true}
                    />
                  </div>
                </CardContent>
              </Card>
            ))}
          </div>
        )}

        {/* No Results */}
        {!isLoading && !error && filteredProducts.length === 0 && (
          <div className="text-center py-20 bg-white/60 backdrop-blur-sm rounded-3xl">
            <div className="w-20 h-20 bg-slate-100 rounded-full flex items-center justify-center mx-auto mb-6">
              <Search className="w-10 h-10 text-slate-400" />
            </div>
            <h3 className="text-xl font-bold mb-2 text-slate-900">No products found</h3>
            <p className="text-slate-500 mb-6 max-w-md mx-auto">
              {searchTerm
                ? `No products match your search for "${searchTerm}"`
                : "No products found matching your criteria."}
            </p>
            {hasActiveFilters && (
              <Button
                variant="outline"
                onClick={() => {
                  setSearchTerm("")
                  handleCategoryChange("all")
                }}
                className="rounded-xl px-6 py-2.5 font-medium"
              >
                Clear all filters
              </Button>
            )}
          </div>
        )}
      </div>
    </div>
  )
}
