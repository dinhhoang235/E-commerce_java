"use client"

import type React from "react"

import { useState } from "react"
import { useRouter } from "next/navigation"
import Link from "next/link"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { Loader2, Eye, EyeOff, User, Mail, ArrowRight, Sparkles } from "lucide-react"
import { useAuth } from "@/components/auth-provider"
import { useToast } from "@/hooks/use-toast"
import { login as loginApi } from "@/lib/services/auth"

export default function LoginPage() {
    const [email, setEmail] = useState("")
    const [password, setPassword] = useState("")
    const [showPassword, setShowPassword] = useState(false)
    const [error, setError] = useState("")
    const [isSubmitting, setIsSubmitting] = useState(false)
    const { login, isLoading } = useAuth()
    const router = useRouter()
    const { toast } = useToast()

    const handleLogin = async (e: React.FormEvent) => {
        e.preventDefault()
        setError("")
        if (isSubmitting) return
        setIsSubmitting(true)
        
        try {
            const token = await loginApi({
                username_or_email: email,
                password
            })
            await login({
                access: token.access,
                refresh: token.refresh,
            })
            toast({
                title: "Welcome back!",
                description: "You have been successfully signed in.",
            })
            router.push("/")
        } catch (error: unknown) {
            let message = "Something went wrong."
            if (error instanceof Error) {
                message = error.message
            } else if (error && typeof error === 'object' && 'response' in error) {
                const apiError = error as { response?: { data?: { detail?: string } } }
                message = apiError.response?.data?.detail || "Login failed."
            }
            setError(message)
        } finally {
            setIsSubmitting(false)
        }
    }

    const handleEmailChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        setEmail(e.target.value)
        if (error) setError("")
    }

    const handlePasswordChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        setPassword(e.target.value)
        if (error) setError("")
    }

    return (
        <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-slate-50 via-blue-50/30 to-purple-50/30 relative overflow-hidden">
            {/* Background decorations */}
            <div className="absolute inset-0">
                <div className="absolute top-20 left-10 w-72 h-72 bg-blue-500/10 rounded-full blur-3xl"></div>
                <div className="absolute bottom-20 right-10 w-72 h-72 bg-purple-500/10 rounded-full blur-3xl"></div>
            </div>
            
            <div className="w-full max-w-md mx-4 relative z-10">
                {/* Logo */}
                <div className="text-center mb-8">
                    <Link href="/" className="inline-flex items-center space-x-3 group">
                        <div className="bg-gradient-to-br from-slate-900 to-slate-700 text-white p-3 rounded-2xl group-hover:from-blue-600 group-hover:to-purple-600 transition-all duration-300 shadow-lg">
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
                        <div className="inline-flex items-center gap-2 bg-blue-50 rounded-full px-4 py-2 mb-4">
                            <Sparkles className="w-4 h-4 text-blue-500" />
                            <span className="text-sm font-medium text-blue-600">Welcome Back</span>
                        </div>
                        <h1 className="text-2xl font-bold text-slate-900">Sign In</h1>
                        <p className="text-slate-500 mt-2">
                            Enter your credentials to access your account
                        </p>
                    </div>

                    <form onSubmit={handleLogin} className="space-y-5">
                        {error && (
                            <Alert variant="destructive" className="rounded-xl border-red-200 bg-red-50">
                                <AlertDescription className="text-sm">{error}</AlertDescription>
                            </Alert>
                        )}

                        <div className="space-y-2">
                            <Label htmlFor="usernameOrEmail" className="text-sm font-medium text-slate-700">
                                Username or Email
                            </Label>
                            <div className="relative">
                                <div className="absolute left-3 top-1/2 -translate-y-1/2">
                                    {email.includes('@') ? (
                                        <Mail className="h-4 w-4 text-slate-400" />
                                    ) : (
                                        <User className="h-4 w-4 text-slate-400" />
                                    )}
                                </div>
                                <Input
                                    id="usernameOrEmail"
                                    type="text"
                                    placeholder="Enter username or email"
                                    value={email}
                                    onChange={handleEmailChange}
                                    className="pl-10 h-12 rounded-xl bg-slate-50/80 border-slate-200/60 focus:bg-white focus:border-blue-300 focus:ring-2 focus:ring-blue-500/20 transition-all duration-300"
                                    required
                                />
                            </div>
                        </div>

                        <div className="space-y-2">
                            <Label htmlFor="password" className="text-sm font-medium text-slate-700">
                                Password
                            </Label>
                            <div className="relative">
                                <Input
                                    id="password"
                                    type={showPassword ? "text" : "password"}
                                    placeholder="Enter your password"
                                    value={password}
                                    onChange={handlePasswordChange}
                                    className="h-12 rounded-xl bg-slate-50/80 border-slate-200/60 focus:bg-white focus:border-blue-300 focus:ring-2 focus:ring-blue-500/20 transition-all duration-300 pr-12"
                                    required
                                />
                                <Button
                                    type="button"
                                    variant="ghost"
                                    size="icon"
                                    className="absolute right-2 top-1/2 -translate-y-1/2 h-8 w-8 rounded-lg hover:bg-slate-100"
                                    onClick={() => setShowPassword(!showPassword)}
                                >
                                    {showPassword ? (
                                        <EyeOff className="h-4 w-4 text-slate-400" />
                                    ) : (
                                        <Eye className="h-4 w-4 text-slate-400" />
                                    )}
                                </Button>
                            </div>
                        </div>

                        <div className="flex items-center justify-between">
                            <Link href="/forgot-password" className="text-sm text-blue-600 hover:text-blue-700 font-medium transition-colors">
                                Forgot password?
                            </Link>
                        </div>

                        <Button 
                            type="submit" 
                            className="w-full h-12 rounded-xl bg-gradient-to-r from-blue-600 to-purple-600 hover:from-blue-700 hover:to-purple-700 font-semibold shadow-lg shadow-blue-500/25 hover:shadow-xl hover:shadow-blue-500/30 transition-all duration-300 group" 
                            disabled={isSubmitting}
                        >
                            {isSubmitting ? (
                                <Loader2 className="mr-2 h-5 w-5 animate-spin" />
                            ) : (
                                <>
                                    Sign In
                                    <ArrowRight className="ml-2 h-4 w-4 group-hover:translate-x-1 transition-transform" />
                                </>
                            )}
                        </Button>
                    </form>

                    <div className="mt-8 pt-6 border-t border-slate-100">
                        <p className="text-center text-sm text-slate-500">
                            Don&apos;t have an account?{" "}
                            <Link href="/register" className="text-blue-600 hover:text-blue-700 font-semibold transition-colors">
                                Sign up
                            </Link>
                        </p>
                    </div>
                </div>
            </div>
        </div>
    )
}
