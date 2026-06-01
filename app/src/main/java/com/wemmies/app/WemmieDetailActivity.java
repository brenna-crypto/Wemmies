package com.wemmies.app;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;
import com.wemmies.app.model.Wemmie;
import java.util.Arrays;
import java.util.List;

public class WemmieDetailActivity extends AppCompatActivity {

    private Wemmie wemmie;
    private TextView tvEmpathyCount;
    private TextView tvTransformStatus;
    private TextView tvWemmieEmoji;

    // Firebase instances
    private FirebaseFirestore db;
    private FirebaseAnalytics analytics;

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

    private String getWemmieEmoji(String emotionType) {
        switch (emotionType) {
            case "sad":     return "😢";
            case "anxious": return "😰";
            case "angry":   return "😤";
            case "tired":   return "😩";
            case "numb":    return "😶";
            case "ashamed": return "😖";
            default:        return "🌑";
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wemmie_detail);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        analytics = FirebaseAnalytics.getInstance(this);

        wemmie = (Wemmie) getIntent().getSerializableExtra("wemmie");

        tvWemmieEmoji = findViewById(R.id.tvWemmieEmoji);
        TextView tvEmotionBadge = findViewById(R.id.tvEmotionBadge);
        TextView tvShamefulThought = findViewById(R.id.tvShamefulThought);
        tvEmpathyCount = findViewById(R.id.tvEmpathyCount);
        tvTransformStatus = findViewById(R.id.tvTransformStatus);
        RecyclerView rvEmpathyWheel = findViewById(R.id.rvEmpathyWheel);
        Button btnBack = findViewById(R.id.btnBack);

        tvWemmieEmoji.setText(getWemmieEmoji(wemmie.getEmotionType()));
        tvEmotionBadge.setText(wemmie.getEmotionType().toUpperCase());
        tvShamefulThought.setText("\"" + wemmie.getShamefulThought() + "\"");

        updateEmpathyUI();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvEmpathyWheel.setLayoutManager(layoutManager);

        EmpathyAdapter adapter = new EmpathyAdapter(empathyResponses, response -> {

            // Update locally so UI feels instant
            wemmie.addEmpathy();
            updateEmpathyUI();

            // If this Wemmie has a Firestore ID, save the update
            // FieldValue.increment is atomic — safe for multiple users tapping at once
            if (wemmie.getId() != null) {
                db.collection("wemmies")
                        .document(wemmie.getId())
                        .update("empathyCount", FieldValue.increment(1))
                        .addOnSuccessListener(unused -> {
                            // Log Analytics event — HW3 requirement
                            Bundle bundle = new Bundle();
                            bundle.putString("emotion_type", wemmie.getEmotionType());
                            bundle.putString("empathy_response", response);
                            analytics.logEvent("empathy_sent", bundle);

                            // Log transformation if threshold just hit
                            if (wemmie.isTransformed()) {
                                analytics.logEvent("wemmie_transformed", null);
                            }
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Couldn't save empathy", Toast.LENGTH_SHORT).show();
                        });
            }

            Toast.makeText(this, response + " sent! 💜", Toast.LENGTH_SHORT).show();
        });

        rvEmpathyWheel.setAdapter(adapter);
        btnBack.setOnClickListener(v -> finish());
    }

    private void updateEmpathyUI() {
        tvEmpathyCount.setText("♥ " + wemmie.getEmpathyCount() + " empathies received");

        if (wemmie.isTransformed()) {
            tvWemmieEmoji.setText("✨");
            tvTransformStatus.setText("This Wemmie has been transformed by empathy 🌟");
            tvTransformStatus.setTextColor(
                    getResources().getColor(R.color.wemmie_cyan, getTheme())
            );
        } else {
            int remaining = 5 - wemmie.getEmpathyCount();
            tvTransformStatus.setText(remaining + " more empathies to transform this Wemmie");
        }
    }
}