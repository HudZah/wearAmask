package com.hudzah.wearamask;


import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;

import yuku.ambilwarna.AmbilWarnaDialog;


/**
 * A simple {@link Fragment} subclass.
 */
public class EditLocationFragment extends Fragment {


    RelativeLayout layout;
    FloatingActionButton selectColorTextView;

    TextInputLayout locationNameInput;

    private int selectedColor = 3394815;

    SeekBar radiusSeekBar;
    TextView radiusTextView;
    TextView discardTextView;

    Button saveButton;

    Location location;

    private static final String TAG = "EditLocationFragment";

    public EditLocationFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit_location, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        layout = (RelativeLayout) view.findViewById(R.id.layout);
        selectColorTextView = (FloatingActionButton) view.findViewById(R.id.selectColorTextView);

        radiusSeekBar = (SeekBar) view.findViewById(R.id.radiusSeekBar);
        radiusTextView = (TextView) view.findViewById(R.id.radiusTextView);
        discardTextView = (TextView) view.findViewById(R.id.discardTextView);

        locationNameInput = view.findViewById(R.id.locationNameInput);

        saveButton = (Button) view.findViewById(R.id.saveButton);

        if (getArguments() != null) {
            EditLocationFragmentArgs args = EditLocationFragmentArgs.fromBundle(getArguments());
            Log.d(TAG, "onViewCreated: args " + args.getLocation().toString());
            location = args.getLocation();
            initUI();
        }


        selectColorTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showColorPicker();
            }
        });

        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard(v);
            }
        });

        radiusSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                radiusTextView.setText("Radius is " + progress + "m");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!locationNameInput.getEditText().getText().equals("")) {
                    if (ConnectivityReceiver.isConnected()) {
                        updateLocationToParse();
                    } else {
                        Toast.makeText(getContext(), "Connection required!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private void updateLocationToParse() {
        LocationRepository locationRepository = new LocationRepository(getContext());
        Location newLocation = new Location(radiusSeekBar.getProgress(), selectedColor,
                location.getLatitude(), location.getLongitude(), location.getAddress(),
                locationNameInput.getEditText().getText().toString());
        newLocation.setLocationID(location.getLocationID());
        locationRepository.update(newLocation);
        NavController navController = Navigation.findNavController(getActivity(), R.id.fragment);
        navController.navigate(R.id.mapFragment);
        Log.d(TAG, "updateLocationToParse: updated successfully");
        Toast.makeText(getContext(), "Updated successfully", Toast.LENGTH_SHORT).show();
    }


    private void initUI() {
        locationNameInput.getEditText().setText(location.getLocationName());
        if (location.getSelectedColor() != selectedColor) {
            selectColorTextView.setBackgroundTintList(ColorStateList.valueOf(location.getSelectedColor()));
            selectedColor = location.getSelectedColor();
        }
        radiusSeekBar.setProgress(location.getSelectedRadius());
        radiusTextView.setText("Radius is " + location.getSelectedRadius() + "m");
    }

    private void hideKeyboard(View v) {
        if (v.getId() == R.id.layout) {
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if(imm != null) imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
        }

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
                selectColorTextView.setBackgroundTintList(ColorStateList.valueOf(selectedColor));
            }
        });

        dialog.show();

    }

}
