package eus.arreseainhize.bookjounal.utils;

import android.util.Log;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.SetOptions;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import eus.arreseainhize.bookjounal.R;

public class FirestoreHelper {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private static final String TAG = "FirestoreHelper";

    public FirestoreHelper() {
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    // Método para crear documento de usuario si no existe
    public void createUserDocumentIfNotExists(OnUserCreatedListener listener) {
        FirebaseUser firebaseUser = getCurrentUser();
        if (firebaseUser == null) {
            if (listener != null) listener.onError(String.valueOf(R.string.error_user_auth));
            return;
        }

        String userId = firebaseUser.getUid();

        Log.d(TAG, "🔍 Verificando documento para usuario: " + userId);

        // ✅ INTENTAR CON CACHE PRIMERO, LUEGO CON SERVER
        getUsersCollection().document(userId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            Log.d(TAG, "✅ Documento ya existe: " + userId);
                            if (listener != null) listener.onSuccess(userId);
                        } else {
                            Log.d(TAG, "📝 Creando nuevo documento para: " + userId);
                            createNewUserDocument(firebaseUser, listener);
                        }
                    } else {
                        Exception exception = task.getException();
                        Log.w(TAG, "⚠️ Error verificando documento: " + exception.getMessage());

                        // ✅ SI ESTÁ OFFLINE, CREAR DE TODOS MODOS
                        if (exception instanceof FirebaseFirestoreException) {
                            FirebaseFirestoreException firestoreEx = (FirebaseFirestoreException) exception;
                            String errorCode = firestoreEx.getCode().toString();

                            if (errorCode.equals("UNAVAILABLE") ||
                                    exception.getMessage().contains("offline") ||
                                    exception.getMessage().contains("Failed to get document")) {

                                Log.w(TAG, "📴 Modo offline detectado - Creando documento localmente");

                                // Crear el documento de todos modos (se sincronizará después)
                                createNewUserDocument(firebaseUser, new OnUserCreatedListener() {
                                    @Override
                                    public void onSuccess(String userId) {
                                        Log.d(TAG, "✅ Documento encolado para sincronización offline");
                                        if (listener != null) listener.onSuccess(userId);
                                    }

                                    @Override
                                    public void onError(String errorMessage) {
                                        Log.w(TAG, "⚠️ Error creando offline: " + errorMessage);
                                        // ✅ PERMITIR CONTINUAR AÚN CON ERROR
                                        if (listener != null) listener.onSuccess(userId);
                                    }
                                });
                                return;
                            }
                        }

                        // ✅ SI ES OTRO ERROR, AÚN ASÍ PERMITIR CONTINUAR
                        Log.w(TAG, "⚠️ Error no crítico, permitiendo continuar: " + exception.getMessage());
                        if (listener != null) listener.onSuccess(userId);
                    }
                });
    }
    // Método para crear nuevo documento (optimizado para offline)
    private void createNewUserDocument(FirebaseUser firebaseUser, OnUserCreatedListener listener) {
        String userId = firebaseUser.getUid();

        Map<String, Object> userData = new HashMap<>();
        userData.put("user_id", userId);
        userData.put("email", firebaseUser.getEmail());
        userData.put("name", firebaseUser.getDisplayName() != null ?
                firebaseUser.getDisplayName() : "Usuario");
        userData.put("created_at", new Date());
        userData.put("updated_at", new Date());

        if (firebaseUser.getPhotoUrl() != null) {
            userData.put("profile_image_url", firebaseUser.getPhotoUrl().toString());
        }

        // Usar set() en lugar de update() para mejor soporte offline
        getUsersCollection().document(userId)
                .set(userData, SetOptions.merge())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Documento creado/encolado: " + userId);
                        if (listener != null) listener.onSuccess(userId);
                    } else {
                        Exception exception = task.getException();
                        Log.e(TAG, "Error creando documento: " + exception.getMessage());

                        // AÚN ASÍ continuamos si es error de conexión
                        if (exception instanceof FirebaseFirestoreException) {
                            FirebaseFirestoreException firestoreEx = (FirebaseFirestoreException) exception;
                            if (firestoreEx.getCode().toString().equals("UNAVAILABLE") ||
                                    exception.getMessage().contains("offline")) {

                                Log.w(TAG, "Documento se creará cuando haya conexión");
                                if (listener != null) listener.onSuccess(userId);
                                return;
                            }
                        }
                        if (listener != null) listener.onError(exception.getMessage());
                    }
                });
    }

    // Resto de tus métodos existentes (sin cambios)...
    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

    public String getCurrentUserId() {
        FirebaseUser user = getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    public CollectionReference getUsersCollection() {
        return db.collection("usuarios");
    }

    public DocumentReference getCurrentUserDocument() {
        String userId = getCurrentUserId();
        if (userId != null) {
            return getUsersCollection().document(userId);
        }
        return null;
    }

    // Interfaces...
    public interface OnUserCreatedListener {
        void onSuccess(String userId);
        void onError(String errorMessage);
    }
}