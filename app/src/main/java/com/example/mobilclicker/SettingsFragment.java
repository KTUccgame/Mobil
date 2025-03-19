package com.example.mobilclicker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

public class SettingsFragment extends Fragment {

    public SettingsFragment() {
        // Reikalingas tuščias konstruktorius
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Sukuriame ir grąžiname fragmento vaizdą
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }
}
