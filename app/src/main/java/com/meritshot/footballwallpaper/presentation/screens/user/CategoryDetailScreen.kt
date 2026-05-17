package com.meritshot.footballwallpaper.presentation.screens.user

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.meritshot.footballwallpaper.presentation.components.*
import com.meritshot.footballwallpaper.presentation.theme.*
import com.meritshot.footballwallpaper.presentation.viewmodel.WallpaperViewModel

@Composable
fun CategoryDetailScreen(
    categoryId: String,
    categoryName: String,
    onBack: () -> Unit,
    onSubcategoryClick: (String, String) -> Unit,
    onWallpaperClick: (String) -> Unit,
    viewModel: WallpaperViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(categoryId) {
        viewModel.loadSubcategories(categoryId)
        viewModel.loadWallpapersByCategory(categoryId)
    }

    Scaffold(
        topBar = { AppTopBar(title = categoryName, onBack = onBack) },
        containerColor = BackgroundDark
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Subcategories row
            if (state.subcategories.isNotEmpty()) {
                item(span = { GridItemSpan(2) }) {
                    Text(
                        "Subcategories",
                        fontSize = 16.sp, fontWeight = FontWeight.Bold,
                        color = TextPrimary, modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                items(state.subcategories, span = { GridItemSpan(1) }) { sub ->
                    Card(
                        onClick = { onSubcategoryClick(sub.id, sub.name) },
                        colors = CardDefaults.cardColors(containerColor = SurfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(sub.name, color = TextPrimary, fontWeight = FontWeight.Medium)
                        }
                    }
                }
                item(span = { GridItemSpan(2) }) {
                    Divider(color = SurfaceVariant, modifier = Modifier.padding(vertical = 8.dp))
                }
            }

            // Wallpapers
            item(span = { GridItemSpan(2) }) {
                Text(
                    "Wallpapers (${state.wallpapers.size})",
                    fontSize = 16.sp, fontWeight = FontWeight.Bold,
                    color = TextPrimary, modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            if (state.wallpapers.isEmpty()) {
                item(span = { GridItemSpan(2) }) {
                    Box(Modifier.fillMaxWidth().height(200.dp),
                        contentAlignment = Alignment.Center) {
                        Text("No wallpapers in this category", color = TextSecondary)
                    }
                }
            } else {
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
