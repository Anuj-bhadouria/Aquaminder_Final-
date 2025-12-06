package com.iar.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class PlumberHistoryActivity extends AppCompatActivity {

    private RecyclerView rvHistory;
    private RequestAdapter requestAdapter;
    private List<Request> historyList = new ArrayList<>();
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plumber_history);

        mAuth = FirebaseAuth.getInstance();
        String plumberId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;

        if (plumberId == null) {
            Toast.makeText(this, "Error: Not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        rvHistory = findViewById(R.id.rv_history);
        rvHistory.setLayoutManager(new LinearLayoutManager(this));

        requestAdapter = new RequestAdapter(historyList, request -> {
            // You can add a details view for past requests here if you want
        });
        rvHistory.setAdapter(requestAdapter);

        mDatabase = FirebaseDatabase.getInstance().getReference("requests");
        fetchHistory(plumberId);
        setupBottomNavigation();
    }

    private void fetchHistory(String plumberId) {
        mDatabase.orderByChild("plumberId").equalTo(plumberId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                historyList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Request request = snapshot.getValue(Request.class);
                    // Assuming 'COMPLETED' or 'CANCELLED' requests are history
                    if (request != null && (request.getStatus().equals("COMPLETED") || request.getStatus().equals("CANCELLED"))) {
                        request.setRequestId(snapshot.getKey());
                        historyList.add(request);
                    }
                }
                requestAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(PlumberHistoryActivity.this, "Failed to load history.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation_plumber);
        bottomNav.setSelectedItemId(R.id.nav_plumber_history);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_plumber_dashboard) {
                startActivity(new Intent(PlumberHistoryActivity.this, PlumberDashboardActivity.class));
                return true;
            } else if (itemId == R.id.nav_incoming_requests) {
                startActivity(new Intent(PlumberHistoryActivity.this, PlumberDashboardActivity.class));
                return true;
            } else if (itemId == R.id.nav_plumber_history) {
                // Already here
                return true;
            } else if (itemId == R.id.nav_plumber_profile) {
                startActivity(new Intent(PlumberHistoryActivity.this, ProfileActivity.class));
                return true;
            }
            return false;
        });
    }
}