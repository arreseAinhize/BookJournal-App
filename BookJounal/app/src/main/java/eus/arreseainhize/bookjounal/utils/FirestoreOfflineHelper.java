package eus.arreseainhize.bookjounal.utils;

import android.util.Log;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.SetOptions;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class FirestoreOfflineHelper {

    private static final String TAG = "FirestoreOfflineHelper";
    private FirebaseFirestore db;

    public FirestoreOfflineHelper() {
        db = FirebaseFirestore.getInstance();
    }

    // ✅ MÉTODO MEJORADO CON VERIFICACIÓN
    public void createUserDocumentAlways(FirebaseUser firebaseUser, SimpleCallback callback) {
        if (firebaseUser == null) {
            callback.onError("Usuario no autenticado");
            return;
        }

        String userId = firebaseUser.getUid();
        Log.d(TAG, "🔄 Creando documento para: " + userId);

        // ✅ 1. PRIMERO VERIFICAR QUE FIRESTORE ESTÁ ACTIVO
        verifyFirestoreConnection(userId, firebaseUser, callback);
    }

    private void verifyFirestoreConnection(String userId, FirebaseUser firebaseUser, SimpleCallback callback) {
        // Operación de prueba muy simple
        db.collection("connection_test").document("test")
                .get()
                .addOnCompleteListener(testTask -> {
                    if (testTask.isSuccessful()) {
                        // ✅ Firestore está activo - crear documento real
                        Log.d(TAG, "✅ Firestore activo - creando documento");
                        createActualDocument(userId, firebaseUser, callback);
                    } else {
                        Exception ex = testTask.getException();
                        Log.w(TAG, "⚠️ Firestore puede tener problemas: " + ex.getMessage());

                        // Aún así intentar crear el documento
                        createActualDocument(userId, firebaseUser, callback);
                    }
                });
    }

    private void createActualDocument(String userId, FirebaseUser firebaseUser, SimpleCallback callback) {
        // Datos del usuario
        Map<String, Object> userData = new HashMap<>();
        userData.put("user_id", userId);
        userData.put("email", firebaseUser.getEmail());

        String nombre = firebaseUser.getEmail();

        if (firebaseUser.getEmail() != null && firebaseUser.getEmail().contains("@")) {
            nombre = firebaseUser.getEmail().substring(0, firebaseUser.getEmail().indexOf("@"));
        }
        userData.put("name", nombre);
        userData.put("created_at", new Date());
        userData.put("updated_at", new Date());

        if (firebaseUser.getPhotoUrl() != null) {
            userData.put("profile_image_url", firebaseUser.getPhotoUrl().toString());
        }

        // ✅ INTENTAR CREAR EL DOCUMENTO REAL
        db.collection("usuarios").document(userId)
                .set(userData, SetOptions.merge())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "✅ Documento creado exitosamente en Firestore: " + userId);
                        callback.onSuccess(userId);
                    } else {
                        Exception exception = task.getException();
                        Log.e(TAG, "❌ Error creando documento: " + exception.getMessage());

                        // ✅ AÚN ASÍ PERMITIR ACCESO
                        callback.onSuccess(userId + "_fallback");

                        // Mostrar advertencia
                        if (exception instanceof FirebaseFirestoreException) {
                            FirebaseFirestoreException fEx = (FirebaseFirestoreException) exception;
                            Log.e(TAG, "❌ Código error: " + fEx.getCode());
                        }
                    }
                });
    }

    public interface SimpleCallback {
        void onSuccess(String userId);
        void onError(String errorMessage);
    }
}