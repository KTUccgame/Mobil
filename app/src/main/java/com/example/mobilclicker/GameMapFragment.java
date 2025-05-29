package com.example.mobilclicker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameMapFragment extends Fragment implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private Location currentLocation;
    private List<LatLng> powerUps = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_game_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            initMap();
        }
    }

    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment == null) {
            SupportMapFragment newMapFragment = SupportMapFragment.newInstance();
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.map_container, newMapFragment)
                    .commit();
            newMapFragment.getMapAsync(this);
        } else {
            mapFragment.getMapAsync(this);
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        Log.i("GameMapFragment", "Location gauta: " + location.getLatitude() + ", " + location.getLongitude());
                        currentLocation = location;
                        LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15));
                        mMap.setMyLocationEnabled(true);

                        generatePowerUpsAround(userLatLng);
                    } else {
                        Log.w("GameMapFragment", "Location yra NULL");
                        Toast.makeText(getContext(), "Nepavyko gauti vietos", Toast.LENGTH_SHORT).show();
                    }
                });

        mMap.setOnMarkerClickListener(marker -> {
            LatLng markerPos = marker.getPosition();
            float distance = distanceBetween(currentLocation.getLatitude(), currentLocation.getLongitude(),
                    markerPos.latitude, markerPos.longitude);

            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle(marker.getTitle())
                    .setMessage("Atstumas: " + String.format("%.2f", distance) + " m\nNori sekti šį power-up?")
                    .setPositiveButton("Sekti", (dialog, which) -> {
                        // Įrašom į SharedPreferences
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
                        prefs.edit()
                                .putString("selectedPowerUpName", marker.getTitle())
                                .putFloat("selectedPowerUpDistance", distance)
                                .apply();

                        // Grįžtam į PlayFragment
                        getParentFragmentManager().popBackStack();
                    })
                    .setNegativeButton("Atšaukti", null)
                    .show();

            return true;
        });

    }

    private void generatePowerUpsAround(LatLng center) {
        Log.i("GameMapFragment", "Generuojami power-up'ai aplink: " + center.latitude + ", " + center.longitude);
        Random rand = new Random();
        powerUps.clear();

        for (int i = 0; i < 5; i++) {
            double randomLat = center.latitude + (rand.nextDouble() - 0.5) / 100;
            double randomLng = center.longitude + (rand.nextDouble() - 0.5) / 100;
            LatLng powerUpLoc = new LatLng(randomLat, randomLng);

            Log.i("GameMapFragment", "Pridedamas power-up: " + powerUpLoc.latitude + ", " + powerUpLoc.longitude);
            powerUps.add(powerUpLoc);

            MarkerOptions markerOptions = new MarkerOptions()
                    .position(powerUpLoc)
                    .title("2x Multiplier")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));

            mMap.addMarker(markerOptions);
        }
    }

    private float distanceBetween(double lat1, double lon1, double lat2, double lon2) {
        float[] results = new float[1];
        Location.distanceBetween(lat1, lon1, lat2, lon2, results);
        return results[0];
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initMap();
            } else {
                Toast.makeText(getContext(), "Leidimas vietai atmestas", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
