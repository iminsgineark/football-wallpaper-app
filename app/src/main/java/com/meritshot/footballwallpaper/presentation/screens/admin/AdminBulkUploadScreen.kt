package com.meritshot.footballwallpaper.presentation.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminBulkUploadScreen(
    onBack: () -> Unit,
    viewModel: WallpaperViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var bulkInput by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf("") }
    var selectedSubcategoryId by remember { mutableStateOf("") }
    var categoryExpanded by remember { mutableStateOf(false) }
    var subcategoryExpanded by remember { mutableStateOf(false) }

    // Handle messages
    LaunchedEffect(state.successMessage) {
        state.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
            bulkInput = ""
        }
    }
    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar("Error: $it")
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(selectedCategoryId) {
        if (selectedCategoryId.isNotEmpty()) viewModel.loadSubcategories(selectedCategoryId)
    }

    val selectedCategory = state.categories.find { it.id == selectedCategoryId }
    val selectedSubcategory = state.subcategories.find { it.id == selectedSubcategoryId }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = GreenPrimary, unfocusedBorderColor = SurfaceVariant,
        focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
        cursorColor = GreenPrimary, focusedLabelColor = GreenPrimary,
        unfocusedLabelColor = TextSecondary, focusedContainerColor = SurfaceDark,
        unfocusedContainerColor = SurfaceDark
    )

    Scaffold(
        topBar = { AppTopBar(title = "Bulk Upload", onBack = onBack) },
        snackbarHost = { FwSnackbarHost(snackbarHostState) },
        containerColor = BackgroundDark
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            Text(
                "Paste your titles and URLs below.\nFormat: Title, URL (one per line)",
                color = TextSecondary, fontSize = 14.sp
            )

            OutlinedTextField(
                value = bulkInput,
                onValueChange = { bulkInput = it },
                label = { Text("Bulk Input") },
                placeholder = { Text("Messi Miami, https://url1.jpg\nCR7 Al Nassr, https://url2.jpg") },
                modifier = Modifier.fillMaxWidth().height(250.dp),
                shape = RoundedCornerShape(12.dp),
                colors = fieldColors
            )

            // Category dropdown
            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedCategory?.let { "${it.iconEmoji} ${it.name}" } ?: "Select Category",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Target Category") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(categoryExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = fieldColors
                )
                ExposedDropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false }
                ) {
                    state.categories.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text("${cat.iconEmoji} ${cat.name}", color = TextPrimary) },
                            onClick = {
                                selectedCategoryId = cat.id
                                selectedSubcategoryId = ""
                                categoryExpanded = false
                            }
                        )
                    }
                }
            }

            // Subcategory dropdown
            if (selectedCategoryId.isNotEmpty() && state.subcategories.isNotEmpty()) {
                ExposedDropdownMenuBox(
                    expanded = subcategoryExpanded,
                    onExpandedChange = { subcategoryExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedSubcategory?.name ?: "Select Subcategory (Optional)",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Subcategory") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(subcategoryExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = fieldColors
                    )
                    ExposedDropdownMenu(
                        expanded = subcategoryExpanded,
                        onDismissRequest = { subcategoryExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("None", color = TextSecondary) },
                            onClick = { selectedSubcategoryId = ""; subcategoryExpanded = false }
                        )
                        state.subcategories.forEach { sub ->
                            DropdownMenuItem(
                                text = { Text(sub.name, color = TextPrimary) },
                                onClick = { selectedSubcategoryId = sub.id; subcategoryExpanded = false }
                            )
                        }
                    }
                }
            }

            if (state.isLoading) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GreenPrimary)
                }
            } else {
                GradientButton(
                    text = "🚀 Start Bulk Upload",
                    onClick = {
                        viewModel.bulkAddWallpapers(bulkInput, selectedCategoryId, selectedSubcategoryId)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = bulkInput.isNotBlank() && selectedCategoryId.isNotEmpty()
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}
