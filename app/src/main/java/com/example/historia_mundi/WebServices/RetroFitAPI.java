package com.example.historia_mundi.WebServices;

import com.example.historia_mundi.Models.GoogleResponseModel;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface RetroFitAPI {

    /**
     * Function that sends out a GET request to the given URL
     */

    @GET
    Call<GoogleResponseModel> getNearByPlaces(@Url String url);
}