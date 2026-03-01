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
import com.wemmies.app.model.Wemmie;
import java.util.ArrayList;
import java.util.List;

public class SpillActivity extends AppCompatActivity {

    // Keep track of which emotion chip the user selected
    private String selectedEmotion = null;
    private TextView lastSelectedChip = null; // needed so we can deselect the previous chip

    // Display labels and their corresponding keys
    private final String[] emotions = {"😔 Sad", "😰 Anxious", "😤 Angry", "😩 Tired", "😶 Numb", "😖 Ashamed"};
    private final String[] emotionKeys = {"sad", "anxious", "angry", "tired", "numb", "ashamed"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spill);

        // Get references to the views we need
        LinearLayout chipsContainer = findViewById(R.id.emotionChipsContainer);
        EditText etThought = findViewById(R.id.etShamefulThought);
        Button btnCreate = findViewById(R.id.btnCreateWemmie);
        RecyclerView rvCommunitySpills = findViewById(R.id.rvCommunitySpills);

        // Build the emotion chips in a loop instead of hardcoding each one in XML
        // This makes it easier to add or remove emotions later
        for (int i = 0; i < emotions.length; i++) {
            final String emotionKey = emotionKeys[i];
            final String emotionLabel = emotions[i];

            // Create each chip as a TextView with custom styling
            TextView chip = new TextView(this);
            chip.setText(emotionLabel);
            chip.setTextColor(getResources().getColor(R.color.wemmie_text_primary, getTheme()));
            chip.setTextSize(14);
            chip.setPadding(32, 16, 32, 16);
            chip.setBackground(getResources().getDrawable(R.drawable.bg_emotion_chip, getTheme()));

            // Add margin between chips so they don't touch each other
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, 16, 0);
            chip.setLayoutParams(params);

            // When a chip is tapped, deselect the previous one and select this one
            chip.setOnClickListener(v -> {
                if (lastSelectedChip != null) {
                    // Reset the previously selected chip back to unselected style
                    lastSelectedChip.setBackground(
                            getResources().getDrawable(R.drawable.bg_emotion_chip, getTheme())
                    );
                }
                // Highlight the newly selected chip
                chip.setBackground(
                        getResources().getDrawable(R.drawable.bg_emotion_chip_selected, getTheme())
                );
                selectedEmotion = emotionKey;
                lastSelectedChip = chip;
            });

            chipsContainer.addView(chip);
        }

        // Hardcoded sample data to simulate a community feed
        // In a real app this would come from a database or API
        List<Wemmie> communitySpills = new ArrayList<>();
        communitySpills.add(new Wemmie("I feel like a total imposter at work...", "anxious"));
        communitySpills.add(new Wemmie("I snapped at someone I love today.", "angry"));
        communitySpills.add(new Wemmie("I said yes when I wanted to say no. Again.", "numb"));
        communitySpills.add(new Wemmie("I'm so burned out but can't take a break.", "tired"));
        communitySpills.add(new Wemmie("I feel like I'm falling behind everyone else.", "sad"));
        communitySpills.add(new Wemmie("I don't feel allowed to struggle.", "ashamed"));

        // Give each sample spill some empathy so the feed looks realistic
        communitySpills.get(0).addEmpathy(); communitySpills.get(0).addEmpathy();
        communitySpills.get(1).addEmpathy(); communitySpills.get(1).addEmpathy();
        communitySpills.get(1).addEmpathy();
        communitySpills.get(2).addEmpathy();
        communitySpills.get(3).addEmpathy(); communitySpills.get(3).addEmpathy();
        communitySpills.get(4).addEmpathy();
        communitySpills.get(5).addEmpathy(); communitySpills.get(5).addEmpathy();

        // Set up the RecyclerView with a LinearLayoutManager (vertical list)
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvCommunitySpills.setLayoutManager(layoutManager);

        // Create the adapter and tell it what to do when a spill is tapped
        // Tapping a community spill opens it in the Wemmie Detail screen
        SpillAdapter spillAdapter = new SpillAdapter(communitySpills, wemmie -> {
            Intent intent = new Intent(this, WemmieDetailActivity.class);
            intent.putExtra("wemmie", wemmie);
            startActivity(intent);
        });

        rvCommunitySpills.setAdapter(spillAdapter);

        // Validate input before creating a Wemmie
        // Both the thought and an emotion must be provided
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

            // Create a new Wemmie with the user's input and open the detail screen
            Wemmie wemmie = new Wemmie(thought, selectedEmotion);
            Intent intent = new Intent(this, WemmieDetailActivity.class);
            intent.putExtra("wemmie", wemmie);
            startActivity(intent);
        });
    }
}