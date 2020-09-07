package com.hudzah.wearamask;


import android.Manifest;
import android.animation.Animator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;
import yuku.ambilwarna.AmbilWarnaDialog;

import static android.content.Context.MODE_PRIVATE;


/**
 * A simple {@link Fragment} subclass.
 */
public class MapFragment extends Fragment implements OnMapReadyCallback, ConnectivityReceiver.ConnectivityReceiverListener, GpsLocationReceiver.GpsLocationReceiverListener, EasyPermissions.PermissionCallbacks {

    private GoogleMap googleMap;
    private MapView mapView;
    private static final String TAG = "MapFragment";
    private SharedPreferences sharedPreferences;
    private boolean loggedIn = false;
    private RelativeLayout notLoggedInLayout;
    private RelativeLayout loggedInLayout;
    private RelativeLayout offlineLayout;
    private LottieAnimationView fabUpButton;
    private FloatingActionButton recenterLocation;
    private EditText searchText;

    private AutocompleteSupportFragment autocompleteFragment;

    BottomSheetBehavior bottomSheetBehavior;
    CardView bottomSheet;
    TextView registerButton;

    private Button loginButton;

    private TextView welcomeTextView;
    private FusedLocationProviderClient mFusedLocationClient;

    private SeekBar radiusSeekBar;
    private FloatingActionButton colorTextView;
    private Button saveButton;
    private TextView discardTextView;
    private ScrollView extraInfoScrollView;
    private TextView radiusTextView;

    private static final float DEFAULT_ZOOM = 16f;

    private int selectedRadius = 15;
    private int selectedColor = 3394815;

    private Place thePlace;

    private int DEFAULT_RADIUS = 30;

    private static MapFragment instance;

    private View view = null;

    private CardView offlineModeLayout;

    ParseGeoPoint lastKnownLocationGeoPoint;

    private String locationName = "";

    private View layout;

    private View initialView;

    String placeId = "";

    private int transitionState;

    public com.hudzah.wearamask.Location location;

    public ArrayList<com.hudzah.wearamask.Location> locations = new ArrayList<>();

    private RequestQueue requestQueue;

    private LottieAnimationView offlineModeCheckButton;

    ConnectivityReceiver connectivityReceiver;
    GpsLocationReceiver gpsLocationReceiver;

    String[] permissions;


    public FloatingActionButton fabSafe;

    LocationRequest locationRequest;

