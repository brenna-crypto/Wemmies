package com.wemmies.app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.wemmies.app.model.Wemmie;

public class SpillActivity extends AppCompatActivity {

    private String selectedEmotion = null;
    private TextView lastSelectedChip = null;

    private final String[] emotions = {"😔 Sad", "😰 Anxious", "😤 Angry", "😩 Tired", "😶 Numb", "😖 Ashamed"};
    private final String[] emotionKeys = {"sad", "anxious", "angry", "tired", "numb", "ashamed"};

    private FirebaseFirestore db;
    private FirebaseAnalytics analytics;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spill);

        db = FirebaseFirestore.getInstance();
        analytics = FirebaseAnalytics.getInstance(this);
        auth = FirebaseAuth.getInstance();

        LinearLayout chipsContainer = findViewById(R.id.emotionChipsContainer);
        EditText etThought = findViewById(R.id.etShamefulThought);
        Button btnCreate = findViewById(R.id.btnCreateWemmie);

        // Set up Bottom Navigation listeners
        findViewById(R.id.navSpill).setOnClickListener(v -> {
            // Already here
        });

        findViewById(R.id.navFeed).setOnClickListener(v -> {
            Intent intent = new Intent(SpillActivity.this, FeedActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        });

        findViewById(R.id.navProfile).setOnClickListener(v -> {
            Intent intent = new Intent(SpillActivity.this, ProfileActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        });

        // Highlight Active Tab
        TextView tvNavText = findViewById(R.id.tvNavSpillText);
        tvNavText.setTextColor(getResources().getColor(R.color.wemmie_cyan, getTheme()));

        for (int i = 0; i < emotions.length; i++) {
            final String emotionKey = emotionKeys[i];
            final String emotionLabel = emotions[i];

            TextView chip = new TextView(this);
            chip.setText(emotionLabel);
            chip.setTextColor(getResources().getColor(R.color.wemmie_text_primary, getTheme()));
            chip.setTextSize(14);
            chip.setPadding(32, 16, 32, 16);
            chip.setBackground(getResources().getDrawable(R.drawable.bg_emotion_chip, getTheme()));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, 16, 0);
            chip.setLayoutParams(params);

            chip.setOnClickListener(v -> {
                if (lastSelectedChip != null) {
                    lastSelectedChip.setBackground(
                            getResources().getDrawable(R.drawable.bg_emotion_chip, getTheme())
                    );
                }

                chip.setBackground(
                        getResources().getDrawable(R.drawable.bg_emotion_chip_selected, getTheme())
                );

                selectedEmotion = emotionKey;
                lastSelectedChip = chip;
            });

            chipsContainer.addView(chip);
        }

        btnCreate.setOnClickListener(v -> {
            String thought = etThought.getText().toString().trim();

            if (thought.isEmpty()) {
                showFeedback("Please type your shameful thought 💭");
                return;
            }

            if (selectedEmotion == null) {
                showFeedback("Please pick an emotion 💜");
                return;
            }

            FirebaseUser currentUser = auth.getCurrentUser();

            if (currentUser == null) {
                showFeedback("Please sign in first.");
                return;
            }

            Wemmie wemmie = new Wemmie(thought, selectedEmotion);
            wemmie.setUserId(currentUser.getUid());
            wemmie.setTimestamp(System.currentTimeMillis());

            db.collection("wemmies")
                    .add(wemmie)
                    .addOnSuccessListener(documentReference -> {
                        wemmie.setId(documentReference.getId());

                        db.collection("users")
                                .document(currentUser.getUid())
                                .update("spillsMade", FieldValue.increment(1))
                                .addOnFailureListener(e ->
                                        Log.e("Wemmies", "Failed to increment spillsMade", e)
                                );

                        Bundle bundle = new Bundle();
                        bundle.putString("emotion_type", selectedEmotion);
                        analytics.logEvent("spill_created", bundle);

                        showFeedback("Your Wemmie was created 💜");

                        // Open the Personal Detail screen since they created it
                        Intent intent = new Intent(this, MyWemmieDetailActivity.class);
                        intent.putExtra("wemmie", wemmie);
                        startActivity(intent);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Wemmies", "Failed to save Wemmie", e);
                        showFeedback("Failed to save. Try again.");
                    });
        });
    }

    private void showFeedback(String message) {
        if (!isFinishing() && !isDestroyed()) {
            Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show();
        }
    }
}
