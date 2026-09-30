package com.aether.aegis.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.aether.aegis.R;
import com.aether.aegis.data.model.PermissionInfo;
import com.aether.aegis.databinding.ItemPermissionBinding;

import java.util.ArrayList;
import java.util.List;

public class PermissionAdapter extends RecyclerView.Adapter<PermissionAdapter.PermissionViewHolder> {

    private final List<PermissionInfo> permissions = new ArrayList<>();

    public void updateList(List<PermissionInfo> newPerms) {
        permissions.clear();
        if (newPerms != null) {
            permissions.addAll(newPerms);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PermissionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPermissionBinding binding = ItemPermissionBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new PermissionViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PermissionViewHolder holder, int position) {
        holder.bind(permissions.get(position));
    }

    @Override
    public int getItemCount() {
        return permissions.size();
    }

    class PermissionViewHolder extends RecyclerView.ViewHolder {
        private final ItemPermissionBinding binding;

        PermissionViewHolder(ItemPermissionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(PermissionInfo perm) {
            binding.tvPermName.setText(perm.getName() + " (" + perm.getGroup() + ")");
            binding.tvPermContext.setText(perm.getUsageContext());
            binding.tvPermLastUsed.setText("Last observed: " + perm.getLastUsed());

            Context context = binding.getRoot().getContext();
            if (perm.isAnomaly()) {
                binding.tvPermBadge.setText("ANOMALY");
                binding.tvPermBadge.setBackgroundResource(R.drawable.bg_badge_danger);
                binding.tvPermBadge.setTextColor(ContextCompat.getColor(context, R.color.aegis_danger));
            } else if (perm.isSensitive()) {
                binding.tvPermBadge.setText("SENSITIVE");
                binding.tvPermBadge.setBackgroundResource(R.drawable.bg_badge_warning);
                binding.tvPermBadge.setTextColor(ContextCompat.getColor(context, R.color.aegis_warning));
            } else {
                binding.tvPermBadge.setText("STANDARD");
                binding.tvPermBadge.setBackgroundResource(R.drawable.bg_badge_safe);
                binding.tvPermBadge.setTextColor(ContextCompat.getColor(context, R.color.aegis_safe));
            }
        }
    }
}
