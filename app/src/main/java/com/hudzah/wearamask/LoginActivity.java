package com.hudzah.wearamask;

import android.app.Dialog;
import android.content.Context;
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
import com.parse.facebook.ParseFacebookUtils;
import com.parse.twitter.ParseTwitterUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Collection;

public class LoginActivity extends AppCompatActivity {

    LinearLayout layout;
    CardView cardView;
    TextInputLayout usernameInput;
    TextInputLayout passwordInput;
    String username;
    String password;
    TextView registerTextView;
    TextView forgotPasswordTextView;
    private FloatingActionButton signInGoogleButton;
    private int RC_SIGN_IN = 0;
    GoogleSignInClient mGoogleSignInClient;
    FloatingActionButton signInFacebookButton;
    FloatingActionButton signInTwitterButton;
    Dialog errorDialog;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        DarkModeHandler.DARK_MODE_HANDLER.checkDarkMode(this);

        DialogAdapter.ADAPTER.initDialogAdapter(this);

        androidx.appcompat.widget.Toolbar toolbar = (androidx.appcompat.widget.Toolbar) findViewById(R.id.toolbar);
        layout = (LinearLayout) findViewById(R.id.layout);
        cardView = (CardView) findViewById(R.id.loginCardView);
        usernameInput = (TextInputLayout) findViewById(R.id.usernameInput);
        passwordInput = (TextInputLayout) findViewById(R.id.passwordInput);
        registerTextView = (TextView) findViewById(R.id.registerButton);
        forgotPasswordTextView = (TextView)findViewById(R.id.forgotButton);
        signInGoogleButton = (FloatingActionButton) findViewById(R.id.signInGoogleButton);
        signInFacebookButton = (FloatingActionButton) findViewById(R.id.signInFacebookButton);
        signInTwitterButton = (FloatingActionButton) findViewById(R.id.signInTwitterButton);

        errorDialog = new Dialog(this);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);

        registerTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });

        forgotPasswordTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                forgotPassword();
            }
        });

        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        signInGoogleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginWithGoogle();
            }
        });

      layout.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
              hideKeyboard(v);
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

        setSupportActionBar(toolbar);

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

    private void hideKeyboard(View v){
        if (v.getId() == R.id.layout) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }

    }

    private void login(){
        username = usernameInput.getEditText().getText().toString();
        password = passwordInput.getEditText().getText().toString();
        if(validateData()){

            DialogAdapter.ADAPTER.loadingDialog();

            ParseUser.logInInBackground(username, password, new LogInCallback() {
                @Override
                public void done(ParseUser user, ParseException e) {
                    if(e == null){
                       goToMaps();
                    }
                    else{
                        DialogAdapter.ADAPTER.displayErrorDialog(e.getMessage(), "");
                    }

                    DialogAdapter.ADAPTER.dismissLoadingDialog();
                }

            });
        }
    }

    private boolean validateData(){
        boolean verified = false;
        if(usernameInput.getEditText().getText().toString().equals("")){
            verified = false;
            usernameInput.setError("Field cannot be empty!");
        }
        if(passwordInput.getEditText().getText().toString().equals("")){
            verified = false;
            passwordInput.setError("Field cannot be empty!");
        }
        else{
            verified = true;
        }

        return verified;
    }

    private void register(){
        Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
        startActivity(intent);
    }

    private void loginWithGoogle(){
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void forgotPassword(){
        Intent intent = new Intent(this, ForgotPasswordActivity.class);
        startActivity(intent);
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

    private void facebookSignUp() {
        DialogAdapter.ADAPTER.loadingDialog();

        Collection<String> permissions = Arrays.asList("public_profile", "email");
        ParseFacebookUtils.logInWithReadPermissionsInBackground(LoginActivity.this, permissions, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException err) {
                if (err != null) {
                    ParseUser.logOut();
                    DialogAdapter.ADAPTER.displayErrorDialog(err.getMessage(), "");
                }
                if (user == null) {

                    ParseUser.logOut();
                    Toast.makeText(LoginActivity.this, "The user cancelled the Facebook login.", Toast.LENGTH_LONG).show();
                    Log.d("MyApp", "Uh oh. The user cancelled the Facebook login.");
                } else if (user.isNew()) {
                    Toast.makeText(LoginActivity.this, "User signed up and logged in through Facebook.", Toast.LENGTH_LONG).show();
                    Log.d("MyApp", "User signed up and logged in through Facebook!");
                    getUserDetailFromFB();
                } else {
                    Toast.makeText(LoginActivity.this, "User logged in through Facebook.", Toast.LENGTH_LONG).show();
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

        ParseTwitterUtils.logIn(LoginActivity.this, new LogInCallback() {

            @Override
            public void done(final ParseUser user, ParseException err) {
                if (err != null) {

                    ParseUser.logOut();
                    DialogAdapter.ADAPTER.displayErrorDialog(err.getMessage(), "");

                }
                if (user == null) {

                    ParseUser.logOut();
                    Toast.makeText(LoginActivity.this, "The user cancelled the Twitter login.", Toast.LENGTH_LONG).show();
                    Log.d("MyApp", "Uh oh. The user cancelled the Twitter login.");
                } else if (user.isNew()) {

                    Toast.makeText(LoginActivity.this, "User signed up and logged in through Twitter.", Toast.LENGTH_LONG).show();
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
                    Toast.makeText(LoginActivity.this, "User logged in through Twitter.", Toast.LENGTH_LONG).show();
                    goToMaps();
                }

                DialogAdapter.ADAPTER.dismissLoadingDialog();

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
                    user.put("signUpMethod", "facebook");
                    user.put("lastKnownLocation", geoPoint);
                    user.setEmail(object.getString("email"));

                }catch(JSONException e){
                    e.printStackTrace();
                }
                user.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if(e == null) {
                            goToMaps();
                        }
                        //alertDisplayer("First Time Login", "Welcome!");
                    }
                });
            }
        });
        Bundle parameters = new Bundle();
        parameters.putString("fields","name,email");
        request.setParameters(parameters);
        request.executeAsync();
    }


    private void handleGoogleSignInResult(Task<GoogleSignInAccount> completedTask) {

        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);


        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Toast.makeText(this, "Code " + e.getStatusCode(), Toast.LENGTH_SHORT).show();
//            Log.e(TAG, "signInResult:failed code=" + e.getStatusCode());
        }

    }

    private void goToMaps(){
        Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }


}
