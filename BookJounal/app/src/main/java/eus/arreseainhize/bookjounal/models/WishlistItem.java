// models/WishlistItem.java
package eus.arreseainhize.bookjounal.models;

import java.util.List;

public class WishlistItem {
    private String id;
    private String title;
    private List<String> authors;
    private String coverImage;
    private String userId;

    public WishlistItem() {}

    public WishlistItem(String title, List<String> authors, String coverImage, String userId) {
        this.title = title;
        this.authors = authors;
        this.coverImage = coverImage;
        this.userId = userId;
    }

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public List<String> getAuthors() { return authors; }
    public void setAuthors(List<String> authors) { this.authors = authors; }

    public String getCoverImage() { return coverImage; }
    public void setCoverImage(String coverImage) { this.coverImage = coverImage; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}