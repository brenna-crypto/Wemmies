package com.wemmies.app;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.wemmies.app.model.Wemmie;
import java.util.Arrays;
import java.util.List;

public class WemmieDetailActivity extends AppCompatActivity {

    private Wemmie wemmie; // the Wemmie object passed from SpillActivity
    private TextView tvEmpathyCount;
    private TextView tvTransformStatus;
    private TextView tvWemmieEmoji;

    // Pre-defined empathy responses — no free text allowed to keep the space safe
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

    // Match each emotion type to an emoji for the Wemmie display
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

        // Retrieve the Wemmie object that was passed from SpillActivity via Intent
        wemmie = (Wemmie) getIntent().getSerializableExtra("wemmie");

        // Get references to all the views we need to update
        tvWemmieEmoji = findViewById(R.id.tvWemmieEmoji);
        TextView tvEmotionBadge = findViewById(R.id.tvEmotionBadge);
        TextView tvShamefulThought = findViewById(R.id.tvShamefulThought);
        tvEmpathyCount = findViewById(R.id.tvEmpathyCount);
        tvTransformStatus = findViewById(R.id.tvTransformStatus);
        RecyclerView rvEmpathyWheel = findViewById(R.id.rvEmpathyWheel);
        Button btnBack = findViewById(R.id.btnBack);

        // Populate the screen with the Wemmie's data
        tvWemmieEmoji.setText(getWemmieEmoji(wemmie.getEmotionType()));
        tvEmotionBadge.setText(wemmie.getEmotionType().toUpperCase());
        tvShamefulThought.setText("\"" + wemmie.getShamefulThought() + "\"");

        // Set the initial empathy count and transformation status
        updateEmpathyUI();

        // Set up the RecyclerView for the empathy wheel
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvEmpathyWheel.setLayoutManager(layoutManager);

        // When an empathy button is tapped, add empathy and refresh the UI
        EmpathyAdapter adapter = new EmpathyAdapter(empathyResponses, response -> {
            wemmie.addEmpathy();
            updateEmpathyUI(); // refresh count and check for transformation
            Toast.makeText(this, response + " sent! 💜", Toast.LENGTH_SHORT).show();
        });

        rvEmpathyWheel.setAdapter(adapter);

        // Back button just closes this activity and returns to the Spill screen
        btnBack.setOnClickListener(v -> finish());
    }

    // Updates the empathy count text and checks if the Wemmie has transformed
    // This is called every time someone sends empathy
    private void updateEmpathyUI() {
        tvEmpathyCount.setText("♥ " + wemmie.getEmpathyCount() + " empathies received");

        if (wemmie.isTransformed()) {
            // The Wemmie has reached 5 empathies — trigger the transformation
            tvWemmieEmoji.setText("✨");
            tvTransformStatus.setText("This Wemmie has been transformed by empathy 🌟");
            tvTransformStatus.setTextColor(
                    getResources().getColor(R.color.wemmie_cyan, getTheme())
            );
        } else {
            // Show how many more empathies are needed to transform
            int remaining = 5 - wemmie.getEmpathyCount();
            tvTransformStatus.setText(remaining + " more empathies to transform this Wemmie");
        }
    }
}