package com.aether.aegis.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.aether.aegis.R;
import com.aether.aegis.data.model.AppInfo;
import com.aether.aegis.data.model.Severity;
import com.aether.aegis.databinding.ItemAppBinding;
import com.aether.aegis.ui.appdetail.AppDetailActivity;

import java.util.ArrayList;
import java.util.List;

public class AppAdapter extends RecyclerView.Adapter<AppAdapter.AppViewHolder> {

    public interface OnAppClickListener {
        void onAppClick(AppInfo app);
    }

    private final List<AppInfo> appList = new ArrayList<>();
    private final OnAppClickListener listener;

    public AppAdapter(OnAppClickListener listener) {
        this.listener = listener;
    }

    public void updateList(List<AppInfo> newApps) {
        appList.clear();
        if (newApps != null) {
            appList.addAll(newApps);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AppViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAppBinding binding = ItemAppBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new AppViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AppViewHolder holder, int position) {
        holder.bind(appList.get(position));
    }

    @Override
    public int getItemCount() {
        return appList.size();
    }

    class AppViewHolder extends RecyclerView.ViewHolder {
        private final ItemAppBinding binding;

        AppViewHolder(ItemAppBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(AppInfo app) {
            binding.tvAppName.setText(app.getName());
            binding.tvPackageName.setText(app.getPackageName());
            binding.tvRiskFactor.setText(app.getPrimaryRiskFactor());

            Context context = binding.getRoot().getContext();
            Severity severity = app.getSeverity();
            int score = app.getRiskScore();
            binding.tvRiskBadge.setText(severity.getLabel() + " · " + score);

            binding.pbAppRisk.setProgress(score);

            switch (severity) {
                case CRITICAL:
                    binding.tvRiskBadge.setBackgroundResource(R.drawable.bg_badge_danger);
                    binding.tvRiskBadge.setTextColor(ContextCompat.getColor(context, R.color.aegis_danger));
                    binding.pbAppRisk.setProgressDrawable(ContextCompat.getDrawable(context, R.drawable.progress_bar_horizontal_danger));
                    binding.tvSignalsCount.setText("4 correlated signals · C2 beaconing confirmed");
                    break;
                case HIGH:
                    binding.tvRiskBadge.setBackgroundResource(R.drawable.bg_badge_danger);
                    binding.tvRiskBadge.setTextColor(ContextCompat.getColor(context, R.color.aegis_danger));
                    binding.pbAppRisk.setProgressDrawable(ContextCompat.getDrawable(context, R.drawable.progress_bar_horizontal_danger));
                    binding.tvSignalsCount.setText("3 anomalous signals · Threat pulse match");
                    break;
                case SUSPICIOUS:
                    binding.tvRiskBadge.setBackgroundResource(R.drawable.bg_badge_warning);
                    binding.tvRiskBadge.setTextColor(ContextCompat.getColor(context, R.color.aegis_warning));
                    binding.pbAppRisk.setProgressDrawable(ContextCompat.getDrawable(context, R.drawable.progress_bar_horizontal_warning));
                    binding.tvSignalsCount.setText("2 elevated drift indicators detected");
                    break;
                case SAFE:
                default:
                    binding.tvRiskBadge.setBackgroundResource(R.drawable.bg_badge_safe);
                    binding.tvRiskBadge.setTextColor(ContextCompat.getColor(context, R.color.aegis_safe));
                    binding.pbAppRisk.setProgressDrawable(ContextCompat.getDrawable(context, R.drawable.progress_bar_horizontal_safe));
                    binding.tvSignalsCount.setText("Operating within normal baseline");
                    break;
            }

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAppClick(app);
                } else {
                    Intent intent = new Intent(context, AppDetailActivity.class);
                    intent.putExtra(AppDetailActivity.EXTRA_PACKAGE_NAME, app.getPackageName());
                    context.startActivity(intent);
                }
            });
        }
    }
}
