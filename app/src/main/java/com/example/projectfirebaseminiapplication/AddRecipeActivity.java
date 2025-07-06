package com.example.projectfirebaseminiapplication;

import android.content.Intent;
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
import com.example.projectfirebaseminiapplication.databinding.ActivityAddRecipeBinding;
import com.google.firebase.firestore.FirebaseFirestore;

public class AddRecipeActivity extends AppCompatActivity {

    private ActivityAddRecipeBinding binding;
    private Uri selectedImageUri;
    private FirebaseFirestore firestore;
    private ActivityResultLauncher<String> pickImageLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddRecipeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firestore = FirebaseFirestore.getInstance();

        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri = uri;
                        Glide.with(this).load(uri).into(binding.recipeImageView);
                    }
                });

        binding.selectImageButton.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        setupCategorySpinner();

        binding.addRecipeButton.setOnClickListener(v -> addRecipe());
    }

    private void setupCategorySpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.categories_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.categorySpinner.setAdapter(adapter);
    }

    private void addRecipe() {
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
        if (TextUtils.isEmpty(category) || category.equals("All")) {
            Toast.makeText(this, "يرجى اختيار الفئة", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedImageUri == null) {
            Toast.makeText(this, "يرجى اختيار صورة للوصفة", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.progressBar.setVisibility(android.view.View.VISIBLE);
        binding.addRecipeButton.setEnabled(false);

        uploadImageToCloudinary(selectedImageUri, new ImageUploadCallback() {
            @Override
            public void onSuccess(String imageUrl) {
                saveRecipeToFirestore(title, ingredients, steps, category, videoUrl, imageUrl);
            }

            @Override
            public void onFailure(String errorMessage) {
                binding.progressBar.setVisibility(android.view.View.GONE);
                binding.addRecipeButton.setEnabled(true);
                Toast.makeText(AddRecipeActivity.this, "فشل رفع الصورة: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    interface ImageUploadCallback {
        void onSuccess(String imageUrl);
        void onFailure(String errorMessage);
    }

    private void uploadImageToCloudinary(Uri imageUri, ImageUploadCallback callback) {
        // مؤقتا محاكاة رفع الصورة - استبدل هذا بالكود الحقيقي للرفع
        binding.recipeImageView.postDelayed(() -> {
            String fakeUrl = "https://res.cloudinary.com/ddsiz8xnl/image/upload/v1751532736/maqluba_tdiajq.jpg";
            callback.onSuccess(fakeUrl);
        }, 2000);
    }

    private void saveRecipeToFirestore(String title, String ingredients, String steps,
                                       String category, String videoUrl, String imageUrl) {
        String docId = firestore.collection("recipes").document().getId();

        Recipe recipe = new Recipe(docId, title, ingredients, steps, category, videoUrl, imageUrl);

        firestore.collection("recipes")
                .add(recipe)
                .addOnSuccessListener(documentReference  -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.addRecipeButton.setEnabled(true);
                    Toast.makeText(this, "تم إضافة الوصفة بنجاح", Toast.LENGTH_SHORT).show();

                    recipe.setDocumentId(documentReference.getId());

                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("newRecipe", recipe);
                    setResult(RESULT_OK, resultIntent);
                    finish();

                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(android.view.View.GONE);
                    binding.addRecipeButton.setEnabled(true);
                    Toast.makeText(this, "فشل إضافة الوصفة: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
