package com.iar.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
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

public class PlumberDashboardActivity extends AppCompatActivity implements RequestAdapter.OnRequestActionListener {

    private RecyclerView rvRequests;
    private RequestAdapter requestAdapter;
    private List<Request> requestList = new ArrayList<>();
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plumber_dashboard);

        mAuth = FirebaseAuth.getInstance();
        String plumberId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;

        if (plumberId == null) {
            Toast.makeText(this, "Error: Not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(plumberId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String role = snapshot.child("role").getValue(String.class);
                    if (!"plumber".equals(role)) {
                        Toast.makeText(PlumberDashboardActivity.this, "Unauthorized access", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(PlumberDashboardActivity.this, LoginActivity.class));
                        finish();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        ImageView ivProfile = findViewById(R.id.iv_profile);
        ivProfile.setOnClickListener(v -> startActivity(new Intent(PlumberDashboardActivity.this, ProfileActivity.class)));

        Button btnOpenCamera = findViewById(R.id.btn_open_camera);
        btnOpenCamera.setOnClickListener(v -> {
            Intent intent = new Intent(PlumberDashboardActivity.this, OtgCameraActivity.class);
            startActivity(intent);
        });

        TextView tvWelcome = findViewById(R.id.tv_welcome);

        rvRequests = findViewById(R.id.rv_requests);
        rvRequests.setLayoutManager(new LinearLayoutManager(this));

        requestAdapter = new RequestAdapter(requestList, request -> {
            Intent intent = new Intent(PlumberDashboardActivity.this, RequestDetailsActivity.class);
            intent.putExtra("REQUEST_ID", request.getRequestId());
            startActivity(intent);
        });
        requestAdapter.setOnRequestActionListener(this);
        rvRequests.setAdapter(requestAdapter);

        mDatabase = FirebaseDatabase.getInstance().getReference("requests");
        fetchRequests(plumberId);
        setupBottomNavigation();
    }

    private void fetchRequests(String plumberId) {
        mDatabase.orderByChild("plumberId").equalTo(plumberId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                requestList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Request request = snapshot.getValue(Request.class);
                    if (request != null) {
                        request.setRequestId(snapshot.getKey());
                        requestList.add(request);
                    }
                }
                requestAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(PlumberDashboardActivity.this, "Failed to load requests.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation_plumber);
        bottomNav.setSelectedItemId(R.id.nav_plumber_dashboard);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_plumber_dashboard) {
                return true;
            } else if (itemId == R.id.nav_incoming_requests) {
                return true;
            } else if (itemId == R.id.nav_plumber_history) {
                startActivity(new Intent(PlumberDashboardActivity.this, PlumberHistoryActivity.class));
                return true;
            } else if (itemId == R.id.nav_plumber_profile) {
                startActivity(new Intent(PlumberDashboardActivity.this, ProfileActivity.class));
                return true;
            }
            return false;
        });
    }

    @Override
    public void onAccept(Request request) {
        updateRequestStatus(request, "ACCEPTED");
    }

    @Override
    public void onReject(Request request) {
        updateRequestStatus(request, "REJECTED");
    }

    private void updateRequestStatus(Request request, String status) {
        mDatabase.child(request.getRequestId()).child("status").setValue(status)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Request " + status.toLowerCase(), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Failed to update status", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}