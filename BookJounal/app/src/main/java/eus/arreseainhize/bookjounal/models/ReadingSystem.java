package eus.arreseainhize.bookjounal.models;

import java.util.HashMap;
import java.util.Map;

public class ReadingSystem {
    private String star1Value;
    private String star2Value;
    private String star3Value;
    private String star4Value;
    private String star5Value;
    private long updatedAt;
    private String userId;

    public ReadingSystem() {
        // Constructor vacío necesario para Firebase
    }

    public ReadingSystem(String userId) {
        this.userId = userId;
        this.star1Value = "";
        this.star2Value = "";
        this.star3Value = "";
        this.star4Value = "";
        this.star5Value = "";
        this.updatedAt = System.currentTimeMillis();
    }

    // Getters y setters
    // Getters y setters
    public String getStar1Value() { return star1Value; }
    public void setStar1Value(String star1Value) { this.star1Value = star1Value; }

    public String getStar2Value() { return star2Value; }
    public void setStar2Value(String star2Value) { this.star2Value = star2Value; }

    public String getStar3Value() { return star3Value; }
    public void setStar3Value(String star3Value) { this.star3Value = star3Value; }

    public String getStar4Value() { return star4Value; }
    public void setStar4Value(String star4Value) { this.star4Value = star4Value; }

    public String getStar5Value() { return star5Value; }
    public void setStar5Value(String star5Value) { this.star5Value = star5Value; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    // Método para convertir a Map (útil para guardar en Firebase)
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("star1Value", star1Value);
        map.put("star2Value", star2Value);
        map.put("star3Value", star3Value);
        map.put("star4Value", star4Value);
        map.put("star5Value", star5Value);
        map.put("updatedAt", updatedAt);
        map.put("userId", userId);
        return map;
    }
}
