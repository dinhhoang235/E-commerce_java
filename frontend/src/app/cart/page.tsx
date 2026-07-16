"use client"

import { useState } from "react"
import Link from "next/link"
import { useRouter } from "next/navigation"
import { Button } from "@/components/ui/button"
import { Card, CardContent } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Input } from "@/components/ui/input"
import { Separator } from "@/components/ui/separator"
import { ShoppingCart, Trash2, Plus, Minus, ArrowLeft, ShoppingBag, Truck, Shield, Tag, ArrowRight } from "lucide-react"
import { Skeleton } from "@/components/ui/skeleton"
import { useCart } from "@/components/cart-provider"
import { useAuth } from "@/components/auth-provider"
import { cartService } from "@/lib/services/cart"

function CartSkeleton() {
  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50/50 via-white to-slate-100/50">
      <div className="container mx-auto px-4 py-8">
        <div className="mb-8">
          <Skeleton className="h-10 w-40 mb-4 rounded-xl" />
          <div className="flex items-center gap-3">
            <Skeleton className="w-12 h-12 rounded-2xl" />
            <div>
              <Skeleton className="h-8 w-48 mb-2" />
              <Skeleton className="h-4 w-24" />
            </div>
          </div>
        </div>
        <div className="grid lg:grid-cols-3 gap-8">
          <div className="lg:col-span-2 space-y-4">
            {Array.from({ length: 3 }).map((_, i) => (
              <div key={i} className="bg-white rounded-2xl border border-slate-100 shadow-sm p-5">
                <div className="flex items-center gap-4">
                  <Skeleton className="w-24 h-24 rounded-xl flex-shrink-0" />
                  <div className="flex-1 space-y-2">
                    <Skeleton className="h-5 w-3/4" />
                    <Skeleton className="h-4 w-1/4" />
                    <div className="flex gap-2">
                      <Skeleton className="h-6 w-16 rounded-full" />
                      <Skeleton className="h-6 w-16 rounded-full" />
                    </div>
                  </div>
                  <div className="space-y-2">
                    <Skeleton className="h-9 w-28 rounded-xl" />
                    <Skeleton className="h-5 w-16" />
                  </div>
                </div>
              </div>
            ))}
          </div>
          <div className="lg:col-span-1">
            <div className="bg-white rounded-2xl border border-slate-100 shadow-sm p-6 space-y-4">
              <Skeleton className="h-6 w-32" />
              <div className="space-y-3">
                <div className="flex justify-between"><Skeleton className="h-4 w-20" /><Skeleton className="h-4 w-16" /></div>
                <div className="flex justify-between"><Skeleton className="h-4 w-20" /><Skeleton className="h-4 w-16" /></div>
                <Skeleton className="h-px w-full" />
                <div className="flex justify-between"><Skeleton className="h-6 w-16" /><Skeleton className="h-6 w-20" /></div>
              </div>
              <Skeleton className="h-12 w-full rounded-xl" />
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}

