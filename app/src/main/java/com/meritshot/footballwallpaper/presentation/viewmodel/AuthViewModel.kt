package com.meritshot.footballwallpaper.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meritshot.footballwallpaper.data.model.Result
import com.meritshot.footballwallpaper.data.model.User
import com.meritshot.footballwallpaper.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val error: String? = null,
    val success: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepo: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    val authState = authRepo.authStateFlow
    val isLoggedIn get() = authRepo.isLoggedIn

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = authRepo.signIn(email, password)) {
                is Result.Success -> _uiState.update {
                    it.copy(isLoading = false, user = result.data, success = true)
                }
                is Result.Error   -> _uiState.update {
                    it.copy(isLoading = false, error = result.exception.message)
                }
                Result.Loading    -> {}
            }
        }
    }

    fun register(email: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = authRepo.register(email, password)) {
                is Result.Success -> _uiState.update {
                    it.copy(isLoading = false, user = result.data, success = true)
                }
                is Result.Error   -> _uiState.update {
                    it.copy(isLoading = false, error = result.exception.message)
                }
                Result.Loading    -> {}
            }
        }
    }

    fun loadCurrentUser() {
        viewModelScope.launch {
            when (val result = authRepo.getCurrentUserProfile()) {
                is Result.Success -> _uiState.update { it.copy(user = result.data) }
                is Result.Error   -> {}
                Result.Loading    -> {}
            }
        }
    }

    fun signOut() {
        authRepo.signOut()
        _uiState.update { AuthUiState() }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
}
