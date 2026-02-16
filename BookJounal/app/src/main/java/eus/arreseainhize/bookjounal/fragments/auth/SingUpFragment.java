package eus.arreseainhize.bookjounal.fragments.auth;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import eus.arreseainhize.bookjounal.R;
import eus.arreseainhize.bookjounal.databinding.FragmentSingUpBinding;
import eus.arreseainhize.bookjounal.utils.FirebaseAuthHelper;
import eus.arreseainhize.bookjounal.utils.FirestoreHelper;
import eus.arreseainhize.bookjounal.utils.FirestoreOfflineHelper;
import eus.arreseainhize.bookjounal.utils.StringResourceHelper;

public class SingUpFragment extends Fragment {

    private FragmentSingUpBinding binding;
    private FirebaseAuthHelper authHelper;
    private FirestoreHelper firestoreHelper;
    private StringResourceHelper stringHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSingUpBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        authHelper = new FirebaseAuthHelper(requireContext());
        firestoreHelper = new FirestoreHelper();
        stringHelper = StringResourceHelper.getInstance(requireContext());

        binding.btnLogIn.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_singUpFragment_to_logInFragment);
        });

        binding.btnSingup.setOnClickListener(v -> {
            String email = binding.Username.getText().toString().trim();
            String password = binding.Password.getText().toString().trim();
            String confirmPassword = binding.PasswordRepeat.getText().toString().trim();

            // Validaciones
            if (!password.equals(confirmPassword)) {
                Toast.makeText(requireContext(),
                        R.string.error_pass1,
                        Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.length() < 6) {
                Toast.makeText(requireContext(),
                        R.string.error_pass2,
                        Toast.LENGTH_SHORT).show();
                return;
            }

            binding.btnSingup.setEnabled(false);

            authHelper.registerWithEmail(email, password,
                    new FirebaseAuthHelper.AuthCallback() {
                        @Override
                        public void onSuccess(com.google.firebase.auth.FirebaseUser user) {
                            Log.d("Registro", "✅ Usuario creado en Auth: " + user.getUid());

                            // ✅ USAR FirestoreOfflineHelper también para registro
                            FirestoreOfflineHelper offlineHelper = new FirestoreOfflineHelper();
                            offlineHelper.createUserDocumentAlways(user, new FirestoreOfflineHelper.SimpleCallback() {
                                @Override
                                public void onSuccess(String userId) {
                                    binding.btnSingup.setEnabled(true);

                                    Log.d("Firestore", "✅ Perfil creado en Firestore: " + userId);

                                    Navigation.findNavController(view)
                                            .navigate(R.id.action_singUpFragment_to_homeFragment);

                                    Toast.makeText(requireContext(),
                                            getString(R.string.welcome) + "!" ,
                                            Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onError(String errorMessage) {
                                    binding.btnSingup.setEnabled(true);

                                    Log.w("Firestore", "⚠️ Advertencia al crear perfil: " + errorMessage);

                                    // ✅ AÚN ASÍ PERMITIR ACCESO
                                    Toast.makeText(requireContext(),
                                            R.string.success_account_created1,
                                            Toast.LENGTH_LONG).show();

                                    Navigation.findNavController(view)
                                            .navigate(R.id.action_singUpFragment_to_homeFragment);
                                }
                            });
                        }

                        @Override
                        public void onError(String errorMessage) {
                            binding.btnSingup.setEnabled(true);

                            Toast.makeText(requireContext(),
                                    errorMessage,
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}