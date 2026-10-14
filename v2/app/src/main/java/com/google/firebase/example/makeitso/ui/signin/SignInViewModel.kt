package com.google.firebase.example.makeitso.ui.signin

import android.app.Activity
import com.google.firebase.example.makeitso.MainViewModel
import com.google.firebase.example.makeitso.data.AppCredentialManager
import com.google.firebase.example.makeitso.data.model.ErrorMessage
import com.google.firebase.example.makeitso.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val appCredentialManager: AppCredentialManager
) : MainViewModel() {
    private val _shouldRestartApp = MutableStateFlow(false)
    val shouldRestartApp: StateFlow<Boolean>
        get() = _shouldRestartApp.asStateFlow()

    fun signIn(
        activity: Activity,
        email: String,
        password: String,
        showErrorSnackbar: (ErrorMessage) -> Unit
    ) {
        launchCatching(showErrorSnackbar) {
            authRepository.signIn(email.trim(), password)

            appCredentialManager.savePassword(
                activity = activity,
                email = email.trim(),
                password = password
            )

            _shouldRestartApp.value = true
        }
    }

    fun signInWithGoogle(
        activity: Activity,
        showErrorSnackbar: (ErrorMessage) -> Unit
    ) {
        launchCatching(showErrorSnackbar) {
            val idToken = appCredentialManager.getGoogleIdToken(activity)
            authRepository.signInWithGoogle(idToken)
            _shouldRestartApp.value = true
        }
    }

    fun checkSavedPassword(
        activity: Activity,
        showErrorSnackbar: (ErrorMessage) -> Unit
    ) {
        launchCatching(showErrorSnackbar) {
            val credential =
                appCredentialManager.getSavedPassword(activity)
                    ?: return@launchCatching

            authRepository.signIn(
                email = credential.id,
                password = credential.password
            )

            _shouldRestartApp.value = true
        }
    }
}