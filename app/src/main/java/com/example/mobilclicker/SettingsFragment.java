package com.example.mobilclicker;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

public class SettingsFragment extends Fragment {
    private MainActivity mainActivity;

    public SettingsFragment() {
        // Reikalingas tuščias konstruktorius
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        mainActivity = (MainActivity) getActivity();

        Button userButton = view.findViewById(R.id.profile_1_button);
        Button adminButton = view.findViewById(R.id.profile_2_button);

        userButton.setOnClickListener(v -> {
            changeToUser();
        });
        adminButton.setOnClickListener(v -> {
            changeToAdmin();
        });
        // Sukuriame ir grąžiname fragmento vaizdą
        //return inflater.inflate(R.layout.fragment_settings, container, false);
        return view;
    }
    private void changeToUser() {
        if (mainActivity != null) {
            // Clear SharedPreferences
            mainActivity.getSharedPreferences("settings_prefs", mainActivity.MODE_PRIVATE)
                    .edit()
                    .clear()
                    .apply();
            Log.i("i","clicked user button");
            if(!mainActivity.isUser)
            {
                ///////
                //Snackbar mySnackbar = Snackbar.make(view, "you are now an admin", 5);
                //mySnackbar.show();
                ///////
                Log.w("settings","going to user mode");
                mainActivity.isUser = true;
            }
        }


    }
    private void changeToAdmin() {
        if (mainActivity != null) {
            // Clear SharedPreferences
            mainActivity.getSharedPreferences("upgrade_prefs", mainActivity.MODE_PRIVATE)
                    .edit()
                    .clear()
                    .apply();
            Log.i("i","clicked admin button");
            if(mainActivity.isUser)
            {
                ///////
                //Snackbar mySnackbar = Snackbar.make(view, "you are now an admin", 5);
                //mySnackbar.show();
                ///////
                Log.w("settings","going to admin mode");
                mainActivity.isUser = false;
            }
        }

    }
}

