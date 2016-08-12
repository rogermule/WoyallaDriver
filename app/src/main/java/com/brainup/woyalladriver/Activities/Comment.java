package com.brainup.woyalladriver.Activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import com.brainup.woyalladriver.Database.Database;
import com.brainup.woyalladriver.R;
import com.brainup.woyalladriver.WoyallaDriver;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class Comment extends AppCompatActivity {
    CollapsingToolbarLayout collapsingToolbarLayout;   //declare an object for the collapsing toolbar
    Button sendCommentBtn;
    RatingBar ratingBar;
    EditText et_comment;
    TextInputLayout inputLayoutComment;
    ProgressDialog myDialog;

    OkHttpClient client;    //this object will handle http requests
    MediaType mediaType;
    RequestBody body;
    Request request;

    String userPhoneNumber;
    int rating;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.comment);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar_comment));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_comment);
        collapsingToolbarLayout.setTitle(getResources().getString(R.string.comment));
        collapsingToolbarLayout.setExpandedTitleColor(getResources().getColor(android.R.color.transparent));

        sendCommentBtn = (Button) findViewById(R.id.comment_send);
        ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        et_comment = (EditText) findViewById(R.id.et_comment);
        inputLayoutComment = (TextInputLayout) findViewById(R.id.comment_txtinput);

        et_comment.addTextChangedListener(new MyTextWatcher(et_comment));

        //initialize user phone number
        userPhoneNumber = WoyallaDriver.myDatabase.get_Value_At_Top(Database.Table_USER,Database.USER_FIELDS[1]);

        Log.i("phone",userPhoneNumber);

        /*
        * Initialize the http request objects
        * */
        client = new OkHttpClient();   //initialize the okHttpClient to send http requests
        mediaType = MediaType.parse("application/x-www-form-urlencoded");

        rating = 0;
        handleRating();
        handleSendComment();
    }

    private void handleRating() {
        ratingBar.setNumStars(5);
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                setRating(Math.round(ratingBar.getRating()));
                Toast.makeText(Comment.this,"Current rate is "+ Math.round(ratingBar.getRating()),Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void handleSendComment() {

        if (!validateComment()) {
            return;
        }

        sendCommentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitComment();
            }
        });
    }

    private void submitComment() {
        //initialize the progress dialog
        myDialog = new ProgressDialog(this);
        myDialog.setTitle(R.string.app_name);
        myDialog.setMessage("Sending the comment ...");
        myDialog.show();

        //start a thread different from the main thread to handle http requests
        Thread comment = new Thread(){
            @Override
            public void run() {
                try {
                    sleep( 1000);
                } catch(InterruptedException e){
                } finally {
                    performSend();
                }
            }
        };

        comment.start();
    }

    private void performSend() {

        String comment = et_comment.getText().toString();


        //initialize the body object for the http post request

        body = RequestBody.create(mediaType,
                "driverPhoneNumber="+getUserPhoneNumber() +
                        "&comment=" + comment +
                        "&rate="+getRating());

        //create the request object from http post
        request = new Request.Builder()
                .url(WoyallaDriver.API_URL + "ratings/create")
                .post(body)
                .addHeader("authorization", "Basic dGhlVXNlcm5hbWU6dGhlUGFzc3dvcmQ=")
                .addHeader("cache-control", "no-cache")
                .addHeader("content-type", "application/x-www-form-urlencoded")
                .build();

        try {
            //make the http post request and get the server response
            Response response = client.newCall(request).execute();
            String responseBody = response.body().string().toString();
            Log.i("responseFull", responseBody);

            //get the json response object
            JSONObject myObject = (JSONObject) new JSONTokener(responseBody).nextValue();

        /**
         * If we get OK response
         *
         * */
            if(myObject.get("status").toString().startsWith("ok") ){

                myDialog.dismiss();
                Comment.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(Comment.this,"Your comment has been sent! Thank you!",Toast.LENGTH_LONG).show();
                    }
                });
                this.finish();
            }

        /**
         * If we get error response
         *
         * */
            if(myObject.get("status").toString().startsWith("error") ){
                Comment.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(Comment.this,"Your comment has been sent! Thank you!",Toast.LENGTH_LONG).show();
                    }
                });

            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public String getUserPhoneNumber(){
        return userPhoneNumber;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

             //noinspection SimplifiableIfStatement
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    private boolean validateComment() {
        if (et_comment.getText().toString().trim().isEmpty()) {
            inputLayoutComment.setError(getString(R.string.err_msg_comment));
            requestFocus(et_comment);
            return false;
        } else {
            inputLayoutComment.setErrorEnabled(false);
        }

        return true;
    }

    private class MyTextWatcher implements TextWatcher {

        private View view;

        private MyTextWatcher(View view) {
            this.view = view;
        }

        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void afterTextChanged(Editable editable) {
            switch (view.getId()) {
                case R.id.et_comment:
                    validateComment();
                    break;
            }
        }
    }

}
