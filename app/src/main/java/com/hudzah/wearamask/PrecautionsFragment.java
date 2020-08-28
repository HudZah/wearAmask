package com.hudzah.wearamask;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


/**
 * A simple {@link Fragment} subclass.
 */
public class PrecautionsFragment extends Fragment {

    TextView maskTextView;
    TextView washHandsTextView;
    TextView socialDistanceTextView;


    public PrecautionsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_precautions, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        maskTextView = (TextView) view.findViewById(R.id.masksTextView);
        washHandsTextView = (TextView) view.findViewById(R.id.washHandsTextView);
        socialDistanceTextView = (TextView) view.findViewById(R.id.socialDistanceTextView);

        maskTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent("android.intent.action.VIEW",
                        Uri.parse("https://www.who.int/emergencies/diseases/novel-coronavirus-2019/advice-for-public/when-and-how-to-use-masks"));
                startActivity(intent);

            }
        });

        washHandsTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent("android.intent.action.VIEW",
                        Uri.parse("https://www.who.int/gpsc/clean_hands_protection/en/"));
                startActivity(intent);

            }
        });

        socialDistanceTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent("android.intent.action.VIEW",
                        Uri.parse("https://www.cdc.gov/coronavirus/2019-ncov/prevent-getting-sick/social-distancing.html#:~:text=Social%20distancing%2C%20also%20called%20“physical,both%20indoor%20and%20outdoor%20spaces."));
                startActivity(intent);
            }
        });
    }
}
