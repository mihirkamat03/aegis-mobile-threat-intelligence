package com.aether.aegis.ui.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.aether.aegis.adapters.ActivityAdapter;
import com.aether.aegis.data.repository.AegisRepository;
import com.aether.aegis.databinding.FragmentActivityBinding;
import com.aether.aegis.ui.motion.MotionUtils;

public class ActivityFragment extends Fragment implements AegisRepository.DataChangeListener {

    private FragmentActivityBinding binding;
    private ActivityAdapter activityAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentActivityBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        activityAdapter = new ActivityAdapter();
        binding.rvActivityTimeline.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvActivityTimeline.setAdapter(activityAdapter);

        activityAdapter.updateList(AegisRepository.getInstance().getTimeline());

        MotionUtils.staggerViews(
                binding.tvTimelineTitle,
                binding.cardTemporalDrift,
                binding.rvActivityTimeline
        );

        AegisRepository.getInstance().addListener(this);
    }

    @Override
    public void onDataChanged() {
        if (isAdded()) {
            activityAdapter.updateList(AegisRepository.getInstance().getTimeline());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        AegisRepository.getInstance().removeListener(this);
        binding = null;
    }
}
