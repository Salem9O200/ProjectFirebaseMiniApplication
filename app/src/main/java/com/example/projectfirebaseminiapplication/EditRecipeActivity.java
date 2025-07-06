package com.example.projectfirebaseminiapplication;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.projectfirebaseminiapplication.databinding.ActivityEditRecipeBinding;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EditRecipeActivity extends AppCompatActivity {

    private ActivityEditRecipeBinding binding;

    private Uri selectedImageUri = null;
    private Recipe currentRecipe;

    private FirebaseFirestore firestore;

    private List<String> categoriesList = new ArrayList<>();

    private ActivityResultLauncher<String> pickImageLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditRecipeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firestore = FirebaseFirestore.getInstance();

        currentRecipe = (Recipe) getIntent().getSerializableExtra("recipe");
        if (currentRecipe == null) {
            Toast.makeText(this, "حدث خطأ في تحميل بيانات الوصفة", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri = uri;
                        Glide.with(this).load(uri).into(binding.recipeImageView);
                    }
                });

        binding.selectImageButton.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        loadCategories();

        populateFields();

        binding.updateRecipeButton.setOnClickListener(v -> updateRecipe());
    }

    private void loadCategories() {
        firestore.collection("recipes")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Set<String> categoriesSet = new HashSet<>();
                    categoriesSet.add("Select category");
                    for (DocumentSnapshot doc : querySnapshot) {
                        String cat = doc.getString("category");
                        if (!TextUtils.isEmpty(cat)) {
                            categoriesSet.add(cat);
                        }
                    }
                    categoriesList.clear();
                    categoriesList.addAll(categoriesSet);

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            this, android.R.layout.simple_spinner_item, categoriesList);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    binding.categorySpinner.setAdapter(adapter);

                    // اختيار الفئة الحالية
                    int pos = categoriesList.indexOf(currentRecipe.getCategory());
                    if (pos >= 0) {
                        binding.categorySpinner.setSelection(pos);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "فشل تحميل الفئات: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void populateFields() {
        binding.titleEditText.setText(currentRecipe.getName());
        binding.ingredientsEditText.setText(currentRecipe.getIngredients());
        binding.stepsEditText.setText(currentRecipe.getSteps());
        binding.videoUrlEditText.setText(currentRecipe.getVideoUrl());
        Glide.with(this).load(currentRecipe.getImageUrl()).into(binding.recipeImageView);
    }

    private void updateRecipe() {
        String title = binding.titleEditText.getText().toString().trim();
        String ingredients = binding.ingredientsEditText.getText().toString().trim();
        String steps = binding.stepsEditText.getText().toString().trim();
        String category = binding.categorySpinner.getSelectedItem() != null
                ? binding.categorySpinner.getSelectedItem().toString() : "";
        String videoUrl = binding.videoUrlEditText.getText().toString().trim();

        if (TextUtils.isEmpty(title)) {
            binding.titleEditText.setError("يرجى إدخال عنوان الوصفة");
            binding.titleEditText.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(ingredients)) {
            binding.ingredientsEditText.setError("يرجى إدخال المكونات");
            binding.ingredientsEditText.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(steps)) {
            binding.stepsEditText.setError("يرجى إدخال خطوات الوصفة");
            binding.stepsEditText.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(category) || category.equals("Select category")) {
            Toast.makeText(this, "يرجى اختيار الفئة", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.updateRecipeButton.setEnabled(false);

        if (selectedImageUri != null) {
            uploadImageToCloudinary(selectedImageUri, new ImageUploadCallback() {
                @Override
                public void onSuccess(String imageUrl) {
                    saveUpdatedRecipe(title, ingredients, steps, category, videoUrl, imageUrl);
                }

                @Override
                public void onFailure(String errorMessage) {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.updateRecipeButton.setEnabled(true);
                    Toast.makeText(EditRecipeActivity.this, "فشل رفع الصورة: " + errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // استخدم الصورة القديمة
            saveUpdatedRecipe(title, ingredients, steps, category, videoUrl, currentRecipe.getImageUrl());
        }
    }

    interface ImageUploadCallback {
        void onSuccess(String imageUrl);
        void onFailure(String errorMessage);
    }

    private void uploadImageToCloudinary(Uri imageUri, ImageUploadCallback callback) {
        binding.recipeImageView.postDelayed(() -> {
            String fakeUrl = "https://res.cloudinary.com/ddsiz8xnl/image/upload/v1751532736/maqluba_tdiajq.jpg";
            callback.onSuccess(fakeUrl);
        }, 2000);
    }

    private void saveUpdatedRecipe(String title, String ingredients, String steps,
                                   String category, String videoUrl, String imageUrl) {
        String docId = currentRecipe.getDocumentId();

        Recipe updatedRecipe = new Recipe(docId, title, ingredients, steps, category, videoUrl, imageUrl);

        firestore.collection("recipes").document(docId)
                .set(updatedRecipe)
                .addOnSuccessListener(aVoid -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.updateRecipeButton.setEnabled(true);
                    Toast.makeText(this, "تم تحديث الوصفة بنجاح", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.updateRecipeButton.setEnabled(true);
                    Toast.makeText(this, "فشل تحديث الوصفة: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
