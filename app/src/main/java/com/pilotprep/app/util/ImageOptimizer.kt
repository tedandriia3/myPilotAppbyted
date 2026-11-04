package com.pilotprep.app.util

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import dagger.hilt.android.qualifiers.ApplicationContext
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.decode.SvgDecoder
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.size.Scale
import coil.size.Size
import coil.transform.CircleCropTransformation
import coil.transform.RoundedCornersTransformation
import coil.util.DebugLogger
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Optimiseur d'images avec Coil pour des performances optimales
 */
@Singleton
class ImageOptimizer @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val imageLoader: ImageLoader by lazy {
        ImageLoader.Builder(context)
            .components {
                add(SvgDecoder.Factory())
            }
            .crossfade(true)
            .respectCacheHeaders(false)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .networkCachePolicy(CachePolicy.ENABLED)
            .logger(DebugLogger())
            .build()
    }

    /**
     * Crée une requête d'image optimisée
     */
    fun createOptimizedRequest(
        data: Any?,
        size: Size = Size.ORIGINAL,
        scale: Scale = Scale.FIT,
        enableMemoryCache: Boolean = true,
        enableDiskCache: Boolean = true,
        crossfade: Boolean = true,
        placeholder: Int? = null,
        error: Int? = null
    ): ImageRequest {
        return ImageRequest.Builder(context)
            .data(data)
            .size(size)
            .scale(scale)
            .crossfade(crossfade)
            .memoryCachePolicy(if (enableMemoryCache) CachePolicy.ENABLED else CachePolicy.DISABLED)
            .diskCachePolicy(if (enableDiskCache) CachePolicy.ENABLED else CachePolicy.DISABLED)
            .apply {
                placeholder?.let { placeholder(it) }
                error?.let { error(it) }
            }
            .build()
    }

    /**
     * Crée une requête pour les images d'avions (optimisée)
     */
    fun createAircraftImageRequest(
        imageUrl: String,
        width: Dp = 200.dp,
        height: Dp = 150.dp,
        roundedCorners: Boolean = true
    ): ImageRequest {
        return ImageRequest.Builder(context)
            .data(imageUrl)
            .size(Size(width.value.toInt(), height.value.toInt()))
            .scale(Scale.FIT)
            .crossfade(true)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .apply {
                if (roundedCorners) {
                    transformations(RoundedCornersTransformation(8f))
                }
            }
            .build()
    }

    /**
     * Crée une requête pour les avatars (circulaire)
     */
    fun createAvatarImageRequest(
        imageUrl: String,
        size: Dp = 40.dp
    ): ImageRequest {
        return ImageRequest.Builder(context)
            .data(imageUrl)
            .size(Size(size.value.toInt(), size.value.toInt()))
            .scale(Scale.FIT)
            .crossfade(true)
            .transformations(CircleCropTransformation())
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .build()
    }

    /**
     * Crée une requête pour les images de fond (grandes images)
     */
    fun createBackgroundImageRequest(
        imageUrl: String,
        width: Dp = 400.dp,
        height: Dp = 300.dp
    ): ImageRequest {
        return ImageRequest.Builder(context)
            .data(imageUrl)
            .size(Size(width.value.toInt(), height.value.toInt()))
            .scale(Scale.FIT)
            .crossfade(true)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .build()
    }

    /**
     * Nettoie le cache d'images
     */
    suspend fun clearImageCache() {
        imageLoader.memoryCache?.clear()
        imageLoader.diskCache?.clear()
    }

    /**
     * Obtient les statistiques du cache d'images
     */
    fun getImageCacheStats(): ImageCacheStats {
        val memoryCache = imageLoader.memoryCache
        val diskCache = imageLoader.diskCache
        
        return ImageCacheStats(
            memoryCacheSize = (memoryCache?.size ?: 0).toLong(),
            memoryCacheMaxSize = (memoryCache?.maxSize ?: 0).toLong(),
            diskCacheSize = diskCache?.size ?: 0L
        )
    }
}

/**
 * Statistiques du cache d'images
 */
data class ImageCacheStats(
    val memoryCacheSize: Long,
    val memoryCacheMaxSize: Long,
    val diskCacheSize: Long
) {
    val memoryCacheUsagePercent: Float
        get() = if (memoryCacheMaxSize > 0) {
            (memoryCacheSize.toFloat() / memoryCacheMaxSize.toFloat()) * 100f
        } else 0f
}

/**
 * Composable pour afficher une image optimisée
 */
@Composable
fun OptimizedAsyncImage(
    imageUrl: String,
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier,
    contentDescription: String? = null,
    width: Dp = 200.dp,
    height: Dp = 150.dp,
    roundedCorners: Boolean = true,
    placeholder: Int? = null,
    error: Int? = null
) {
    val context = LocalContext.current
    val imageOptimizer = remember { ImageOptimizer(context) }
    
    val imageRequest = remember(imageUrl, width, height, roundedCorners) {
        imageOptimizer.createAircraftImageRequest(
            imageUrl = imageUrl,
            width = width,
            height = height,
            roundedCorners = roundedCorners
        )
    }

    AsyncImage(
        model = imageRequest,
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = androidx.compose.ui.layout.ContentScale.Crop
    )
}

/**
 * Composable pour afficher un avatar optimisé
 */
@Composable
fun OptimizedAvatarImage(
    imageUrl: String,
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier,
    contentDescription: String? = null,
    size: Dp = 40.dp
) {
    val context = LocalContext.current
    val imageOptimizer = remember { ImageOptimizer(context) }
    
    val imageRequest = remember(imageUrl, size) {
        imageOptimizer.createAvatarImageRequest(
            imageUrl = imageUrl,
            size = size
        )
    }

    AsyncImage(
        model = imageRequest,
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = androidx.compose.ui.layout.ContentScale.Crop
    )
}
