package com.aether.aegis.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.aether.aegis.R;
import com.aether.aegis.data.model.Severity;
import com.aether.aegis.data.model.Threat;
import com.aether.aegis.databinding.ItemThreatBinding;
import com.aether.aegis.ui.appdetail.AppDetailActivity;

import java.util.ArrayList;
import java.util.List;

public class ThreatAdapter extends RecyclerView.Adapter<ThreatAdapter.ThreatViewHolder> {

    public interface OnThreatActionListener {
        void onInvestigate(Threat threat);
    }

    private final List<Threat> threatList = new ArrayList<>();
    private final OnThreatActionListener listener;

    public ThreatAdapter(OnThreatActionListener listener) {
        this.listener = listener;
    }

    public void updateList(List<Threat> newThreats) {
        threatList.clear();
        if (newThreats != null) {
            threatList.addAll(newThreats);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ThreatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemThreatBinding binding = ItemThreatBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new ThreatViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ThreatViewHolder holder, int position) {
        holder.bind(threatList.get(position));
    }

    @Override
    public int getItemCount() {
        return threatList.size();
    }

    class ThreatViewHolder extends RecyclerView.ViewHolder {
        private final ItemThreatBinding binding;

        ThreatViewHolder(ItemThreatBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Threat threat) {
            Context context = binding.getRoot().getContext();
            binding.tvThreatTitle.setText(threat.getTitle());
            binding.tvAppName.setText("Target: " + threat.getAppName());
            binding.tvThreatDescription.setText(threat.getDescription());
            binding.tvEvidenceCount.setText(threat.getEvidenceCount() + " Correlated Signals");
            binding.tvDetectionTime.setText(threat.getDetectionTime());

            boolean isResolved = "RESOLVED".equalsIgnoreCase(threat.getStatus()) || threat.getStatus().contains("BLOCKED");

            if (isResolved) {
                binding.tvThreatSeverity.setText("RESOLVED");
                binding.tvThreatSeverity.setBackgroundResource(R.drawable.bg_badge_safe);
                binding.tvThreatSeverity.setTextColor(ContextCompat.getColor(context, R.color.aegis_safe));
                binding.cardThreatRoot.setStrokeColor(ContextCompat.getColor(context, R.color.aegis_border_subtle));
                binding.tvThreatTitle.setTextColor(ContextCompat.getColor(context, R.color.aegis_text_secondary));
            } else {
                Severity severity = threat.getSeverity();
                binding.tvThreatSeverity.setText(severity.getLabel());
                binding.tvThreatTitle.setTextColor(ContextCompat.getColor(context, R.color.aegis_text_primary));

                switch (severity) {
                    case CRITICAL:
                        binding.tvThreatSeverity.setBackgroundResource(R.drawable.bg_badge_danger);
                        binding.tvThreatSeverity.setTextColor(ContextCompat.getColor(context, R.color.aegis_danger));
                        binding.cardThreatRoot.setStrokeColor(ContextCompat.getColor(context, R.color.aegis_danger_stroke));
                        break;
                    case HIGH:
                        binding.tvThreatSeverity.setBackgroundResource(R.drawable.bg_badge_danger);
                        binding.tvThreatSeverity.setTextColor(ContextCompat.getColor(context, R.color.aegis_danger));
                        binding.cardThreatRoot.setStrokeColor(ContextCompat.getColor(context, R.color.aegis_border));
                        break;
                    case SUSPICIOUS:
                        binding.tvThreatSeverity.setBackgroundResource(R.drawable.bg_badge_warning);
                        binding.tvThreatSeverity.setTextColor(ContextCompat.getColor(context, R.color.aegis_warning));
                        binding.cardThreatRoot.setStrokeColor(ContextCompat.getColor(context, R.color.aegis_border));
                        break;
                    case SAFE:
                    default:
                        binding.tvThreatSeverity.setBackgroundResource(R.drawable.bg_badge_safe);
                        binding.tvThreatSeverity.setTextColor(ContextCompat.getColor(context, R.color.aegis_safe));
                        binding.cardThreatRoot.setStrokeColor(ContextCompat.getColor(context, R.color.aegis_border_subtle));
                        break;
                }
            }

            binding.btnInvestigate.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onInvestigate(threat);
                } else {
                    Intent intent = new Intent(context, AppDetailActivity.class);
                    intent.putExtra(AppDetailActivity.EXTRA_PACKAGE_NAME, threat.getPackageName());
                    context.startActivity(intent);
                }
            });
        }
    }
}
