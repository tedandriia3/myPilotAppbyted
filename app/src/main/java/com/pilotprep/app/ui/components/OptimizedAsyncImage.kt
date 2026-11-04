package com.pilotprep.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import coil.size.Size
import coil.util.CoilUtils

/**
 * Composant d'image optimisé avec cache, placeholder et gestion d'erreur
 * Utilise Coil pour le chargement efficace des images
 */
@Composable
fun OptimizedAsyncImage(
    imageUrl: String?,
    contentDescription: String,
    modifier: Modifier = Modifier,
    placeholder: Int? = null,
    errorImage: Int? = null,
    contentScale: ContentScale = ContentScale.Crop,
    size: Size? = null
) {
    val context = LocalContext.current
    
    if (imageUrl.isNullOrBlank()) {
        errorImage?.let {
            AsyncImage(
                model = painterResource(it),
                contentDescription = contentDescription,
                modifier = modifier,
                contentScale = contentScale
            )
        }
        return
    }
    
    SubcomposeAsyncImage(
        model = com.pilotprep.app.util.ImageOptimizer(context).createOptimizedRequest(
            data = imageUrl,
            size = size ?: Size.ORIGINAL,
            enableMemoryCache = true,
            enableDiskCache = true,
            crossfade = true,
            placeholder = placeholder,
            error = errorImage
        ),
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale,
        loading = {
            Box(modifier = modifier.fillMaxSize()) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                )
            }
        },
        error = {
            errorImage?.let {
                Image(
                    painter = painterResource(it),
                    contentDescription = "Image error placeholder",
                    modifier = modifier,
                    contentScale = contentScale
                )
            }
        }
    )
}

/**
 * Composant d'image optimisé pour les thumbnails
 */
@Composable
fun OptimizedThumbnail(
    imageUrl: String?,
    contentDescription: String,
    modifier: Modifier = Modifier,
    placeholder: Int? = null,
    errorImage: Int? = null
) {
    OptimizedAsyncImage(
        imageUrl = imageUrl,
        contentDescription = contentDescription,
        modifier = modifier,
        placeholder = placeholder,
        errorImage = errorImage,
        contentScale = ContentScale.Crop,
        size = Size(200, 200) // Taille optimisée pour les thumbnails
    )
}

/**
 * Composant d'image optimisé pour les images fullscreen
 */
@Composable
fun OptimizedFullscreenImage(
    imageUrl: String?,
    contentDescription: String,
    modifier: Modifier = Modifier,
    placeholder: Int? = null,
    errorImage: Int? = null
) {
    OptimizedAsyncImage(
        imageUrl = imageUrl,
        contentDescription = contentDescription,
        modifier = modifier,
        placeholder = placeholder,
        errorImage = errorImage,
        contentScale = ContentScale.Fit,
        size = Size(800, 800) // Taille optimisée pour les images fullscreen
    )
}

/**
 * Composant d'image optimisé pour les avatars
 */
@Composable
fun OptimizedAvatar(
    imageUrl: String?,
    contentDescription: String,
    modifier: Modifier = Modifier,
    placeholder: Int? = null,
    errorImage: Int? = null
) {
    OptimizedAsyncImage(
        imageUrl = imageUrl,
        contentDescription = contentDescription,
        modifier = modifier,
        placeholder = placeholder,
        errorImage = errorImage,
        contentScale = ContentScale.Crop,
        size = Size(100, 100) // Taille optimisée pour les avatars
    )
}


