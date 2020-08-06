package com.hudzah.wearamask;


import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

import org.w3c.dom.Text;

import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 */
public class MapFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap googleMap;
    private MapView mapView;
    private static final String TAG = "MapFragment";
    private SharedPreferences sharedPreferences;
    private boolean loggedIn;
    private RelativeLayout notLoggedInLayout;
    private RelativeLayout loggedInLayout;
    private LottieAnimationView fabUpButton;

    BottomSheetBehavior bottomSheetBehavior;
    CardView bottomSheet;
    TextView registerButton;

    private Button loginButton;

    private TextView welcomeTextView;

    public MapFragment() {
        // Required empty public constructor
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mapView.onCreate(savedInstanceState);
        mapView.onResume();

        mapView.getMapAsync(this);
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
        // Position the map's camera near Sydney, Australia.
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(-34, 151)));
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        notLoggedInLayout = (RelativeLayout) view.findViewById(R.id.notLoggedInLayout);
        loggedInLayout = (RelativeLayout) view.findViewById(R.id.loggedInLayout);
        mapView = (MapView) view.findViewById(R.id.mapView);

        bottomSheet = view.findViewById(R.id.bottomSheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

        welcomeTextView = (TextView) view.findViewById(R.id.welcomeTextView);

        loginButton = (Button) view.findViewById(R.id.loginButton);

        fabUpButton = (LottieAnimationView) view.findViewById(R.id.fabUpArrow);

        registerButton = (TextView) view.findViewById(R.id.registerButton);

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

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToSignUp();
            }
        });

        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View view, int i) {
                switch (i) {
                    case BottomSheetBehavior.STATE_HIDDEN:
                        fabUpButton.setVisibility(View.VISIBLE);
                        fabUpButton.playAnimation();
                        // TODO: 7/27/2020 float in
                        break;

                    case BottomSheetBehavior.STATE_DRAGGING:
                        hideUpButton();
                        break;

                    case BottomSheetBehavior.STATE_EXPANDED:
                        hideUpButton();
                        break;

                    case BottomSheetBehavior.STATE_HALF_EXPANDED:
                        hideUpButton();
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View view, float v) {

            }
        });
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
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
    }

    private void hideUpButton() {
        fabUpButton.setVisibility(View.INVISIBLE);
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

}
