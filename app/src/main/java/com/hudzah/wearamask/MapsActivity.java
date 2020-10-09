package com.hudzah.wearamask;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;

import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

public class MapsActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, EasyPermissions.PermissionCallbacks {

    public Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private NavController navController;
    private NavigationView navView;
    private static final String TAG = "MapsActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_maps);

        DarkModeHandler.DARK_MODE_HANDLER.checkDarkMode(this);

        DialogAdapter.ADAPTER.initDialogAdapter(this);

        setupNavigation();
    }

    private void setupNavigation() {


        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);


        drawerLayout = findViewById(R.id.drawerLayout);

        navView = findViewById(R.id.navView);

        navController = Navigation.findNavController(this, R.id.fragment);

        NavigationUI.setupActionBarWithNavController(this, navController, drawerLayout);

        NavigationUI.setupWithNavController(navView, navController);

        navView.setNavigationItemSelectedListener(this);


    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(Navigation.findNavController(this, R.id.fragment), drawerLayout);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.help) {
            CoachMarks.Manager.init(this);
            CoachMarks.Manager.showLocationCoachMarks();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

        menuItem.setChecked(true);

        drawerLayout.closeDrawers();

        int id = menuItem.getItemId();


        switch (id) {

            case R.id.locationsFragment:
                navController.navigate(R.id.locationsFragment);

                break;
            case R.id.settingsFragment:
                navController.navigate(R.id.settingsFragment);
                break;

            case R.id.dashboardFragment:
                navController.navigate(R.id.dashboardFragment);
                break;

            case R.id.contactFragment:
                navController.navigate(R.id.contactFragment);
                break;

            case R.id.precautionsFragment:
                navController.navigate(R.id.precautionsFragment);
                break;

            case R.id.rateUs:
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("market://details?id=" + getPackageName())));
                    break;
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://play.google.com/store/apps/details?id=" + getPackageName())));
                    break;
                }

            case R.id.legalFragment:
                navController.navigate(R.id.legalFragment);
                break;

            default:
                break;

        }
        return true;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        Log.d(TAG, "onRequestPermissionsResult: call fragment onRequestPermissionsResult");
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);

    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }


    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        MapFragment.getInstance().onPermissionsGranted(requestCode, perms);
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        MapFragment.getInstance().onPermissionsDenied(requestCode, perms);
    }
}
