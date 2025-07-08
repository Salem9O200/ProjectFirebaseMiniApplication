package com.example.projectfirebaseminiapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.cloudinary.Configuration;
import com.cloudinary.android.MediaManager;
import com.example.projectfirebaseminiapplication.databinding.ActivityLoginBinding;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Locale;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private FirebaseAuth firebaseAuth;
    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "loginPrefs";




    private ActivityResultLauncher<Intent> registerLauncher;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        CloudinaryHelper.init(this);
        FirebaseApp.initializeApp(this);
        firebaseAuth = FirebaseAuth.getInstance();
        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);



        if (sharedPreferences.getBoolean("rememberMe", false)) {
            String savedEmail = sharedPreferences.getString("email", "");
            String savedPassword = sharedPreferences.getString("password", "");
            binding.emailEditText.setText(savedEmail);
            binding.passwordEditText.setText(savedPassword);
            binding.rememberMeCheckBox.setChecked(true);
        }

        registerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        String email = result.getData().getStringExtra("email");
                        String password = result.getData().getStringExtra("password");
                        binding.emailEditText.setText(email);
                        binding.passwordEditText.setText(password);
                    }
                }
        );

        binding.loginButton.setOnClickListener(v -> loginUser());


        binding.registerRedirect.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            registerLauncher.launch(intent);
        });
    }

    private void loginUser() {
        String email = binding.emailEditText.getText().toString().trim();
        String password = binding.passwordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            binding.emailInputLayout.setError("يرجى إدخال البريد الإلكتروني");
            return;
        } else {
            binding.emailInputLayout.setError(null);
        }

        if (TextUtils.isEmpty(password)) {
            binding.passwordInputLayout.setError("يرجى إدخال كلمة المرور");
            return;
        } else {
            binding.passwordInputLayout.setError(null);
        }

        binding.progressBar.setVisibility(View.VISIBLE);

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    binding.progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        // حفظ بيانات الدخول إذا تم تفعيل "تذكرني"
                        if (binding.rememberMeCheckBox.isChecked()) {
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("email", email);
                            editor.putString("password", password);
                            editor.putBoolean("rememberMe", true);
                            editor.apply();
                        } else {
                            sharedPreferences.edit().clear().apply();
                        }

                        Toast.makeText(this, "تم تسجيل الدخول بنجاح", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(this, "فشل تسجيل الدخول: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
