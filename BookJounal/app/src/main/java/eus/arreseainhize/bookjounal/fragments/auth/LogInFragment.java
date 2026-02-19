package eus.arreseainhize.bookjounal.fragments.auth;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import eus.arreseainhize.bookjounal.R;
import eus.arreseainhize.bookjounal.databinding.FragmentLogInBinding;
import eus.arreseainhize.bookjounal.utils.FirebaseAuthHelper;
import eus.arreseainhize.bookjounal.utils.FirestoreOfflineHelper;
import eus.arreseainhize.bookjounal.utils.GoogleSignInHelper;
import eus.arreseainhize.bookjounal.utils.StringResourceHelper;

public class LogInFragment extends Fragment {

    private static final String TAG = "LogInFragment";

    private FragmentLogInBinding binding;
    private FirebaseAuthHelper authHelper;
    private StringResourceHelper stringHelper;
    private FirebaseAuth mAuth;
    private GoogleSignInHelper googleSignInHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentLogInBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        authHelper = new FirebaseAuthHelper(requireContext());
        stringHelper = StringResourceHelper.getInstance(requireContext());
        mAuth = FirebaseAuth.getInstance();

        // Inicializar Google Sign-In Helper con Credential Manager (NO deprecado)
        googleSignInHelper = new GoogleSignInHelper(this, mAuth);
        googleSignInHelper.initialize();

        setupClickListeners(view);
    }

    private void setupClickListeners(View view) {
        binding.btnSingup.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_logInFragment_to_singUpFragment)
        );

        binding.btnLogGoogle.setOnClickListener(v -> signInWithGoogle());

        binding.btnLogin.setOnClickListener(v -> signInWithEmailPassword(view));
    }

    private void signInWithGoogle() {
        googleSignInHelper.signIn(new GoogleSignInHelper.GoogleAuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                Log.d(TAG, "Google auth success: " + user.getUid());
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(requireContext(),
                        getString(R.string.error_google_generic) + ": " + errorMessage,
                        Toast.LENGTH_LONG).show();
                binding.btnLogGoogle.setEnabled(true);
            }

            @Override
            public void onFirestoreUserCreated(String userId) {
                Log.d(TAG, "Usuario listo, navegando a home");
                Navigation.findNavController(binding.getRoot())
                        .navigate(R.id.action_logInFragment_to_homeFragment);
                Toast.makeText(requireContext(),
                        getString(R.string.welcome) + "!",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNoAccountAvailable() {
                new AlertDialog.Builder(requireContext())
                        .setTitle(R.string.error_no_account_title)
                        .setMessage(R.string.error_no_account_message)
                        .setPositiveButton(R.string.action_open_settings, (dialog, which) -> {
                            try {
                                Intent intent = new Intent(Settings.ACTION_SYNC_SETTINGS);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            } catch (Exception e) {
                                Intent intent = new Intent(Settings.ACTION_SETTINGS);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .show();

                binding.btnLogGoogle.setEnabled(true);
            }
        });
    }

    private void signInWithEmailPassword(View view) {
        String email = binding.Username.getText().toString().trim();
        String password = binding.Password.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(requireContext(),
                    R.string.error_complete_all_fields,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        binding.btnLogin.setEnabled(false);

        authHelper.loginWithEmail(email, password,
                new FirebaseAuthHelper.AuthCallback() {
                    @Override
                    public void onSuccess(FirebaseUser user) {
                        Log.d(TAG, "Email auth success: " + user.getUid());
                        createFirestoreUserDocument(user, view);
                    }

                    @Override
                    public void onError(String errorMessage) {
                        binding.btnLogin.setEnabled(true);
                        Toast.makeText(requireContext(),
                                getString(R.string.error_unknown) + ": " + errorMessage,
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void createFirestoreUserDocument(FirebaseUser user, View view) {
        FirestoreOfflineHelper offlineHelper = new FirestoreOfflineHelper();
        offlineHelper.createUserDocumentAlways(user, new FirestoreOfflineHelper.SimpleCallback() {
            @Override
            public void onSuccess(String userId) {
                binding.btnLogin.setEnabled(true);
                navigateToHome(view);
            }

            @Override
            public void onError(String errorMessage) {
                binding.btnLogin.setEnabled(true);
                Log.w(TAG, "Firestore warning: " + errorMessage);
                navigateToHome(view);
            }
        });
    }

    private void navigateToHome(View view) {
        Navigation.findNavController(view)
                .navigate(R.id.action_logInFragment_to_homeFragment);
        Toast.makeText(requireContext(),
                getString(R.string.welcome) + "!",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}