package com.iar.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class PlumbersListActivity extends AppCompatActivity {

    private RecyclerView rvPlumbers;
    private PlumberAdapter plumberAdapter;
    private List<Plumber> plumberList = new ArrayList<>();
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plumbers_list);

        rvPlumbers = findViewById(R.id.rv_plumbers);
        rvPlumbers.setLayoutManager(new LinearLayoutManager(this));

        plumberAdapter = new PlumberAdapter(plumberList, plumber -> {
            Intent intent = new Intent(PlumbersListActivity.this, PlumberProfileActivity.class);
            intent.putExtra("PLUMBER_UID", plumber.getUid());
            startActivity(intent);
        });

        rvPlumbers.setAdapter(plumberAdapter);

        mDatabase = FirebaseDatabase.getInstance().getReference("users");
        fetchPlumbers();
        setupBottomNavigation();
    }

    private void fetchPlumbers() {
        mDatabase.orderByChild("role").equalTo("plumber").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                plumberList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Plumber plumber = snapshot.getValue(Plumber.class);
                    if (plumber != null && plumber.isAvailable()) {
                        plumber.setUid(snapshot.getKey());
                        plumberList.add(plumber);
                    }
                }
                plumberAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(PlumbersListActivity.this, "Failed to load plumbers.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_book);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(PlumbersListActivity.this, Dashboard.class));
                return true;
            } else if (itemId == R.id.nav_book) {
                startActivity(new Intent(PlumbersListActivity.this, PlumbersListActivity.class));
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(PlumbersListActivity.this,  ProfileActivity.class));

            }

            return false;
        });
    }
}