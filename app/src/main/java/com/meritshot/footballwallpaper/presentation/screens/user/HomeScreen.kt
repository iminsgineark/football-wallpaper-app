package com.meritshot.footballwallpaper.presentation.screens.user

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.meritshot.footballwallpaper.data.model.User
import com.meritshot.footballwallpaper.presentation.components.*
import com.meritshot.footballwallpaper.presentation.theme.*
import com.meritshot.footballwallpaper.presentation.viewmodel.AuthViewModel
import com.meritshot.footballwallpaper.presentation.viewmodel.WallpaperViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onCategoryClick: (String, String) -> Unit,
    onWallpaperClick: (String) -> Unit,
    onFavoritesClick: () -> Unit,
    onProfileClick: () -> Unit,
    onAdminClick: () -> Unit,
    wallpaperViewModel: WallpaperViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val wallpaperState by wallpaperViewModel.uiState.collectAsState()
    val authState by authViewModel.uiState.collectAsState()
    var currentUser by remember { mutableStateOf<User?>(null) }

    LaunchedEffect(Unit) {
        authViewModel.loadCurrentUser()
    }
    LaunchedEffect(authState.user) {
        currentUser = authState.user
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("⚽ ", fontSize = 22.sp)
                        Text("Football Wallpaper", fontWeight = FontWeight.ExtraBold, color = GreenPrimary)
                    }
                },
                actions = {
                    // Show admin button only for admins
                    if (currentUser?.isAdmin == true) {
                        IconButton(onClick = onAdminClick) {
                            Icon(Icons.Default.Settings, contentDescription = "Admin",
                                tint = AdminAccent)
                        }
                    }
                    IconButton(onClick = onFavoritesClick) {
                        Icon(Icons.Default.Favorite, contentDescription = "Favorites",
                            tint = GreenPrimary)
                    }
                    IconButton(onClick = onProfileClick) {
                        Icon(Icons.Default.Person, contentDescription = "Profile",
                            tint = TextSecondary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceDark)
            )
        },
        containerColor = BackgroundDark
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item { Spacer(Modifier.height(8.dp)) }

            // Greeting
            item {
                Column {
                    val greeting = when (java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)) {
                        in 5..11  -> "Good Morning"
                        in 12..16 -> "Good Afternoon"
                        else      -> "Good Evening"
                    }
                    Text(
                        text = "$greeting ⚽",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Pick your favourite football wallpaper",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }
            }

            // Categories section
            item {
                Text("Browse Categories", fontSize = 18.sp,
                    fontWeight = FontWeight.Bold, color = TextPrimary)
            }

            if (wallpaperState.categories.isEmpty()) {
                item {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No categories yet", color = TextSecondary)
                    }
                }
            } else {
                item {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.heightIn(max = 600.dp)
                    ) {
                        items(wallpaperState.categories) { category ->
                            CategoryCard(
                                name = category.name,
                                emoji = category.iconEmoji,
                                onClick = { onCategoryClick(category.id, category.name) }
                            )
                        }
                    }
                }
            }

            // Latest wallpapers section
            item {
                Text("Latest Wallpapers", fontSize = 18.sp,
                    fontWeight = FontWeight.Bold, color = TextPrimary)
            }

            if (wallpaperState.wallpapers.isEmpty()) {
                item {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No wallpapers yet", color = TextSecondary)
                    }
                }
            } else {
                item {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.heightIn(max = 800.dp)
                    ) {
                        items(wallpaperState.wallpapers.take(10)) { wallpaper ->
                            WallpaperCard(
                                wallpaper = wallpaper,
                                isFavorite = wallpaperViewModel.isFavorite(wallpaper.id),
                                onClick = { onWallpaperClick(wallpaper.id) }
                            )
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}
