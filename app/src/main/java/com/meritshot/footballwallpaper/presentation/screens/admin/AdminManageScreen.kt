package com.meritshot.footballwallpaper.presentation.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.meritshot.footballwallpaper.data.model.Wallpaper
import com.meritshot.footballwallpaper.presentation.components.*
import com.meritshot.footballwallpaper.presentation.theme.*
import com.meritshot.footballwallpaper.presentation.viewmodel.WallpaperViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminManageScreen(
    onBack: () -> Unit,
    viewModel: WallpaperViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var deleteTarget by remember { mutableStateOf<Wallpaper?>(null) }
    var editTarget by remember { mutableStateOf<Wallpaper?>(null) }
    var editTitle by remember { mutableStateOf("") }
    var editTags by remember { mutableStateOf("") }

    LaunchedEffect(state.successMessage) {
        state.successMessage?.let { snackbarHostState.showSnackbar(it); viewModel.clearMessages() }
    }
    LaunchedEffect(state.error) {
        state.error?.let { snackbarHostState.showSnackbar("Error: $it"); viewModel.clearMessages() }
    }

    Scaffold(
        topBar = { AppTopBar(title = "Manage Wallpapers", onBack = onBack) },
        snackbarHost = { FwSnackbarHost(snackbarHostState) },
        containerColor = BackgroundDark
    ) { padding ->
        if (state.wallpapers.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No wallpapers uploaded yet", color = TextSecondary)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize().padding(padding)
            ) {
                items(state.wallpapers) { wallpaper ->
                    WallpaperManageCard(
                        wallpaper = wallpaper,
                        onEdit = {
                            editTarget = wallpaper
                            editTitle = wallpaper.title
                            editTags = wallpaper.tags.joinToString(", ")
                        },
                        onDelete = { deleteTarget = wallpaper }
                    )
                }
            }
        }
    }

    // Delete confirmation dialog
    deleteTarget?.let { wallpaper ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            containerColor = SurfaceDark,
            title = { Text("Delete Wallpaper?", color = TextPrimary) },
            text = { Text("\"${wallpaper.title}\" will be permanently deleted.", color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteWallpaper(wallpaper)
                    deleteTarget = null
                }) { Text("Delete", color = ErrorColor, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) { Text("Cancel", color = TextSecondary) }
            }
        )
    }

    // Edit dialog
    editTarget?.let { wallpaper ->
        AlertDialog(
            onDismissRequest = { editTarget = null },
            containerColor = SurfaceDark,
            title = { Text("Edit Wallpaper", color = TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = editTitle, onValueChange = { editTitle = it },
                        label = { Text("Title") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true, shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GreenPrimary, unfocusedBorderColor = SurfaceVariant,
                            focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
                            cursorColor = GreenPrimary, focusedLabelColor = GreenPrimary,
                            unfocusedLabelColor = TextSecondary, focusedContainerColor = SurfaceDark,
                            unfocusedContainerColor = SurfaceDark
                        )
                    )
                    OutlinedTextField(
                        value = editTags, onValueChange = { editTags = it },
                        label = { Text("Tags (comma-separated)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true, shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GreenPrimary, unfocusedBorderColor = SurfaceVariant,
                            focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
                            cursorColor = GreenPrimary, focusedLabelColor = GreenPrimary,
                            unfocusedLabelColor = TextSecondary, focusedContainerColor = SurfaceDark,
                            unfocusedContainerColor = SurfaceDark
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val tags = editTags.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    viewModel.updateWallpaper(wallpaper.id, editTitle, tags)
                    editTarget = null
                }) { Text("Save", color = GreenPrimary, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { editTarget = null }) { Text("Cancel", color = TextSecondary) }
            }
        )
    }
}

@Composable
private fun WallpaperManageCard(
    wallpaper: Wallpaper,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail
            AsyncImage(
                model = wallpaper.imageUrl,
                contentDescription = wallpaper.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(wallpaper.title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                if (wallpaper.tags.isNotEmpty()) {
                    Text(wallpaper.tags.joinToString(", "), color = TextSecondary, fontSize = 11.sp,
                        maxLines = 1)
                }
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, "Edit", tint = AdminAccent, modifier = Modifier.size(20.dp))
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, "Delete", tint = ErrorColor, modifier = Modifier.size(20.dp))
            }
        }
    }
}
