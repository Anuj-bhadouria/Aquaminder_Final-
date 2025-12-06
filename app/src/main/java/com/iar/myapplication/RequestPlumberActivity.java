package com.iar.myapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.UUID;

public class RequestPlumberActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText etDescription, etAddress;
    private ImageView ivIssueImage;
    private Button btnSelectImage, btnSubmit;
    private Uri imageUri;

    private String plumberId;
    private DatabaseReference mDatabase;
    private StorageReference mStorage;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_plumber);

        etDescription = findViewById(R.id.et_issue_description);
        etAddress = findViewById(R.id.et_address);
        ivIssueImage = findViewById(R.id.iv_issue_image);
        btnSelectImage = findViewById(R.id.btn_select_image);
        btnSubmit = findViewById(R.id.btn_submit_request);

        plumberId = getIntent().getStringExtra("PLUMBER_UID");
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("requests");
        mStorage = FirebaseStorage.getInstance().getReference("request_images");

        btnSelectImage.setOnClickListener(v -> openFileChooser());
        btnSubmit.setOnClickListener(v -> submitRequest());
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            ivIssueImage.setImageURI(imageUri);
        }
    }

    private void submitRequest() {
        String description = etDescription.getText().toString().trim();
        String address = etAddress.getText().toString().trim();

        if (description.isEmpty() || address.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (imageUri != null) {
            uploadImageAndSaveRequest(description, address);
        } else {
            saveRequestToDatabase(description, address, null);
        }
    }

    private void uploadImageAndSaveRequest(String description, String address) {
        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Uploading...");
        pd.show();

        String imageName = UUID.randomUUID().toString();
        StorageReference imageRef = mStorage.child(imageName);

        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    pd.dismiss();
                    saveRequestToDatabase(description, address, uri.toString());
                }))
                .addOnFailureListener(e -> {
                    pd.dismiss();
                    Toast.makeText(RequestPlumberActivity.this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void saveRequestToDatabase(String description, String address, String imageUrl) {
        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "anonymous";
        String requestId = mDatabase.push().getKey();

        Request request = new Request(requestId, userId, plumberId, description, imageUrl, "PENDING", address, null);

        if (requestId != null) {
            mDatabase.child(requestId).setValue(request)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(RequestPlumberActivity.this, "Request sent successfully!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(RequestPlumberActivity.this, "Failed to send request.", Toast.LENGTH_SHORT).show());
        }
    }
}