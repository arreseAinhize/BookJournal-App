package eus.arreseainhize.bookjounal.viewmodels;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eus.arreseainhize.bookjounal.R;
import eus.arreseainhize.bookjounal.models.Book;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class BookViewModel extends AndroidViewModel {

    public MutableLiveData<List<Book.VolumeInfo>> volumeInfoLiveData = new MutableLiveData<>();

    // NUEVO: LiveData para las sinopsis
    private MutableLiveData<Map<String, String>> synopsisMap = new MutableLiveData<>(new HashMap<>());

    public BookViewModel(@NonNull Application application) {
        super(application);
    }

    // En BookViewModel.java, modifica el método search:

    public void search(String query) {
        Log.d("ViewModel.search", "Buscando en Open Library: " + query);

        if (query == null || query.isEmpty() || query.trim().length() < 2) {
            volumeInfoLiveData.postValue(new ArrayList<>());
            return;
        }

        String searchQuery = query.trim();

        // Añadir más campos y mejorar la búsqueda
        String fields = "key,title,author_name,first_publish_year,isbn,cover_i";

        Book.api.search(searchQuery, 30, fields).enqueue(new Callback<Book.BookResponse>() {
            @Override
            public void onResponse(@NonNull Call<Book.BookResponse> call, @NonNull Response<Book.BookResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Book.BookResponse bookResponse = response.body();
                    List<Book.VolumeInfo> volumeInfos = new ArrayList<>();

                    if (bookResponse.docs != null && !bookResponse.docs.isEmpty()) {
                        // Filtrar resultados para mayor precisión (opcional)
                        for (Book.Doc doc : bookResponse.docs) {
                            // Solo añadir si el título contiene la búsqueda (case insensitive)
                            if (doc.title != null && doc.title.toLowerCase().contains(searchQuery.toLowerCase())) {
                                Book.VolumeInfo volumeInfo = new Book.VolumeInfo(doc);
                                volumeInfos.add(volumeInfo);
                            }
                        }

                        // Si no hay resultados filtrados, usar todos
                        if (volumeInfos.isEmpty()) {
                            for (Book.Doc doc : bookResponse.docs) {
                                Book.VolumeInfo volumeInfo = new Book.VolumeInfo(doc);
                                volumeInfos.add(volumeInfo);
                            }
                        }

                        Log.d("ViewModel", "Resultados encontrados: " + volumeInfos.size());

                        // Limpiar mapa de sinopsis antes de nueva búsqueda
                        synopsisMap.postValue(new HashMap<>());

                        volumeInfoLiveData.postValue(volumeInfos);

                        // Cargar sinopsis para cada libro
                        for (Book.VolumeInfo volumeInfo : volumeInfos) {
                            if (volumeInfo.workKey != null && !volumeInfo.workKey.isEmpty()) {
                                fetchSynopsis(volumeInfo.workKey, volumeInfo);
                            }
                        }
                    } else {
                        volumeInfoLiveData.postValue(new ArrayList<>());
                    }
                } else {
                    volumeInfoLiveData.postValue(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(@NonNull Call<Book.BookResponse> call, @NonNull Throwable t) {
                volumeInfoLiveData.postValue(new ArrayList<>());
                Log.e("ViewModel", "Error de conexión: " + t.getMessage());
            }
        });
    }
    // NUEVO: Método para obtener la sinopsis de una obra
    // En BookViewModel.java, modifica el método fetchSynopsis:

    public void fetchSynopsis(String key, Book.VolumeInfo volumeInfo) {
        // Evitar llamadas duplicadas
        if (synopsisMap.getValue() != null && synopsisMap.getValue().containsKey(volumeInfo.title)) {
            return; // Ya tenemos la sinopsis
        }

        // Marcar que estamos cargando (para evitar múltiples llamadas)
        Map<String, String> currentMap = synopsisMap.getValue();
        if (currentMap == null) currentMap = new HashMap<>();
        currentMap.put(volumeInfo.title, String.valueOf(R.string.loading));
        synopsisMap.postValue(currentMap);

        Book.api.getWorkDetails(key).enqueue(new Callback<Book.WorkDetails>() {
            @Override
            public void onResponse(@NonNull Call<Book.WorkDetails> call, @NonNull Response<Book.WorkDetails> response) {
                Map<String, String> map = synopsisMap.getValue();
                if (map == null) map = new HashMap<>();

                if (response.isSuccessful() && response.body() != null) {
                    String synopsis = response.body().getDescriptionText();
                    if (synopsis != null && !synopsis.isEmpty()) {
                        map.put(volumeInfo.title, synopsis);
                        Log.d("ViewModel", "✅ Sinopsis obtenida para: " + volumeInfo.title);
                    } else {
                        map.put(volumeInfo.title, String.valueOf(R.string.no_available_sinopsis));
                        Log.d("ViewModel", "⚠️ Sinopsis vacía para: " + volumeInfo.title);
                    }
                } else {
                    map.put(volumeInfo.title, String.valueOf(R.string.error_loading_synopsis));
                    Log.e("ViewModel", "❌ Error obteniendo sinopsis para: " + volumeInfo.title);
                }
                synopsisMap.postValue(map);
            }

            @Override
            public void onFailure(@NonNull Call<Book.WorkDetails> call, @NonNull Throwable t) {
                Map<String, String> map = synopsisMap.getValue();
                if (map == null) map = new HashMap<>();
                map.put(volumeInfo.title, String.valueOf(R.string.error_conexion));
                synopsisMap.postValue(map);
                Log.e("ViewModel", "❌ Error de conexión: " + t.getMessage());
            }
        });
    }

    // NUEVO: Getter para el mapa de sinopsis
    public MutableLiveData<Map<String, String>> getSynopsisMap() {
        return synopsisMap;
    }
}