package com.iar.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;



public class RoleSelectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_role_selection);

        // Use 'View' or 'CardView', NOT 'Button'
        View btnUser = findViewById(R.id.btnUser);
        View btnPlumber = findViewById(R.id.btnPlumber);

        btnUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to User Login/Register
                Intent intent = new Intent(RoleSelectionActivity.this, LoginActivity.class);
                intent.putExtra("role", "user");
                startActivity(intent);
            }
        });

        btnPlumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to Plumber Login/Register
                Intent intent = new Intent(RoleSelectionActivity.this, LoginActivity.class);
                intent.putExtra("role", "plumber");
                startActivity(intent);
            }
        });
    }
}