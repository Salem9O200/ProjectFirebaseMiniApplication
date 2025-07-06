package com.example.projectfirebaseminiapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.projectfirebaseminiapplication.databinding.ActivityHomeBinding;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity {

    private ActivityHomeBinding binding;
    private RecipeAdapter adapter;
    private ArrayList<Recipe> allRecipes = new ArrayList<>();
    private ArrayList<Recipe> filteredRecipes = new ArrayList<>();
    private String selectedCategory = "All";

    private FirebaseFirestore firestore;

    private final int ADD_RECIPE_REQUEST_CODE = 101; // بدل استخدام registerForActivityResult

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firestore = FirebaseFirestore.getInstance();

        binding.recipesRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new RecipeAdapter(allRecipes, new OnRecipeClickListener() {
            @Override
            public void onItemClicked(Recipe recipe) {
                Intent intent = new Intent(HomeActivity.this, RecipeDetailsActivity.class);
                intent.putExtra("recipe", recipe);
                startActivity(intent);
            }

            @Override
            public void onEditClicked(Recipe recipe) {
                Intent intent = new Intent(HomeActivity.this, EditRecipeActivity.class);
                intent.putExtra("recipeId", recipe.getDocumentId());
                startActivity(intent);
            }

            @Override
            public void onDeleteClicked(Recipe recipe) {
                firestore.collection("recipes").document(recipe.getDocumentId())
                        .delete()
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(HomeActivity.this, "تم حذف الوصفة", Toast.LENGTH_SHORT).show();
                            loadRecipes();
                        })
                        .addOnFailureListener(e -> Toast.makeText(HomeActivity.this, "فشل حذف الوصفة: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
        binding.recipesRecyclerView.setAdapter(adapter);

        setupTabs();

        loadRecipes();

        binding.fabAddRecipe.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, AddRecipeActivity.class);
            startActivityForResult(intent, ADD_RECIPE_REQUEST_CODE);
        });

        binding.fabProfile.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
        });

        binding.searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterRecipes(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterRecipes(newText);
                return false;
            }
        });

        binding.searchView.setOnCloseListener(() -> {
            filterRecipes("");
            return false;
        });
    }

    private void setupTabs() {
        String[] categories = {"All", "Appetizer", "Main Course", "Dessert", "Drinks"};

        for (String category : categories) {
            binding.tabLayout.addTab(binding.tabLayout.newTab().setText(category));
        }

        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                selectedCategory = tab.getText().toString();
                filterRecipes(binding.searchView.getQuery().toString());
            }

            @Override public void onTabUnselected(TabLayout.Tab tab) { }

            @Override public void onTabReselected(TabLayout.Tab tab) { }
        });
    }

    private void loadRecipes() {
        binding.progressBar.setVisibility(View.VISIBLE);

        firestore.collection("recipes")
                .get()
                .addOnCompleteListener(task -> {
                    binding.progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        allRecipes.clear();
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            for (DocumentSnapshot doc : querySnapshot) {
                                Recipe recipe = doc.toObject(Recipe.class);
                                if (recipe != null) {
                                    recipe.setDocumentId(doc.getId());
                                    allRecipes.add(recipe);
                                }
                            }
                            filterRecipes(binding.searchView.getQuery().toString());
                            showEmptyView(false);
                        } else {
                            showEmptyView(true);
                        }
                    } else {
                        Toast.makeText(HomeActivity.this, "فشل تحميل الوصفات", Toast.LENGTH_SHORT).show();
                        showEmptyView(true);
                    }
                });
    }

    private void filterRecipes(String query) {
        filteredRecipes.clear();

        if (TextUtils.isEmpty(query)) {
            if (selectedCategory.equals("All")) {
                filteredRecipes.addAll(allRecipes);
            } else {
                for (Recipe r : allRecipes) {
                    if (r.getCategory().equalsIgnoreCase(selectedCategory)) {
                        filteredRecipes.add(r);
                    }
                }
            }
        } else {
            String lowerQuery = query.toLowerCase();
            for (Recipe r : allRecipes) {
                boolean matchesCategory = selectedCategory.equals("All") || r.getCategory().equalsIgnoreCase(selectedCategory);
                boolean matchesQuery = r.getName().toLowerCase().contains(lowerQuery);
                if (matchesCategory && matchesQuery) {
                    filteredRecipes.add(r);
                }
            }
        }

        adapter.setRecipeList(filteredRecipes);
        showEmptyView(filteredRecipes.isEmpty());
    }

    private void showEmptyView(boolean show) {
        binding.emptyTextView.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.recipesRecyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    // استقبال وصفة جديدة من AddRecipeActivity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_RECIPE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Recipe newRecipe = (Recipe) data.getSerializableExtra("newRecipe");
            if (newRecipe != null) {
                allRecipes.add(0, newRecipe);
                filterRecipes(binding.searchView.getQuery().toString());
                Toast.makeText(this, "تمت إضافة وصفة جديدة: " + newRecipe.getName(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
