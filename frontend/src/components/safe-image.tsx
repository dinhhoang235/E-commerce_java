"use client"

import Image from "next/image"
import { useState, useEffect } from "react"
import { formatImageUrl, isExternalImage } from "@/lib/utils/image"

interface SafeImageProps {
  src: string
  alt: string
  width: number
  height: number
  className?: string
  unoptimized?: boolean
}

export function SafeImage({ src, alt, width, height, className = "", unoptimized = false }: SafeImageProps) {
  const [imageError, setImageError] = useState(false)
  const [imageSrc, setImageSrc] = useState(formatImageUrl(src))

  // Reset error state when src changes
  useEffect(() => {
    setImageError(false)
    setImageSrc(formatImageUrl(src))
  }, [src])

  // Backend media paths, blob URLs, and data URLs should bypass Next optimization.
  const shouldUseImgTag = imageError ||
                         unoptimized ||
                         isExternalImage(imageSrc) ||
                         imageSrc.startsWith('blob:') ||
                         imageSrc.startsWith('data:')

  if (shouldUseImgTag) {
    return (
      <img
        src={imageSrc}
        alt={alt}
        width={width}
        height={height}
        className={className}
        style={{ objectFit: 'contain' }}
        onError={() => setImageSrc("/placeholder.svg")}
      />
    )
  }

  // For local images (like placeholder.svg), use Next.js Image
  return (
    <Image
      src={imageSrc}
      alt={alt}
      width={width}
      height={height}
      className={className}
      unoptimized={unoptimized}
      onError={() => {
        setImageError(true)
        setImageSrc(src) // Keep original src for img fallback
      }}
    />
  )
}
