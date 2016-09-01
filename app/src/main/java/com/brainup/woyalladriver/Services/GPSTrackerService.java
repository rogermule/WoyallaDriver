package com.brainup.woyalladriver.Services;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.util.Log;

import com.brainup.woyalladriver.Checkups;
import com.brainup.woyalladriver.Database.Database;
import com.brainup.woyalladriver.Notifications;
import com.brainup.woyalladriver.WoyallaDriver;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;

import me.tatarka.support.job.JobParameters;
import me.tatarka.support.job.JobService;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Roger on 8/11/2016.
 */
public class GPSTrackerService extends JobService {

    /**
     * Object declaration
     */
    ////OkHttp objects
    OkHttpClient client;    //this object will handle http requests
    MediaType mediaType;
    RequestBody body;
    Request request;
    Response response;

    //GPS tracker object
    GPSTracker gps;
    SharedPreferences settings;

    String phone;    //current user phone number
    boolean hasUser = false;    //check if there is a user
    boolean userActive = false;    //check if the user is active
    int status;     //what is the status of the user
    int user_id;
    boolean network_available;

    public GPSTrackerService(){
        client = new OkHttpClient();   //initialize the okHttpClient to send http requests
        mediaType = MediaType.parse("application/x-www-form-urlencoded");
        //settings = getSharedPreferences(WoyallaDriver.PREFS_NAME, 0);

        if(WoyallaDriver.myDatabase.count(Database.Table_USER)==1){
            phone = WoyallaDriver.myDatabase.get_Value_At_Top(Database.Table_USER,Database.USER_FIELDS[1]);
            hasUser = true;
        }
        status = 0;  //initialize the user to be offline
    }
    @Override
    public boolean onStartJob(JobParameters params) {

        if(WoyallaDriver.myDatabase.count(Database.Table_USER)==1) {     //check if there is user in the database
            status = 0;   //initialize the user to be offline just in case
            hasUser = true;
            user_id =WoyallaDriver.myDatabase.get_Top_ID(Database.Table_USER);
            status = Integer.parseInt(WoyallaDriver.myDatabase.get_Value_At_Top(Database.Table_USER, Database.USER_FIELDS[8]));
            if (status >= 1) {
                userActive = true;
            }

            gps = new GPSTracker(this);
            network_available = Checkups.isNetworkAvailable(this);
            phone = WoyallaDriver.myDatabase.get_Value_At_Top(Database.Table_USER, Database.USER_FIELDS[1]);

            settings = getSharedPreferences(WoyallaDriver.PREFS_NAME, 0);
            SharedPreferences.Editor editor = settings.edit();
            int offline_status_from_preference = settings.getInt("status",0);

            if(network_available && status ==0 && offline_status_from_preference==1){
                Log.i("SendindStatus", "Status OFFline");
                sendStatusOff();
            }

            if (gps.canGetLocation() && network_available && userActive && status >= 1 ) {
                Log.i("SendindStatus", "Status Current");
                sendCurrentStatus();
            }


        }
        return false;
    }

