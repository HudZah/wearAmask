package com.hudzah.wearamask;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


public class LocationsFragment extends Fragment {

    private RecyclerView recyclerView;
    private LocationAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private static final String TAG = "LocationsFragment";

    private com.hudzah.wearamask.Location location;

    public LocationsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_locations, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        location = MapFragment.getInstance().location;

        recyclerView = view.findViewById(R.id.recyclerView);
        layoutManager = new LinearLayoutManager(getContext());
        adapter = new LocationAdapter(location.getLocationsArrayList());

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new LocationAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                NavController navController = Navigation.findNavController(getActivity(), R.id.fragment);
                LocationsFragmentDirections.ActionLocationsFragmentToEditLocationFragment action = LocationsFragmentDirections.actionLocationsFragmentToEditLocationFragment(location.getLocationsArrayList().get(position));
                navController.navigate(action);
                //navController.navigate(R.id.editLocationFragment);
            }
        });
    }
}
