package eus.arreseainhize.bookjounal.fragments.books;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

import eus.arreseainhize.bookjounal.R;
import eus.arreseainhize.bookjounal.databinding.FragmentFavoriteBooksBinding;
import eus.arreseainhize.bookjounal.databinding.FragmentReadingLogBinding;
import eus.arreseainhize.bookjounal.databinding.ItemFavoriteBookBinding;
import eus.arreseainhize.bookjounal.databinding.ItemLogBookBinding;
import eus.arreseainhize.bookjounal.fragments.reading.ReadingLogFragment;
import eus.arreseainhize.bookjounal.models.FavoriteBooks;
import eus.arreseainhize.bookjounal.models.ReadingLog;
import eus.arreseainhize.bookjounal.utils.LogoutManager;
import eus.arreseainhize.bookjounal.viewmodels.ReadingViewModel;

public class FavoriteBooksFragment extends Fragment {
    private FragmentFavoriteBooksBinding binding;
    private FirebaseAuth mAuth;
    private FavoriteBookAdapter adapter;
    private ReadingViewModel readingViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentFavoriteBooksBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        readingViewModel = new ViewModelProvider(requireActivity()).get(ReadingViewModel.class);

        setupRecyclerView();
        observeReadingLogs();

        // Configurar el botón de logout
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
    }

    private void setupRecyclerView() {
        adapter = new FavoriteBookAdapter();
        binding.recyclerviewFBcontents.setAdapter(adapter);
    }

    private void observeReadingLogs() {
        if (mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();
            readingViewModel.getFavoriteBooks(userId).observe(getViewLifecycleOwner(), favoriteBooks -> {
                if (favoriteBooks != null && !favoriteBooks.isEmpty()) {
                    adapter.setFavoriteBook(favoriteBooks);
                    binding.recyclerviewFBcontents.setVisibility(View.VISIBLE);
                } else {
                    adapter.setFavoriteBook(new ArrayList<>());
                    binding.recyclerviewFBcontents.setVisibility(View.GONE);
                    Toast.makeText(requireContext(), R.string.no_reading_logs, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    static class FavoriteBookViewHolder extends RecyclerView.ViewHolder {
        ItemFavoriteBookBinding binding;

        FavoriteBookViewHolder(@NonNull ItemFavoriteBookBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    class FavoriteBookAdapter extends RecyclerView.Adapter<FavoriteBookViewHolder> {
        private List<FavoriteBooks> favoriteBooks = new ArrayList<>();

        @NonNull
        @Override
        public FavoriteBookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemFavoriteBookBinding binding = ItemFavoriteBookBinding.inflate(getLayoutInflater(), parent, false);
            return new FavoriteBookViewHolder(binding);
        }

        @SuppressLint("ResourceAsColor")
        @Override
        public void onBindViewHolder(@NonNull FavoriteBookViewHolder holder, int position) {
            FavoriteBooks log = favoriteBooks.get(position);

            holder.binding.titleID.setText(log.getTitle() != null ? log.getTitle() :
                    getString(R.string.no_available_title));
            holder.binding.authorID.setText(log.getAuthor() != null ? log.getAuthor() :
                    getString(R.string.no_available_author));

            int rating = log.getRating();
            holder.binding.ratingID.setText(rating + "/5");

            if (log.getImageUrl() != null && !log.getImageUrl().isEmpty()) {
                Glide.with(holder.itemView.getContext())
                        .load(log.getImageUrl())
                        .error(R.drawable.book_ribbon_24dp)
                        .placeholder(R.drawable.book_ribbon_24dp)
                        .into(holder.binding.image2);
            } else {
                holder.binding.image2.setImageResource(R.drawable.book_ribbon_24dp);
            }
        }

        @Override
        public int getItemCount() {
            return favoriteBooks.size();
        }

        void setFavoriteBook(List<FavoriteBooks> logs) {
            this.favoriteBooks = logs;
            notifyDataSetChanged();
        }
    }
}