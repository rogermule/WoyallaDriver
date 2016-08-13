package com.brainup.woyalladriver.Activities;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.brainup.woyalladriver.Database.Database;
import com.brainup.woyalladriver.R;
import com.brainup.woyalladriver.Services.GPSTracker;
import com.brainup.woyalladriver.WoyallaDriver;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {

    /**
     * object delarations
     */
    private GoogleMap mMap; //map object
    SupportMapFragment mapFragment; //fragment that holds the map object
    Button showClient;   //button to show the client location
    Switch avaialbilitySwitch;    //toggle button to switch the drivers availability on or off
    GPSTracker gps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //initialize the navigation drawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //handle map initialization
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //initialize availability switch button
        avaialbilitySwitch = (Switch) findViewById(R.id.availability);

        //initialize the show client button
        showClient = (Button) findViewById(R.id.showclient);

        //initialize the gps tracker object
        gps  = new GPSTracker(this);

        checkGPS();
        initAvailabilitySwitch();
        handleAvailabilitySwitch();
        handleShowClientButton();
    }

    private void checkGPS() {
        if(!gps.canGetLocation()){
            gps.showSettingsAlert();
        }
    }

    /**
     * this wil initialize the switch
     * if status is online (1), set the switch checked true
     * if status is offline (0), set the switch checked false
     * return: void
     */

    private void initAvailabilitySwitch() {

        String status = WoyallaDriver.myDatabase.get_Value_At_Top(Database.Table_USER,Database.USER_FIELDS[8]);
        if(status.startsWith("1")){
                avaialbilitySwitch.setChecked(true);
        }
        else if (status.startsWith("0")){
            avaialbilitySwitch.setChecked(false);
        }

    }

    /**
        this method handle the availability switch
        return: void
    */
    private void handleAvailabilitySwitch() {
        avaialbilitySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                int id = WoyallaDriver.myDatabase.get_Top_ID(Database.Table_USER);

                if(isChecked){
                    ContentValues cv = new ContentValues();
                    cv.put(Database.USER_FIELDS[8],"1");
                    long check = WoyallaDriver.myDatabase.update(Database.Table_USER,cv,id);
                    if(check!=-1){
                        Toast.makeText(MainActivity.this,"Availability is ON!",Toast.LENGTH_SHORT).show();
                    }
                }
                else if(!isChecked){
                    ContentValues cv = new ContentValues();
                    cv.put(Database.USER_FIELDS[8],"0");
                    long check = WoyallaDriver.myDatabase.update(Database.Table_USER,cv,id);
                    if(check!=-1){
                        Toast.makeText(MainActivity.this,"Availability is OFF!",Toast.LENGTH_SHORT).show();
                    }

                }
            }
        });

    }

    /**
     * this method handle show client button click
     * Client info (name, phone, location) will be displayed to the driver
     * return: void
     */
    public void handleShowClientButton(){
        showClient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String clientName = WoyallaDriver.myDatabase.get_Value_At_Bottom(Database.Table_CLIENT,Database.CLIENT_FIELDS[0]);
                String clientPhone = WoyallaDriver.myDatabase.get_Value_At_Bottom(Database.Table_CLIENT,Database.CLIENT_FIELDS[1]);
                String latitudeString = WoyallaDriver.myDatabase.get_Value_At_Bottom(Database.Table_CLIENT,Database.CLIENT_FIELDS[2]);
                String longitudeString = WoyallaDriver.myDatabase.get_Value_At_Bottom(Database.Table_CLIENT,Database.CLIENT_FIELDS[3]);

                if(latitudeString!=null && longitudeString!=null){
                    double latitude = Double.parseDouble(latitudeString);
                    double longitude = Double.parseDouble(longitudeString);
                    moveMap(latitude,longitude,clientName);
                    ShowDialog("Client Name: " + clientName + "\nClient Phone: " + clientPhone +"\n\nYou can view the location on the map now!");
                }

                else{
                    ShowDialog("You don't have client yet!");
                }

            }
        });
    }

    /**
     * Show message
    * */
    public void ShowDialog(String message) {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        // Yes button clicked
                        break;
                }
            }
        };
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(MainActivity.this);
        builder.setTitle(R.string.app_name)
                .setMessage(message)
                .setPositiveButton("Ok", dialogClickListener).show();
    }

    /**
     * Get latitude and longitude from database and move the map to that specific place
     * the background service will update the current location int he
     */
    private void moveMap(double latitude, double longitude,String title) {
        //Creating a LatLng Object to store Coordinates
        LatLng latLng = new LatLng(latitude,longitude);

        //Adding marker to map
        mMap.addMarker(new MarkerOptions()
                .position(latLng) //setting position
                .draggable(true) //Making the marker draggable
                .title(title)); //Adding a title

        //Moving the camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

        //Animating the camera
        mMap.animateCamera(CameraUpdateFactory.zoomTo(25));
    }


    public double getLatitudeFromDb(){
        double latitude=  Double.parseDouble(WoyallaDriver.myDatabase.get_Value_At_Top(Database.Table_USER,Database.USER_FIELDS[2]));
        return  latitude;
    }

    public double getLongitudeFromDb(){
        double longitude=  Double.parseDouble(WoyallaDriver.myDatabase.get_Value_At_Top(Database.Table_USER,Database.USER_FIELDS[3]));
        return  longitude;
    }

    //reload the client map & data
    public void reload(){
        //remove all clients
        WoyallaDriver.myDatabase.Delete_All(Database.Table_CLIENT);
        /**
         * First get the location data from the database.
         * then view it on the map
         */
        moveMap(getLatitudeFromDb(),getLongitudeFromDb(),"My Location");
        Toast.makeText(MainActivity.this,"Data is reloaded \nPrevious client info has been removed also." +
                " \nThe map is also set to your current location.",Toast.LENGTH_LONG).show();
    }

    //logout method
    public void logOut(){
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        //Yes button clicked
                        WoyallaDriver.myDatabase.Delete_All(Database.Table_USER);
                        Intent intent = new Intent(MainActivity.this,Register.class);
                        startActivity(intent);
                        finish();
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.app_name).setMessage("Are you sure you want to Logout?")
                .setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener)
                .show();
    }


    //share app method

    public void shareApp(){
        String shareBody = "Get Free amharic dictionary  market://details?id=com.brainup.woyalladriver";
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Amharic dictionary");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.app_name)));
    }

    //rate the app method

    public void rateMyApp() {
        Uri uri = Uri.parse("market://details?id=com.brainup.woyalladriver");
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        try {
            startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=com.brainup.woyalladriver")));
        }
    }



    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        int id = item.getItemId();
        if (id == R.id.menu_update_my_location) {
            reload();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Intent intent = null;
        if (id == R.id.nav_home) {
            // Handle the camera action
        } else if (id == R.id.nav_about) {
            intent = new Intent(this,About.class);
            startActivity(intent);
        } else if (id == R.id.nav_logout) {
            logOut();
        } else if (id == R.id.nav_share) {
            shareApp();
        } else if (id == R.id.nav_rate) {
            rateMyApp();
        } else if (id == R.id.nav_comment) {
            intent = new Intent(this,Comment.class);
            startActivity(intent);
        }
        else if (id == R.id.nav_exit) {
            finish();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        double latitude = gps.getLatitude();
        double longitude = gps.getLongitude();

        // Add a marker in current location and move the camera
        LatLng mylocation = new LatLng(latitude, longitude);
        mMap.addMarker(new MarkerOptions().position(mylocation).title("My Location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(mylocation));
    }



}
