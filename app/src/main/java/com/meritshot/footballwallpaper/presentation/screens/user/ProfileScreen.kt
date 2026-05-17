package com.meritshot.footballwallpaper.presentation.screens.user

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.meritshot.footballwallpaper.presentation.components.AppTopBar
import com.meritshot.footballwallpaper.presentation.theme.*
import com.meritshot.footballwallpaper.presentation.viewmodel.AuthViewModel
import com.meritshot.footballwallpaper.presentation.viewmodel.WallpaperViewModel

@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel(),
    wallpaperViewModel: WallpaperViewModel = hiltViewModel()
) {
    val authState by authViewModel.uiState.collectAsState()
    val wallpaperState by wallpaperViewModel.uiState.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { authViewModel.loadCurrentUser() }

    val user = authState.user
    val favCount = wallpaperState.favorites.size

    Scaffold(
        topBar = { AppTopBar(title = "Profile", onBack = onBack) },
        containerColor = BackgroundDark
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(20.dp))

            // Avatar circle
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(GreenDark),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = user?.email?.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                    fontSize = 40.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(16.dp))
            Text(user?.email ?: "Loading...", color = TextPrimary, fontSize = 18.sp,
                fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))

            // Role badge
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (user?.isAdmin == true) AdminAccent else GreenDark
            ) {
                Text(
                    text = if (user?.isAdmin == true) "⚙ ADMIN" else "👤 USER",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    color = if (user?.isAdmin == true) Color.Black else Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(32.dp))

            // Stats card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceVariant),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem("Favorites", favCount.toString())
                    Divider(
                        modifier = Modifier
                            .height(40.dp)
                            .width(1.dp),
                        color = SurfaceVariant
                    )
                    StatItem("Wallpapers", wallpaperState.wallpapers.size.toString())
                }
            }

            Spacer(Modifier.height(32.dp))

            // Logout button
            Button(
                onClick = { showLogoutDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ErrorColor),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Sign Out", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            containerColor = SurfaceDark,
            title = { Text("Sign Out", color = TextPrimary) },
            text = { Text("Are you sure you want to sign out?", color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    authViewModel.signOut()
                    onLogout()
                }) { Text("Sign Out", color = ErrorColor) }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = GreenPrimary)
        Text(label, fontSize = 13.sp, color = TextSecondary)
    }
}