    com.google.android.gms.location.LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            if (locationResult == null) {
                return;
            } else {
                for (Location location : locationResult.getLocations()) {
                    moveCamera(new LatLng(location.getLatitude(), location.getLongitude()), DEFAULT_ZOOM);
                }
            }
        }
    };

    Location currentLocation;

    AlertDialog namingDialog;

    public MapFragment() {
        // Required empty public constructor
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        DialogAdapter.ADAPTER.initDialogAdapter(getActivity());
        Log.d(TAG, "onActivityCreated: view is " + initialView);
        if(initialView == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (allPermissionsGranted()) {
                    mapView.onCreate(savedInstanceState);
                    mapView.onResume();
                    mapView.getMapAsync(this);
                }
            } else {
                mapView.onCreate(savedInstanceState);
                mapView.onResume();
                mapView.getMapAsync(this);

            }
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        initialView = view;
        if(view == null){
            view =  inflater.inflate(R.layout.fragment_map, container, false);
        }

        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        Log.d(TAG, "onMapReady: map is ready");

        CircleManager.Manager.init(getContext(), googleMap);

        styleMap();
        googleMap.setMyLocationEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        googleMap.getUiSettings().setCompassEnabled(false);

        googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                if(ParseUser.getCurrentUser() != null) {
                    if (ConnectivityReceiver.isConnected()) {
                        getPlaceId(latLng);
                    } else {
                        Toast.makeText(getContext(), "No connections found, try again", Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    Toast.makeText(getContext(), "You must be logged in to add a location", Toast.LENGTH_SHORT).show();
                }

            }
        });


    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());

        locationRequest = LocationRequest.create();
        locationRequest.setInterval(4000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        requestQueue = Volley.newRequestQueue(getContext());

        initLocationClass();

        if (!Places.isInitialized()) {
            Places.initialize(getContext(), getResources().getString(R.string.google_maps_key));
        }

        instance = this;

        if (ParseUser.getCurrentUser() != null) loggedIn = true;
        else loggedIn = false;

        notLoggedInLayout = (RelativeLayout) view.findViewById(R.id.notLoggedInLayout);
        loggedInLayout = (RelativeLayout) view.findViewById(R.id.loggedInLayout);
        offlineLayout = (RelativeLayout) view.findViewById(R.id.offlineLayout);
        mapView = (MapView) view.findViewById(R.id.mapView);

        autocompleteFragment = (AutocompleteSupportFragment)
                getChildFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        bottomSheet = view.findViewById(R.id.bottomSheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

        colorTextView = (FloatingActionButton) view.findViewById(R.id.selectColorTextView);

        extraInfoScrollView = (ScrollView) view.findViewById(R.id.extraInfoScrollView);

        radiusSeekBar = (SeekBar) view.findViewById(R.id.radiusSeekBar);

        layout = view.findViewById(R.id.layout);

        welcomeTextView = (TextView) view.findViewById(R.id.welcomeTextView);

        loginButton = (Button) view.findViewById(R.id.loginButton);

        saveButton = (Button) view.findViewById(R.id.saveButton);

        discardTextView = (TextView) view.findViewById(R.id.discardTextView);

        fabUpButton = (LottieAnimationView) view.findViewById(R.id.fabUpArrow);

        registerButton = (TextView) view.findViewById(R.id.registerButton);

        offlineModeLayout = (CardView) view.findViewById(R.id.offlineModeLayout);

        checkIfLoggedIn();

        fabUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBottomSheet();
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        radiusTextView = (TextView) view.findViewById(R.id.radiusTextView);

        offlineModeCheckButton = (LottieAnimationView) view.findViewById(R.id.offlineModeCheckButton);

        //searchText = (EditText) view.findViewById(R.id.locationEditText);

        fabSafe = (FloatingActionButton) view.findViewById(R.id.fabSafe);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToSignUp();
            }
        });

        recenterLocation = (FloatingActionButton) view.findViewById(R.id.recenterLocation);

        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View view, int i) {
                switch (i) {
                    case BottomSheetBehavior.STATE_HIDDEN:
                        googleMap.setPadding(0, 0, 0, 0);
                        fabUpButton.setVisibility(View.VISIBLE);
                        fabUpButton.playAnimation();
                        break;

                    case BottomSheetBehavior.STATE_DRAGGING:
                        hideUpButton();
                        break;

                    case BottomSheetBehavior.STATE_EXPANDED:
                        googleMap.setPadding(0, 0, 0, bottomSheet.getHeight());
                        hideUpButton();
                        break;

                    case BottomSheetBehavior.STATE_HALF_EXPANDED:
                        googleMap.setPadding(0, 0, 0, bottomSheet.getHeight());
                        hideUpButton();
                        break;
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        googleMap.setPadding(0, 0, 0, bottomSheetBehavior.getPeekHeight());
                        hideUpButton();

                }
            }

            @Override
            public void onSlide(@NonNull View view, float v) {

            }
        });

        initSearchText();

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLocationNameSelect();
            }
        });

        discardTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                discardLocation();
                location.getAllLocations(true);
                getLastDeviceLocation();
            }
        });

        colorTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showColorPicker();
            }
        });

        recenterLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (GpsLocationReceiver.checkLocationServicesEnabled(getContext())) {
                    getLastDeviceLocation();
                } else {
                    DialogAdapter.ADAPTER.displayErrorDialog(getContext().getResources().getString(R.string.dialog_enable_location_prompt), getContext().getResources().getString(R.string.dialog_enable_location_button));
                }
            }
        });

        radiusSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                selectedRadius = progress;
                radiusTextView.setText("Radius is " + selectedRadius + "m");
                if (thePlace != null) {
                    CircleManager.Manager.drawCircleOnMap(selectedRadius, selectedColor, thePlace);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        offlineModeCheckButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                manualConnectionRetry();
            }
        });

        fabSafe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (transitionState > 0) {
                    if (transitionState == Geofence.GEOFENCE_TRANSITION_ENTER || transitionState == Geofence.GEOFENCE_TRANSITION_DWELL) {
                        DialogAdapter.ADAPTER.displaySafeDialog();
                    } else if (transitionState == Geofence.GEOFENCE_TRANSITION_EXIT) {
                        DialogAdapter.ADAPTER.displayWarningDialog();

                    }
                } else {
                    DialogAdapter.ADAPTER.displayWarningDialog();
                }
            }
        });

        offlineModeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                offlineModeLayout.setAlpha(0.7f);
                offlineModeLayout.animate().alpha(0f).translationYBy(70).setDuration(180);
                //offlineModeLayout.setVisibility(View.GONE);

            }
        });

    }


    private void getPlaceId(LatLng latLng) {
        String url = "https://maps.googleapis.com/maps/api/geocode/json?latlng=" + latLng.latitude + "," + latLng.longitude + "&key=" + getResources().getString(R.string.google_maps_key);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    placeId = ((JSONArray) response.get("results")).getJSONObject(0).get("place_id").toString();
                    Log.d(TAG, "getPlaceId: place id is " + placeId);
                    getPlace(placeId);

                } catch (JSONException e) {
                    Log.d(TAG, "onResponse: error is " + e.getLocalizedMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "onErrorResponse: error is " + error.getLocalizedMessage());
            }
        });

        requestQueue.add(request);


    }

    private Place getPlace(String placeID) {
        // Define a Place ID.
        final String placeId = placeID;

        final List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS);

