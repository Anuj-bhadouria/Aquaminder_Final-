package com.iar.myapplication;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RequestDetailsActivity extends AppCompatActivity {

    private TextView tvUserName, tvUserContact, tvRequestAddress, tvIssueDescription;
    private ImageView ivIssueImage;
    private DatabaseReference mRequestDb, mUserDb;
    private String requestId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_details);

        tvUserName = findViewById(R.id.tv_user_name);
        tvUserContact = findViewById(R.id.tv_user_contact);
        tvRequestAddress = findViewById(R.id.tv_request_address);
        tvIssueDescription = findViewById(R.id.tv_issue_description);
        ivIssueImage = findViewById(R.id.iv_issue_image);

        requestId = getIntent().getStringExtra("REQUEST_ID");

        if (requestId == null) {
            Toast.makeText(this, "Error: Invalid Request", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        mRequestDb = FirebaseDatabase.getInstance().getReference("requests").child(requestId);
        mUserDb = FirebaseDatabase.getInstance().getReference("users");

        fetchRequestDetails();
    }

    private void fetchRequestDetails() {
        mRequestDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Request request = snapshot.getValue(Request.class);
                if (request != null) {
                    tvRequestAddress.setText("Address: " + request.getAddress());
                    tvIssueDescription.setText("Issue: " + request.getIssueDescription());

                    mUserDb.child(request.getUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                            User user = userSnapshot.getValue(User.class);
                            if (user != null) {
                                tvUserName.setText("User: " + user.getName());
                                tvUserContact.setText("Contact: " + user.getPhone());
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(RequestDetailsActivity.this, "Failed to load user details", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(RequestDetailsActivity.this, "Failed to load request details", Toast.LENGTH_SHORT).show();
            }
        });
    }
}