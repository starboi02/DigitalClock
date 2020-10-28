package com.example.digitalclock.ui.stopwatch;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.digitalclock.R;
import com.example.digitalclock.ui.alarm.AlarmViewModel;

public class StopwatchFragment extends Fragment {

    private StopwatchViewModel mViewModel;

    public static StopwatchFragment newInstance() {
        return new StopwatchFragment();
    }

    private StopwatchViewModel stopwatchViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        stopwatchViewModel =
                new ViewModelProvider(this).get(StopwatchViewModel.class);
        View root = inflater.inflate(R.layout.fragment_alarm, container, false);
        final TextView textView = root.findViewById(R.id.text_dashboard);
        stopwatchViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        return root;
    }

}