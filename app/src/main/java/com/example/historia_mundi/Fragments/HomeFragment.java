package com.example.historia_mundi.Fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.historia_mundi.Adapters.InfoWindowAdapter;
import com.example.historia_mundi.Constants.AllConstants;
import com.example.historia_mundi.GooglePlaceModel;
import com.example.historia_mundi.Models.GoogleResponseModel;
import com.example.historia_mundi.Permissions.AppPermissions;
import com.example.historia_mundi.PlaceModel;
import com.example.historia_mundi.R;
import com.example.historia_mundi.WebServices.RetroFitAPI;
import com.example.historia_mundi.WebServices.RetroFitClient;
import com.example.historia_mundi.databinding.CustomInfoWindowBinding;
import com.example.historia_mundi.databinding.FragmentHomeBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment implements OnMapReadyCallback {

    private FragmentHomeBinding binding;
    private GoogleMap mGoogleMap;
    private AppPermissions appPermissions;
    private boolean isLocationPermissionOn;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Location currentLocation;
    private FirebaseAuth firebaseAuth;
    private Marker currentMarker;
    private int radius = 1000;
    private RetroFitAPI retroFitAPI;
    private List<GooglePlaceModel> googlePlaceModelList;
    private PlaceModel selectedPlaceModel;
    private InfoWindowAdapter infoWindowAdapter;

    /**
     * Home Screen Fragment. The "Home Screen" of the application
     */

    public HomeFragment() {
    }

    /**
     * Initialization of the App
     * calls on different bindings for responsive view,
     * app permission and firebase authentication,
     * RetroFitAPI to get places from Google
     * and initializes a list for places given by Google and the RetroFitAPI
     */

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        appPermissions = new AppPermissions();
        firebaseAuth = FirebaseAuth.getInstance();
        retroFitAPI = RetroFitClient.getRetrofitClient().create(RetroFitAPI.class);
        googlePlaceModelList = new ArrayList<>();

        binding.placeGroup.setOnCheckedChangeListener(new ChipGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(ChipGroup group, int checkedId) {

                if (checkedId != -1) {
                    PlaceModel placeModel = AllConstants.placeName.get(checkedId - 1);
                    binding.edtPlaceName.setText(placeModel.getName());
                    selectedPlaceModel = placeModel;
                    getPlaces(placeModel.getPlaceType());
                }
            }
        });
        return binding.getRoot();
    }

    /**
     * Once app is Initialized
     * Call on the homeMap to show the user a Google Map
     * and calls on the Chips used by the user to select different places for viewing
     */

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.homeMap);

        mapFragment.getMapAsync(this);

        for (PlaceModel placeModel : AllConstants.placeName) {
            Chip chip = new Chip(requireContext());
            chip.setText(placeModel.getName());
            chip.setId(placeModel.getId());
            chip.setPadding(8, 8, 8, 8);
            chip.setTextColor(getResources().getColor(R.color.white, null));
            chip.setChipBackgroundColor(getResources().getColorStateList(R.color.primaryColor, null));
            chip.setChipIcon(ResourcesCompat.getDrawable(getResources(), placeModel.getDrawableId(), null));
            chip.setCheckedIconVisible(false);
            chip.setCheckable(true);

            binding.placeGroup.addView(chip);
        }
    }

    /**
     * Checks if the user given permission for the app to use GPS location
     * if not, asks for permission
     */

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        mGoogleMap = googleMap;

        if (appPermissions.isLocationOn(requireContext())) {
            setUpGoogleMap();
            isLocationPermissionOn = true;

        } else if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)) {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Location Permission")
                    .setMessage("Historia Mundi Requires Location Permission")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            requestLocation();
                        }
                    })
                    .create().show();
        } else {
            requestLocation();
        }
    }

    public void requestLocation() {
        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                AllConstants.LOCATION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == AllConstants.LOCATION_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                isLocationPermissionOn = true;
                setUpGoogleMap();
            } else {
                isLocationPermissionOn = false;
                Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Function to set up the Google Map using the users location
     * and updates user location
     */

    private void setUpGoogleMap() {

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            isLocationPermissionOn = false;
            return;
        }
        mGoogleMap.setMyLocationEnabled(true);
        mGoogleMap.getUiSettings().setTiltGesturesEnabled(true);

        setUpLocationUpdate();
    }

    private void setUpLocationUpdate() {

        locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null) {
                    for (Location location : locationResult.getLocations()) {
                        Log.d("TAG", "OnLocationResult: " + location.getLongitude() + "" + location.getLatitude());
                    }
                }
                super.onLocationResult(locationResult);
            }
        };

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext());
        startLocationUpdates();
    }

    /**
     * Function that Updates the user location
     */

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            isLocationPermissionOn = false;
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {
                            Toast.makeText(requireContext(), "Location Update Started", Toast.LENGTH_SHORT).show();
                        }

                    }
                });

        getCurrentLocation();
    }

    /**
     * Function to set up the user's location
     */

    private void getCurrentLocation() {

        FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext());

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            isLocationPermissionOn = false;
            return;
        }
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {

                currentLocation = location;
                infoWindowAdapter = null;
                infoWindowAdapter = new InfoWindowAdapter(currentLocation, requireContext());
                mGoogleMap.setInfoWindowAdapter(infoWindowAdapter);
                moveCameraToLocation(location);

            }
        });
    }

    /**
     * Function that moves the camera to the user's location
     */
    private void moveCameraToLocation(Location location) {

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new
                LatLng(location.getLatitude(),location.getLongitude()), 15);

        MarkerOptions markerOptions = new MarkerOptions()
                .position(new LatLng(location.getLatitude(),location.getLongitude()))
                .title("Current Location")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                .snippet(firebaseAuth.getCurrentUser().getDisplayName());

        if(currentMarker != null)
        {
            currentMarker.remove();
        }

        currentMarker = mGoogleMap.addMarker(markerOptions);
        currentMarker.setTag(703);
        mGoogleMap.animateCamera(cameraUpdate);
    }

    /**
     * Function that stops the location update
     */
    private void stopLocationUpdate() {
        Log.d("TAG","stopLocationUpdate: Location Update Stopped");
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    @Override
    public void onPause() {
        super.onPause();

        if(fusedLocationProviderClient != null)
            stopLocationUpdate();
    }

    @Override
    public void onResume() {
        super.onResume();

        if(fusedLocationProviderClient != null)
        {
            startLocationUpdates();
            if(currentMarker != null)
            {
                currentMarker.remove();
            }
        }
    }

    /**
     * The "Main Function" of the app
     * It uses the API address of GooglePlacesAPI
     * To show the user the places he wishes to see
     */
    private void getPlaces(String placeName) {

        if (isLocationPermissionOn) {
            String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="
                    + currentLocation.getLatitude() + "," + currentLocation.getLongitude()
                    + "&radius=" + radius + "&type=" + placeName + "&key=" +
                    getResources().getString(R.string.API_KEY);

            if(currentLocation != null)
            {
                retroFitAPI.getNearByPlaces(url).enqueue(new Callback<GoogleResponseModel>() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onResponse(Call<GoogleResponseModel> call, Response<GoogleResponseModel> response) {
                        if(response.errorBody() == null)
                        {
                            if(response.body() != null)
                            {
                                if(response.body().getGooglePlaceModelList() != null && response.body().getGooglePlaceModelList().size() > 0)
                                {
                                    googlePlaceModelList.clear();
                                    mGoogleMap.clear();
                                    for(int i = 0; i < response.body().getGooglePlaceModelList().size(); i++)
                                    {
                                        googlePlaceModelList.add(response.body().getGooglePlaceModelList().get(i));
                                        addMarker(response.body().getGooglePlaceModelList().get(i), i);
                                }
                                }else{
                                    mGoogleMap.clear();
                                    googlePlaceModelList.clear();
                                    radius += 1000;
                                    getPlaces(placeName);
                                }
                            }
                        }else {
                            Log.d("TAG","onResponse: " + response.errorBody());
                            Toast.makeText(requireContext(),"Error:Places(1) " + response.errorBody(),Toast.LENGTH_SHORT).show();
                        }

                    }

                    @Override
                    public void onFailure(Call<GoogleResponseModel> call, Throwable t) {

                        Log.d("TAG","onFailure:Places(2) " + t);


                    }
                });
            }

        }
    }

    /**
     * Functions that set up the different markers for places
     * on the Google Map
     * And Sets up the custom icon used to show the user's location
     */

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void addMarker(GooglePlaceModel googlePlaceModel,int position) {

        MarkerOptions markerOptions = new MarkerOptions()
                .position(new LatLng(googlePlaceModel.getGeometry().getLocation().getLat(),googlePlaceModel.getGeometry().getLocation().getLng()))
                .title(googlePlaceModel.getName())
                .snippet(googlePlaceModel.getVicinity());
        markerOptions.icon(getCustomIcon());
        mGoogleMap.addMarker(markerOptions).setTag(position);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private BitmapDescriptor getCustomIcon() {
        Drawable background = ContextCompat.getDrawable(requireContext(),R.drawable.ic_custom_point);
        background.setTint(getResources().getColor(R.color.quantum_googred900,null));
        background.setBounds(0,0,background.getIntrinsicWidth(),background.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(background.getIntrinsicWidth(),background.getIntrinsicHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        background.draw(canvas);

        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
}