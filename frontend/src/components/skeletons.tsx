import { Skeleton } from "@/components/ui/skeleton"

export function ProductCardSkeleton() {
  return (
    <div className="rounded-2xl overflow-hidden border border-slate-100 bg-white">
      <Skeleton className="h-48 sm:h-56 w-full rounded-none" />
      <div className="p-4 space-y-3">
        <Skeleton className="h-4 w-3/4" />
        <Skeleton className="h-3 w-1/2" />
        <div className="flex items-center gap-2">
          <Skeleton className="h-5 w-16 rounded-full" />
          <Skeleton className="h-5 w-12 rounded-full" />
        </div>
        <div className="flex items-center justify-between pt-2">
          <Skeleton className="h-6 w-20" />
          <Skeleton className="h-9 w-9 rounded-xl" />
        </div>
      </div>
    </div>
  )
}

export function ProductGridSkeleton({ count = 8 }: { count?: number }) {
  return (
    <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4 sm:gap-6">
      {Array.from({ length: count }).map((_, i) => (
        <ProductCardSkeleton key={i} />
      ))}
    </div>
  )
}

export function ProductDetailSkeleton() {
  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50/50 via-white to-slate-100/50">
      <div className="container mx-auto px-4 sm:px-6 py-6 sm:py-8">
        <Skeleton className="h-4 w-48 mb-6 hidden md:block" />
        
        <div className="grid lg:grid-cols-2 gap-6 sm:gap-8 lg:gap-12">
          {/* Gallery */}
          <div className="space-y-4">
            <Skeleton className="aspect-square w-full rounded-2xl" />
            <div className="grid grid-cols-4 gap-3">
              {Array.from({ length: 4 }).map((_, i) => (
                <Skeleton key={i} className="aspect-square rounded-xl" />
              ))}
            </div>
          </div>
          
          {/* Info */}
          <div className="space-y-6">
            <div className="space-y-3">
              <Skeleton className="h-5 w-24 rounded-full" />
              <Skeleton className="h-8 sm:h-10 w-3/4" />
              <Skeleton className="h-4 w-40" />
            </div>
            
            <Skeleton className="h-8 w-32" />
            
            <Skeleton className="h-16 w-full rounded-xl" />
            
            <div className="space-y-3">
              <Skeleton className="h-4 w-16" />
              <div className="flex gap-3">
                {Array.from({ length: 4 }).map((_, i) => (
                  <Skeleton key={i} className="h-10 w-10 rounded-full" />
                ))}
              </div>
            </div>
            
            <div className="space-y-3">
              <Skeleton className="h-4 w-16" />
              <div className="flex gap-3">
                {Array.from({ length: 3 }).map((_, i) => (
                  <Skeleton key={i} className="h-11 w-20 rounded-xl" />
                ))}
              </div>
            </div>
            
            <div className="space-y-3">
              <Skeleton className="h-4 w-20" />
              <Skeleton className="h-11 w-36 rounded-xl" />
            </div>
            
            <div className="flex gap-3">
              <Skeleton className="h-12 sm:h-14 flex-1 rounded-xl sm:rounded-2xl" />
              <Skeleton className="h-12 sm:h-14 flex-1 rounded-xl sm:rounded-2xl" />
            </div>
            
            <Skeleton className="h-28 w-full rounded-2xl" />
          </div>
        </div>
        
        {/* Tabs */}
        <div className="mt-10 sm:mt-16 space-y-4">
          <Skeleton className="h-12 w-full rounded-2xl" />
          <Skeleton className="h-64 w-full rounded-2xl" />
        </div>
      </div>
    </div>
  )
}

export function OrdersListSkeleton() {
  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 via-white to-slate-100">
      <div className="container mx-auto px-4 sm:px-6 py-6 sm:py-8">
        <div className="mb-6 sm:mb-8">
          <Skeleton className="h-8 sm:h-9 w-40 mb-2" />
          <Skeleton className="h-4 w-56" />
        </div>
        
        <div className="space-y-4">
          {Array.from({ length: 3 }).map((_, i) => (
            <div key={i} className="p-4 sm:p-5 bg-white/80 backdrop-blur-xl rounded-2xl border-0 shadow-lg space-y-4">
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
  )
}

export function OrderDetailSkeleton() {
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

export function HomepageSkeleton() {
  return (
    <div className="min-h-screen">
      {/* Hero */}
      <section className="bg-gradient-hero text-white overflow-hidden">
        <div className="container mx-auto px-4 sm:px-6 py-12 sm:py-20 lg:py-32">
          <div className="grid lg:grid-cols-2 gap-8 lg:gap-12 items-center">
            <div className="space-y-6 text-center lg:text-left">
              <Skeleton className="h-6 w-32 bg-white/20 mx-auto lg:mx-0" />
              <div className="space-y-3">
                <Skeleton className="h-10 sm:h-12 lg:h-16 w-3/4 bg-white/20 mx-auto lg:mx-0" />
                <Skeleton className="h-6 sm:h-7 w-full bg-white/20 mx-auto lg:mx-0" />
              </div>
              <Skeleton className="h-8 w-28 bg-white/20 mx-auto lg:mx-0" />
              <div className="flex gap-4 justify-center lg:justify-start">
                <Skeleton className="h-12 sm:h-14 w-36 rounded-2xl bg-white/20" />
                <Skeleton className="h-12 sm:h-14 w-36 rounded-2xl bg-white/20" />
              </div>
            </div>
            <div className="flex justify-center lg:justify-end">
              <Skeleton className="w-[220px] h-[220px] sm:w-[300px] sm:h-[300px] lg:w-[500px] lg:h-[500px] rounded-3xl bg-white/10" />
            </div>
          </div>
        </div>
      </section>
      
      {/* Categories */}
      <section className="py-16 sm:py-24">
        <div className="container mx-auto px-4 sm:px-6">
          <div className="text-center mb-10 sm:mb-16">
            <Skeleton className="h-6 w-24 mx-auto mb-4 rounded-full" />
            <Skeleton className="h-8 sm:h-10 w-64 mx-auto mb-4" />
            <Skeleton className="h-4 w-80 mx-auto" />
          </div>
          <div className="flex gap-4 sm:gap-6 overflow-hidden md:grid md:grid-cols-3 max-w-6xl mx-auto">
            {Array.from({ length: 3 }).map((_, i) => (
              <div key={i} className="flex-shrink-0 w-[240px] sm:w-auto">
                <div className="rounded-2xl sm:rounded-3xl border border-slate-100 bg-white overflow-hidden">
                  <Skeleton className="h-40 sm:h-48" />
                  <div className="p-4 sm:p-6 text-center space-y-2">
                    <Skeleton className="h-5 w-24 mx-auto" />
                    <Skeleton className="h-3 w-20 mx-auto" />
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      </section>
      
      {/* Product Sections */}
      {[1, 2, 3].map((section) => (
        <section key={section} className="py-12 sm:py-16">
          <div className="container mx-auto px-4 sm:px-6">
            <div className="flex items-center justify-between mb-8">
              <Skeleton className="h-7 sm:h-8 w-40" />
              <Skeleton className="h-9 w-20 rounded-full" />
            </div>
            <ProductGridSkeleton count={4} />
          </div>
        </section>
      ))}
    </div>
  )
}

export function CheckoutSkeleton() {
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
