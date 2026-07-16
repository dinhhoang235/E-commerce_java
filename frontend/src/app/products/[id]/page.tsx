"use client"

import { useState, useEffect } from "react"
import { useParams, useRouter } from "next/navigation"
import Link from "next/link"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { Separator } from "@/components/ui/separator"
import { ShoppingCart, Heart, Share2, ChevronLeft, Star, Check, Shield, Truck, Minus, Plus, Loader2 } from "lucide-react"
import { useCart } from "@/components/cart-provider"
import { useAuth } from "@/components/auth-provider"
import { WishlistButton } from "@/components/wishlist-button"
import { ProductGallery } from "@/components/product-gallery"
import { ProductSpecs } from "@/components/product-specs"
import { ProductRecommendations } from "@/components/product-recommendations"
import { getAllProducts, getProductById } from "@/lib/services/products"
import { WriteReviewDialog } from "@/components/write-review-dialog"
import { ReviewList } from "@/components/review-list"
import { StarRating } from "@/components/star-rating"
import { useToast } from "@/hooks/use-toast"
// Define Category interface
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

// Define ProductColor interface
interface ProductColor {
  id: number
  name: string
  hex_code: string
}

// Define ProductVariant interface
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

// Define the Product interface to match backend structure
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

export default function ProductPage() {
  const params = useParams()
  const router = useRouter()
  const { addItem } = useCart()
  const { isAuthenticated } = useAuth()
  const { toast } = useToast()
  const [product, setProduct] = useState<Product | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState("")
  const [selectedColor, setSelectedColor] = useState<string>("")
  const [selectedStorage, setSelectedStorage] = useState<string>("")
  const [quantity, setQuantity] = useState(1)
  const [addingToCart, setAddingToCart] = useState(false)
  const [reviewRefreshTrigger, setReviewRefreshTrigger] = useState(0)
  const [currentPrice, setCurrentPrice] = useState<number>(0)

  // Compute the currently selected variant for stock checks
  const selectedVariant = product?.variants?.find(variant => 
    variant.color.name === selectedColor && variant.storage === selectedStorage
  ) || product?.variants?.find(variant => variant.color.name === selectedColor) || product?.variants?.[0] || null

  const refreshProductData = async () => {
    try {
      const productId = params.id as string
      const foundProduct = await getProductById(productId)
      if (foundProduct) {
        setProduct(foundProduct)
      }
    } catch (err) {
      console.error("Error refreshing product data:", err)
    }
  }

  const updatePrice = (color: string, storage: string, product: Product) => {
    // Find the exact variant based on color and storage
    let selectedVariant = product.variants.find(variant => 
      variant.color.name === color && variant.storage === storage
    );
    
    // If no exact match, find variant with selected color
    if (!selectedVariant && color) {
      selectedVariant = product.variants.find(variant => 
        variant.color.name === color
      );
    }
    
    // If still no match, use the first available variant
    if (!selectedVariant && product.variants.length > 0) {
      selectedVariant = product.variants[0];
    }
    
    const price = selectedVariant ? parseFloat(selectedVariant.price) : product.min_price;
    setCurrentPrice(price);
  }

  // Get available storage options for the selected color
  const getAvailableStoragesForColor = (color: string, product: Product) => {
    if (!color || !product.variants) return product.available_storages || [];
    
    const availableStorages = product.variants
      .filter(variant => variant.color.name === color && variant.is_in_stock)
      .map(variant => variant.storage)
      .filter((storage, index, array) => array.indexOf(storage) === index); // Remove duplicates
    
    // Sort storage options in logical order
    return availableStorages.sort((a, b) => {
      const storageOrder = {
        '128GB': 1,
        '256GB': 2,
        '512GB': 3,
        '1TB': 4,
        '2TB': 5
      };
      
      const aOrder = storageOrder[a as keyof typeof storageOrder] || 999;
      const bOrder = storageOrder[b as keyof typeof storageOrder] || 999;
      
      return aOrder - bOrder;
    });
  }

  const handleReviewChange = () => {
    setReviewRefreshTrigger(prev => prev + 1)
    // Also refresh product data to get updated rating and review count
    refreshProductData()
  }

  useEffect(() => {
    const fetchProduct = async () => {
      try {
        setLoading(true)
        const productId = params.id as string
        const foundProduct = await getProductById(productId)

        if (foundProduct) {
          setProduct(foundProduct)
          const defaultColor = foundProduct.available_colors && foundProduct.available_colors.length > 0 
            ? foundProduct.available_colors[0].name 
            : "";
          
          const availableStoragesForColor = getAvailableStoragesForColor(defaultColor, foundProduct);
          const defaultStorage = availableStoragesForColor.length > 0 
            ? availableStoragesForColor[0] 
            : "";
          
          setSelectedColor(defaultColor)
          setSelectedStorage(defaultStorage)
          
          updatePrice(defaultColor, defaultStorage, foundProduct)
          setLoading(false)
        } else {
          setError("Product not found")
          setLoading(false)
        }
      } catch (err) {
        console.error("Error fetching product:", err)
        setError("Failed to load product. Please try again later.")
        setLoading(false)
      }
    }

    fetchProduct()
  }, [params.id])

  const handleAddToCart = async () => {
    if (!product) return

    if (!isAuthenticated) {
      router.push("/login")
      return
    }

    try {
      setAddingToCart(true)
      
      // Find the selected variant or use the first available variant
      let selectedVariant = product.variants.find(variant => 
        variant.color.name === selectedColor && variant.storage === selectedStorage
      );
      
      // If no exact match, find by color only
      if (!selectedVariant) {
        selectedVariant = product.variants.find(variant => 
          variant.color.name === selectedColor
        );
      }
      
      // If still no match, use the first variant
      if (!selectedVariant && product.variants.length > 0) {
        selectedVariant = product.variants[0];
      }
      
      const variantPrice = currentPrice || (selectedVariant ? parseFloat(selectedVariant.price) : product.min_price);
      
      await addItem({
        id: Number(product.id),
        productId: Number(product.id),
        name: product.name,
        price: variantPrice,
        image: product.image || '',
        color: selectedColor,
        storage: selectedStorage,
        quantity,
      })
      
      toast({
        title: "Added to Cart",
        description: `${product.name} has been added to your cart.`,
      })
    } catch (error) {
      console.error('Failed to add item to cart:', error)
      toast({
        title: "Error",
        description: "Failed to add item to cart. Please try again.",
        variant: "destructive",
      })
    } finally {
      setAddingToCart(false)
    }
  }

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="animate-pulse space-y-8 w-full max-w-4xl px-4">
          <div className="h-8 bg-slate-100 rounded-full w-1/3"></div>
          <div className="grid md:grid-cols-2 gap-12">
            <div className="h-[500px] bg-slate-100 rounded-3xl"></div>
            <div className="space-y-4">
              <div className="h-8 bg-slate-100 rounded-full w-3/4"></div>
              <div className="h-6 bg-slate-100 rounded-full w-1/4"></div>
              <div className="h-4 bg-slate-100 rounded-full w-full"></div>
              <div className="h-4 bg-slate-100 rounded-full w-full"></div>
              <div className="h-4 bg-slate-100 rounded-full w-3/4"></div>
            </div>
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
          <h1 className="text-2xl font-bold mb-2 text-slate-900">Error</h1>
          <p className="text-slate-500 mb-6">{error}</p>
          <Button onClick={() => router.push("/products")} className="bg-gradient-to-r from-blue-600 to-purple-600 rounded-xl px-6 py-2.5 font-semibold shadow-lg shadow-blue-500/25">
            <ChevronLeft className="mr-2 h-4 w-4" /> Back to Products
          </Button>
        </div>
      </div>
    )
  }

  if (!product) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center p-8 bg-white rounded-3xl shadow-xl max-w-md mx-4">
          <div className="w-16 h-16 bg-slate-100 rounded-full flex items-center justify-center mx-auto mb-4">
            <span className="text-2xl">🔍</span>
          </div>
          <h1 className="text-2xl font-bold mb-2 text-slate-900">Product Not Found</h1>
          <p className="text-slate-500 mb-6">Sorry, we couldn&apos;t find the product you&apos;re looking for.</p>
          <Button onClick={() => router.push("/products")} className="bg-gradient-to-r from-blue-600 to-purple-600 rounded-xl px-6 py-2.5 font-semibold shadow-lg shadow-blue-500/25">
            <ChevronLeft className="mr-2 h-4 w-4" /> Back to Products
          </Button>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50/50 via-white to-slate-100/50">
      <div className="container mx-auto px-4 sm:px-6 py-6 sm:py-8">
        {/* Breadcrumb */}
        <div className="inline-flex items-center text-sm text-slate-500 mb-4 bg-white/80 backdrop-blur-sm rounded-xl px-4 py-2.5 shadow-sm">
          <Link href="/" className="hover:text-blue-600 transition-colors">Home</Link>
          <span className="mx-2 text-slate-300">/</span>
          <Link href="/products" className="hover:text-blue-600 transition-colors">Products</Link>
          <span className="mx-2 text-slate-300">/</span>
          {product.category && (
            <>
              <Link href={`/products?category=${product.category.slug}`} className="hover:text-blue-600 transition-colors capitalize">
                {product.category.name}
              </Link>
              <span className="mx-2 text-slate-300">/</span>
            </>
          )}
          <span className="text-slate-900 font-medium">{product.name}</span>
        </div>

        {/* Back Button - Mobile only */}
        <Button variant="ghost" onClick={() => router.back()} className="mb-6 rounded-xl hover:bg-slate-100 transition-colors md:hidden">
          <ChevronLeft className="mr-2 h-4 w-4" /> Back
        </Button>

        <div className="grid lg:grid-cols-2 gap-6 sm:gap-8 lg:gap-12">
          {/* Product Gallery */}
          <ProductGallery product={product} />

          {/* Product Info */}
          <div className="space-y-6">
            {/* Title and Badges */}
            <div>
              {product.badge && (
                <Badge className="mb-3 bg-gradient-to-r from-red-500 to-rose-500 text-white font-bold px-4 py-1.5 rounded-full shadow-lg shadow-red-500/25">
                  {product.badge}
                </Badge>
              )}
              <h1 className="text-2xl sm:text-3xl lg:text-4xl font-bold tracking-tight text-slate-900">{product.name}</h1>

              <div className="flex items-center gap-3 mt-3">
                <StarRating rating={product.rating} size="md" />
                <span className="text-sm text-slate-500 font-medium">
                  {product.rating} ({product.reviews} reviews)
                </span>
              </div>
            </div>

            {/* Price */}
            <div className="flex items-center gap-4">
              <span className="text-3xl sm:text-4xl font-bold bg-gradient-to-r from-blue-600 to-purple-600 bg-clip-text text-transparent">
                ${currentPrice || product.min_price}
              </span>
              {product.min_price !== product.max_price && (
                <span className="text-sm text-slate-500 bg-slate-100 px-3 py-1 rounded-full">
                  ${product.min_price} - ${product.max_price}
                </span>
              )}
            </div>

            {/* Short Description */}
            <p className="text-slate-600 leading-relaxed">{product.description}</p>

            <Separator className="bg-slate-100" />

            {/* Color Options */}
            {product.available_colors && product.available_colors.length > 0 && (
              <div className="space-y-3">
                <h3 className="font-semibold text-slate-900">Color</h3>
                <div className="flex flex-wrap gap-3">
                  {Array.from(new Map(product.available_colors.map(color => [color.id, color])).values()).map((color: ProductColor) => (
                    <Button
                      key={color.id}
                      variant="outline"
                      className={`rounded-full w-12 h-12 p-0 border-2 transition-all duration-200 ${
                        selectedColor === color.name 
                          ? "border-blue-600 ring-2 ring-blue-600/20 scale-110" 
                          : "border-slate-200 hover:border-slate-300"
                      }`}
                      style={{ backgroundColor: color.hex_code }}
                      onClick={() => {
                        const newColor = color.name;
                        const availableStoragesForNewColor = getAvailableStoragesForColor(newColor, product);
                        let newStorage = selectedStorage;
                        if (!availableStoragesForNewColor.includes(selectedStorage)) {
                          newStorage = availableStoragesForNewColor.length > 0 ? availableStoragesForNewColor[0] : "";
                        }
                        setSelectedColor(newColor)
                        setSelectedStorage(newStorage)
                        updatePrice(newColor, newStorage, product)
                      }}
                    >
                      {selectedColor === color.name && <Check className="h-4 w-4 text-white drop-shadow" />}
                    </Button>
                  ))}
                </div>
              </div>
            )}

            {/* Storage Options */}
            {(() => {
              const availableStoragesForSelectedColor = getAvailableStoragesForColor(selectedColor, product);
              return availableStoragesForSelectedColor.length > 0 && (
                <div className="space-y-3">
                  <h3 className="font-semibold text-slate-900">Storage</h3>
                  <div className="flex flex-wrap gap-3">
                    {availableStoragesForSelectedColor.map((size: string) => (
                      <Button
                        key={size}
                        variant={selectedStorage === size ? "default" : "outline"}
                        className={`h-11 px-6 rounded-xl font-medium transition-all duration-200 ${
                          selectedStorage === size 
                            ? "bg-gradient-to-r from-blue-600 to-purple-600 text-white shadow-lg shadow-blue-500/25" 
                            : "border-slate-200 hover:border-blue-300 hover:bg-blue-50"
                        }`}
                        onClick={() => {
                          setSelectedStorage(size)
                          updatePrice(selectedColor, size, product)
                        }}
                      >
                        {size}
                      </Button>
                    ))}
                  </div>
                </div>
              );
            })()}

            {/* Quantity */}
            <div className="space-y-3">
              <h3 className="font-semibold text-slate-900">Quantity</h3>
              <div className="flex items-center gap-4">
                <div className="flex items-center bg-slate-50 rounded-xl border border-slate-200/60">
                  <Button
                    variant="ghost"
                    size="icon"
                    className="h-11 w-11 rounded-l-xl hover:bg-slate-100"
                    onClick={() => setQuantity(Math.max(1, quantity - 1))}
                    disabled={quantity <= 1}
                  >
                    <Minus className="h-4 w-4" />
                  </Button>
                  <span className="w-12 text-center font-semibold">{quantity}</span>
                  <Button
                    variant="ghost"
                    size="icon"
                    className="h-11 w-11 rounded-r-xl hover:bg-slate-100"
                    onClick={() => {
                      const maxStock = selectedVariant?.stock ?? selectedVariant?.total_stock ?? 999
                      setQuantity(Math.min(quantity + 1, maxStock))
                    }}
                    disabled={quantity >= (selectedVariant?.stock ?? selectedVariant?.total_stock ?? 999)}
                  >
                    <Plus className="h-4 w-4" />
                  </Button>
                </div>
                {selectedVariant && selectedVariant.stock <= 5 && selectedVariant.stock > 0 && (
                  <Badge className="bg-orange-100 text-orange-600 rounded-full">
                    Only {selectedVariant.stock} left
                  </Badge>
                )}
              </div>
            </div>

            {/* Add to Cart and Wishlist */}
            <div className="flex flex-col sm:flex-row gap-3 sm:gap-4 pt-2">
              <Button 
                size="lg" 
                className="h-12 sm:h-14 flex-1 min-h-[48px] sm:min-h-[56px] bg-gradient-to-r from-blue-600 to-purple-600 hover:from-blue-700 hover:to-purple-700 text-base sm:text-lg font-semibold rounded-xl sm:rounded-2xl shadow-lg shadow-blue-500/25 hover:shadow-xl hover:shadow-blue-500/30 transition-all duration-300 btn-touch" 
                onClick={handleAddToCart} 
                disabled={addingToCart}
              >
                {addingToCart ? (
                  <Loader2 className="mr-2 h-5 w-5 animate-spin" />
                ) : (
                  <ShoppingCart className="mr-2 h-5 w-5 group-hover:scale-110 transition-transform" />
                )}
                {addingToCart ? "Adding..." : "Add to Cart"}
              </Button>
              <WishlistButton 
                productId={typeof product.id === 'string' ? parseInt(product.id) : product.id} 
                variant="default" 
                size="lg" 
                className="h-12 sm:h-14 flex-1 min-h-[48px] sm:min-h-[56px] rounded-xl sm:rounded-2xl text-base sm:text-lg btn-touch"
              />
            </div>

            {/* Delivery and Returns */}
            <div className="bg-gradient-to-r from-slate-50 to-slate-100/50 p-4 sm:p-5 rounded-2xl space-y-3 sm:space-y-4">
              <div className="flex items-start gap-3">
                <div className="w-10 h-10 bg-blue-100 rounded-xl flex items-center justify-center flex-shrink-0">
                  <Truck className="h-5 w-5 text-blue-600" />
                </div>
                <div>
                  <h4 className="font-semibold text-slate-900">Free Delivery</h4>
                  <p className="text-sm text-slate-500">Free standard shipping on orders over $99</p>
                </div>
              </div>
              <div className="flex items-start gap-3">
                <div className="w-10 h-10 bg-green-100 rounded-xl flex items-center justify-center flex-shrink-0">
                  <Shield className="h-5 w-5 text-green-600" />
                </div>
                <div>
                  <h4 className="font-semibold text-slate-900">1-Year Warranty</h4>
                  <p className="text-sm text-slate-500">All products come with a 1-year warranty</p>
                </div>
              </div>
            </div>

            {/* Share */}
            <div className="flex items-center gap-4 pt-2">
              <span className="text-sm text-slate-500 font-medium">Share:</span>
              <Button variant="ghost" size="sm" className="rounded-xl p-2 h-auto hover:bg-slate-100">
                <Share2 className="h-4 w-4 text-slate-500" />
              </Button>
            </div>
          </div>
        </div>

        {/* Product Details Tabs */}
        <div className="mt-10 sm:mt-16">
          <Tabs defaultValue="description">
            <TabsList className="grid w-full grid-cols-3 bg-slate-100/80 rounded-2xl p-1">
              <TabsTrigger value="description" className="rounded-xl font-medium text-xs sm:text-sm">Description</TabsTrigger>
              <TabsTrigger value="specifications" className="rounded-xl font-medium text-xs sm:text-sm">Specs</TabsTrigger>
              <TabsTrigger value="reviews" className="rounded-xl font-medium text-xs sm:text-sm">Reviews</TabsTrigger>
            </TabsList>
            <TabsContent value="description" className="p-4 sm:p-8 bg-white rounded-2xl mt-4 shadow-sm">
              <div className="prose max-w-none">
                <h3 className="text-xl font-bold text-slate-900 mb-4">About {product.name}</h3>
                <p className="text-slate-600 leading-relaxed">{product.full_description || product.description}</p>

                {product.features && product.features.length > 0 && (
                  <>
                    <h4 className="text-lg font-semibold text-slate-900 mt-6 mb-3">Key Features</h4>
                    <ul className="space-y-2">
                      {product.features.map((feature: string, index: number) => (
                        <li key={index} className="flex items-start gap-2 text-slate-600">
                          <Check className="h-4 w-4 text-green-500 mt-1 flex-shrink-0" />
                          {feature}
                        </li>
                      ))}
                    </ul>
                  </>
                )}
              </div>
            </TabsContent>
            <TabsContent value="specifications" className="p-4 sm:p-8 bg-white rounded-2xl mt-4 shadow-sm">
              <ProductSpecs product={product} />
            </TabsContent>
            <TabsContent value="reviews" className="p-4 sm:p-8 bg-white rounded-2xl mt-4 shadow-sm">
              <div className="space-y-4 sm:space-y-6">
                <div className="flex items-center justify-between">
                  <div>
                    <h3 className="text-2xl sm:text-3xl font-bold text-slate-900">{product.rating}</h3>
                    <StarRating rating={product.rating} size="lg" />
                    <p className="text-sm text-slate-500 mt-1 font-medium">Based on {product.reviews} reviews</p>
                  </div>
                  <WriteReviewDialog 
                    productId={Number(product.id)} 
                    productName={product.name}
                    onReviewSubmitted={handleReviewChange}
                  />
                </div>

                <Separator className="bg-slate-100" />

                <ReviewList 
                  productId={Number(product.id)} 
                  refreshTrigger={reviewRefreshTrigger}
                />
              </div>
            </TabsContent>
          </Tabs>
        </div>

        {/* Product Recommendations */}
        <ProductRecommendations productId={product.id} />
      </div>
    </div>
  )
}
