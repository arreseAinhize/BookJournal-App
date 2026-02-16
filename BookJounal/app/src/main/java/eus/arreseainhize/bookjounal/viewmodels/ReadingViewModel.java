package eus.arreseainhize.bookjounal.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import eus.arreseainhize.bookjounal.models.FavoriteBooks;
import eus.arreseainhize.bookjounal.models.ReadingLog;

public class ReadingViewModel extends ViewModel {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();

    public LiveData<Boolean> saveReadingLog(ReadingLog readingLog) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();

        String userId = mAuth.getCurrentUser().getUid();

        db.collection("usuarios")
                .document(userId)
                .collection("reading_logs")
                .add(readingLog)
                .addOnSuccessListener(documentReference -> {
                    // Guardamos el ID del documento en el objeto para referencia futura
                    readingLog.setDocumentId(documentReference.getId());
                    result.setValue(true);
                })
                .addOnFailureListener(e -> {
                    result.setValue(false);
                });

        return result;
    }

    public LiveData<List<ReadingLog>> getUserReadingLogs(String userId) {
        MutableLiveData<List<ReadingLog>> logsLiveData = new MutableLiveData<>();

        db.collection("usuarios")
                .document(userId)
                .collection("reading_logs")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<ReadingLog> logs = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            ReadingLog log = document.toObject(ReadingLog.class);
                            log.setDocumentId(document.getId()); // Asignamos el ID del documento
                            logs.add(log);
                        }
                        logsLiveData.setValue(logs);
                    } else {
                        logsLiveData.setValue(new ArrayList<>());
                    }
                });

        return logsLiveData;
    }

    public LiveData<Boolean> updateReadingLog(String documentId, ReadingLog updatedLog) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        String userId = mAuth.getCurrentUser().getUid();

        db.collection("usuarios")
                .document(userId)
                .collection("reading_logs")
                .document(documentId)
                .set(updatedLog)
                .addOnSuccessListener(aVoid -> {
                    updatedLog.setDocumentId(documentId);
                    result.setValue(true);
                })
                .addOnFailureListener(e -> result.setValue(false));

        return result;
    }

    public LiveData<Boolean> deleteReadingLog(String documentId) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        String userId = mAuth.getCurrentUser().getUid();

        db.collection("usuarios")
                .document(userId)
                .collection("reading_logs")
                .document(documentId)
                .delete()
                .addOnSuccessListener(aVoid -> result.setValue(true))
                .addOnFailureListener(e -> result.setValue(false));

        return result;
    }

    public LiveData<List<FavoriteBooks>> getFavoriteBooks(String userId){
        MutableLiveData<List<FavoriteBooks>> favsLiveData = new MutableLiveData<>();

        db.collection("usuarios")
                .document(userId)
                .collection("reading_logs")
                .orderBy("rating", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<FavoriteBooks> favBooks = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            FavoriteBooks favBook = document.toObject(FavoriteBooks.class);
                            if(favBook.getRating()>4){
                                favBook.setDocumentId(document.getId()); // Asignamos el ID del documento
                                favBooks.add(favBook);
                            }
                        }
                        favsLiveData.setValue(favBooks);
                    } else {
                        favsLiveData.setValue(new ArrayList<>());
                    }
                });

        return favsLiveData;
    }
}