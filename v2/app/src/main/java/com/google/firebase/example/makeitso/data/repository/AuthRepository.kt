package com.google.firebase.example.makeitso.data.repository

import com.google.firebase.auth.FirebaseUser
import com.google.firebase.example.makeitso.data.datasource.AuthRemoteDataSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val authRemoteDataSource: AuthRemoteDataSource
) {
    val currentUser: FirebaseUser?
        get() = authRemoteDataSource.currentUser
    val currentUserIdFlow: Flow<String?>
        get() = authRemoteDataSource.currentUserIdFlow

    suspend fun createGuestAccount() {
        authRemoteDataSource.createGuestAccount()
    }

    suspend fun signIn(email: String, password: String) {
        authRemoteDataSource.signIn(email, password)
    }

    suspend fun signInWithGoogle(idToken: String) {
        authRemoteDataSource.signInWithGoogle(idToken)
    }

    suspend fun linkGoogleToGuest(idToken: String) {
        authRemoteDataSource.linkGoogleToGuest(idToken)
    }

    suspend fun signUp(email: String, password: String) {
       authRemoteDataSource.signUp(email, password)
    }

    suspend fun signOut() {
        authRemoteDataSource.signOut()
    }

    suspend fun deleteAccount() {
        authRemoteDataSource.deleteAccount()
    }
}