export default function CartPage() {
  const { items, removeItem, updateQuantity, total, clearCart, loading, error } = useCart()
  const { user } = useAuth()
  const router = useRouter()
  const [promoCode, setPromoCode] = useState("")
  const [promoError, setPromoError] = useState("")
  const [discount, setDiscount] = useState(0)
  const [promoLoading, setPromoLoading] = useState(false)

  const shipping = 0
  const discountedTotal = total - discount
  const finalTotal = discountedTotal + shipping

  const handleQuantityChange = async (itemId: number, newQuantity: number) => {
    if (newQuantity < 1) return
    await updateQuantity(itemId, newQuantity)
  }

  const handleRemoveItem = async (itemId: number) => {
    await removeItem(itemId)
  }

  const handleApplyPromo = async () => {
    if (!promoCode) { setPromoError("Please enter a promo code"); return }

    try {
      setPromoLoading(true)
      setPromoError("")
      
      const result = await cartService.applyPromoCode({ code: promoCode })
      
      if (result.valid) {
        setPromoError(result.message)
        let discountAmount = 0
        if (result.discount_percentage) {
          discountAmount = total * (result.discount_percentage / 100)
        } else if (result.discount_amount) {
          discountAmount = result.discount_amount
        }
        setDiscount(discountAmount)
      } else {
        setPromoError(result.message)
        setDiscount(0)
      }
    } catch (err) {
      setPromoError("Failed to apply promo code")
      setDiscount(0)
    } finally {
      setPromoLoading(false)
    }
  }

  if (loading && items.length === 0) {
    return <CartSkeleton />
  }

  if (items.length === 0 && !loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-slate-50/50 via-white to-slate-100/50">
        <div className="text-center p-8 bg-white rounded-3xl shadow-xl max-w-md mx-4">
          <div className="w-20 h-20 bg-slate-100 rounded-full flex items-center justify-center mx-auto mb-6">
            <ShoppingBag className="w-10 h-10 text-slate-400" />
          </div>
          <h1 className="text-2xl font-bold mb-2 text-slate-900">Your cart is empty</h1>
          <p className="text-slate-500 mb-8">Looks like you haven&apos;t added anything to your cart yet.</p>
          <Link href="/products">
            <Button className="bg-gradient-to-r from-blue-600 to-purple-600 hover:from-blue-700 hover:to-purple-700 rounded-xl px-8 py-3 font-semibold shadow-lg shadow-blue-500/25 group">
              <ShoppingBag className="w-4 h-4 mr-2" />
              Start Shopping
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
        {error && (
          <div className="mb-4 p-4 bg-red-50 text-red-600 rounded-2xl border border-red-100">
            {error}
          </div>
        )}
        
        {/* Header */}
        <div className="mb-8">
          <Button 
            variant="ghost" 
            onClick={() => router.back()} 
            className="mb-4 rounded-xl hover:bg-slate-100 transition-colors"
          >
            <ArrowLeft className="mr-2 h-4 w-4" /> Continue Shopping
          </Button>
          <div className="flex items-center gap-3">
            <div className="w-12 h-12 bg-gradient-to-br from-blue-500 to-purple-500 rounded-2xl flex items-center justify-center shadow-lg shadow-blue-500/25">
              <ShoppingCart className="w-6 h-6 text-white" />
            </div>
            <div>
              <h1 className="text-3xl font-bold tracking-tight text-slate-900">Shopping Cart</h1>
              <p className="text-slate-500">
                {items.length} item{items.length !== 1 ? "s" : ""} in your cart
              </p>
            </div>
          </div>
        </div>

        <div className="grid lg:grid-cols-3 gap-8">
          {/* Cart Items */}
          <div className="lg:col-span-2 space-y-4">
            {items.map((item) => (
              <Card key={item.itemId} className="glass-card rounded-2xl overflow-hidden border-0 hover:shadow-lg transition-all duration-300">
                <CardContent className="p-0">
                  <div className="flex flex-col sm:flex-row items-start sm:items-center gap-4 p-5">
                    {/* Image */}
                    <div className="w-24 h-24 bg-gradient-to-br from-slate-50 to-slate-100 rounded-xl flex items-center justify-center overflow-hidden flex-shrink-0">
                      <img
                        src={item.image || "/placeholder.jpg"}
                        alt={item.name}
                        className="w-full h-full object-contain p-2"
                        onError={(e) => {
                          e.currentTarget.src = "/placeholder.jpg";
                        }}
                      />
                    </div>
                    
                    {/* Info */}
                    <div className="flex-1 min-w-0">
                      <Link href={`/products/${item.id}`} className="font-semibold text-slate-900 hover:text-blue-600 transition-colors line-clamp-1">
                        {item.name}
                      </Link>
                      <p className="text-sm text-slate-500 mt-1">
                        Unit Price: ${typeof item.price === 'number' ? item.price.toFixed(2) : '0.00'}
                      </p>
                      <div className="flex items-center gap-2 mt-2">
                        {item.color && (
                          <Badge variant="secondary" className="text-xs rounded-full px-2.5 py-0.5 bg-slate-100">
                            {item.color}
                          </Badge>
                        )}
                        {item.storage && (
                          <Badge variant="secondary" className="text-xs rounded-full px-2.5 py-0.5 bg-slate-100">
                            {item.storage}
                          </Badge>
                        )}
                      </div>
                    </div>
                    
                    {/* Quantity Controls */}
                    <div className="flex items-center gap-3">
                      <div className="flex items-center bg-slate-50 rounded-xl border border-slate-200/60">
                        <Button
                          variant="ghost"
                          size="icon"
                          className="h-9 w-9 rounded-l-xl hover:bg-slate-100"
                          onClick={() => handleQuantityChange(item.itemId, item.quantity - 1)}
                          disabled={item.quantity <= 1 || loading}
                        >
                          <Minus className="h-3.5 w-3.5" />
                        </Button>
                        <span className="w-10 text-center font-semibold text-sm">{item.quantity}</span>
                        <Button
                          variant="ghost"
                          size="icon"
                          className="h-9 w-9 rounded-r-xl hover:bg-slate-100"
                          onClick={() => handleQuantityChange(item.itemId, item.quantity + 1)}
                          disabled={loading}
                        >
                          <Plus className="h-3.5 w-3.5" />
                        </Button>
                      </div>
                    </div>
                    
                    {/* Price & Remove */}
                    <div className="flex items-center gap-4">
                      <span className="font-bold text-lg text-slate-900">
                        ${typeof item.price === 'number' ? (item.price * item.quantity).toFixed(2) : '0.00'}
                      </span>
                      <Button
                        variant="ghost"
                        size="icon"
                        className="h-9 w-9 rounded-xl hover:bg-red-50 text-slate-400 hover:text-red-500 transition-colors"
                        onClick={() => handleRemoveItem(item.itemId)}
                        disabled={loading}
                      >
                        <Trash2 className="h-4 w-4" />
                      </Button>
                    </div>
                  </div>
                </CardContent>
              </Card>
            ))}

            {/* Bottom Actions */}
            <div className="flex justify-between items-center pt-4">
              <Button 
                variant="outline" 
                onClick={clearCart} 
                disabled={loading}
                className="rounded-xl border-slate-200 hover:bg-red-50 hover:text-red-600 hover:border-red-200 transition-all"
              >
                <Trash2 className="w-4 h-4 mr-2" />
                Clear Cart
              </Button>
              <Link href="/products">
                <Button variant="outline" className="rounded-xl border-slate-200 hover:bg-slate-50">
                  Add More Items
                </Button>
              </Link>
            </div>

            {/* Promo Code */}
            <Card className="glass-card rounded-2xl border-0 overflow-hidden">
              <CardContent className="p-5">
                <div className="flex items-center gap-2 mb-4">
                  <Tag className="w-4 h-4 text-blue-500" />
                  <h3 className="font-semibold text-slate-900">Promo Code</h3>
                </div>
                <div className="flex gap-3">
                  <Input
                    placeholder="Enter promo code"
                    value={promoCode}
                    onChange={(e) => setPromoCode(e.target.value)}
                    disabled={promoLoading}
                    className="h-11 rounded-xl bg-slate-50 border-slate-200/60 focus:bg-white focus:border-blue-300 focus:ring-2 focus:ring-blue-500/20"
                  />
                  <Button 
                    onClick={handleApplyPromo} 
                    disabled={promoLoading}
                    className="h-11 px-6 rounded-xl bg-gradient-to-r from-blue-600 to-purple-600 hover:from-blue-700 hover:to-purple-700 font-semibold shadow-lg shadow-blue-500/25"
                  >
                    {promoLoading ? "Applying..." : "Apply"}
                  </Button>
                </div>
                {promoError && (
                  <p className={`mt-3 text-sm font-medium ${promoError.includes("successfully") ? "text-green-600" : "text-red-500"}`}>
                    {promoError}
                  </p>
                )}
              </CardContent>
            </Card>
          </div>

          {/* Order Summary */}
          <div className="lg:col-span-1">
            <Card className="glass-card rounded-2xl border-0 overflow-hidden sticky top-24">
              <CardContent className="p-6">
                <h3 className="text-lg font-bold text-slate-900 mb-6">Order Summary</h3>
                
                <div className="space-y-4">
                  <div className="flex justify-between text-sm">
                    <span className="text-slate-500">Subtotal</span>
                    <span className="font-medium">${total.toFixed(2)}</span>
                  </div>
                  {discount > 0 && (
                    <div className="flex justify-between text-sm">
                      <span className="text-green-600">Discount</span>
                      <span className="font-medium text-green-600">-${discount.toFixed(2)}</span>
                    </div>
                  )}
                  <div className="flex justify-between text-sm">
                    <span className="text-slate-500">Shipping</span>
                    <span className="font-medium text-green-600">Free</span>
                  </div>
                </div>

                <Separator className="my-4" />

                <div className="flex justify-between items-center">
                  <span className="text-lg font-bold text-slate-900">Total</span>
                  <span className="text-2xl font-bold bg-gradient-to-r from-blue-600 to-purple-600 bg-clip-text text-transparent">
                    ${finalTotal.toFixed(2)}
                  </span>
                </div>

                <Link href="/checkout" className="block mt-6">
                  <Button className="w-full h-12 rounded-xl bg-gradient-to-r from-blue-600 to-purple-600 hover:from-blue-700 hover:to-purple-700 font-semibold shadow-lg shadow-blue-500/25 hover:shadow-xl hover:shadow-blue-500/30 transition-all duration-300 group">
                    Proceed to Checkout
                    <ArrowRight className="w-4 h-4 ml-2 group-hover:translate-x-1 transition-transform" />
                  </Button>
                </Link>

                {!user && (
                  <p className="text-sm text-slate-500 text-center mt-4">
                    <Link href="/login?redirect=/checkout" className="text-blue-600 hover:text-blue-700 font-medium">
                      Sign in
                    </Link>{" "}
                    to use saved shipping details
                  </p>
                )}

                {/* Benefits */}
                <div className="mt-6 pt-6 border-t border-slate-100 space-y-3">
                  <div className="flex items-center gap-3">
                    <div className="w-8 h-8 bg-blue-50 rounded-lg flex items-center justify-center">
                      <Truck className="w-4 h-4 text-blue-500" />
                    </div>
                    <div>
                      <p className="text-sm font-medium text-slate-900">Free Shipping</p>
                      <p className="text-xs text-slate-500">On all orders over $99</p>
                    </div>
                  </div>
                  <div className="flex items-center gap-3">
                    <div className="w-8 h-8 bg-green-50 rounded-lg flex items-center justify-center">
                      <Shield className="w-4 h-4 text-green-500" />
                    </div>
                    <div>
                      <p className="text-sm font-medium text-slate-900">Secure Checkout</p>
                      <p className="text-xs text-slate-500">Safe & protected payment</p>
                    </div>
                  </div>
                </div>
              </CardContent>
            </Card>
          </div>
        </div>
      </div>
    </div>
  )
}
