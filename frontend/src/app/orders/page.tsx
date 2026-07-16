"use client"

import { useState, useEffect } from "react"
import { useRouter } from "next/navigation"
import Link from "next/link"
import { Button } from "@/components/ui/button"
import { Card, CardContent } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Package, Eye, ShoppingBag, ArrowRight } from "lucide-react"
import { Skeleton } from "@/components/ui/skeleton"
import { useAuth } from "@/components/auth-provider"
import { userOrdersApi, type Order } from "@/lib/services/orders"
import { useToast } from "@/hooks/use-toast"
import { CancelOrderButton } from "@/components/ui/cancel-order-button"
import { ContinuePaymentButton } from "@/components/ui/continue-payment-button"
import { OrderStatusBadge } from "@/components/ui/order-status-badge"
import { PaymentStatusBadge } from "@/components/ui/payment-status-badge"

export default function OrdersPage() {
  const { user, isLoading: authLoading } = useAuth()
  const router = useRouter()
  const { toast } = useToast()
  const [orders, setOrders] = useState<Order[]>([])
  const [isLoading, setIsLoading] = useState(true)

  const handleOrderCancelled = (cancelledOrder: Order) => {
    setOrders(prevOrders => 
      prevOrders.map(order => 
        order.id === cancelledOrder.id ? cancelledOrder : order
      )
    )
  }

  useEffect(() => {
    if (authLoading) return
    if (!user) {
      router.push("/login")
      return
    }

    const fetchOrders = async () => {
      try {
        setIsLoading(true)
        const userOrders = await userOrdersApi.getMyOrders()
        setOrders(userOrders)
      } catch (error) {
        console.error("Error fetching orders:", error)
        toast({
          title: "Error",
          description: "Failed to load your orders. Please try again.",
          variant: "destructive",
        })
        setOrders([])
      } finally {
        setIsLoading(false)
      }
    }

    fetchOrders()
  }, [user, authLoading, router, toast])

  if (authLoading || !user) {
    return null
  }

  const getItemCount = (order: Order) => {
    if (order.items && Array.isArray(order.items)) {
      return order.items.reduce((total, item) => total + item.quantity, 0)
    }
    return order.products?.length || 0
  }

  if (isLoading) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-slate-50 via-white to-slate-100">
        <div className="container mx-auto px-4 sm:px-6 py-6 sm:py-8">
          <div className="max-w-4xl mx-auto">
            <div className="mb-6 sm:mb-8">
              <Skeleton className="h-6 w-24 mb-3 rounded-full" />
              <Skeleton className="h-8 sm:h-9 w-40 mb-2" />
              <Skeleton className="h-4 w-48" />
            </div>
            
            <div className="space-y-4">
              {Array.from({ length: 3 }).map((_, i) => (
                <div key={i} className="p-4 sm:p-6 bg-white/80 backdrop-blur-xl rounded-2xl border-0 shadow-lg space-y-4">
                  <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
                    <div className="space-y-2">
                      <Skeleton className="h-5 w-32" />
                      <Skeleton className="h-3 w-48" />
                    </div>
                    <div className="flex gap-2">
                      <Skeleton className="h-6 w-20 rounded-full" />
                      <Skeleton className="h-6 w-16 rounded-full" />
                    </div>
                  </div>
                  <Skeleton className="h-px w-full" />
                  <div className="flex items-center justify-between">
                    <Skeleton className="h-4 w-24" />
                    <Skeleton className="h-5 w-20" />
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50/50 via-white to-slate-100/50">
      <div className="container mx-auto px-4 py-8">
        <div className="max-w-4xl mx-auto">
          {/* Header */}
          <div className="mb-8">
            <Badge variant="secondary" className="bg-blue-100 text-blue-700 mb-3 px-4 py-1.5 rounded-full">
              <Package className="w-3 h-3 mr-1.5" />
              Orders
            </Badge>
            <h1 className="text-3xl font-bold tracking-tight text-slate-900">Order History</h1>
            <p className="text-slate-500 mt-1">Track and manage your orders</p>
          </div>
          
          {/* Pending Payment Notice */}
          {orders.some(order => order.can_continue_payment) && (
            <div className="mb-6 p-4 bg-gradient-to-r from-blue-50 to-purple-50 border border-blue-100 rounded-2xl">
              <p className="text-sm text-blue-700 font-medium">
                💡 You have orders with pending payments. Click &quot;Pay Now&quot; to complete your purchase.
              </p>
            </div>
          )}

          {orders.length === 0 ? (
            <div className="text-center p-12 bg-white rounded-3xl shadow-xl">
              <div className="w-20 h-20 bg-slate-100 rounded-full flex items-center justify-center mx-auto mb-6">
                <Package className="w-10 h-10 text-slate-400" />
              </div>
              <h2 className="text-xl font-bold mb-2 text-slate-900">No orders yet</h2>
              <p className="text-slate-500 mb-8">Start shopping to see your orders here</p>
              <Link href="/products">
                <Button className="bg-gradient-to-r from-blue-600 to-purple-600 hover:from-blue-700 hover:to-purple-700 rounded-xl px-8 py-3 font-semibold shadow-lg shadow-blue-500/25 group">
                  <ShoppingBag className="w-4 h-4 mr-2" />
                  Browse Products
                  <ArrowRight className="w-4 h-4 ml-2 group-hover:translate-x-1 transition-transform" />
                </Button>
              </Link>
            </div>
          ) : (
            <div className="space-y-4">
              {orders.map((order) => (
                <Card key={order.id} className="glass-card rounded-2xl overflow-hidden border-0 hover:shadow-lg transition-all duration-300">
                  <CardContent className="p-6">
                    <div className="flex flex-col sm:flex-row justify-between items-start gap-4">
                      <div className="flex-1">
                        <div className="flex items-center gap-3 mb-2">
                          <h3 className="text-lg font-bold text-slate-900">Order #{order.id}</h3>
                          <OrderStatusBadge status={order.status} />
                        </div>
                        <p className="text-sm text-slate-500">
                          Placed on {new Date(order.date).toLocaleDateString()}
                        </p>
                        <div className="flex items-center gap-4 mt-3">
                          <p className="text-sm text-slate-600">
                            {getItemCount(order)} item{getItemCount(order) !== 1 ? "s" : ""}
                          </p>
                          <PaymentStatusBadge 
                            status={order.payment_status || 'no_payment'} 
                            isPaid={order.is_paid}
                            className="text-xs"
                          />
                        </div>
                        <p className="text-xl font-bold bg-gradient-to-r from-blue-600 to-purple-600 bg-clip-text text-transparent mt-3">
                          ${(() => {
                            if (order.total_with_shipping) {
                              return parseFloat(order.total_with_shipping).toFixed(2)
                            }
                            const subtotal = parseFloat(order.subtotal || order.total)
                            const shipping = order.shipping?.cost || 0
                            return (subtotal + shipping).toFixed(2)
                          })()}
                        </p>
                      </div>
                      <div className="flex gap-2 flex-wrap sm:flex-nowrap">
                        <Button variant="outline" size="sm" asChild className="rounded-xl border-slate-200 hover:bg-slate-50">
                          <Link href={`/orders/${order.id}`}>
                            <Eye className="w-4 h-4 mr-1.5" />
                            View Details
                          </Link>
                        </Button>
                        
                        {order.can_continue_payment && (
                          <ContinuePaymentButton 
                            order={order}
                            variant="outline"
                            size="sm"
                            className="rounded-xl"
                          />
                        )}
                        
                        <CancelOrderButton 
                          order={order}
                          onOrderCancelled={handleOrderCancelled}
                          variant="outline"
                          size="sm"
                          className="rounded-xl"
                        />
                      </div>
                    </div>
                  </CardContent>
                </Card>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
