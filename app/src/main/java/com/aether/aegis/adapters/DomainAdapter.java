package com.aether.aegis.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.aether.aegis.R;
import com.aether.aegis.data.model.DomainInfo;
import com.aether.aegis.databinding.ItemDomainBinding;

import java.util.ArrayList;
import java.util.List;

public class DomainAdapter extends RecyclerView.Adapter<DomainAdapter.DomainViewHolder> {

    public interface OnDomainActionListener {
        void onBlock(DomainInfo domain);
    }

    private final List<DomainInfo> domainList = new ArrayList<>();
    private final OnDomainActionListener listener;

    public DomainAdapter(OnDomainActionListener listener) {
        this.listener = listener;
    }

    public void updateList(List<DomainInfo> newDomains) {
        domainList.clear();
        if (newDomains != null) {
            domainList.addAll(newDomains);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DomainViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemDomainBinding binding = ItemDomainBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new DomainViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull DomainViewHolder holder, int position) {
        holder.bind(domainList.get(position));
    }

    @Override
    public int getItemCount() {
        return domainList.size();
    }

    class DomainViewHolder extends RecyclerView.ViewHolder {
        private final ItemDomainBinding binding;

        DomainViewHolder(ItemDomainBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(DomainInfo domain) {
            binding.tvDomainName.setText(domain.getDomain());
            binding.tvDomainIp.setText("IP: " + domain.getIp() + " · " + domain.getReputation());
            binding.tvThreatIntel.setText(domain.getThreatIndicator());

            Context context = binding.getRoot().getContext();

            if (domain.isBlocked()) {
                binding.tvDomainReputation.setText("BLOCKED");
                binding.tvDomainReputation.setBackgroundResource(R.drawable.bg_badge_safe);
                binding.tvDomainReputation.setTextColor(ContextCompat.getColor(context, R.color.aegis_safe));
                binding.btnBlockDomain.setEnabled(false);
                binding.btnBlockDomain.setText("Blocked Locally");
            } else if (domain.getReputation().contains("C2") || domain.getReputation().contains("Threat")) {
                binding.tvDomainReputation.setText("C2 HOST");
                binding.tvDomainReputation.setBackgroundResource(R.drawable.bg_badge_danger);
                binding.tvDomainReputation.setTextColor(ContextCompat.getColor(context, R.color.aegis_danger));
                binding.btnBlockDomain.setEnabled(true);
                binding.btnBlockDomain.setText("Block via Local DNS");
            } else {
                binding.tvDomainReputation.setText("CLEAN");
                binding.tvDomainReputation.setBackgroundResource(R.drawable.bg_badge_safe);
                binding.tvDomainReputation.setTextColor(ContextCompat.getColor(context, R.color.aegis_safe));
                binding.btnBlockDomain.setEnabled(true);
                binding.btnBlockDomain.setText("Add to Watchlist");
            }

            binding.btnBlockDomain.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onBlock(domain);
                }
            });
        }
    }
}
