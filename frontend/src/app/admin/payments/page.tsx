"use client"

import React, { useState, useEffect } from "react"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Skeleton } from "@/components/ui/skeleton"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"

import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog"
import { 
  Search, 
  Filter, 
  RefreshCw, 
  Eye, 
  Download,
  CreditCard,
  DollarSign,
  TrendingUp,
  AlertCircle
} from "lucide-react"
import { paymentService, PaymentTransaction, PaymentStats } from "@/lib/services/payments"

function PaymentsSkeleton() {
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

export default function PaymentsPage() {
  const [payments, setPayments] = useState<PaymentTransaction[]>([])
  const [stats, setStats] = useState<PaymentStats | null>(null)
  const [loading, setLoading] = useState(true)
  const [searchTerm, setSearchTerm] = useState("")
  const [statusFilter, setStatusFilter] = useState<string>("all")
  const [selectedPayment, setSelectedPayment] = useState<PaymentTransaction | null>(null)

  useEffect(() => {
    fetchPayments()
    fetchStats()
  }, [])

  const fetchPayments = async () => {
    try {
      setLoading(true)
      const data = await paymentService.getAdminPaymentTransactions()
      setPayments(data)
    } catch (error) {
      console.error('Error fetching payments:', error)
    } finally {
      setLoading(false)
    }
  }

  const fetchStats = async () => {
    try {
      const data = await paymentService.getAdminPaymentStats()
      setStats(data)
    } catch (error) {
      console.error('Error fetching payment stats:', error)
    }
  }

  const getStatusBadge = (status: string) => {
    const variants: Record<string, string> = {
      pending: "bg-yellow-100 text-yellow-700 border-0",
      success: "bg-green-100 text-green-700 border-0",
      failed: "bg-red-100 text-red-700 border-0",
      refunded: "bg-blue-100 text-blue-700 border-0",
      canceled: "bg-slate-100 text-slate-700 border-0",
      cancelled: "bg-slate-100 text-slate-700 border-0"
    }
    
    return (
      <Badge className={`text-xs rounded-full px-2.5 py-1 ${variants[status] || "bg-slate-100 text-slate-700 border-0"}`}>
        {status.charAt(0).toUpperCase() + status.slice(1)}
      </Badge>
    )
  }

  const filteredPayments = payments.filter(payment => {
    const matchesSearch = 
      payment.stripe_checkout_id.toLowerCase().includes(searchTerm.toLowerCase()) ||
      payment.order_id.toString().includes(searchTerm)
    const matchesStatus = statusFilter === "all" || payment.status === statusFilter
    return matchesSearch && matchesStatus
  })

  const formatCurrency = (amount: string | number) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD'
    }).format(typeof amount === 'string' ? parseFloat(amount) : amount)
  }

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      month: 'short',
      day: 'numeric',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    })
  }

  const getCustomerName = (payment: PaymentTransaction) => {
    if (payment.order?.user) {
      return `${payment.order.user.first_name} ${payment.order.user.last_name}`.trim()
    }
    return null
  }

  const getCustomerEmail = (payment: PaymentTransaction) => {
    return payment.order?.user?.email || null
  }

  const exportPayments = () => {
    const csvContent = [
      ['Order ID', 'Checkout ID', 'Amount', 'Status', 'Date'].join(','),
      ...filteredPayments.map(payment => [
        payment.order_id,
        payment.stripe_checkout_id,
        payment.amount,
        payment.status,
        payment.created_at
      ].join(','))
    ].join('\n')

    const blob = new Blob([csvContent], { type: 'text/csv' })
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `payments-${new Date().toISOString().split('T')[0]}.csv`
    a.click()
    window.URL.revokeObjectURL(url)
  }

  if (loading) {
    return <PaymentsSkeleton />
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl sm:text-3xl font-bold tracking-tight text-slate-900">Payment Transactions</h1>
          <p className="text-slate-500 text-sm">Manage and monitor all payment transactions</p>
        </div>
        <div className="flex gap-2">
          <Button onClick={fetchPayments} variant="outline" size="sm" className="rounded-xl h-9">
            <RefreshCw className="h-4 w-4 mr-1.5" />
            Refresh
          </Button>
          <Button onClick={exportPayments} variant="outline" size="sm" className="rounded-xl h-9">
            <Download className="h-4 w-4 mr-1.5" />
            Export
          </Button>
        </div>
      </div>

      {/* Stats Cards */}
      {stats && (
        <div className="grid grid-cols-2 lg:grid-cols-4 gap-3 sm:gap-4">
          {[
            { label: "Total", value: stats.total_transactions, icon: CreditCard, color: "from-blue-500 to-indigo-500" },
            { label: "Revenue", value: formatCurrency(stats.total_amount), icon: DollarSign, color: "from-green-500 to-emerald-500" },
            { label: "Successful", value: stats.successful_transactions, icon: TrendingUp, color: "from-green-500 to-teal-500", textGreen: true },
            { label: "Failed/Pending", value: stats.failed_transactions + stats.pending_transactions, icon: AlertCircle, color: "from-red-500 to-rose-500", textRed: true },
          ].map((stat) => (
            <Card key={stat.label} className="border-0 shadow-sm bg-white rounded-2xl overflow-hidden hover:shadow-md transition-shadow">
              <CardContent className="p-4">
                <div className="flex items-start justify-between mb-3">
                  <p className="text-xs sm:text-sm font-medium text-slate-500">{stat.label}</p>
                  <div className={`w-9 h-9 bg-gradient-to-br ${stat.color} rounded-xl flex items-center justify-center shadow-lg`}>
                    <stat.icon className="h-4 w-4 text-white" />
                  </div>
                </div>
                <div className={`text-xl sm:text-2xl font-bold ${stat.textGreen ? 'text-green-600' : stat.textRed ? 'text-red-600' : 'text-slate-900'}`}>
                  {stat.value}
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}

      {/* Filters */}
      <Card className="border-0 shadow-sm bg-white rounded-2xl overflow-hidden">
        <CardContent className="p-4 sm:p-5">
          <div className="flex flex-col sm:flex-row gap-3">
            <div className="flex-1 relative">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-slate-400 w-4 h-4" />
              <Input
                placeholder="Search by order ID..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="pl-10 h-11 bg-slate-50 border-slate-200 rounded-xl focus:bg-white"
              />
            </div>
            <Select value={statusFilter} onValueChange={setStatusFilter}>
              <SelectTrigger className="w-full sm:w-40 h-11 bg-slate-50 border-slate-200 rounded-xl">
                <Filter className="h-4 w-4 mr-2 text-slate-400" />
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">All Status</SelectItem>
                <SelectItem value="pending">Pending</SelectItem>
                <SelectItem value="success">Success</SelectItem>
                <SelectItem value="failed">Failed</SelectItem>
                <SelectItem value="refunded">Refunded</SelectItem>
              </SelectContent>
            </Select>
          </div>
        </CardContent>
      </Card>

      {/* Payments Table */}
      <Card className="border-0 shadow-sm bg-white rounded-2xl overflow-hidden">
        <CardHeader className="p-4 sm:p-5 pb-0">
          <CardTitle className="text-lg font-bold text-slate-900">
            Transactions
            <span className="text-sm font-normal text-slate-500 ml-2">
              ({filteredPayments.length} of {payments.length})
            </span>
          </CardTitle>
        </CardHeader>
        <CardContent className="p-4 sm:p-5">
          {filteredPayments.length === 0 ? (
            <div className="text-center py-12">
              <div className="w-16 h-16 bg-slate-100 rounded-full flex items-center justify-center mx-auto mb-4">
                <CreditCard className="w-8 h-8 text-slate-300" />
              </div>
              <h3 className="text-lg font-medium text-slate-900 mb-1">No transactions found</h3>
              <p className="text-slate-500 text-sm">
                {searchTerm || statusFilter !== "all" 
                  ? "Try adjusting your search or filter"
                  : "Transactions will appear here once orders are processed"}
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
                  <span className="text-xs font-medium text-slate-500 uppercase tracking-wider">Amount</span>
                  <span className="text-xs font-medium text-slate-500 uppercase tracking-wider">Status</span>
                  <span className="text-xs font-medium text-slate-500 uppercase tracking-wider">Date</span>
                  <span></span>
                </div>
                {/* Rows */}
                {filteredPayments.map((payment) => {
                  const customerName = getCustomerName(payment)
                  const customerEmail = getCustomerEmail(payment)
                  return (
                    <div key={payment.id} className="grid grid-cols-[1fr_1fr_1fr_1fr_1fr_auto] gap-x-6 items-center px-4 py-4 border-b hover:bg-slate-50/50 transition-colors">
                      <span className="font-semibold text-slate-900">#{payment.order_id}</span>
                      <div>
                        {customerName ? (
                          <>
                            <p className="font-medium text-slate-900 text-sm">{customerName}</p>
                            {customerEmail && <p className="text-xs text-slate-500">{customerEmail}</p>}
                          </>
                        ) : (
                          <span className="text-slate-400">-</span>
                        )}
                      </div>
                      <span className="font-bold text-slate-900">{formatCurrency(payment.amount)}</span>
                      <div>{getStatusBadge(payment.status)}</div>
                      <span className="text-xs text-slate-500">{formatDate(payment.created_at)}</span>
                      <div className="flex justify-center">
                        <Dialog>
                          <DialogTrigger asChild>
                            <Button 
                              variant="ghost" 
                              size="icon"
                              className="h-8 w-8 rounded-lg hover:bg-slate-100"
                              onClick={() => setSelectedPayment(payment)}
                            >
                              <Eye className="h-4 w-4 text-slate-500" />
                            </Button>
                          </DialogTrigger>
                          <DialogContent className="w-[calc(100%-2rem)] max-w-md rounded-2xl p-0 overflow-hidden">
                            <div className="p-5">
                              <DialogHeader className="mb-4">
                                <DialogTitle className="text-lg font-bold text-slate-900">Payment Details</DialogTitle>
                              </DialogHeader>
                              {selectedPayment && (
                                <div className="space-y-3">
                                  <div className="grid grid-cols-2 gap-2">
                                    <div className="p-3 bg-slate-50 rounded-xl">
                                      <label className="text-[10px] font-medium text-slate-500 uppercase tracking-wide">Order ID</label>
                                      <p className="text-sm font-semibold text-slate-900 mt-0.5">#{selectedPayment.order_id}</p>
                                    </div>
                                    <div className="p-3 bg-slate-50 rounded-xl">
                                      <label className="text-[10px] font-medium text-slate-500 uppercase tracking-wide">Amount</label>
                                      <p className="text-sm font-semibold text-slate-900 mt-0.5">{formatCurrency(selectedPayment.amount)}</p>
                                    </div>
                                    <div className="p-3 bg-slate-50 rounded-xl">
                                      <label className="text-[10px] font-medium text-slate-500 uppercase tracking-wide">Status</label>
                                      <div className="mt-1">{getStatusBadge(selectedPayment.status)}</div>
                                    </div>
                                    <div className="p-3 bg-slate-50 rounded-xl">
                                      <label className="text-[10px] font-medium text-slate-500 uppercase tracking-wide">Date</label>
                                      <p className="text-xs text-slate-900 mt-0.5">{formatDate(selectedPayment.created_at)}</p>
                                    </div>
                                  </div>
                                  <div className="p-3 bg-slate-50 rounded-xl">
                                    <label className="text-[10px] font-medium text-slate-500 uppercase tracking-wide">Checkout ID</label>
                                    <p className="text-[11px] text-slate-700 font-mono break-all mt-1 leading-relaxed">{selectedPayment.stripe_checkout_id}</p>
                                  </div>
                                  {selectedPayment.stripe_payment_intent && (
                                    <div className="p-3 bg-slate-50 rounded-xl">
                                      <label className="text-[10px] font-medium text-slate-500 uppercase tracking-wide">Payment Intent</label>
                                      <p className="text-[11px] text-slate-700 font-mono break-all mt-1 leading-relaxed">{selectedPayment.stripe_payment_intent}</p>
                                    </div>
                                  )}
                                  {customerName && (
                                    <div className="p-3 bg-slate-50 rounded-xl">
                                      <label className="text-[10px] font-medium text-slate-500 uppercase tracking-wide">Customer</label>
                                      <p className="text-sm font-medium text-slate-900 mt-0.5">{customerName}</p>
                                      {customerEmail && <p className="text-xs text-slate-500">{customerEmail}</p>}
                                    </div>
                                  )}
                                </div>
                              )}
                            </div>
                          </DialogContent>
                        </Dialog>
                      </div>
                    </div>
                  )
                })}
              </div>

              {/* Mobile Cards */}
              <div className="md:hidden space-y-3">
                {filteredPayments.map((payment) => {
                  const customerName = getCustomerName(payment)
                  return (
                    <div key={payment.id} className="p-4 bg-slate-50 rounded-xl space-y-3">
                      <div className="flex items-center justify-between">
                        <span className="font-semibold text-slate-900">#{payment.order_id}</span>
                        {getStatusBadge(payment.status)}
                      </div>
                      {customerName && (
                        <p className="text-sm text-slate-600">{customerName}</p>
                      )}
                      <div className="flex items-center justify-between">
                        <span className="font-bold text-slate-900">{formatCurrency(payment.amount)}</span>
                        <span className="text-xs text-slate-500">{formatDate(payment.created_at)}</span>
                      </div>
                      <Dialog>
                        <DialogTrigger asChild>
                          <Button 
                            variant="outline" 
                            size="sm" 
                            className="w-full rounded-xl"
                            onClick={() => setSelectedPayment(payment)}
                          >
                            <Eye className="h-4 w-4 mr-2" />
                            View Details
                          </Button>
                        </DialogTrigger>
                        <DialogContent className="w-[calc(100%-2rem)] max-w-md rounded-2xl p-0 overflow-hidden">
                          <div className="p-5">
                            <DialogHeader className="mb-4">
                              <DialogTitle className="text-lg font-bold text-slate-900">Payment Details</DialogTitle>
                            </DialogHeader>
                            {selectedPayment && (
                              <div className="space-y-3">
                                <div className="grid grid-cols-2 gap-2">
                                  <div className="p-3 bg-slate-50 rounded-xl">
                                    <label className="text-[10px] font-medium text-slate-500 uppercase tracking-wide">Order ID</label>
                                    <p className="text-sm font-semibold text-slate-900 mt-0.5">#{selectedPayment.order_id}</p>
                                  </div>
                                  <div className="p-3 bg-slate-50 rounded-xl">
                                    <label className="text-[10px] font-medium text-slate-500 uppercase tracking-wide">Amount</label>
                                    <p className="text-sm font-semibold text-slate-900 mt-0.5">{formatCurrency(selectedPayment.amount)}</p>
                                  </div>
                                  <div className="p-3 bg-slate-50 rounded-xl">
                                    <label className="text-[10px] font-medium text-slate-500 uppercase tracking-wide">Status</label>
                                    <div className="mt-1">{getStatusBadge(selectedPayment.status)}</div>
                                  </div>
                                  <div className="p-3 bg-slate-50 rounded-xl">
                                    <label className="text-[10px] font-medium text-slate-500 uppercase tracking-wide">Date</label>
                                    <p className="text-xs text-slate-900 mt-0.5">{formatDate(selectedPayment.created_at)}</p>
                                  </div>
                                </div>
                                <div className="p-3 bg-slate-50 rounded-xl">
                                  <label className="text-[10px] font-medium text-slate-500 uppercase tracking-wide">Checkout ID</label>
                                  <p className="text-[11px] text-slate-700 font-mono break-all mt-1 leading-relaxed">{selectedPayment.stripe_checkout_id}</p>
                                </div>
                              </div>
                            )}
                          </div>
                        </DialogContent>
                      </Dialog>
                    </div>
                  )
                })}
              </div>
            </>
          )}
        </CardContent>
      </Card>
    </div>
  )
}
