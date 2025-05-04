package com.example.mobilclicker;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

public class SettingsFragment extends Fragment {
    private MainActivity mainActivity;
    private final String adminPW = "aa";
    public SettingsFragment() {
        // Reikalingas tuščias konstruktorius
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        mainActivity = (MainActivity) getActivity();

        Button profile1Button = view.findViewById(R.id.profile_1_button);
        Button profile2Button = view.findViewById(R.id.profile_2_button);
        Button profile3Button = view.findViewById(R.id.profile_3_button);
        Button adminButton = view.findViewById(R.id.admin_text_button);
        Button creditsButton = view.findViewById(R.id.credits_button);
        TextView adminText = view.findViewById(R.id.admin_text_text);

        profile1Button.setOnClickListener(v -> {
            swapProfile(1);
        });
        profile2Button.setOnClickListener(v -> {
            swapProfile(2);
        });
        profile3Button.setOnClickListener(v -> {
            swapProfile(3);
        });
        creditsButton.setOnClickListener(v -> {
            // popup info about people who made the app
            // make textbox read info from .txt file?
        });
        adminButton.setOnClickListener(v -> {
            if(adminPW.contentEquals(adminText.getText()))
            {
                toggleUserMode();
            }
        });
        // Sukuriame ir grąžiname fragmento vaizdą
        //return inflater.inflate(R.layout.fragment_settings, container, false);
        return view;
    }
    private void swapProfile(int profileNumber)
    {

    }
    private void toggleUserMode() {
        if (mainActivity != null) {
            SharedPreferences prefs = mainActivity.getSharedPreferences(
                    mainActivity.isUser ? "upgrade_prefs" : "settings_prefs",
                    mainActivity.MODE_PRIVATE
            );
            prefs.edit().clear().apply();
            mainActivity.isUser = !mainActivity.isUser;
            Log.w("settings", mainActivity.isUser ? "going to user mode" : "going to admin mode");
        }
    }
}

