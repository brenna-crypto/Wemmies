package com.wemmies.app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Displays the onboarding screen and signs users in with Google or Anonymously before entering the app.
 */
public class OnboardingActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001;
    private static final String TAG = "Wemmies";

    private static final String[] ADJECTIVES = {
            "Calm", "Gentle", "Silent", "Peaceful", "Warm", "Brave", "Wise",
            "Kind", "Cozy", "Soft", "Serene", "Mild", "Quiet", "Bright"
    };

    private static final String[] NOUNS = {
            "Panda", "Forest", "Cloud", "Fox", "Whale", "Wave", "Star",
            "Moon", "River", "Breeze", "Owl", "Koala", "Deer", "Meadow"
    };

    private static final String[] DEFAULT_EMOJIS = {
            "🐼", "🦊", "🐋", "🌸", "🌙", "🌊", "⭐", "🍃", "🦉", "🐨", "🦌", "🍁", "🎈", "☀️"
    };

    private Button btnGoogleSignIn;
    private Button btnAnonymousSignIn;
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
            openFeedActivity();
            return;
        }

        setContentView(R.layout.activity_onboarding);

        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);

        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn);
        btnAnonymousSignIn = findViewById(R.id.btnAnonymousSignIn);

        // Starts the Google Sign-In flow when the user taps Google Sign In.
        btnGoogleSignIn.setOnClickListener(v -> {
            setSigningIn(true);
            Intent signInIntent = googleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });

        // Starts the Anonymous Sign-In flow.
        btnAnonymousSignIn.setOnClickListener(v -> {
            setSigningIn(true);
            auth.signInAnonymously()
                    .addOnSuccessListener(authResult -> {
                        FirebaseUser user = authResult.getUser();
                        if (user != null) {
                            createOrUpdateUserDocument(user);
                        } else {
                            setSigningIn(false);
                            showError("Sign-in failed. Please try again.");
                        }
                    })
                    .addOnFailureListener(e -> {
                        setSigningIn(false);
                        Log.e(TAG, "Anonymous authentication failed", e);
                        showError("Anonymous authentication failed. Please try again.");
                    });
        });
    }

    private void setSigningIn(boolean isSigningIn) {
        if (btnGoogleSignIn != null) btnGoogleSignIn.setEnabled(!isSigningIn);
        if (btnAnonymousSignIn != null) {
            btnAnonymousSignIn.setEnabled(!isSigningIn);
            if (isSigningIn) {
                btnAnonymousSignIn.setText("SIGNING IN...");
            } else {
                btnAnonymousSignIn.setText("CONTINUE ANONYMOUSLY");
            }
        }
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
                setSigningIn(false);
                Log.e(TAG, "Google sign-in failed", e);
                showError("Google sign-in failed. Please try again.");
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
                        setSigningIn(false);
                        showError("Sign-in failed. Please try again.");
                    }
                })
                .addOnFailureListener(e -> {
                    setSigningIn(false);
                    Log.e(TAG, "Firebase authentication failed", e);
                    showError("Firebase authentication failed. Please try again.");
                });
    }

    private void createOrUpdateUserDocument(FirebaseUser user) {
        // Check if user already has a document to preserve stats and nickname/emoji
        db.collection("users")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (isFinishing() || isDestroyed()) return;

                    if (documentSnapshot.exists()) {
                        // User exists. Clean up displayName and email if they exist in the document.
                        boolean hasNickname = documentSnapshot.contains("avatarNickname");
                        boolean hasEmoji = documentSnapshot.contains("avatarEmoji");

                        if (hasNickname && hasEmoji) {
                            // Nickname and emoji are already set. Just delete legacy fields.
                            Map<String, Object> updates = new HashMap<>();
                            updates.put("displayName", com.google.firebase.firestore.FieldValue.delete());
                            updates.put("email", com.google.firebase.firestore.FieldValue.delete());

                            db.collection("users")
                                    .document(user.getUid())
                                    .update(updates)
                                    .addOnSuccessListener(unused -> {
                                        if (!isFinishing() && !isDestroyed()) openFeedActivity();
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Failed to clean legacy user fields", e);
                                        if (!isFinishing() && !isDestroyed()) openFeedActivity();
                                    });
                        } else {
                            // Missing nickname or emoji. Generate them and verify unique.
                            String nickname = generateRandomNickname();
                            String emoji = getRandomDefaultEmoji();
                            checkNicknameAndUpdate(user, nickname, emoji, 0);
                        }
                    } else {
                        // Brand new user profile. Initialize all stats to 0.
                        String nickname = generateRandomNickname();
                        String emoji = getRandomDefaultEmoji();
                        checkNicknameAndSave(user, nickname, emoji, 0);
                    }
                })
                .addOnFailureListener(e -> {
                    if (isFinishing() || isDestroyed()) return;
                    Log.e(TAG, "Failed to check user document", e);
                    saveUserProfile(user, generateRandomNickname(), getRandomDefaultEmoji());
                });
    }

    private void checkNicknameAndUpdate(FirebaseUser user, String nickname, String emoji, int attempt) {
        if (attempt > 10) {
            saveUserUpdates(user, nickname, emoji);
            return;
        }

        db.collection("users")
                .whereEqualTo("avatarNickname", nickname)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (isFinishing() || isDestroyed()) return;
                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                        // Collision! Generate a new name and try again
                        checkNicknameAndUpdate(user, generateRandomNickname(), emoji, attempt + 1);
                    } else {
                        saveUserUpdates(user, nickname, emoji);
                    }
                })
                .addOnFailureListener(e -> {
                    if (isFinishing() || isDestroyed()) return;
                    saveUserUpdates(user, nickname, emoji);
                });
    }

    private void saveUserUpdates(FirebaseUser user, String nickname, String emoji) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("avatarNickname", nickname);
        updates.put("avatarEmoji", emoji);
        updates.put("displayName", com.google.firebase.firestore.FieldValue.delete());
        updates.put("email", com.google.firebase.firestore.FieldValue.delete());

        db.collection("users")
                .document(user.getUid())
                .update(updates)
                .addOnSuccessListener(unused -> {
                    if (!isFinishing() && !isDestroyed()) openFeedActivity();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to update legacy user doc", e);
                    if (!isFinishing() && !isDestroyed()) openFeedActivity();
                });
    }

    private void checkNicknameAndSave(FirebaseUser user, String nickname, String emoji, int attempt) {
        if (attempt > 10) {
            saveUserProfile(user, nickname, emoji);
            return;
        }

        db.collection("users")
                .whereEqualTo("avatarNickname", nickname)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (isFinishing() || isDestroyed()) return;
                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                        // Collision! Generate a new name and try again
                        checkNicknameAndSave(user, generateRandomNickname(), emoji, attempt + 1);
                    } else {
                        saveUserProfile(user, nickname, emoji);
                    }
                })
                .addOnFailureListener(e -> {
                    if (isFinishing() || isDestroyed()) return;
                    saveUserProfile(user, nickname, emoji);
                });
    }

    private void saveUserProfile(FirebaseUser user, String nickname, String emoji) {
        // Creates a brand new user's Firestore profile, initializing stats to 0.
        Map<String, Object> userData = new HashMap<>();
        userData.put("avatarNickname", nickname);
        userData.put("avatarEmoji", emoji);
        userData.put("empathiesSent", 0L);
        userData.put("spillsMade", 0L);
        userData.put("monstersTamed", 0L);

        db.collection("users")
                .document(user.getUid())
                .set(userData, SetOptions.merge())
                .addOnSuccessListener(unused -> {
                    if (!isFinishing() && !isDestroyed()) openFeedActivity();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to create user document", e);
                    if (!isFinishing() && !isDestroyed()) openFeedActivity();
                });
    }

    private String generateRandomNickname() {
        Random random = new Random();
        String adj = ADJECTIVES[random.nextInt(ADJECTIVES.length)];
        String noun = NOUNS[random.nextInt(NOUNS.length)];
        int num = random.nextInt(1000);
        return adj + " " + noun + " " + num;
    }

    private String getRandomDefaultEmoji() {
        Random random = new Random();
        return DEFAULT_EMOJIS[random.nextInt(DEFAULT_EMOJIS.length)];
    }

    private void openFeedActivity() {
        // Opens the main community feed screen.
        Intent intent = new Intent(OnboardingActivity.this, FeedActivity.class);
        startActivity(intent);
        finish();
    }

    private void showError(String message) {
        android.view.View root = findViewById(android.R.id.content);
        if (!isFinishing() && !isDestroyed() && root != null) {
            Snackbar.make(root, message, Snackbar.LENGTH_LONG).show();
        }
    }
}
