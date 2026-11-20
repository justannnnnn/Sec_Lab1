package com.google.firebase.example.makeitso.ui

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.firebase.example.makeitso.data.repository.AuthRepository
import kotlinx.serialization.Serializable

@Serializable
data object AuthGateRoute

@Composable
fun AuthGate(
    authRepository: AuthRepository,
    openTodo: () -> Unit,
    openSignIn: () -> Unit
) {
    val isAuthenticated by authRepository.isAuthenticatedFlow
        .collectAsStateWithLifecycle(initialValue = null)

    when (isAuthenticated) {
        null -> {
            CircularProgressIndicator()
        }

        true -> {
            LaunchedEffect(Unit) {
                openTodo()
            }
        }

        false -> {
            LaunchedEffect(Unit) {
                openSignIn()
            }
        }
    }
}