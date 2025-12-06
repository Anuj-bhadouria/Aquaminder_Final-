package com.iar.myapplication;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class PlumberProfileActivity extends AppCompatActivity {

    private TextView tvName, tvLocation, tvContact, tvExperience;
    private Button btnRequest, btnCall, btnRate;
    private DatabaseReference mDatabase;
    private String plumberUid;
    private String plumberPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plumber_profile);

        tvName = findViewById(R.id.tv_plumber_profile_name);
        tvLocation = findViewById(R.id.tv_plumber_profile_location);
        tvContact = findViewById(R.id.tv_plumber_profile_contact);
        tvExperience = findViewById(R.id.tv_plumber_profile_experience);
        btnRequest = findViewById(R.id.btn_request_plumber);
        btnCall = findViewById(R.id.btn_call_plumber);
        btnRate = findViewById(R.id.btn_rate_plumber);

        plumberUid = getIntent().getStringExtra("PLUMBER_UID");

        if (plumberUid == null) {
            Toast.makeText(this, "Error: Invalid Plumber", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        mDatabase = FirebaseDatabase.getInstance().getReference("users").child(plumberUid);
        fetchPlumberDetails();

        btnRequest.setOnClickListener(v -> {
            Intent intent = new Intent(PlumberProfileActivity.this, RequestPlumberActivity.class);
            intent.putExtra("PLUMBER_UID", plumberUid);
            startActivity(intent);
        });

        btnCall.setOnClickListener(v -> {
            if (plumberPhone != null && !plumberPhone.isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + plumberPhone));
                startActivity(intent);
            }
        });

        btnRate.setOnClickListener(v -> showRatingDialog());

        setupBottomNavigation();
    }

    private void fetchPlumberDetails() {
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Plumber plumber = snapshot.getValue(Plumber.class);
                if (plumber != null) {
                    tvName.setText(plumber.getName());
                    tvLocation.setText("Service Area: " + plumber.getServiceArea());
                    tvContact.setText("Contact: " + plumber.getPhone());
                    tvExperience.setText("Address: " + plumber.getAddress());
                    plumberPhone = plumber.getPhone();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PlumberProfileActivity.this, "Failed to load details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation_plumber);
        // We don't have a dedicated menu item for this screen, so we won't set it as selected.
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_plumber_dashboard) {
                startActivity(new Intent(PlumberProfileActivity.this, PlumberDashboardActivity.class));
                return true;
            } else if (itemId == R.id.nav_incoming_requests) {
                startActivity(new Intent(PlumberProfileActivity.this, PlumberDashboardActivity.class));
                return true;
            } else if (itemId == R.id.nav_plumber_history) {
                startActivity(new Intent(PlumberProfileActivity.this, PlumberHistoryActivity.class));
                return true;
            } else if (itemId == R.id.nav_plumber_profile) {
                startActivity(new Intent(PlumberProfileActivity.this, ProfileActivity.class));
                return true;
            }
            return false;
        });
    }

    private void showRatingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_rate_plumber, null);
        builder.setView(dialogView);

        final RatingBar ratingBar = dialogView.findViewById(R.id.rating_bar);
        final Button btnSubmit = dialogView.findViewById(R.id.btn_submit_rating);

        AlertDialog dialog = builder.create();

        btnSubmit.setOnClickListener(v -> {
            float rating = ratingBar.getRating();
            updatePlumberRating(rating);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void updatePlumberRating(float rating) {
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Plumber plumber = snapshot.getValue(Plumber.class);
                if (plumber != null) {
                    double currentRating = plumber.getRating();
                    int numRatings = plumber.getNumRatings();
                    double newRating = ((currentRating * numRatings) + rating) / (numRatings + 1);

                    Map<String, Object> updates = new HashMap<>();
                    updates.put("rating", newRating);
                    updates.put("numRatings", numRatings + 1);

                    mDatabase.updateChildren(updates).addOnCompleteListener(task -> {
                        if(task.isSuccessful()){
                            Toast.makeText(PlumberProfileActivity.this, "Rating submitted!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(PlumberProfileActivity.this, "Failed to submit rating.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PlumberProfileActivity.this, "Failed to update rating.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}