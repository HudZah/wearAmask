package com.hudzah.wearamask;


import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


public class LocationsFragment extends Fragment {

    private RecyclerView recyclerView;
    private LocationAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private static final String TAG = "LocationsFragment";

    private com.hudzah.wearamask.Location location;

    GeofenceBroadcastReceiver geofenceBroadcastReceiver;

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

        geofenceBroadcastReceiver = new GeofenceBroadcastReceiver();

        adapter.setOnItemClickListener(new LocationAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                NavController navController = Navigation.findNavController(getActivity(), R.id.fragment);
                LocationsFragmentDirections.ActionLocationsFragmentToEditLocationFragment action = LocationsFragmentDirections.actionLocationsFragmentToEditLocationFragment(location.getLocationsArrayList().get(position));
                navController.navigate(action);
                //navController.navigate(R.id.editLocationFragment);
            }
        });

        adapter.setOnLongClickListener(new LocationAdapter.OnLongClickListener() {
            @Override
            public void onLongClick(final int position) {
                androidx.appcompat.app.AlertDialog.Builder choiceBuilder = new AlertDialog.Builder(getContext());
                choiceBuilder.setCancelable(true);
                choiceBuilder.setTitle("Select an option");
                choiceBuilder.setItems(new String[]{"Delete"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            deleteItem(position);
                        }
                    }
                }).show();
            }
        });

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                deleteItem(viewHolder.getAdapterPosition());
            }
        }).attachToRecyclerView(recyclerView);
    }

    private void deleteItem(int position) {
        DialogAdapter.ADAPTER.loadingDialog();
        LocationRepository locationRepository = new LocationRepository(getContext());
        locationRepository.delete(location.getLocationsArrayList().get(position));
        location.getLocationsArrayList().remove(position);
        adapter.notifyItemRemoved(position);
        MapFragment.getInstance().discardLocation();
        CircleManager.Manager.clearGeofences();
        location.getAllLocations(true);
        DialogAdapter.ADAPTER.dismissLoadingDialog();
        Toast.makeText(getContext(), "Deleted successfully", Toast.LENGTH_SHORT).show();

    }
}
