package com.meritshot.footballwallpaper.presentation.screens.user

import android.app.WallpaperManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.meritshot.footballwallpaper.presentation.components.LoadingScreen
import com.meritshot.footballwallpaper.presentation.theme.*
import com.meritshot.footballwallpaper.presentation.viewmodel.WallpaperViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WallpaperDetailScreen(
    wallpaperId: String,
    onBack: () -> Unit,
    viewModel: WallpaperViewModel = hiltViewModel()
) {
    val state   = viewModel.uiState.collectAsState().value
    val context = LocalContext.current
    val scope   = rememberCoroutineScope()
    var showWallpaperDialog by remember { mutableStateOf(false) }

    LaunchedEffect(wallpaperId) {
        viewModel.loadWallpaperById(wallpaperId)
    }

    val wallpaper = state.selectedWallpaper

    Scaffold(
        containerColor = BackgroundDark,
        bottomBar = {
            if (wallpaper != null) {
                Surface(
                    color = SurfaceDark,
                    tonalElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ActionButton(
                            icon = if (viewModel.isFavorite(wallpaperId)) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            label = "Favorite",
                            tint = if (viewModel.isFavorite(wallpaperId)) GreenPrimary else TextSecondary,
                            onClick = { viewModel.toggleFavorite(wallpaperId) }
                        )
                        ActionButton(
                            icon = Icons.Default.Download,
                            label = "Download",
                            tint = TextSecondary,
                            onClick = {
                                scope.launch {
                                    downloadWallpaper(context, wallpaper.imageUrl, wallpaper.title)
                                }
                            }
                        )
                        ActionButton(
                            icon = Icons.Default.Wallpaper,
                            label = "Set",
                            tint = TextSecondary,
                            onClick = { showWallpaperDialog = true }
                        )
                        ActionButton(
                            icon = Icons.Default.Share,
                            label = "Share",
                            tint = TextSecondary,
                            onClick = { shareWallpaper(context, wallpaper.imageUrl) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                state.isLoading -> LoadingScreen()
                wallpaper == null -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Wallpaper not found", color = TextSecondary)
                    }
                }
                else -> {
                    val imageModel = remember(wallpaper.imageUrl) {
                        val url = if (wallpaper.imageUrl.startsWith("asset://")) {
                            "file:///android_asset/${wallpaper.imageUrl.substringAfter("asset://")}"
                        } else {
                            wallpaper.imageUrl
                        }
                        
                        ImageRequest.Builder(context)
                            .data(url)
                            .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36")
                            .addHeader("Referer", "https://www.google.com/")
                            .allowHardware(false)
                            .crossfade(true)
                            .build()
                    }

                    // Full-screen image
                    AsyncImage(
                        model = imageModel,
                        contentDescription = wallpaper.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Top gradient overlay
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .align(Alignment.TopCenter)
                            .background(
                                androidx.compose.ui.graphics.Brush.verticalGradient(
                                    colors = listOf(Color.Black.copy(alpha = 0.7f), Color.Transparent)
                                )
                            )
                    )

                    // Back button
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .statusBarsPadding()
                            .padding(8.dp)
                            .align(Alignment.TopStart)
                            .background(Color.Black.copy(0.5f), CircleShape)
                    ) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                    }

                    // Title
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .statusBarsPadding()
                            .padding(top = 16.dp)
                    ) {
                        Text(
                            wallpaper.title,
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Tags
                    if (wallpaper.tags.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(start = 12.dp, bottom = 80.dp)
                                .padding(padding),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            wallpaper.tags.take(4).forEach { tag ->
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = GreenDark.copy(alpha = 0.85f)
                                ) {
                                    Text(
                                        "#$tag",
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        color = GreenLight,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showWallpaperDialog && wallpaper != null) {
        AlertDialog(
            onDismissRequest = { showWallpaperDialog = false },
            containerColor = SurfaceDark,
            title = { Text("Set as Wallpaper", color = TextPrimary) },
            confirmButton = {},
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Choose where to apply this wallpaper", color = TextSecondary, fontSize = 14.sp)
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = {
                            scope.launch {
                                setWallpaper(context, wallpaper.imageUrl, WallpaperManager.FLAG_SYSTEM)
                                showWallpaperDialog = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = GreenDark)
                    ) { Text("Home Screen") }
                    Button(
                        onClick = {
                            scope.launch {
                                setWallpaper(context, wallpaper.imageUrl, WallpaperManager.FLAG_LOCK)
                                showWallpaperDialog = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = GreenDark)
                    ) { Text("Lock Screen") }
                    Button(
                        onClick = {
                            scope.launch {
                                setWallpaper(context, wallpaper.imageUrl,
                                    WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK)
                                showWallpaperDialog = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                    ) { Text("Both", color = Color.Black) }
                    TextButton(
                        onClick = { showWallpaperDialog = false },
                        modifier = Modifier.align(Alignment.End)
                    ) { Text("Cancel", color = TextSecondary) }
                }
            }
        )
    }
}

@Composable
private fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        IconButton(onClick = onClick) {
            Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(26.dp))
        }
        Text(label, fontSize = 10.sp, color = TextSecondary)
    }
}

// ── Utility functions ──────────────────────────────────────────

private fun getRealUrl(imageUrl: String): String {
    return if (imageUrl.startsWith("asset://")) {
        "file:///android_asset/${imageUrl.substringAfter("asset://")}"
    } else {
        imageUrl
    }
}

private suspend fun downloadWallpaper(context: Context, imageUrl: String, title: String) {
    val finalUrl = getRealUrl(imageUrl)
    withContext(Dispatchers.IO) {
        try {
            val loader  = ImageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(finalUrl)
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36")
                .addHeader("Referer", "https://www.google.com/")
                .allowHardware(false)
                .build()
            val result  = (loader.execute(request) as? SuccessResult)?.drawable
            val bitmap  = (result as? BitmapDrawable)?.bitmap

            if (bitmap != null) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, "$title.jpg")
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                    put(MediaStore.Images.Media.RELATIVE_PATH,
                        Environment.DIRECTORY_PICTURES + "/FootballWallpaper")
                }
                val uri = context.contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues
                )
                uri?.let {
                    context.contentResolver.openOutputStream(it)?.use { stream ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 95, stream)
                    }
                }
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Saved to Gallery!", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Download failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

private suspend fun setWallpaper(context: Context, imageUrl: String, flag: Int) {
    val finalUrl = getRealUrl(imageUrl)
    withContext(Dispatchers.IO) {
        try {
            val loader  = ImageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(finalUrl)
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36")
                .addHeader("Referer", "https://www.google.com/")
                .allowHardware(false)
                .build()
            val result  = (loader.execute(request) as? SuccessResult)?.drawable
            val bitmap  = (result as? BitmapDrawable)?.bitmap

            if (bitmap != null) {
                val wallpaperManager = WallpaperManager.getInstance(context)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    wallpaperManager.setBitmap(bitmap, null, true, flag)
                } else {
                    wallpaperManager.setBitmap(bitmap)
                }
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Wallpaper set successfully!", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

private fun shareWallpaper(context: Context, imageUrl: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, "Check out this amazing football wallpaper! $imageUrl")
    }
    context.startActivity(Intent.createChooser(intent, "Share Wallpaper"))
}
