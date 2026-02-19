package com.wemmies.app;

import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.wemmies.app.model.Wemmie;

public class WemmieDetailActivity extends AppCompatActivity {

    private Wemmie wemmie;
    private TextView tvEmpathyCount;
    private TextView tvTransformStatus;
    private TextView tvWemmieEmoji;

    private final String[] empathyResponses = {
            "💜 So relatable",
            "🫂 You're not alone",
            "✨ I believe you",
            "🌙 Your feelings are valid",
            "💙 Lending strength",
            "🕊️ It's ok to rest",
            "🔥 Sending courage",
            "👁️ I see you"
    };

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

        wemmie = (Wemmie) getIntent().getSerializableExtra("wemmie");

        tvWemmieEmoji = findViewById(R.id.tvWemmieEmoji);
        TextView tvEmotionBadge = findViewById(R.id.tvEmotionBadge);
        TextView tvShamefulThought = findViewById(R.id.tvShamefulThought);
        tvEmpathyCount = findViewById(R.id.tvEmpathyCount);
        tvTransformStatus = findViewById(R.id.tvTransformStatus);
        GridLayout empathyGrid = findViewById(R.id.empathyWheelGrid);
        Button btnBack = findViewById(R.id.btnBack);

        tvWemmieEmoji.setText(getWemmieEmoji(wemmie.getEmotionType()));
        tvEmotionBadge.setText(wemmie.getEmotionType().toUpperCase());
        tvShamefulThought.setText("\"" + wemmie.getShamefulThought() + "\"");

        updateEmpathyUI();

        for (String response : empathyResponses) {
            TextView empathyBtn = new TextView(this);
            empathyBtn.setText(response);
            empathyBtn.setTextColor(getResources().getColor(R.color.wemmie_text_primary, getTheme()));
            empathyBtn.setTextSize(13);
            empathyBtn.setGravity(Gravity.CENTER);
            empathyBtn.setPadding(16, 24, 16, 24);
            empathyBtn.setBackground(getResources().getDrawable(R.drawable.bg_empathy_button, getTheme()));

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.setMargins(8, 8, 8, 8);
            empathyBtn.setLayoutParams(params);

            empathyBtn.setOnClickListener(v -> {
                wemmie.addEmpathy();
                updateEmpathyUI();
                empathyBtn.setBackground(
                        getResources().getDrawable(R.drawable.bg_emotion_chip_selected, getTheme())
                );
                empathyBtn.postDelayed(() ->
                        empathyBtn.setBackground(
                                getResources().getDrawable(R.drawable.bg_empathy_button, getTheme())
                        ), 500
                );
                Toast.makeText(this, response + " sent! 💜", Toast.LENGTH_SHORT).show();
            });

            empathyGrid.addView(empathyBtn);
        }

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
