"use client"

import { useState, useEffect } from "react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Badge } from "@/components/ui/badge"
import { Card, CardContent } from "@/components/ui/card"
import { Skeleton } from "@/components/ui/skeleton"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Search, Eye, Mail, Phone, MapPin, Users, UserCheck, DollarSign, TrendingUp, AlertCircle } from "lucide-react"
import { useCustomers, useCustomerStats } from "@/hooks/useCustomers"
import { toast } from "sonner"

function CustomersSkeleton() {
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

export default function AdminCustomersPage() {
  const [searchTerm, setSearchTerm] = useState("")
  const [statusFilter, setStatusFilter] = useState<"all" | "active" | "inactive">("all")

  const { stats, loading: statsLoading } = useCustomerStats()
  const {
    customers,
    loading: customersLoading,
    error,
    totalCustomers: totalCustomersCount,
    fetchCustomers
  } = useCustomers()

  useEffect(() => {
    const timer = setTimeout(() => {
      fetchCustomers({
        search: searchTerm || undefined,
        status: statusFilter === "all" ? undefined : statusFilter,
        page: 1,
        page_size: 50
      })
    }, 500)

    return () => clearTimeout(timer)
  }, [searchTerm, statusFilter])

  useEffect(() => {
    if (error) {
      toast.error(error)
    }
  }, [error])

  const filteredCustomers = customers

  if (customersLoading && filteredCustomers.length === 0) {
    return <CustomersSkeleton />
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <h1 className="text-2xl sm:text-3xl font-bold tracking-tight text-slate-900">Customers</h1>
        <p className="text-slate-500 text-sm sm:text-base">Manage your customer base and relationships</p>
      </div>

      {/* Stats Cards */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-3 sm:gap-4">
        {[
          { label: "Total Customers", value: stats.totalCustomers, icon: Users, color: "from-blue-500 to-indigo-500" },
          { label: "Active Customers", value: stats.activeCustomers, icon: UserCheck, color: "from-green-500 to-emerald-500" },
          { label: "Total Revenue", value: `$${stats.totalRevenue.toLocaleString()}`, icon: DollarSign, color: "from-purple-500 to-pink-500" },
          { label: "Avg Order Value", value: `$${stats.avgOrderValue.toFixed(0)}`, icon: TrendingUp, color: "from-yellow-500 to-amber-500" },
        ].map((stat) => (
          <Card key={stat.label} className="border-0 shadow-sm bg-white rounded-2xl overflow-hidden hover:shadow-md transition-shadow">
            <CardContent className="p-4 sm:p-5">
              <div className="flex items-start justify-between mb-3">
                <p className="text-xs sm:text-sm font-medium text-slate-500">{stat.label}</p>
                <div className={`w-9 h-9 sm:w-10 sm:h-10 bg-gradient-to-br ${stat.color} rounded-xl flex items-center justify-center shadow-lg`}>
                  <stat.icon className="h-4 w-4 sm:h-5 sm:w-5 text-white" />
                </div>
              </div>
              <div className="text-xl sm:text-2xl font-bold text-slate-900">
                {statsLoading ? <Skeleton className="h-7 w-16 inline-block" /> : stat.value}
              </div>
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
                placeholder="Search customers..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="pl-10 h-11 bg-slate-50 border-slate-200 rounded-xl focus:bg-white"
              />
            </div>
            <Select value={statusFilter} onValueChange={(value) => setStatusFilter(value as "all" | "active" | "inactive")}>
              <SelectTrigger className="w-full sm:w-48 h-11 bg-slate-50 border-slate-200 rounded-xl">
                <SelectValue placeholder="Status" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">All Status</SelectItem>
                <SelectItem value="active">Active</SelectItem>
                <SelectItem value="inactive">Inactive</SelectItem>
              </SelectContent>
            </Select>
          </div>
        </CardContent>
      </Card>

      {/* Customers List */}
      <Card className="border-0 shadow-sm bg-white rounded-2xl overflow-hidden">
        <CardContent className="p-4 sm:p-5">
          {filteredCustomers.length === 0 ? (
            <div className="text-center py-12">
              <div className="w-16 h-16 bg-slate-100 rounded-full flex items-center justify-center mx-auto mb-4">
                <AlertCircle className="w-8 h-8 text-slate-300" />
              </div>
              <p className="text-slate-500 font-medium">
                {searchTerm || statusFilter !== "all" ? "No customers match your filters." : "No customers found."}
              </p>
            </div>
          ) : (
            <>
              {/* Desktop Table */}
              <div className="hidden md:block space-y-0">
                {/* Header */}
                <div className="grid grid-cols-[1fr_1fr_1fr_1fr_1fr_1fr_1fr_auto] gap-x-6 px-4 py-3 border-b">
                  <span className="text-xs font-medium text-slate-500 uppercase tracking-wider">Customer</span>
                  <span className="text-xs font-medium text-slate-500 uppercase tracking-wider">Contact</span>
                  <span className="text-xs font-medium text-slate-500 uppercase tracking-wider">Location</span>
                  <span className="text-xs font-medium text-slate-500 uppercase tracking-wider">Orders</span>
                  <span className="text-xs font-medium text-slate-500 uppercase tracking-wider">Total Spent</span>
                  <span className="text-xs font-medium text-slate-500 uppercase tracking-wider">Status</span>
                  <span className="text-xs font-medium text-slate-500 uppercase tracking-wider">Join Date</span>
                  <span className="text-xs font-medium text-slate-500 uppercase tracking-wider">Actions</span>
                </div>
                {/* Rows */}
                {filteredCustomers.map((customer) => (
                  <div key={customer.id} className="grid grid-cols-[1fr_1fr_1fr_1fr_1fr_1fr_1fr_auto] gap-x-6 items-center px-4 py-4 border-b hover:bg-slate-50/50 transition-colors">
                    <div>
                      <p className="font-semibold text-slate-900 text-sm">{customer.name}</p>
                      <p className="text-xs text-slate-500">ID: {customer.id}</p>
                    </div>
                    <div className="space-y-1">
                      <div className="flex items-center text-sm text-slate-600">
                        <Mail className="h-3 w-3 mr-1.5 text-slate-400" />
                        {customer.email}
                      </div>
                      {customer.phone && (
                        <div className="flex items-center text-sm text-slate-600">
                          <Phone className="h-3 w-3 mr-1.5 text-slate-400" />
                          {customer.phone}
                        </div>
                      )}
                    </div>
                    <div className="flex items-center text-sm text-slate-600">
                      <MapPin className="h-3 w-3 mr-1.5 text-slate-400" />
                      {customer.location}
                    </div>
                    <span className="font-medium text-slate-900 text-sm">{customer.orders}</span>
                    <span className="font-bold text-slate-900">${customer.totalSpent.toLocaleString()}</span>
                    <Badge className={`text-xs rounded-full px-2.5 py-1 w-fit ${customer.status === "active" ? "bg-green-100 text-green-700 border-0" : "bg-slate-100 text-slate-700 border-0"}`}>
                      {customer.status}
                    </Badge>
                    <span className="text-sm text-slate-500">{new Date(customer.joinDate).toLocaleDateString()}</span>
                    <div>
                      <Button variant="ghost" size="icon" className="h-9 w-9 rounded-xl hover:bg-slate-100">
                        <Eye className="h-4 w-4 text-slate-500" />
                      </Button>
                    </div>
                  </div>
                ))}
              </div>

              {/* Mobile Cards */}
              <div className="md:hidden space-y-3">
                {filteredCustomers.map((customer) => (
                  <div key={customer.id} className="p-4 bg-slate-50 rounded-xl space-y-3">
                    <div className="flex items-center justify-between">
                      <p className="font-semibold text-slate-900 text-sm">{customer.name}</p>
                      <Badge className={`text-xs rounded-full px-2.5 py-1 ${customer.status === "active" ? "bg-green-100 text-green-700 border-0" : "bg-slate-100 text-slate-700 border-0"}`}>
                        {customer.status}
                      </Badge>
                    </div>
                    <div className="space-y-1">
                      <div className="flex items-center text-xs text-slate-500">
                        <Mail className="h-3 w-3 mr-1.5" />
                        {customer.email}
                      </div>
                      {customer.phone && (
                        <div className="flex items-center text-xs text-slate-500">
                          <Phone className="h-3 w-3 mr-1.5" />
                          {customer.phone}
                        </div>
                      )}
                    </div>
                    <div className="flex items-center justify-between">
                      <div className="flex items-center gap-3 text-xs text-slate-500">
                        <span><strong className="text-slate-900">{customer.orders}</strong> orders</span>
                        <span className="font-bold text-slate-900">${customer.totalSpent.toLocaleString()}</span>
                      </div>
                      <Button variant="outline" size="sm" className="rounded-xl">
                        <Eye className="h-4 w-4 mr-2" />
                        View
                      </Button>
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
