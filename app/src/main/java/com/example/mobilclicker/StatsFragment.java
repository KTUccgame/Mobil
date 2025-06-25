package com.example.mobilclicker;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class StatsFragment extends Fragment {

    private MainActivity mainActivity;

    private PlayFragment playfragment;

    private SharedPreferences sharedPreferences;
    TextView profileText;
    public StatsFragment() {
        // empty to not break app
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_stats, container, false);
        mainActivity = (MainActivity) getActivity();

        TextView adminText = view.findViewById(R.id.admin_text_text);
        TextView points = view.findViewById(R.id.admin_text_text2);

        Bundle bundle = this.getArguments();

        if (bundle !=null) {
            int score = bundle.getInt("key",0);
            String time = bundle.getString("tiem","");

            adminText.setText("Time spent playing the game:  " + time);

            points.setText("Score: " + score);
        }
        profileText = view.findViewById(R.id.Profile_text);

        return view;
    }

}

