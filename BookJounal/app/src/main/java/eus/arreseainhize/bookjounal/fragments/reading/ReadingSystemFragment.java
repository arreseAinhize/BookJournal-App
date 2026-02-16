package eus.arreseainhize.bookjounal.fragments.reading;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;  // Cambiado
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import eus.arreseainhize.bookjounal.R;
import eus.arreseainhize.bookjounal.models.ReadingSystem;
import eus.arreseainhize.bookjounal.utils.LogoutManager;

public class ReadingSystemFragment extends Fragment {

    private static final String TAG = "ReadingSystemFragment";

    FirebaseAuth mAuth;
    FirebaseFirestore db;
    FirebaseUser currentUser;

    // UI Elements
    private TextView txtStar1, txtStar2, txtStar3, txtStar4, txtStar5;
    private Spinner spinnerStars;  // Cambiado a Spinner
    private TextInputEditText editTextValue;
    private Button btnEditValue;

    // Data
    private ReadingSystem readingData;
    private String selectedStar = "1 ⭐";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reading_system, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupSpinner();
        setupButtons(view);

        // Verificar si hay usuario autenticado
        if (currentUser == null) {
            Toast.makeText(requireContext(), R.string.error_user_auth, Toast.LENGTH_SHORT).show();
            Navigation.findNavController(view).navigate(R.id.action_readingSystemFragment_to_fristFragment);
            return;
        }

        loadReadingSystemData();
    }

    private void loadReadingSystemData() {
        if (currentUser == null) return;

        String userId = currentUser.getUid();
        Log.d(TAG, "Loading reading system data for user: " + userId);

        // Referencia al documento del reading system del usuario
        DocumentReference docRef = db.collection("usuarios")
                .document(userId)
                .collection("reading_system")
                .document("stats");

        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    // Datos existentes - cargarlos
                    readingData = document.toObject(ReadingSystem.class);
                    Log.d(TAG, "Reading system data loaded: " + readingData);
                } else {
                    // No hay datos - crear nuevo
                    Log.d(TAG, "No reading system data found, creating new");
                    readingData = new ReadingSystem(userId);
                }
                // Actualizar UI con los datos
                updateUIWithData();
            } else {
                Log.w(TAG, "Error loading reading system data", task.getException());
                Toast.makeText(requireContext(),
                        R.string.error_loading_data + task.getException().getMessage(),
                        Toast.LENGTH_SHORT).show();
                // Crear datos por defecto
                readingData = new ReadingSystem(userId);
                updateUIWithData();
            }
        });
    }

    private void initViews(View view) {
        txtStar1 = view.findViewById(R.id.txtStar1);
        txtStar2 = view.findViewById(R.id.txtStar2);
        txtStar3 = view.findViewById(R.id.txtStar3);
        txtStar4 = view.findViewById(R.id.txtStar4);
        txtStar5 = view.findViewById(R.id.txtStar5);
        spinnerStars = view.findViewById(R.id.spinnerStars);  // Ahora es Spinner
        editTextValue = view.findViewById(R.id.editTextValue);
        btnEditValue = view.findViewById(R.id.btnEditValue);
    }

    private void setupSpinner() {
        List<String> starOptions = new ArrayList<>();
        starOptions.add("1 ⭐");
        starOptions.add("2 ⭐");
        starOptions.add("3 ⭐");
        starOptions.add("4 ⭐");
        starOptions.add("5 ⭐");

        // Adaptador para Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                starOptions
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStars.setAdapter(adapter);

        // Listener para Spinner
        spinnerStars.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedStar = (String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No hacer nada
            }
        });
    }

    private void setupButtons(View view) {
        // Botón de logout
        view.findViewById(R.id.btnLogOut).setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle(R.string.alertTitle)
                    .setMessage(R.string.msgConfLout)
                    .setPositiveButton(R.string.btnYes, (dialog, which) ->
                            LogoutManager.INSTANCE.logout(this)  // Usar el nuevo manager
                    )
                    .setNegativeButton(R.string.btnNo, null)
                    .show();
        });


        // Botón de editar valor
        btnEditValue.setOnClickListener(v -> {
            if (currentUser == null) {
                Toast.makeText(requireContext(), R.string.error_user_auth, Toast.LENGTH_SHORT).show();
                return;
            }

            String newValueStr = editTextValue.getText().toString().trim();
            if (TextUtils.isEmpty(newValueStr)) {
                Toast.makeText(requireContext(), R.string.error_complete_all_fields, Toast.LENGTH_SHORT).show();
                return;
            }

            // Guardar directamente como String, sin convertir a int
            updateStarValue(selectedStar, newValueStr);
        });
    }

    private void updateStarValue(String star, String newValue) {
        if (readingData == null || currentUser == null) return;

        // Actualizar el valor según la estrella seleccionada
        switch (star) {
            case "1 ⭐":
                readingData.setStar1Value(newValue);
                break;
            case "2 ⭐":
                readingData.setStar2Value(newValue);
                break;
            case "3 ⭐":
                readingData.setStar3Value(newValue);
                break;
            case "4 ⭐":
                readingData.setStar4Value(newValue);
                break;
            case "5 ⭐":
                readingData.setStar5Value(newValue);
                break;
            default:
                return;
        }

        // Actualizar timestamp
        readingData.setUpdatedAt(System.currentTimeMillis());

        // Guardar en Firebase
        saveReadingSystemData();
    }

    private void updateUIWithData() {
        if (readingData != null) {
            txtStar1.setText((readingData.getStar1Value().isEmpty() ? getString(R.string.hint_1star) : readingData.getStar1Value()));
            txtStar2.setText((readingData.getStar2Value().isEmpty() ? getString(R.string.hint_2star) : readingData.getStar2Value()));
            txtStar3.setText((readingData.getStar3Value().isEmpty() ? getString(R.string.hint_3star) : readingData.getStar3Value()));
            txtStar4.setText((readingData.getStar4Value().isEmpty() ? getString(R.string.hint_4star) : readingData.getStar4Value()));
            txtStar5.setText((readingData.getStar5Value().isEmpty() ? getString(R.string.hint_5star) : readingData.getStar5Value()));
        }
    }
    private void saveReadingSystemData() {
        if (currentUser == null || readingData == null) return;

        String userId = currentUser.getUid();

        // Mostrar indicador de guardado
        Toast.makeText(requireContext(), R.string.saving_data, Toast.LENGTH_SHORT).show();

        // Guardar en Firestore
        db.collection("usuarios")
                .document(userId)
                .collection("reading_system")
                .document("stats")
                .set(readingData.toMap())
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Reading system data saved successfully");
                    Toast.makeText(requireContext(), R.string.data_saved, Toast.LENGTH_SHORT).show();

                    // Limpiar campo de entrada
                    editTextValue.setText("");

                    // Actualizar UI
                    updateUIWithData();
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error saving reading system data", e);
                    Toast.makeText(requireContext(), R.string.error_saving_data + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}