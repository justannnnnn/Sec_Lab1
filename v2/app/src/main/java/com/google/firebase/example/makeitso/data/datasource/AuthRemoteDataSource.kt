package com.google.firebase.example.makeitso.data.datasource

import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRemoteDataSource @Inject constructor(private val auth: FirebaseAuth) {
    val currentUser: FirebaseUser?
        get() = auth.currentUser

    val currentUserIdFlow: Flow<String?>
        get() = callbackFlow {
            val listener = FirebaseAuth.AuthStateListener { _ -> this.trySend(currentUser?.uid) }
            auth.addAuthStateListener(listener)
            awaitClose { auth.removeAuthStateListener(listener) }
        }

    suspend fun createGuestAccount() {
        auth.signInAnonymously().await()
    }

    suspend fun signUp(email: String, password: String) {
        val user = auth.currentUser

        if (user?.isAnonymous == true) {
            val credential = EmailAuthProvider.getCredential(email, password)
            user.linkWithCredential(credential).await()
        } else {
            auth.createUserWithEmailAndPassword(email, password).await()
        }
    }

    suspend fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).await()
    }

    suspend fun signInWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        auth.signInWithCredential(credential).await()
    }

    suspend fun linkGoogleToGuest(idToken: String) {
        val user = auth.currentUser

        if (user?.isAnonymous != true) {
            throw IllegalStateException("Для привязки Google нужен гостевой аккаунт")
        }

        val credential = GoogleAuthProvider.getCredential(idToken, null)
        user.linkWithCredential(credential).await()
    }

    suspend fun linkAccount(email: String, password: String) {
        val user = auth.currentUser
            ?: error("Нет авторизованного пользователя для привязки аккаунта")

        val credential = EmailAuthProvider.getCredential(email, password)
        user.linkWithCredential(credential).await()
    }

    suspend fun signOut() {
        val user = auth.currentUser
        if (user?.isAnonymous == true) {
            user.delete().await()
        }
        auth.signOut()
    }

    suspend fun deleteAccount() {
        val user = auth.currentUser
            ?: error("Пользователь не авторизован")

        user.delete().await()
    }
}