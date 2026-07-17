package com.wemmies.app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.wemmies.app.model.Wemmie;

import java.util.ArrayList;
import java.util.List;

public class FeedActivity extends AppCompatActivity {

    private static final String TAG = "FeedActivity";

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private final List<Wemmie> communitySpills = new ArrayList<>();
    private SpillAdapter spillAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        RecyclerView rvCommunitySpills = findViewById(R.id.rvCommunitySpills);

        // Set up Bottom Navigation listeners
        findViewById(R.id.navSpill).setOnClickListener(v -> {
            Intent intent = new Intent(FeedActivity.this, SpillActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        });

        findViewById(R.id.navFeed).setOnClickListener(v -> {
            // Already here
        });

        findViewById(R.id.navProfile).setOnClickListener(v -> {
            Intent intent = new Intent(FeedActivity.this, ProfileActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        });

        // Highlight Active Tab
        TextView tvNavText = findViewById(R.id.tvNavFeedText);
        tvNavText.setTextColor(getResources().getColor(R.color.wemmie_cyan, getTheme()));

        rvCommunitySpills.setLayoutManager(new LinearLayoutManager(this));

        spillAdapter = new SpillAdapter(communitySpills, wemmie -> {
            FirebaseUser currentUser = auth.getCurrentUser();
            Intent intent;
            // Route to MyWemmieDetailActivity if the wemmie belongs to the current user,
            // otherwise route to WemmieDetailActivity.
            if (currentUser != null && wemmie.getUserId() != null && wemmie.getUserId().equals(currentUser.getUid())) {
                intent = new Intent(this, MyWemmieDetailActivity.class);
            } else {
                intent = new Intent(this, WemmieDetailActivity.class);
            }
            intent.putExtra("wemmie", wemmie);
            startActivity(intent);
        });

        rvCommunitySpills.setAdapter(spillAdapter);

        // Check authentication state to avoid permission denied errors due to initialization latency
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            startFirestoreListeners();
        } else {
            auth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
                @Override
                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    if (user != null) {
                        auth.removeAuthStateListener(this);
                        startFirestoreListeners();
                    }
                }
            });
        }
    }

    private void startFirestoreListeners() {
        // Load community Wemmies in real time (ordered database-side by empathyCount)
        // Passing 'this' (Activity) makes the listener lifecycle-aware, automatically stopping on destroy.
        db.collection("wemmies")
                .orderBy("empathyCount", Query.Direction.DESCENDING)
                .addSnapshotListener(this, (snapshots, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Failed to load Wemmies", error);
                        showFeedback("Failed to load Wemmies.");
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
                        spillAdapter.updateData(newList);
                    }
                });
    }

    private void showFeedback(String message) {
        if (!isFinishing() && !isDestroyed()) {
            Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show();
        }
    }
}
