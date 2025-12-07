package com.iar.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RequestDetailsActivity extends AppCompatActivity {


    private TextView tvUserName, tvUserContact, tvAddress, tvIssue;
    private ImageView ivIssueImage;
    private LinearLayout layoutPendingActions;
    private Button btnAccept, btnReject, btnComplete;


    private DatabaseReference mRequestDb;
    private String requestId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_details);


        requestId = getIntent().getStringExtra("requestId");

        if (requestId == null) {
            Toast.makeText(this, "Error: Request ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }


        tvUserName = findViewById(R.id.tv_user_name);
        tvUserContact = findViewById(R.id.tv_user_contact);
        tvAddress = findViewById(R.id.tv_request_address);
        tvIssue = findViewById(R.id.tv_issue_description);
        ivIssueImage = findViewById(R.id.iv_issue_image);

        layoutPendingActions = findViewById(R.id.layout_pending_actions);
        btnAccept = findViewById(R.id.btn_accept);
        btnReject = findViewById(R.id.btn_reject);
        btnComplete = findViewById(R.id.btn_complete_work);


        mRequestDb = FirebaseDatabase.getInstance().getReference("requests").child(requestId);


        loadRequestDetails();


        btnAccept.setOnClickListener(v -> updateStatus("ACCEPTED"));
        btnReject.setOnClickListener(v -> updateStatus("REJECTED"));
        btnComplete.setOnClickListener(v -> updateStatus("COMPLETED"));


        tvUserContact.setOnClickListener(v -> {
            String contactText = tvUserContact.getText().toString();

            String phone = contactText.replace("Contact: ", "").trim();
            if (!phone.isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + phone));
                startActivity(intent);
            }
        });
    }

    private void loadRequestDetails() {
        mRequestDb.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("userName").getValue(String.class);
                    String contact = snapshot.child("userContact").getValue(String.class);
                    String address = snapshot.child("address").getValue(String.class);
                    String issue = snapshot.child("issue").getValue(String.class);
                    String status = snapshot.child("status").getValue(String.class);
                    String imageUrl = snapshot.child("imageUrl").getValue(String.class);

                    tvUserName.setText("User: " + (name != null ? name : "N/A"));
                    tvUserContact.setText("Contact: " + (contact != null ? contact : "N/A"));
                    tvAddress.setText("Address: " + (address != null ? address : "N/A"));
                    tvIssue.setText("Issue: " + (issue != null ? issue : "N/A"));


                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        ivIssueImage.setVisibility(View.VISIBLE);

                    } else {
                        ivIssueImage.setVisibility(View.GONE);
                    }

                    updateUIBasedOnStatus(status);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(RequestDetailsActivity.this, "Failed to load details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUIBasedOnStatus(String status) {
        if (status == null) return;

        if (status.equals("PENDING")) {
            layoutPendingActions.setVisibility(View.VISIBLE);
            btnComplete.setVisibility(View.GONE);
        } else if (status.equals("ACCEPTED")) {
            layoutPendingActions.setVisibility(View.GONE);
            btnComplete.setVisibility(View.VISIBLE);
        } else if (status.equals("COMPLETED")) {
            Toast.makeText(this, "Job Completed", Toast.LENGTH_SHORT).show();
            finish();
        } else if (status.equals("REJECTED")) {
            finish();
        }
    }

    private void updateStatus(String newStatus) {
        mRequestDb.child("status").setValue(newStatus).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {


                if (newStatus.equals("COMPLETED")) {
                    incrementPlumberJobCount();
                } else {
                    Toast.makeText(RequestDetailsActivity.this, "Status: " + newStatus, Toast.LENGTH_SHORT).show();
                }

            } else {
                Toast.makeText(RequestDetailsActivity.this, "Update Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void incrementPlumberJobCount() {
        String currentPlumberId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference plumberRef = FirebaseDatabase.getInstance().getReference("users").child(currentPlumberId);

        plumberRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    long currentJobs = 0;
                    if (snapshot.child("jobsCompleted").exists()) {
                        // Firebase stores numbers as Long
                        try {
                            currentJobs = snapshot.child("jobsCompleted").getValue(Long.class);
                        } catch (Exception e) {
                        }
                    }


                    plumberRef.child("jobsCompleted").setValue(currentJobs + 1);
                    Toast.makeText(RequestDetailsActivity.this, "Job Completed & Count Updated!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }
}
