package com.iar.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPassword, etPhone, etAddress, etPincode, etServiceArea;
    private Button btnRegister;
    private TextView tvSwitchToLogin;
    private TextInputLayout tilServiceArea;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    private String role;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        role = getIntent().getStringExtra("role");

        etName = findViewById(R.id.et_name);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etPhone = findViewById(R.id.et_phone);
        etAddress = findViewById(R.id.et_address);
        etPincode = findViewById(R.id.et_pincode);
        etServiceArea = findViewById(R.id.et_service_area);
        tilServiceArea = findViewById(R.id.tilServiceArea);
        btnRegister = findViewById(R.id.btn_register);
        tvSwitchToLogin = findViewById(R.id.tvLogin);

        // Apply animations
        Animation fadeInUp = AnimationUtils.loadAnimation(this, R.anim.fade_in_up);
        findViewById(R.id.tvBrandName).startAnimation(fadeInUp);
        findViewById(R.id.tvCreateAccount).startAnimation(fadeInUp);
        findViewById(R.id.tvSubtitle).startAnimation(fadeInUp);
        findViewById(R.id.cardRegister).startAnimation(fadeInUp);
        findViewById(R.id.tvFooter).startAnimation(fadeInUp);


        if ("plumber".equals(role)) {
            if(tilServiceArea != null) {
                tilServiceArea.setVisibility(View.VISIBLE);
            } else {
                // Fallback if using old layout or ID mismatch (though layout updated)
                if(etServiceArea != null) etServiceArea.setVisibility(View.VISIBLE);
            }
        }

        btnRegister.setOnClickListener(v -> registerUser());

        tvSwitchToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            intent.putExtra("role", role);
            startActivity(intent);
        });
    }

    private void registerUser() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String pincode = etPincode.getText().toString().trim();
        String serviceArea = etServiceArea.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(phone) || TextUtils.isEmpty(address) || TextUtils.isEmpty(pincode)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if ("plumber".equals(role) && TextUtils.isEmpty(serviceArea)) {
            Toast.makeText(this, "Please enter service area", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        String userId = mAuth.getCurrentUser().getUid();
                        HashMap<String, Object> userData = new HashMap<>();
                        userData.put("name", name);
                        userData.put("email", email);
                        userData.put("phone", phone);
                        userData.put("address", address);
                        userData.put("pincode", pincode);
                        userData.put("role", role);
                        if ("plumber".equals(role)) {
                            userData.put("serviceArea", serviceArea);
                        }

                        mDatabase.child("users").child(userId).setValue(userData)
                                .addOnCompleteListener(taskDb -> {
                                    if (taskDb.isSuccessful()) {
                                        Toast.makeText(RegisterActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
                                        // Redirect to appropriate dashboard
                                        if(role.equals("user")){
                                            startActivity(new Intent(RegisterActivity.this, Dashboard.class));
                                        } else {
                                            startActivity(new Intent(RegisterActivity.this, PlumberDashboardActivity.class));
                                        }
                                        finish();
                                    } else {
                                        Toast.makeText(RegisterActivity.this, "Database error: " + taskDb.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(RegisterActivity.this, "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}