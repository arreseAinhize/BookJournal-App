package eus.arreseainhize.bookjounal.fragments.wishlist;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import eus.arreseainhize.bookjounal.R;
import eus.arreseainhize.bookjounal.databinding.FragmentWishListBinding;
import eus.arreseainhize.bookjounal.fragments.adapters.WishlistAdapter;
import eus.arreseainhize.bookjounal.models.WishlistItem;
import eus.arreseainhize.bookjounal.utils.LogoutManager;
import eus.arreseainhize.bookjounal.viewmodels.WishlistViewModel;

public class WishListFragment extends Fragment {

    private static final String TAG = "WishListFragment";

    private FragmentWishListBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private WishlistViewModel wishlistViewModel;
    private WishlistAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();
        wishlistViewModel = new ViewModelProvider(this).get(WishlistViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentWishListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (currentUser == null) {
            Toast.makeText(requireContext(),
                    R.string.error_user_not_authenticated,
                    Toast.LENGTH_SHORT).show();
            Navigation.findNavController(view).navigate(R.id.action_wishListFragment_to_fristFragment);
            return;
        }

        initAdapter();
        setupRecyclerView();
        setupButtons();
        observeViewModel();
        loadWishlistItemsFromFirestore();
    }

    private void initAdapter() {
        // Listener para click en item
        WishlistAdapter.OnItemClickListener itemClickListener = item -> {
            Toast.makeText(requireContext(),
                    "Item clicked: " + item.getTitle(),
                    Toast.LENGTH_SHORT).show();
        };

        // Listener para click en botón eliminar
        WishlistAdapter.OnDeleteClickListener deleteClickListener = (item, position) -> {
            showDeleteConfirmationDialog(item, position);
        };

        adapter = new WishlistAdapter(itemClickListener, deleteClickListener);
        Log.d(TAG, "✅ Adapter inicializado con listeners");
    }

    @SuppressLint("StringFormatInvalid")
    private void showDeleteConfirmationDialog(WishlistItem item, int position) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.delete_title)
                .setMessage(getString(R.string.delete_confirmation_message, item.getTitle()))
                .setPositiveButton(R.string.btnYes, (dialog, which) -> {
                    deleteItemFromFirestore(item, position);
                })
                .setNegativeButton(R.string.btnNo, null)
                .show();
    }

    private void deleteItemFromFirestore(WishlistItem item, int position) {
        if (currentUser == null || item.getId() == null) {
            Toast.makeText(requireContext(),
                    R.string.error_unknown,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Mostrar indicador de carga (opcional)
        binding.btnSearch.setEnabled(false);

        db.collection("usuarios")
                .document(currentUser.getUid())
                .collection("wishlist")
                .document(item.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "✅ Item eliminado: " + item.getTitle());

                    // Eliminar del adapter y ViewModel
                    adapter.removeItem(position);
                    wishlistViewModel.removeItem(position);

                    Toast.makeText(requireContext(),
                            R.string.delete_success,
                            Toast.LENGTH_SHORT).show();

                    binding.btnSearch.setEnabled(true);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error eliminando: " + e.getMessage());
                    Toast.makeText(requireContext(),
                            getString(R.string.error_deleting) + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    binding.btnSearch.setEnabled(true);
                });
    }

    private void setupRecyclerView() {
        if (adapter == null) return;
        binding.recyclerviewWLcontents.setAdapter(adapter);
    }

    private void setupButtons() {
        binding.btnLogOut.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle(R.string.alertTitle)
                    .setMessage(R.string.msgConfLout)
                    .setPositiveButton(R.string.btnYes, (dialog, which) ->
                            LogoutManager.INSTANCE.logout(this)
                    )
                    .setNegativeButton(R.string.btnNo, null)
                    .show();
        });

        binding.btnSearch.setOnClickListener(v -> {
            Navigation.findNavController(requireView())
                    .navigate(R.id.action_wishListFragment_to_searchWishBookFragment);
        });
    }

    private void observeViewModel() {
        if (adapter == null) return;

        wishlistViewModel.getWishlistItems().observe(getViewLifecycleOwner(), items -> {
            if (items != null) {
                adapter.updateList(items);
            }
        });

        wishlistViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(requireContext(),
                        getString(R.string.error_loading_data) + error,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadWishlistItemsFromFirestore() {
        if (wishlistViewModel.getWishlistItems().getValue() != null &&
                !wishlistViewModel.getWishlistItems().getValue().isEmpty()) {
            return;
        }

        wishlistViewModel.setLoading(true);

        db.collection("usuarios")
                .document(currentUser.getUid())
                .collection("wishlist")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<WishlistItem> items = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        WishlistItem item = document.toObject(WishlistItem.class);
                        if (item != null) {
                            item.setId(document.getId());
                            items.add(item);
                        }
                    }
                    wishlistViewModel.setWishlistItems(items);
                    wishlistViewModel.setLoading(false);
                })
                .addOnFailureListener(e -> {
                    wishlistViewModel.setError(e.getMessage());
                    wishlistViewModel.setLoading(false);
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}