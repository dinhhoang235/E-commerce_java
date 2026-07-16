"use client"

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { Skeleton } from "@/components/ui/skeleton"
import { DollarSign, TrendingUp, TrendingDown, ShoppingCart, Eye } from "lucide-react"
import { useAnalytics } from "@/hooks/useAnalytics"

function AnalyticsSkeleton() {
  return (
    <div className="space-y-6">
      <div>
        <Skeleton className="h-8 w-48 mb-2" />
        <Skeleton className="h-4 w-64" />
      </div>
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-3 sm:gap-4">
        {Array.from({ length: 4 }).map((_, i) => (
          <div key={i} className="p-4 bg-white rounded-2xl border border-slate-100 shadow-sm space-y-2">
            <Skeleton className="h-4 w-24" />
            <Skeleton className="h-8 w-20" />
          </div>
        ))}
      </div>
      <div className="p-5 bg-white rounded-2xl border border-slate-100 shadow-sm">
        {Array.from({ length: 5 }).map((_, i) => (
          <div key={i} className="flex items-center gap-4 py-3 border-b last:border-0">
            <Skeleton className="h-4 w-20" />
            <Skeleton className="h-4 flex-1" />
            <Skeleton className="h-4 w-16" />
            <Skeleton className="h-6 w-20 rounded-full" />
          </div>
        ))}
      </div>
    </div>
  )
}

