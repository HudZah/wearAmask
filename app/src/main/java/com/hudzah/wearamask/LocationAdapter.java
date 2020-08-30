package com.hudzah.wearamask;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.LocationViewHolder> {

    private ArrayList<Location> mLocationsList;
    private static final String TAG = "LocationAdapter";
    public static Context context;

    private OnItemClickListener mListener;

    private OnLongClickListener mLongClickListener;

    public interface OnItemClickListener{
        void onItemClick(int position);
    }

    public interface OnLongClickListener{
        void onLongClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        mListener = listener;
    }

    public void setOnLongClickListener(OnLongClickListener longClickListener){
        mLongClickListener = longClickListener;
    }

    public static class LocationViewHolder extends RecyclerView.ViewHolder {


        public ImageView mLocationColorImageView;
        public TextView mLocationNameTextView;
        public TextView mLocationAddressTextView;
        public TextView mLocationRadiusTextView;

        public LocationViewHolder(@NonNull View itemView, final OnItemClickListener listener, final OnLongClickListener longClickListener) {
            super(itemView);
            mLocationColorImageView = (ImageView) itemView.findViewById(R.id.locationColorImageView);
            mLocationNameTextView = (TextView) itemView.findViewById(R.id.locationNameTextView);
            mLocationAddressTextView = (TextView) itemView.findViewById(R.id.locationAddressTextView);
            mLocationRadiusTextView = (TextView) itemView.findViewById(R.id.locationRadiusTextView);
            LocationAdapter.context = itemView.getContext();

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(listener != null){
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION){
                            listener.onItemClick(position);
                        }
                    }
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if(longClickListener != null){
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION){
                            longClickListener.onLongClick(position);
                        }
                    }

                    return false;
                }
            });
        }
    }

    public LocationAdapter(ArrayList<Location> locationArrayList){
        mLocationsList = locationArrayList;

    }

    @NonNull
    @Override
    public LocationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_location_item, parent, false);
        LocationViewHolder lvh = new LocationViewHolder(v, mListener, mLongClickListener);
        return  lvh;
    }

    @Override
    public void onBindViewHolder(@NonNull LocationViewHolder holder, int position) {
        Location currentItem = mLocationsList.get(position);

        int r = Color.red(currentItem.getSelectedColor());
        int g = Color.green(currentItem.getSelectedColor());
        int b = Color.blue(currentItem.getSelectedColor());
        int alpha = Color.alpha(currentItem.getSelectedColor());

        holder.mLocationColorImageView.setColorFilter(Color.argb(alpha, r, g, b));
        holder.mLocationRadiusTextView.setText(currentItem.getSelectedRadius() + "m");
        holder.mLocationAddressTextView.setText(currentItem.getAddress());
        holder.mLocationNameTextView.setText(currentItem.getLocationName());

        if((Color.rgb(r,g, b) != -13382401)){
            Log.d(TAG, "onBindViewHolder: both colors are " + Color.rgb(r, g, b) + context.getResources().getColor(R.color.colorPrimaryDark));
            holder.mLocationRadiusTextView.setTextColor(Color.argb(alpha, r, g, b));
            holder.mLocationAddressTextView.setTextColor(Color.argb(alpha, r, g, b));
        }

    }



    @Override
    public int getItemCount() {
        return mLocationsList.size();
    }
}
