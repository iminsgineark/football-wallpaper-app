package com.meritshot.footballwallpaper.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meritshot.footballwallpaper.data.model.*
import com.meritshot.footballwallpaper.data.repository.WallpaperRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// ─────────────────────────────────────────────────────────────
// UI State
// ─────────────────────────────────────────────────────────────

data class WallpaperUiState(
    val isLoading: Boolean = false,
    val wallpapers: List<Wallpaper> = emptyList(),
    val categories: List<Category> = emptyList(),
    val subcategories: List<Subcategory> = emptyList(),
    val favorites: List<Favorite> = emptyList(),
    val selectedWallpaper: Wallpaper? = null,
    val uploadProgress: Float = 0f,
    val error: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class WallpaperViewModel @Inject constructor(
    private val repo: WallpaperRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WallpaperUiState())
    val uiState: StateFlow<WallpaperUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
        loadWallpapers()
        loadFavorites()
    }

    // ── Load ──────────────────────────────────────────────────

    fun loadCategories() {
        viewModelScope.launch {
            repo.getCategoriesFlow().collect { cats ->
                _uiState.update { it.copy(categories = cats) }
            }
        }
    }

    fun loadWallpapers() {
        viewModelScope.launch {
            repo.getWallpapersFlow().collect { walls ->
                _uiState.update { it.copy(wallpapers = walls) }
            }
        }
    }

    fun loadWallpapersByCategory(categoryId: String) {
        viewModelScope.launch {
            repo.getWallpapersByCategoryFlow(categoryId).collect { walls ->
                _uiState.update { it.copy(wallpapers = walls) }
            }
        }
    }

    fun loadWallpapersBySubcategory(subcategoryId: String) {
        viewModelScope.launch {
            repo.getWallpapersBySubcategoryFlow(subcategoryId).collect { walls ->
                _uiState.update { it.copy(wallpapers = walls) }
            }
        }
    }

    fun loadSubcategories(categoryId: String) {
        viewModelScope.launch {
            repo.getSubcategoriesFlow(categoryId).collect { subs ->
                _uiState.update { it.copy(subcategories = subs) }
            }
        }
    }

    fun loadFavorites() {
        viewModelScope.launch {
            repo.getFavoritesFlow().collect { favs ->
                _uiState.update { it.copy(favorites = favs) }
            }
        }
    }

    fun loadWallpaperById(wallpaperId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = repo.getWallpaperById(wallpaperId)) {
                is Result.Success -> _uiState.update {
                    it.copy(isLoading = false, selectedWallpaper = result.data)
                }
                is Result.Error   -> _uiState.update {
                    it.copy(isLoading = false, error = result.exception.message)
                }
                Result.Loading    -> {}
            }
        }
    }

    // ── Favorites ─────────────────────────────────────────────

    fun isFavorite(wallpaperId: String): Boolean =
        _uiState.value.favorites.any { it.wallpaperId == wallpaperId }

    fun toggleFavorite(wallpaperId: String) {
        viewModelScope.launch {
            val fav = _uiState.value.favorites.find { it.wallpaperId == wallpaperId }
            if (fav != null) {
                repo.removeFavorite(fav.id)
            } else {
                repo.addFavorite(wallpaperId)
            }
        }
    }

    // ── Admin: Upload ─────────────────────────────────────────

    fun addWallpaperWithUrl(
        imageUrl: String,
        title: String,
        categoryId: String,
        subcategoryId: String,
        tags: List<String>
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = repo.addWallpaperWithUrl(imageUrl, title, categoryId, subcategoryId, tags)) {
                is Result.Success -> _uiState.update {
                    it.copy(isLoading = false, successMessage = "Wallpaper added successfully!")
                }
                is Result.Error   -> _uiState.update {
                    it.copy(isLoading = false, error = result.exception.message)
                }
                Result.Loading    -> {}
            }
        }
    }

    fun bulkAddWallpapers(
        input: String,
        categoryId: String,
        subcategoryId: String = ""
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            // Parse input: "Title, URL" or just "URL" per line
            val wallpapers = input.lines()
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .map { line ->
                    val parts = line.split(",", limit = 2)
                    if (parts.size == 2) {
                        WallpaperRepository.WallpaperData(
                            title = parts[0].trim(),
                            imageUrl = parts[1].trim(),
                            tags = emptyList()
                        )
                    } else {
                        WallpaperRepository.WallpaperData(
                            title = "Wallpaper ${System.currentTimeMillis() % 1000}",
                            imageUrl = line.trim(),
                            tags = emptyList()
                        )
                    }
                }

            if (wallpapers.isEmpty()) {
                _uiState.update { it.copy(isLoading = false, error = "No valid input found") }
                return@launch
            }

            // Firebase batches are limited to 500 operations
            val chunks = wallpapers.chunked(450)
            var totalAdded = 0
            
            chunks.forEach { chunk ->
                when (val result = repo.bulkAddWallpapers(chunk, categoryId, subcategoryId)) {
                    is Result.Success -> totalAdded += result.data
                    is Result.Error -> {
                        _uiState.update { it.copy(isLoading = false, error = result.exception.message) }
                        return@launch
                    }
                    else -> {}
                }
            }

            _uiState.update { 
                it.copy(isLoading = false, successMessage = "Successfully added $totalAdded wallpapers!")
            }
        }
    }

    // ── Admin: Edit / Delete ──────────────────────────────────

    fun updateWallpaper(wallpaperId: String, title: String, tags: List<String>) {
        viewModelScope.launch {
            when (val result = repo.updateWallpaper(wallpaperId, title, tags)) {
                is Result.Success -> _uiState.update {
                    it.copy(successMessage = "Wallpaper updated!")
                }
                is Result.Error   -> _uiState.update {
                    it.copy(error = result.exception.message)
                }
                Result.Loading    -> {}
            }
        }
    }

    fun deleteWallpaper(wallpaper: Wallpaper) {
        viewModelScope.launch {
            when (val result = repo.deleteWallpaper(wallpaper)) {
                is Result.Success -> _uiState.update {
                    it.copy(successMessage = "Wallpaper deleted!")
                }
                is Result.Error   -> _uiState.update {
                    it.copy(error = result.exception.message)
                }
                Result.Loading    -> {}
            }
        }
    }

    // ── Admin: Categories ─────────────────────────────────────

    fun addCategory(name: String, iconEmoji: String) {
        viewModelScope.launch {
            when (val result = repo.addCategory(name, iconEmoji)) {
                is Result.Success -> _uiState.update { it.copy(successMessage = "Category added!") }
                is Result.Error   -> _uiState.update { it.copy(error = result.exception.message) }
                Result.Loading    -> {}
            }
        }
    }

    fun addSubcategory(categoryId: String, name: String) {
        viewModelScope.launch {
            when (val result = repo.addSubcategory(categoryId, name)) {
                is Result.Success -> _uiState.update { it.copy(successMessage = "Subcategory added!") }
                is Result.Error   -> _uiState.update { it.copy(error = result.exception.message) }
                Result.Loading    -> {}
            }
        }
    }

    fun clearMessages() = _uiState.update { it.copy(error = null, successMessage = null) }
}
