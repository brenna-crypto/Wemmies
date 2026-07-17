package com.wemmies.app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.wemmies.app.model.Wemmie;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executor;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";

    private static final String[] ADJECTIVES = {
            "Calm", "Gentle", "Silent", "Peaceful", "Warm", "Brave", "Wise",
            "Kind", "Cozy", "Soft", "Serene", "Mild", "Quiet", "Bright"
    };

    private static final String[] NOUNS = {
            "Panda", "Forest", "Cloud", "Fox", "Whale", "Wave", "Star",
            "Moon", "River", "Breeze", "Owl", "Koala", "Deer", "Meadow"
    };

    private static final String[] PICKABLE_EMOJIS = {
            "🐼", "🦊", "🐋", "🌸", "🌙", "🌊", "⭐", "🍃", "🦉", "🐨", "🦌", "🍁", "🎈", "☀️",
            "🐱", "🐶", "🦁", "🦄", "🌻", "🍀", "🌈", "🔥", "🔮", "🧸", "🎨", "🚀"
    };

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private ScrollView profileContent;
    private LinearLayout lockOverlay;
    private Button btnUnlock;

    private TextView tvProfileAvatar;
    private TextInputLayout tilNickname;
    private EditText etNickname;
    private Button btnReroll;
    private Button btnSaveNickname;
    private ImageView ivProviderIcon;

    private TextView tvSpills;
    private TextView tvEmpathies;
    private TextView tvMonsters;
    private Button btnSignOut;

    private RecyclerView rvMyWemmies;
    private final List<Wemmie> myWemmies = new ArrayList<>();
    private SpillAdapter myWemmiesAdapter;

    private boolean isUnlocked = false;
    private String currentNicknameFromServer = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        profileContent = findViewById(R.id.profileContent);
        lockOverlay = findViewById(R.id.lockOverlay);
        btnUnlock = findViewById(R.id.btnUnlock);

        tvProfileAvatar = findViewById(R.id.tvProfileAvatar);
        tilNickname = findViewById(R.id.tilNickname);
        etNickname = findViewById(R.id.etNickname);
        btnReroll = findViewById(R.id.btnReroll);
        btnSaveNickname = findViewById(R.id.btnSaveNickname);
        ivProviderIcon = findViewById(R.id.ivProviderIcon);

        tvSpills = findViewById(R.id.tvSpills);
        tvEmpathies = findViewById(R.id.tvEmpathies);
        tvMonsters = findViewById(R.id.tvMonsters);
        btnSignOut = findViewById(R.id.btnSignOut);
        rvMyWemmies = findViewById(R.id.rvMyWemmies);

        // Bottom Navigation setup
        findViewById(R.id.navSpill).setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, SpillActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        });

        findViewById(R.id.navFeed).setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, FeedActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        });

        findViewById(R.id.navProfile).setOnClickListener(v -> {
            // Already here
        });

        TextView tvNavText = findViewById(R.id.tvNavProfileText);
        tvNavText.setTextColor(getResources().getColor(R.color.wemmie_cyan, getTheme()));

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            showFeedback("Please sign in first.");
            finish();
            return;
        }

        // Set provider icon (Google vs Anonymous)
        boolean isAnonymous = true;
        for (com.google.firebase.auth.UserInfo info : currentUser.getProviderData()) {
            if ("google.com".equals(info.getProviderId())) {
                isAnonymous = false;
                break;
            }
        }

        if (isAnonymous) {
            ivProviderIcon.setImageResource(R.drawable.ic_anonymous);
        } else {
            ivProviderIcon.setImageResource(R.drawable.ic_google);
        }

        // RecyclerView setup for personal Wemmies
        rvMyWemmies.setLayoutManager(new LinearLayoutManager(this));
        myWemmiesAdapter = new SpillAdapter(myWemmies, wemmie -> {
            Intent intent = new Intent(ProfileActivity.this, MyWemmieDetailActivity.class);
            intent.putExtra("wemmie", wemmie);
            startActivity(intent);
        });
        rvMyWemmies.setAdapter(myWemmiesAdapter);

        // Stats real-time updates - Passing 'this' makes it lifecycle-aware
        db.collection("users")
                .document(currentUser.getUid())
                .addSnapshotListener(this, (snapshot, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Failed to load user stats", error);
                        return;
                    }

                    if (snapshot != null && snapshot.exists()) {
                        String nickname = snapshot.getString("avatarNickname");
                        currentNicknameFromServer = nickname != null ? nickname : "";
                        String emoji = snapshot.getString("avatarEmoji");
                        Long spills = snapshot.getLong("spillsMade");
                        Long empathies = snapshot.getLong("empathiesSent");
                        Long monsters = snapshot.getLong("monstersTamed");

                        boolean hasDisplayName = snapshot.contains("displayName");
                        boolean hasEmail = snapshot.contains("email");

                        if (nickname == null || emoji == null || hasDisplayName || hasEmail) {
                            // Automatically migrate old accounts by generating nickname/emoji and deleting legacy fields
                            Map<String, Object> updates = new HashMap<>();
                            if (nickname == null) {
                                updates.put("avatarNickname", generateRandomNickname());
                            }
                            if (emoji == null) {
                                updates.put("avatarEmoji", getRandomDefaultEmoji());
                            }
                            if (hasDisplayName) {
                                updates.put("displayName", com.google.firebase.firestore.FieldValue.delete());
                            }
                            if (hasEmail) {
                                updates.put("email", com.google.firebase.firestore.FieldValue.delete());
                            }

                            db.collection("users")
                                    .document(currentUser.getUid())
                                    .update(updates)
                                    .addOnFailureListener(e -> Log.e(TAG, "Failed to self-migrate profile", e));
                        }

                        if (nickname != null) etNickname.setText(nickname);
                        if (emoji != null) tvProfileAvatar.setText(emoji);

                        tvSpills.setText("📝 Wemmies Shared: " + (spills == null ? 0 : spills));
                        tvEmpathies.setText("❤️ Empathies Sent: " + (empathies == null ? 0 : empathies));
                        tvMonsters.setText("✨ Monsters Tamed: " + (monsters == null ? 0 : monsters));
                    }
                });

        // Load personal Wemmies (sorted by database using composite index)
        // Passing 'this' ensures listener stops when activity is finished (e.g. on sign out)
        db.collection("wemmies")
                .whereEqualTo("userId", currentUser.getUid())
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener(this, (snapshots, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Failed to load my wemmies", error);
                        return;
                    }

                    if (snapshots != null) {
                        // Optimizing main thread usage: Parse documents into a local temp list first
                        List<Wemmie> newList = new ArrayList<>();
                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            Wemmie w = doc.toObject(Wemmie.class);
                            if (w != null) {
                                w.setId(doc.getId());
                                newList.add(w);
                            }
                        }
                        // Update RecyclerView adapter via DiffUtil callback to run granular animations
                        myWemmiesAdapter.updateData(newList);
                    }
                });

        // Emoji selection click listener
        tvProfileAvatar.setOnClickListener(v -> showEmojiPickerDialog(currentUser));

        // Reroll random nickname listener
        btnReroll.setOnClickListener(v -> etNickname.setText(generateRandomNickname()));

        // Save custom nickname listener
        btnSaveNickname.setOnClickListener(v -> {
            String newNickname = etNickname.getText().toString().trim();
            
            // 1. Basic validation
            if (newNickname.isEmpty()) {
                tilNickname.setError("Nickname cannot be empty");
                return;
            }

            // 2. Optimization: Don't save if it hasn't changed
            if (newNickname.equals(currentNicknameFromServer)) {
                showFeedback("Nickname is already up to date!");
                return;
            }

            tilNickname.setError(null);
            btnSaveNickname.setEnabled(false); // Disable to prevent double-taps
            validateAndSaveNickname(currentUser, newNickname);
        });

        // Biometric / PIN validation
        btnUnlock.setOnClickListener(v -> checkBiometricAndPrompt());
        checkBiometricAndPrompt();

        btnSignOut.setOnClickListener(v -> {
            auth.signOut();
            Intent intent = new Intent(ProfileActivity.this, OnboardingActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void checkBiometricAndPrompt() {
        BiometricManager biometricManager = BiometricManager.from(this);
        int authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.DEVICE_CREDENTIAL;
        int canAuthenticate = biometricManager.canAuthenticate(authenticators);

        if (canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS) {
            showBiometricPrompt(authenticators);
        } else {
            // Secure lock is not configured on the device (common in emulators).
            // Warn the user with Snackbar but unlock the profile.
            showFeedback("Screen lock not configured. Unlocking profile for testing.");
            revealProfile();
        }
    }

    private void showBiometricPrompt(int authenticators) {
        Executor executor = ContextCompat.getMainExecutor(this);
        BiometricPrompt biometricPrompt = new BiometricPrompt(this, executor,
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                        showFeedback("Authentication error: " + errString);
                        // Navigate away to Feed if cancelled or errored
                        if (!isUnlocked) {
                            navigateToFeed();
                        }
                    }

                    @Override
                    public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        revealProfile();
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                        showFeedback("Authentication failed. Please try again.");
                    }
                });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Unlock Profile")
                .setSubtitle("Authenticate to view your stats and feed history")
                .setAllowedAuthenticators(authenticators)
                .build();

        biometricPrompt.authenticate(promptInfo);
    }

    private void revealProfile() {
        isUnlocked = true;
        lockOverlay.setVisibility(View.GONE);
        profileContent.setVisibility(View.VISIBLE);
    }

    private void navigateToFeed() {
        Intent intent = new Intent(ProfileActivity.this, FeedActivity.class);
        startActivity(intent);
        finish();
    }

    private void showEmojiPickerDialog(FirebaseUser user) {
        GridView gridView = new GridView(this);
        gridView.setNumColumns(4);
        gridView.setPadding(32, 32, 32, 32);
        gridView.setVerticalSpacing(16);
        gridView.setHorizontalSpacing(16);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, PICKABLE_EMOJIS) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                TextView textView = (TextView) super.getView(position, convertView, parent);
                textView.setGravity(Gravity.CENTER);
                textView.setTextSize(28);
                textView.setPadding(8, 8, 8, 8);
                return textView;
            }
        };

        gridView.setAdapter(adapter);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Choose an Avatar Emoji")
                .setView(gridView)
                .setNegativeButton("Cancel", null)
                .create();

        gridView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedEmoji = PICKABLE_EMOJIS[position];
            tvProfileAvatar.setText(selectedEmoji);

            db.collection("users")
                    .document(user.getUid())
                    .update("avatarEmoji", selectedEmoji)
                    .addOnSuccessListener(unused -> showFeedback("Avatar emoji updated! 🌟"))
                    .addOnFailureListener(e -> showFeedback("Failed to update avatar."));

            dialog.dismiss();
        });

        dialog.show();
    }

    private void validateAndSaveNickname(FirebaseUser user, String newNickname) {
        db.collection("users")
                .whereEqualTo("avatarNickname", newNickname)
                .limit(1) // Optimization: Stop searching after finding one match
                .get()
                .addOnCompleteListener(task -> {
                    btnSaveNickname.setEnabled(true); // Re-enable button
                    
                    if (task.isSuccessful() && task.getResult() != null) {
                        boolean isTaken = false;
                        for (DocumentSnapshot doc : task.getResult()) {
                            if (!doc.getId().equals(user.getUid())) {
                                isTaken = true;
                            }
                        }

                        if (!isTaken) {
                            saveNickname(user.getUid(), newNickname);
                        } else {
                            tilNickname.setError("This nickname is already taken! 😔");
                        }
                    } else {
                        showFeedback("Error checking nickname. Try again.");
                    }
                });
    }

    private void saveNickname(String userId, String name) {
        db.collection("users").document(userId)
                .update("avatarNickname", name)
                .addOnSuccessListener(a -> showFeedback("Nickname updated! 🌟"))
                .addOnFailureListener(e -> showFeedback("Failed to save."));
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
        return PICKABLE_EMOJIS[random.nextInt(PICKABLE_EMOJIS.length)];
    }

    private void showFeedback(String message) {
        if (!isFinishing() && !isDestroyed()) {
            Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show();
        }
    }
}