package eus.arreseainhize.bookjounal.utils

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.credentials.CredentialManager
import androidx.credentials.ClearCredentialStateRequest
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.google.firebase.auth.FirebaseAuth
import eus.arreseainhize.bookjounal.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object LogoutManager {

    private val tag = "LogoutManager"

    fun logout(fragment: Fragment) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                clearCredentialManager(fragment.requireContext())
                FirebaseAuth.getInstance().signOut()

                Log.d(tag, "${fragment.getString(R.string.success_logout)}")

                Toast.makeText(
                    fragment.requireContext(),
                    fragment.getString(R.string.success_logout),
                    Toast.LENGTH_SHORT
                ).show()

                navigateToLogin(fragment)

            } catch (e: Exception) {
                Log.e(tag, "Error en logout: ${e.message}")

                try {
                    FirebaseAuth.getInstance().signOut()
                    Toast.makeText(
                        fragment.requireContext(),
                        fragment.getString(R.string.success_logout),
                        Toast.LENGTH_SHORT
                    ).show()
                    navigateToLogin(fragment)
                } catch (ex: Exception) {
                    Toast.makeText(
                        fragment.requireContext(),
                        fragment.getString(R.string.error_logout_msg) + ex.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private suspend fun clearCredentialManager(context: Context) {
        try {
            val credentialManager = CredentialManager.create(context)
            credentialManager.clearCredentialState(
                ClearCredentialStateRequest()
            )
        } catch (e: Exception) {
            Log.w(tag, "No se pudo limpiar Credential Manager: ${e.message}")
        }
    }

    private fun navigateToLogin(fragment: Fragment) {
        try {
            val navController = Navigation.findNavController(fragment.requireView())

            val navAction = when (fragment.javaClass.simpleName) {
                "WishListFragment" -> R.id.action_wishListFragment_to_fristFragment
                "ReadingLogFragment" -> R.id.action_readingLogFragment_to_fristFragment
                "BookInfoFragment" -> R.id.action_bookInfoFragment_to_fristFragment
                "FavoriteBooksFragment" -> R.id.action_favoriteBooksFragment_to_fristFragment
                else -> {
                    try {
                        navController.navigate(R.id.fristFragment)
                        return
                    } catch (e: Exception) {
                        R.id.fristFragment
                    }
                }
            }

            navController.navigate(navAction)
        } catch (e: Exception) {
            Log.e(tag, "Error navegando: ${e.message}")
        }
    }
}