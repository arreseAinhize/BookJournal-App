package eus.arreseainhize.bookjounal.fragments.reading;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import eus.arreseainhize.bookjounal.R;
import eus.arreseainhize.bookjounal.databinding.FragmentAddBookLogBinding;
import eus.arreseainhize.bookjounal.databinding.ItemSearchBookBinding;
import eus.arreseainhize.bookjounal.models.Book;
import eus.arreseainhize.bookjounal.models.ReadingLog;
import eus.arreseainhize.bookjounal.viewmodels.BookViewModel;
import eus.arreseainhize.bookjounal.viewmodels.ReadingViewModel;


public class AddBookLogFragment extends Fragment {
    private FragmentAddBookLogBinding binding;
    private BookViewModel bookViewModel;
    private ReadingViewModel readingViewModel;

    private ContentsAdapter contentsAdapter;
    private Handler searchHandler;
    private Runnable searchRunnable;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inicializar Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAddBookLogBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicializar Handler
        searchHandler = new Handler(Looper.getMainLooper());

        // Botón de atrás
        view.findViewById(R.id.btnBack).setOnClickListener(v -> {
            clearPendingSearches();
            Navigation.findNavController(requireView()).navigate(R.id.action_addBookLogFragment_to_readingLogFragment);
        });

        // Inicializar ViewModels
        bookViewModel = new ViewModelProvider(requireActivity()).get(BookViewModel.class);
        readingViewModel = new ViewModelProvider(requireActivity()).get(ReadingViewModel.class);

        // Inicializar Adapter
        contentsAdapter = new ContentsAdapter();
        binding.recyclerviewRLcontents.setAdapter(contentsAdapter);

        // Configurar SearchView
        setupSearchView();

        // Observar los cambios en los datos
        bookViewModel.volumeInfoLiveData.observe(getViewLifecycleOwner(), volumeInfos -> {
            if (volumeInfos != null && !volumeInfos.isEmpty()) {
                contentsAdapter.establishContentList(volumeInfos);
            } else {
                contentsAdapter.establishContentList(null);
            }
        });
    }

    private void setupSearchView() {
        binding.text.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                clearPendingSearches();
                if (s != null && s.trim().length() >= 3) {
                    performSearch(s);
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                clearPendingSearches();

                if (s != null && s.trim().length() >= 3) {
                    searchRunnable = () -> performSearch(s);
                    searchHandler.postDelayed(searchRunnable, 1000);
                } else if (s == null || s.trim().isEmpty()) {
                    bookViewModel.search("");
                }
                return false;
            }
        });
    }

    private void performSearch(String queryText) {
        if (queryText != null && !queryText.trim().isEmpty()) {
            bookViewModel.search(queryText.trim());
        }
    }

    private void clearPendingSearches() {
        if (searchRunnable != null) {
            searchHandler.removeCallbacks(searchRunnable);
            searchRunnable = null;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        clearPendingSearches();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        clearPendingSearches();
        searchHandler = null;
        binding = null;
    }

    static class ContentViewHolder extends RecyclerView.ViewHolder {
        ItemSearchBookBinding binding;

        public ContentViewHolder(@NonNull ItemSearchBookBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    class ContentsAdapter extends RecyclerView.Adapter<ContentViewHolder> {
        List<Book.VolumeInfo> contentList;

        @NonNull
        @Override
        public ContentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ContentViewHolder(ItemSearchBookBinding.inflate(getLayoutInflater(), parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ContentViewHolder holder, int position) {
            if (contentList == null || position >= contentList.size()) {
                return;
            }

            Book.VolumeInfo volumeInfo = contentList.get(position);

            // Configurar el botón para guardar
            holder.binding.btnSearchBook.setText(R.string.btn_add);
            holder.binding.btnSearchBook.setOnClickListener(v -> {
                // Crear objeto con los datos del libro seleccionado
                ReadingLog bookData = new ReadingLog();

                // Título
                bookData.setTitle(volumeInfo.title != null ? volumeInfo.title :
                        holder.itemView.getContext().getString(R.string.no_available_title));

                // Autor (formatear múltiples autores)
                if (volumeInfo.authors != null && !volumeInfo.authors.isEmpty()) {
                    StringBuilder authorsText = new StringBuilder();
                    for (String author : volumeInfo.authors) {
                        if (authorsText.length() > 0) authorsText.append(", ");
                        authorsText.append(author);
                    }
                    bookData.setAuthor(authorsText.toString());
                } else {
                    bookData.setAuthor(holder.itemView.getContext().getString(R.string.no_available_author));
                }

                // ISBN
                if (volumeInfo.isbns != null && !volumeInfo.isbns.isEmpty()) {
                    bookData.setIsbn(volumeInfo.isbns.get(0));
                } else {
                    bookData.setIsbn(holder.itemView.getContext().getString(R.string.no_available_isbn));
                }

                // Género
                bookData.setGenre(holder.itemView.getContext().getString(R.string.unknown_genre));

                // URL de la imagen
                bookData.setImageUrl(volumeInfo.coverImage != null ? volumeInfo.coverImage : "");

                // ID del libro
                bookData.setBookId(volumeInfo.workKey != null ? volumeInfo.workKey : "");

                // Crear Bundle y pasar los datos
                Bundle bundle = new Bundle();
                bundle.putSerializable("book_data", bookData);

                Navigation.findNavController(requireView())
                        .navigate(R.id.action_addBookLogFragment_to_logBookInfoFragment, bundle);
            });

            // Mostrar título
            holder.binding.titleID.setText(volumeInfo.title != null ? volumeInfo.title :
                    holder.itemView.getContext().getString(R.string.no_available_title));

            // Mostrar autores
            if (volumeInfo.authors != null && !volumeInfo.authors.isEmpty()) {
                StringBuilder authorsText = new StringBuilder();
                for (String author : volumeInfo.authors) {
                    if (authorsText.length() > 0) authorsText.append(", ");
                    authorsText.append(author);
                }
                holder.binding.authorID.setText(authorsText.toString());
            } else {
                holder.binding.authorID.setText(R.string.no_available_author);
            }

            // Mostrar imagen
            if (volumeInfo.coverImage != null && !volumeInfo.coverImage.isEmpty()) {
                Glide.with(holder.itemView.getContext())
                        .load(volumeInfo.coverImage)
                        .error(R.drawable.book_24dp)
                        .placeholder(R.drawable.book_24dp)
                        .into(holder.binding.image2);
            } else {
                holder.binding.image2.setImageResource(R.drawable.book_24dp);
            }
        }

        @Override
        public int getItemCount() {
            return contentList == null ? 0 : contentList.size();
        }

        void establishContentList(List<Book.VolumeInfo> contentList) {
            this.contentList = contentList;
            notifyDataSetChanged();
        }
    }
}