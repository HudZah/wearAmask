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

import com.parse.DeleteCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;


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

        adapter.setOnLongClickListener(new LocationAdapter.OnLongClickListener() {
            @Override
            public void onLongClick(final int position) {
                androidx.appcompat.app.AlertDialog.Builder choiceBuilder = new AlertDialog.Builder(getContext());
                choiceBuilder.setCancelable(true);
                choiceBuilder.setTitle("Select an option");
                choiceBuilder.setItems(new String[]{"Delete"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(which == 0){
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

    private void deleteItem(int position){
        if(ConnectivityReceiver.isConnected()) {
            ParseQuery<ParseObject> query = ParseQuery.getQuery("Locations");
            query.whereEqualTo("username", ParseUser.getCurrentUser().getUsername());
            query.whereEqualTo("objectId", location.getLocationsArrayList().get(position).getLocationID());
            query.getFirstInBackground(new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject object, ParseException e) {
                    if (e == null) {
                        object.deleteInBackground(new DeleteCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e == null) {
                                    Toast.makeText(getContext(), "Deleted Successfully!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }
            });
            location.getLocationsArrayList().remove(position);
            adapter.notifyItemRemoved(position);
        }
        else{
            Toast.makeText(getContext(), "You need to have a valid connection to delete an item", Toast.LENGTH_SHORT).show();
        }
    }
}
