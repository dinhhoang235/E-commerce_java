"use client"

import { useState } from "react"
import Image from "next/image"
import { cn } from "@/lib/utils"
import { ChevronLeft, ChevronRight, ZoomIn } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Dialog, DialogContent, DialogTrigger } from "@/components/ui/dialog"
import { formatImageUrl, isExternalImage } from "@/lib/utils/image"

interface ProductGalleryProps {
  product: any
}

export function ProductGallery({ product }: ProductGalleryProps) {
  const [selectedImage, setSelectedImage] = useState(0)

  // Use actual product images from the API, fallback to single image
  const images: string[] = product.images && product.images.length > 0
    ? product.images.map((img: any) => formatImageUrl(img.image_url))
    : [formatImageUrl(product.image)]

  const nextImage = () => {
    setSelectedImage((prev) => (prev + 1) % images.length)
  }

  const prevImage = () => {
    setSelectedImage((prev) => (prev - 1 + images.length) % images.length)
  }

  return (
    <div className="space-y-4">
      {/* Main Image */}
      <div className="relative aspect-square bg-white rounded-lg overflow-hidden">
        <Dialog>
          <DialogTrigger asChild>
            <Button variant="outline" size="icon" className="absolute top-4 right-4 z-10 bg-white/80 backdrop-blur-sm">
              <ZoomIn className="h-4 w-4" />
            </Button>
          </DialogTrigger>
          <DialogContent className="max-w-3xl">
            <div className="aspect-square relative">
              <Image
                src={images[selectedImage] || "/placeholder.svg"}
                alt={product.name}
                fill
                unoptimized={isExternalImage(images[selectedImage])}
                className="object-contain"
              />
            </div>
          </DialogContent>
        </Dialog>

        <Image 
          src={images[selectedImage] || "/placeholder.svg"} 
          alt={product.name} 
          fill 
          unoptimized={isExternalImage(images[selectedImage])}
          className="object-contain" 
        />

        {/* Navigation Arrows - only show if multiple images */}
        {images.length > 1 && (
          <>
            <Button
              variant="outline"
              size="icon"
              className="absolute left-4 top-1/2 -translate-y-1/2 z-10 bg-white/80 backdrop-blur-sm"
              onClick={prevImage}
            >
              <ChevronLeft className="h-4 w-4" />
            </Button>

            <Button
              variant="outline"
              size="icon"
              className="absolute right-4 top-1/2 -translate-y-1/2 z-10 bg-white/80 backdrop-blur-sm"
              onClick={nextImage}
            >
              <ChevronRight className="h-4 w-4" />
            </Button>
          </>
        )}
      </div>

      {/* Thumbnails */}
      {images.length > 1 && (
        <div className="grid grid-cols-4 gap-4">
          {images.map((image, index) => (
            <button
              key={index}
              className={cn(
                "aspect-square rounded-md overflow-hidden border-2",
                selectedImage === index ? "border-blue-600" : "border-transparent",
              )}
              onClick={() => setSelectedImage(index)}
            >
              <div className="relative h-full w-full">
                <Image
                  src={image || "/placeholder.svg"}
                  alt={`${product.name} - Image ${index + 1}`}
                  fill
                  unoptimized={isExternalImage(image)}
                  className="object-cover"
                />
              </div>
            </button>
          ))}
        </div>
      )}
    </div>
  )
}
