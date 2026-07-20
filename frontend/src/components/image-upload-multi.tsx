"use client"

import type React from "react"
import { useState, useRef } from "react"
import { Button } from "@/components/ui/button"
import { Label } from "@/components/ui/label"
import { Card, CardContent } from "@/components/ui/card"
import { Upload, X, ImageIcon, Star } from "lucide-react"
import { useToast } from "@/hooks/use-toast"
import Image from "next/image"
import { isExternalImage } from "@/lib/utils/image"

interface ImageUploadMultiProps {
  existingImages?: Array<{ id: number; image_url: string; is_primary: boolean; sort_order: number }>
  newFiles: File[]
  onFilesChange: (files: File[]) => void
  onExistingImagesChange?: (images: Array<{ id: number; action: "keep" | "delete" }>) => void
  onSetPrimary?: (imageId: number) => void
  label?: string
  maxImages?: number
  className?: string
}

export function ImageUploadMulti({
  existingImages = [],
  newFiles,
  onFilesChange,
  onExistingImagesChange,
  onSetPrimary,
  label = "Product Images",
  maxImages = 10,
  className,
}: ImageUploadMultiProps) {
  const [isDragging, setIsDragging] = useState(false)
  const fileInputRef = useRef<HTMLInputElement>(null)
  const { toast } = useToast()
  const [removedIds, setRemovedIds] = useState<Set<number>>(new Set())

  const totalImages = existingImages.filter(img => !removedIds.has(img.id)).length + newFiles.length
  const canAddMore = totalImages < maxImages

  const handleFilesSelect = (fileList: FileList | null) => {
    if (!fileList) return
    const files = Array.from(fileList)
    
    const validFiles = files.filter(file => {
      if (!file.type.startsWith("image/")) {
        toast({
          title: "Invalid file type",
          description: `${file.name} is not an image file`,
          variant: "destructive",
        })
        return false
      }
      if (file.size > 10 * 1024 * 1024) {
        toast({
          title: "File too large",
          description: `${file.name} exceeds 10MB limit`,
          variant: "destructive",
        })
        return false
      }
      return true
    })

    const remainingSlots = maxImages - totalImages
    const filesToAdd = validFiles.slice(0, remainingSlots)
    
    if (validFiles.length > remainingSlots) {
      toast({
        title: "Too many images",
        description: `Only ${remainingSlots} more image(s) can be added. Maximum is ${maxImages}.`,
        variant: "destructive",
      })
    }

    onFilesChange([...newFiles, ...filesToAdd])
  }

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault()
    setIsDragging(false)
    handleFilesSelect(e.dataTransfer.files)
  }

  const handleFileInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    handleFilesSelect(e.target.files)
    if (fileInputRef.current) {
      fileInputRef.current.value = ""
    }
  }

  const handleRemoveNewFile = (index: number) => {
    const updated = newFiles.filter((_, i) => i !== index)
    onFilesChange(updated)
  }

  const handleRemoveExistingImage = (id: number) => {
    const newRemoved = new Set(removedIds)
    newRemoved.add(id)
    setRemovedIds(newRemoved)
    onExistingImagesChange?.(
      existingImages.map(img => ({
        id: img.id,
        action: newRemoved.has(img.id) ? "delete" as const : "keep" as const,
      }))
    )
  }

  const handleRestoreExistingImage = (id: number) => {
    const newRemoved = new Set(removedIds)
    newRemoved.delete(id)
    setRemovedIds(newRemoved)
    onExistingImagesChange?.(
      existingImages.map(img => ({
        id: img.id,
        action: newRemoved.has(img.id) ? "delete" as const : "keep" as const,
      }))
    )
  }

  const visibleExisting = existingImages.filter(img => !removedIds.has(img.id))

  return (
    <div className={`space-y-2 ${className}`}>
      <Label>{label}</Label>
      <p className="text-sm text-slate-500">
        {totalImages}/{maxImages} images uploaded
      </p>

      {/* Existing Images */}
      {visibleExisting.length > 0 && (
        <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-3">
          {visibleExisting.map((image) => (
            <Card key={image.id} className="relative group">
              <CardContent className="p-2">
                <div className="aspect-square relative rounded-md overflow-hidden bg-slate-100">
                  <img
                    src={image.image_url}
                    alt="Product image"
                    className="absolute inset-0 w-full h-full object-contain"
                  />
                  {image.is_primary && (
                    <div className="absolute top-1 left-1 bg-blue-600 text-white rounded-full p-0.5">
                      <Star className="h-3 w-3 fill-current" />
                    </div>
                  )}
                  <div className="absolute top-1 right-1 flex gap-1">
                    {!image.is_primary && onSetPrimary && (
                      <Button
                        type="button"
                        variant="secondary"
                        size="icon"
                        className="h-6 w-6"
                        title="Set as primary"
                        onClick={() => onSetPrimary(image.id)}
                      >
                        <Star className="h-3 w-3" />
                      </Button>
                    )}
                    <Button
                      type="button"
                      variant="destructive"
                      size="icon"
                      className="h-6 w-6"
                      onClick={() => handleRemoveExistingImage(image.id)}
                    >
                      <X className="h-3 w-3" />
                    </Button>
                  </div>
                </div>
                <p className="text-xs text-slate-500 mt-1 truncate">
                  {image.is_primary ? "Primary" : `#${image.sort_order + 1}`}
                </p>
              </CardContent>
            </Card>
          ))}
        </div>
      )}

      {/* New Files */}
      {newFiles.length > 0 && (
        <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-3">
          {newFiles.map((file, index) => (
            <Card key={`new-${index}`} className="relative group">
              <CardContent className="p-2">
                <div className="aspect-square relative rounded-md overflow-hidden bg-slate-100">
                  <img
                    src={URL.createObjectURL(file)}
                    alt={`New image ${index + 1}`}
                    className="absolute inset-0 w-full h-full object-contain"
                  />
                  <Button
                    type="button"
                    variant="destructive"
                    size="icon"
                    className="absolute top-1 right-1 h-6 w-6 opacity-0 group-hover:opacity-100 transition-opacity"
                    onClick={() => handleRemoveNewFile(index)}
                  >
                    <X className="h-3 w-3" />
                  </Button>
                </div>
                <p className="text-xs text-slate-500 mt-1 truncate">{file.name}</p>
              </CardContent>
            </Card>
          ))}
        </div>
      )}

      {/* Upload Area */}
      {canAddMore && (
        <Card
          className={`border-2 border-dashed transition-colors cursor-pointer ${
            isDragging ? "border-blue-500 bg-blue-50" : "border-slate-300 hover:border-slate-400"
          }`}
          onDrop={handleDrop}
          onDragOver={(e) => {
            e.preventDefault()
            setIsDragging(true)
          }}
          onDragLeave={(e) => {
            e.preventDefault()
            setIsDragging(false)
          }}
          onClick={() => fileInputRef.current?.click()}
        >
          <CardContent className="p-6">
            <div className="text-center space-y-2">
              <div className="mx-auto w-12 h-12 bg-slate-100 rounded-lg flex items-center justify-center">
                <ImageIcon className="h-6 w-6 text-slate-400" />
              </div>
              <div>
                <p className="text-sm font-medium">Upload Images</p>
                <p className="text-xs text-slate-500">Drag & drop or click to select ({maxImages - totalImages} remaining)</p>
              </div>
              <Button type="button" variant="outline" size="sm" className="mx-auto bg-transparent">
                <Upload className="mr-2 h-3 w-3" />
                Choose Files
              </Button>
              <p className="text-xs text-slate-400">JPG, PNG, GIF up to 10MB each</p>
            </div>
          </CardContent>
        </Card>
      )}

      <input
        ref={fileInputRef}
        type="file"
        accept="image/*"
        multiple
        onChange={handleFileInputChange}
        className="hidden"
      />
    </div>
  )
}
