package com.example.historia_mundi.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.example.historia_mundi.GooglePlaceModel;

import java.util.List;

public class GoogleResponseModel {

    /**
     * Creates a list of places from the info
     * it gets from the googlePlacesModel
     */

    @SerializedName("results")
    @Expose
    private List<GooglePlaceModel> googlePlaceModelList;

    @SerializedName("error_message")
    @Expose
    private String error;

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public List<GooglePlaceModel> getGooglePlaceModelList() {
        return googlePlaceModelList;
    }

    public void setGooglePlaceModelList(List<GooglePlaceModel> googlePlaceModelList) {
        this.googlePlaceModelList = googlePlaceModelList;
    }
}