package eus.arreseainhize.bookjounal.utils

import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.fragment.app.Fragment
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import eus.arreseainhize.bookjounal.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class GoogleSignInHelper(
    private val fragment: Fragment,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    private val tag = "GoogleSignInHelper"
    private lateinit var credentialManager: CredentialManager

    interface GoogleAuthCallback {
        fun onSuccess(user: FirebaseUser)
        fun onError(errorMessage: String)
        fun onFirestoreUserCreated(userId: String)
        fun onNoAccountAvailable()
    }

    fun initialize() {
        credentialManager = CredentialManager.create(fragment.requireContext())
    }

    fun signIn(callback: GoogleAuthCallback) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val idToken = tryGetGoogleIdToken()

                if (idToken != null) {
                    firebaseAuthWithGoogle(idToken, callback)
                } else {
                    callback.onNoAccountAvailable()
                }

            } catch (e: NoCredentialException) {
                Log.e(tag, "No credentials available: ${e.message}")
                callback.onNoAccountAvailable()

            } catch (e: GetCredentialException) {
                Log.e(tag, "Error getting credential: ${e.message}")
                when {
                    e.message?.contains("cancelled") == true ->
                        callback.onError(fragment.getString(R.string.error_google_cancelled))
                    e.message?.contains("12") == true ->
                        callback.onError(fragment.getString(R.string.error_google_generic))
                    else ->
                        callback.onError(fragment.getString(R.string.error_google_generic))
                }

            } catch (e: Exception) {
                Log.e(tag, "Error en sign-in: ${e.message}")
                callback.onError(e.message ?: fragment.getString(R.string.error_unknown))
            }
        }
    }

    private suspend fun tryGetGoogleIdToken(): String? {
        return try {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setServerClientId(fragment.getString(R.string.default_web_client_id))
                .setFilterByAuthorizedAccounts(false)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(
                request = request,
                context = fragment.requireContext()
            )

            extractIdToken(result)

        } catch (e: NoCredentialException) {
            Log.d(tag, "No credentials available in device")
            null
        } catch (e: Exception) {
            Log.e(tag, "Error getting token: ${e.message}")
            null
        }
    }

    private fun extractIdToken(response: GetCredentialResponse): String {
        val credential = response.credential

        if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            val googleIdTokenCredential = GoogleIdTokenCredential
                .createFrom(credential.data)
            return googleIdTokenCredential.idToken
        } else {
            throw Exception(fragment.getString(R.string.error_google_generic))
        }
    }

    private suspend fun firebaseAuthWithGoogle(idToken: String, callback: GoogleAuthCallback) {
        try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(credential).await()
            val user = authResult.user

            if (user != null) {
                callback.onSuccess(user)
                createFirestoreUserDocument(user, callback)
            } else {
                callback.onError(fragment.getString(R.string.error_user_not_found))
            }
        } catch (e: Exception) {
            Log.e(tag, "Error en Firebase Auth: ${e.message}")
            callback.onError(fragment.getString(R.string.error_auth_fall))
        }
    }

    private fun createFirestoreUserDocument(user: FirebaseUser, callback: GoogleAuthCallback) {
        val offlineHelper = FirestoreOfflineHelper()
        offlineHelper.createUserDocumentAlways(user, object : FirestoreOfflineHelper.SimpleCallback {
            override fun onSuccess(userId: String) {
                Log.d(tag, "${fragment.getString(R.string.data_saved)}: $userId")
                callback.onFirestoreUserCreated(userId)
            }

            override fun onError(errorMessage: String) {
                Log.w(tag, " ${fragment.getString(R.string.error_unknown)}: $errorMessage")
                callback.onFirestoreUserCreated(user.uid)
            }
        })
    }
}