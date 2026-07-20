package com.wemmies.app;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.wemmies.app.model.Wemmie;

import java.util.ArrayList;
import java.util.List;

public class MyWemmieDetailActivity extends AppCompatActivity {

    private static final String TAG = "MyWemmieDetail";

    private Wemmie wemmie;
    private TextView tvEmpathyCount;
    private TextView tvTransformStatus;
    private TextView tvWemmieEmoji;
    private TextView tvEmpathiesHeader;

    private FirebaseFirestore db;

    private final List<String> receivedEmpathies = new ArrayList<>();
    private ReceivedEmpathyAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_wemmie_detail);

        db = FirebaseFirestore.getInstance();

        wemmie = (Wemmie) getIntent().getSerializableExtra("wemmie");

        tvWemmieEmoji = findViewById(R.id.tvWemmieEmoji);
        TextView tvEmotionBadge = findViewById(R.id.tvEmotionBadge);
        TextView tvShamefulThought = findViewById(R.id.tvShamefulThought);
        tvEmpathyCount = findViewById(R.id.tvEmpathyCount);
        tvTransformStatus = findViewById(R.id.tvTransformStatus);
        tvEmpathiesHeader = findViewById(R.id.tvEmpathiesHeader);
        RecyclerView rvReceivedEmpathies = findViewById(R.id.rvReceivedEmpathies);
        Button btnBack = findViewById(R.id.btnBack);

        if (wemmie == null) {
            showFeedback("Wemmie not found.");
            finish();
            return;
        }

        String emotionType = wemmie.getEmotionType();
        tvWemmieEmoji.setText(getWemmieEmoji(emotionType != null ? emotionType : ""));
        tvEmotionBadge.setText(emotionType != null ? emotionType.toUpperCase() : "UNKNOWN");
        tvShamefulThought.setText("\"" + (wemmie.getShamefulThought() != null ? wemmie.getShamefulThought() : "...") + "\"");

        updateEmpathyUI();

        rvReceivedEmpathies.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ReceivedEmpathyAdapter(receivedEmpathies);
        rvReceivedEmpathies.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());

        // Listen in real time to empathy messages sent to this Wemmie
        // Passing 'this' makes the listener lifecycle-aware.
        db.collection("wemmies")
                .document(wemmie.getId())
                .collection("empathies")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener(this, (snapshots, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Failed to load empathy messages", error);
                        return;
                    }

                    if (snapshots != null) {
                        receivedEmpathies.clear();
                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            String response = doc.getString("response");
                            if (response != null) {
                                receivedEmpathies.add(response);
                            }
                        }

                        // Toggle header label based on list size
                        if (receivedEmpathies.isEmpty()) {
                            tvEmpathiesHeader.setText("NO SUPPORT RECEIVED YET");
                        } else {
                            tvEmpathiesHeader.setText("COMMUNITY SUPPORT RECEIVED");
                        }

                        adapter.notifyDataSetChanged();
                    }
                });
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

    private void showFeedback(String message) {
        if (!isFinishing() && !isDestroyed()) {
            Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show();
        }
    }

    // Inner Adapter to display empathy messages
    public static class ReceivedEmpathyAdapter extends RecyclerView.Adapter<ReceivedEmpathyAdapter.ViewHolder> {
        private final List<String> messages;

        public ReceivedEmpathyAdapter(List<String> messages) {
            this.messages = messages;
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvEmpathyText;

            public ViewHolder(View view) {
                super(view);
                tvEmpathyText = view.findViewById(R.id.tvEmpathyText);
            }
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_received_empathy, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.tvEmpathyText.setText(messages.get(position));
        }

        @Override
        public int getItemCount() {
            return messages.size();
        }
    }
}
