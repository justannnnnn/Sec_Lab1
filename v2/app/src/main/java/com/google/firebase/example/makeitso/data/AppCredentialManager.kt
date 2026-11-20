package com.google.firebase.example.makeitso.data

import android.app.Activity
import android.content.Context
import androidx.credentials.CreatePasswordRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetPasswordOption
import androidx.credentials.PasswordCredential
import androidx.credentials.exceptions.CreateCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.example.makeitso.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AppCredentialManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val credentialManager = CredentialManager.create(context)

    suspend fun savePassword(
        activity: Activity,
        email: String,
        password: String
    ): Boolean {
        return try {
            credentialManager.createCredential(
                context = activity,
                request = CreatePasswordRequest(
                    id = email,
                    password = password
                )
            )
            true
        } catch (e: CreateCredentialException) {
            false
        }
    }

    suspend fun getGoogleIdToken(activity: Activity): String {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setServerClientId(
                context.getString(R.string.default_web_client_id)
            )
            .setFilterByAuthorizedAccounts(false)
            .build()

        val request = GetCredentialRequest(
            credentialOptions = listOf(googleIdOption)
        )

        val result = credentialManager.getCredential(
            context = activity,
            request = request
        )

        val credential = GoogleIdTokenCredential.createFrom(
            result.credential.data
        )

        return credential.idToken
    }

    suspend fun getSignInCredential(
        activity: Activity
    ): SignInCredential? {
        return try {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setServerClientId(context.getString(R.string.default_web_client_id))
                .setFilterByAuthorizedAccounts(false)
                .build()

            val request = GetCredentialRequest(
                credentialOptions = listOf(
                    GetPasswordOption(),
                    googleIdOption
                )
            )

            val result = credentialManager.getCredential(
                context = activity,
                request = request
            )

            return when (val credential = result.credential) {
                is PasswordCredential -> {
                    SignInCredential.Password(
                        email = credential.id,
                        password = credential.password
                    )
                }

                is CustomCredential -> {
                    if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                        val googleCredential =
                            GoogleIdTokenCredential.createFrom(credential.data)

                        SignInCredential.Google(
                            idToken = googleCredential.idToken
                        )
                    } else {
                        throw IllegalStateException(
                            "Unsupported credential type: ${credential.type}"
                        )
                    }
                }

                else -> {
                    throw IllegalStateException(
                        "Unsupported credential type: ${credential::class.java.name}"
                    )
                }
            }
        } catch (e: NoCredentialException) {
            null
        }
    }
}

sealed interface SignInCredential {
    data class Password(
        val email: String,
        val password: String
    ) : SignInCredential

    data class Google(
        val idToken: String
    ) : SignInCredential
}