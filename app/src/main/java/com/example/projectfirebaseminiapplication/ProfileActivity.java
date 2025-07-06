package com.example.projectfirebaseminiapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.projectfirebaseminiapplication.databinding.ActivityProfileBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;
    private ArrayList<Recipe> allRecipes = new ArrayList<>();
    private ArrayList<Recipe> filteredRecipes = new ArrayList<>();
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private RecipeAdapter adapter;
    private static final int ADD_RECIPE_REQUEST_CODE = 101;

    private ArrayList<Recipe> userRecipes;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        userRecipes = new ArrayList<>();
        binding.progressBar.setVisibility(android.view.View.VISIBLE);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            firestore.collection("users").document(currentUser.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("name");
                            String email = documentSnapshot.getString("email");
                            String imageUrl = documentSnapshot.getString("imageUrl");

                            binding.userNameTextView.setText(name != null ? name : "No Name");
                            binding.userEmailTextView.setText(email != null ? email : "No Email");

                            Glide.with(this)
                                    .load(imageUrl)
                                    .placeholder(R.drawable.ic_profile)
                                    .error(R.drawable.ic_profile)
                                    .circleCrop()
                                    .into(binding.userImageView);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "فشل في تحميل بيانات المستخدم", Toast.LENGTH_SHORT).show();
                    });

            adapter = new RecipeAdapter(userRecipes, new OnRecipeClickListener() {
                @Override
                public void onItemClicked(Recipe recipe) {
                    Intent intent = new Intent(getApplicationContext(), RecipeDetailsActivity.class);
                    intent.putExtra("recipe", recipe);
                    startActivity(intent);
                }

            });

            binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
            binding.recyclerView.setAdapter(adapter);

            loadUserRecipes();
        }

        binding.logoutIcon.setOnClickListener(v -> {
            mAuth.signOut();
            Toast.makeText(this, "تم تسجيل الخروج بنجاح", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
            finish();
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        loadUserRecipes();
    }

    private void loadUserRecipes() {
        binding.progressBar.setVisibility(View.VISIBLE);
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        firestore.collection("recipes")
                .whereEqualTo("userId", currentUserId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    userRecipes.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Recipe recipe = document.toObject(Recipe.class);
                        recipe.setDocumentId(document.getId());
                        userRecipes.add(recipe);
                    }
                    adapter.notifyDataSetChanged();
                    binding.progressBar.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "فشل في تحميل الوصفات", Toast.LENGTH_SHORT).show();
                    binding.progressBar.setVisibility(View.GONE);
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_RECIPE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Recipe newRecipe = (Recipe) data.getSerializableExtra("newRecipe");
            if (newRecipe != null) {
                allRecipes.add(0, newRecipe);
                Toast.makeText(this, "تمت إضافة وصفة جديدة: " + newRecipe.getName(), Toast.LENGTH_SHORT).show();
            }
        }
    }


}
