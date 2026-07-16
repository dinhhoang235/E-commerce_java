"use client"

import { useState, useEffect, useRef, useCallback } from "react"
import { useRouter } from "next/navigation"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Separator } from "@/components/ui/separator"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { Loader2, Truck, Shield, ArrowLeft, CreditCard } from "lucide-react"
import { Skeleton } from "@/components/ui/skeleton"
import { useCart } from "@/components/cart-provider"
import { useAuth } from "@/components/auth-provider"
import { useToast } from "@/hooks/use-toast"
import DirectPaymentButton from "@/components/direct-payment-button"
import { userOrdersApi } from "@/lib/services/orders"

export default function CheckoutPage() {
  const { items, total } = useCart()
  const { user, isLoading: authLoading } = useAuth()
  const router = useRouter()
  const { toast } = useToast()
  const [isProcessing, setIsProcessing] = useState(false)
  const [step, setStep] = useState(1)
  const [isLoading, setIsLoading] = useState(true)
  const [pendingOrder, setPendingOrder] = useState<any>(null)
  const [timeLeft, setTimeLeft] = useState<number>(60)
  const timerRef = useRef<NodeJS.Timeout | null>(null)

  const [shippingData, setShippingData] = useState({
    firstName: "",
    lastName: "",
    email: "",
    phone: "",
    address: "",
    city: "",
    state: "",
    zipCode: "",
    country: "Vietnam",
  })
  
  const [shippingMethod, setShippingMethod] = useState("standard")

  const [errors, setErrors] = useState<Record<string, string>>({})

  const shippingCosts = {
    standard: 0,
    express: 5,
    overnight: 10,
  }

  const subtotal = total
  const shippingCost = shippingCosts[shippingMethod as keyof typeof shippingCosts] || 0
  const finalTotal = total + shippingCost

  // Pre-fill shipping data when user loads
  useEffect(() => {
    if (user) {
      setShippingData({
        firstName: user.first_name || "",
        lastName: user.last_name || "",
        email: user.email || "",
        phone: user.phone || "",
        address: user.address?.address_line1 || "",
        city: user.address?.city || "",
        state: user.address?.state || "",
        zipCode: user.address?.zip_code || "",
        country: user.address?.country || "Vietnam",
      })
      setIsLoading(false)
    } else if (!authLoading) {
      setIsLoading(false)
    }
  }, [user, authLoading])

  // Redirect to login if not authenticated
  useEffect(() => {
    if (!authLoading && !user) {
      router.push("/login")
    }
  }, [user, authLoading, router])

  // Timer countdown - only depends on step and pendingOrder
  const handleTimeoutCancel = useCallback(async () => {
    if (pendingOrder?.id) {
      try {
        await userOrdersApi.cancelOrder(pendingOrder.id)
      } catch (error) {
        console.error('Error cancelling order on timeout:', error)
      }
    }
    toast({
      variant: "destructive",
      title: "Payment Timeout",
      description: "Payment time has expired. Your order has been cancelled.",
    })
    const params = new URLSearchParams()
    if (pendingOrder?.id) params.set('orderId', pendingOrder.id)
    if (finalTotal) params.set('total', finalTotal.toFixed(2))
    router.push(`/payment-timeout?${params.toString()}`)
  }, [pendingOrder, finalTotal, toast, router])

  useEffect(() => {
    if (step === 3 && pendingOrder && timeLeft > 0) {
      timerRef.current = setInterval(() => {
        setTimeLeft((prev) => {
          if (prev <= 1) {
            if (timerRef.current) {
              clearInterval(timerRef.current)
              timerRef.current = null
            }
            setTimeout(() => handleTimeoutCancel(), 0)
            return 0
          }
          return prev - 1
        })
      }, 1000)
    }

    return () => {
      if (timerRef.current) {
        clearInterval(timerRef.current)
        timerRef.current = null
      }
    }
  }, [step === 3, !!pendingOrder, handleTimeoutCancel])

  // Format time display
  const formatTime = (seconds: number) => {
    const minutes = Math.floor(seconds / 60)
    const remainingSeconds = seconds % 60
    return `${minutes}:${remainingSeconds.toString().padStart(2, '0')}`
  }

  // Calculate progress percentage (0-100)
  const getProgressPercentage = () => {
    return (timeLeft / 60) * 100
  }

  // Helper functions for formatting
  const getStatePlaceholder = (country: string) => {
    switch (country) {
      case "US":
        return "State"
      case "CA":
        return "Province"
      case "GB":
        return "County"
      default:
        return "State/Province"
    }
  }

  const getZipPlaceholder = (country: string) => {
    switch (country) {
      case "US":
        return "12345"
      case "CA":
        return "K1A 0A6"
      case "GB":
        return "SW1A 1AA"
      default:
        return "Postal Code"
    }
  }

  const validateStep = (stepNumber: number) => {
    const newErrors: Record<string, string> = {}

    if (stepNumber === 1) {
      // Validate shipping information
      if (!shippingData.firstName.trim()) newErrors.firstName = "First name is required"
      if (!shippingData.lastName.trim()) newErrors.lastName = "Last name is required"
      if (!shippingData.email.trim()) newErrors.email = "Email is required"
      else if (!/\S+@\S+\.\S+/.test(shippingData.email)) newErrors.email = "Email is invalid"
      if (!shippingData.address.trim()) newErrors.address = "Address is required"
      if (!shippingData.city.trim()) newErrors.city = "City is required"
      if (!shippingData.state.trim()) newErrors.state = "State/Province is required"
      if (!shippingData.zipCode.trim()) newErrors.zipCode = "ZIP/Postal code is required"
    }

    setErrors(newErrors)
    return Object.keys(newErrors).length === 0
  }

  const handleNext = () => {
    if (validateStep(step)) {
      setStep(step + 1)
    }
  }

  const handleCreatePendingOrder = async () => {
    try {
      setIsProcessing(true)
      
      // Create order in pending state from cart
      const orderData = {
        shipping_address: {
          first_name: shippingData.firstName,
          last_name: shippingData.lastName,
          email: shippingData.email,
          phone: shippingData.phone,
          address_line1: shippingData.address,
          city: shippingData.city,
          state: shippingData.state,
          zip_code: shippingData.zipCode,
          country: shippingData.country,
        },
        shipping_method: shippingMethod as "standard" | "express" | "overnight",
      }

      const order = await userOrdersApi.createOrderFromCart(orderData)
      setPendingOrder(order)
      setTimeLeft(60) // Reset timer to 1 minute
      setStep(3) // Move to payment step
      
      toast({
        title: "Order Created",
        description: `Order #${order.id} has been created. Please complete payment within 1 minute.`,
      })
    } catch (error: any) {
      console.error("Error creating order:", error)
      toast({
        variant: "destructive",
        title: "Order Creation Failed",
        description: error.message || "Failed to create order. Please try again.",
      })
    } finally {
      setIsProcessing(false)
    }
  }

  const handlePaymentStart = () => {
    setIsProcessing(true)
    console.log("Payment started - order will be created after successful payment")
  }

  const handlePaymentError = (error: string) => {
    setIsProcessing(false)
    toast({
      variant: "destructive",
      title: "Payment Error",
      description: error,
    })
  }

  if (authLoading || isLoading) {
    return (
      <div className="container mx-auto px-4 sm:px-6 py-6 sm:py-8">
        <div className="mb-4 sm:mb-6">
          <Skeleton className="h-8 sm:h-9 w-32 mb-2" />
        </div>
        
        <div className="grid lg:grid-cols-3 gap-6 sm:gap-8">
          <div className="lg:col-span-2 space-y-4 sm:space-y-6">
            {/* Progress */}
            <div className="flex items-center gap-4 mb-6 sm:mb-8">
              {Array.from({ length: 3 }).map((_, i) => (
                <div key={i} className="flex items-center">
                  <Skeleton className="w-7 h-7 sm:w-8 sm:h-8 rounded-full" />
                  <Skeleton className="h-4 w-16 ml-2" />
                  {i < 2 && <Skeleton className="w-6 sm:w-8 h-px mx-2 sm:mx-4" />}
                </div>
              ))}
            </div>
            
            {/* Form */}
            <div className="p-4 sm:p-6 bg-white rounded-xl shadow-sm space-y-4">
              <Skeleton className="h-6 w-48" />
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 sm:gap-4">
                {Array.from({ length: 6 }).map((_, i) => (
                  <div key={i} className="space-y-2">
                    <Skeleton className="h-4 w-20" />
                    <Skeleton className="h-11 w-full rounded-xl" />
                  </div>
                ))}
              </div>
              <Skeleton className="h-11 w-full rounded-xl" />
            </div>
          </div>
          
          {/* Summary */}
          <div className="lg:col-span-1 order-first lg:order-last">
            <div className="p-4 sm:p-6 bg-white rounded-xl shadow-sm space-y-4">
              <Skeleton className="h-6 w-32" />
              <div className="space-y-3">
                {Array.from({ length: 3 }).map((_, i) => (
                  <div key={i} className="flex justify-between">
                    <Skeleton className="h-4 w-24" />
                    <Skeleton className="h-4 w-16" />
                  </div>
                ))}
              </div>
              <Skeleton className="h-px w-full" />
              <Skeleton className="h-6 w-full" />
            </div>
          </div>
        </div>
      </div>
    )
  }

  if (!user) {
    return null
  }

  if (items.length === 0) {
    return (
      <div className="container mx-auto px-4 py-8 flex justify-center items-center min-h-screen">
        <div className="text-center">
          <p className="text-slate-600 mb-4">Your cart is empty.</p>
           <Button onClick={() => router.push("/products")} className="bg-gradient-to-r from-blue-600 to-purple-600 hover:from-blue-700 hover:to-purple-700 rounded-xl">
            Continue Shopping
          </Button>
        </div>
      </div>
    )
  }

  return (
    <div className="container mx-auto px-4 sm:px-6 py-6 sm:py-8">
      <div className="mb-4 sm:mb-6">
        <Button variant="ghost" onClick={() => router.back()} className="mb-3 sm:mb-4 md:hidden">
          <ArrowLeft className="mr-2 h-4 w-4" /> Back
        </Button>
        <h1 className="text-2xl sm:text-3xl font-bold">Checkout</h1>
      </div>

      <div className="grid lg:grid-cols-3 gap-6 sm:gap-8">
        {/* Main Content */}
        <div className="lg:col-span-2 space-y-4 sm:space-y-6">
          {/* Progress Steps */}
          <div className="flex items-center space-x-2 sm:space-x-4 mb-6 sm:mb-8 overflow-x-auto scroll-mobile pb-2">
            {[1, 2, 3].map((stepNumber) => (
              <div key={stepNumber} className="flex items-center flex-shrink-0">
                <div
                  className={`w-7 h-7 sm:w-8 sm:h-8 rounded-full flex items-center justify-center text-xs sm:text-sm font-medium ${
                    step >= stepNumber ? "bg-gradient-to-r from-blue-600 to-purple-600 text-white" : "bg-slate-200 text-slate-600"
                  }`}
                >
                  {stepNumber}
                </div>
                <span className={`ml-1.5 sm:ml-2 text-xs sm:text-sm ${step >= stepNumber ? "text-blue-600" : "text-slate-600"}`}>
                  {stepNumber === 1 ? "Shipping" : stepNumber === 2 ? "Review" : "Payment"}
                </span>
                {stepNumber < 3 && <div className="w-6 sm:w-8 h-px bg-slate-200 mx-2 sm:mx-4" />}
              </div>
            ))}
          </div>

          {/* Step 1: Shipping Information */}
          {step === 1 && (
            <Card className="border-0 shadow-sm bg-white/80 backdrop-blur-xl rounded-2xl overflow-hidden">
              <CardHeader className="p-4 sm:p-6">
                <CardTitle className="flex items-center text-lg sm:text-xl">
                  <Truck className="mr-2 h-5 w-5" />
                  Shipping Information
                </CardTitle>
                {user?.address && (
                  <p className="text-xs sm:text-sm text-slate-600">
                    We've pre-filled your saved address. You can edit it below if needed.
                  </p>
                )}
              </CardHeader>
              <CardContent className="p-4 sm:p-6 space-y-3 sm:space-y-4">
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 sm:gap-4">
                  <div className="space-y-1.5 sm:space-y-2">
                    <Label htmlFor="firstName" className="text-sm">First Name *</Label>
                    <Input
                      id="firstName"
                      value={shippingData.firstName}
                      onChange={(e) => setShippingData((prev) => ({ ...prev, firstName: e.target.value }))}
                      className={`h-11 sm:h-10 ${errors.firstName ? "border-red-500" : ""}`}
                    />
                    {errors.firstName && <p className="text-xs sm:text-sm text-red-500">{errors.firstName}</p>}
                  </div>
                  <div className="space-y-1.5 sm:space-y-2">
                    <Label htmlFor="lastName" className="text-sm">Last Name *</Label>
                    <Input
                      id="lastName"
                      value={shippingData.lastName}
                      onChange={(e) => setShippingData((prev) => ({ ...prev, lastName: e.target.value }))}
                      className={`h-11 sm:h-10 ${errors.lastName ? "border-red-500" : ""}`}
                    />
                    {errors.lastName && <p className="text-xs sm:text-sm text-red-500">{errors.lastName}</p>}
                  </div>
                </div>

                <div className="space-y-1.5 sm:space-y-2">
                  <Label htmlFor="email" className="text-sm">Email *</Label>
                  <Input
                    id="email"
                    type="email"
                    value={shippingData.email}
                    onChange={(e) => setShippingData((prev) => ({ ...prev, email: e.target.value }))}
                    className={`h-11 sm:h-10 ${errors.email ? "border-red-500" : ""}`}
                  />
                  {errors.email && <p className="text-xs sm:text-sm text-red-500">{errors.email}</p>}
                </div>

                <div className="space-y-1.5 sm:space-y-2">
                  <Label htmlFor="phone" className="text-sm">Phone Number</Label>
                  <Input
                    id="phone"
                    type="tel"
                    value={shippingData.phone}
                    onChange={(e) => setShippingData((prev) => ({ ...prev, phone: e.target.value }))}
                    className="h-11 sm:h-10"
                  />
                </div>

                <div className="space-y-1.5 sm:space-y-2">
                  <Label htmlFor="address" className="text-sm">Address *</Label>
                  <Input
                    id="address"
                    value={shippingData.address}
                    onChange={(e) => setShippingData((prev) => ({ ...prev, address: e.target.value }))}
                    className={`h-11 sm:h-10 ${errors.address ? "border-red-500" : ""}`}
                  />
                  {errors.address && <p className="text-xs sm:text-sm text-red-500">{errors.address}</p>}
                </div>

                <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 sm:gap-4">
                  <div className="space-y-1.5 sm:space-y-2">
                    <Label htmlFor="city" className="text-sm">City *</Label>
                    <Input
                      id="city"
                      value={shippingData.city}
                      onChange={(e) => setShippingData((prev) => ({ ...prev, city: e.target.value }))}
                      className={`h-11 sm:h-10 ${errors.city ? "border-red-500" : ""}`}
                    />
                    {errors.city && <p className="text-xs sm:text-sm text-red-500">{errors.city}</p>}
                  </div>
                  <div className="space-y-1.5 sm:space-y-2">
                    <Label htmlFor="state" className="text-sm">State/Province *</Label>
                    <Input
                      id="state"
                      placeholder={getStatePlaceholder(shippingData.country)}
                      value={shippingData.state}
                      onChange={(e) => setShippingData((prev) => ({ ...prev, state: e.target.value }))}
                      className={`h-11 sm:h-10 ${errors.state ? "border-red-500" : ""}`}
                    />
                    {errors.state && <p className="text-xs sm:text-sm text-red-500">{errors.state}</p>}
                  </div>
                </div>

                <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 sm:gap-4">
                  <div className="space-y-1.5 sm:space-y-2">
                    <Label htmlFor="zipCode" className="text-sm">ZIP/Postal Code *</Label>
                    <Input
                      id="zipCode"
                      placeholder={getZipPlaceholder(shippingData.country)}
                      value={shippingData.zipCode}
                      onChange={(e) => setShippingData((prev) => ({ ...prev, zipCode: e.target.value }))}
                      className={`h-11 sm:h-10 ${errors.zipCode ? "border-red-500" : ""}`}
                    />
                    {errors.zipCode && <p className="text-xs sm:text-sm text-red-500">{errors.zipCode}</p>}
                  </div>
                  <div className="space-y-1.5 sm:space-y-2">
                    <Label htmlFor="country" className="text-sm">Country</Label>
                    <Select
                      value={shippingData.country}
                      onValueChange={(value) => setShippingData((prev) => ({ ...prev, country: value }))}
                    >
                      <SelectTrigger className="h-11 sm:h-10">
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="VN">Vietnam</SelectItem>
                        <SelectItem value="US">United States</SelectItem>
                        <SelectItem value="CA">Canada</SelectItem>
                        <SelectItem value="GB">United Kingdom</SelectItem>
                        <SelectItem value="AU">Australia</SelectItem>
                        <SelectItem value="SG">Singapore</SelectItem>
                        <SelectItem value="JP">Japan</SelectItem>
                      </SelectContent>
                    </Select>
                  </div>
                </div>

                {/* Shipping Method Selection */}
                <div className="space-y-1.5 sm:space-y-2">
                  <Label htmlFor="shippingMethod" className="text-sm">Shipping Method</Label>
                  <Select
                    value={shippingMethod}
                    onValueChange={(value) => setShippingMethod(value)}
                  >
                    <SelectTrigger className="h-11 sm:h-10">
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="standard">
                        Standard Shipping (Free)
                      </SelectItem>
                      <SelectItem value="express">
                        Express Shipping (+$5.00)
                      </SelectItem>
                      <SelectItem value="overnight">
                        Overnight Shipping (+$10.00)
                      </SelectItem>
                    </SelectContent>
                  </Select>
                  <p className="text-xs text-slate-500">
                    {shippingMethod === "standard" && "5-7 business days delivery"}
                    {shippingMethod === "express" && "2-3 business days delivery"}
                    {shippingMethod === "overnight" && "Next business day delivery"}
                  </p>
                </div>

                <Button 
                  onClick={handleNext} 
                  className="w-full h-12 sm:h-10 bg-gradient-to-r from-blue-600 to-purple-600 hover:from-blue-700 hover:to-purple-700 btn-touch"
                  disabled={isProcessing}
                >
                  Continue to Review
                </Button>
              </CardContent>
            </Card>
          )}

          {/* Step 2: Review Order */}
          {step === 2 && (
            <Card className="border-0 shadow-sm bg-white/80 backdrop-blur-xl rounded-2xl overflow-hidden">
              <CardHeader className="p-4 sm:p-6">
                <CardTitle className="flex items-center text-lg sm:text-xl">
                  <CreditCard className="mr-2 h-5 w-5" />
                  Review Order
                </CardTitle>
                <p className="text-xs sm:text-sm text-slate-600">
                  Please review your order details before proceeding to payment.
                </p>
              </CardHeader>
              <CardContent className="p-4 sm:p-6 space-y-4 sm:space-y-6">
                {/* Shipping Address */}
                <div>
                  <h3 className="font-medium mb-2 text-sm sm:text-base">Shipping Address</h3>
                  <div className="text-xs sm:text-sm text-slate-600 bg-slate-50 p-3 rounded-lg">
                    <p>
                      {shippingData.firstName} {shippingData.lastName}
                    </p>
                    <p>{shippingData.address}</p>
                    <p>
                      {shippingData.city}, {shippingData.state} {shippingData.zipCode}
                    </p>
                    <p>{shippingData.country}</p>
                  </div>
                </div>

                <Separator />

                {/* Order Items */}
                <div>
                  <h3 className="font-medium mb-3 sm:mb-4 text-sm sm:text-base">Order Items</h3>
                  <div className="space-y-3 sm:space-y-4">
                    {items.map((item) => (
                      <div key={item.id} className="flex items-center space-x-3 sm:space-x-4">
                        <div className="w-12 h-12 sm:w-16 sm:h-16 bg-slate-100 rounded-lg flex items-center justify-center flex-shrink-0">
                          <img
                            src={item.image || "/placeholder.svg"}
                            alt={item.name}
                            width={48}
                            height={48}
                            className="object-contain sm:w-16 sm:h-16"
                          />
                        </div>
                        <div className="flex-1 min-w-0">
                          <h4 className="font-medium text-sm sm:text-base truncate">{item.name}</h4>
                          {item.color && (
                            <p className="text-xs sm:text-sm text-slate-600">Color: {item.color}</p>
                          )}
                          {item.storage && (
                            <p className="text-xs sm:text-sm text-slate-600">Storage: {item.storage}</p>
                          )}
                          <p className="text-xs sm:text-sm text-slate-600">Qty: {item.quantity}</p>
                        </div>
                        <div className="text-right flex-shrink-0">
                          <p className="font-medium text-sm sm:text-base">${(item.price * item.quantity).toFixed(2)}</p>
                        </div>
                      </div>
                    ))}
                  </div>
                </div>

                <Separator />

                {/* Shipping Method */}
                <div>
                  <h3 className="font-medium mb-2 text-sm sm:text-base">Shipping Method</h3>
                  <div className="text-xs sm:text-sm text-slate-600 bg-slate-50 p-3 rounded-lg">
                    <div className="flex justify-between items-center">
                      <div>
                        <p className="font-medium">
                          {shippingMethod === "standard" && "Standard Shipping"}
                          {shippingMethod === "express" && "Express Shipping"}
                          {shippingMethod === "overnight" && "Overnight Shipping"}
                        </p>
                        <p className="text-xs">
                          {shippingMethod === "standard" && "5-7 business days delivery"}
                          {shippingMethod === "express" && "2-3 business days delivery"}
                          {shippingMethod === "overnight" && "Next business day delivery"}
                        </p>
                      </div>
                      <div className="text-right flex-shrink-0">
                        {shippingCost === 0 ? (
                          <span className="text-green-600 font-medium">Free</span>
                        ) : (
                          <span className="font-medium">${shippingCost.toFixed(2)}</span>
                        )}
                      </div>
                    </div>
                  </div>
                </div>

                <Separator />

                {/* Order Summary */}
                <div className="bg-slate-50 p-3 sm:p-4 rounded-lg">
                  <div className="space-y-2 text-sm">
                    <div className="flex justify-between">
                      <span>Subtotal (inc. tax)</span>
                      <span>${total.toFixed(2)}</span>
                    </div>
                    <div className="flex justify-between">
                      <span>Shipping</span>
                      {shippingCost === 0 ? (
                        <span className="text-green-600">Free</span>
                      ) : (
                        <span>${shippingCost.toFixed(2)}</span>
                      )}
                    </div>
                    <Separator />
                    <div className="flex justify-between font-bold text-base">
                      <span>Total</span>
                      <span>${finalTotal.toFixed(2)}</span>
                    </div>
                    <p className="text-xs text-slate-500 mt-1">Prices include applicable taxes</p>
                  </div>
                </div>

                <div className="flex flex-col sm:flex-row gap-3">
                  <Button 
                    variant="outline" 
                    onClick={() => setStep(1)} 
                    className="flex-1 h-11 sm:h-10"
                    disabled={isProcessing}
                  >
                    Back to Shipping
                  </Button>
                  <Button 
                    onClick={handleCreatePendingOrder} 
                    className="flex-1 h-11 sm:h-10 bg-gradient-to-r from-blue-600 to-purple-600 hover:from-blue-700 hover:to-purple-700 btn-touch"
                    disabled={isProcessing}
                  >
                    {isProcessing ? "Creating Order..." : "Proceed to Payment"}
                  </Button>
                </div>
              </CardContent>
            </Card>
          )}

          {/* Step 3: Payment */}
          {step === 3 && pendingOrder && (
            <Card className="border-0 shadow-sm bg-white/80 backdrop-blur-xl rounded-2xl overflow-hidden">
              <CardHeader className="p-4 sm:p-6">
                <CardTitle className="flex items-center justify-between text-lg sm:text-xl">
                  <div className="flex items-center">
                    <Shield className="mr-2 h-5 w-5" />
                    Payment
                  </div>
                  <div className="flex items-center space-x-2">
                    <div className={`w-2 h-2 rounded-full animate-pulse ${
                      timeLeft > 60 ? 'bg-green-500' : timeLeft > 30 ? 'bg-yellow-500' : 'bg-red-500'
                    }`}></div>
                    <span className={`text-xs sm:text-sm font-mono ${
                      timeLeft > 60 ? 'text-green-600' : timeLeft > 30 ? 'text-yellow-600' : 'text-red-600'
                    }`}>
                      {formatTime(timeLeft)}
                    </span>
                  </div>
                </CardTitle>
                <p className="text-xs sm:text-sm text-slate-600">
                  Your order #{pendingOrder.id} has been created. Complete payment within the remaining time to confirm your order.
                </p>
              </CardHeader>
              <CardContent className="p-4 sm:p-6 space-y-4 sm:space-y-6">
                <div className={`border p-3 sm:p-4 rounded-lg ${
                  timeLeft > 60 ? 'bg-yellow-50 border-yellow-200' : 'bg-red-50 border-red-200'
                }`}>
                  <div className="flex items-center justify-between mb-2 sm:mb-3">
                    <div className="flex items-center space-x-2">
                      <div className={`w-2 h-2 rounded-full animate-pulse ${
                        timeLeft > 60 ? 'bg-yellow-500' : 'bg-red-500'
                      }`}></div>
                      <span className={`font-medium text-sm sm:text-base ${
                        timeLeft > 60 ? 'text-yellow-800' : 'text-red-800'
                      }`}>
                        {timeLeft > 60 ? 'Payment Required' : 'Payment Expiring Soon!'}
                      </span>
                    </div>
                    <span className={`font-mono text-base sm:text-lg font-bold ${
                      timeLeft > 60 ? 'text-yellow-900' : 'text-red-900'
                    }`}>
                      {formatTime(timeLeft)}
                    </span>
                  </div>
                  
                  {/* Progress Bar */}
                  <div className="mb-2 sm:mb-3">
                    <div className="flex justify-between text-xs mb-1">
                      <span className={timeLeft > 60 ? 'text-yellow-700' : 'text-red-700'}>
                        Time Remaining
                      </span>
                      <span className={timeLeft > 60 ? 'text-yellow-700' : 'text-red-700'}>
                        {Math.round(getProgressPercentage())}%
                      </span>
                    </div>
                    <div className="w-full bg-gray-200 rounded-full h-2">
                      <div 
                        className={`h-2 rounded-full transition-all duration-1000 ${
                          timeLeft > 60 ? 'bg-yellow-500' : timeLeft > 30 ? 'bg-orange-500' : 'bg-red-500'
                        }`}
                        style={{ width: `${getProgressPercentage()}%` }}
                      ></div>
                    </div>
                  </div>
                  
                  <p className={`text-xs sm:text-sm ${
                    timeLeft > 60 ? 'text-yellow-700' : 'text-red-700'
                  }`}>
                    {timeLeft > 60 
                      ? 'Please complete your payment within the time limit. After this time, your order will be automatically cancelled.'
                      : 'Hurry! Your payment time is about to expire. Complete payment now to secure your order.'
                    }
                  </p>
                </div>

                <div className="bg-blue-50 p-3 sm:p-4 rounded-lg">
                  <div className="flex items-center space-x-2 mb-2">
                    <Shield className="h-5 w-5 text-blue-600" />
                    <span className="font-medium text-blue-900 text-sm">Secure Payment with Stripe</span>
                  </div>
                  <p className="text-xs sm:text-sm text-blue-800">
                    Your payment will be processed securely. The shipping address above will be used for delivery.
                  </p>
                </div>

                <DirectPaymentButton
                  shippingAddress={shippingData}
                  shippingMethod={shippingMethod}
                  onPaymentStart={handlePaymentStart}
                  onPaymentError={handlePaymentError}
                  className="w-full h-12 sm:h-10 btn-touch"
                  disabled={isProcessing || timeLeft <= 0}
                >
                  {isProcessing ? "Processing..." : timeLeft <= 0 ? "Payment Expired" : `Pay $${finalTotal.toFixed(2)}`}
                </DirectPaymentButton>

                {timeLeft <= 0 && (
                  <div className="bg-red-50 border border-red-200 p-3 sm:p-4 rounded-lg">
                    <div className="flex items-center space-x-2 mb-2">
                      <div className="w-2 h-2 bg-red-500 rounded-full"></div>
                      <span className="font-medium text-red-800 text-sm">Payment Expired</span>
                    </div>
                    <p className="text-xs sm:text-sm text-red-700 mb-3">
                      The payment time limit has expired. Your order has been automatically cancelled and items have been returned to stock.
                    </p>
                    <Button 
                      onClick={() => {
                        const params = new URLSearchParams()
                        if (pendingOrder?.id) params.set('orderId', pendingOrder.id)
                        if (finalTotal) params.set('total', finalTotal.toFixed(2))
                        router.push(`/payment-timeout?${params.toString()}`)
                      }} 
                      variant="outline" 
                      className="w-full h-11 sm:h-10"
                    >
                      View Details
                    </Button>
                  </div>
                )}

                <Button 
                  variant="outline" 
                  onClick={() => setStep(2)} 
                  className="w-full h-11 sm:h-10"
                  disabled={isProcessing || timeLeft <= 0}
                >
                  Back to Review
                </Button>
              </CardContent>
            </Card>
          )}
        </div>

        {/* Order Summary - On mobile, show first */}
        <div className="lg:col-span-1 order-first lg:order-last">
          <Card className="border-0 shadow-sm bg-white/80 backdrop-blur-xl rounded-2xl overflow-hidden sticky top-24">
            <CardHeader className="p-4 sm:p-6">
              <CardTitle className="flex items-center justify-between text-lg sm:text-xl">
                <span>Order Summary</span>
                {step === 3 && pendingOrder && (
                  <div className="flex items-center space-x-2">
                    <div className={`w-2 h-2 rounded-full animate-pulse ${
                      timeLeft > 60 ? 'bg-green-500' : timeLeft > 30 ? 'bg-yellow-500' : 'bg-red-500'
                    }`}></div>
                    <span className={`text-xs font-mono ${
                      timeLeft > 60 ? 'text-green-600' : timeLeft > 30 ? 'text-yellow-600' : 'text-red-600'
                    }`}>
                      {formatTime(timeLeft)}
                    </span>
                  </div>
                )}
              </CardTitle>
            </CardHeader>
            <CardContent className="p-4 sm:p-6 space-y-3 sm:space-y-4">
              {/* Payment Timer Alert */}
              {step === 3 && pendingOrder && (
                <div className={`p-3 rounded-lg border ${
                  timeLeft > 60 ? 'bg-green-50 border-green-200' : 
                  timeLeft > 30 ? 'bg-yellow-50 border-yellow-200' : 'bg-red-50 border-red-200'
                }`}>
                  <div className="flex items-center justify-between mb-2">
                    <span className={`text-xs font-medium ${
                      timeLeft > 60 ? 'text-green-800' : 
                      timeLeft > 30 ? 'text-yellow-800' : 'text-red-800'
                    }`}>
                      Payment Time
                    </span>
                    <span className={`text-sm font-mono font-bold ${
                      timeLeft > 60 ? 'text-green-900' : 
                      timeLeft > 30 ? 'text-yellow-900' : 'text-red-900'
                    }`}>
                      {formatTime(timeLeft)}
                    </span>
                  </div>
                  
                  {/* Mini Progress Bar */}
                  <div className="mb-2">
                    <div className="w-full bg-gray-200 rounded-full h-1.5">
                      <div 
                        className={`h-1.5 rounded-full transition-all duration-1000 ${
                          timeLeft > 60 ? 'bg-green-500' : 
                          timeLeft > 30 ? 'bg-yellow-500' : 'bg-red-500'
                        }`}
                        style={{ width: `${getProgressPercentage()}%` }}
                      ></div>
                    </div>
                  </div>
                  
                  <p className={`text-xs ${
                    timeLeft > 60 ? 'text-green-700' : 
                    timeLeft > 30 ? 'text-yellow-700' : 'text-red-700'
                  }`}>
                    {timeLeft > 60 ? 'Complete payment soon' : 
                     timeLeft > 30 ? 'Payment expiring soon!' : 'Payment about to expire!'}
                  </p>
                </div>
              )}

              {/* Order Items */}
              <div className="space-y-3">
                {items.map((item) => (
                  <div key={item.id} className="space-y-1">
                    <div className="flex justify-between text-sm">
                      <span className="font-medium">
                        {item.name} × {item.quantity}
                      </span>
                      <span>${(item.price * item.quantity).toFixed(2)}</span>
                    </div>
                    {(item.color || item.storage) && (
                      <div className="text-xs text-slate-500 ml-2">
                        {item.color && <span>Color: {item.color}</span>}
                        {item.color && item.storage && <span> • </span>}
                        {item.storage && <span>Storage: {item.storage}</span>}
                      </div>
                    )}
                  </div>
                ))}
              </div>

              <Separator />

              {/* Totals */}
              <div className="space-y-2 text-sm">
                <div className="flex justify-between">
                  <span>Subtotal (inc. tax)</span>
                  <span>${total.toFixed(2)}</span>
                </div>
                <div className="flex justify-between">
                  <span>Shipping</span>
                  {shippingCost === 0 ? (
                    <span className="text-green-600">Free</span>
                  ) : (
                    <span>${shippingCost.toFixed(2)}</span>
                  )}
                </div>
              </div>

              <Separator />

              <div className="flex justify-between font-bold">
                <span>Total</span>
                <span>${finalTotal.toFixed(2)}</span>
              </div>
              
              <p className="text-xs text-slate-500 text-center">Prices include applicable taxes</p>

              {/* Security Badge */}
              <Alert>
                <Shield className="h-4 w-4" />
                <AlertDescription className="text-xs">
                  Your payment information is secure and encrypted.
                </AlertDescription>
              </Alert>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  )
}
