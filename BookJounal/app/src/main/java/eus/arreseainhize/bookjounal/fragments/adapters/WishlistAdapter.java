package eus.arreseainhize.bookjounal.fragments.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import eus.arreseainhize.bookjounal.R;
import eus.arreseainhize.bookjounal.databinding.ItemWishBookBinding;
import eus.arreseainhize.bookjounal.models.WishlistItem;

public class WishlistAdapter extends RecyclerView.Adapter<WishlistAdapter.WishlistViewHolder> {

    private List<WishlistItem> wishlistItems = new ArrayList<>();
    private OnItemClickListener listener;
    private OnDeleteClickListener deleteListener;

    public interface OnItemClickListener {
        void onItemClick(WishlistItem item);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(WishlistItem item, int position);
    }

    public WishlistAdapter(OnItemClickListener listener, OnDeleteClickListener deleteListener) {
        this.listener = listener;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public WishlistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemWishBookBinding binding = ItemWishBookBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new WishlistViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull WishlistViewHolder holder, int position) {
        WishlistItem item = wishlistItems.get(position);
        holder.bind(item, listener, deleteListener, position);
    }

    @Override
    public int getItemCount() {
        return wishlistItems.size();
    }

    public void updateList(List<WishlistItem> newList) {
        this.wishlistItems = newList != null ? newList : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        if (position >= 0 && position < wishlistItems.size()) {
            wishlistItems.remove(position);
            notifyItemRemoved(position);
        }
    }

    static class WishlistViewHolder extends RecyclerView.ViewHolder {
        private final ItemWishBookBinding binding;

        public WishlistViewHolder(@NonNull ItemWishBookBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(WishlistItem item, OnItemClickListener listener,
                         OnDeleteClickListener deleteListener, int position) {

            // ============================================================
            // TITULO
            // ============================================================
            if (item.getTitle() != null && !item.getTitle().isEmpty()) {
                binding.titleID.setText(item.getTitle());
            } else {
                binding.titleID.setText(R.string.no_available_title);
            }

            // ============================================================
            // AUTORES
            // ============================================================
            String authorText = "";

            if (item.getAuthors() != null && !item.getAuthors().isEmpty()) {
                StringBuilder authors = new StringBuilder();
                List<String> authorList = item.getAuthors();
                for (int i = 0; i < authorList.size(); i++) {
                    if (i > 0) authors.append(", ");
                    authors.append(authorList.get(i));
                }
                authorText = authors.toString();
            }

            if (!authorText.isEmpty()) {
                binding.authorID.setText(authorText);
            } else {
                binding.authorID.setText(R.string.no_available_author);
            }

            // ============================================================
            // IMAGEN
            // ============================================================
            if (item.getCoverImage() != null && !item.getCoverImage().isEmpty()) {
                Glide.with(binding.getRoot().getContext())
                        .load(item.getCoverImage())
                        .error(R.drawable.book_ribbon_24dp)
                        .placeholder(R.drawable.book_ribbon_24dp)
                        .into(binding.image2);
            } else {
                binding.image2.setImageResource(R.drawable.book_ribbon_24dp);
            }

            // ============================================================
            // ITEM COMPLETO
            // ============================================================
            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(item);
                }
            });

            // ============================================================
            // BOTON DE ELIMINAR
            // ============================================================
            binding.btnWishDelete.setOnClickListener(v -> {
                if (deleteListener != null) {
                    deleteListener.onDeleteClick(item, position);
                }
            });
        }
    }
}