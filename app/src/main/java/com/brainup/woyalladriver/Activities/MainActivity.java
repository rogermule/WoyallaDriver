package com.brainup.woyalladriver.Activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.brainup.woyalladriver.Checkups;
import com.brainup.woyalladriver.Database.Database;
import com.brainup.woyalladriver.R;
import com.brainup.woyalladriver.Services.GPSTracker;
import com.brainup.woyalladriver.WoyallaDriver;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {

    /**
     * object delarations
     */
    private GoogleMap mMap; //map object
    private SupportMapFragment mapFragment; //fragment that holds the map object
    private Button btn_showClient;   //button to show the client location
    private Switch avaialbilitySwitch;    //toggle button to switch the drivers availability on or off
    private GPSTracker gps;

    private TextView client_available;
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
        btn_showClient = (Button) findViewById(R.id.showclient);

        //initialize client available text view which tells if a client is available
        client_available = (TextView) findViewById(R.id.tv_notification);


        //initialize the gps tracker object
        gps  = new GPSTracker(this);

        checkIfFromNotification();
        checkGPS();
        initAvailabilitySwitch();
        handleAvailabilitySwitch();
        handleShowClientButton();
        handleClientAvailableTextView();
    }


    private void checkIfFromNotification() {
        Bundle bundle = this.getIntent().getExtras();
        if(bundle!=null) {
            if (bundle.getString("newDriver")!=null)
                showClient();
        }
    }


    private void handleClientAvailableTextView() {
        if(WoyallaDriver.myDatabase.count(Database.Table_CLIENT)>0){
            client_available.setText(R.string.new_client);
            client_available.setVisibility(View.VISIBLE);
        }
        else{
            client_available.setVisibility(View.GONE);
        }

    }

    private void handleMapTypeChange() {

            if(mMap != null) {
                if (mMap.getMapType() == GoogleMap.MAP_TYPE_NORMAL) {
                    mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                } else if (mMap.getMapType() == GoogleMap.MAP_TYPE_SATELLITE) {
                    mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                }
            }
            else{
                Log.i("changemap","Map type is null");
            }

    }

    private boolean checkGPS() {
        gps = new GPSTracker(MainActivity.this);
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //if it permission not granted, request a dialog box that asks the client to grant the permission
            //the response will be handled by onRequestPermissionResult method
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},2);
            return false;
        }
        else if(!Checkups.isNetworkAvailable(MainActivity.this)){
            Checkups.showDialog("No connection found!\nPlease open cellular data or connect to wifi for the app to work properly.",MainActivity.this);
            return false;
        }
        else if(!gps.canGetLocation()){
            Checkups.showSettingsAlert(MainActivity.this);
            return false;
        }
        else {
            return true;
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
                    if(checkGPS()){
                        ContentValues cv = new ContentValues();
                        cv.put(Database.USER_FIELDS[8],"1");
                        long check = WoyallaDriver.myDatabase.update(Database.Table_USER,cv,id);
                        if(check!=-1){
                            Toast.makeText(MainActivity.this,MainActivity.this.getResources().getString(R.string.toast_availability_on),Toast.LENGTH_SHORT).show();
                        }
                    }
                    else{
                        avaialbilitySwitch.setChecked(false);
                    }
                }
                else if(!isChecked){
                    ContentValues cv = new ContentValues();
                    cv.put(Database.USER_FIELDS[8],"0");
                    long check = WoyallaDriver.myDatabase.update(Database.Table_USER,cv,id);
                    if(check!=-1){
                        Toast.makeText(MainActivity.this,MainActivity.this.getResources().getString(R.string.toast_availability_off),Toast.LENGTH_SHORT).show();
                    }
                    String userPhone = WoyallaDriver.myDatabase.get_Value_At_Top(Database.Table_USER,Database.USER_FIELDS[1]);
                    sendStatusOff(userPhone,0);
                }
            }
        });

    }

    public void sendStatusOff(final String phone, final int status) {
        final OkHttpClient client = new OkHttpClient();;    //this object will handle http requests
        final MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        final RequestBody body = RequestBody.create(mediaType,
                "phoneNumber=" + phone +
                        "&status=" + status +
                        "&gpsLatitude=" + gps.getLatitude() +
                        "&gpsLongitude=" + gps.getLongitude());

        final Request request  = new Request.Builder()
                .url(WoyallaDriver.API_URL + "drivers/update/" + phone)
                .put(body)
                .addHeader("authorization", "Basic dGhlVXNlcm5hbWU6dGhlUGFzc3dvcmQ=")
                .addHeader("cache-control", "no-cache")
                .addHeader("content-type", "application/x-www-form-urlencoded")
                .build();

        Thread sendStatusOff = new Thread() {
            @Override
            public void run() {
                    try {
                        //make the http post request and get the server response
                        Response response = client.newCall(request).execute();
                        String responseBody = response.body().string().toString();
                        Log.i("avaiabliltiyOFF", responseBody);
                        //get the json response object
                        JSONObject myObject = (JSONObject) new JSONTokener(responseBody).nextValue();

                        /**
                         * If we get OK response
                         *
                         * */

                        if (myObject.get("status").toString().startsWith("ok")) {
                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MainActivity.this,MainActivity.this.getResources().getString(R.string.toast_status_sent_ok), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        /**
                         * If we get error response
                         *
                         * */
                        else if (myObject.get("status").toString().startsWith("error")) {
                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MainActivity.this,MainActivity.this.getResources().getString(R.string.toast_status_sent_error), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch(Exception e){
                        e.printStackTrace();
                    }
            }
        };
        sendStatusOff.start();
    }

    /**
     * this method handle show client button click
     * Client info (name, phone, location) will be displayed to the driver
     * return: void
     */
    public void handleShowClientButton(){
        btn_showClient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showClient();
            }
        });
    }

    //actual show client method
    public void showClient(){
        String clientName = WoyallaDriver.myDatabase.get_Value_At_Bottom(Database.Table_CLIENT,Database.CLIENT_FIELDS[0]);
        String clientPhone = WoyallaDriver.myDatabase.get_Value_At_Bottom(Database.Table_CLIENT,Database.CLIENT_FIELDS[1]);
        String latitudeString = WoyallaDriver.myDatabase.get_Value_At_Bottom(Database.Table_CLIENT,Database.CLIENT_FIELDS[2]);
        String longitudeString = WoyallaDriver.myDatabase.get_Value_At_Bottom(Database.Table_CLIENT,Database.CLIENT_FIELDS[3]);

        if(latitudeString!=null && longitudeString!=null){
            double latitude = Double.parseDouble(latitudeString);
            double longitude = Double.parseDouble(longitudeString);
            moveMapForClient(latitude,longitude,clientName);
            //ShowDialog("Client Name: " + clientName + "\nClient Phone: " + clientPhone +"\n\nYou can view the location on the map now!");
        }

        else{
            ShowDialog(MainActivity.this.getResources().getString(R.string.no_client));
        }
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
    private void moveMapForClient(double latitude, double longitude,String title) {
        //Creating a LatLng Object to store Coordinates
        LatLng latLng = new LatLng(latitude,longitude);

        CameraPosition cameraPosition = new CameraPosition.Builder().target(latLng).zoom(15).build();

        //Adding marker to map
        mMap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_client_map))
                .position(latLng) //setting position
                .draggable(true) //Making the marker draggable
                .title(title)); //Adding a title

        //Moving the camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

        //Animating the camera
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }


    /**
     * Get latitude and longitude from database and move the map to that specific place
     * the background service will update the current location int he
     */
    private void moveMap(String title) {
        //Creating a LatLng Object to store Coordinates
        gps = new GPSTracker(this);
        double latitude = gps.getLatitude();
        double longitude = gps.getLongitude();

        if(!mMap.equals(null)) {
            mMap.clear();
            LatLng latLng = new LatLng(latitude, longitude);

            CameraPosition cameraPosition = new CameraPosition.Builder().target(latLng).zoom(15).build();
            //Adding marker to map
            mMap.addMarker(new MarkerOptions()
                    .position(latLng) //setting position
                    .draggable(true) //Making the marker draggable
                    .title(title)); //Adding a title

            //Moving the camera
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

            //Animating the camera
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
    }

    /**
     * get current location data from database
     * @return double
     */
    public double getLatitudeFromDb(){
        double latitude=  Double.parseDouble(WoyallaDriver.myDatabase.get_Value_At_Top(Database.Table_USER,Database.USER_FIELDS[2]));
        return  latitude;
    }

    /**
     * return current longitude from database
     * @return
     */
    public double getLongitudeFromDb(){
        double longitude=  Double.parseDouble(WoyallaDriver.myDatabase.get_Value_At_Top(Database.Table_USER,Database.USER_FIELDS[3]));
        return  longitude;
    }

    /**
     * reload the map to clear previous clients and update location
     */
    public void reload(){
        //remove all clients
        WoyallaDriver.myDatabase.Delete_All(Database.Table_CLIENT);
        client_available.setVisibility(View.GONE);
        int user_id = WoyallaDriver.myDatabase.get_Top_ID(Database.Table_USER);
        int currentStatus = Integer.parseInt(WoyallaDriver.myDatabase.get_Value_At_Top(Database.Table_USER,Database.USER_FIELDS[8]));
        if(currentStatus>1){
            if(avaialbilitySwitch.isChecked()){
                //Change the client status to active
                ContentValues userStatus = new ContentValues();
                userStatus.put(Database.USER_FIELDS[8],"1");
                WoyallaDriver.myDatabase.update(Database.Table_USER,userStatus,user_id);
            }
            else{
                //Change the client status to offline
                ContentValues userStatus = new ContentValues();
                userStatus.put(Database.USER_FIELDS[8],"0");
                WoyallaDriver.myDatabase.update(Database.Table_USER,userStatus,user_id);
            }
        }

        mMap.clear();   //clear the client marker from the map
        /**
         * First get the location data from the database.
         * then view it on the map
         */
        moveMap(MainActivity.this.getResources().getString(R.string.my_location));
        Toast.makeText(MainActivity.this,MainActivity.this.getResources().getString(R.string.toast_reload),Toast.LENGTH_LONG).show();
    }

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
        String shareBody = "Get Weyala driver app at  market://details?id=com.brainup.woyalladriver";
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Weyala Driver");
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

    private void setLanguage(String lang) {
        SharedPreferences settings = getSharedPreferences(WoyallaDriver.PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        Locale locale;
        Configuration configuration;
        Intent intent;
        switch (lang){
            case "am":
                editor.putString("lang","am");
                editor.commit();
                locale = new Locale("am");
                Locale.setDefault(locale);
                configuration = new Configuration();
                configuration.locale = locale;
                getBaseContext().getResources().updateConfiguration(configuration,getBaseContext().getResources().getDisplayMetrics());
                break;
            case "en":
                editor.putString("lang","en");
                editor.commit();
                locale = new Locale("en");
                Locale.setDefault(locale);
                configuration = new Configuration();
                configuration.locale = locale;
                getBaseContext().getResources().updateConfiguration(configuration,getBaseContext().getResources().getDisplayMetrics());
                break;
        }
        this.finish();
        intent = new Intent(MainActivity.this,MainActivity.class);
        startActivity(intent);
    }

    /**
     * Select language dialog
     * */
    public void selectLanguage() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        setLanguage("en");
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        setLanguage("am");
                        break;
                }
            }
        };
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(MainActivity.this);
        builder.setTitle(R.string.app_name)
                .setMessage(getResources().getString(R.string.dialog_select_language))
                .setPositiveButton("English", dialogClickListener)
                .setNegativeButton("አማርኛ",dialogClickListener).show();
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
        if (id == R.id.menu_change_map_type) {
            handleMapTypeChange();
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
        }else if (id == R.id.nav_lang) {
            selectLanguage();
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
        mMap.getUiSettings().setZoomControlsEnabled(true);
        moveMap(MainActivity.this.getResources().getString(R.string.my_location));

    }



}
