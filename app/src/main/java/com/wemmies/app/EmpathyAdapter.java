package com.wemmies.app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

// Adapter for the empathy wheel RecyclerView on the Wemmie Detail screen
// Each item is one empathy response button
public class EmpathyAdapter extends RecyclerView.Adapter<EmpathyAdapter.EmpathyViewHolder> {

    // Interface so the activity can handle what happens when empathy is sent
    public interface OnEmpathyClickListener {
        void onEmpathyClick(String response);
    }

    private List<String> empathyResponses;
    private OnEmpathyClickListener listener;

    public EmpathyAdapter(List<String> empathyResponses, OnEmpathyClickListener listener) {
        this.empathyResponses = empathyResponses;
        this.listener = listener;
    }

    // ViewHolder holds a reference to the TextView for each empathy button
    // This avoids calling findViewById repeatedly which would be slow
    public static class EmpathyViewHolder extends RecyclerView.ViewHolder {
        TextView tvEmpathyResponse;

        public EmpathyViewHolder(View itemView) {
            super(itemView);
            tvEmpathyResponse = itemView.findViewById(R.id.tvEmpathyResponse);
        }
    }

    // Inflate the item layout for each empathy button in the list
    @NonNull
    @Override
    public EmpathyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_empathy, parent, false);
        return new EmpathyViewHolder(view);
    }

    // Bind each empathy response string to the button and handle the click
    @Override
    public void onBindViewHolder(@NonNull EmpathyViewHolder holder, int position) {
        String response = empathyResponses.get(position);
        holder.tvEmpathyResponse.setText(response);

        holder.itemView.setOnClickListener(v -> {
            // Flash the button background to give visual feedback when tapped
            holder.tvEmpathyResponse.setBackgroundResource(R.drawable.bg_emotion_chip_selected);
            holder.itemView.postDelayed(() ->
                            holder.tvEmpathyResponse.setBackgroundResource(R.drawable.bg_empathy_button),
                    500 // reset back to normal after 500ms
            );
            // Notify the activity that this empathy response was chosen
            listener.onEmpathyClick(response);
        });
    }

    // Return the total number of empathy responses so RecyclerView knows how many rows to create
    @Override
    public int getItemCount() {
        return empathyResponses.size();
    }
}