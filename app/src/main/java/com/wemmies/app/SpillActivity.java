package com.wemmies.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.wemmies.app.model.Wemmie;

public class SpillActivity extends AppCompatActivity {

    private String selectedEmotion = null;
    private TextView lastSelectedChip = null;

    private final String[] emotions = {"😔 Sad", "😰 Anxious", "😤 Angry", "😩 Tired", "😶 Numb", "😖 Ashamed"};
    private final String[] emotionKeys = {"sad", "anxious", "angry", "tired", "numb", "ashamed"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spill);

        LinearLayout chipsContainer = findViewById(R.id.emotionChipsContainer);
        EditText etThought = findViewById(R.id.etShamefulThought);
        Button btnCreate = findViewById(R.id.btnCreateWemmie);

        // Build emotion chips programmatically
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
                // Deselect previous chip
                if (lastSelectedChip != null) {
                    lastSelectedChip.setBackground(
                            getResources().getDrawable(R.drawable.bg_emotion_chip, getTheme())
                    );
                }
                // Select this chip
                chip.setBackground(
                        getResources().getDrawable(R.drawable.bg_emotion_chip_selected, getTheme())
                );
                selectedEmotion = emotionKey;
                lastSelectedChip = chip;
            });

            chipsContainer.addView(chip);
        }

        // Create Wemmie button
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

            Intent intent = new Intent(this, WemmieDetailActivity.class);
            intent.putExtra("wemmie", wemmie);
            startActivity(intent);
        });
    }
}