package eus.arreseainhize.bookjounal.utils;

import android.content.Context;
import android.widget.Toast;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;

import eus.arreseainhize.bookjounal.R;

public class FirebaseAuthHelper {

    private FirebaseAuth mAuth;
    private Context context;
    private static final String TAG = "FirebaseAuthHelper";

    public FirebaseAuthHelper(Context context) {
        this.context = context;
        this.mAuth = FirebaseAuth.getInstance();
    }

    // Método para login con email
    public void loginWithEmail(String email, String password,
                               AuthCallback callback) {
        if (email.isEmpty() || password.isEmpty()) {
            callback.onError(context.getString(R.string.error_complete_all_fields));
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        callback.onSuccess(user);
                    } else {
                        // Manejo específico de errores de Firebase
                        String errorMessage = getFirebaseAuthErrorMessage(task.getException());
                        callback.onError(errorMessage);
                    }
                });
    }

    // Método para registro
    public void registerWithEmail(String email, String password,
                                  AuthCallback callback) {
        if (email.isEmpty() || password.isEmpty()) {
            callback.onError(context.getString(R.string.error_complete_all_fields));
            return;
        }

        if (password.length() < 6) {
            callback.onError(context.getString(R.string.error_password_length));
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        callback.onSuccess(user);
                    } else {
                        // Manejo específico de errores de Firebase
                        String errorMessage = getFirebaseAuthErrorMessage(task.getException());
                        callback.onError(errorMessage);
                    }
                });
    }

    // Método para obtener mensajes de error específicos de Firebase
    private String getFirebaseAuthErrorMessage(Exception exception) {
        if (exception instanceof FirebaseAuthException) {
            String errorCode = ((FirebaseAuthException) exception).getErrorCode();

            switch (errorCode) {
                case "ERROR_INVALID_EMAIL":
                    return context.getString(R.string.error_invalid_email);

                case "ERROR_WRONG_PASSWORD":
                    return context.getString(R.string.error_wrong_password);

                case "ERROR_USER_NOT_FOUND":
                    return context.getString(R.string.error_user_not_found);

                case "ERROR_USER_DISABLED":
                    return context.getString(R.string.error_user_disabled);

                case "ERROR_EMAIL_ALREADY_IN_USE":
                    return context.getString(R.string.error_email_already_in_use);

                case "ERROR_WEAK_PASSWORD":
                    return context.getString(R.string.error_weak_password);

                case "ERROR_NETWORK_REQUEST_FAILED":
                    return context.getString(R.string.error_network);

                default:
                    return context.getString(R.string.error_unknown);
            }
        }
        return exception != null ?
                exception.getMessage() :
                context.getString(R.string.error_unknown);
    }

    // Interfaz para callbacks
    public interface AuthCallback {
        void onSuccess(FirebaseUser user);
        void onError(String errorMessage);
    }
}