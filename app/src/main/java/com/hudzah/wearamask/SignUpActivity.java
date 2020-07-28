package com.hudzah.wearamask;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;

import com.google.android.material.textfield.TextInputLayout;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

public class SignUpActivity extends AppCompatActivity {

    LinearLayout layout;
    CardView signupCardView;
    String username;
    String password;
    String email;
    TextInputLayout usernameInput;
    TextInputLayout passwordInput;
    ParseUser user;
    TextInputLayout emailInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        usernameInput = (TextInputLayout) findViewById(R.id.usernameInput);
        passwordInput = (TextInputLayout) findViewById(R.id.passwordInput);
        emailInput = (TextInputLayout) findViewById(R.id.emailInput);

        androidx.appcompat.widget.Toolbar toolbar = (androidx.appcompat.widget.Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        layout = (LinearLayout) findViewById(R.id.layout);
        signupCardView = (CardView) findViewById(R.id.signupCardView);

        signupCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signup();
            }
        });

        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
            }
        });

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    private void hideKeyboard(){
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if(getCurrentFocus() != null) {
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    private void signup(){
        if(validateData()){
            username = usernameInput.getEditText().getText().toString();
            password = passwordInput.getEditText().getText().toString();
            email = emailInput.getEditText().getText().toString();

            ParseUser.logOut();
            user = new ParseUser();

            user.setUsername(username);
            user.setEmail(email);
            user.setPassword(password);
            
            user.signUpInBackground(new SignUpCallback() {
                @Override
                public void done(ParseException e) {
                    if(e == null){
                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                    else{
                        // TODO: 7/28/2020 Show custom error dialog 
                    }
                }
            });
        }
    }

    private boolean validateData(){
        boolean verified = false;
        if(!usernameInput.getEditText().getText().toString().matches("^(?=.{5,20}$)(?![_.])(?!.*[_.]{2})[a-zA-Z0-9._]+(?<![_.])$")){
            verified = false;
            usernameInput.setError(getResources().getString(R.string.error_username_length));
        }

        if(!emailInput.getEditText().getText().toString().matches("^(.+)@(.+)$")){
            verified = false;
            emailInput.setError(getResources().getString(R.string.error_email_format));
        }
        if(!(passwordInput.getEditText().getText().toString().length() > 7)){
            verified = false;
            passwordInput.setError(getResources().getString(R.string.error_password_length));
        }

        else{
            verified = true;
        }

        return verified;


    }


}
