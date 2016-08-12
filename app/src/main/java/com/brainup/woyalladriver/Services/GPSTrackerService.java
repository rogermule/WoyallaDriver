package com.brainup.woyalladriver.Services;

import android.content.ContentValues;
import android.util.Log;

import com.brainup.woyalladriver.Database.Database;
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

    OkHttpClient client;    //this object will handle http requests
    MediaType mediaType;
    RequestBody body;
    Request request;
    GPSTracker gps;

    String phone;
    boolean hasUser = false;
    boolean userActive = false;
    int status;

    public GPSTrackerService(){
        client = new OkHttpClient();   //initialize the okHttpClient to send http requests

        mediaType = MediaType.parse("application/x-www-form-urlencoded");
        if(WoyallaDriver.myDatabase.count(Database.Table_USER)==1){
            phone = WoyallaDriver.myDatabase.get_Value_At_Top(Database.Table_USER,Database.USER_FIELDS[1]);
            hasUser = true;
        }
        status = 0;
    }
    @Override
    public boolean onStartJob(JobParameters params) {
        gps = new GPSTracker(this);
        status = 0;
        if(WoyallaDriver.myDatabase.count(Database.Table_USER)==1){
            phone = WoyallaDriver.myDatabase.get_Value_At_Top(Database.Table_USER,Database.USER_FIELDS[1]);
            hasUser = true;

            status = Integer.parseInt(WoyallaDriver.myDatabase.get_Value_At_Top(Database.Table_USER,Database.USER_FIELDS[8]));
            if(status >=1){
                userActive = true;
            }
        }

        if(gps.canGetLocation() && hasUser && userActive && status>=1) {

            int id = WoyallaDriver.myDatabase.get_Top_ID(Database.Table_USER);
            ContentValues cv = new ContentValues();
            cv.put(Database.USER_FIELDS[2],gps.getLatitude());
            cv.put(Database.USER_FIELDS[3],gps.getLongitude());

            WoyallaDriver.myDatabase.update(Database.Table_USER,cv,id);

            Thread account = new Thread(){
                @Override
                public void run() {
                    try {

                    //initialize the body object for the http post request
                    body = RequestBody.create(mediaType,
                            "PhoneNumber="+phone +
                                    "&status="+ status+
                                    "&gpsLatitude="+gps.getLatitude() +
                                    "&gpsLongitude="+gps.getLongitude());

                    //create the request object from http post
                    request = new Request.Builder()
                            .url(WoyallaDriver.API_URL + "drivers/update/phoneNumber ")
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

                    /**
                     * If we get OK response
                     *
                     * */

                        if (myObject.get("status").toString().startsWith("ok")) {
                                Log.i("myResponse",myObject.get("description").toString());

                            /**
                             * If the driver status is on service
                             */
                            if(myObject.get("data")!=null) {

                            }

                            /**
                             * If the driver status is active
                             */
                            if(myObject.get("data")==null) {

                            }
                        }

                    /**
                     * If we get OK response
                     *
                     * */
                        else if (myObject.get("status").toString().startsWith("error")) {
                            Log.i("myResponse",myObject.get("description").toString());
                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                } catch(Exception e){

                }
                }
            };

            account.start();
        }

        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }

}