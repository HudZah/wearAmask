package com.hudzah.wearamask;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;

public class DialogAdapter {

    private Activity activity;
    private AlertDialog dialog;
    AlertDialog.Builder builder;
    LayoutInflater inflater;


    DialogAdapter(Activity myActivity){
        activity = myActivity;
        builder = new AlertDialog.Builder(activity);
        inflater = activity.getLayoutInflater();
    }

    public void loadingDialog(){

        builder.setView(inflater.inflate(R.layout.dialog_loading, null));
        builder.setCancelable(false);

        dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }

    public void dismissLoadingDialog(){

        dialog.dismiss();
    }

    public void locationFindingDialog(){
        builder.setView(inflater.inflate(R.layout.dialog_location_finding, null));
        builder.setCancelable(false);

        dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }

    public void dismissLocationDialog(){
        dialog.dismiss();
    }


}
