package com.example.projectfirebaseminiapplication;

import java.io.Serializable;

public class Recipe implements Serializable {
    private String documentId;
    private String name;
    private String ingredients;
    private String steps;
    private String category;
    private String videoUrl;
    private String imageUrl;

    public Recipe() {
    }

    public Recipe(String documentId, String name, String ingredients, String steps, String category, String videoUrl, String imageUrl) {
        this.documentId = documentId;
        this.name = name;
        this.ingredients = ingredients;
        this.steps = steps;
        this.category = category;
        this.videoUrl = videoUrl;
        this.imageUrl = imageUrl;

    }

    // getters & setters

    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getIngredients() { return ingredients; }
    public void setIngredients(String ingredients) { this.ingredients = ingredients; }

    public String getSteps() { return steps; }
    public void setSteps(String steps) { this.steps = steps; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }


}
