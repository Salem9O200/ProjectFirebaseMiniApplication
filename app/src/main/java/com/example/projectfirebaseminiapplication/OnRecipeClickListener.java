package com.example.projectfirebaseminiapplication;



public interface OnRecipeClickListener {

    void onItemClicked(Recipe recipe);
    void onEditClicked(Recipe recipe);
    void onDeleteClicked(Recipe recipe);
}