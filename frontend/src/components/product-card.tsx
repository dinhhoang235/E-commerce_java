"use client"

import Link from "next/link"
import Image from "next/image"
import { Card, CardContent } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { ShoppingCart, Heart, Eye } from "lucide-react"
import { StarRating } from "@/components/star-rating"
import { formatImageUrl, isExternalImage } from "@/lib/utils/image"

interface Category {
  id: string | number
  name: string
  slug: string
  description?: string
  image?: string
  is_active?: boolean
}

interface Product {
  id: string | number
  name: string
  description: string
  min_price: number
  max_price: number
  image?: string
  category: string | Category
  rating: number
  reviews: number
  badge?: string
  total_stock?: number
}

interface ProductCardProps {
  product: Product
  className?: string
}

export function ProductCard({ product, className = "" }: ProductCardProps) {
  const formatPrice = (product: Product) => {
    if (product.min_price === product.max_price) {
      return `$${product.min_price}`
    }
    return `$${product.min_price} - $${product.max_price}`
  }

  return (
    <Card className={`group glass-card hover:shadow-card-hover transition-all duration-500 hover:-translate-y-2 rounded-3xl overflow-hidden border-0 ${className}`}>
      <CardContent className="p-0">
        {/* Image Container */}
        <div className="relative bg-white">
          {/* Badge */}
          {product.badge && (
            <Badge className="absolute top-3 left-3 z-10 bg-gradient-to-r from-red-500 to-rose-500 text-white font-bold px-3 py-1 rounded-full shadow-lg shadow-red-500/25">
              {product.badge}
            </Badge>
          )}
          
          {/* Quick Action Buttons */}
          <div className="absolute top-3 right-3 z-10 flex flex-col gap-2 opacity-0 group-hover:opacity-100 transition-all duration-300 transform translate-x-2 group-hover:translate-x-0">
            <Button
              size="icon"
              variant="secondary"
              className="h-9 w-9 rounded-full bg-white/90 backdrop-blur-sm hover:bg-white shadow-lg border-0"
            >
              <Heart className="h-4 w-4 text-slate-600 hover:text-pink-500 transition-colors" />
            </Button>
            <Link href={`/products/${product.id}`}>
              <Button
                size="icon"
                variant="secondary"
                className="h-9 w-9 rounded-full bg-white/90 backdrop-blur-sm hover:bg-white shadow-lg border-0"
              >
                <Eye className="h-4 w-4 text-slate-600 hover:text-blue-500 transition-colors" />
              </Button>
            </Link>
          </div>
          
          {/* Product Image */}
          <Link href={`/products/${product.id}`}>
            <div className="relative aspect-square overflow-hidden rounded-2xl">
              <Image
                src={formatImageUrl(product.image)}
                alt={product.name}
                fill
                unoptimized={isExternalImage(product.image)}
                className="object-contain group-hover:scale-110 transition-transform duration-500"
              />
            </div>
          </Link>
        </div>
        
        {/* Content */}
        <div className="p-5 bg-white space-y-3">
          {/* Product Name */}
          <Link href={`/products/${product.id}`} className="block">
            <h3 className="font-semibold text-slate-900 line-clamp-2 hover:text-blue-600 transition-colors leading-tight">
              {product.name}
            </h3>
          </Link>
          
          {/* Rating */}
          <div className="flex items-center gap-2">
            <StarRating rating={product.rating} size="sm" />
            <span className="text-sm text-slate-500 font-medium">
              ({product.reviews})
            </span>
          </div>
          
          {/* Price and Action */}
          <div className="flex items-center justify-between pt-2">
            <span className="text-xl font-bold bg-gradient-to-r from-slate-900 to-slate-700 bg-clip-text text-transparent">
              {formatPrice(product)}
            </span>
            <Link href={`/products/${product.id}`}>
              <Button
                size="sm"
                className="h-9 px-4 rounded-xl bg-gradient-to-r from-blue-500 to-purple-500 hover:from-blue-600 hover:to-purple-600 text-white font-semibold shadow-md shadow-blue-500/25 hover:shadow-lg hover:shadow-blue-500/30 transition-all duration-300 group/btn"
              >
                <ShoppingCart className="h-4 w-4 mr-1.5 group-hover/btn:scale-110 transition-transform" />
                Buy
              </Button>
            </Link>
          </div>
        </div>
      </CardContent>
    </Card>
  )
}
