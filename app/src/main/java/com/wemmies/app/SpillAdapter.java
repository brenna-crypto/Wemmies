package com.wemmies.app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.wemmies.app.model.Wemmie;
import java.util.List;

// Adapter for the community spills RecyclerView on the Spill screen
// It takes a list of Wemmie objects and displays each one as a card
public class SpillAdapter extends RecyclerView.Adapter<SpillAdapter.SpillViewHolder> {

    // Interface to handle click events — the activity decides what happens on tap
    public interface OnSpillClickListener {
        void onSpillClick(Wemmie wemmie);
    }

    private List<Wemmie> spills;
    private OnSpillClickListener listener;

    public SpillAdapter(List<Wemmie> spills, OnSpillClickListener listener) {
        this.spills = spills;
        this.listener = listener;
    }

    // ViewHolder holds references to the views in each row
    // This avoids calling findViewById repeatedly which would be slow
    public static class SpillViewHolder extends RecyclerView.ViewHolder {
        TextView tvSpillEmoji;
        TextView tvSpillThought;
        TextView tvSpillEmpathy;

        public SpillViewHolder(View itemView) {
            super(itemView);
            tvSpillEmoji = itemView.findViewById(R.id.tvSpillEmoji);
            tvSpillThought = itemView.findViewById(R.id.tvSpillThought);
            tvSpillEmpathy = itemView.findViewById(R.id.tvSpillEmpathy);
        }
    }

    // Inflates the item layout for each row in the RecyclerView
    @NonNull
    @Override
    public SpillViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_spill, parent, false);
        return new SpillViewHolder(view);
    }

    // Binds the data from each Wemmie object to the views in the row
    @Override
    public void onBindViewHolder(@NonNull SpillViewHolder holder, int position) {
        Wemmie wemmie = spills.get(position);

        holder.tvSpillThought.setText(wemmie.getShamefulThought());
        holder.tvSpillEmpathy.setText("♥ " + wemmie.getEmpathyCount());

        // Pick the right emoji based on the emotion type
        switch (wemmie.getEmotionType()) {
            case "sad":     holder.tvSpillEmoji.setText("😢"); break;
            case "anxious": holder.tvSpillEmoji.setText("😰"); break;
            case "angry":   holder.tvSpillEmoji.setText("😤"); break;
            case "tired":   holder.tvSpillEmoji.setText("😩"); break;
            case "numb":    holder.tvSpillEmoji.setText("😶"); break;
            case "ashamed": holder.tvSpillEmoji.setText("😖"); break;
            default:        holder.tvSpillEmoji.setText("🌑"); break;
        }

        // When a spill is tapped, notify the activity via the listener
        holder.itemView.setOnClickListener(v -> listener.onSpillClick(wemmie));
    }

    // Tells the RecyclerView how many items to display
    @Override
    public int getItemCount() {
        return spills.size();
    }
}