package com.meritshot.footballwallpaper.presentation.screens.user

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.meritshot.footballwallpaper.data.repository.WallpaperRepository
import com.meritshot.footballwallpaper.presentation.components.*
import com.meritshot.footballwallpaper.presentation.theme.*
import com.meritshot.footballwallpaper.presentation.viewmodel.WallpaperViewModel
import com.meritshot.footballwallpaper.data.model.Wallpaper
import kotlinx.coroutines.launch
import javax.inject.Inject

@Composable
fun FavoritesScreen(
    onBack: () -> Unit,
    onWallpaperClick: (String) -> Unit,
    viewModel: WallpaperViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = { AppTopBar(title = "My Favorites ♥", onBack = onBack) },
        containerColor = BackgroundDark
    ) { padding ->
        if (state.favorites.isEmpty()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("♡", fontSize = 64.sp, color = TextSecondary)
                    Spacer(Modifier.height(12.dp))
                    Text("No favorites yet", color = TextSecondary)
                    Text("Tap ♥ on any wallpaper to save it here", color = TextSecondary,
                        fontSize = 12.sp)
                }
            }
        } else {
            // We need to show favorite wallpapers - use ids from favorites
            val favoriteIds = state.favorites.map { it.wallpaperId }.toSet()
            val favoriteWallpapers = state.wallpapers.filter { it.id in favoriteIds }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                items(favoriteWallpapers) { wallpaper ->
                    WallpaperCard(
                        wallpaper = wallpaper,
                        isFavorite = true,
                        onClick = { onWallpaperClick(wallpaper.id) }
                    )
                }
            }
        }
    }
}
