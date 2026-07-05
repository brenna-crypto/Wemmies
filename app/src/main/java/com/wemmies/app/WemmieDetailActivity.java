package com.wemmies.app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.wemmies.app.model.Wemmie;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Shows one Wemmie, lets users send empathy, and handles transformation.
 */
public class WemmieDetailActivity extends AppCompatActivity {

    private static final String TAG = "Wemmies";

    private Wemmie wemmie;
    private TextView tvEmpathyCount;
    private TextView tvTransformStatus;
    private TextView tvWemmieEmoji;

    private FirebaseFirestore db;
    private FirebaseAnalytics analytics;
    private FirebaseAuth auth;

    private final List<String> empathyResponses = Arrays.asList(
            "💜 So relatable",
            "🫂 You're not alone",
            "✨ I believe you",
            "🌙 Your feelings are valid",
            "💙 Lending strength",
            "🕊️ It's ok to rest",
            "🔥 Sending courage",
            "👁️ I see you"
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wemmie_detail);

        db = FirebaseFirestore.getInstance();
        analytics = FirebaseAnalytics.getInstance(this);
        auth = FirebaseAuth.getInstance();

        wemmie = (Wemmie) getIntent().getSerializableExtra("wemmie");

        tvWemmieEmoji = findViewById(R.id.tvWemmieEmoji);
        TextView tvEmotionBadge = findViewById(R.id.tvEmotionBadge);
        TextView tvShamefulThought = findViewById(R.id.tvShamefulThought);
        tvEmpathyCount = findViewById(R.id.tvEmpathyCount);
        tvTransformStatus = findViewById(R.id.tvTransformStatus);
        RecyclerView rvEmpathyWheel = findViewById(R.id.rvEmpathyWheel);
        Button btnBack = findViewById(R.id.btnBack);
        TextView btnProfile = findViewById(R.id.btnProfile);

        btnProfile.setOnClickListener(v -> {
            Intent intent = new Intent(WemmieDetailActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        tvWemmieEmoji.setText(getWemmieEmoji(wemmie.getEmotionType()));
        tvEmotionBadge.setText(wemmie.getEmotionType().toUpperCase());
        tvShamefulThought.setText("\"" + wemmie.getShamefulThought() + "\"");

        updateEmpathyUI();

        rvEmpathyWheel.setLayoutManager(new LinearLayoutManager(this));

        EmpathyAdapter adapter = new EmpathyAdapter(empathyResponses, this::sendEmpathy);
        rvEmpathyWheel.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());
    }

    private void sendEmpathy(String response) {
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "Please sign in first.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (wemmie == null || wemmie.getId() == null) {
            Toast.makeText(this, "Could not find this Wemmie.", Toast.LENGTH_SHORT).show();
            return;
        }

        wemmie.addEmpathy();
        updateEmpathyUI();

        db.collection("wemmies")
                .document(wemmie.getId())
                .update("empathyCount", FieldValue.increment(1))
                .addOnSuccessListener(unused -> {
                    incrementEmpathiesSent(currentUser);
                    checkAndMarkTransformed();

                    Bundle bundle = new Bundle();
                    bundle.putString("emotion_type", wemmie.getEmotionType());
                    bundle.putString("empathy_response", response);
                    analytics.logEvent("empathy_sent", bundle);

                    Toast.makeText(this, response + " sent! 💜", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Couldn't save empathy", e);
                    Toast.makeText(this, "Couldn't save empathy", Toast.LENGTH_SHORT).show();
                });
    }

    private void incrementEmpathiesSent(FirebaseUser currentUser) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("displayName", currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "Anonymous");
        userData.put("email", currentUser.getEmail());
        userData.put("empathiesSent", FieldValue.increment(1));
        userData.put("spillsMade", FieldValue.increment(0));
        userData.put("monstersTamed", FieldValue.increment(0));

        db.collection("users")
                .document(currentUser.getUid())
                .set(userData, SetOptions.merge())
                .addOnFailureListener(e ->
                        Log.e(TAG, "Failed to increment empathiesSent", e)
                );
    }

    private void checkAndMarkTransformed() {
        if (wemmie.getId() == null) {
            return;
        }

        if (wemmie.getEmpathyCount() >= 5 && !wemmie.isTransformed()) {
            wemmie.setTransformed(true);

            db.collection("wemmies")
                    .document(wemmie.getId())
                    .update("transformed", true)
                    .addOnSuccessListener(unused -> {
                        incrementMonstersTamed();
                        analytics.logEvent("wemmie_transformed", null);
                        updateEmpathyUI();
                    })
                    .addOnFailureListener(e ->
                            Log.e(TAG, "Failed to mark Wemmie transformed", e)
                    );
        }
    }

    private void incrementMonstersTamed() {
        String ownerId = wemmie.getUserId();

        if (ownerId == null) {
            return;
        }

        Map<String, Object> ownerData = new HashMap<>();
        ownerData.put("monstersTamed", FieldValue.increment(1));

        db.collection("users")
                .document(ownerId)
                .set(ownerData, SetOptions.merge())
                .addOnFailureListener(e ->
                        Log.e(TAG, "Failed to increment monstersTamed", e)
                );
    }

    private String getWemmieEmoji(String emotionType) {
        switch (emotionType) {
            case "sad":
                return "😢";
            case "anxious":
                return "😰";
            case "angry":
                return "😤";
            case "tired":
                return "😩";
            case "numb":
                return "😶";
            case "ashamed":
                return "😖";
            default:
                return "🌑";
        }
    }

    private void updateEmpathyUI() {
        tvEmpathyCount.setText("♥ " + wemmie.getEmpathyCount() + " empathies received");

        if (wemmie.isTransformed()) {
            tvWemmieEmoji.setText("✨");
            tvTransformStatus.setText("This Wemmie has been transformed by empathy 🌟");
            tvTransformStatus.setTextColor(getResources().getColor(R.color.wemmie_cyan, getTheme()));
        } else {
            int remaining = 5 - wemmie.getEmpathyCount();
            tvTransformStatus.setText(remaining + " more empathies to transform this Wemmie");
        }
    }
}