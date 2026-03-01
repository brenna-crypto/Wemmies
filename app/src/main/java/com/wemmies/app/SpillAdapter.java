package com.wemmies.app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.wemmies.app.model.Wemmie;
import java.util.List;

public class SpillAdapter extends RecyclerView.Adapter<SpillAdapter.SpillViewHolder> {

    public interface OnSpillClickListener {
        void onSpillClick(Wemmie wemmie);
    }

    private List<Wemmie> spills;
    private OnSpillClickListener listener;

    public SpillAdapter(List<Wemmie> spills, OnSpillClickListener listener) {
        this.spills = spills;
        this.listener = listener;
    }

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

    @NonNull
    @Override
    public SpillViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_spill, parent, false);
        return new SpillViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SpillViewHolder holder, int position) {
        Wemmie wemmie = spills.get(position);

        holder.tvSpillThought.setText(wemmie.getShamefulThought());
        holder.tvSpillEmpathy.setText("♥ " + wemmie.getEmpathyCount());

        // Set emoji based on emotion type
        switch (wemmie.getEmotionType()) {
            case "sad":     holder.tvSpillEmoji.setText("😢"); break;
            case "anxious": holder.tvSpillEmoji.setText("😰"); break;
            case "angry":   holder.tvSpillEmoji.setText("😤"); break;
            case "tired":   holder.tvSpillEmoji.setText("😩"); break;
            case "numb":    holder.tvSpillEmoji.setText("😶"); break;
            case "ashamed": holder.tvSpillEmoji.setText("😖"); break;
            default:        holder.tvSpillEmoji.setText("🌑"); break;
        }

        // Tapping a community spill opens it in Wemmie Detail
        holder.itemView.setOnClickListener(v -> listener.onSpillClick(wemmie));
    }

    @Override
    public int getItemCount() {
        return spills.size();
    }
}