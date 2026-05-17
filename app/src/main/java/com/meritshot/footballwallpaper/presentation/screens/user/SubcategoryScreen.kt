package com.meritshot.footballwallpaper.presentation.screens.user

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.meritshot.footballwallpaper.presentation.components.*
import com.meritshot.footballwallpaper.presentation.theme.*
import com.meritshot.footballwallpaper.presentation.viewmodel.WallpaperViewModel

@Composable
fun SubcategoryScreen(
    subcategoryId: String,
    subcategoryName: String,
    onBack: () -> Unit,
    onWallpaperClick: (String) -> Unit,
    viewModel: WallpaperViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(subcategoryId) {
        viewModel.loadWallpapersBySubcategory(subcategoryId)
    }

    Scaffold(
        topBar = { AppTopBar(title = subcategoryName, onBack = onBack) },
        containerColor = BackgroundDark
    ) { padding ->
        if (state.isLoading) {
            LoadingScreen()
        } else if (state.wallpapers.isEmpty()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No wallpapers found", color = TextSecondary)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                items(state.wallpapers) { wallpaper ->
                    WallpaperCard(
                        wallpaper = wallpaper,
                        isFavorite = viewModel.isFavorite(wallpaper.id),
                        onClick = { onWallpaperClick(wallpaper.id) }
                    )
                }
            }
        }
    }
}
