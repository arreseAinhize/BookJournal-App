package eus.arreseainhize.bookjounal.fragments.reading;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import eus.arreseainhize.bookjounal.R;
import eus.arreseainhize.bookjounal.databinding.FragmentLogBookInfoBinding;
import eus.arreseainhize.bookjounal.models.ReadingLog;
import eus.arreseainhize.bookjounal.viewmodels.ReadingViewModel;

public class LogBookInfoFragment extends Fragment {

    private FragmentLogBookInfoBinding binding;
    private ReadingViewModel readingViewModel;
    private ReadingLog bookData;
    private int selectedRating = 0;
    private List<ImageButton> starButtons;
    private String documentId;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    private boolean isEditing = false;

    // Formato de fecha
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (getArguments() != null) {
            bookData = (ReadingLog) getArguments().getSerializable("book_data");
            documentId = getArguments().getString("document_id");
            isEditing = (documentId != null && !documentId.isEmpty());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentLogBookInfoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        readingViewModel = new ViewModelProvider(requireActivity()).get(ReadingViewModel.class);

        initStarButtons();

        // Configurar DatePickers para los campos de fecha
        setupDatePickers();

        if (isEditing) {
            binding.btnSaveBookInfo.setText(R.string.update);
        }

        binding.btnBackBookInfo.setOnClickListener(v -> {
            Navigation.findNavController(requireView())
                    .navigate(R.id.action_logBookInfoFragment_to_readingLogFragment);
        });

        if (bookData != null) {
            displayBookData();
            if (isEditing && bookData.getRating() > 0) {
                setRating(bookData.getRating());
            }
        }

        binding.btnSaveBookInfo.setOnClickListener(v -> {
            if (isEditing) {
                updateReadingLog();
            } else {
                saveReadingLog();
            }
        });
    }

    private void setupDatePickers() {
        // DatePicker para fecha de inicio
        binding.sDateValue.setOnClickListener(v -> showDatePicker(true));
        binding.sDateValue.setFocusable(false); // Para evitar que abra el teclado

        // DatePicker para fecha de fin
        binding.eDateValue.setOnClickListener(v -> showDatePicker(false));
        binding.eDateValue.setFocusable(false); // Para evitar que abra el teclado
    }

