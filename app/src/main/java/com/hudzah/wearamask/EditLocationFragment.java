package com.hudzah.wearamask;


import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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
    }

    private void hideKeyboard(View v){
        if (v.getId() == R.id.layout) {
            InputMethodManager imm = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
        }

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
                selectColorTextView.setBackgroundTintList(ColorStateList.valueOf(selectedColor));
            }
        });

        dialog.show();

    }

}
