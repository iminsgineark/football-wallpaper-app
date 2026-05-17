package com.meritshot.footballwallpaper.presentation.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.meritshot.footballwallpaper.presentation.components.AppTopBar
import com.meritshot.footballwallpaper.presentation.theme.*
import com.meritshot.footballwallpaper.presentation.viewmodel.AuthViewModel
import com.meritshot.footballwallpaper.presentation.viewmodel.WallpaperViewModel

@Composable
fun AdminDashboardScreen(
    onUpload: () -> Unit,
    onBulkUpload: () -> Unit,
    onManage: () -> Unit,
    onCategories: () -> Unit,
    onBack: () -> Unit,
    onLogout: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel(),
    wallpaperViewModel: WallpaperViewModel = hiltViewModel()
) {
    val wallpaperState by wallpaperViewModel.uiState.collectAsState()
    val authState by authViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { authViewModel.loadCurrentUser() }

    val adminMenuItems = listOf(
        AdminMenuItem("📤", "Single Upload", "Add one wallpaper", GreenPrimary, onUpload),
        AdminMenuItem("🚀", "Bulk Upload", "Add 100+ wallpapers", GreenPrimary, onBulkUpload),
        AdminMenuItem("🗂️", "Manage Wallpapers", "Edit or delete", AdminAccent, onManage),
        AdminMenuItem("📂", "Categories", "Manage folders", GreenLight, onCategories),
    )

    Scaffold(
        topBar = {
            AppTopBar(
                title = "⚙ Admin Panel",
                onBack = onBack,
                actions = {
                    TextButton(onClick = onLogout) {
                        Text("Logout", color = ErrorColor, fontSize = 13.sp)
                    }
                }
            )
        },
        containerColor = BackgroundDark
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            // Admin welcome
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceVariant),
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(listOf(GreenDark.copy(0.3f), AdminAccent.copy(0.1f)))
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Text("Welcome, Admin", fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold, color = AdminAccent)
                        Text(authState.user?.email ?: "", fontSize = 13.sp, color = TextSecondary)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Stats row
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard("Wallpapers", wallpaperState.wallpapers.size.toString(),
                    Modifier.weight(1f))
                StatCard("Categories", wallpaperState.categories.size.toString(),
                    Modifier.weight(1f))
            }

            Spacer(Modifier.height(24.dp))
            Text("Admin Actions", fontSize = 16.sp,
                fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(Modifier.height(12.dp))

            // Action grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(adminMenuItems) { item ->
                    AdminMenuCard(item)
                }
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = SurfaceVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = GreenPrimary)
            Text(label, fontSize = 12.sp, color = TextSecondary)
        }
    }
}

data class AdminMenuItem(
    val emoji: String,
    val title: String,
    val subtitle: String,
    val color: androidx.compose.ui.graphics.Color,
    val onClick: () -> Unit
)

@Composable
private fun AdminMenuCard(item: AdminMenuItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = item.onClick),
        colors = CardDefaults.cardColors(containerColor = SurfaceVariant),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(item.emoji, fontSize = 34.sp)
            Spacer(Modifier.height(10.dp))
            Text(item.title, color = item.color, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(item.subtitle, color = TextSecondary, fontSize = 11.sp)
        }
    }
}