    public void sendCurrentStatus(){

        ContentValues cv = new ContentValues();
        cv.put(Database.USER_FIELDS[2], gps.getLatitude());
        cv.put(Database.USER_FIELDS[3], gps.getLongitude());

        WoyallaDriver.myDatabase.update(Database.Table_USER, cv, user_id);

        Thread account = new Thread() {
            @Override
            public void run() {
                try {

                    //initialize the body object for the http post request
                    body = RequestBody.create(mediaType,
                            "phoneNumber=" + phone +
                                    "&status=" + status +
                                    "&gpsLatitude=" + gps.getLatitude() +
                                    "&gpsLongitude=" + gps.getLongitude());

                    //create the request object from http post
                    request = new Request.Builder()
                            .url(WoyallaDriver.API_URL + "drivers/update/" + phone)
                            .put(body)
                            .addHeader("authorization", "Basic dGhlVXNlcm5hbWU6dGhlUGFzc3dvcmQ=")
                            .addHeader("cache-control", "no-cache")
                            .addHeader("content-type", "application/x-www-form-urlencoded")
                            .build();

                    try {
                        //make the http post request and get the server response
                        response = client.newCall(request).execute();
                        String responseBody = response.body().string().toString();
                        Log.i("Response", responseBody);
                        //get the json response object
                        JSONObject myObject = (JSONObject) new JSONTokener(responseBody).nextValue();
                        Log.i("errorResponse", myObject.toString());
                        Log.i("phonenumber", phone);

                        /**
                         * If we get OK response
                         *
                         * */

                        if (myObject.get("status").toString().startsWith("ok")) {
                            Log.i("myResponse", myObject.get("description").toString());

                            boolean isDataExist = false;
                            try {
                                isDataExist = myObject.get("data").equals(null) ? false : true;
                            } catch (Exception e) {
                                isDataExist = false;
                            }
                            /**
                             * If the driver status is on service
                             */
                            if (isDataExist) {
                                JSONObject json_response = myObject.getJSONObject("data");
                                Log.i("dataResponse", json_response.toString());

                                ContentValues cv = new ContentValues();
                                cv.put(Database.CLIENT_FIELDS[0], json_response.getString("clientName"));
                                cv.put(Database.CLIENT_FIELDS[1], json_response.getString("clientPhoneNumber"));
                                cv.put(Database.CLIENT_FIELDS[2], json_response.get("clientGpsLatitude").toString());
                                cv.put(Database.CLIENT_FIELDS[3], json_response.get("clientGpsLongitude").toString());
                                cv.put(Database.CLIENT_FIELDS[5], "0");

                                long check = WoyallaDriver.myDatabase.insert(Database.Table_CLIENT, cv);
                                if (check != -1) {
                                    //Update the client status to on Service (2)
                                    ContentValues userStatus = new ContentValues();
                                    userStatus.put(Database.USER_FIELDS[8], "2");
                                    WoyallaDriver.myDatabase.update(Database.Table_USER, userStatus, user_id);

                                    //Send notification to the user that a client exists
                                    Notifications notifications = new Notifications(getApplicationContext(), 1);
                                    notifications.buildNotification();
                                    Log.i("client", "Client Successfully added");
                                } else {
                                    Log.i("client", "Error adding client info");
                                }

                            }

                            /**
                             * If the driver status is active
                             */
                            else {

                            }
                        }

                        /**
                         * If we get error response
                         *
                         * */
                        else if (myObject.get("status").toString().startsWith("error")) {
                            Log.i("myResponse", myObject.get("description").toString());
                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        response.close();
                        e.printStackTrace();
                    } catch (Exception e){
                        e.printStackTrace();
                    }

                } catch (Exception e) {

                }
            }
        };

        account.start();
    }

    public void sendStatusOff() {

        body = RequestBody.create(mediaType,
                "phoneNumber=" + phone +
                        "&status=" + status +
                        "&gpsLatitude=" + gps.getLatitude() +
                        "&gpsLongitude=" + gps.getLongitude());

        request  = new Request.Builder()
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
                    response = client.newCall(request).execute();
                    String responseBody = response.body().string().toString();
                    Log.i("avaiabliltiyOFF", responseBody);

                    //get the json response object
                    JSONObject myObject = (JSONObject) new JSONTokener(responseBody).nextValue();

                    /**
                     * If we get OK response
                     *
                     * */

                    if (myObject.get("status").toString().startsWith("ok")) {
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putInt("status",0);
                        editor.commit();
                    }

                    /**
                     * If we get error response
                     *
                     * */
                    else if (myObject.get("status").toString().startsWith("error")) {

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    response.close();
                    e.printStackTrace();
                } catch(Exception e){
                    e.printStackTrace();
                }
            }
        };
        sendStatusOff.start();

    }
    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }

}