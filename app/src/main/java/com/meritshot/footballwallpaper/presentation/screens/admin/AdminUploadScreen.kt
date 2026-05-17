package com.meritshot.footballwallpaper.presentation.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.meritshot.footballwallpaper.presentation.components.*
import com.meritshot.footballwallpaper.presentation.theme.*
import com.meritshot.footballwallpaper.presentation.viewmodel.WallpaperViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUploadScreen(
    onBack: () -> Unit,
    viewModel: WallpaperViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var externalUrl by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf("") }
    var selectedSubcategoryId by remember { mutableStateOf("") }
    var tagsInput by remember { mutableStateOf("") }
    var categoryExpanded by remember { mutableStateOf(false) }
    var subcategoryExpanded by remember { mutableStateOf(false) }

    // Handle messages
    LaunchedEffect(state.successMessage) {
        state.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
            // Reset form on success
            externalUrl = ""
            title = ""
            selectedCategoryId = ""
            selectedSubcategoryId = ""
            tagsInput = ""
        }
    }
    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar("Error: $it")
            viewModel.clearMessages()
        }
    }

    // Load subcategories when category changes
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
        topBar = { AppTopBar(title = "Upload via URL", onBack = onBack) },
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

            // External URL input
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = externalUrl, onValueChange = { externalUrl = it },
                    label = { Text("Direct Image URL") },
                    placeholder = { Text("https://example.com/image.jpg") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true, shape = RoundedCornerShape(12.dp),
                    colors = fieldColors
                )
                
                if (externalUrl.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(externalUrl)
                                .addHeader("User-Agent", "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36")
                                .addHeader("Referer", "https://www.pinterest.com/")
                                .allowHardware(false)
                                .crossfade(true)
                                .build(),
                            contentDescription = "URL Preview",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            // Title
            OutlinedTextField(
                value = title, onValueChange = { title = it },
                label = { Text("Wallpaper Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true, shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
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
                    label = { Text("Category") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(categoryExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
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
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
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

            // Tags
            OutlinedTextField(
                value = tagsInput, onValueChange = { tagsInput = it },
                label = { Text("Tags (comma-separated)") },
                placeholder = { Text("e.g. ronaldo, real madrid, cr7", color = TextSecondary) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true, shape = RoundedCornerShape(12.dp),
                colors = fieldColors
            )

            // Action button
            if (state.isLoading) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GreenPrimary)
                }
            } else {
                GradientButton(
                    text = "✨ Add Wallpaper",
                    onClick = {
                        val tags = tagsInput.split(",")
                            .map { it.trim() }
                            .filter { it.isNotEmpty() }
                        
                        viewModel.addWallpaperWithUrl(externalUrl.trim(), title.trim(), 
                            selectedCategoryId, selectedSubcategoryId, tags)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = externalUrl.isNotBlank() && title.isNotBlank() && selectedCategoryId.isNotEmpty()
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
