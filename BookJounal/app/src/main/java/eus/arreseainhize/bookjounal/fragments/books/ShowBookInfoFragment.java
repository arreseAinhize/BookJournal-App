package eus.arreseainhize.bookjounal.fragments.books;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;

import eus.arreseainhize.bookjounal.R;
import eus.arreseainhize.bookjounal.databinding.FragmentShowBookInfoBinding;

public class ShowBookInfoFragment extends Fragment {
    private FragmentShowBookInfoBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentShowBookInfoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Recibir los datos del Bundle
        Bundle args = getArguments();
        if (args != null) {
            String title = args.getString("book_title", String.valueOf(R.string.no_available_title));
            String[] authors = args.getStringArray("book_authors");
            String cover = args.getString("book_cover");
            String published = args.getString("book_published", String.valueOf(R.string.no_available_published));
            String isbn = args.getString("book_isbn", String.valueOf(R.string.no_available_isbn));
            String synopsis = args.getString("book_synopsis", String.valueOf(R.string.no_available_sinopsis));

            // ============================================================
            // MOSTRAR LOS DATOS EN LAS VISTAS DEL LAYOUT
            // ============================================================

            // TÍTULO (tvTitel)
            binding.tvTitel.setText(title);

            // AUTOR (tvAutor)
            if (authors != null && authors.length > 0) {
                StringBuilder authorsText = new StringBuilder();
                for (String author : authors) {
                    if (authorsText.length() > 0) authorsText.append(", ");
                    authorsText.append(author);
                }
                binding.tvAutor.setText(authorsText.toString());
            } else {
                binding.tvAutor.setText(R.string.no_available_author);
            }

            // ISBN (tvISBN)
            binding.tvISBN.setText(isbn);

            // GÉNERO / AÑO (tvGenre) - Usando el campo genre para mostrar el año
            binding.tvGenre.setText(published);

            // SINOPSIS (tvSinopsis) - ¡AHORA CON DATOS REALES!
            binding.tvSinopsis.setText(synopsis);

            // IMAGEN DE PORTADA (image2)
            if (cover != null && !cover.isEmpty()) {
                Glide.with(requireContext())
                        .load(cover)
                        .error(R.drawable.book_ribbon_24dp)
                        .placeholder(R.drawable.book_ribbon_24dp)
                        .into(binding.image2);
            } else {
                binding.image2.setImageResource(R.drawable.book_ribbon_24dp);
            }

        }

        // ============================================================
        // BOTÓN CERRAR - Volver al fragment anterior
        // ============================================================
        binding.btnCloseBookInfo.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_showBookInfoFragment_to_bookInfoFragment);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}