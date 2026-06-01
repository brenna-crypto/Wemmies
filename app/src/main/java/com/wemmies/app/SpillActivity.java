package com.wemmies.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.wemmies.app.model.Wemmie;
import java.util.ArrayList;
import java.util.List;

public class SpillActivity extends AppCompatActivity {

    private String selectedEmotion = null;
    private TextView lastSelectedChip = null;

    private final String[] emotions = {"😔 Sad", "😰 Anxious", "😤 Angry", "😩 Tired", "😶 Numb", "😖 Ashamed"};
    private final String[] emotionKeys = {"sad", "anxious", "angry", "tired", "numb", "ashamed"};

    private FirebaseFirestore db;
    private FirebaseAnalytics analytics;
    private List<Wemmie> communitySpills = new ArrayList<>();
    private SpillAdapter spillAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spill);

        db = FirebaseFirestore.getInstance();
        analytics = FirebaseAnalytics.getInstance(this);

        LinearLayout chipsContainer = findViewById(R.id.emotionChipsContainer);
        EditText etThought = findViewById(R.id.etShamefulThought);
        Button btnCreate = findViewById(R.id.btnCreateWemmie);
        RecyclerView rvCommunitySpills = findViewById(R.id.rvCommunitySpills);

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

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvCommunitySpills.setLayoutManager(layoutManager);

        spillAdapter = new SpillAdapter(communitySpills, wemmie -> {
            Intent intent = new Intent(this, WemmieDetailActivity.class);
            intent.putExtra("wemmie", wemmie);
            startActivity(intent);
        });

        rvCommunitySpills.setAdapter(spillAdapter);

        // Load Wemmies from Firestore in real time — ASYNC (Class 2)
        db.collection("wemmies")
                .orderBy("empathyCount", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Failed to load wemmies", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (snapshots != null) {
                        communitySpills.clear();
                        // Fixed: replaced 'var' with explicit DocumentSnapshot type
                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            Wemmie w = doc.toObject(Wemmie.class);
                            if (w != null) {
                                w.setId(doc.getId());
                                communitySpills.add(w);
                            }
                        }
                        spillAdapter.notifyDataSetChanged();
                    }
                });

        btnCreate.setOnClickListener(v -> {
            String thought = etThought.getText().toString().trim();

            if (thought.isEmpty()) {
                Toast.makeText(this, "Please type your shameful thought 💭", Toast.LENGTH_SHORT).show();
                return;
            }
            if (selectedEmotion == null) {
                Toast.makeText(this, "Please pick an emotion 💜", Toast.LENGTH_SHORT).show();
                return;
            }

            Wemmie wemmie = new Wemmie(thought, selectedEmotion);

            db.collection("wemmies")
                    .add(wemmie)
                    .addOnSuccessListener(documentReference -> {
                        wemmie.setId(documentReference.getId());

                        // Log Analytics event
                        Bundle bundle = new Bundle();
                        bundle.putString("emotion_type", selectedEmotion);
                        analytics.logEvent("spill_created", bundle);

                        Toast.makeText(this, "Your Wemmie was created 💜", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(this, WemmieDetailActivity.class);
                        intent.putExtra("wemmie", wemmie);
                        startActivity(intent);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to save. Try again.", Toast.LENGTH_SHORT).show();
                    });
        });
    }
}