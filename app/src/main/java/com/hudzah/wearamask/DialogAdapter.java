package com.hudzah.wearamask;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class DialogAdapter {

    private Activity activity;
    private AlertDialog dialog;
    AlertDialog.Builder builder;
    LayoutInflater inflater;

    private AlertDialog errorDialog;


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

    public void displayErrorDialog(String error, String buttonText){


        builder.setView(inflater.inflate(R.layout.dialog_error, null));

        errorDialog = builder.create();
        errorDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        errorDialog.show();
        TextView errorTextView = (TextView) errorDialog.findViewById(R.id.errorTextView);
        errorTextView.setText(error);

        Button closeButton = (Button) errorDialog.findViewById(R.id.closeButton);
        if(!buttonText.equals("")) closeButton.setText(buttonText);
            closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissErrorDialog();
            }
        });

    }

    public void dismissErrorDialog(){
        errorDialog.dismiss();
    }

}
