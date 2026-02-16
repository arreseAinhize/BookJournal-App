// models/Book.java
package eus.arreseainhize.bookjounal.models;

import android.util.Log;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public class Book {

    // ============================================================
    // RESPUESTA DE BÚSQUEDA (search.json)
    // ============================================================
    public static class BookResponse {
        public int numFound;
        public int start;
        public boolean numFoundExact;
        public List<Doc> docs;
    }

    // Documento de Open Library (resultado de búsqueda)
    public static class Doc {
        public String key;                    // Clave de la obra (ej: "/works/OL15395W")
        public String title;                   // Título del libro
        public String title_suggest;           // Título sugerido
        public List<String> author_name;       // Lista de autores
        public int first_publish_year;         // Año de primera publicación
        public List<String> isbn;              // Lista de ISBNs
        public int cover_i;                     // ID de la portada
        public int edition_count;                // Número de ediciones

        // Método para obtener la URL de la portada
        public String getCoverUrl() {
            if (cover_i > 0) {
                return "https://covers.openlibrary.org/b/id/" + cover_i + "-M.jpg";
            }
            return null;
        }
    }

    // ============================================================
    // RESPUESTA DE DETALLES DE OBRA ({key}.json)
    // ============================================================
    public static class WorkDetails {
        public String title;                    // Título de la obra
        public Object description;               // Descripción (puede ser String u objeto)
        public List<String> subjects;            // Lista de materias/géneros
        public List<Author> authors;             // Lista de autores
        public Created created;                   // Fecha de creación
        public LastModified last_modified;        // Fecha de última modificación
        public List<Integer> covers;              // Lista de IDs de portadas
        public String key;                         // Clave de la obra

        // Clase para la fecha de creación
        public static class Created {
            public String type;
            public String value;
        }

        // Clase para la fecha de modificación
        public static class LastModified {
            public String type;
            public String value;
        }

        // Clase para los autores
        public static class Author {
            public Key author;
            public Key type;

            public static class Key {
                public String key;
            }
        }

        // Método para obtener la descripción como String
        public String getDescriptionText() {
            if (description == null) return null;

            // Si description es un String directamente
            if (description instanceof String) {
                return (String) description;
            }

            // Si description es un objeto con campo "value"
            if (description instanceof Map) {
                Map<String, Object> descMap = (Map<String, Object>) description;
                Object value = descMap.get("value");
                if (value instanceof String) {
                    return (String) value;
                }
            }

            return null;
        }

        // Método para obtener la URL de la portada (de la lista de covers)
        public String getCoverUrl() {
            if (covers != null && !covers.isEmpty()) {
                return "https://covers.openlibrary.org/b/id/" + covers.get(0) + "-L.jpg";
            }
            return null;
        }
    }

    // ============================================================
    // CLASE VOLUMEINFO (para compatibilidad con el código existente)
    // ============================================================
    public static class VolumeInfo {
        public String title;                    // Título del libro
        public List<String> authors;             // Lista de autores
        public String publishedDate;             // Fecha de publicación
        public String description;                // Descripción (sinopsis)
        public String coverImage;                 // URL de la portada
        public List<String> isbns;                // Lista de ISBNs
        public String workKey;                     // Clave de la obra (para obtener más detalles)

        // Constructor para convertir desde Doc de Open Library (búsqueda)
        public VolumeInfo(Doc doc) {
            this.title = doc.title != null ? doc.title : doc.title_suggest;
            this.authors = doc.author_name;
            this.publishedDate = doc.first_publish_year > 0 ?
                    String.valueOf(doc.first_publish_year) : null;
            this.coverImage = doc.getCoverUrl();
            this.isbns = doc.isbn;
            this.workKey = doc.key;  // Guardamos la key para futuras consultas
            this.description = null;  // La sinopsis se cargará después

            Log.d("Book", "Creando VolumeInfo: " + this.title + " con key: " + this.workKey);
        }

        // Constructor para convertir desde WorkDetails (detalles)
        public VolumeInfo(WorkDetails details) {
            this.title = details.title;
            this.description = details.getDescriptionText();
            this.coverImage = details.getCoverUrl();
            this.workKey = details.key;

            // Extraer autores si existen
            if (details.authors != null && !details.authors.isEmpty()) {
                // Nota: Aquí necesitarías hacer otra llamada para obtener nombres de autores
                // Por simplicidad, lo dejamos vacío
            }
        }

        // Método para actualizar la sinopsis cuando se obtiene
        public void setDescription(String description) {
            this.description = description;
        }
    }

    // ============================================================
    // INTERFAZ DE LA API DE OPEN LIBRARY
    // ============================================================
    public interface Api {
        // Endpoint para búsqueda por título
        @GET("search.json")
        Call<BookResponse> search(
                @Query("title") String title,
                @Query("limit") int limit,
                @Query("fields") String fields
        );

        // Endpoint para obtener detalles de una obra específica
        @GET("{key}.json")
        Call<WorkDetails> getWorkDetails(
                @Path("key") String key
        );
    }

    // ============================================================
    // INSTANCIA DE LA API (Singleton)
    // ============================================================
    public static Api api = new Retrofit.Builder()
            .baseUrl("https://openlibrary.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(Api.class);
}