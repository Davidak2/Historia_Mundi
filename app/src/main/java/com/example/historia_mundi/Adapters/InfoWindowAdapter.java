package com.example.historia_mundi.Adapters;

import android.content.Context;
import android.location.GnssAntennaInfo;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.historia_mundi.databinding.CustomInfoWindowBinding;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.maps.android.SphericalUtil;

public class InfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private CustomInfoWindowBinding binding;
    private Location location;
    private Context context;

    /**
     *Using the InfoWindowAdpater class from GoogleMap
     * We created a custom info window for our markers
     * to show the place's name and distance from user
     */

    public InfoWindowAdapter(Location location, Context context) {
        this.location = location;
        this.context = context;

        binding = CustomInfoWindowBinding.inflate(LayoutInflater.from(context), null, false);
    }

    /**
     *Function to get info window when user taps on a location
     * shows the user his distance to the location
     */

    @Nullable
    @Override
    public View getInfoWindow(@NonNull Marker marker) {

        binding.txtlocationName.setText(marker.getTitle());

        double distance = SphericalUtil.computeDistanceBetween(new LatLng(location.getLatitude(),location.getLongitude()),
                marker.getPosition());

        if(distance > 1000)
        {
            double kilometers = distance/1000;
            binding.txtlocationDistance.setText(distance + " KM");
        }else {
            binding.txtlocationDistance.setText(distance + " Meters");
        }

        return binding.getRoot();
    }

    /**
     *Function to get info window when user taps on a location
     * shows the user his distance to the location
     */

    @Nullable
    @Override
    public View getInfoContents(@NonNull Marker marker) {

        binding.txtlocationName.setText(marker.getTitle());

        double distance = SphericalUtil.computeDistanceBetween(new LatLng(location.getLatitude(),location.getLongitude()),
                marker.getPosition());

        if(distance > 1000)
        {
            double kilometers = distance/1000;
            binding.txtlocationDistance.setText(distance + "KM");
        }else {
            binding.txtlocationDistance.setText(distance + "Meters");
        }

        return binding.getRoot();

    }
}
