package com.wemmies.app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

/**
 * Displays the onboarding screen and signs users in with Google before entering the app.
 */
public class OnboardingActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001;
    private static final String TAG = "Wemmies";

    private Button btnGetStarted;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private GoogleSignInClient googleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            openSpillActivity();
            return;
        }

        setContentView(R.layout.activity_onboarding);

        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);

        btnGetStarted = findViewById(R.id.btnGetStarted);

        // Starts the Google Sign-In flow when the user taps Get Started.
        btnGetStarted.setOnClickListener(v -> {
            Intent signInIntent = googleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Handles the result returned from the Google Sign-In screen.
        if (requestCode == RC_SIGN_IN) {
            try {
                GoogleSignInAccount account = GoogleSignIn.getSignedInAccountFromIntent(data)
                        .getResult(ApiException.class);

                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Log.e(TAG, "Google sign-in failed", e);
                Toast.makeText(this, "Sign-in failed. Please try again.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        // Converts the Google ID token into a Firebase credential.
        auth.signInWithCredential(GoogleAuthProvider.getCredential(idToken, null))
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = authResult.getUser();

                    if (user != null) {
                        createOrUpdateUserDocument(user);
                    } else {
                        Toast.makeText(this, "Sign-in failed. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Firebase authentication failed", e);
                    Toast.makeText(this, "Sign-in failed. Please try again.", Toast.LENGTH_SHORT).show();
                });
    }

    private void createOrUpdateUserDocument(FirebaseUser user) {
        // Creates the user's Firestore profile without overwriting existing stats.
        Map<String, Object> userData = new HashMap<>();
        userData.put("displayName", user.getDisplayName() != null ? user.getDisplayName() : "Anonymous");
        userData.put("empathiesSent", 0L);
        userData.put("spillsMade", 0L);
        userData.put("monstersTamed", 0L);

        db.collection("users")
                .document(user.getUid())
                .set(userData, SetOptions.merge())
                .addOnSuccessListener(unused -> openSpillActivity())
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to create user document", e);
                    Toast.makeText(this, "Sign-in saved, but profile setup failed.", Toast.LENGTH_SHORT).show();
                    openSpillActivity();
                });
    }

    private void openSpillActivity() {
        // Opens the main community spill screen.
        Intent intent = new Intent(OnboardingActivity.this, SpillActivity.class);
        startActivity(intent);
        finish();
    }
}