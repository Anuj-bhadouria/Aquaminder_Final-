package com.iar.myapplication;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            checkUserRole(currentUser.getUid());
        } else {
            startActivity(new Intent(MainActivity.this, RoleSelectionActivity.class));
            finish();
        }
    }

    private void checkUserRole(String userId) {
        mDatabase.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String userRole = snapshot.child("role").getValue(String.class);
                    if (userRole != null) {
                        if (userRole.equals("user")) {
                            startActivity(new Intent(MainActivity.this, Dashboard.class));
                        } else if (userRole.equals("plumber")) {
                            startActivity(new Intent(MainActivity.this, PlumberDashboardActivity.class));
                        }
                        finish();
                    }
                } else { //If user is authenticated but not in database, send to role selection
                    startActivity(new Intent(MainActivity.this, RoleSelectionActivity.class));
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                 //Could be an error, or the user doesn't exist in db
                 startActivity(new Intent(MainActivity.this, RoleSelectionActivity.class));
                 finish();
            }
        });
    }
}
