"use client"

import { useEffect } from "react"
import { useRouter } from "next/navigation"
import { Button } from "@/components/ui/button"
import { WishlistItemCard } from "@/components/wishlist-item-card"
import { useWishlist } from "@/components/wishlist-provider"
import { useAuth } from "@/components/auth-provider"
import { Heart, ShoppingBag, Trash2, ArrowRight } from "lucide-react"
import Link from "next/link"
import { Skeleton } from "@/components/ui/skeleton"
import { Badge } from "@/components/ui/badge"

export default function WishlistPage() {
  const { items, total, loading, error, clearWishlist, refreshWishlist } = useWishlist()
  const { user, isLoading: authLoading } = useAuth()
  const router = useRouter()

  useEffect(() => {
    if (authLoading) return
    if (!user) {
      router.push("/login")
      return
    }
    refreshWishlist()
  }, [user, authLoading, refreshWishlist, router])

  if (authLoading || !user) {
    return null
  }

  if (loading && items.length === 0) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-slate-50/50 via-white to-slate-100/50">
        <div className="container mx-auto px-4 py-8">
          <div className="mb-8">
            <Skeleton className="h-8 w-48 mb-3 rounded-full" />
            <Skeleton className="h-4 w-32 rounded-full" />
          </div>
          <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-6">
            {Array.from({ length: 8 }).map((_, i) => (
              <div key={i} className="bg-white rounded-3xl overflow-hidden shadow-sm">
                <Skeleton className="aspect-square w-full rounded-none" />
                <div className="p-5 space-y-3">
                  <Skeleton className="h-4 w-full rounded-full" />
                  <Skeleton className="h-4 w-3/4 rounded-full" />
                  <Skeleton className="h-6 w-1/2 rounded-full" />
                  <Skeleton className="h-10 w-full rounded-xl" />
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
      <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-slate-50/50 via-white to-slate-100/50">
        <div className="text-center p-8 bg-white rounded-3xl shadow-xl max-w-md mx-4">
          <div className="w-16 h-16 bg-red-100 rounded-full flex items-center justify-center mx-auto mb-4">
            <Heart className="w-8 h-8 text-red-500" />
          </div>
          <h1 className="text-2xl font-bold mb-2 text-slate-900">Error Loading Wishlist</h1>
          <p className="text-slate-500 mb-6">{error}</p>
          <Button onClick={refreshWishlist} className="bg-gradient-to-r from-blue-600 to-purple-600 rounded-xl px-6 py-2.5 font-semibold shadow-lg shadow-blue-500/25">
            Try Again
          </Button>
        </div>
      </div>
    )
  }

  if (items.length === 0) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-slate-50/50 via-white to-slate-100/50">
        <div className="text-center p-8 bg-white rounded-3xl shadow-xl max-w-md mx-4">
          <div className="w-20 h-20 bg-pink-100 rounded-full flex items-center justify-center mx-auto mb-6">
            <Heart className="w-10 h-10 text-pink-400" />
          </div>
          <h1 className="text-2xl font-bold mb-2 text-slate-900">Your Wishlist is Empty</h1>
          <p className="text-slate-500 mb-8">Start adding products you love to your wishlist.</p>
          <Link href="/products">
            <Button className="bg-gradient-to-r from-pink-500 to-rose-500 hover:from-pink-600 hover:to-rose-600 rounded-xl px-8 py-3 font-semibold shadow-lg shadow-pink-500/25 group">
              <ShoppingBag className="w-4 h-4 mr-2" />
              Browse Products
              <ArrowRight className="w-4 h-4 ml-2 group-hover:translate-x-1 transition-transform" />
            </Button>
          </Link>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50/50 via-white to-slate-100/50">
      <div className="container mx-auto px-4 py-8">
        {/* Header */}
        <div className="mb-8">
          <Badge variant="secondary" className="bg-pink-100 text-pink-700 mb-3 px-4 py-1.5 rounded-full">
            <Heart className="w-3 h-3 mr-1.5 fill-current" />
            Wishlist
          </Badge>
          <div className="flex items-center justify-between">
            <div>
              <h1 className="text-3xl font-bold tracking-tight text-slate-900">My Wishlist</h1>
              <p className="text-slate-500 mt-1">
                {total} {total === 1 ? 'item' : 'items'} saved
              </p>
            </div>
            
            {total > 0 && (
              <Button
                variant="outline"
                onClick={clearWishlist}
                disabled={loading}
                className="rounded-xl border-red-200 text-red-600 hover:bg-red-50 hover:border-red-300 transition-all"
              >
                <Trash2 className="mr-2 h-4 w-4" />
                Clear All
              </Button>
            )}
          </div>
        </div>

        {/* Wishlist Items Grid */}
        <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-6">
          {items.map((item) => (
            <WishlistItemCard key={item.id} item={item} />
          ))}
        </div>

        {/* Continue Shopping */}
        <div className="mt-12 text-center">
          <Link href="/products">
            <Button variant="outline" size="lg" className="rounded-xl border-slate-200 hover:bg-slate-50 font-medium">
              <ShoppingBag className="mr-2 h-4 w-4" />
              Continue Shopping
            </Button>
          </Link>
        </div>
      </div>
    </div>
  )
}
