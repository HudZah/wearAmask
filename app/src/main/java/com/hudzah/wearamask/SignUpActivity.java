package com.hudzah.wearamask;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentManager;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;
import com.parse.facebook.ParseFacebookUtils;
import com.parse.twitter.ParseTwitterUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Collection;

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
    FloatingActionButton signInGoogleButton;
    GoogleSignInClient mGoogleSignInClient;
    private int RC_SIGN_IN = 0;
    private static final String TAG = "SignUpActivity";
    FloatingActionButton signInFacebookButton;
    FloatingActionButton signInTwitterButton;
    Dialog errorDialog;
    TextView registerButton;
    TextView termsAgreement;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        DarkModeHandler.DARK_MODE_HANDLER.checkDarkMode(this);

        DialogAdapter.ADAPTER.initDialogAdapter(this);

        final LegalFragment legalFragment = new LegalFragment();
        final FragmentManager fragmentManager = getSupportFragmentManager();

        usernameInput = (TextInputLayout) findViewById(R.id.usernameInput);
        passwordInput = (TextInputLayout) findViewById(R.id.passwordInput);
        emailInput = (TextInputLayout) findViewById(R.id.emailInput);
        signInGoogleButton = (FloatingActionButton) findViewById(R.id.signInGoogleButton);
        signInFacebookButton = (FloatingActionButton) findViewById(R.id.signInFacebookButton);
        signInTwitterButton = (FloatingActionButton) findViewById(R.id.signInTwitterButton);
        errorDialog = new Dialog(this);
        registerButton = (TextView) findViewById(R.id.registerButton);
        termsAgreement = (TextView) findViewById(R.id.termsAgreement);

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

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard(v);
            }
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        signInGoogleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                googleSignUp();
            }
        });

        signInFacebookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                facebookSignUp();
            }
        });

        signInTwitterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                twitterSignUp();
            }
        });

        termsAgreement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Go to terms and conditions
                DialogAdapter.ADAPTER.displayLegal();
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

    private void hideKeyboard(View view){
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if(getCurrentFocus() != null) {
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void googleSignUp(){

    }

    private void facebookSignUp() {
        Collection<String> permissions = Arrays.asList("public_profile", "email");
        DialogAdapter.ADAPTER.loadingDialog();
        ParseFacebookUtils.logInWithReadPermissionsInBackground(SignUpActivity.this, permissions, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException err) {
                if (err != null) {
                    ParseUser.logOut();
                    DialogAdapter.ADAPTER.displayErrorDialog(err.getMessage(), "");
                }
                if (user == null) {
                    ParseUser.logOut();
                    Log.d("MyApp", "Uh oh. The user cancelled the Facebook login.");
                } else if (user.isNew()) {
                    Toast.makeText(SignUpActivity.this, "User signed up and logged in through Facebook.", Toast.LENGTH_LONG).show();
                    Log.d("MyApp", "User signed up and logged in through Facebook!");
                    getUserDetailFromFB();
                } else {
                    Toast.makeText(SignUpActivity.this, "User logged in through Facebook.", Toast.LENGTH_LONG).show();
                    Log.d("MyApp", "User logged in through Facebook!");
                    goToMaps();

                }

                DialogAdapter.ADAPTER.dismissLoadingDialog();
            }

        });
    }

    private void twitterSignUp(){
        final ParseGeoPoint geoPoint = new ParseGeoPoint(0 , 0);
        DialogAdapter.ADAPTER.loadingDialog();

        ParseTwitterUtils.logIn(SignUpActivity.this, new LogInCallback() {

            @Override
            public void done(final ParseUser user, ParseException err) {
                if (err != null) {

                    ParseUser.logOut();
                    DialogAdapter.ADAPTER.displayErrorDialog(err.getMessage(), "");

                }
                if (user == null) {

                    ParseUser.logOut();
                    Log.d("MyApp", "Uh oh. The user cancelled the Twitter login.");
                } else if (user.isNew()) {

                    Toast.makeText(SignUpActivity.this, "User signed up and logged in through Twitter.", Toast.LENGTH_LONG).show();
                    user.setUsername(ParseTwitterUtils.getTwitter().getScreenName());
                    user.put("signUpMethod", "twitter");
                    user.put("lastKnownLocation", geoPoint);
                    user.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (null == e) {

                                goToMaps();

                            } else {
                                ParseUser.logOut();
                                DialogAdapter.ADAPTER.displayErrorDialog(e.getMessage(), "");
                            }
                        }
                    });
                } else {
                    Toast.makeText(SignUpActivity.this, "User logged in through Twitter.", Toast.LENGTH_LONG).show();
                    goToMaps();
                }

                DialogAdapter.ADAPTER.dismissLoadingDialog();

            }
        });
    }

    private void signup(){
        if(validateData()){
            username = usernameInput.getEditText().getText().toString();
            password = passwordInput.getEditText().getText().toString();
            email = emailInput.getEditText().getText().toString();

           saveToParse("normal");
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleGoogleSignInResult(task);
        }
    }

    private void handleGoogleSignInResult(Task<GoogleSignInAccount> completedTask) {

        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.
            Toast.makeText(this, account.getDisplayName(), Toast.LENGTH_SHORT).show();
            username = account.getDisplayName();
            email = account.getEmail();
            Log.d(TAG, "handleGoogleSignInResult: Password is " + password);

            saveToParse("google");
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Toast.makeText(this, "Code " + e.getStatusCode(), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "signInResult:failed code=" + e.getStatusCode());
        }

    }

    private void saveToParse(final String method){
        ParseUser.logOut();
        user = new ParseUser();

        DialogAdapter.ADAPTER.loadingDialog();

        ParseGeoPoint geoPoint = new ParseGeoPoint(0 , 0);

        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        user.put("signUpMethod", method);
        user.put("lastKnownLocation", geoPoint);

        user.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                if(e == null){

                    DialogAdapter.ADAPTER.dismissLoadingDialog();

                    if(!method.equals("google")) {
                        Toast.makeText(SignUpActivity.this, "Account created, continue by logging in", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                    else{
                        goToMaps();
                    }
                }
                else{
                    DialogAdapter.ADAPTER.dismissLoadingDialog();
                    Log.d(TAG, "done: " + e.getMessage());
                    DialogAdapter.ADAPTER.displayErrorDialog(e.getMessage(), "");
                }


            }
        });
    }

    void getUserDetailFromFB(){
        final ParseGeoPoint geoPoint = new ParseGeoPoint(0 , 0);
        GraphRequest request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(),new  GraphRequest.GraphJSONObjectCallback(){
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                final ParseUser user = ParseUser.getCurrentUser();
                try{
                    user.setUsername(object.getString("name"));
                    user.setEmail(object.getString("email"));
                    user.put("signUpMethod", "facebook");
                    user.put("lastKnownLocation", geoPoint);

                }catch(JSONException e){
                    e.printStackTrace();
                }

                user.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if(e == null) {
                            goToMaps();
                        }
                        else{
                            DialogAdapter.ADAPTER.displayErrorDialog(e.getMessage(), "");
                        }


                    }
                });
            }
        });
        Bundle parameters = new Bundle();
        parameters.putString("fields","name,email");
        request.setParameters(parameters);
        request.executeAsync();
    }

    private void goToMaps(){
        Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }





}
