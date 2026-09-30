package com.aether.aegis.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.aether.aegis.R;
import com.aether.aegis.data.model.BehaviourEvent;
import com.aether.aegis.data.model.Severity;
import com.aether.aegis.databinding.ItemActivityEventBinding;

import java.util.ArrayList;
import java.util.List;

public class ActivityAdapter extends RecyclerView.Adapter<ActivityAdapter.ActivityViewHolder> {

    private final List<BehaviourEvent> eventList = new ArrayList<>();

    public void updateList(List<BehaviourEvent> newEvents) {
        eventList.clear();
        if (newEvents != null) {
            eventList.addAll(newEvents);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ActivityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemActivityEventBinding binding = ItemActivityEventBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new ActivityViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ActivityViewHolder holder, int position) {
        holder.bind(eventList.get(position), position == 0, position == eventList.size() - 1);
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    class ActivityViewHolder extends RecyclerView.ViewHolder {
        private final ItemActivityEventBinding binding;

        ActivityViewHolder(ItemActivityEventBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(BehaviourEvent event, boolean isFirst, boolean isLast) {
            binding.tvEventTime.setText(event.getTimestamp());
            binding.tvEventApp.setText(event.getAssociatedApp());
            binding.tvEventTitle.setText(event.getTitle());
            binding.tvEventDescription.setText(event.getDescription());

            // Handle rail connections
            binding.timelineRailTop.setVisibility(isFirst ? View.INVISIBLE : View.VISIBLE);
            binding.timelineRailBottom.setVisibility(isLast ? View.INVISIBLE : View.VISIBLE);

            Context context = binding.getRoot().getContext();
            Severity severity = event.getSeverity();

            int dotBg;
            switch (severity) {
                case CRITICAL:
                case HIGH:
                    dotBg = R.drawable.bg_badge_danger;
                    break;
                case SUSPICIOUS:
                    dotBg = R.drawable.bg_badge_warning;
                    break;
                case SAFE:
                default:
                    dotBg = R.drawable.bg_badge_safe;
                    break;
            }
            binding.dotSeverity.setBackgroundResource(dotBg);

            if (event.hasScoreShift()) {
                binding.tvScoreShift.setVisibility(View.VISIBLE);
                binding.tvScoreShift.setText(event.getScoreShiftFrom() + " → " + event.getScoreShiftTo());
                binding.tvScoreShift.setBackgroundResource(R.drawable.bg_badge_danger);
                binding.tvScoreShift.setTextColor(ContextCompat.getColor(context, R.color.aegis_danger));
            } else {
                binding.tvScoreShift.setVisibility(View.GONE);
            }
        }
    }
}
