package com.hudzah.wearamask;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class DialogAdapter {

    private Activity activity;
    private AlertDialog dialog;
    AlertDialog.Builder builder;
    LayoutInflater inflater;

    private AlertDialog errorDialog;

    String name = "";


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

    public void displayErrorDialog(final String error, final String buttonText){


        builder.setView(inflater.inflate(R.layout.dialog_error, null));

        errorDialog = builder.create();
        errorDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        errorDialog.show();
        TextView errorTextView = (TextView) errorDialog.findViewById(R.id.errorTextView);
        errorTextView.setText(error);

        Button actionButton = (Button) errorDialog.findViewById(R.id.actionButton);
        ImageView closeDialogButton = (ImageView) errorDialog.findViewById(R.id.closeDialogButton);
        if(!buttonText.equals("")) actionButton.setText(buttonText);

        closeDialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissErrorDialog();
            }
        });

        actionButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(buttonText.equals("")) {
                dismissErrorDialog();
            }
            else if(buttonText.equals(activity.getApplicationContext().getResources().getString(R.string.dialog_enable_location_button))){
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                activity.getApplicationContext().startActivity(intent);
                dismissErrorDialog();
            }
        }
    });

    }

    public void dismissErrorDialog(){
        errorDialog.dismiss();
    }

}