export default function AdminAnalyticsPage() {
  const { dashboard, loading, error, refetch } = useAnalytics()

  if (loading) {
    return <AnalyticsSkeleton />
  }

  if (error) {
    return (
      <div className="space-y-6">
        <div>
          <h1 className="text-2xl sm:text-3xl font-bold tracking-tight text-slate-900">Analytics</h1>
          <p className="text-red-600">Error loading analytics data: {error}</p>
          <Button onClick={refetch} variant="outline" size="sm" className="mt-3 rounded-xl h-9">
            Retry
          </Button>
        </div>
      </div>
    )
  }

  if (!dashboard) {
    return (
      <div className="space-y-6">
        <div>
          <h1 className="text-2xl sm:text-3xl font-bold tracking-tight text-slate-900">Analytics</h1>
          <p className="text-slate-500">No analytics data available</p>
        </div>
      </div>
    )
  }

  const { salesData, topProducts, customerMetrics, trafficSources, conversionRate } = dashboard

  const todayData = salesData.find(d => d.period === "Today")
  const thisWeekData = salesData.find(d => d.period === "This Week")

  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <h1 className="text-2xl sm:text-3xl font-bold tracking-tight text-slate-900">Analytics</h1>
        <p className="text-slate-500 text-sm">Track your store&apos;s performance and insights</p>
      </div>

      {/* Stat Cards */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-3 sm:gap-4">
        {[
          { label: "Today's Revenue", value: `$${todayData?.revenue.toLocaleString() || "0"}`, icon: DollarSign, color: "from-green-500 to-emerald-500", change: todayData?.change || "+0%", sub: "from yesterday", textGreen: true },
          { label: "Today's Orders", value: todayData?.orders || 0, icon: ShoppingCart, color: "from-blue-500 to-indigo-500", change: todayData?.change || "+0%", sub: "from yesterday", textGreen: true },
          { label: "Conversion Rate", value: conversionRate?.rate || "0.0%", icon: TrendingUp, color: "from-purple-500 to-pink-500", change: conversionRate?.change || "0%", sub: "from yesterday", textGreen: conversionRate?.trend === "up" },
          { label: "This Week Revenue", value: `$${thisWeekData?.revenue.toLocaleString() || "0"}`, icon: DollarSign, color: "from-amber-500 to-orange-500", change: thisWeekData?.change || "+0%", sub: "from last week", textGreen: true },
        ].map((stat) => (
          <Card key={stat.label} className="border-0 shadow-sm bg-white rounded-2xl overflow-hidden hover:shadow-md transition-shadow">
            <CardContent className="p-4">
              <div className="flex items-start justify-between mb-3">
                <p className="text-xs sm:text-sm font-medium text-slate-500">{stat.label}</p>
                <div className={`w-9 h-9 bg-gradient-to-br ${stat.color} rounded-xl flex items-center justify-center shadow-lg`}>
                  <stat.icon className="h-4 w-4 text-white" />
                </div>
              </div>
              <div className="text-xl sm:text-2xl font-bold text-slate-900">
                {stat.value}
              </div>
              <div className={`flex items-center text-xs mt-1 ${stat.textGreen ? "text-green-600" : "text-red-600"}`}>
                {stat.textGreen ? (
                  <TrendingUp className="mr-1 h-3 w-3" />
                ) : (
                  <TrendingDown className="mr-1 h-3 w-3" />
                )}
                {stat.change} {stat.sub}
              </div>
            </CardContent>
          </Card>
        ))}
      </div>

      {/* Sales Performance */}
      <Card className="border-0 shadow-sm bg-white rounded-2xl overflow-hidden">
        <CardHeader className="p-4 sm:p-5 pb-0">
          <CardTitle className="text-lg font-bold text-slate-900">Sales Performance</CardTitle>
        </CardHeader>
        <CardContent className="p-4 sm:p-5">
          {/* Desktop Table */}
          <div className="hidden md:block space-y-0">
            <div className="grid grid-cols-[1fr_1fr_1fr_1fr] gap-x-6 px-4 py-3 border-b">
              <span className="text-xs font-medium text-slate-500 uppercase tracking-wider">Period</span>
              <span className="text-xs font-medium text-slate-500 uppercase tracking-wider">Revenue</span>
              <span className="text-xs font-medium text-slate-500 uppercase tracking-wider">Orders</span>
              <span className="text-xs font-medium text-slate-500 uppercase tracking-wider">Change</span>
            </div>
            {salesData.map((data) => (
              <div key={data.period} className="grid grid-cols-[1fr_1fr_1fr_1fr] gap-x-6 items-center px-4 py-4 border-b hover:bg-slate-50/50 transition-colors">
                <span className="font-semibold text-slate-900">{data.period}</span>
                <span className="font-bold text-slate-900">${data.revenue.toLocaleString()}</span>
                <span className="text-slate-700">{data.orders}</span>
                <div>
                  <Badge className="text-xs rounded-full px-2.5 py-1 bg-green-100 text-green-700 border-0">
                    {data.change}
                  </Badge>
                </div>
              </div>
            ))}
          </div>

          {/* Mobile Cards */}
          <div className="md:hidden space-y-3">
            {salesData.map((data) => (
              <div key={data.period} className="p-4 bg-slate-50 rounded-xl space-y-2">
                <div className="flex items-center justify-between">
                  <span className="font-semibold text-slate-900">{data.period}</span>
                  <Badge className="text-xs rounded-full px-2.5 py-1 bg-green-100 text-green-700 border-0">
                    {data.change}
                  </Badge>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-sm text-slate-600">Revenue</span>
                  <span className="font-bold text-slate-900">${data.revenue.toLocaleString()}</span>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-sm text-slate-600">Orders</span>
                  <span className="text-slate-700">{data.orders}</span>
                </div>
              </div>
            ))}
          </div>
        </CardContent>
      </Card>

      <div className="grid gap-6 md:grid-cols-2">
        {/* Top Products */}
        <Card className="border-0 shadow-sm bg-white rounded-2xl overflow-hidden">
          <CardHeader className="p-4 sm:p-5 pb-0">
            <CardTitle className="text-lg font-bold text-slate-900">Top Performing Products</CardTitle>
          </CardHeader>
          <CardContent className="p-4 sm:p-5">
            <div className="space-y-4">
              {topProducts.map((product, index) => (
                <div key={product.name} className="flex items-center justify-between">
                  <div className="flex items-center space-x-3">
                    <div className="w-8 h-8 bg-slate-100 rounded-lg flex items-center justify-center">
                      <span className="text-sm font-medium">{index + 1}</span>
                    </div>
                    <div>
                      <p className="font-medium">{product.name}</p>
                      <div className="flex items-center space-x-4 text-sm text-slate-600">
                        <span>{product.sales} sales</span>
                        <span className="flex items-center">
                          <Eye className="h-3 w-3 mr-1" />
                          {product.views}
                        </span>
                      </div>
                    </div>
                  </div>
                  <div className="text-right">
                    <p className="font-medium">${product.revenue.toLocaleString()}</p>
                  </div>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>

        {/* Customer Metrics */}
        <Card className="border-0 shadow-sm bg-white rounded-2xl overflow-hidden">
          <CardHeader className="p-4 sm:p-5 pb-0">
            <CardTitle className="text-lg font-bold text-slate-900">Customer Metrics</CardTitle>
          </CardHeader>
          <CardContent className="p-4 sm:p-5">
            <div className="space-y-4">
              {customerMetrics.map((metric) => (
                <div key={metric.metric} className="flex items-center justify-between">
                  <div>
                    <p className="font-medium">{metric.metric}</p>
                    <div className="flex items-center text-xs">
                      {metric.trend === "up" ? (
                        <TrendingUp className="mr-1 h-3 w-3 text-green-600" />
                      ) : (
                        <TrendingDown className="mr-1 h-3 w-3 text-red-600" />
                      )}
                      <span className={metric.trend === "up" ? "text-green-600" : "text-red-600"}>{metric.change}</span>
                    </div>
                  </div>
                  <div className="text-right">
                    <p className="text-2xl font-bold">{metric.value}</p>
                  </div>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Traffic Sources */}
      <Card className="border-0 shadow-sm bg-white rounded-2xl overflow-hidden">
        <CardHeader className="p-4 sm:p-5 pb-0">
          <CardTitle className="text-lg font-bold text-slate-900">Traffic Sources</CardTitle>
        </CardHeader>
        <CardContent className="p-4 sm:p-5">
          <div className="space-y-4">
            {trafficSources.map((source) => (
              <div key={source.source} className="flex items-center justify-between">
                <div className="flex items-center space-x-3">
                  <div className="w-4 h-4 bg-blue-600 rounded-full"></div>
                  <span className="font-medium">{source.source}</span>
                </div>
                <div className="flex items-center space-x-4">
                  <span className="text-sm text-slate-600">{source.visitors.toLocaleString()} visitors</span>
                  <div className="w-20 bg-slate-200 rounded-full h-2">
                    <div className="bg-blue-600 h-2 rounded-full" style={{ width: `${source.percentage}%` }}></div>
                  </div>
                  <span className="text-sm font-medium w-8">{source.percentage}%</span>
                </div>
              </div>
            ))}
          </div>
        </CardContent>
      </Card>
    </div>
  )
}
