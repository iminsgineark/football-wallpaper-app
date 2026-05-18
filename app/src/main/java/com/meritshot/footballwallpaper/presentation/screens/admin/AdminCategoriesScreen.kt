package com.meritshot.footballwallpaper.presentation.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.meritshot.footballwallpaper.data.model.Category
import com.meritshot.footballwallpaper.presentation.components.*
import com.meritshot.footballwallpaper.presentation.theme.*
import com.meritshot.footballwallpaper.presentation.viewmodel.WallpaperViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminCategoriesScreen(
    onBack: () -> Unit,
    viewModel: WallpaperViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var showAddSubcategoryDialog by remember { mutableStateOf(false) }
    var selectedCategoryForSub by remember { mutableStateOf<Category?>(null) }

    LaunchedEffect(state.successMessage) {
        state.successMessage?.let { snackbarHostState.showSnackbar(it); viewModel.clearMessages() }
    }
    LaunchedEffect(state.error) {
        state.error?.let { snackbarHostState.showSnackbar("Error: $it"); viewModel.clearMessages() }
    }

    Scaffold(
        topBar = { AppTopBar(title = "Manage Categories", onBack = onBack) },
        snackbarHost = { FwSnackbarHost(snackbarHostState) },
        containerColor = BackgroundDark,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddCategoryDialog = true },
                containerColor = GreenPrimary,
                contentColor = androidx.compose.ui.graphics.Color.Black
            ) {
                Text("+", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
        }
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            item {
                Text("Categories (${state.categories.size})",
                    color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(Modifier.height(8.dp))
            }
            items(state.categories) { category ->
                CategoryManageCard(
                    category = category,
                    onAddSubcategory = {
                        selectedCategoryForSub = category
                        viewModel.loadSubcategories(category.id)
                        showAddSubcategoryDialog = true
                    },
                    onDelete = {
                        viewModel.deleteCategory(category.id)
                    }
                )
            }

            // Default football categories hint
            if (state.categories.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceVariant),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text("No categories yet", color = TextSecondary)
                            Spacer(Modifier.height(8.dp))
                            Text("Suggested categories:", color = TextSecondary, fontSize = 12.sp)
                            val suggestions = listOf("🏟️ Clubs", "⭐ Players", "🏟️ Stadiums", "🏆 UCL")
                            suggestions.forEach {
                                Text("  • $it", color = TextSecondary, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Category dialog
    if (showAddCategoryDialog) {
        var newCategoryName by remember { mutableStateOf("") }
        var newEmoji by remember { mutableStateOf("⚽") }

        AlertDialog(
            onDismissRequest = { showAddCategoryDialog = false },
            containerColor = SurfaceDark,
            title = { Text("Add Category", color = TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = newEmoji, onValueChange = { newEmoji = it },
                        label = { Text("Emoji Icon") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true, shape = RoundedCornerShape(8.dp),
                        colors = dialogFieldColors()
                    )
                    OutlinedTextField(
                        value = newCategoryName, onValueChange = { newCategoryName = it },
                        label = { Text("Category Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true, shape = RoundedCornerShape(8.dp),
                        colors = dialogFieldColors()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newCategoryName.isNotBlank()) {
                        viewModel.addCategory(newCategoryName.trim(), newEmoji.trim())
                        showAddCategoryDialog = false
                    }
                }) { Text("Add", color = GreenPrimary, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showAddCategoryDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    // Add Subcategory dialog
    if (showAddSubcategoryDialog && selectedCategoryForSub != null) {
        var newSubName by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddSubcategoryDialog = false },
            containerColor = SurfaceDark,
            title = { Text("Add Subcategory to ${selectedCategoryForSub!!.name}", color = TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Show existing subcategories
                    if (state.subcategories.isNotEmpty()) {
                        Text("Existing:", color = TextSecondary, fontSize = 12.sp)
                        state.subcategories.forEach {
                            Text("  • ${it.name}", color = TextSecondary, fontSize = 12.sp)
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                    OutlinedTextField(
                        value = newSubName, onValueChange = { newSubName = it },
                        label = { Text("Subcategory Name") },
                        placeholder = { Text("e.g. Real Madrid", color = TextSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true, shape = RoundedCornerShape(8.dp),
                        colors = dialogFieldColors()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newSubName.isNotBlank()) {
                        viewModel.addSubcategory(selectedCategoryForSub!!.id, newSubName.trim())
                        showAddSubcategoryDialog = false
                    }
                }) { Text("Add", color = GreenPrimary, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showAddSubcategoryDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
private fun CategoryManageCard(
    category: Category, 
    onAddSubcategory: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(category.iconEmoji, fontSize = 28.sp)
            Spacer(Modifier.width(12.dp))
            Text(category.name, color = TextPrimary, fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f))
            
            IconButton(onClick = onDelete) {
                Text("🗑️", fontSize = 18.sp)
            }

            OutlinedButton(
                onClick = onAddSubcategory,
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    width = 1.dp
                ),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = GreenPrimary)
            ) { Text("+ Sub", fontSize = 12.sp) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun dialogFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = GreenPrimary, unfocusedBorderColor = SurfaceVariant,
    focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
    cursorColor = GreenPrimary, focusedLabelColor = GreenPrimary,
    unfocusedLabelColor = TextSecondary, focusedContainerColor = SurfaceDark,
    unfocusedContainerColor = SurfaceDark
)
