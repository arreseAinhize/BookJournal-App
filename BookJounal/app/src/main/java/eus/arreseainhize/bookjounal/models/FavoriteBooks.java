package eus.arreseainhize.bookjounal.models;

public class FavoriteBooks {
    private String documentId; // ID del documento en Firestore

    // Datos del libro
    private String title;
    private String author;
    private String imageUrl;
    private String bookId; // workKey de Open Library

    // Datos de la lectura
    private int rating;

    // Metadatos
    private String userId;

    public FavoriteBooks() {
    }

    public FavoriteBooks(String title, String author, String imageUrl, String bookId) {
        this.title = title;
        this.author = author;
        this.imageUrl = imageUrl;
        this.bookId = bookId;
    }

    // Getters y Setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }


    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }
}
