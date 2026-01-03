package com.example.myapplication.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.YuvImage
import android.util.Base64
import androidx.camera.core.ImageProxy
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

/**
 * Helper object for converting CameraX frames to Base64-encoded JPEG images.
 * 
 * This handles the conversion pipeline:
 * 1. ImageProxy (CameraX) → Bitmap
 * 2. Bitmap → JPEG byte array
 * 3. JPEG → Base64 String
 * 
 * Usage:
 * val base64 = FrameConverter.imageProxyToBase64(imageProxy)
 */
object FrameConverter {

    private const val JPEG_QUALITY = 90
    
    /**
     * Convert CameraX ImageProxy to Base64-encoded JPEG string.
     * 
     * This method:
     * - Converts ImageProxy to Bitmap
     * - Handles rotation automatically
     * - Compresses to JPEG format
     * - Encodes to Base64
     * 
     * @param imageProxy The CameraX ImageProxy from ImageCapture
     * @return Base64-encoded JPEG string, or null if conversion fails
     */
    fun imageProxyToBase64(imageProxy: ImageProxy): String? {
        return try {
            val bitmap = imageProxyToBitmap(imageProxy) ?: return null
            bitmapToBase64(bitmap).also {
                bitmap.recycle()
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Convert ImageProxy to Bitmap.
     * 
     * Handles YUV_420_888 format commonly used by CameraX.
     * Includes rotation correction based on imageInfo.
     * 
     * @param imageProxy The CameraX ImageProxy
     * @return Bitmap or null if conversion fails
     */
    private fun imageProxyToBitmap(imageProxy: ImageProxy): Bitmap? {
        return try {
            val image = imageProxy.image ?: return null
            val planes = image.planes
            
            // Get the Y (luminance) buffer
            val yBuffer: ByteBuffer = planes[0].buffer
            val uBuffer: ByteBuffer = planes[1].buffer
            val vBuffer: ByteBuffer = planes[2].buffer
            
            val ySize = yBuffer.remaining()
            val uSize = uBuffer.remaining()
            val vSize = vBuffer.remaining()
            
            // Create a buffer that holds all pixel data
            val nv21 = ByteArray(ySize + uSize + vSize)
            
            yBuffer.get(nv21, 0, ySize)
            vBuffer.get(nv21, ySize, vSize)
            uBuffer.get(nv21, ySize + vSize, uSize)
            
            // Get image dimensions
            val width = image.width
            val height = image.height
            
            // Convert NV21 to RGB bitmap using BitmapFactory
            val bitmap = rawNV21ToBitmap(nv21, width, height)
            
            // Apply rotation correction
            val rotationDegrees = imageProxy.imageInfo.rotationDegrees
            if (rotationDegrees != 0) {
                val matrix = Matrix()
                matrix.postRotate(rotationDegrees.toFloat())
                Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true).also {
                    bitmap.recycle()
                }
            } else {
                bitmap
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Convert NV21 byte array to Bitmap.
     * NV21 is the YUV format used by CameraX.
     */
    private fun rawNV21ToBitmap(nv21: ByteArray, width: Int, height: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(width * height)
        
        var yIdx = 0
        var uvIdx = nv21.size - width * height / 4
        
        for (j in 0 until height) {
            for (i in 0 until width) {
                val y = nv21[yIdx++].toInt() and 0xFF
                if (i % 2 == 0 && j % 2 == 0) {
                    val v = nv21[uvIdx++].toInt() and 0xFF
                    val u = nv21[uvIdx++].toInt() and 0xFF
                    pixels[j * width + i] = yuvToRgb(y, u, v)
                } else {
                    pixels[j * width + i] = y or 0xFF000000.toInt()
                }
            }
        }
        
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return bitmap
    }

    /**
     * Convert YUV values to RGB.
     * Standard Y'UV to RGB conversion for camera images.
     */
    private fun yuvToRgb(y: Int, u: Int, v: Int): Int {
        val yf = y - 16
        val uf = u - 128
        val vf = v - 128
        
        var r = (1.164 * yf + 1.596 * vf).toInt()
        var g = (1.164 * yf - 0.813 * vf - 0.392 * uf).toInt()
        var b = (1.164 * yf + 2.017 * uf).toInt()
        
        r = r.coerceIn(0, 255)
        g = g.coerceIn(0, 255)
        b = b.coerceIn(0, 255)
        
        return (0xFF shl 24) or (r shl 16) or (g shl 8) or b
    }

    /**
     * Convert Bitmap to Base64-encoded JPEG string.
     * 
     * @param bitmap The source Bitmap
     * @param quality JPEG compression quality (0-100)
     * @return Base64-encoded JPEG string
     */
    private fun bitmapToBase64(bitmap: Bitmap, quality: Int = JPEG_QUALITY): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }

    /**
     * Alternative method using YuvImage for faster conversion.
     * Useful for real-time processing.
     * 
     * @param imageProxy The CameraX ImageProxy
     * @return Base64-encoded JPEG string
     */
    fun imageProxyToBase64Fast(imageProxy: ImageProxy): String? {
        return try {
            val image = imageProxy.image ?: return null
            val planes = image.planes
            
            // Get buffer from Y plane
            val buffer = planes[0].buffer
            val data = ByteArray(buffer.remaining())
            buffer.get(data)
            
            // Create YuvImage
            val yuvImage = YuvImage(
                data,
                ImageFormat.NV21,
                image.width,
                image.height,
                null
            )
            
            // Compress to JPEG
            val outputStream = ByteArrayOutputStream()
            yuvImage.compressToJpeg(
                Rect(0, 0, image.width, image.height),
                JPEG_QUALITY,
                outputStream
            )
            
            // Encode to Base64
            Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
        } catch (e: Exception) {
            // Fallback to slower but more reliable method
            imageProxyToBase64(imageProxy)
        }
    }
}

