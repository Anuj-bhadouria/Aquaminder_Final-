package com.iar.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class PlumberRegistrationActivity extends AppCompatActivity {

    private EditText etName, etContact, etLocation, etExperience;
    private Button btnSubmit;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plumber_registration);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("plumbers");

        etName = findViewById(R.id.et_name);
        etContact = findViewById(R.id.et_contact);
        etLocation = findViewById(R.id.et_location);
        etExperience = findViewById(R.id.et_experience);
        btnSubmit = findViewById(R.id.btn_submit_profile);

        btnSubmit.setOnClickListener(v -> savePlumberProfile());
    }

    private void savePlumberProfile() {
        String name = etName.getText().toString().trim();
        String contact = etContact.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String experience = etExperience.getText().toString().trim();

        if (name.isEmpty() || contact.isEmpty() || location.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        
        if (userId == null) {
            Toast.makeText(this, "Error: User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> plumberData = new HashMap<>();
        plumberData.put("name", name);
        plumberData.put("contact", contact);
        plumberData.put("location", location);
        plumberData.put("experience", experience);
        plumberData.put("uid", userId);

        mDatabase.child(userId).setValue(plumberData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(PlumberRegistrationActivity.this, "Profile Created!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(PlumberRegistrationActivity.this, PlumberDashboardActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(PlumberRegistrationActivity.this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}