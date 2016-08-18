package com.brainup.woyalladriver.Services;

import android.content.ContentValues;
import android.util.Log;

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

    //GPS tracker object
    GPSTracker gps;

    String phone;    //current user phone number
    boolean hasUser = false;    //check if there is a user
    boolean userActive = false;    //check if the user is active
    int status;     //what is the status of the user

    public GPSTrackerService(){
        client = new OkHttpClient();   //initialize the okHttpClient to send http requests

        mediaType = MediaType.parse("application/x-www-form-urlencoded");
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
            phone = WoyallaDriver.myDatabase.get_Value_At_Top(Database.Table_USER, Database.USER_FIELDS[1]);
            hasUser = true;

            status = Integer.parseInt(WoyallaDriver.myDatabase.get_Value_At_Top(Database.Table_USER, Database.USER_FIELDS[8]));
            if (status >= 1) {
                userActive = true;
            }

            gps = new GPSTracker(this);
            if (gps.canGetLocation() && hasUser && userActive && status >= 1) {
                final int id = WoyallaDriver.myDatabase.get_Top_ID(Database.Table_USER);
                ContentValues cv = new ContentValues();
                cv.put(Database.USER_FIELDS[2], gps.getLatitude());
                cv.put(Database.USER_FIELDS[3], gps.getLongitude());

                WoyallaDriver.myDatabase.update(Database.Table_USER, cv, id);

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
                                Response response = client.newCall(request).execute();
                                String responseBody = response.body().string().toString();

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
                                    /**
                                     * If the driver status is on service
                                     */
                                    boolean isDataExist = false;
                                    try {
                                        isDataExist = myObject.get("data").equals(null) ? false : true;
                                    } catch (Exception e) {
                                        isDataExist = false;
                                    }

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
                                            WoyallaDriver.myDatabase.update(Database.Table_USER, userStatus, id);

                                            //Send notification to the user that a client exists
                                            Notifications notifications = new Notifications(getApplicationContext(), (int) check);
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
                                e.printStackTrace();
                            }


                        } catch (Exception e) {

                        }
                    }
                };

                account.start();
            }

        }
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }

}