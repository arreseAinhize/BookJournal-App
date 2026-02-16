package eus.arreseainhize.bookjounal.models;

import java.io.Serializable;

public class ReadingLog implements Serializable {
    private String documentId; // ID del documento en Firestore

    // Datos del libro
    private String title;
    private String author;
    private String isbn;
    private String genre;
    private String imageUrl;
    private String bookId; // workKey de Open Library

    // Datos de la lectura
    private String startDate;
    private String endDate;
    private String lectureType;
    private int rating;
    private String notes;

    // Metadatos
    private String userId;
    private long timestamp;

    public ReadingLog() {
    }

    public ReadingLog(String title, String author, String isbn, String genre,
                      String imageUrl, String bookId) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.genre = genre;
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

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
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

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getLectureType() {
        return lectureType;
    }

    public void setLectureType(String lectureType) {
        this.lectureType = lectureType;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }
}