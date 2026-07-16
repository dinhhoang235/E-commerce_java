"use client"

import { useState, useEffect } from "react"
import { useRouter, useParams } from "next/navigation"
import Link from "next/link"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Separator } from "@/components/ui/separator"
import { Badge } from "@/components/ui/badge"
import { ArrowLeft, Package, MapPin, AlertCircle, Clock, CheckCircle2, Truck, Shield, CreditCard } from "lucide-react"
import { Skeleton } from "@/components/ui/skeleton"
import { useAuth } from "@/components/auth-provider"
import { userOrdersApi, type Order } from "@/lib/services/orders"
import { useToast } from "@/hooks/use-toast"
import { CancelOrderButton } from "@/components/ui/cancel-order-button"
import { ContinuePaymentButton } from "@/components/ui/continue-payment-button"
import { RefundButton } from "@/components/ui/refund-button"
import { RefundStatusCard } from "@/components/ui/refund-status-card"
import { OrderStatusBadge } from "@/components/ui/order-status-badge"
import { PaymentStatusBadge } from "@/components/ui/payment-status-badge"

const orderSteps = [
  { key: "pending", label: "Pending", icon: Clock },
  { key: "confirmed", label: "Confirmed", icon: CheckCircle2 },
  { key: "shipping", label: "Shipping", icon: Truck },
  { key: "delivered", label: "Delivered", icon: Package },
]

function getStepIndex(status: string) {
  const s = status?.toLowerCase()
  if (s === "pending" || s === "awaiting_payment" || s === "cancelled") return 0
  if (s === "confirmed" || s === "processing") return 1
  if (s === "shipping" || s === "shipped") return 2
  if (s === "delivered" || s === "completed") return 3
  return 0
}

