package com.iar.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
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
    private PlumberAdapter adapter;
    private List<Plumber> plumberList;
    private List<Plumber> filteredList;
    private DatabaseReference mUsersDb;
    private EditText searchField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plumbers_list);


        rvPlumbers = findViewById(R.id.rv_plumbers);
        rvPlumbers.setLayoutManager(new LinearLayoutManager(this));
        searchField = findViewById(R.id.search_plumbers);


        plumberList = new ArrayList<>();
        filteredList = new ArrayList<>();
        adapter = new PlumberAdapter(filteredList);
        rvPlumbers.setAdapter(adapter);


        mUsersDb = FirebaseDatabase.getInstance().getReference("users");


        fetchPlumbers();


        searchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        setupBottomNavigation();
    }

    private void fetchPlumbers() {
        String currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid();

        mUsersDb.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                plumberList.clear();

                for (DataSnapshot data : snapshot.getChildren()) {
                    String role = data.child("role").getValue(String.class);
                    String name = data.child("name").getValue(String.class);
                    String uid = data.getKey();


                    if ("plumber".equals(role) && uid != null && !uid.equals(currentUserId)) {

                        String phone = data.child("phone").getValue(String.class);
                        String rating = "0.0";
                        if (data.child("rating").exists()) {
                            rating = String.valueOf(data.child("rating").getValue());
                        }

                        String jobs = "0 jobs";
                        if (data.child("jobsCompleted").exists()) {
                            jobs = data.child("jobsCompleted").getValue() + " jobs";
                        }

                        plumberList.add(new Plumber(uid, name, "Available", rating, jobs, phone));
                    }
                }
                filter(searchField.getText().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }


    private void filter(String text) {
        filteredList.clear();
        if (text.isEmpty()) {
            filteredList.addAll(plumberList);
        } else {
            text = text.toLowerCase();
            for (Plumber item : plumberList) {
                if (item.name != null && item.name.toLowerCase().contains(text)) {
                    filteredList.add(item);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    // --- Navigation Setup ---
    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(getApplicationContext(), Dashboard.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_book) {
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
        bottomNavigationView.setSelectedItemId(R.id.nav_book);
    }

    // --- Internal Class for Plumber Data ---
    public static class Plumber {
        public String uid, name, status, rating, jobs, phone;

        public Plumber(String uid, String name, String status, String rating, String jobs, String phone) {
            this.uid = uid;
            this.name = name;
            this.status = status;
            this.rating = rating;
            this.jobs = jobs;
            this.phone = phone;
        }
    }


    private class PlumberAdapter extends RecyclerView.Adapter<PlumberAdapter.ViewHolder> {
        private List<Plumber> list;

        public PlumberAdapter(List<Plumber> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // Use your item layout (make sure you created item_plumber.xml)
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_plumber, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Plumber p = list.get(position);
            holder.tvName.setText(p.name);
            holder.tvRating.setText(p.rating + " • " + p.jobs);

            holder.itemView.setOnClickListener(v -> {

                Intent intent = new Intent(PlumbersListActivity.this, PlumberProfileActivity.class);
                intent.putExtra("plumberId", p.uid);
                intent.putExtra("plumberName", p.name);
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvRating;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);

                tvName = itemView.findViewById(R.id.tv_plumber_name);
                tvRating = itemView.findViewById(R.id.tv_plumber_rate);
                if(tvRating == null){
                    
                }
            }
        }
    }
}
