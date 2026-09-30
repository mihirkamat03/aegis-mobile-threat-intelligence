package com.aether.aegis.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aether.aegis.data.model.Recommendation;
import com.aether.aegis.databinding.ItemRecommendationBinding;

import java.util.ArrayList;
import java.util.List;

public class RecommendationAdapter extends RecyclerView.Adapter<RecommendationAdapter.RecommendationViewHolder> {

    public interface OnRecommendationActionListener {
        void onExecute(Recommendation recommendation);
    }

    private final List<Recommendation> recommendations = new ArrayList<>();
    private final OnRecommendationActionListener listener;

    public RecommendationAdapter(OnRecommendationActionListener listener) {
        this.listener = listener;
    }

    public void updateList(List<Recommendation> newRecs) {
        recommendations.clear();
        if (newRecs != null) {
            recommendations.addAll(newRecs);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecommendationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemRecommendationBinding binding = ItemRecommendationBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new RecommendationViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RecommendationViewHolder holder, int position) {
        holder.bind(recommendations.get(position));
    }

    @Override
    public int getItemCount() {
        return recommendations.size();
    }

    class RecommendationViewHolder extends RecyclerView.ViewHolder {
        private final ItemRecommendationBinding binding;

        RecommendationViewHolder(ItemRecommendationBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Recommendation rec) {
            binding.tvRecTitle.setText(rec.getTitle());
            binding.tvRecDescription.setText(rec.getDescription());

            if (rec.isCompleted()) {
                binding.btnRecAction.setText("Action Completed ✓");
                binding.btnRecAction.setEnabled(false);
            } else {
                binding.btnRecAction.setText("Execute: " + rec.getTitle());
                binding.btnRecAction.setEnabled(true);
            }

            binding.btnRecAction.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onExecute(rec);
                }
            });
        }
    }
}
