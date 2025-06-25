package com.example.mobilclicker;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

public class deathFragment extends Fragment {

    private MainActivity mainActivity;
    private SharedPreferences sharedPreferences;
    int score;
    String time;
    TextView profileText;
    public deathFragment() {
        // empty to not break app
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_death, container, false);
        mainActivity = (MainActivity) getActivity();


        Button retry = view.findViewById(R.id.button);

        retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlayFragment playFragment = new PlayFragment();
                requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,playFragment).addToBackStack(null).commit();
            }
        });

        Bundle bundle = this.getArguments();

        if (bundle !=null) {
            score = bundle.getInt("key",0);
            time = bundle.getString("tiem","");
        }

        Button stats = view.findViewById(R.id.button2);
        stats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StatsFragment statsFragment = new StatsFragment();
                Bundle bundle = new Bundle();
                bundle.putInt("key",score);
                bundle.putString("tiem",time);
                statsFragment.setArguments(bundle);
                requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,statsFragment).addToBackStack(null).commit();
            }
        });


        return view;
    }

}

