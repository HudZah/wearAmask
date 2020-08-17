package com.hudzah.wearamask;


import android.Manifest;
import android.animation.Animator;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.Arrays;

import yuku.ambilwarna.AmbilWarnaDialog;


/**
 * A simple {@link Fragment} subclass.
 */
public class MapFragment extends Fragment implements OnMapReadyCallback, ConnectivityReceiver.ConnectivityReceiverListener, GpsLocationReceiver.GpsLocationReceiverListener {

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

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int PLACES_CODE = 101;
    private Boolean mLocationsPermissionsGranted = false;
    private static final int PERMISSION_REQUEST_CODE = 1234;
    String[] PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    String[] PERMISSIONS_API_29 = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION};
    private FusedLocationProviderClient mFusedLocationClient;

    private SeekBar radiusSeekBar;
    private FloatingActionButton colorTextView;
    private Button saveButton;
    private TextView discardTextView;
    private ScrollView extraInfoScrollView;
    private TextView radiusTextView;

    private static final float DEFAULT_ZOOM = 18f;
    private static final float DEFAULT_RADIUS = 14f;

    private int selectedRadius = 15;
    private int selectedColor = 3394815;

    private Place thePlace;
    public CircleManager circleManager;

    private static MapFragment instance;
    
    private CardView offlineModeLayout;

    ParseGeoPoint lastKnownLocationGeoPoint;

    private View layout;

    private com.hudzah.wearamask.Location location;

    public ArrayList<com.hudzah.wearamask.Location> locations = new ArrayList<>();

    private LottieAnimationView offlineModeCheckButton;

    ConnectivityReceiver connectivityReceiver;
    GpsLocationReceiver gpsLocationReceiver;

    Location currentLocation;

    public static DialogAdapter dialogAdapter;

    public static GeofencingClient geofencingClient;
    public static GeofenceHelper geofenceHelper;


    public MapFragment() {
        // Required empty public constructor
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        dialogAdapter = new DialogAdapter(getActivity());

        if(allPermissionsGranted()) {
            mapView.onCreate(savedInstanceState);
            mapView.onResume();
            mapView.getMapAsync(this);
        }
        else{
            if(Build.VERSION.SDK_INT >= 29){
                requestPermissions(PERMISSIONS_API_29, PERMISSION_REQUEST_CODE);
            }
            requestPermissions(PERMISSIONS, PERMISSION_REQUEST_CODE);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        Log.d(TAG, "onMapReady: map is ready");

        circleManager = new CircleManager(getContext(), googleMap);


        styleMap();
        if(allPermissionsGranted()){
            if(GpsLocationReceiver.checkLocationServicesEnabled(getContext())) {
                getLastDeviceLocation();
                googleMap.setMyLocationEnabled(true);
                googleMap.getUiSettings().setMyLocationButtonEnabled(false);
                googleMap.getUiSettings().setCompassEnabled(false);

                // retrives all locations and draws them
                if (ParseUser.getCurrentUser() != null) {
                    if (ConnectivityReceiver.isConnected()) {
                        location.getAllLocations(true);
                    } else {
                        locations = location.getLocationsFromSharedPreferences(true);
                        Log.d(TAG, "onMapReady: locations in offline mode are " + locations);
                    }

                }
            }
            else{
                dialogAdapter.displayErrorDialog(getContext().getResources().getString(R.string.dialog_enable_location_prompt), getContext().getResources().getString(R.string.dialog_enable_location_button));
            }

        }


    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initLocationClass();

        if (!Places.isInitialized()) {
            Places.initialize(getContext(), getResources().getString(R.string.google_maps_key));
        }

        instance = this;

        if(ParseUser.getCurrentUser() != null) loggedIn = true;
        else loggedIn = false;

        notLoggedInLayout = (RelativeLayout) view.findViewById(R.id.notLoggedInLayout);
        loggedInLayout = (RelativeLayout) view.findViewById(R.id.loggedInLayout);
        offlineLayout = (RelativeLayout) view.findViewById(R.id.offlineLayout);
        mapView = (MapView) view.findViewById(R.id.mapView);

        geofencingClient = LocationServices.getGeofencingClient(getContext());

        geofenceHelper = new GeofenceHelper(getContext());

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
                saveLocation();
            }
        });

        discardTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                discardLocation();
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
                if(GpsLocationReceiver.checkLocationServicesEnabled(getContext())) {
                    getLastDeviceLocation();
                }
                else{
                    dialogAdapter.displayErrorDialog(getContext().getResources().getString(R.string.dialog_enable_location_prompt), getContext().getResources().getString(R.string.dialog_enable_location_button));
                }
            }
        });

        radiusSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                selectedRadius = progress;
                radiusTextView.setText("Radius is " + progress + "m");
                if(thePlace != null) {
                    circleManager.drawCircleOnMap(selectedRadius, selectedColor, thePlace);
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


    }

    private void initLocationClass(){
        location = new com.hudzah.wearamask.Location( 0, 0, null, "");
        location.setContext(getContext());
    }

    private void initSearchText(){
        Log.d(TAG, "initSearchText: init searhctext");


        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS));

        // Set up a PlaceSelectionListener to handle the response.
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

    private void saveLocation(){
        // TODO: 8/9/2020 Create object of location
        Log.d(TAG, "saveLocation: saving info " + selectedColor + " " + selectedRadius);
        location = new com.hudzah.wearamask.Location(
                selectedRadius,
                selectedColor,
                thePlace.getLatLng(),
                thePlace.getAddress());

        locations.add(location);
        location.saveLocationToParse(thePlace);

    }

    public void discardLocation(){
        googleMap.clear();
        extraInfoScrollView.setVisibility(View.INVISIBLE);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        autocompleteFragment.setText("");
        // TODO: 8/9/2020 show only already saved locations and discard others
    }

    private void showColorPicker(){
        AmbilWarnaDialog dialog = new AmbilWarnaDialog(getContext(), getResources().getColor(R.color.colorPrimaryDark), new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onCancel(AmbilWarnaDialog dialog) {
                selectedColor = getResources().getColor(R.color.colorPrimaryDark);
            }

            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                selectedColor = color;
                colorTextView.setBackgroundTintList(ColorStateList.valueOf(selectedColor));
                circleManager.drawCircleOnMap(selectedRadius, selectedColor, thePlace);
            }
        });

        dialog.show();

    }

    private void geoLocate(Place place){

        moveCamera(place.getLatLng(), DEFAULT_ZOOM/0.88f);
        circleManager.drawCircleOnMap(selectedRadius, selectedColor, thePlace);
        updateUI();
    }

    private void updateUI(){

        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

    }

    public void getLastDeviceLocation(){
        Log.d(TAG, "getDeviceLocation: get device location");

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());

        try{
            if(allPermissionsGranted()){

                Task location = mFusedLocationClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if(task.isSuccessful()){
                            currentLocation = (Location) task.getResult();
                            Log.d(TAG, "onComplete: ");
                            try {
                                Log.d(TAG, "onComplete: found location" + currentLocation.getLongitude());

                                moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), DEFAULT_ZOOM);
                            }
                            catch (Exception e){
                                Log.d(TAG, "onComplete: crash with error of " + e.getMessage());
                            }
                        }
                        else{
                            Log.d(TAG, "onComplete: current location is null");
                            Toast.makeText(getContext(), "Unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }
        catch (SecurityException e){
            Log.e(TAG, "getDeviceLocation: SecurityException " + e.getMessage());
        }
    }

    private void moveCamera(LatLng latLng, float zoom){
        Log.d(TAG, "moveCamera: move camera to: " + latLng.latitude + " " + latLng.longitude);
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    private void checkIfLoggedIn() {
        if (ParseUser.getCurrentUser() != null) {
            notLoggedInLayout.setVisibility(View.INVISIBLE);
            loggedInLayout.setVisibility(View.VISIBLE);
            welcomeTextView.append(" " + ParseUser.getCurrentUser().getUsername());

        } else {
            notLoggedInLayout.setVisibility(View.VISIBLE);
            loggedInLayout.setVisibility(View.INVISIBLE);
        }
    }

    private void openBottomSheet() {
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    private void hideUpButton() {
        fabUpButton.setVisibility(View.INVISIBLE);
    }

    private void styleMap(){
        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            getContext(), R.raw.light_map_style));

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

    private void goToSignUp(){
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        Intent intent = new Intent(getActivity(), SignUpActivity.class);
        getContext().startActivity(intent);
    }

    private void showOfflineMode(){
        enableOfflineLayout();
        Log.d(TAG, "showOfflineMode: offline mode enabled");
        offlineModeLayout.setVisibility(View.VISIBLE);
        offlineModeLayout.setTranslationY(70f);
        offlineModeLayout.setAlpha(0.7f);
        offlineModeLayout.animate().alpha(1f).translationYBy(-70).setDuration(180);
    }

    private void showOnlineMode(){
        enableOnlineLayout();
        //location.getAllLocations(true);
        offlineModeLayout.setAlpha(1);
        offlineModeLayout.animate().alpha(0f).translationYBy(70).setDuration(180).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if(ConnectivityReceiver.isConnected()) {
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

    private void enableOnlineLayout(){
        if(offlineLayout.getVisibility() == View.VISIBLE && loggedIn) {
            offlineLayout.setVisibility(View.INVISIBLE);
            loggedInLayout.setVisibility(View.VISIBLE);
        }
    }

    private void enableOfflineLayout(){
        if((loggedInLayout.getVisibility() == View.VISIBLE) && loggedIn){
            loggedInLayout.setVisibility(View.INVISIBLE);
            offlineLayout.setVisibility(View.VISIBLE);
        }
    }

    private void manualConnectionRetry(){
        Handler handler = new Handler();
        offlineModeCheckButton.resumeAnimation();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(ConnectivityReceiver.isConnected()){
                    offlineModeCheckButton.pauseAnimation();
                    enableOnlineLayout();
                    showOnlineMode();
                }
                else{
                    offlineModeCheckButton.pauseAnimation();
                    Toast.makeText(getContext(), "No connections found, try again", Toast.LENGTH_SHORT).show();

                }
            }
        }, 4060);
    }

    private boolean allPermissionsGranted(){

        for(String permission : PERMISSIONS){
            if(ContextCompat.checkSelfPermission(getContext(), permission) != PackageManager.PERMISSION_GRANTED){
                return false;
            }
        }

        if(Build.VERSION.SDK_INT >= 29){
            if(ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                    != PackageManager.PERMISSION_GRANTED){
                return false;
            }
        }


        Log.d(TAG, "allPermissionsGranted: all permissions granted!");
        return true;
    }

    private void hideSoftKeyboard(){
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = getActivity().getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(getActivity());
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static MapFragment getInstance(){
        return instance;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        Log.d(TAG, "onRequestPermissionsResult: code is " + requestCode);
        if(requestCode == PERMISSION_REQUEST_CODE){
            Log.d(TAG, "onRequestPermissionsResult: matching request code");
            if(allPermissionsGranted()){
                // TODO: 8/7/2020 init map
                Intent intent = new Intent(getContext(), MapsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            } else{

                Toast.makeText(getContext(), "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
            }
        }
        else{
            Log.d(TAG, "onRequestPermissionsResult: not matching request code");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
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
        if(!isConnected) showOfflineMode();
        else {
            showOnlineMode();
        }
    }

    private void saveLastKnownLocation(){
        lastKnownLocationGeoPoint = new ParseGeoPoint(0, 0);
        if(currentLocation != null) {
            lastKnownLocationGeoPoint = new ParseGeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude());
        }
        ParseUser currentUser = ParseUser.getCurrentUser();

        if(currentUser != null){
            currentUser.put("lastKnownLocation", lastKnownLocationGeoPoint);
            currentUser.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if(e == null){
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
    public void onLocationProviderChanged(boolean isLocationOn) {
        Log.d(TAG, "onLocationProviderChanged: location is " + isLocationOn);
        if(isLocationOn){
            dialogAdapter.dismissErrorDialog();
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    getLastDeviceLocation();
                }
            }, 1000);
        }
        else{
            dialogAdapter.displayErrorDialog(getResources().getString(R.string.dialog_enable_location_prompt), getResources().getString(R.string.dialog_enable_location_button));

        }
    }
}

