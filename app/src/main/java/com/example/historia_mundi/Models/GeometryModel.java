package com.example.historia_mundi.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class GeometryModel {

    /**
     * Gets the location from the RetroFitAPI
     */

    @SerializedName("location")
    @Expose
    private LocationModel location;

    public LocationModel getLocation() {
        return location;
    }

    public void setLocation(LocationModel location) {
        this.location = location;
    }

}