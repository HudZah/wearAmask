package com.hudzah.wearamask;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public enum DialogAdapter {
    ADAPTER;

    private Activity activity;
    private AlertDialog dialog;
    LayoutInflater inflater;

    String name = "";




    public void initDialogAdapter(Activity myActivity){
        activity = myActivity;
        inflater = activity.getLayoutInflater();
    }

    public void loadingDialog(){

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        builder.setView(inflater.inflate(R.layout.dialog_loading, null));
        builder.setCancelable(false);

        Log.d("Tag", "loadingDialog: coming from " + activity);

        dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }

    public void dismissLoadingDialog(){

        dialog.dismiss();
        Log.d("Tag", "dismissLoadingDialog: called");
    }

    public void locationFindingDialog(){

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);


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

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);


        builder.setView(inflater.inflate(R.layout.dialog_error, null));

        dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
        TextView errorTextView = (TextView) dialog.findViewById(R.id.errorTextView);
        errorTextView.setText(error);

        Button actionButton = (Button) dialog.findViewById(R.id.actionButton);
        ImageView closeDialogButton = (ImageView) dialog.findViewById(R.id.closeDialogButton);
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
        dialog.dismiss();
    }

    public void displaySafeDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);


        builder.setView(inflater.inflate(R.layout.dialog_safe, null));

        dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

        Button closeDialogButton = (Button) dialog.findViewById(R.id.closeDialogButton);
        closeDialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissSafeAndWarningDialog();
            }
        });
    }

    public void displayWarningDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);


        builder.setView(inflater.inflate(R.layout.dialog_warning, null));

        dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

        ImageView closeDialogButton = (ImageView) dialog.findViewById(R.id.closeDialogButton);
        Button actionButton = (Button) dialog.findViewById(R.id.actionButton);

        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("https://www.who.int/emergencies/diseases/novel-coronavirus-2019/advice-for-public"); // missing 'http://' will cause crashed
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                activity.startActivity(intent);
                dismissSafeAndWarningDialog();

            }
        });

        closeDialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissSafeAndWarningDialog();
            }
        });
    }

    public void dismissSafeAndWarningDialog(){
        dialog.dismiss();
    }

}
