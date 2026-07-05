package com.wemmies.app;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;

/**
 * Displays the signed-in user's profile, progress stats, and profile photo.
 */
public class ProfileActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1001;
    private static final int REQUEST_CAMERA_PERMISSION = 1002;
    private static final String PREFS_NAME = "wemmies_profile";
    private static final String PROFILE_PHOTO_KEY = "profile_photo_base64";

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private TextView tvName;
    private TextView tvEmail;
    private TextView tvSpills;
    private TextView tvEmpathies;
    private TextView tvMonsters;
    private TextView btnBackToSpill;
    private Button btnSignOut;
    private ImageView ivProfilePhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        tvName = findViewById(R.id.tvName);
        tvEmail = findViewById(R.id.tvEmail);
        tvSpills = findViewById(R.id.tvSpills);
        tvEmpathies = findViewById(R.id.tvEmpathies);
        tvMonsters = findViewById(R.id.tvMonsters);
        btnBackToSpill = findViewById(R.id.btnBackToSpill);
        btnSignOut = findViewById(R.id.btnSignOut);
        ivProfilePhoto = findViewById(R.id.ivProfilePhoto);

        btnBackToSpill.setOnClickListener(v -> finish());

        loadSavedProfilePhoto();

        ivProfilePhoto.setOnClickListener(v -> openCameraWithPermission());

        FirebaseUser user = auth.getCurrentUser();

        if (user == null) {
            Toast.makeText(this, "Please sign in first.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvName.setText("Hi, " + user.getDisplayName());
        tvEmail.setText(user.getEmail());

        db.collection("users")
                .document(user.getUid())
                .addSnapshotListener((snapshot, error) -> {
                    if (snapshot != null && snapshot.exists()) {
                        Long spills = snapshot.getLong("spillsMade");
                        Long empathies = snapshot.getLong("empathiesSent");
                        Long monsters = snapshot.getLong("monstersTamed");

                        tvSpills.setText("📝 Wemmies Shared: " + (spills == null ? 0 : spills));
                        tvEmpathies.setText("❤️ Empathies Sent: " + (empathies == null ? 0 : empathies));
                        tvMonsters.setText("✨ Monsters Tamed: " + (monsters == null ? 0 : monsters));
                    }
                });

        btnSignOut.setOnClickListener(v -> {
            auth.signOut();

            Intent intent = new Intent(ProfileActivity.this, OnboardingActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void openCameraWithPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION
            );
        }
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
        } else {
            Toast.makeText(this, "Camera not available.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String[] permissions,
            int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Camera permission denied.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            @Nullable Intent data
    ) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && data != null) {
            Bundle extras = data.getExtras();

            if (extras != null) {
                Bitmap imageBitmap = (Bitmap) extras.get("data");

                if (imageBitmap != null) {
                    ivProfilePhoto.setImageBitmap(imageBitmap);
                    saveProfilePhoto(imageBitmap);
                }
            }
        }
    }

    private void saveProfilePhoto(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);

        String encodedImage = Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putString(PROFILE_PHOTO_KEY, encodedImage).apply();
    }

    private void loadSavedProfilePhoto() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String encodedImage = prefs.getString(PROFILE_PHOTO_KEY, null);

        if (encodedImage != null) {
            byte[] imageBytes = Base64.decode(encodedImage, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            ivProfilePhoto.setImageBitmap(bitmap);
        }
    }
}