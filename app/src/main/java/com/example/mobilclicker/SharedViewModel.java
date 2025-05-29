package com.example.mobilclicker;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SharedViewModel extends ViewModel {
    private final MutableLiveData<Integer> currentTowerType = new MutableLiveData<>();

    // Bendrai laikysime kamerinio žaidimo round score
    private final MutableLiveData<Integer> cameraRoundScore = new MutableLiveData<>(0);

    // Taip pat galim turėti bendrą viso žaidimo score (pvz., jei reikia sumuoti)
    private final MutableLiveData<Integer> totalScore = new MutableLiveData<>(0);

    // Getteriai
    public LiveData<Integer> getCurrentTowerType() {
        return currentTowerType;
    }

    public LiveData<Integer> getCameraRoundScore() {
        return cameraRoundScore;
    }

    public LiveData<Integer> getTotalScore() {
        return totalScore;
    }

    // Setteriai
    public void setCurrentTowerType(int type) {
        currentTowerType.setValue(type);
    }

    // Kamerinio round score nustatymas (panašu į tavo `sendCameraRoundScore`)
    public void sendCameraRoundScore(int score) {
        cameraRoundScore.setValue(score);

        // Pridedame prie bendro score (jei nori kaupti bendrą sumą)
        Integer currentTotal = totalScore.getValue();
        if (currentTotal == null) currentTotal = 0;
        totalScore.setValue(currentTotal + score);
    }

    // Jei nori, gali pridėti metodą, kad nustatyti totalScore tiesiogiai
    public void resetScores() {
        cameraRoundScore.setValue(0);
        totalScore.setValue(0);
    }
}
