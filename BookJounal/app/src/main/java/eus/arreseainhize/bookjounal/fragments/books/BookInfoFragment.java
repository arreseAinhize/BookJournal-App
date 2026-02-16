package eus.arreseainhize.bookjounal.fragments.books;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eus.arreseainhize.bookjounal.R;
import eus.arreseainhize.bookjounal.databinding.FragmentBookInfoBinding;
import eus.arreseainhize.bookjounal.databinding.ItemSearchBookBinding;
import eus.arreseainhize.bookjounal.models.Book;
import eus.arreseainhize.bookjounal.utils.LogoutManager;
import eus.arreseainhize.bookjounal.viewmodels.BookViewModel;

public class BookInfoFragment extends Fragment {

    private FirebaseAuth mAuth;
    private FragmentBookInfoBinding binding;
    private BookViewModel bookViewModel;
    private ContentsAdapterBI contentsAdapter;
    private Handler searchHandler;
    private Runnable searchRunnable;

    // Mapa para almacenar las sinopsis temporalmente
    private Map<String, String> synopsisMap = new HashMap<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentBookInfoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicializar Handler
        searchHandler = new Handler(Looper.getMainLooper());

        // ============================================================
        // VERIFICAR QUE EL BINDING NO ES NULL
        // ============================================================
        if (binding == null) {
            Log.e("BookInfoFragment", "❌ ERROR: binding es null");
            return;
        }

        // ============================================================
        // BOTÓN ATRÁS - VERIFICAR QUE EXISTE ANTES DE USARLO
        // ============================================================
        if (binding.btnBack != null) {
            binding.btnBack.setOnClickListener(v -> {
                new AlertDialog.Builder(requireContext())
                        .setTitle(R.string.alertTitle)
                        .setMessage(R.string.msgConfLout)
                        .setPositiveButton(R.string.btnYes, (dialog, which) ->
                                LogoutManager.INSTANCE.logout(this)  // Usar el nuevo manager
                        )
                        .setNegativeButton(R.string.btnNo, null)
                        .show();
            });
            Log.d("BookInfoFragment", "✅ btnBack configurado correctamente");
        } else {
            Log.e("BookInfoFragment", "❌ ERROR: btnBack es null en el layout");
        }

        // ============================================================
        // VERIFICAR QUE EL RECYCLERVIEW EXISTE
        // ============================================================
        if (binding.recyclerviewWScontents == null) {
            Log.e("BookInfoFragment", "❌ ERROR: recyclerviewWScontents es null");
            return;
        }

        // Inicializar ViewModel
        bookViewModel = new ViewModelProvider(requireActivity()).get(BookViewModel.class);

        // Inicializar Adapter
        contentsAdapter = new ContentsAdapterBI();
        binding.recyclerviewWScontents.setAdapter(contentsAdapter);

        // ============================================================
        // VERIFICAR QUE EL SEARCHVIEW EXISTE
        // ============================================================
        if (binding.text == null) {
            Log.e("BookInfoFragment", "❌ ERROR: SearchView (text) es null");
            return;
        }

        // Configurar SearchView
        setupSearchView();

        // Observar los cambios en los datos (resultados de búsqueda)
        bookViewModel.volumeInfoLiveData.observe(getViewLifecycleOwner(), volumeInfos -> {
            Log.d("BookInfoFragment", "Observando datos: " + (volumeInfos != null ? volumeInfos.size() : 0));
            if (volumeInfos != null && !volumeInfos.isEmpty()) {
                contentsAdapter.establishContentList(volumeInfos);
            } else {
                contentsAdapter.establishContentList(null);
            }
        });

        // Observar el mapa de sinopsis
        bookViewModel.getSynopsisMap().observe(getViewLifecycleOwner(), synopsisMap -> {
            this.synopsisMap = synopsisMap;
            // Actualizar el adapter si es necesario
            if (contentsAdapter != null) {
                contentsAdapter.notifyDataSetChanged();
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
            Log.d("BookInfoFragment", "Buscando: " + queryText);
        }
    }

    private void clearPendingSearches() {
        if (searchRunnable != null) {
            searchHandler.removeCallbacks(searchRunnable);
            searchRunnable = null;
        }
    }
    // ============================================================
    // VIEWHOLDER
    // ============================================================
    static class ContentViewHolderBI extends RecyclerView.ViewHolder {
        ItemSearchBookBinding binding;

        public ContentViewHolderBI(@NonNull ItemSearchBookBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    // ============================================================
    // ADAPTER - CON NAVEGACIÓN A DETALLES
    // ============================================================
    class ContentsAdapterBI extends RecyclerView.Adapter<ContentViewHolderBI> {
        List<Book.VolumeInfo> contentList;

        @NonNull
        @Override
        public ContentViewHolderBI onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemSearchBookBinding binding = ItemSearchBookBinding.inflate(getLayoutInflater(), parent, false);
            return new ContentViewHolderBI(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull ContentViewHolderBI holder, int position) {
            if (contentList == null || position >= contentList.size()) {
                return;
            }

            Book.VolumeInfo volumeInfo = contentList.get(position);

            // ============================================================
            // BOTÓN SELECT - Navega a showBookInfoFragment con los datos
            // ============================================================
            if (holder.binding.btnSearchBook != null) {
                holder.binding.btnSearchBook.setText(R.string.select);
                holder.binding.btnSearchBook.setOnClickListener(v -> {
                    // Crear Bundle con la información del libro
                    Bundle bundle = new Bundle();
                    bundle.putString("book_title", volumeInfo.title);

                    // Pasar autores como array
                    if (volumeInfo.authors != null && !volumeInfo.authors.isEmpty()) {
                        bundle.putStringArray("book_authors", volumeInfo.authors.toArray(new String[0]));
                    }

                    bundle.putString("book_cover", volumeInfo.coverImage);
                    bundle.putString("book_published", volumeInfo.publishedDate);

                    if (volumeInfo.isbns != null && !volumeInfo.isbns.isEmpty()) {
                        bundle.putString("book_isbn", volumeInfo.isbns.get(0));
                    }

                    // Obtener la sinopsis del mapa
                    String synopsis = synopsisMap.get(volumeInfo.title);
                    if (synopsis != null && !synopsis.isEmpty()) {
                        bundle.putString("book_synopsis", synopsis);
                    } else {
                        bundle.putString("book_synopsis", String.valueOf(R.string.error_sinopsis_notfound));
                    }

                    // Navegar al fragment de detalles
                    if (getView() != null) {
                        Navigation.findNavController(requireView())
                                .navigate(R.id.action_bookInfoFragment_to_showBookInfoFragment, bundle);
                    }
                });
            }

            // Título
            if (holder.binding.titleID != null) {
                holder.binding.titleID.setText(volumeInfo.title != null ?
                        volumeInfo.title : holder.itemView.getContext().getString(R.string.no_available_title));
            }

            // Autores
            if (holder.binding.authorID != null) {
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
            }

            // Imagen
            if (holder.binding.image2 != null) {
                if (volumeInfo.coverImage != null && !volumeInfo.coverImage.isEmpty()) {
                    Glide.with(holder.itemView.getContext())
                            .load(volumeInfo.coverImage)
                            .error(R.drawable.book_24dp)
                            .placeholder(R.drawable.book_24dp)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(holder.binding.image2);
                } else {
                    holder.binding.image2.setImageResource(R.drawable.book_24dp);
                }
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Limpiar binding para evitar memory leaks
    }
}