    private void showDatePicker(boolean isStartDate) {
        Calendar calendar = Calendar.getInstance();

        // Si ya hay una fecha seleccionada, usarla como valor inicial
        String currentDate = isStartDate ?
                binding.sDateValue.getText().toString() :
                binding.eDateValue.getText().toString();

        if (!currentDate.isEmpty()) {
            try {
                String[] parts = currentDate.split("/");
                int day = Integer.parseInt(parts[0]);
                int month = Integer.parseInt(parts[1]) - 1; // Month es 0-based en Calendar
                int year = Integer.parseInt(parts[2]);
                calendar.set(year, month, day);
            } catch (Exception e) {
                // Si hay error al parsear, usar fecha actual
                Log.e("DatePicker", "Error parsing date: " + e.getMessage());
            }
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    // Formatear la fecha seleccionada (month+1 porque month es 0-based)
                    String selectedDate = String.format(Locale.getDefault(), "%02d/%02d/%d",
                            dayOfMonth, month + 1, year);

                    if (isStartDate) {
                        binding.sDateValue.setText(selectedDate);
                    } else {
                        binding.eDateValue.setText(selectedDate);
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.show();
    }

    private void initStarButtons() {
        starButtons = Arrays.asList(
                binding.star1,
                binding.star2,
                binding.star3,
                binding.star4,
                binding.star5
        );

        for (int i = 0; i < starButtons.size(); i++) {
            final int rating = i + 1;
            ImageButton starButton = starButtons.get(i);
            starButton.setOnClickListener(v -> setRating(rating));
        }
    }

    private void setRating(int rating) {
        selectedRating = rating;

        for (int i = 0; i < starButtons.size(); i++) {
            ImageButton starButton = starButtons.get(i);
            if (i < rating) {
                starButton.setImageResource(R.drawable.ic_star_filled_24dp);
            } else {
                starButton.setImageResource(R.drawable.ic_star_border_24dp);
            }
        }

        binding.tvRatingSelected.setText(rating + "/5");
    }

    private void displayBookData() {
        binding.tvTitel.setText(bookData.getTitle() != null ? bookData.getTitle() :
                getString(R.string.no_available_title));
        binding.tvAutor.setText(bookData.getAuthor() != null ? bookData.getAuthor() :
                getString(R.string.no_available_author));
        binding.tvISBN.setText(bookData.getIsbn() != null ? bookData.getIsbn() :
                getString(R.string.no_available_isbn));
        binding.tvGenre.setText(bookData.getGenre() != null ? bookData.getGenre() :
                getString(R.string.unknown_genre));

        if (isEditing) {
            binding.sDateValue.setText(bookData.getStartDate() != null ? bookData.getStartDate() : "");
            binding.eDateValue.setText(bookData.getEndDate() != null ? bookData.getEndDate() : "");
            binding.lTypeValue.setText(bookData.getLectureType() != null ? bookData.getLectureType() : "");
            binding.bookNotesValue.setText(bookData.getNotes() != null ? bookData.getNotes() : "");
        }

        if (bookData.getImageUrl() != null && !bookData.getImageUrl().isEmpty()) {
            Glide.with(requireContext())
                    .load(bookData.getImageUrl())
                    .error(R.drawable.book_ribbon_24dp)
                    .placeholder(R.drawable.book_ribbon_24dp)
                    .into(binding.image2);
        } else {
            binding.image2.setImageResource(R.drawable.book_ribbon_24dp);
        }
    }

    private void saveReadingLog() {
        if (currentUser == null) {
            Toast.makeText(requireContext(), R.string.error_user_auth, Toast.LENGTH_SHORT).show();
            return;
        }

        if (bookData == null) {
            Toast.makeText(requireContext(), R.string.error_unknown, Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedRating == 0) {
            Toast.makeText(requireContext(), R.string.error_select_rating, Toast.LENGTH_SHORT).show();
            return;
        }

        if (binding.sDateValue.getText().toString().trim().isEmpty()) {
            Toast.makeText(requireContext(), R.string.start_date_required, Toast.LENGTH_SHORT).show();
            return;
        }

        if (binding.eDateValue.getText().toString().trim().isEmpty()) {
            Toast.makeText(requireContext(), R.string.end_date_required, Toast.LENGTH_SHORT).show();
            return;
        }

        // Validar que la fecha de fin no sea anterior a la de inicio
        try {
            String startDate = binding.sDateValue.getText().toString();
            String endDate = binding.eDateValue.getText().toString();

            java.util.Date start = dateFormat.parse(startDate);
            java.util.Date end = dateFormat.parse(endDate);

            if (end.before(start)) {
                Toast.makeText(requireContext(), R.string.error_end_date_before_start, Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (Exception e) {
            Toast.makeText(requireContext(), R.string.error_invalid_date_format, Toast.LENGTH_SHORT).show();
            return;
        }

        updateBookDataFromForm();

        readingViewModel.saveReadingLog(bookData).observe(getViewLifecycleOwner(), success -> {
            if (success) {
                Toast.makeText(requireContext(), R.string.book_saved, Toast.LENGTH_SHORT).show();
                Navigation.findNavController(requireView())
                        .navigate(R.id.action_logBookInfoFragment_to_readingLogFragment);
            } else {
                Toast.makeText(requireContext(), R.string.error_saving_data, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateReadingLog() {
        if (currentUser == null) {
            Toast.makeText(requireContext(), R.string.error_user_auth, Toast.LENGTH_SHORT).show();
            return;
        }

        if (bookData == null || documentId == null) {
            Toast.makeText(requireContext(), R.string.error_unknown, Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedRating == 0) {
            Toast.makeText(requireContext(), R.string.error_select_rating, Toast.LENGTH_SHORT).show();
            return;
        }

        if (binding.sDateValue.getText().toString().trim().isEmpty()) {
            Toast.makeText(requireContext(), R.string.start_date_required, Toast.LENGTH_SHORT).show();
            return;
        }

        if (binding.eDateValue.getText().toString().trim().isEmpty()) {
            Toast.makeText(requireContext(), R.string.end_date_required, Toast.LENGTH_SHORT).show();
            return;
        }

        // Validar que la fecha de fin no sea anterior a la de inicio
        try {
            String startDate = binding.sDateValue.getText().toString();
            String endDate = binding.eDateValue.getText().toString();

            java.util.Date start = dateFormat.parse(startDate);
            java.util.Date end = dateFormat.parse(endDate);

            if (end.before(start)) {
                Toast.makeText(requireContext(), R.string.error_end_date_before_start, Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (Exception e) {
            Toast.makeText(requireContext(), R.string.error_invalid_date_format, Toast.LENGTH_SHORT).show();
            return;
        }

        updateBookDataFromForm();
        bookData.setDocumentId(documentId);

        readingViewModel.updateReadingLog(documentId, bookData).observe(getViewLifecycleOwner(), success -> {
            if (success) {
                Toast.makeText(requireContext(), R.string.data_saved, Toast.LENGTH_SHORT).show();
                Navigation.findNavController(requireView())
                        .navigate(R.id.action_logBookInfoFragment_to_readingLogFragment);
            } else {
                Toast.makeText(requireContext(), R.string.error_saving_data, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateBookDataFromForm() {
        bookData.setStartDate(binding.sDateValue.getText() != null ?
                binding.sDateValue.getText().toString() : "");
        bookData.setEndDate(binding.eDateValue.getText() != null ?
                binding.eDateValue.getText().toString() : "");
        bookData.setLectureType(binding.lTypeValue.getText() != null ?
                binding.lTypeValue.getText().toString() : "");
        bookData.setRating(selectedRating);
        bookData.setNotes(binding.bookNotesValue.getText() != null ?
                binding.bookNotesValue.getText().toString() : "");
        bookData.setUserId(currentUser.getUid());
        bookData.setTimestamp(System.currentTimeMillis());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}