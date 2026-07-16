"use client"

import { useEffect, useState } from "react"
import Link from "next/link"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { Skeleton } from "@/components/ui/skeleton"
import { DollarSign, Package, ShoppingCart, Users, TrendingUp, TrendingDown, Eye, ArrowUpRight, ArrowRight, BarChart3, ShoppingBag, BoxesIcon } from "lucide-react"
import { adminPanelApi, transformToDashboardStats, formatCurrency, DashboardStats, RecentOrder, TopProduct } from "@/lib/services/adminpanel"

const iconMap = {
  DollarSign,
  ShoppingCart,
  Package,
  Users
}

const iconColors = {
  DollarSign: "from-green-500 to-emerald-500",
  ShoppingCart: "from-blue-500 to-indigo-500",
  Package: "from-purple-500 to-pink-500",
  Users: "from-orange-500 to-amber-500"
}

interface TopProductDisplay extends Omit<TopProduct, 'revenue'> {
  revenue: string
}

function DashboardSkeleton() {
  return (
    <div className="space-y-8">
      <div>
        <Skeleton className="h-8 w-48 mb-2" />
        <Skeleton className="h-4 w-72" />
      </div>
      
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        {Array.from({ length: 4 }).map((_, i) => (
          <div key={i} className="p-5 bg-white rounded-2xl border border-slate-100 shadow-sm space-y-3">
            <div className="flex items-center justify-between">
              <Skeleton className="h-4 w-24" />
              <Skeleton className="h-10 w-10 rounded-xl" />
            </div>
            <Skeleton className="h-8 w-32" />
            <Skeleton className="h-3 w-28" />
          </div>
        ))}
      </div>
      
      <div className="grid gap-6 md:grid-cols-2">
        <div className="p-5 bg-white rounded-2xl border border-slate-100 shadow-sm space-y-4">
          <Skeleton className="h-6 w-32" />
          {Array.from({ length: 4 }).map((_, i) => (
            <div key={i} className="flex items-center justify-between py-2">
              <div className="flex items-center gap-3">
                <Skeleton className="h-8 w-8 rounded-lg" />
                <div className="space-y-1">
                  <Skeleton className="h-4 w-24" />
                  <Skeleton className="h-3 w-16" />
                </div>
              </div>
              <Skeleton className="h-4 w-16" />
            </div>
          ))}
        </div>
        
        <div className="p-5 bg-white rounded-2xl border border-slate-100 shadow-sm space-y-4">
          <Skeleton className="h-6 w-32" />
          {Array.from({ length: 4 }).map((_, i) => (
            <div key={i} className="flex items-center justify-between py-2">
              <div className="flex items-center gap-3">
                <Skeleton className="h-10 w-10 rounded-xl" />
                <div className="space-y-1">
                  <Skeleton className="h-4 w-32" />
                  <Skeleton className="h-3 w-20" />
                </div>
              </div>
              <Skeleton className="h-5 w-20 rounded-full" />
            </div>
          ))}
        </div>
      </div>
    </div>
  )
}

