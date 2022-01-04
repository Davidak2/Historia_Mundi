package com.example.historia_mundi.Constants;

import com.example.historia_mundi.PlaceModel;
import com.example.historia_mundi.R;

import java.util.ArrayList;
import java.util.Arrays;

public interface AllConstants {

    int STORAGE_REQUEST_CODE = 1000;
    int LOCATION_REQUEST_CODE = 2000;

    String IMAGE_PATH = "/profile/image_profile.jpg";

    ArrayList<PlaceModel> placeName = new ArrayList<>(
            Arrays.asList(
                    new PlaceModel(1, R.drawable.ic_mosque,"Mosque","mosque"),
                    new PlaceModel(2, R.drawable.ic_church,"Church","church"),
                    new PlaceModel(3, R.drawable.ic_synagogue,"Synagogue","synagogue")
            )
    );
}
