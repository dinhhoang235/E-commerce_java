"use client"

import { useState, useEffect } from "react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Badge } from "@/components/ui/badge"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Skeleton } from "@/components/ui/skeleton"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog"
import { Search, Eye, Package, Truck, CheckCircle, XCircle, Clock, AlertCircle } from "lucide-react"
import { adminOrdersApi, type Order, type OrderStats } from "@/lib/services/orders"
import { useToast } from "@/hooks/use-toast"
import { PaymentStatusBadge } from "@/components/ui/payment-status-badge"
import Link from "next/link"

function OrdersSkeleton() {
  return (
    <div className="space-y-6">
      <div>
        <Skeleton className="h-8 w-32 mb-2" />
        <Skeleton className="h-4 w-64" />
      </div>
      
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
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
            <Skeleton className="h-4 w-20" />
            <Skeleton className="h-6 w-20 rounded-full" />
            <Skeleton className="h-8 w-8 rounded-lg" />
          </div>
        ))}
      </div>
    </div>
  )
}

export default function AdminOrdersPage() {
  const [searchTerm, setSearchTerm] = useState("")
  const [statusFilter, setStatusFilter] = useState("all")
  const [orders, setOrders] = useState<Order[]>([])
  const [stats, setStats] = useState<OrderStats | null>(null)
  const [loading, setLoading] = useState(true)
  const [updating, setUpdating] = useState(false)
  const { toast } = useToast()

  useEffect(() => {
    fetchOrders()
    fetchStats()
  }, [])

  useEffect(() => {
    fetchOrders()
  }, [statusFilter])

  const fetchOrders = async () => {
    try {
      setLoading(true)
      const filters: any = {}
      if (statusFilter !== "all") filters.status = statusFilter
      if (searchTerm) filters.customer = searchTerm
      
      const ordersData = await adminOrdersApi.getOrders(filters)
      if (Array.isArray(ordersData)) {
        setOrders(ordersData)
      } else {
        setOrders([])
      }
    } catch (error) {
      setOrders([])
      toast({
        title: "Error",
        description: "Failed to fetch orders. Please try again.",
        variant: "destructive",
      })
    } finally {
      setLoading(false)
    }
  }

  const fetchStats = async () => {
    try {
      const statsData = await adminOrdersApi.getOrderStats()
      setStats(statsData)
    } catch (error) {
      console.error("Error fetching stats:", error)
    }
  }

  useEffect(() => {
    const timer = setTimeout(() => { fetchOrders() }, 500)
    return () => clearTimeout(timer)
  }, [searchTerm])

  const handleSearchChange = (value: string) => {
    setSearchTerm(value)
    if (value === "") fetchOrders()
  }

  const filteredOrders = Array.isArray(orders) ? orders.filter((order) => {
    const matchesSearch = order.id.toLowerCase().includes(searchTerm.toLowerCase()) || order.customer.toLowerCase().includes(searchTerm.toLowerCase())
    const matchesStatus = statusFilter === "all" || order.status === statusFilter
    return matchesSearch && matchesStatus
  }) : []

  const getStatusIcon = (status: string) => {
    switch (status) {
      case "pending": return <Clock className="h-3.5 w-3.5" />
      case "processing": return <Package className="h-3.5 w-3.5" />
      case "shipped": return <Truck className="h-3.5 w-3.5" />
      case "completed": return <CheckCircle className="h-3.5 w-3.5" />
      case "cancelled": return <XCircle className="h-3.5 w-3.5" />
      default: return <Package className="h-3.5 w-3.5" />
    }
  }

  const getStatusColor = (status: string) => {
    switch (status) {
      case "pending": return "bg-yellow-100 text-yellow-700 border-0"
      case "processing": return "bg-blue-100 text-blue-700 border-0"
      case "shipped": return "bg-purple-100 text-purple-700 border-0"
      case "completed": return "bg-green-100 text-green-700 border-0"
      case "cancelled": return "bg-red-100 text-red-700 border-0"
      case "refunded": return "bg-slate-100 text-slate-700 border-0"
      default: return "bg-slate-100 text-slate-700 border-0"
    }
  }

  const updateOrderStatus = async (orderId: string, newStatus: string) => {
    try {
      setUpdating(true)
      await adminOrdersApi.updateOrderStatus(orderId, newStatus as Order['status'])
      setOrders(orders.map(order => order.id === orderId ? { ...order, status: newStatus as Order['status'] } : order))
      fetchStats()
      toast({ title: "Success", description: `Order ${orderId} status updated to ${newStatus}` })
    } catch (error) {
      toast({ title: "Error", description: "Failed to update order status.", variant: "destructive" })
    } finally {
      setUpdating(false)
    }
  }

  if (loading && orders.length === 0) {
    return <OrdersSkeleton />
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <h1 className="text-2xl sm:text-3xl font-bold tracking-tight text-slate-900">Orders</h1>
        <p className="text-slate-500 text-sm sm:text-base">Manage customer orders and fulfillment</p>
      </div>

      {/* Stats Cards */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-3 sm:gap-4">
        {[
          { label: "Total Orders", value: stats?.total_orders || 0, icon: Package, color: "from-blue-500 to-indigo-500" },
          { label: "Pending", value: stats?.pending_orders || 0, icon: Clock, color: "from-yellow-500 to-amber-500" },
          { label: "Processing", value: stats?.processing_orders || 0, icon: Truck, color: "from-purple-500 to-pink-500" },
          { label: "Completed", value: stats?.completed_orders || 0, icon: CheckCircle, color: "from-green-500 to-emerald-500" },
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
                placeholder="Search by order ID or customer..."
                value={searchTerm}
                onChange={(e) => handleSearchChange(e.target.value)}
                className="pl-10 h-11 bg-slate-50 border-slate-200 rounded-xl focus:bg-white"
              />
            </div>
            <Select value={statusFilter} onValueChange={setStatusFilter}>
              <SelectTrigger className="w-full sm:w-48 h-11 bg-slate-50 border-slate-200 rounded-xl">
                <SelectValue placeholder="Status" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">All Status</SelectItem>
                <SelectItem value="pending">Pending</SelectItem>
                <SelectItem value="processing">Processing</SelectItem>
                <SelectItem value="shipped">Shipped</SelectItem>
                <SelectItem value="completed">Completed</SelectItem>
                <SelectItem value="cancelled">Cancelled</SelectItem>
                <SelectItem value="refunded">Refunded</SelectItem>
              </SelectContent>
            </Select>
          </div>
        </CardContent>
      </Card>

      {/* Orders List */}
      <Card className="border-0 shadow-sm bg-white rounded-2xl overflow-hidden">
        <CardHeader className="p-4 sm:p-5 pb-0">
          <CardTitle className="text-lg font-bold text-slate-900">
            Orders ({filteredOrders.length})
          </CardTitle>
        </CardHeader>
        <CardContent className="p-4 sm:p-5">
          {filteredOrders.length === 0 ? (
            <div className="text-center py-12">
              <div className="w-16 h-16 bg-slate-100 rounded-full flex items-center justify-center mx-auto mb-4">
                <AlertCircle className="w-8 h-8 text-slate-300" />
              </div>
              <p className="text-slate-500 font-medium">
                {searchTerm || statusFilter !== "all" ? "No orders match your filters." : "No orders found."}
              </p>
            </div>
          ) : (
            <>
              {/* Desktop Table */}
              <div className="hidden md:block space-y-0">
                {/* Header */}
                <div className="grid grid-cols-[1fr_1fr_1fr_1fr_1fr_auto] gap-x-6 px-4 py-3 border-b">
                  <span className="text-xs font-medium text-slate-500 uppercase tracking-wider">Order</span>
                  <span className="text-xs font-medium text-slate-500 uppercase tracking-wider">Customer</span>
                  <span className="text-xs font-medium text-slate-500 uppercase tracking-wider">Products</span>
                  <span className="text-xs font-medium text-slate-500 uppercase tracking-wider">Total</span>
                  <span className="text-xs font-medium text-slate-500 uppercase tracking-wider">Status</span>
                  <span></span>
                </div>
                {/* Rows */}
                {filteredOrders.map((order) => (
                  <div key={order.id} className="grid grid-cols-[1fr_1fr_1fr_1fr_1fr_auto] gap-x-6 items-center px-4 py-4 border-b hover:bg-slate-50/50 transition-colors">
                    <div>
                      <Link href={`/admin/orders/${order.id}`} className="font-semibold text-blue-600 hover:text-blue-700 text-sm">
                        {order.id}
                      </Link>
                      <p className="text-xs text-slate-500">{new Date(order.date).toLocaleDateString()}</p>
                    </div>
                    <div>
                      <p className="font-medium text-slate-900 text-sm">{order.customer}</p>
                      <p className="text-xs text-slate-500">{order.email}</p>
                    </div>
                    <div className="flex items-center gap-2 min-w-0">
                      {order.items?.slice(0, 2).map((item, idx) => (
                        <div key={idx} className="flex items-center gap-2 bg-slate-50 px-2 py-1 rounded-lg">
                          {item.product_variant_image && (
                            <img src={item.product_variant_image} alt="" className="w-5 h-5 object-cover rounded" />
                          )}
                          <span className="text-xs text-slate-600 truncate max-w-[100px]">{item.product_variant_name}</span>
                        </div>
                      ))}
                      {order.items && order.items.length > 2 && (
                        <span className="text-xs text-slate-400">+{order.items.length - 2}</span>
                      )}
                    </div>
                    <span className="font-bold text-slate-900">
                      ${(() => {
                        if (order.total_with_shipping) return parseFloat(order.total_with_shipping).toFixed(2)
                        const subtotal = parseFloat(order.subtotal || order.total)
                        const shipping = order.shipping?.cost || 0
                        return (subtotal + shipping).toFixed(2)
                      })()}
                    </span>
                    <div className="flex items-center gap-2">
                      <Badge className={`text-xs rounded-full px-2.5 py-1 ${getStatusColor(order.status)}`}>
                        {getStatusIcon(order.status)}
                        <span className="ml-1 capitalize">{order.status}</span>
                      </Badge>
                      <PaymentStatusBadge status={order.payment_status || 'no_payment'} isPaid={order.is_paid} className="text-xs" />
                    </div>
                    <div>
                      <Link href={`/admin/orders/${order.id}`}>
                        <Button variant="ghost" size="icon" className="h-9 w-9 rounded-xl hover:bg-slate-100">
                          <Eye className="h-4 w-4 text-slate-500" />
                        </Button>
                      </Link>
                    </div>
                  </div>
                ))}
              </div>

              {/* Mobile Cards */}
              <div className="md:hidden space-y-3">
                {filteredOrders.map((order) => (
                  <div key={order.id} className="p-4 bg-slate-50 rounded-xl space-y-3">
                    <div className="flex items-center justify-between">
                      <Link href={`/admin/orders/${order.id}`} className="font-semibold text-blue-600 hover:text-blue-700 text-sm">
                        {order.id}
                      </Link>
                      <Badge className={`text-xs rounded-full px-2.5 py-1 ${getStatusColor(order.status)}`}>
                        {getStatusIcon(order.status)}
                        <span className="ml-1 capitalize">{order.status}</span>
                      </Badge>
                    </div>
                    <div className="flex items-center justify-between">
                      <div className="min-w-0">
                        <p className="font-medium text-slate-900 text-sm">{order.customer}</p>
                        <p className="text-xs text-slate-500">{order.email}</p>
                      </div>
                      {order.items && order.items.length > 0 && (
                        <div className="flex items-center gap-1.5 flex-shrink-0 ml-3">
                          <span className="text-xs bg-white text-slate-600 px-2 py-1 rounded-lg border border-slate-200 truncate max-w-[140px]">
                            {order.items[0].product_variant_name}
                          </span>
                          {order.items.length > 1 && (
                            <span className="text-xs text-slate-400">+{order.items.length - 1}</span>
                          )}
                        </div>
                      )}
                    </div>
                    <div className="flex items-center justify-between">
                      <p className="font-bold text-slate-900">
                        ${(() => {
                          if (order.total_with_shipping) return parseFloat(order.total_with_shipping).toFixed(2)
                          const subtotal = parseFloat(order.subtotal || order.total)
                          const shipping = order.shipping?.cost || 0
                          return (subtotal + shipping).toFixed(2)
                        })()}
                      </p>
                      <Link href={`/admin/orders/${order.id}`}>
                        <Button variant="outline" size="sm" className="rounded-xl">
                          <Eye className="h-4 w-4 mr-2" />
                          View
                        </Button>
                      </Link>
                    </div>
                  </div>
                ))}
              </div>
            </>
          )}
        </CardContent>
      </Card>
    </div>
  )
}