export default function AdminDashboard() {
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [stats, setStats] = useState<DashboardStats[]>([])
  const [recentOrders, setRecentOrders] = useState<RecentOrder[]>([])
  const [topProducts, setTopProducts] = useState<TopProductDisplay[]>([])

  useEffect(() => {
    const loadDashboardData = async () => {
      try {
        setLoading(true)
        setError(null)

        const [dashboardData, recentOrdersData] = await Promise.all([
          adminPanelApi.getDashboardData(),
          adminPanelApi.getRecentOrders(4)
        ])

        const dashboardStats = transformToDashboardStats(dashboardData)
        const formattedTopProducts = dashboardData.topProducts.map(product => ({
          ...product,
          revenue: formatCurrency(product.revenue)
        }))

        setStats(dashboardStats)
        setRecentOrders(recentOrdersData)
        setTopProducts(formattedTopProducts)

      } catch (err) {
        console.error('Failed to load dashboard data:', err)
        setError('Failed to load dashboard data. Please try again.')
      } finally {
        setLoading(false)
      }
    }

    loadDashboardData()
  }, [])

  if (loading) {
    return <DashboardSkeleton />
  }

  return (
    <div className="space-y-6 sm:space-y-8">
      {/* Header */}
      <div>
        <h1 className="text-2xl sm:text-3xl font-bold tracking-tight text-slate-900">Dashboard</h1>
        <p className="text-slate-500 text-sm sm:text-base">Welcome back! Here's what's happening with your store.</p>
      </div>

      {/* Error State */}
      {error && (
        <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-xl">
          <p className="text-sm">{error}</p>
          <Button 
            variant="outline" 
            size="sm" 
            className="mt-2 rounded-lg"
            onClick={() => window.location.reload()}
          >
            Retry
          </Button>
        </div>
      )}

      {/* Stats Cards */}
      {!loading && (
        <>
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
            {stats.map((stat) => {
              const IconComponent = iconMap[stat.icon as keyof typeof iconMap]
              const colorClass = iconColors[stat.icon as keyof typeof iconColors]
              return (
                <Card key={stat.title} className="border-0 shadow-sm bg-white rounded-2xl overflow-hidden hover:shadow-md transition-shadow">
                  <CardContent className="p-5">
                    <div className="flex items-start justify-between mb-3">
                      <p className="text-sm font-medium text-slate-500">{stat.title}</p>
                      {IconComponent && (
                        <div className={`w-10 h-10 bg-gradient-to-br ${colorClass} rounded-xl flex items-center justify-center shadow-lg`}>
                          <IconComponent className="h-5 w-5 text-white" />
                        </div>
                      )}
                    </div>
                    <div className="text-2xl sm:text-3xl font-bold text-slate-900 mb-1">{stat.value}</div>
                    <div className="flex items-center text-xs">
                      {stat.trend === "up" ? (
                        <TrendingUp className="mr-1 h-3.5 w-3.5 text-green-500" />
                      ) : (
                        <TrendingDown className="mr-1 h-3.5 w-3.5 text-red-500" />
                      )}
                      <span className={stat.trend === "up" ? "text-green-600 font-medium" : "text-red-600 font-medium"}>
                        {stat.change}
                      </span>
                      <span className="text-slate-400 ml-1">from last month</span>
                    </div>
                  </CardContent>
                </Card>
              )
            })}
          </div>

          {/* Main Content Grid */}
          <div className="grid gap-6 md:grid-cols-2">
            {/* Recent Orders */}
            <Card className="border-0 shadow-sm bg-white rounded-2xl overflow-hidden">
              <CardHeader className="flex flex-row items-center justify-between p-5 pb-0">
                <CardTitle className="text-lg font-bold text-slate-900">Recent Orders</CardTitle>
                <Link href="/admin/orders">
                  <Button variant="ghost" size="sm" className="text-blue-600 hover:text-blue-700 hover:bg-blue-50 rounded-xl text-sm font-medium">
                    View All
                    <ArrowRight className="ml-1 h-4 w-4" />
                  </Button>
                </Link>
              </CardHeader>
              <CardContent className="p-5">
                {recentOrders.length === 0 ? (
                  <div className="text-center py-8">
                    <div className="w-14 h-14 bg-slate-100 rounded-full flex items-center justify-center mx-auto mb-3">
                      <ShoppingBag className="w-7 h-7 text-slate-300" />
                    </div>
                    <p className="text-slate-400 text-sm">No recent orders found</p>
                  </div>
                ) : (
                  <div className="space-y-3">
                    {recentOrders.map((order) => (
                      <div key={order.id} className="flex items-center justify-between p-3 bg-slate-50 rounded-xl hover:bg-slate-100 transition-colors">
                        <div className="flex items-center gap-3 min-w-0">
                          <div className="w-9 h-9 bg-white rounded-lg flex items-center justify-center shadow-sm flex-shrink-0">
                            <span className="text-xs font-bold text-slate-600">#{order.id.slice(-4)}</span>
                          </div>
                          <div className="min-w-0">
                            <p className="font-medium text-slate-900 text-sm truncate">{order.customer}</p>
                            <p className="text-xs text-slate-500">{order.amount}</p>
                          </div>
                        </div>
                        <div className="flex items-center gap-2 flex-shrink-0">
                          <Badge
                            variant={
                              order.status === "completed" ? "default"
                              : order.status === "processing" ? "secondary"
                              : "outline"
                            }
                            className={`text-xs rounded-full px-2.5 ${
                              order.status === "completed" ? "bg-green-100 text-green-700 border-0"
                              : order.status === "processing" ? "bg-blue-100 text-blue-700 border-0"
                              : ""
                            }`}
                          >
                            {order.status}
                          </Badge>
                          <Link href={`/admin/orders/${order.id}`}>
                            <Button variant="ghost" size="icon" className="h-8 w-8 rounded-lg hover:bg-white">
                              <Eye className="h-4 w-4 text-slate-400" />
                            </Button>
                          </Link>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </CardContent>
            </Card>

            {/* Top Products */}
            <Card className="border-0 shadow-sm bg-white rounded-2xl overflow-hidden">
              <CardHeader className="flex flex-row items-center justify-between p-5 pb-0">
                <CardTitle className="text-lg font-bold text-slate-900">Top Products</CardTitle>
                <Link href="/admin/products">
                  <Button variant="ghost" size="sm" className="text-blue-600 hover:text-blue-700 hover:bg-blue-50 rounded-xl text-sm font-medium">
                    View All
                    <ArrowRight className="ml-1 h-4 w-4" />
                  </Button>
                </Link>
              </CardHeader>
              <CardContent className="p-5">
                {topProducts.length === 0 ? (
                  <div className="text-center py-8">
                    <div className="w-14 h-14 bg-slate-100 rounded-full flex items-center justify-center mx-auto mb-3">
                      <Package className="w-7 h-7 text-slate-300" />
                    </div>
                    <p className="text-slate-400 text-sm">No products found</p>
                  </div>
                ) : (
                  <div className="space-y-3">
                    {topProducts.map((product, index) => (
                      <div key={product.name} className="flex items-center justify-between p-3 bg-slate-50 rounded-xl hover:bg-slate-100 transition-colors">
                        <div className="flex items-center gap-3 min-w-0">
                          <div className={`w-9 h-9 rounded-lg flex items-center justify-center flex-shrink-0 ${
                            index === 0 ? "bg-gradient-to-br from-yellow-400 to-orange-400" 
                            : index === 1 ? "bg-gradient-to-br from-slate-300 to-slate-400"
                            : index === 2 ? "bg-gradient-to-br from-amber-600 to-amber-700"
                            : "bg-gradient-to-br from-slate-200 to-slate-300"
                          }`}>
                            <span className="text-xs font-bold text-white">{index + 1}</span>
                          </div>
                          <div className="min-w-0">
                            <p className="font-medium text-slate-900 text-sm truncate">{product.name}</p>
                            <p className="text-xs text-slate-500">{product.sales} sales</p>
                          </div>
                        </div>
                        <p className="font-semibold text-slate-900 text-sm flex-shrink-0">{product.revenue}</p>
                      </div>
                    ))}
                  </div>
                )}
              </CardContent>
            </Card>
          </div>

          {/* Quick Actions */}
          <Card className="border-0 shadow-sm bg-white rounded-2xl overflow-hidden">
            <CardHeader className="p-5 pb-0">
              <CardTitle className="text-lg font-bold text-slate-900">Quick Actions</CardTitle>
            </CardHeader>
            <CardContent className="p-5">
              <div className="grid grid-cols-2 md:grid-cols-4 gap-3 sm:gap-4">
                <Link href="/admin/products">
                  <div className="group p-4 bg-gradient-to-br from-blue-50 to-indigo-50 rounded-2xl border border-blue-100 hover:shadow-md transition-all duration-300 cursor-pointer">
                    <div className="w-10 h-10 bg-gradient-to-br from-blue-500 to-indigo-500 rounded-xl flex items-center justify-center mb-3 group-hover:scale-110 transition-transform">
                      <Package className="h-5 w-5 text-white" />
                    </div>
                    <p className="font-semibold text-slate-900 text-sm">Products</p>
                    <p className="text-xs text-slate-500 mt-0.5">Manage inventory</p>
                  </div>
                </Link>
                <Link href="/admin/orders">
                  <div className="group p-4 bg-gradient-to-br from-purple-50 to-pink-50 rounded-2xl border border-purple-100 hover:shadow-md transition-all duration-300 cursor-pointer">
                    <div className="w-10 h-10 bg-gradient-to-br from-purple-500 to-pink-500 rounded-xl flex items-center justify-center mb-3 group-hover:scale-110 transition-transform">
                      <ShoppingCart className="h-5 w-5 text-white" />
                    </div>
                    <p className="font-semibold text-slate-900 text-sm">Orders</p>
                    <p className="text-xs text-slate-500 mt-0.5">View all orders</p>
                  </div>
                </Link>
                <Link href="/admin/customers">
                  <div className="group p-4 bg-gradient-to-br from-green-50 to-emerald-50 rounded-2xl border border-green-100 hover:shadow-md transition-all duration-300 cursor-pointer">
                    <div className="w-10 h-10 bg-gradient-to-br from-green-500 to-emerald-500 rounded-xl flex items-center justify-center mb-3 group-hover:scale-110 transition-transform">
                      <Users className="h-5 w-5 text-white" />
                    </div>
                    <p className="font-semibold text-slate-900 text-sm">Customers</p>
                    <p className="text-xs text-slate-500 mt-0.5">Manage users</p>
                  </div>
                </Link>
                <Link href="/admin/analytics">
                  <div className="group p-4 bg-gradient-to-br from-orange-50 to-amber-50 rounded-2xl border border-orange-100 hover:shadow-md transition-all duration-300 cursor-pointer">
                    <div className="w-10 h-10 bg-gradient-to-br from-orange-500 to-amber-500 rounded-xl flex items-center justify-center mb-3 group-hover:scale-110 transition-transform">
                      <BarChart3 className="h-5 w-5 text-white" />
                    </div>
                    <p className="font-semibold text-slate-900 text-sm">Analytics</p>
                    <p className="text-xs text-slate-500 mt-0.5">View reports</p>
                  </div>
                </Link>
              </div>
            </CardContent>
          </Card>
        </>
      )}
    </div>
  )
}