// Construct a request object, passing the place ID and fields array.
        final FetchPlaceRequest request = FetchPlaceRequest.newInstance(placeId, placeFields);

        PlacesClient placesClient = Places.createClient(getContext());

        placesClient.fetchPlace(request).addOnSuccessListener(new OnSuccessListener<FetchPlaceResponse>() {
            @Override
            public void onSuccess(FetchPlaceResponse fetchPlaceResponse) {
                Place thisPlace = fetchPlaceResponse.getPlace();
                Log.i(TAG, "Place found: " + thisPlace.getLatLng());
                if (thisPlace != null) {
                    thePlace = thisPlace;
                    geoLocate(thePlace);
                    autocompleteFragment.setText(thisPlace.getAddress());
                    extraInfoScrollView.setVisibility(View.VISIBLE);
                }
            }


        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ApiException) {
                    final ApiException apiException = (ApiException) e;
                    Log.d(TAG, "Place not found: " + e.getMessage());
                    final int statusCode = apiException.getStatusCode();
                    DialogAdapter.ADAPTER.displayErrorDialog(e.getLocalizedMessage(), "");
                }
            }
        });

        return thePlace;

    }

    public void switchFabSafeState(final int state) {

        if (state == Geofence.GEOFENCE_TRANSITION_EXIT) {
            fabSafe.setImageDrawable(getResources().getDrawable(R.drawable.icon_warning_red));
            Log.d(TAG, "switchFabSafeState: not safe, state is " + state);
            transitionState = state;
        } else {
            fabSafe.setImageDrawable(getResources().getDrawable(R.drawable.ic_noti_safe));
            Log.d(TAG, "switchFabSafeState: safe, state is " + state);
            transitionState = state;
        }

    }

    private void initLocationClass() {
        location = new com.hudzah.wearamask.Location("",0, 0, null, "", "");
        location.setContext(getContext());
    }

    private void initSearchText() {
        Log.d(TAG, "initSearchText: init searhctext");


        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS));
        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setHint("Search");
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                thePlace = place;
                geoLocate(place);
                extraInfoScrollView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onError(@NonNull Status status) {
                Log.d(TAG, "onError: " + status.getStatusMessage());
            }
        });

    }

    private void showLocationNameSelect() {
        Log.d(TAG, "saveLocation: saving info " + selectedColor + " " + selectedRadius);
        displayLocationNamingDialog(thePlace.getAddress());
        Log.d(TAG, "saveLocation: name of location is " + locationName);
    }

    private void displayLocationNamingDialog(final String address) {
        AlertDialog.Builder builder = new AlertDialog.Builder((Activity) getContext());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        builder.setView(inflater.inflate(R.layout.dialog_location_name, null));

        namingDialog = builder.create();
        namingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        namingDialog.show();

        final TextInputLayout locationNameInput = (TextInputLayout) namingDialog.findViewById(R.id.locationNameInput);
        final Button saveButton = (Button) namingDialog.findViewById(R.id.saveButton);
        TextView leaveBlankTextView = (TextView) namingDialog.findViewById(R.id.leaveBlankTextView);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!locationNameInput.getEditText().getText().toString().equals("")) {
                    locationName = locationNameInput.getEditText().getText().toString().trim();
                } else {
                    locationName = address;
                }

                saveLocation();

                dismissLocationNamingDialog();
            }
        });

        leaveBlankTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                locationName = address;
                dismissLocationNamingDialog();
                saveLocation();
            }
        });

    }

    private void saveLocation() {
        Log.d(TAG, "saveLocation: location name is " + locationName);
        location = new com.hudzah.wearamask.Location(
                "",
                selectedRadius,
                selectedColor,
                thePlace.getLatLng(),
                thePlace.getAddress(),
                locationName);

        location.setContext(getContext());

        locations.add(location);
        location.saveLocationToParse(thePlace);

    }

    public void dismissLocationNamingDialog() {
        namingDialog.dismiss();
    }


    public void discardLocation() {
        googleMap.clear();
        extraInfoScrollView.setVisibility(View.INVISIBLE);
        radiusSeekBar.setProgress(30);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        autocompleteFragment.setText("");
        autocompleteFragment.setHint("Search");
    }

    private void showColorPicker() {
        AmbilWarnaDialog dialog = new AmbilWarnaDialog(getContext(), getResources().getColor(R.color.colorPrimaryDark), new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onCancel(AmbilWarnaDialog dialog) {
                selectedColor = getResources().getColor(R.color.colorPrimaryDark);
            }

            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                selectedColor = color;
                colorTextView.setBackgroundTintList(ColorStateList.valueOf(selectedColor));
                CircleManager.Manager.drawCircleOnMap(selectedRadius, selectedColor, thePlace);
            }
        });

        dialog.show();

    }

    private void geoLocate(Place place) {
        CircleManager.Manager.drawCircleOnMap(selectedRadius, selectedColor, thePlace);
        moveCamera(place.getLatLng(), DEFAULT_ZOOM / 0.88f);
        Log.d(TAG, "geoLocate: place is " + place);
        updateUI();
    }

    private void updateUI() {
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

    }

    public void getLastDeviceLocation() {
        Log.d(TAG, "getDeviceLocation: get device location");

        try {
            if(allPermissionsGranted()) {

                Task location = mFusedLocationClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            currentLocation = (Location) task.getResult();
                            Log.d(TAG, "onComplete: ");
                            try {
                                Log.d(TAG, "onComplete: found location" + currentLocation.getLongitude());

                                moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), DEFAULT_ZOOM);
                            } catch (Exception e) {
                                Log.d(TAG, "onComplete: crash with error of " + e.getMessage());
                            }
                        } else {
                            Log.d(TAG, "onComplete: current location is null");
                            Toast.makeText(getContext(), "Unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

        } catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation: SecurityException " + e.getMessage());
        }
    }

    private void moveCamera(LatLng latLng, float zoom) {
        Log.d(TAG, "moveCamera: move camera to: " + latLng.latitude + " " + latLng.longitude);
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    private void checkIfLoggedIn() {
        if (ParseUser.getCurrentUser() != null) {
            notLoggedInLayout.setVisibility(View.INVISIBLE);
            loggedInLayout.setVisibility(View.VISIBLE);
            welcomeTextView.setText(getResources().getString(R.string.login_welcome) + " " + ParseUser.getCurrentUser().getUsername());

        } else {
            notLoggedInLayout.setVisibility(View.VISIBLE);
            loggedInLayout.setVisibility(View.INVISIBLE);
            if(allPermissionsGranted()) {
                CoachMarks.Manager.init(getActivity());
                CoachMarks.Manager.showLocationCoachMarks();
            }
        }
    }

    private void openBottomSheet() {
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    private void hideUpButton() {
        fabUpButton.setVisibility(View.INVISIBLE);
    }

    private void styleMap() {
        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success;
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());

            boolean darkModeEnabled = preferences.getBoolean("enable_dark_mode", false);
            if(darkModeEnabled){
                success = googleMap.setMapStyle(
                        MapStyleOptions.loadRawResourceStyle(
                                getContext(), R.raw.dark_map_style));
            } else {

                success = googleMap.setMapStyle(
                        MapStyleOptions.loadRawResourceStyle(
                                getContext(), R.raw.light_map_style));
            }

            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }
    }

    private void login() {
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        getContext().startActivity(intent);
    }

    private void goToSignUp() {
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        Intent intent = new Intent(getActivity(), SignUpActivity.class);
        getContext().startActivity(intent);
    }

    private void showOfflineMode() {
        enableOfflineLayout();
        if (ParseUser.getCurrentUser() != null && locations.isEmpty()) {
            locations = location.getLocationsFromSharedPreferences(true);
            Log.d(TAG, "showOfflineMode: locations in offline mode are " + locations);
        } else if (locations.size() > 0) {
            location.locationsArrayList = locations;
            location.drawAllLocations();
        }
        Log.d(TAG, "showOfflineMode: offline mode enabled");
        offlineModeLayout.setVisibility(View.VISIBLE);
        offlineModeLayout.setTranslationY(70f);
        offlineModeLayout.setAlpha(0.7f);
        offlineModeLayout.animate().alpha(1f).translationYBy(-70).setDuration(180);
    }

    private void showOnlineMode() {
        enableOnlineLayout();
        Log.d(TAG, "showOnlineMode: locations is " + locations);
        // retrives all locations and draws them
        if (ParseUser.getCurrentUser() != null && locations.isEmpty()) {
            Log.d(TAG, "showOnlineMode: get all locations");
            if(allPermissionsGranted()) {
                location.getAllLocations(true);
            }
        } else if (locations.size() > 0) {
            location.locationsArrayList = locations;
            location.drawAllLocations();
        }
        offlineModeLayout.setAlpha(1);
        offlineModeLayout.animate().alpha(0f).translationYBy(70).setDuration(180).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (ConnectivityReceiver.isConnected()) {
                    offlineModeLayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

    }

    private void enableOnlineLayout() {
        if (offlineLayout.getVisibility() == View.VISIBLE && loggedIn) {
            offlineLayout.setVisibility(View.INVISIBLE);
            loggedInLayout.setVisibility(View.VISIBLE);
        }
    }

    private void enableOfflineLayout() {
        if ((loggedInLayout.getVisibility() == View.VISIBLE) && loggedIn) {
            loggedInLayout.setVisibility(View.INVISIBLE);
            offlineLayout.setVisibility(View.VISIBLE);
        }
    }

    private void manualConnectionRetry() {
        Handler handler = new Handler();
        offlineModeCheckButton.resumeAnimation();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (ConnectivityReceiver.isConnected()) {
                    offlineModeCheckButton.pauseAnimation();
                    enableOnlineLayout();
                    showOnlineMode();
                } else {
                    offlineModeCheckButton.pauseAnimation();
                    Toast.makeText(getContext(), "No connections found, try again", Toast.LENGTH_SHORT).show();

                }
            }
        }, 4060);
    }

    private boolean allPermissionsGranted() {
        if (Build.VERSION.SDK_INT >= 29) {
            permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_BACKGROUND_LOCATION};
            if(EasyPermissions.hasPermissions(getContext(), permissions)){
                return true;
            }else{
                EasyPermissions.requestPermissions(getActivity(), "Background location permissions are required for wearAmask to work.",
                                                    10001, permissions);
                return false;
            }
        }
        else {
            permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
            if(EasyPermissions.hasPermissions(getContext(), permissions)){
                return true;
            }else{
                EasyPermissions.requestPermissions(getActivity(), "Background location permissions are required for wearAmask to work.",
                        10002, permissions);
                return false;
            }
        }

    }


    private void hideSoftKeyboard() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = getActivity().getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(getActivity());
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static MapFragment getInstance() {
        return instance;
    }

    private void checkSettingsAndStartLocationUpdates() {

        LocationSettingsRequest request = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest).build();
        SettingsClient client = LocationServices.getSettingsClient(getContext());
        Task<LocationSettingsResponse> locationSettingsResponseTask = client.checkLocationSettings(request);
        locationSettingsResponseTask.addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                // settings satisfied
                startLocationUpdates();
            }
        });

        locationSettingsResponseTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    ResolvableApiException apiException = (ResolvableApiException) e;
                    try {
                        apiException.startResolutionForResult(getActivity(), 1001);
                    } catch (IntentSender.SendIntentException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
    }

    private void startLocationUpdates() {
        mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private void stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(locationCallback);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if(requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE){
            if(allPermissionsGranted()){
                Log.d(TAG, "onActivityResult: Permissions granted after opened settings");
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        SharedPreferences prefs = getContext().getSharedPreferences(getContext().getPackageName(), MODE_PRIVATE);

        switchFabSafeState(prefs.getInt("transitionState", Geofence.GEOFENCE_TRANSITION_EXIT));
        Log.d(TAG, "onStart: transition state is " + prefs.getInt("transitionState", Geofence.GEOFENCE_TRANSITION_EXIT));

        if(initialView == null) {
            if(allPermissionsGranted()) {
                if (GpsLocationReceiver.checkLocationServicesEnabled(getContext())) {
                    getLastDeviceLocation();
                    //checkSettingsAndStartLocationUpdates();
                } else {
                    DialogAdapter.ADAPTER.displayErrorDialog(getContext().getResources().getString(R.string.dialog_enable_location_prompt), getContext().getResources().getString(R.string.dialog_enable_location_button));
                }
            }


        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: broadcast service started");
        final IntentFilter intentFilter = new IntentFilter();
        IntentFilter gpsIntentFilter = new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION);
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);

        connectivityReceiver = new ConnectivityReceiver();
        gpsLocationReceiver = new GpsLocationReceiver();

        getContext().registerReceiver(connectivityReceiver, intentFilter);
        getContext().registerReceiver(gpsLocationReceiver, gpsIntentFilter);

        App.getInstance().setConnectivityListener(this);
        App.getInstance().setLocationProviderListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();

        getContext().unregisterReceiver(connectivityReceiver);
        getContext().unregisterReceiver(gpsLocationReceiver);
        Log.d(TAG, "onPause: broadcast service unregistered");
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        Log.d(TAG, "onNetworkConnectionChanged: is connected = " + isConnected);
        if (!isConnected) showOfflineMode();
        else {
            showOnlineMode();
        }
    }

    private void saveLastKnownLocation() {
        lastKnownLocationGeoPoint = new ParseGeoPoint(0, 0);
        if (currentLocation != null) {
            lastKnownLocationGeoPoint = new ParseGeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude());
        }
        ParseUser currentUser = ParseUser.getCurrentUser();

        if (currentUser != null) {
            currentUser.put("lastKnownLocation", lastKnownLocationGeoPoint);
            currentUser.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        Log.d(TAG, "done: saved location successfully at " + lastKnownLocationGeoPoint);
                    }
                }
            });
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        saveLastKnownLocation();
        Log.d(TAG, "onDestroy: in here location");
    }

    @Override
    public void onStop() {
        super.onStop();
        stopLocationUpdates();
    }

    @Override
    public void onLocationProviderChanged(boolean isLocationOn) {
        Log.d(TAG, "onLocationProviderChanged: location is " + isLocationOn);
        if (isLocationOn) {
            DialogAdapter.ADAPTER.dismissErrorDialog();
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    //checkSettingsAndStartLocationUpdates();
                    getLastDeviceLocation();
                }
            }, 1000);
        } else {
            DialogAdapter.ADAPTER.displayErrorDialog(getResources().getString(R.string.dialog_enable_location_prompt), getResources().getString(R.string.dialog_enable_location_button));

        }
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        Log.d(TAG, "onPermissionsGranted: permissions granted");
        Intent intent = new Intent(getContext(), MapsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        getActivity().overridePendingTransition(0, 0);
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        Log.d(TAG, "onPermissionsDenied: permissions denied");
        if(EasyPermissions.somePermissionPermanentlyDenied(this, perms)){
            new AppSettingsDialog.Builder(this).build().show();
        }
    }

    

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }
    
    
}