export default function OrderDetailPage() {
  const { user, isLoading: authLoading } = useAuth()
  const router = useRouter()
  const params = useParams()
  const { toast } = useToast()
  const [order, setOrder] = useState<Order | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [imageErrors, setImageErrors] = useState<Set<number>>(new Set())

  const orderId = params.id as string

  const handleImageError = (itemId: number) => {
    setImageErrors(prev => new Set(prev).add(itemId))
  }

  const handleOrderCancelled = (cancelledOrder: Order) => {
    setOrder(cancelledOrder)
    toast({
      title: "Order Updated",
      description: "Order status has been updated.",
    })
  }

  const handleRefundProcessed = () => {
    const fetchOrder = async () => {
      try {
        const orderData = await userOrdersApi.getOrderById(orderId)
        setOrder(orderData)
      } catch (error) {
        console.error("Error refreshing order:", error)
      }
    }
    fetchOrder()
    
    toast({
      title: "Refund Processed",
      description: "Your refund has been processed successfully.",
    })
  }

  useEffect(() => {
    if (authLoading) return
    if (!user) {
      router.push("/login")
      return
    }

    const fetchOrder = async () => {
      try {
        setIsLoading(true)
        setError(null)
        const orderData = await userOrdersApi.getOrderById(orderId)
        setOrder(orderData)
      } catch (error: any) {
        console.error("Error fetching order:", error)
        setError(error.response?.status === 404 ? "Order not found" : "Failed to load order details")
        toast({
          title: "Error",
          description: "Failed to load order details. Please try again.",
          variant: "destructive",
        })
      } finally {
        setIsLoading(false)
      }
    }

    if (orderId) {
      fetchOrder()
    }
  }, [user, router, orderId, toast])

  if (!user) {
    return null
  }

  if (isLoading) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-slate-50 via-white to-slate-100">
        <div className="container mx-auto px-4 py-6 sm:py-8">
          <div className="max-w-4xl mx-auto">
            {/* Header */}
            <div className="mb-6 sm:mb-8">
              <Skeleton className="h-8 sm:h-9 w-48 mb-2" />
              <Skeleton className="h-4 w-64" />
            </div>
            
            {/* Progress */}
            <div className="mb-6 sm:mb-8 p-4 sm:p-6 bg-white/80 backdrop-blur-xl rounded-2xl shadow-lg">
              <div className="flex items-center justify-between">
                {Array.from({ length: 4 }).map((_, i) => (
                  <div key={i} className="flex items-center flex-1 last:flex-none">
                    <div className="flex flex-col items-center">
                      <Skeleton className="w-10 h-10 sm:w-12 sm:h-12 rounded-full" />
                      <Skeleton className="h-3 w-14 mt-2" />
                    </div>
                    {i < 3 && <Skeleton className="flex-1 h-1 mx-2 sm:mx-3" />}
                  </div>
                ))}
              </div>
            </div>
            
            <div className="grid lg:grid-cols-3 gap-6">
              <div className="lg:col-span-2 space-y-6">
                {/* Items */}
                <div className="p-4 sm:p-6 bg-white/80 backdrop-blur-xl rounded-2xl shadow-lg space-y-4">
                  <Skeleton className="h-6 w-32" />
                  {Array.from({ length: 2 }).map((_, i) => (
                    <div key={i} className="flex items-center gap-4 p-3 bg-slate-50 rounded-xl">
                      <Skeleton className="w-14 h-14 sm:w-16 sm:h-16 rounded-xl flex-shrink-0" />
                      <div className="flex-1 space-y-2">
                        <Skeleton className="h-4 w-3/4" />
                        <Skeleton className="h-3 w-1/2" />
                        <Skeleton className="h-3 w-1/3" />
                      </div>
                      <Skeleton className="h-5 w-16" />
                    </div>
                  ))}
                </div>
                
                {/* Shipping */}
                <div className="p-4 sm:p-6 bg-white/80 backdrop-blur-xl rounded-2xl shadow-lg space-y-4">
                  <Skeleton className="h-6 w-40" />
                  <div className="grid sm:grid-cols-2 gap-4">
                    <Skeleton className="h-16 rounded-xl" />
                    <Skeleton className="h-16 rounded-xl" />
                  </div>
                </div>
              </div>
              
              {/* Summary */}
              <div className="lg:col-span-1">
                <div className="p-4 sm:p-6 bg-white/80 backdrop-blur-xl rounded-2xl shadow-lg space-y-4">
                  <Skeleton className="h-6 w-32" />
                  <div className="p-3 bg-slate-50 rounded-xl space-y-2">
                    <Skeleton className="h-4 w-full" />
                    <Skeleton className="h-4 w-3/4" />
                    <Skeleton className="h-4 w-1/2" />
                  </div>
                  <Skeleton className="h-px w-full" />
                  <div className="space-y-2">
                    <Skeleton className="h-4 w-full" />
                    <Skeleton className="h-4 w-full" />
                  </div>
                  <Skeleton className="h-px w-full" />
                  <Skeleton className="h-6 w-full" />
                  <Skeleton className="h-11 w-full rounded-xl" />
                  <Skeleton className="h-11 w-full rounded-xl" />
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    )
  }

  if (error || !order) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-slate-50 via-white to-slate-100">
        <div className="container mx-auto px-4 py-6 sm:py-8">
          <div className="max-w-4xl mx-auto">
            <Button variant="ghost" onClick={() => router.back()} className="mb-4 md:hidden">
              <ArrowLeft className="mr-2 h-4 w-4" /> Back
            </Button>
            <Card className="border-0 shadow-xl bg-white/80 backdrop-blur-xl rounded-2xl overflow-hidden">
              <CardContent className="text-center py-16 px-6">
                <div className="w-20 h-20 bg-red-100 rounded-full flex items-center justify-center mx-auto mb-4">
                  <AlertCircle className="w-10 h-10 text-red-500" />
                </div>
                <h2 className="text-xl font-bold text-slate-900 mb-2">{error || "Order not found"}</h2>
                <p className="text-slate-500 mb-6 max-w-md mx-auto">
                  The order you're looking for doesn't exist or you don't have permission to view it.
                </p>
                <Button asChild className="bg-gradient-to-r from-blue-600 to-purple-600 hover:from-blue-700 hover:to-purple-700 text-white rounded-xl px-6">
                  <Link href="/orders">Back to Orders</Link>
                </Button>
              </CardContent>
            </Card>
          </div>
        </div>
      </div>
    )
  }

  const currentStep = getStepIndex(order.status)

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 via-white to-slate-100">
      <div className="container mx-auto px-4 py-6 sm:py-8">
        <div className="max-w-4xl mx-auto">
          {/* Back button - mobile only */}
          <Button variant="ghost" onClick={() => router.back()} className="mb-4 md:hidden">
            <ArrowLeft className="mr-2 h-4 w-4" /> Back
          </Button>

          {/* Header */}
          <div className="mb-6 sm:mb-8">
            <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3 mb-2">
              <div>
                <h1 className="text-2xl sm:text-3xl font-bold text-slate-900">Order #{order.id}</h1>
                <p className="text-sm text-slate-500 mt-1">
                  Placed on {new Date(order.date).toLocaleDateString()} at{" "}
                  {new Date(order.date).toLocaleTimeString()}
                </p>
              </div>
              <OrderStatusBadge status={order.status} className="text-sm w-fit" />
            </div>
          </div>

          {/* Progress Steps */}
          <Card className="mb-6 sm:mb-8 border-0 shadow-lg bg-white/80 backdrop-blur-xl rounded-2xl overflow-hidden">
            <CardContent className="p-4 sm:p-6">
              <div className="flex items-center justify-between">
                {orderSteps.map((step, index) => {
                  const StepIcon = step.icon
                  const isActive = index <= currentStep
                  const isCurrent = index === currentStep
                  return (
                    <div key={step.key} className="flex items-center flex-1 last:flex-none">
                      <div className="flex flex-col items-center">
                        <div className={`w-10 h-10 sm:w-12 sm:h-12 rounded-full flex items-center justify-center transition-all duration-300 ${
                          isActive 
                            ? "bg-gradient-to-br from-blue-500 to-purple-500 text-white shadow-lg shadow-blue-500/30" 
                            : "bg-slate-100 text-slate-400"
                        } ${isCurrent ? "ring-4 ring-blue-100 scale-110" : ""}`}>
                          <StepIcon className="w-5 h-5 sm:w-6 sm:h-6" />
                        </div>
                        <span className={`text-[10px] sm:text-xs mt-2 font-medium ${isActive ? "text-blue-600" : "text-slate-400"}`}>
                          {step.label}
                        </span>
                      </div>
                      {index < orderSteps.length - 1 && (
                        <div className={`flex-1 h-1 mx-2 sm:mx-3 rounded-full transition-all duration-300 ${
                          index < currentStep ? "bg-gradient-to-r from-blue-500 to-purple-500" : "bg-slate-100"
                        }`} />
                      )}
                    </div>
                  )
                })}
              </div>
            </CardContent>
          </Card>

          <div className="grid lg:grid-cols-3 gap-6">
            {/* Left Column - Order Items & Shipping */}
            <div className="lg:col-span-2 space-y-6">
              {/* Order Items */}
              <Card className="border-0 shadow-lg bg-white/80 backdrop-blur-xl rounded-2xl overflow-hidden">
                <CardHeader className="p-4 sm:p-6 pb-0">
                  <CardTitle className="flex items-center text-lg">
                    <div className="w-8 h-8 bg-blue-100 rounded-xl flex items-center justify-center mr-3">
                      <Package className="h-4 w-4 text-blue-600" />
                    </div>
                    Order Items
                    {order.items && (
                      <Badge variant="secondary" className="ml-2 bg-slate-100 text-slate-600 rounded-full px-2 py-0.5 text-xs">
                        {order.items.length}
                      </Badge>
                    )}
                  </CardTitle>
                </CardHeader>
                <CardContent className="p-4 sm:p-6">
                  <div className="space-y-3">
                    {order.items && order.items.length > 0 ? (
                      order.items.map((item) => (
                        <div key={item.id} className="flex items-center space-x-3 sm:space-x-4 p-3 sm:p-4 bg-slate-50 rounded-xl hover:bg-slate-100 transition-colors">
                          <div className="w-14 h-14 sm:w-16 sm:h-16 bg-white rounded-xl flex items-center justify-center overflow-hidden shadow-sm flex-shrink-0">
                            {item.product_variant_image && !imageErrors.has(item.id) ? (
                              <img
                                src={item.product_variant_image}
                                alt={item.product_variant_name}
                                width={64}
                                height={64}
                                className="w-full h-full object-cover rounded-xl"
                                onError={() => handleImageError(item.id)}
                              />
                            ) : (
                              <Package className="w-6 h-6 sm:w-8 sm:h-8 text-slate-300" />
                            )}
                          </div>
                          <div className="flex-1 min-w-0">
                            <h4 className="font-semibold text-slate-900 text-sm sm:text-base truncate">{item.product_variant_name}</h4>
                            <div className="text-xs sm:text-sm text-slate-500 space-y-0.5 mt-1">
                              {item.product_variant_color && (
                                <p>Color: <span className="text-slate-700">{item.product_variant_color}</span></p>
                              )}
                              {item.product_variant_storage && (
                                <p>Storage: <span className="text-slate-700">{item.product_variant_storage}</span></p>
                              )}
                              <p>Qty: {item.quantity} × ${parseFloat(item.price).toFixed(2)}</p>
                            </div>
                          </div>
                          <div className="text-right flex-shrink-0">
                            <p className="font-bold text-slate-900 text-sm sm:text-base">
                              ${(parseFloat(item.price) * item.quantity).toFixed(2)}
                            </p>
                          </div>
                        </div>
                      ))
                    ) : (
                      <div className="text-center py-12">
                        <div className="w-16 h-16 bg-slate-100 rounded-full flex items-center justify-center mx-auto mb-3">
                          <Package className="w-8 h-8 text-slate-300" />
                        </div>
                        <p className="text-slate-500 font-medium">No item details available</p>
                        {order.products && order.products.length > 0 && (
                          <p className="text-sm text-slate-400 mt-1">
                            This order contains {order.products.length} product(s)
                          </p>
                        )}
                      </div>
                    )}
                  </div>
                </CardContent>
              </Card>

              {/* Shipping Information */}
              {order.shipping && (
                <Card className="border-0 shadow-lg bg-white/80 backdrop-blur-xl rounded-2xl overflow-hidden">
                  <CardHeader className="p-4 sm:p-6 pb-0">
                    <CardTitle className="flex items-center text-lg">
                      <div className="w-8 h-8 bg-green-100 rounded-xl flex items-center justify-center mr-3">
                        <MapPin className="h-4 w-4 text-green-600" />
                      </div>
                      Shipping Information
                    </CardTitle>
                  </CardHeader>
                  <CardContent className="p-4 sm:p-6">
                    <div className="grid sm:grid-cols-2 gap-4">
                      <div className="p-3 bg-slate-50 rounded-xl">
                        <p className="text-xs font-medium text-slate-500 mb-1">Address</p>
                        <p className="text-sm text-slate-900">{order.shipping.address || "No address available"}</p>
                      </div>
                      <div className="p-3 bg-slate-50 rounded-xl">
                        <p className="text-xs font-medium text-slate-500 mb-1">Method</p>
                        <p className="text-sm text-slate-900 capitalize">{order.shipping.method || "Standard"}</p>
                      </div>
                    </div>
                  </CardContent>
                </Card>
              )}

              {/* Refund Status Card */}
              <RefundStatusCard order={order} />
            </div>

            {/* Right Column - Order Summary */}
            <div className="lg:col-span-1 order-first lg:order-last">
              <Card className="sticky top-24 border-0 shadow-lg bg-white/80 backdrop-blur-xl rounded-2xl overflow-hidden">
                <CardHeader className="p-4 sm:p-6 pb-0">
                  <CardTitle className="flex items-center text-lg">
                    <div className="w-8 h-8 bg-purple-100 rounded-xl flex items-center justify-center mr-3">
                      <CreditCard className="h-4 w-4 text-purple-600" />
                    </div>
                    Order Summary
                  </CardTitle>
                </CardHeader>
                <CardContent className="p-4 sm:p-6 space-y-4">
                  {/* Customer Info */}
                  <div className="p-3 bg-slate-50 rounded-xl space-y-2">
                    <div className="flex justify-between text-sm">
                      <span className="text-slate-500">Customer</span>
                      <span className="font-medium text-slate-900">{order.customer}</span>
                    </div>
                    <div className="flex justify-between text-sm">
                      <span className="text-slate-500">Email</span>
                      <span className="font-medium text-slate-900 text-right break-all max-w-[180px]">{order.email}</span>
                    </div>
                    <div className="flex justify-between text-sm items-center">
                      <span className="text-slate-500">Payment</span>
                      <PaymentStatusBadge 
                        status={order.payment_status || 'no_payment'} 
                        isPaid={order.is_paid}
                        className="text-xs"
                      />
                    </div>
                  </div>

                  <Separator className="bg-slate-100" />

                  {/* Pricing */}
                  <div className="space-y-2 text-sm">
                    <div className="flex justify-between">
                      <span className="text-slate-500">Subtotal (inc. tax)</span>
                      <span className="text-slate-900">${parseFloat(order.subtotal || order.total).toFixed(2)}</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-slate-500">Shipping</span>
                      {order.shipping?.cost !== undefined && order.shipping.cost > 0 ? (
                        <span className="text-slate-900">${order.shipping.cost.toFixed(2)}</span>
                      ) : (
                        <span className="text-green-600 font-medium">Free</span>
                      )}
                    </div>
                  </div>

                  <Separator className="bg-slate-100" />

                  <div className="flex justify-between items-center">
                    <span className="font-semibold text-slate-900">Total</span>
                    <span className="text-xl font-bold bg-gradient-to-r from-blue-600 to-purple-600 bg-clip-text text-transparent">
                      ${(() => {
                        if (order.total_with_shipping) {
                          return parseFloat(order.total_with_shipping).toFixed(2)
                        }
                        const subtotal = parseFloat(order.subtotal || order.total)
                        const shipping = order.shipping?.cost || 0
                        return (subtotal + shipping).toFixed(2)
                      })()}
                    </span>
                  </div>
                  
                  <p className="text-[10px] text-slate-400 text-center">
                    Prices include applicable taxes
                  </p>

                  {/* Actions */}
                  <div className="space-y-3 pt-2">
                    {order.can_continue_payment && (
                      <ContinuePaymentButton 
                        order={order}
                        className="w-full h-11 rounded-xl bg-gradient-to-r from-blue-600 to-purple-600 hover:from-blue-700 hover:to-purple-700 text-white font-semibold btn-touch"
                      />
                    )}
                    
                    <RefundButton 
                      order={order}
                      onRefundProcessed={handleRefundProcessed}
                      variant="outline"
                      size="default"
                      className="w-full h-11 rounded-xl btn-touch"
                    />
                    
                    <CancelOrderButton 
                      order={order}
                      onOrderCancelled={handleOrderCancelled}
                      variant="destructive"
                      size="default"
                      className="w-full h-11 rounded-xl btn-touch"
                    />
                  </div>

                  {/* Security Badge */}
                  <div className="flex items-center justify-center gap-2 pt-2 text-slate-400">
                    <Shield className="w-3.5 h-3.5" />
                    <span className="text-[10px]">Secure & Encrypted</span>
                  </div>
                </CardContent>
              </Card>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
