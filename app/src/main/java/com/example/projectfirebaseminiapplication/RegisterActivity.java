package com.example.projectfirebaseminiapplication;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.example.projectfirebaseminiapplication.databinding.ActivityRegisterBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    private Uri imageUri;
    private String imageUrl = "";

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // إعداد الدول
        String[] countries = {"فلسطين", "الأردن", "مصر", "سوريا", "لبنان"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, countries);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.countrySpinner.setAdapter(adapter);


        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        imageUri = result.getData().getData();
                        binding.userImageView.setImageURI(imageUri);
                    }
                }
        );

        binding.userImageView.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });

        binding.registerButton.setOnClickListener(v -> registerUser());

        binding.loginText.setOnClickListener(v -> finish());
    }

    private void registerUser() {
        String name = binding.nameEditText.getText().toString().trim();
        String email = binding.emailEditText.getText().toString().trim();
        String password = binding.passwordEditText.getText().toString().trim();
        String country = binding.countrySpinner.getSelectedItem().toString();

        if (TextUtils.isEmpty(name)) {
            binding.txFName.setError("يرجى إدخال الاسم");
            return;
        } else {
            binding.txFName.setError(null);
        }

        if (TextUtils.isEmpty(email)) {
            binding.TxFEmail.setError("يرجى إدخال البريد الإلكتروني");
            return;
        } else {
            binding.TxFEmail.setError(null);
        }

        if (TextUtils.isEmpty(password)) {
            binding.TxFPassword.setError("يرجى إدخال كلمة المرور");
            return;
        } else {
            binding.TxFPassword.setError(null);
        }

        if (imageUri == null) {
            Toast.makeText(this, "يرجى اختيار صورة", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);

        // رفع الصورة إلى Cloudinary
        MediaManager.get().upload(imageUri)
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {}

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {}

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        Log.d("CloudinaryResponse", resultData.toString());

                        if (resultData.get("secure_url") != null) {
                            imageUrl = resultData.get("secure_url").toString();
                            createFirebaseAccount(name, email, password, country, imageUrl);
                        } else {
                            Toast.makeText(RegisterActivity.this, "فشل الحصول على رابط الصورة", Toast.LENGTH_SHORT).show();
                        }
                        createFirebaseAccount(name, email, password, country, imageUrl);
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        binding.progressBar.setVisibility(View.GONE);
                        Toast.makeText(RegisterActivity.this, "فشل رفع الصورة: " + error.getDescription(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {}
                })
                .dispatch();
    }

    private void createFirebaseAccount(String name, String email, String password, String country, String imageUrl) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String userId = firebaseAuth.getCurrentUser().getUid();
                        Map<String, Object> user = new HashMap<>();
                        user.put("name", name);
                        user.put("email", email);
                        user.put("country", country);
                        user.put("imageUrl", imageUrl);

                        firestore.collection("users").document(userId)
                                .set(user)
                                .addOnSuccessListener(aVoid -> {
                                    binding.progressBar.setVisibility(View.GONE);
                                    Toast.makeText(RegisterActivity.this, "تم إنشاء الحساب بنجاح", Toast.LENGTH_SHORT).show();

                                    Intent resultIntent = new Intent();
                                    resultIntent.putExtra("email", email);
                                    resultIntent.putExtra("password", password);
                                    setResult(RESULT_OK, resultIntent);
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    binding.progressBar.setVisibility(View.GONE);
                                    Toast.makeText(RegisterActivity.this, "فشل حفظ البيانات: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        binding.progressBar.setVisibility(View.GONE);
                        Toast.makeText(RegisterActivity.this, "فشل إنشاء الحساب: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
