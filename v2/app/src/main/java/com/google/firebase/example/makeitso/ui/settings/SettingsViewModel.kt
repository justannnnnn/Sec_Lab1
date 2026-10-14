package com.google.firebase.example.makeitso.ui.settings

import com.google.firebase.example.makeitso.MainViewModel
import com.google.firebase.example.makeitso.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class UserProfileUiState(
    val email: String?,
    val displayName: String?,
    val authMethod: String,
    val uid: String
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : MainViewModel() {
    private val _shouldRestartApp = MutableStateFlow(false)
    val shouldRestartApp: StateFlow<Boolean>
        get() = _shouldRestartApp.asStateFlow()

    private val _profile = MutableStateFlow<UserProfileUiState?>(null)
    val profile: StateFlow<UserProfileUiState?>
        get() = _profile.asStateFlow()

    private val _isAnonymous = MutableStateFlow(true)
    val isAnonymous: StateFlow<Boolean>
        get() = _isAnonymous.asStateFlow()

    fun loadCurrentUser() {
        launchCatching {
            val currentUser = authRepository.currentUser

            _isAnonymous.value = currentUser?.isAnonymous == true

            _profile.value = currentUser?.let { user ->
                val providers = user.providerData
                    .map { it.providerId }
                    .filter { it != "firebase" }
                    .distinct()

                val authMethod = when {
                    user.isAnonymous -> "Гостевой режим"

                    providers.isEmpty() -> "Неизвестный способ входа"

                    else -> providers.mapNotNull { provider ->
                        when (provider) {
                            "password" -> "Email/Password"
                            "google.com" -> "Google"
                            else -> null
                        }
                    }.distinct().joinToString(", ")
                        .ifBlank { "Другой способ входа" }
                }

                UserProfileUiState(
                    email = user.email,
                    displayName = user.displayName,
                    authMethod = authMethod,
                    uid = user.uid
                )
            }
        }
    }

    fun signOut() {
        launchCatching {
            authRepository.signOut()
            _shouldRestartApp.value = true
        }
    }

    fun deleteAccount() {
        launchCatching {
            authRepository.deleteAccount()
            _shouldRestartApp.value = true
        }
    }
}