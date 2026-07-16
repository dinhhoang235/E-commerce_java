"use client"

import type React from "react"

import { useState, useEffect } from "react"
import { useRouter } from "next/navigation"
import Link from "next/link"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { Checkbox } from "@/components/ui/checkbox"
import { Loader2, Eye, EyeOff, Check, X, ArrowRight, Sparkles } from "lucide-react"
import { useAuth } from "@/components/auth-provider"
import { useToast } from "@/hooks/use-toast"
import { checkUsernameAvailability, checkEmailAvailability } from "@/lib/services/auth"

export default function RegisterPage() {
  const [formData, setFormData] = useState({
    username: "",
    firstName: "",
    lastName: "",
    email: "",
    phone: "",
    password: "",
    confirmPassword: "",
  })
  const [showPassword, setShowPassword] = useState(false)
  const [showConfirmPassword, setShowConfirmPassword] = useState(false)
  const [acceptTerms, setAcceptTerms] = useState(false)
  const [error, setError] = useState("")
  const [usernameAvailable, setUsernameAvailable] = useState<boolean | null>(null)
  const [emailAvailable, setEmailAvailable] = useState<boolean | null>(null)
  const { register, isLoading } = useAuth()
  const router = useRouter()
  const { toast } = useToast()

  useEffect(() => {
    const checkUsername = async () => {
      if (formData.username.length >= 3) {
        try {
          const available = await checkUsernameAvailability(formData.username)
          setUsernameAvailable(available)
        } catch (error) {
          setUsernameAvailable(null)
        }
      } else {
        setUsernameAvailable(null)
      }
    }

    const timeoutId = setTimeout(checkUsername, 300)
    return () => clearTimeout(timeoutId)
  }, [formData.username])

  useEffect(() => {
    const checkEmail = async () => {
      if (formData.email.includes("@")) {
        try {
          const available = await checkEmailAvailability(formData.email)
          setEmailAvailable(available)
        } catch (error) {
          setEmailAvailable(null)
        }
      } else {
        setEmailAvailable(null)
      }
    }

    const timeoutId = setTimeout(checkEmail, 300)
    return () => clearTimeout(timeoutId)
  }, [formData.email])

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setFormData((prev) => ({
      ...prev,
      [e.target.name]: e.target.value,
    }))
  }

  const validateForm = () => {
    if (!formData.username.trim()) { setError("Username is required"); return false }
    if (formData.username.length < 3) { setError("Username must be at least 3 characters long"); return false }
    if (!/^[a-zA-Z0-9_]+$/.test(formData.username)) { setError("Username can only contain letters, numbers, and underscores"); return false }
    if (usernameAvailable === false) { setError("Username is already taken"); return false }
    if (!formData.firstName.trim()) { setError("First name is required"); return false }
    if (!formData.lastName.trim()) { setError("Last name is required"); return false }
    if (!formData.email.trim()) { setError("Email is required"); return false }
    if (!/\S+@\S+\.\S+/.test(formData.email)) { setError("Please enter a valid email address"); return false }
    if (emailAvailable === false) { setError("Email is already registered"); return false }
    if (!formData.password) { setError("Password is required"); return false }
    if (formData.password.length < 6) { setError("Password must be at least 6 characters long"); return false }
    if (formData.password !== formData.confirmPassword) { setError("Passwords do not match"); return false }
    if (!acceptTerms) { setError("Please accept the terms and conditions"); return false }
    return true
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError("")
    if (!validateForm()) return

    const success = await register({
      username: formData.username,
      firstName: formData.firstName,
      lastName: formData.lastName,
      email: formData.email,
      phone: formData.phone,
      password: formData.password,
    })

    if (success) {
      toast({
        title: "Account created!",
        description: "Welcome to Apple Store. You have been automatically signed in.",
      })
      router.push("/")
    } else {
      setError("Username or email already exists. Please try different credentials.")
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-slate-50 via-purple-50/30 to-blue-50/30 py-12 relative overflow-hidden">
      {/* Background decorations */}
      <div className="absolute inset-0">
        <div className="absolute top-20 right-10 w-72 h-72 bg-purple-500/10 rounded-full blur-3xl"></div>
        <div className="absolute bottom-20 left-10 w-72 h-72 bg-blue-500/10 rounded-full blur-3xl"></div>
      </div>
      
      <div className="w-full max-w-md mx-4 relative z-10">
        {/* Logo */}
        <div className="text-center mb-8">
          <Link href="/" className="inline-flex items-center space-x-3 group">
            <div className="bg-gradient-to-br from-slate-900 to-slate-700 text-white p-3 rounded-2xl group-hover:from-purple-600 group-hover:to-blue-600 transition-all duration-300 shadow-lg">
              <span className="font-bold text-xl">A</span>
            </div>
            <span className="font-bold text-2xl tracking-tight bg-gradient-to-r from-slate-900 to-slate-600 bg-clip-text text-transparent">
              Apple Store
            </span>
          </Link>
        </div>

        {/* Card */}
        <div className="bg-white/80 backdrop-blur-xl rounded-3xl shadow-xl shadow-black/5 border border-white/20 p-8">
          <div className="text-center mb-8">
            <div className="inline-flex items-center gap-2 bg-purple-50 rounded-full px-4 py-2 mb-4">
              <Sparkles className="w-4 h-4 text-purple-500" />
              <span className="text-sm font-medium text-purple-600">Join Us</span>
            </div>
            <h1 className="text-2xl font-bold text-slate-900">Create Account</h1>
            <p className="text-slate-500 mt-2">Sign up to start shopping for Apple products</p>
          </div>

          <form onSubmit={handleSubmit} className="space-y-4">
            {error && (
              <Alert variant="destructive" className="rounded-xl border-red-200 bg-red-50">
                <AlertDescription className="text-sm">{error}</AlertDescription>
              </Alert>
            )}

            {/* Username */}
            <div className="space-y-2">
              <Label htmlFor="username" className="text-sm font-medium text-slate-700">Username *</Label>
              <div className="relative">
                <Input
                  id="username"
                  name="username"
                  placeholder="Choose a username"
                  value={formData.username}
                  onChange={handleChange}
                  className={`h-11 rounded-xl bg-slate-50/80 border-slate-200/60 focus:bg-white focus:border-blue-300 focus:ring-2 focus:ring-blue-500/20 transition-all duration-300 pr-10 ${
                    usernameAvailable === true ? "border-green-500" : usernameAvailable === false ? "border-red-500" : ""
                  }`}
                  required
                />
                {formData.username.length >= 3 && (
                  <div className="absolute right-3 top-1/2 -translate-y-1/2">
                    {usernameAvailable === true ? (
                      <Check className="h-4 w-4 text-green-500" />
                    ) : usernameAvailable === false ? (
                      <X className="h-4 w-4 text-red-500" />
                    ) : (
                      <div className="h-4 w-4 border-2 border-slate-300 border-t-transparent rounded-full animate-spin" />
                    )}
                  </div>
                )}
              </div>
              {formData.username && (
                <p className={`text-xs ${usernameAvailable === true ? "text-green-600" : usernameAvailable === false ? "text-red-600" : "text-slate-500"}`}>
                  {usernameAvailable === true ? "Username is available" : usernameAvailable === false ? "Username is already taken" : "Checking availability..."}
                </p>
              )}
            </div>

            {/* Name */}
            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="firstName" className="text-sm font-medium text-slate-700">First Name *</Label>
                <Input
                  id="firstName"
                  name="firstName"
                  value={formData.firstName}
                  onChange={handleChange}
                  className="h-11 rounded-xl bg-slate-50/80 border-slate-200/60 focus:bg-white focus:border-blue-300 focus:ring-2 focus:ring-blue-500/20 transition-all duration-300"
                  required
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="lastName" className="text-sm font-medium text-slate-700">Last Name *</Label>
                <Input
                  id="lastName"
                  name="lastName"
                  value={formData.lastName}
                  onChange={handleChange}
                  className="h-11 rounded-xl bg-slate-50/80 border-slate-200/60 focus:bg-white focus:border-blue-300 focus:ring-2 focus:ring-blue-500/20 transition-all duration-300"
                  required
                />
              </div>
            </div>

            {/* Email */}
            <div className="space-y-2">
              <Label htmlFor="email" className="text-sm font-medium text-slate-700">Email *</Label>
              <div className="relative">
                <Input
                  id="email"
                  name="email"
                  type="email"
                  value={formData.email}
                  onChange={handleChange}
                  className={`h-11 rounded-xl bg-slate-50/80 border-slate-200/60 focus:bg-white focus:border-blue-300 focus:ring-2 focus:ring-blue-500/20 transition-all duration-300 pr-10 ${
                    emailAvailable === true ? "border-green-500" : emailAvailable === false ? "border-red-500" : ""
                  }`}
                  required
                />
                {formData.email.includes("@") && (
                  <div className="absolute right-3 top-1/2 -translate-y-1/2">
                    {emailAvailable === true ? (
                      <Check className="h-4 w-4 text-green-500" />
                    ) : emailAvailable === false ? (
                      <X className="h-4 w-4 text-red-500" />
                    ) : (
                      <div className="h-4 w-4 border-2 border-slate-300 border-t-transparent rounded-full animate-spin" />
                    )}
                  </div>
                )}
              </div>
              {formData.email.includes("@") && (
                <p className={`text-xs ${emailAvailable === true ? "text-green-600" : emailAvailable === false ? "text-red-600" : "text-slate-500"}`}>
                  {emailAvailable === true ? "Email is available" : emailAvailable === false ? "Email is already registered" : "Checking availability..."}
                </p>
              )}
            </div>

            {/* Phone */}
            <div className="space-y-2">
              <Label htmlFor="phone" className="text-sm font-medium text-slate-700">Phone Number</Label>
              <Input
                id="phone"
                name="phone"
                type="tel"
                value={formData.phone}
                onChange={handleChange}
                className="h-11 rounded-xl bg-slate-50/80 border-slate-200/60 focus:bg-white focus:border-blue-300 focus:ring-2 focus:ring-blue-500/20 transition-all duration-300"
              />
            </div>

            {/* Password */}
            <div className="space-y-2">
              <Label htmlFor="password" className="text-sm font-medium text-slate-700">Password *</Label>
              <div className="relative">
                <Input
                  id="password"
                  name="password"
                  type={showPassword ? "text" : "password"}
                  placeholder="Create a password"
                  value={formData.password}
                  onChange={handleChange}
                  className="h-11 rounded-xl bg-slate-50/80 border-slate-200/60 focus:bg-white focus:border-blue-300 focus:ring-2 focus:ring-blue-500/20 transition-all duration-300 pr-12"
                  required
                />
                <Button
                  type="button"
                  variant="ghost"
                  size="icon"
                  className="absolute right-2 top-1/2 -translate-y-1/2 h-8 w-8 rounded-lg hover:bg-slate-100"
                  onClick={() => setShowPassword(!showPassword)}
                >
                  {showPassword ? <EyeOff className="h-4 w-4 text-slate-400" /> : <Eye className="h-4 w-4 text-slate-400" />}
                </Button>
              </div>
            </div>

            {/* Confirm Password */}
            <div className="space-y-2">
              <Label htmlFor="confirmPassword" className="text-sm font-medium text-slate-700">Confirm Password *</Label>
              <div className="relative">
                <Input
                  id="confirmPassword"
                  name="confirmPassword"
                  type={showConfirmPassword ? "text" : "password"}
                  placeholder="Confirm your password"
                  value={formData.confirmPassword}
                  onChange={handleChange}
                  className="h-11 rounded-xl bg-slate-50/80 border-slate-200/60 focus:bg-white focus:border-blue-300 focus:ring-2 focus:ring-blue-500/20 transition-all duration-300 pr-12"
                  required
                />
                <Button
                  type="button"
                  variant="ghost"
                  size="icon"
                  className="absolute right-2 top-1/2 -translate-y-1/2 h-8 w-8 rounded-lg hover:bg-slate-100"
                  onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                >
                  {showConfirmPassword ? <EyeOff className="h-4 w-4 text-slate-400" /> : <Eye className="h-4 w-4 text-slate-400" />}
                </Button>
              </div>
            </div>

            {/* Terms */}
            <div className="flex items-start space-x-3 pt-1">
              <Checkbox
                id="terms"
                checked={acceptTerms}
                onCheckedChange={(checked) => setAcceptTerms(checked as boolean)}
                className="mt-0.5"
              />
              <Label htmlFor="terms" className="text-sm text-slate-600 leading-relaxed">
                I agree to the{" "}
                <Link href="/terms" className="text-blue-600 hover:text-blue-700 font-medium">Terms of Service</Link>
                {" "}and{" "}
                <Link href="/privacy" className="text-blue-600 hover:text-blue-700 font-medium">Privacy Policy</Link>
              </Label>
            </div>

            <Button
              type="submit"
              className="w-full h-12 rounded-xl bg-gradient-to-r from-purple-600 to-blue-600 hover:from-purple-700 hover:to-blue-700 font-semibold shadow-lg shadow-purple-500/25 hover:shadow-xl hover:shadow-purple-500/30 transition-all duration-300 group mt-2"
              disabled={isLoading || usernameAvailable === false || emailAvailable === false}
            >
              {isLoading ? (
                <Loader2 className="mr-2 h-5 w-5 animate-spin" />
              ) : (
                <>
                  Create Account
                  <ArrowRight className="ml-2 h-4 w-4 group-hover:translate-x-1 transition-transform" />
                </>
              )}
            </Button>
          </form>

          <div className="mt-8 pt-6 border-t border-slate-100">
            <p className="text-center text-sm text-slate-500">
              Already have an account?{" "}
              <Link href="/login" className="text-blue-600 hover:text-blue-700 font-semibold transition-colors">
                Sign in
              </Link>
            </p>
          </div>
        </div>
      </div>
    </div>
  )
}
