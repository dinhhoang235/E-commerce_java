"use client"

import { useState, useEffect } from "react"
import Link from "next/link"
import { Button } from "@/components/ui/button"
import { Card, CardContent } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { ShoppingCart, Star, Truck, Shield, Headphones, ArrowRight, Sparkles, Zap, Heart } from "lucide-react"
import { Skeleton } from "@/components/ui/skeleton"
import { getAllProducts, getTopSellers, getNewArrivals, getPersonalizedRecommendations } from "@/lib/services/products"
import { getAllCategories } from "@/lib/services/categories"
import { StarRating } from "@/components/star-rating"
import { formatImageUrl } from "@/lib/utils/image"

interface Category {
  id: string | number
  name: string
  slug: string
  description?: string
  image?: string
  product_count?: number
  is_active?: boolean
}

interface Product {
  id: string | number
  name: string
  description?: string
  min_price: number
  max_price: number
  image?: string
  category: string | Category
  rating: number
  reviews: number
  badge?: string
  total_stock?: number
}

export default function HomePage() {
  const [categories, setCategories] = useState<Category[]>([])
  const [featuredProducts, setFeaturedProducts] = useState<Product[]>([])
  const [topSellers, setTopSellers] = useState<Product[]>([])
  const [newArrivals, setNewArrivals] = useState<Product[]>([])
  const [personalizedRecommendations, setPersonalizedRecommendations] = useState<Product[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    const fetchData = async () => {
      try {
        setIsLoading(true)
        setError(null)

        const [categoriesResponse, productsResponse, topSellersResponse, newArrivalsResponse, personalizedResponse] = await Promise.all([
          getAllCategories(),
          getAllProducts(),
          getTopSellers(),
          getNewArrivals(),
          getPersonalizedRecommendations()
        ])

        let categoriesData: Category[] = []
        if (Array.isArray(categoriesResponse)) {
          categoriesData = categoriesResponse
        } else if (categoriesResponse && Array.isArray(categoriesResponse.results)) {
          categoriesData = categoriesResponse.results
        }

        const activeCategories = categoriesData
          .filter((cat: Category) => cat.is_active !== false)
          .slice(0, 3)
        
        setCategories(activeCategories)

        let productsData: Product[] = []
        if (Array.isArray(productsResponse)) {
          productsData = productsResponse
        } else if (productsResponse && Array.isArray(productsResponse.results)) {
          productsData = productsResponse.results
        }

        const featured = productsData
          .filter((product: Product) => (product.badge || product.rating >= 4.5) && (product.total_stock ?? 0) > 0)
          .slice(0, 3)
        
        setFeaturedProducts(featured)

        setTopSellers(Array.isArray(topSellersResponse) ? topSellersResponse.slice(0, 6) : [])
        setNewArrivals(Array.isArray(newArrivalsResponse) ? newArrivalsResponse.slice(0, 6) : [])
        setPersonalizedRecommendations(Array.isArray(personalizedResponse) ? personalizedResponse.slice(0, 6) : [])

      } catch (err) {
        console.error("Error fetching data:", err)
        setError("Failed to load data. Please try again later.")
      } finally {
        setIsLoading(false)
      }
    }

    fetchData()
  }, [])

  const formatPrice = (product: Product) => {
    if (product.min_price === product.max_price) {
      return `$${product.min_price}`
    }
    return `$${product.min_price} - $${product.max_price}`
  }

  if (error) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-slate-50 to-slate-100">
        <div className="text-center p-8 bg-white rounded-3xl shadow-xl shadow-black/5 max-w-md mx-4">
          <div className="w-16 h-16 bg-red-100 rounded-full flex items-center justify-center mx-auto mb-4">
            <span className="text-2xl">⚠️</span>
          </div>
          <h2 className="text-2xl font-bold text-slate-900 mb-2">Oops!</h2>
          <p className="text-slate-600 mb-6">{error}</p>
          <Button 
            onClick={() => window.location.reload()} 
            className="bg-gradient-to-r from-blue-600 to-purple-600 hover:from-blue-700 hover:to-purple-700 rounded-xl px-8 py-3 font-semibold shadow-lg shadow-blue-500/25"
          >
            Try Again
          </Button>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen">
      {/* Hero Section */}
      <section className="relative bg-gradient-hero text-white overflow-hidden">
        {/* Animated background elements */}
        <div className="absolute inset-0 overflow-hidden">
          <div className="absolute -top-40 -right-40 w-60 h-60 sm:w-80 sm:h-80 bg-purple-500/20 rounded-full blur-3xl animate-float"></div>
          <div className="absolute -bottom-40 -left-40 w-60 h-60 sm:w-80 sm:h-80 bg-blue-500/20 rounded-full blur-3xl animate-float-slow"></div>
          <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-64 h-64 sm:w-96 sm:h-96 bg-indigo-500/10 rounded-full blur-3xl animate-pulse-soft"></div>
          <div className="absolute inset-0 opacity-5" style={{
            backgroundImage: 'linear-gradient(rgba(255,255,255,0.1) 1px, transparent 1px), linear-gradient(90deg, rgba(255,255,255,0.1) 1px, transparent 1px)',
            backgroundSize: '50px 50px'
          }}></div>
        </div>
        
        <div className="container mx-auto px-4 sm:px-6 py-12 sm:py-20 lg:py-32 relative z-10">
          <div className="grid lg:grid-cols-2 gap-8 lg:gap-12 items-center">
            <div className="space-y-6 sm:space-y-8 animate-fade-in-up text-center lg:text-left">
              <div className="space-y-4 sm:space-y-6">
                <Badge 
                  variant="secondary" 
                  className="bg-white/10 text-white hover:bg-white/20 text-xs sm:text-sm px-4 sm:px-5 py-1.5 sm:py-2 rounded-full border border-white/20 backdrop-blur-sm"
                >
                  <Sparkles className="w-3.5 h-3.5 sm:w-4 sm:h-4 mr-1.5 sm:mr-2" />
                  {featuredProducts.length > 0 && featuredProducts[0].badge ? featuredProducts[0].badge : "New Arrivals"}
                </Badge>
                
                <h1 className="text-3xl sm:text-4xl lg:text-6xl font-bold leading-tight tracking-tight">
                  {featuredProducts.length > 0 ? (
                    <>
                      Discover the
                      <span className="block mt-1 sm:mt-2 bg-gradient-to-r from-blue-400 via-purple-400 to-pink-400 bg-clip-text text-transparent">
                        {featuredProducts[0].name}
                      </span>
                    </>
                  ) : (
                    <>
                      The Future of
                      <span className="block mt-1 sm:mt-2 bg-gradient-to-r from-blue-400 via-purple-400 to-pink-400 bg-clip-text text-transparent">
                        Technology
                      </span>
                    </>
                  )}
                </h1>
                
                <p className="text-base sm:text-lg lg:text-xl text-slate-300/90 leading-relaxed max-w-lg mx-auto lg:mx-0">
                  {featuredProducts.length > 0 && featuredProducts[0].description ? 
                    featuredProducts[0].description :
                    "Experience innovation at its finest. Discover cutting-edge technology and elegant design."
                  }
                </p>
                
                {featuredProducts.length > 0 && (
                  <div className="flex items-center gap-3 sm:gap-4 justify-center lg:justify-start">
                    <span className="text-3xl sm:text-4xl font-bold bg-gradient-to-r from-white to-white/80 bg-clip-text text-transparent">
                      {formatPrice(featuredProducts[0])}
                    </span>
                    <div className="flex items-center gap-1 px-3 py-1 bg-green-500/20 rounded-full">
                      <Zap className="w-3.5 h-3.5 sm:w-4 sm:h-4 text-green-400" />
                      <span className="text-xs sm:text-sm text-green-300">In Stock</span>
                    </div>
                  </div>
                )}
              </div>
              
              <div className="flex flex-col sm:flex-row gap-3 sm:gap-4 justify-center lg:justify-start">
                <Link href="/products" className="w-full sm:w-auto">
                  <Button 
                    size="lg" 
                    className="w-full sm:w-auto bg-white text-slate-900 hover:bg-white/90 text-base sm:text-lg px-6 sm:px-8 py-3 sm:py-4 rounded-2xl font-bold shadow-xl shadow-black/20 hover:shadow-2xl hover:shadow-black/30 transition-all duration-300 btn-touch"
                  >
                    Shop Now
                    <ArrowRight className="w-4 h-4 sm:w-5 sm:h-5 ml-2 group-hover:translate-x-1 transition-transform" />
                  </Button>
                </Link>
                {featuredProducts.length > 0 && (
                  <Link href={`/products/${featuredProducts[0].id}`} className="w-full sm:w-auto">
                    <Button
                      size="lg"
                      variant="outline"
                      className="w-full sm:w-auto text-base sm:text-lg px-6 sm:px-8 py-3 sm:py-4 bg-transparent text-white border-white/30 hover:bg-white/10 hover:border-white/50 rounded-2xl font-semibold backdrop-blur-sm transition-all duration-300 btn-touch"
                    >
                      View Product
                    </Button>
                  </Link>
                )}
              </div>
            </div>
            
            <div className="relative flex justify-center lg:justify-end animate-fade-in-up order-first lg:order-last" style={{ animationDelay: '0.2s' }}>
              <div className="relative">
                <div className="absolute inset-0 bg-gradient-to-r from-blue-500/30 to-purple-500/30 rounded-3xl blur-3xl animate-glow-pulse"></div>
                
                {/* Mobile: smaller image, hide floating badges */}
                <div className="relative z-10 w-[220px] h-[220px] sm:w-[300px] sm:h-[300px] lg:w-[500px] lg:h-[500px]">
                  <img
                    src={
                      featuredProducts.length > 0 
                        ? formatImageUrl(featuredProducts[0].image)
                        : categories.length > 0 
                        ? formatImageUrl(categories[0].image)
                        : "/placeholder.svg?height=500&width=500"
                    }
                    alt={featuredProducts.length > 0 ? featuredProducts[0].name : "Latest Technology"}
                    className="w-full h-full object-contain drop-shadow-2xl animate-float-slow"
                  />
                </div>
                
                {/* Floating badges - hidden on mobile */}
                <div className="hidden sm:block absolute top-10 left-10 bg-white/90 backdrop-blur-sm rounded-2xl px-4 py-3 shadow-lg animate-float" style={{ animationDelay: '1s' }}>
                  <div className="flex items-center gap-2">
                    <div className="w-8 h-8 bg-green-100 rounded-full flex items-center justify-center">
                      <span className="text-green-600">✓</span>
                    </div>
                    <span className="text-sm font-semibold text-slate-900">Verified</span>
                  </div>
                </div>
                
                <div className="hidden sm:block absolute bottom-10 right-10 bg-white/90 backdrop-blur-sm rounded-2xl px-4 py-3 shadow-lg animate-float" style={{ animationDelay: '2s' }}>
                  <div className="flex items-center gap-2">
                    <div className="flex -space-x-1">
                      {[1, 2, 3, 4, 5].map((i) => (
                        <Star key={i} className="w-4 h-4 fill-yellow-400 text-yellow-400" />
                      ))}
                    </div>
                    <span className="text-sm font-semibold text-slate-900">4.9</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Categories Section */}
      <section className="section-mobile bg-gradient-to-b from-slate-50 to-white relative overflow-hidden">
        <div className="absolute inset-0 mesh-gradient-1 opacity-50"></div>
        <div className="container mx-auto px-4 sm:px-6 relative z-10">
          <div className="text-center mb-10 sm:mb-16">
            <Badge variant="secondary" className="bg-blue-100 text-blue-700 mb-3 sm:mb-4 px-4 py-1.5 rounded-full text-xs sm:text-sm">
              Categories
            </Badge>
            <h2 className="text-2xl sm:text-4xl lg:text-5xl font-bold mb-3 sm:mb-4 tracking-tight">
              Shop by{' '}
              <span className="bg-gradient-to-r from-blue-600 to-purple-600 bg-clip-text text-transparent">
                Category
              </span>
            </h2>
            <p className="text-slate-600 text-sm sm:text-lg max-w-2xl mx-auto">
              Find the perfect device for your needs
            </p>
          </div>
          
          {isLoading ? (
            <div className="flex gap-4 sm:gap-6 overflow-hidden md:grid md:grid-cols-3 max-w-6xl mx-auto">
              {Array.from({ length: 3 }).map((_, i) => (
                <div key={i} className="flex-shrink-0 w-[240px] sm:w-auto">
                  <div className="rounded-2xl sm:rounded-3xl border border-slate-100 bg-white overflow-hidden">
                    <Skeleton className="h-40 sm:h-48" />
                    <div className="p-4 sm:p-6 text-center space-y-2">
                      <Skeleton className="h-5 w-24 mx-auto" />
                      <Skeleton className="h-3 w-20 mx-auto" />
                    </div>
                  </div>
                </div>
              ))}
            </div>
          ) : (
            /* Mobile: horizontal scroll, Desktop: grid */
            <div className="flex gap-4 sm:gap-6 overflow-x-auto scroll-mobile pb-4 sm:pb-0 md:grid md:grid-cols-3 md:overflow-visible max-w-6xl mx-auto snap-x snap-mandatory sm:snap-none">
              {categories.map((category, index) => (
                <Link key={category.id} href={`/products?category=${category.slug}`} className="flex-shrink-0 w-[240px] sm:w-auto snap-start">
                  <Card 
                    className="group glass-card hover:shadow-card-hover transition-all duration-500 hover:-translate-y-1 sm:hover:-translate-y-2 rounded-2xl sm:rounded-3xl overflow-hidden border-0 h-full"
                  >
                    <CardContent className="p-0">
                      <div className="relative bg-gradient-to-br from-slate-50 to-slate-100 p-5 sm:p-8 text-center">
                        <div className="absolute inset-0 bg-gradient-to-br from-blue-500/5 to-purple-500/5 opacity-0 group-hover:opacity-100 transition-opacity duration-500"></div>
                        <div className="relative mb-4 sm:mb-6">
                          <div className="absolute inset-0 bg-gradient-to-br from-blue-100 to-purple-100 rounded-2xl blur-xl opacity-60 group-hover:opacity-100 transition-opacity duration-500"></div>
                          <img
                            src={formatImageUrl(category.image)}
                            alt={category.name}
                            className="relative z-10 mx-auto w-[120px] h-[120px] sm:w-[160px] sm:h-[160px] object-contain group-hover:scale-110 transition-transform duration-500"
                          />
                        </div>
                      </div>
                      <div className="p-4 sm:p-6 text-center bg-white">
                        <h3 className="text-base sm:text-xl font-bold mb-1 sm:mb-2 text-slate-900 group-hover:text-blue-600 transition-colors">
                          {category.name}
                        </h3>
                        <p className="text-xs sm:text-slate-500 font-medium">
                          {category.product_count ? `${category.product_count} products` : 'Browse products'}
                        </p>
                      </div>
                    </CardContent>
                  </Card>
                </Link>
              ))}
            </div>
          )}
        </div>
      </section>

      {/* Top Sellers Section */}
      <section className="py-24 bg-white relative">
        <div className="container mx-auto px-4">
          <div className="text-center mb-16">
            <Badge variant="secondary" className="bg-orange-100 text-orange-700 mb-4 px-4 py-1.5 rounded-full">
              🔥 Best Sellers
            </Badge>
            <h2 className="text-4xl lg:text-5xl font-bold mb-4 tracking-tight">
              Top Sellers
            </h2>
            <p className="text-slate-600 text-lg max-w-2xl mx-auto">
              Our best-selling products loved by customers
            </p>
          </div>
          
          {isLoading ? (
            <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-8 max-w-7xl mx-auto">
              {[...Array(6)].map((_, i) => (
                <div key={i} className="animate-pulse">
                  <div className="bg-slate-100 aspect-square rounded-3xl mb-4"></div>
                  <div className="bg-slate-100 h-6 rounded-full mb-3 w-3/4"></div>
                  <div className="bg-slate-100 h-4 rounded-full mb-3 w-1/2"></div>
                  <div className="bg-slate-100 h-10 rounded-xl"></div>
                </div>
              ))}
            </div>
          ) : (
            <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-8 max-w-7xl mx-auto">
              {topSellers.map((product, index) => (
                <Card 
                  key={product.id} 
                  className="group glass-card hover:shadow-card-hover transition-all duration-500 hover:-translate-y-2 rounded-3xl overflow-hidden border-0"
                >
                  <CardContent className="p-0">
                    <div className="relative bg-white">
                      {index < 3 && (
                        <Badge className="absolute top-4 left-4 z-10 bg-gradient-to-r from-amber-400 to-orange-500 text-white font-bold px-4 py-1.5 rounded-full shadow-lg shadow-orange-500/25">
                          #{index + 1} Best Seller
                        </Badge>
                      )}
                      <Link href={`/products/${product.id}`}>
                        <div className="img-zoom rounded-2xl">
                          <img
                            src={formatImageUrl(product.image)}
                            alt={product.name}
                            className="w-full h-64 object-contain"
                          />
                        </div>
                      </Link>
                    </div>
                    <div className="p-6 bg-white space-y-4">
                      <Link href={`/products/${product.id}`}>
                        <h3 className="text-lg font-bold hover:text-orange-600 transition-colors leading-tight line-clamp-2">
                          {product.name}
                        </h3>
                      </Link>
                      <div className="flex items-center gap-2">
                        <StarRating rating={product.rating} size="md" />
                        <span className="text-sm text-slate-500 font-medium">({product.reviews || 0})</span>
                      </div>
                      <div className="flex items-center justify-between">
                        <span className="text-2xl font-bold bg-gradient-to-r from-slate-900 to-slate-700 bg-clip-text text-transparent">
                          {formatPrice(product)}
                        </span>
                      </div>
                      <Link href={`/products/${product.id}`}>
                        <Button 
                          className="w-full bg-gradient-to-r from-orange-500 to-red-500 hover:from-orange-600 hover:to-red-600 text-white font-semibold py-3 rounded-xl transition-all duration-300 shadow-lg shadow-orange-500/25 hover:shadow-xl hover:shadow-orange-500/30 group"
                        >
                          <ShoppingCart className="w-4 h-4 mr-2 group-hover:scale-110 transition-transform" />
                          Buy Now
                        </Button>
                      </Link>
                    </div>
                  </CardContent>
                </Card>
              ))}
            </div>
          )}
          
          {!isLoading && topSellers.length === 0 && (
            <div className="text-center py-16 bg-slate-50 rounded-3xl">
              <div className="w-16 h-16 bg-slate-200 rounded-full flex items-center justify-center mx-auto mb-4">
                <ShoppingCart className="w-8 h-8 text-slate-400" />
              </div>
              <p className="text-slate-600 text-lg mb-6">No top sellers available at the moment.</p>
              <Link href="/products">
                <Button className="bg-gradient-to-r from-orange-500 to-red-500 hover:from-orange-600 hover:to-red-600 px-8 py-3 rounded-xl font-semibold shadow-lg shadow-orange-500/25">
                  Browse All Products
                </Button>
              </Link>
            </div>
          )}
        </div>
      </section>

      {/* New Arrivals Section */}
      <section className="py-24 bg-gradient-to-br from-blue-50/50 via-purple-50/50 to-pink-50/50 relative overflow-hidden">
        <div className="absolute inset-0 mesh-gradient-2 opacity-30"></div>
        <div className="container mx-auto px-4 relative z-10">
          <div className="text-center mb-16">
            <Badge variant="secondary" className="bg-purple-100 text-purple-700 mb-4 px-4 py-1.5 rounded-full">
              ✨ Just Arrived
            </Badge>
            <h2 className="text-4xl lg:text-5xl font-bold mb-4 tracking-tight">
              New Arrivals
            </h2>
            <p className="text-slate-600 text-lg max-w-2xl mx-auto">
              Fresh products just added to our collection
            </p>
          </div>
          
          {isLoading ? (
            <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-8 max-w-7xl mx-auto">
              {[...Array(6)].map((_, i) => (
                <div key={i} className="animate-pulse">
                  <div className="bg-white/60 aspect-square rounded-3xl mb-4"></div>
                  <div className="bg-white/60 h-6 rounded-full mb-3 w-3/4"></div>
                  <div className="bg-white/60 h-4 rounded-full mb-3 w-1/2"></div>
                  <div className="bg-white/60 h-10 rounded-xl"></div>
                </div>
              ))}
            </div>
          ) : (
            <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-8 max-w-7xl mx-auto">
              {newArrivals.map((product) => (
                <Card 
                  key={product.id} 
                  className="group glass-card hover:shadow-card-hover transition-all duration-500 hover:-translate-y-2 rounded-3xl overflow-hidden border-0"
                >
                  <CardContent className="p-0">
                    <div className="relative bg-white">
                      <Badge className="absolute top-4 left-4 z-10 bg-gradient-to-r from-blue-500 to-purple-500 text-white font-bold px-4 py-1.5 rounded-full shadow-lg shadow-purple-500/25">
                        NEW
                      </Badge>
                      <Link href={`/products/${product.id}`}>
                        <div className="img-zoom rounded-2xl">
                          <img
                            src={formatImageUrl(product.image)}
                            alt={product.name}
                            className="w-full h-64 object-contain"
                          />
                        </div>
                      </Link>
                    </div>
                    <div className="p-6 bg-white space-y-4">
                      <Link href={`/products/${product.id}`}>
                        <h3 className="text-lg font-bold hover:text-purple-600 transition-colors leading-tight line-clamp-2">
                          {product.name}
                        </h3>
                      </Link>
                      <div className="flex items-center gap-2">
                        <StarRating rating={product.rating} size="md" />
                        <span className="text-sm text-slate-500 font-medium">({product.reviews || 0})</span>
                      </div>
                      <div className="flex items-center justify-between">
                        <span className="text-2xl font-bold bg-gradient-to-r from-slate-900 to-slate-700 bg-clip-text text-transparent">
                          {formatPrice(product)}
                        </span>
                      </div>
                      <Link href={`/products/${product.id}`}>
                        <Button 
                          className="w-full bg-gradient-to-r from-blue-500 to-purple-500 hover:from-blue-600 hover:to-purple-600 text-white font-semibold py-3 rounded-xl transition-all duration-300 shadow-lg shadow-purple-500/25 hover:shadow-xl hover:shadow-purple-500/30 group"
                        >
                          <ShoppingCart className="w-4 h-4 mr-2 group-hover:scale-110 transition-transform" />
                          Buy Now
                        </Button>
                      </Link>
                    </div>
                  </CardContent>
                </Card>
              ))}
            </div>
          )}
          
          {!isLoading && newArrivals.length === 0 && (
            <div className="text-center py-16 bg-white/60 backdrop-blur-sm rounded-3xl">
              <div className="w-16 h-16 bg-purple-100 rounded-full flex items-center justify-center mx-auto mb-4">
                <Sparkles className="w-8 h-8 text-purple-400" />
              </div>
              <p className="text-slate-600 text-lg mb-6">No new arrivals available at the moment.</p>
              <Link href="/products">
                <Button className="bg-gradient-to-r from-blue-500 to-purple-500 hover:from-blue-600 hover:to-purple-600 px-8 py-3 rounded-xl font-semibold shadow-lg shadow-purple-500/25">
                  Browse All Products
                </Button>
              </Link>
            </div>
          )}
        </div>
      </section>

      {/* Personalized Recommendations Section */}
      <section className="py-24 bg-white relative">
        <div className="container mx-auto px-4">
          <div className="text-center mb-16">
            <Badge variant="secondary" className="bg-pink-100 text-pink-700 mb-4 px-4 py-1.5 rounded-full">
              💖 For You
            </Badge>
            <h2 className="text-4xl lg:text-5xl font-bold mb-4 tracking-tight">
              Recommended for You
            </h2>
            <p className="text-slate-600 text-lg max-w-2xl mx-auto">
              Curated products that match your interests
            </p>
          </div>
          
          {isLoading ? (
            <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-8 max-w-7xl mx-auto">
              {[...Array(6)].map((_, i) => (
                <div key={i} className="animate-pulse">
                  <div className="bg-slate-100 aspect-square rounded-3xl mb-4"></div>
                  <div className="bg-slate-100 h-6 rounded-full mb-3 w-3/4"></div>
                  <div className="bg-slate-100 h-4 rounded-full mb-3 w-1/2"></div>
                  <div className="bg-slate-100 h-10 rounded-xl"></div>
                </div>
              ))}
            </div>
          ) : (
            <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-8 max-w-7xl mx-auto">
              {personalizedRecommendations.map((product) => (
                <Card 
                  key={product.id} 
                  className="group glass-card hover:shadow-card-hover transition-all duration-500 hover:-translate-y-2 rounded-3xl overflow-hidden border-0"
                >
                  <CardContent className="p-0">
                    <div className="relative bg-white">
                      <Badge className="absolute top-4 left-4 z-10 bg-gradient-to-r from-pink-500 to-rose-500 text-white font-bold px-4 py-1.5 rounded-full shadow-lg shadow-pink-500/25">
                        <Heart className="w-3 h-3 inline mr-1" />
                        For You
                      </Badge>
                      <Link href={`/products/${product.id}`}>
                        <div className="img-zoom rounded-2xl">
                          <img
                            src={formatImageUrl(product.image)}
                            alt={product.name}
                            className="w-full h-64 object-contain"
                          />
                        </div>
                      </Link>
                    </div>
                    <div className="p-6 bg-white space-y-4">
                      <Link href={`/products/${product.id}`}>
                        <h3 className="text-lg font-bold hover:text-pink-600 transition-colors leading-tight line-clamp-2">
                          {product.name}
                        </h3>
                      </Link>
                      <div className="flex items-center gap-2">
                        <StarRating rating={product.rating} size="md" />
                        <span className="text-sm text-slate-500 font-medium">({product.reviews || 0})</span>
                      </div>
                      <div className="flex items-center justify-between">
                        <span className="text-2xl font-bold bg-gradient-to-r from-slate-900 to-slate-700 bg-clip-text text-transparent">
                          {formatPrice(product)}
                        </span>
                      </div>
                      <Link href={`/products/${product.id}`}>
                        <Button 
                          className="w-full bg-gradient-to-r from-pink-500 to-rose-500 hover:from-pink-600 hover:to-rose-600 text-white font-semibold py-3 rounded-xl transition-all duration-300 shadow-lg shadow-pink-500/25 hover:shadow-xl hover:shadow-pink-500/30 group"
                        >
                          <ShoppingCart className="w-4 h-4 mr-2 group-hover:scale-110 transition-transform" />
                          Buy Now
                        </Button>
                      </Link>
                    </div>
                  </CardContent>
                </Card>
              ))}
            </div>
          )}
          
          {!isLoading && personalizedRecommendations.length === 0 && (
            <div className="text-center py-16 bg-slate-50 rounded-3xl">
              <div className="w-16 h-16 bg-pink-100 rounded-full flex items-center justify-center mx-auto mb-4">
                <Heart className="w-8 h-8 text-pink-400" />
              </div>
              <p className="text-slate-600 text-lg mb-6">No personalized recommendations available at the moment.</p>
              <Link href="/products">
                <Button className="bg-gradient-to-r from-pink-500 to-rose-500 hover:from-pink-600 hover:to-rose-600 px-8 py-3 rounded-xl font-semibold shadow-lg shadow-pink-500/25">
                  Browse All Products
                </Button>
              </Link>
            </div>
          )}
        </div>
      </section>

      {/* Quick Navigation Section */}
      <section className="py-20 bg-gradient-to-br from-slate-900 via-slate-800 to-slate-900 text-white relative overflow-hidden">
        <div className="absolute inset-0">
          <div className="absolute top-0 left-1/4 w-96 h-96 bg-blue-500/10 rounded-full blur-3xl"></div>
          <div className="absolute bottom-0 right-1/4 w-96 h-96 bg-purple-500/10 rounded-full blur-3xl"></div>
        </div>
        <div className="container mx-auto px-4 relative z-10">
          <div className="text-center mb-12">
            <h2 className="text-3xl lg:text-4xl font-bold mb-4">Explore More</h2>
            <p className="text-slate-400 text-lg">Discover our full range of products</p>
          </div>
          <div className="grid md:grid-cols-3 gap-6 max-w-4xl mx-auto">
            <Link href="/products?filter=top_sellers">
              <Button 
                variant="outline" 
                size="lg" 
                className="w-full bg-white/5 border-white/20 text-white hover:bg-orange-500 hover:border-orange-500 hover:text-white transition-all duration-300 rounded-2xl py-6 text-lg font-semibold backdrop-blur-sm group"
              >
                🔥 View All Top Sellers
                <ArrowRight className="w-5 h-5 ml-2 opacity-0 group-hover:opacity-100 transition-all duration-300 transform -translate-x-2 group-hover:translate-x-0" />
              </Button>
            </Link>
            <Link href="/products?filter=new_arrivals">
              <Button 
                variant="outline" 
                size="lg" 
                className="w-full bg-white/5 border-white/20 text-white hover:bg-purple-500 hover:border-purple-500 hover:text-white transition-all duration-300 rounded-2xl py-6 text-lg font-semibold backdrop-blur-sm group"
              >
                ✨ See All New Arrivals
                <ArrowRight className="w-5 h-5 ml-2 opacity-0 group-hover:opacity-100 transition-all duration-300 transform -translate-x-2 group-hover:translate-x-0" />
              </Button>
            </Link>
            <Link href="/products">
              <Button 
                variant="outline" 
                size="lg" 
                className="w-full bg-white/5 border-white/20 text-white hover:bg-pink-500 hover:border-pink-500 hover:text-white transition-all duration-300 rounded-2xl py-6 text-lg font-semibold backdrop-blur-sm group"
              >
                💖 Browse All Products
                <ArrowRight className="w-5 h-5 ml-2 opacity-0 group-hover:opacity-100 transition-all duration-300 transform -translate-x-2 group-hover:translate-x-0" />
              </Button>
            </Link>
          </div>
        </div>
      </section>

      {/* Features Section */}
      <section className="py-20 bg-gradient-to-b from-white to-slate-50">
        <div className="container mx-auto px-4">
          <div className="grid md:grid-cols-3 gap-8 max-w-6xl mx-auto">
            {[
              {
                icon: Truck,
                title: "Free Shipping",
                description: "Free shipping on orders over $99",
                color: "blue",
                gradient: "from-blue-500 to-blue-600",
              },
              {
                icon: Shield,
                title: "Warranty",
                description: "1-year warranty on all products",
                color: "green",
                gradient: "from-emerald-500 to-green-600",
              },
              {
                icon: Headphones,
                title: "24/7 Support",
                description: "Round-the-clock customer support",
                color: "purple",
                gradient: "from-purple-500 to-violet-600",
              },
            ].map((feature, index) => (
              <div 
                key={index} 
                className="group text-center p-8 bg-white rounded-3xl shadow-lg shadow-black/5 hover:shadow-card-hover transition-all duration-500 hover:-translate-y-2"
              >
                <div className={`w-16 h-16 bg-gradient-to-br ${feature.gradient} rounded-2xl flex items-center justify-center mx-auto mb-6 group-hover:scale-110 transition-transform duration-300 shadow-lg`}>
                  <feature.icon className="w-8 h-8 text-white" />
                </div>
                <h3 className="text-xl font-bold mb-3 text-slate-900">{feature.title}</h3>
                <p className="text-slate-600">{feature.description}</p>
              </div>
            ))}
          </div>
        </div>
      </section>
    </div>
  )
}
