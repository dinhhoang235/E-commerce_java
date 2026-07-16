"use client"

import type React from "react"

import Link from "next/link"
import { useState, useRef, useEffect } from "react"
import { useRouter } from "next/navigation"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Badge } from "@/components/ui/badge"
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar"
import { Sheet, SheetContent, SheetHeader, SheetTitle, SheetTrigger } from "@/components/ui/sheet"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import { Search, ShoppingCart, Menu, Heart, LogOut, Settings, Package, X } from "lucide-react"
import { useCart } from "@/components/cart-provider"
import { useWishlist } from "@/components/wishlist-provider"
import { useAuth } from "@/components/auth-provider"
import { SearchDropdown } from "@/components/search-dropdown"

export function Header() {
  const [searchQuery, setSearchQuery] = useState("")
  const [isSearchDropdownOpen, setIsSearchDropdownOpen] = useState(false)
  const [isMobileSearchOpen, setIsMobileSearchOpen] = useState(false)
  const [mobileSearchQuery, setMobileSearchQuery] = useState("")
  const [isMobileSearchDropdownOpen, setIsMobileSearchDropdownOpen] = useState(false)
  const [isCartOpen, setIsCartOpen] = useState(false)
  const [isScrolled, setIsScrolled] = useState(false)
  const { items } = useCart()
  const { total: wishlistCount } = useWishlist()
  const { user, logout } = useAuth()
  const router = useRouter()
  const itemCount = items.reduce((sum, item) => sum + item.quantity, 0)
  const searchRef = useRef<HTMLDivElement>(null)
  const mobileSearchRef = useRef<HTMLDivElement>(null)

  const getInitials = (firstName?: string, lastName?: string) => {
    const first = firstName?.charAt(0) || ''
    const last = lastName?.charAt(0) || ''
    return `${first}${last}`.toUpperCase() || 'U'
  }

  // Handle scroll effect
  useEffect(() => {
    const handleScroll = () => {
      setIsScrolled(window.scrollY > 20)
    }
    window.addEventListener('scroll', handleScroll)
    return () => window.removeEventListener('scroll', handleScroll)
  }, [])

  // Handle clicks outside search dropdown
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (searchRef.current && !searchRef.current.contains(event.target as Node)) {
        setIsSearchDropdownOpen(false)
      }
      if (mobileSearchRef.current && !mobileSearchRef.current.contains(event.target as Node)) {
        setIsMobileSearchDropdownOpen(false)
      }
    }

    document.addEventListener("mousedown", handleClickOutside)
    return () => document.removeEventListener("mousedown", handleClickOutside)
  }, [])

  const handleSearch = (query: string) => {
    if (query.trim()) {
      router.push(`/products?search=${encodeURIComponent(query.trim())}`)
      setSearchQuery("")
      setMobileSearchQuery("")
      setIsSearchDropdownOpen(false)
      setIsMobileSearchDropdownOpen(false)
      setIsMobileSearchOpen(false)
    }
  }

  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    handleSearch(searchQuery)
  }

  const handleMobileSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    handleSearch(mobileSearchQuery)
  }

  const handleProductSelect = (productId: string) => {
    router.push(`/products/${productId}`)
    setSearchQuery("")
    setMobileSearchQuery("")
    setIsSearchDropdownOpen(false)
    setIsMobileSearchDropdownOpen(false)
    setIsMobileSearchOpen(false)
  }

  const handleSearchInputChange = (value: string) => {
    setSearchQuery(value)
    setIsSearchDropdownOpen(value.trim().length > 0)
  }

  const handleMobileSearchInputChange = (value: string) => {
    setMobileSearchQuery(value)
    setIsMobileSearchDropdownOpen(value.trim().length > 0)
  }

  return (
    <header className={`sticky top-0 z-50 w-full transition-all duration-300 ${
      isScrolled 
        ? 'bg-white/80 backdrop-blur-xl shadow-lg shadow-black/5 border-b border-white/20' 
        : 'bg-white/95 backdrop-blur supports-[backdrop-filter]:bg-white/60'
    }`}>
      <div className="container mx-auto px-4">
        <div className="flex h-16 items-center justify-between">
          {/* Logo */}
          <Link href="/" className="flex items-center space-x-3 mr-8 group">
            <div className="relative">
              <div className="bg-gradient-to-br from-slate-900 to-slate-700 text-white p-2.5 rounded-xl group-hover:from-blue-600 group-hover:to-purple-600 transition-all duration-300 group-hover:shadow-lg group-hover:shadow-blue-500/25">
                <span className="font-bold text-lg">A</span>
              </div>
              <div className="absolute -inset-1 bg-gradient-to-br from-blue-500 to-purple-500 rounded-xl opacity-0 group-hover:opacity-20 transition-opacity duration-300 blur-sm"></div>
            </div>
            <span className="font-bold text-xl tracking-tight bg-gradient-to-r from-slate-900 to-slate-600 bg-clip-text text-transparent group-hover:from-blue-600 group-hover:to-purple-600 transition-all duration-300">
              Apple Store
            </span>
          </Link>

          {/* Desktop Navigation */}
          <nav className="hidden md:flex items-center space-x-1">
            {[
              { href: "/products?category=iphone", label: "iPhone" },
              { href: "/products?category=ipad", label: "iPad" },
              { href: "/products?category=macbook", label: "MacBook" },
              { href: "/products", label: "All Products" },
            ].map((link) => (
              <Link
                key={link.href}
                href={link.href}
                className="relative px-4 py-2 text-sm font-medium text-slate-600 hover:text-slate-900 transition-colors group"
              >
                {link.label}
                <span className="absolute bottom-0 left-1/2 -translate-x-1/2 w-0 h-0.5 bg-gradient-to-r from-blue-500 to-purple-500 group-hover:w-3/4 transition-all duration-300 rounded-full"></span>
              </Link>
            ))}
          </nav>

          {/* Desktop Search Bar */}
          <div className="hidden lg:flex items-center space-x-4 flex-1 max-w-md mx-8">
            <div ref={searchRef} className="relative w-full">
              <form onSubmit={handleSearchSubmit} className="relative">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                  <Search className="h-4 w-4 text-slate-400 group-hover:text-blue-500 transition-colors" />
                </div>
                <Input
                  placeholder="Search products..."
                  className="pl-10 pr-4 h-10 bg-slate-50/80 border-slate-200/60 rounded-xl focus:bg-white focus:border-blue-300 focus:ring-2 focus:ring-blue-500/20 transition-all duration-300"
                  value={searchQuery}
                  onChange={(e) => handleSearchInputChange(e.target.value)}
                  onFocus={() => {
                    if (searchQuery.trim()) {
                      setIsSearchDropdownOpen(true)
                    }
                  }}
                />
              </form>
              <SearchDropdown
                query={searchQuery}
                isOpen={isSearchDropdownOpen}
                onClose={() => setIsSearchDropdownOpen(false)}
                onSelect={handleProductSelect}
              />
            </div>
          </div>

          {/* Actions */}
          <div className="flex items-center space-x-1">
            {/* Mobile Search */}
            <Button
              variant="ghost"
              size="icon"
              className="lg:hidden h-10 w-10 rounded-xl hover:bg-slate-100 transition-colors"
              onClick={() => setIsMobileSearchOpen(!isMobileSearchOpen)}
            >
              <Search className="w-5 h-5 text-slate-600" />
            </Button>

            {/* Wishlist */}
            <Link href="/wishlist">
              <Button 
                variant="ghost" 
                size="icon" 
                className="hidden sm:flex relative h-10 w-10 rounded-xl hover:bg-pink-50 transition-colors group"
              >
                <Heart className="w-5 h-5 text-slate-600 group-hover:text-pink-500 transition-colors" />
                {wishlistCount > 0 && (
                  <Badge className="absolute -right-1 -top-1 h-5 w-5 flex items-center justify-center p-0 text-xs bg-gradient-to-r from-pink-500 to-rose-500 border-2 border-white shadow-sm animate-bounce-subtle">
                    {wishlistCount}
                  </Badge>
                )}
              </Button>
            </Link>

            {/* Account */}
            {user ? (
              <DropdownMenu>
                <DropdownMenuTrigger asChild>
                  <Button 
                    variant="ghost" 
                    className="hidden sm:flex items-center space-x-2 px-3 h-10 rounded-xl hover:bg-slate-100 transition-colors"
                  >
                    <Avatar className="h-8 w-8 ring-2 ring-slate-100 group-hover:ring-blue-200 transition-all">
                      <AvatarImage src={user.avatar || ""} alt={`${user.first_name} ${user.last_name}`} />
                      <AvatarFallback className="text-xs bg-gradient-to-br from-blue-500 to-purple-500 text-white font-semibold">
                        {getInitials(user.first_name, user.last_name)}
                      </AvatarFallback>
                    </Avatar>
                    <span className="text-sm font-medium text-slate-700">{user.first_name}</span>
                  </Button>
                </DropdownMenuTrigger>
                <DropdownMenuContent align="end" className="w-64 p-2 rounded-2xl border-0 shadow-xl shadow-black/10 bg-white/95 backdrop-blur-xl">
                  <div className="px-3 py-3 mb-1 bg-gradient-to-r from-slate-50 to-slate-100 rounded-xl">
                    <p className="text-sm font-semibold text-slate-900">
                      {user.first_name} {user.last_name}
                    </p>
                    <p className="text-xs text-slate-500 mt-0.5">{user.email}</p>
                  </div>
                  <DropdownMenuSeparator className="my-1" />
                  <DropdownMenuItem asChild className="rounded-xl px-3 py-2.5 cursor-pointer hover:bg-slate-100 transition-colors">
                    <Link href="/account" className="flex items-center">
                      <Settings className="mr-3 h-4 w-4 text-slate-500" />
                      <span className="font-medium">Account Settings</span>
                    </Link>
                  </DropdownMenuItem>
                  <DropdownMenuItem asChild className="rounded-xl px-3 py-2.5 cursor-pointer hover:bg-slate-100 transition-colors">
                    <Link href="/orders" className="flex items-center">
                      <Package className="mr-3 h-4 w-4 text-slate-500" />
                      <span className="font-medium">Order History</span>
                    </Link>
                  </DropdownMenuItem>
                  <DropdownMenuSeparator className="my-1" />
                  <DropdownMenuItem 
                    onClick={logout} 
                    className="rounded-xl px-3 py-2.5 cursor-pointer hover:bg-red-50 text-red-600 transition-colors"
                  >
                    <LogOut className="mr-3 h-4 w-4" />
                    <span className="font-medium">Sign Out</span>
                  </DropdownMenuItem>
                </DropdownMenuContent>
              </DropdownMenu>
            ) : (
              <div className="hidden sm:flex items-center space-x-2">
                <Button 
                  variant="ghost" 
                  size="sm" 
                  asChild 
                  className="h-9 px-4 rounded-xl font-medium hover:bg-slate-100 transition-colors"
                >
                  <Link href="/login">Sign In</Link>
                </Button>
                <Button 
                  size="sm" 
                  asChild 
                  className="h-9 px-4 rounded-xl font-medium bg-gradient-to-r from-blue-600 to-purple-600 hover:from-blue-700 hover:to-purple-700 shadow-md shadow-blue-500/25 hover:shadow-lg hover:shadow-blue-500/30 transition-all duration-300"
                >
                  <Link href="/register">Sign Up</Link>
                </Button>
              </div>
            )}

            {/* Cart */}
            <Sheet open={isCartOpen} onOpenChange={setIsCartOpen}>
              <SheetTrigger asChild>
                <Button 
                  variant="ghost" 
                  size="icon" 
                  className="relative h-10 w-10 rounded-xl hover:bg-blue-50 transition-colors group"
                >
                  <ShoppingCart className="w-5 h-5 text-slate-600 group-hover:text-blue-500 transition-colors" />
                  {itemCount > 0 && (
                    <Badge className="absolute -right-1 -top-1 h-5 w-5 rounded-full p-0 flex items-center justify-center text-xs bg-gradient-to-r from-blue-500 to-purple-500 border-2 border-white shadow-sm animate-bounce-subtle">
                      {itemCount}
                    </Badge>
                  )}
                </Button>
              </SheetTrigger>
              <SheetContent className="w-full sm:max-w-md p-0">
                <div className="flex flex-col h-full">
                  <SheetHeader className="px-6 py-4 border-b border-slate-100">
                    <SheetTitle className="text-xl font-bold bg-gradient-to-r from-slate-900 to-slate-600 bg-clip-text text-transparent">
                      Shopping Cart ({itemCount})
                    </SheetTitle>
                  </SheetHeader>
                  <div className="flex-1 overflow-y-auto px-6 py-4">
                    {items.length === 0 ? (
                      <div className="flex flex-col items-center justify-center h-full text-center py-12">
                        <div className="w-20 h-20 bg-slate-100 rounded-full flex items-center justify-center mb-4">
                          <ShoppingCart className="w-8 h-8 text-slate-400" />
                        </div>
                        <p className="text-slate-500 font-medium">Your cart is empty</p>
                        <p className="text-slate-400 text-sm mt-1">Add some items to get started</p>
                      </div>
                    ) : (
                      <div className="space-y-4">
                        {items.map((item) => (
                          <div 
                            key={item.itemId} 
                            className="flex items-center space-x-4 p-4 bg-slate-50/80 rounded-2xl hover:bg-slate-100/80 transition-colors"
                          >
                            <div className="w-16 h-16 bg-white rounded-xl overflow-hidden shadow-sm flex-shrink-0">
                              <img 
                                src={item.image || "/placeholder.jpg"} 
                                alt={item.name}
                                className="w-full h-full object-cover"
                                onError={(e) => {
                                  e.currentTarget.src = "/placeholder.jpg";
                                }}
                              />
                            </div>
                            <div className="flex-1 min-w-0">
                              <h4 className="font-semibold text-slate-900 truncate">{item.name}</h4>
                              <div className="text-sm text-slate-500 space-y-0.5 mt-1">
                                <p>Qty: {item.quantity}</p>
                                {item.color && <p>Color: {item.color}</p>}
                                {item.storage && <p>Storage: {item.storage}</p>}
                              </div>
                              <p className="font-bold text-slate-900 mt-1">
                                ${typeof item.price === 'number' ? (item.price * item.quantity).toFixed(2) : '0.00'}
                              </p>
                            </div>
                          </div>
                        ))}
                      </div>
                    )}
                  </div>
                  {items.length > 0 && (
                    <div className="px-6 py-4 border-t border-slate-100 bg-white/80 backdrop-blur-sm">
                      <div className="space-y-3">
                        <div className="flex justify-between text-sm">
                          <span className="text-slate-500">Subtotal</span>
                          <span className="font-semibold">
                            ${items.reduce((sum, item) => sum + (item.price * item.quantity), 0).toFixed(2)}
                          </span>
                        </div>
                        <Button 
                          className="w-full h-12 rounded-xl bg-gradient-to-r from-blue-600 to-purple-600 hover:from-blue-700 hover:to-purple-700 font-semibold shadow-lg shadow-blue-500/25 hover:shadow-xl hover:shadow-blue-500/30 transition-all duration-300" 
                          asChild
                        >
                          <Link href="/checkout" onClick={() => setIsCartOpen(false)}>
                            Checkout
                          </Link>
                        </Button>
                        <Button 
                          variant="outline" 
                          className="w-full h-12 rounded-xl border-slate-200 hover:bg-slate-50 font-medium transition-colors" 
                          asChild
                        >
                          <Link href="/cart" onClick={() => setIsCartOpen(false)}>
                            View Cart
                          </Link>
                        </Button>
                      </div>
                    </div>
                  )}
                </div>
              </SheetContent>
            </Sheet>

            {/* Mobile Menu */}
            <Sheet>
              <SheetTrigger asChild>
                <Button 
                  variant="ghost" 
                  size="icon" 
                  className="md:hidden h-10 w-10 rounded-xl hover:bg-slate-100 transition-colors"
                >
                  <Menu className="w-5 h-5 text-slate-600" />
                </Button>
              </SheetTrigger>
              <SheetContent side="left" className="w-80 p-0">
                <SheetHeader className="px-6 py-4 border-b border-slate-100">
                  <div className="flex items-center justify-between">
                    <SheetTitle className="text-xl font-bold bg-gradient-to-r from-slate-900 to-slate-600 bg-clip-text text-transparent">
                      Menu
                    </SheetTitle>
                  </div>
                </SheetHeader>
                <nav className="flex flex-col p-6 space-y-2">
                  {[
                    { href: "/products?category=iphone", label: "iPhone", icon: "📱" },
                    { href: "/products?category=ipad", label: "iPad", icon: "📱" },
                    { href: "/products?category=macbook", label: "MacBook", icon: "💻" },
                    { href: "/products", label: "All Products", icon: "🛍️" },
                  ].map((link) => (
                    <Link 
                      key={link.href} 
                      href={link.href} 
                      className="flex items-center space-x-3 px-4 py-3 text-slate-700 hover:bg-slate-100 rounded-xl transition-colors group"
                    >
                      <span className="text-lg">{link.icon}</span>
                      <span className="font-medium group-hover:text-blue-600 transition-colors">{link.label}</span>
                    </Link>
                  ))}
                  {user && (
                    <>
                      <div className="border-t border-slate-100 my-2"></div>
                      <Link 
                        href="/wishlist" 
                        className="flex items-center space-x-3 px-4 py-3 text-slate-700 hover:bg-pink-50 rounded-xl transition-colors group"
                      >
                        <Heart className="w-5 h-5 text-slate-500 group-hover:text-pink-500 transition-colors" />
                        <span className="font-medium group-hover:text-pink-600 transition-colors">Wishlist</span>
                        {wishlistCount > 0 && (
                          <Badge className="ml-auto bg-pink-100 text-pink-600 hover:bg-pink-200">
                            {wishlistCount}
                          </Badge>
                        )}
                      </Link>
                    </>
                  )}
                  {!user && (
                    <>
                      <div className="border-t border-slate-100 my-2"></div>
                      <Link 
                        href="/login" 
                        className="flex items-center justify-center px-4 py-3 text-slate-700 hover:bg-slate-100 rounded-xl transition-colors"
                      >
                        <span className="font-medium">Sign In</span>
                      </Link>
                      <Link 
                        href="/register" 
                        className="flex items-center justify-center px-4 py-3 bg-gradient-to-r from-blue-600 to-purple-600 text-white rounded-xl font-medium shadow-lg shadow-blue-500/25"
                      >
                        <span>Sign Up</span>
                      </Link>
                    </>
                  )}
                </nav>
              </SheetContent>
            </Sheet>
          </div>
        </div>

        {/* Mobile Search Bar */}
        {isMobileSearchOpen && (
          <div className="lg:hidden border-t border-slate-100 bg-white/95 backdrop-blur-xl p-4 animate-fade-in-down">
            <div ref={mobileSearchRef} className="relative">
              <form onSubmit={handleMobileSearchSubmit} className="relative">
                <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-slate-400 w-4 h-4" />
                <Input
                  placeholder="Search products..."
                  className="pl-10 pr-4 h-11 bg-slate-50 rounded-xl"
                  value={mobileSearchQuery}
                  onChange={(e) => handleMobileSearchInputChange(e.target.value)}
                  autoFocus
                />
              </form>
              <SearchDropdown
                query={mobileSearchQuery}
                isOpen={isMobileSearchDropdownOpen}
                onClose={() => setIsMobileSearchDropdownOpen(false)}
                onSelect={handleProductSelect}
              />
            </div>
          </div>
        )}
      </div>
    </header>
  )
}
