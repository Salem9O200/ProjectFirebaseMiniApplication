package com.example.projectfirebaseminiapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.projectfirebaseminiapplication.databinding.ActivityRecipeDetailsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class RecipeDetailsActivity extends AppCompatActivity {

    private ActivityRecipeDetailsBinding binding;
    private Recipe recipe;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRecipeDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        recipe = (Recipe) getIntent().getSerializableExtra("recipe");
        if (recipe == null) {
            Toast.makeText(this, "فشل تحميل تفاصيل الوصفة", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupUI();
        setupListeners();
    }

    private void setupUI() {
        binding.screenTitleTextView.setText("تفاصيل الوصفة");

        // تحميل الصورة
        Glide.with(this)
                .load(recipe.getImageUrl())
                .placeholder(R.drawable.ic_profile)
                .into(binding.recipeImageView);

        binding.recipeNameTextView.setText(recipe.getName());
        binding.categoryTextView.setText("الفئة: " + recipe.getCategory());

        String ingredientsFormatted = formatCommaSeparatedText(recipe.getIngredients());
        binding.ingredientsTextView.setText("المكونات:\n" + ingredientsFormatted);

        String stepsFormatted = formatCommaSeparatedText(recipe.getSteps());
        binding.stepsTextView.setText("الخطوات:\n" + stepsFormatted);

        if (!TextUtils.isEmpty(recipe.getVideoUrl())) {
            binding.videoUrlTextView.setText("رابط الفيديو");
            binding.videoUrlTextView.setVisibility(View.VISIBLE);
            binding.videoUrlTextView.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
            binding.videoUrlTextView.setPaintFlags(binding.videoUrlTextView.getPaintFlags() | android.graphics.Paint.UNDERLINE_TEXT_FLAG);

            binding.videoUrlTextView.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(recipe.getVideoUrl()));
                intent.setPackage("com.google.android.youtube");
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                } else {
                    Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(recipe.getVideoUrl()));
                    startActivity(webIntent);
                }
            });

        } else {
            binding.videoUrlTextView.setVisibility(View.GONE);
        }


        String currentUserId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "";
        boolean isOwner = true;

        if (isOwner) {
            binding.editDeleteLayout.setVisibility(View.VISIBLE);
        } else {
            binding.editDeleteLayout.setVisibility(View.GONE);
        }
    }

    private String formatCommaSeparatedText(String text) {
        if (TextUtils.isEmpty(text)) return "";

        String[] parts = text.split(",");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            builder.append("• ").append(part.trim()).append("\n");
        }
        return builder.toString();
    }

    private void setupListeners() {
        binding.videoUrlTextView.setOnClickListener(v -> {
            String url = recipe.getVideoUrl();
            if (!TextUtils.isEmpty(url)) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
            }
        });

        binding.editIcon.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditRecipeActivity.class);
            intent.putExtra("recipe", recipe);
            startActivity(intent);
            finish();
        });

        binding.deleteIcon.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("حذف الوصفة")
                    .setMessage("هل أنت متأكد من حذف هذه الوصفة؟")
                    .setPositiveButton("نعم", (dialog, which) -> deleteRecipe())
                    .setNegativeButton("لا", null)
                    .show();
        });
    }

    private void deleteRecipe() {
        binding.progressBar.setVisibility(View.VISIBLE);
        firestore.collection("recipes").document(recipe.getDocumentId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "تم حذف الوصفة بنجاح", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "فشل حذف الوصفة: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
