package eus.arreseainhize.bookjounal.fragments.reading;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

import eus.arreseainhize.bookjounal.R;
import eus.arreseainhize.bookjounal.databinding.FragmentReadingLogBinding;
import eus.arreseainhize.bookjounal.databinding.ItemLogBookBinding;
import eus.arreseainhize.bookjounal.models.ReadingLog;
import eus.arreseainhize.bookjounal.utils.LogoutManager;
import eus.arreseainhize.bookjounal.viewmodels.ReadingViewModel;

public class ReadingLogFragment extends Fragment {

    private FragmentReadingLogBinding binding;
    private ReadingViewModel readingViewModel;
    private FirebaseAuth mAuth;
    private ReadingLogAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentReadingLogBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        readingViewModel = new ViewModelProvider(requireActivity()).get(ReadingViewModel.class);

        setupRecyclerView();
        observeReadingLogs();

        binding.btnLogAdd.setOnClickListener(v -> {
            Navigation.findNavController(requireView())
                    .navigate(R.id.action_readingLogFragment_to_addBookLogFragment);
        });

        binding.btnLogOut.setOnClickListener(v -> {
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
        adapter = new ReadingLogAdapter();
        binding.recyclerviewRLcontents.setAdapter(adapter);
    }

    private void observeReadingLogs() {
        if (mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();
            readingViewModel.getUserReadingLogs(userId).observe(getViewLifecycleOwner(), readingLogs -> {
                if (readingLogs != null && !readingLogs.isEmpty()) {
                    adapter.setReadingLogs(readingLogs);
                    binding.recyclerviewRLcontents.setVisibility(View.VISIBLE);
                } else {
                    adapter.setReadingLogs(new ArrayList<>());
                    binding.recyclerviewRLcontents.setVisibility(View.GONE);
                    Toast.makeText(requireContext(), R.string.no_reading_logs, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    static class ReadingLogViewHolder extends RecyclerView.ViewHolder {
        ItemLogBookBinding binding;

        ReadingLogViewHolder(@NonNull ItemLogBookBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    class ReadingLogAdapter extends RecyclerView.Adapter<ReadingLogViewHolder> {
        private List<ReadingLog> readingLogs = new ArrayList<>();

        @NonNull
        @Override
        public ReadingLogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemLogBookBinding binding = ItemLogBookBinding.inflate(getLayoutInflater(), parent, false);
            return new ReadingLogViewHolder(binding);
        }

        @SuppressLint("ResourceAsColor")
        @Override
        public void onBindViewHolder(@NonNull ReadingLogViewHolder holder, int position) {
            ReadingLog log = readingLogs.get(position);

            holder.binding.titleID.setText(log.getTitle() != null ? log.getTitle() :
                    getString(R.string.no_available_title));
            holder.binding.authorID.setText(log.getAuthor() != null ? log.getAuthor() :
                    getString(R.string.no_available_author));

            holder.binding.startDateID.setText(log.getStartDate() != null && !log.getStartDate().isEmpty() ?
                    log.getStartDate() : getString(R.string.no_date));
            holder.binding.endDateID.setText(log.getEndDate() != null && !log.getEndDate().isEmpty() ?
                    log.getEndDate() : getString(R.string.no_date));

            int rating = log.getRating();
            holder.binding.ratingID.setText(rating + "/5");

            if (rating > 0) {
                holder.binding.ratingID.setTextColor(requireContext().getColor(R.color.border_blue));
            } else {
                holder.binding.ratingID.setTextColor(requireContext().getColor(R.color.white));
            }

            if (log.getImageUrl() != null && !log.getImageUrl().isEmpty()) {
                Glide.with(holder.itemView.getContext())
                        .load(log.getImageUrl())
                        .error(R.drawable.book_ribbon_24dp)
                        .placeholder(R.drawable.book_ribbon_24dp)
                        .into(holder.binding.image2);
            } else {
                holder.binding.image2.setImageResource(R.drawable.book_ribbon_24dp);
            }

            holder.binding.btnLogEdit.setOnClickListener(v -> {
                Bundle bundle = new Bundle();
                bundle.putSerializable("book_data", log);
                bundle.putString("document_id", log.getDocumentId());

                Navigation.findNavController(requireView())
                        .navigate(R.id.action_readingLogFragment_to_logBookInfoFragment, bundle);
            });
        }

        @Override
        public int getItemCount() {
            return readingLogs.size();
        }

        void setReadingLogs(List<ReadingLog> logs) {
            this.readingLogs = logs;
            notifyDataSetChanged();
        }
    }
}