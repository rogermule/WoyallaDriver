package com.brainup.woyalladriver.Activities;


import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.brainup.woyalladriver.Adapters.Service_Type_Adapter;
import com.brainup.woyalladriver.Database.Database;
import com.brainup.woyalladriver.Model.User;
import com.brainup.woyalladriver.R;
import com.brainup.woyalladriver.WoyallaDriver;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class Register extends AppCompatActivity {

    /*
    * Object declarations
    * */
    private static final String TAG = "Log_In";
    private  AlertDialog.Builder builder;
	private ProgressDialog pDialog;
	private Context myContext;
	EditText ed_phoneNumber, ed_name,ed_plate_num,ed_car_model,ed_licence_num;
    private Spinner sp_service_type;
	Button bt_login;
	private TextInputLayout inputLayoutPhone, inputLayoutName,inputLayoutCarModel,inputLayoutPlateNumber,inputLayoutLicenceNumber;
    private User Main_User;
    ProgressDialog myDialog;
    ArrayList<String> servicesList;

    OkHttpClient client;    //this object will handle http requests
    MediaType mediaType;
    RequestBody body;
    Request request;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		super.onCreate(savedInstanceState);
		setContentView(R.layout.register);
		init();
        setButtonActions();
    }

	public void init(){
		myContext = this;
		ed_phoneNumber = (EditText) findViewById(R.id.login_phone);
        ed_name = (EditText) findViewById(R.id.login_name);
        ed_car_model = (EditText) findViewById(R.id.login_car_model);
        ed_plate_num = (EditText) findViewById(R.id.login_plate_number);
        ed_licence_num = (EditText) findViewById(R.id.login_licence_num);
        sp_service_type = (Spinner) findViewById(R.id.sp_service_type);
		bt_login = (Button) findViewById(R.id.btnLogin);

        inputLayoutPhone = (TextInputLayout) findViewById(R.id.login_txtinput_phone);
        inputLayoutName = (TextInputLayout) findViewById(R.id.login_inputtxt_name);
        inputLayoutCarModel = (TextInputLayout) findViewById(R.id.login_txtinput_car_model);
        inputLayoutPlateNumber = (TextInputLayout) findViewById(R.id.login_txtinput_plate_number);
        inputLayoutLicenceNumber = (TextInputLayout) findViewById(R.id.login_txtinput_licence_num);

		ed_phoneNumber.addTextChangedListener(new MyTextWatcher(ed_phoneNumber));
        ed_name.addTextChangedListener(new MyTextWatcher(ed_name));
        ed_plate_num.addTextChangedListener(new MyTextWatcher(ed_plate_num));
        ed_car_model.addTextChangedListener(new MyTextWatcher(ed_car_model));
        ed_licence_num.addTextChangedListener(new MyTextWatcher(ed_licence_num));

        /*
        * Initialize the http request objects
        * */
        client = new OkHttpClient();   //initialize the okHttpClient to send http requests
        mediaType = MediaType.parse("application/x-www-form-urlencoded");

        //initialize the User object
        Main_User = new User();
        spinnerInit();   //populate the service type spinner
	}

    public void spinnerInit() {
        //initialize the service ArrayList which holds the services available
        servicesList = new ArrayList<String>();

        //get reference to the array in the resources
        String[] servicesArray = this.getResources().getStringArray(R.array.service_type);

        //for every item in the array, add it to the service ArrayList
        for(int i=0;i<servicesArray.length;i++){
            servicesList.add(servicesArray[i]);
        }

        //finally initialize the service type spinner
        sp_service_type.setAdapter(new Service_Type_Adapter(this, R.layout.spinner_service_type, servicesList));
    }

	public void setButtonActions(){

		/*
				Button register works
		*/

		bt_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitForm();
            }

        });


	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}


    /*
    * this method will validate the forms and perform the login
    * */
    private void submitForm() {

        if (!validateName()) {
            return;
        }
        if (!validatePhone()) {
            return;
        }

        if (!validateLicenceNumber()) {
            return;
        }
        if (!validateCarModel()) {
            return;
        }
        if (!validatePlateNumber()) {
            return;
        }

        //initialize the progress dialog
        myDialog = new ProgressDialog(this);
        myDialog.setTitle(R.string.app_name);
        myDialog.setMessage("Creating the Account ....");
        myDialog.show();

        //start a thread different from the main thread to handle http requests
        Thread account = new Thread(){
            @Override
            public void run() {
                try {
                    sleep( 1000);
                } catch(InterruptedException e){
                } finally {
                    createAccount();
                }
            }
        };

        account.start();

    }

    public void createAccount(){

        //populate the user object with data from text fields
        Main_User.setName(ed_name.getText().toString());
        Main_User.setPhoneNumber(ed_phoneNumber.getText().toString());
        Main_User.setCarModelDescription(ed_car_model.getText().toString());
        Main_User.setLicencePlateNumber(ed_plate_num.getText().toString());
        Main_User.setDriverLicenceIdNo(ed_licence_num.getText().toString());
        Main_User.setServiceModel(sp_service_type.getSelectedItemPosition());

        //initialize the body object for the http post request
        body = RequestBody.create(mediaType,
                "phoneNumber="+Main_User.getPhoneNumber() +
                        "&name="+Main_User.getName()+
                        "&licencePlateNumber="+Main_User.getLicencePlateNumber() +
                        "&carModelDescription="+Main_User.getCarModelDescription() +
                        "&driverLicenceIdNo="+Main_User.getDriverLicenceIdNo());

        //create the request object from http post
        request = new Request.Builder()
                .url(WoyallaDriver.API_URL + "drivers/register")
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
            /**
            * If we get OK response & we get a data object with in the response json
            * This is a new user
            * */
                if(myObject.get("data")!=null) {
                    Log.i("resposeJsonStatus", myObject.get("status").toString());
                    Log.i("resposeJsonMessage", myObject.get("message").toString());

                    JSONObject json_response = myObject.getJSONObject("data");
                    Log.i("responseJsonName", json_response.get("name").toString());
                    Log.i("responseJsonPhoneNumber", json_response.get("phoneNumber").toString());

                    ContentValues cv = new ContentValues();
                    cv.put(Database.USER_FIELDS[0], Main_User.getName());
                    cv.put(Database.USER_FIELDS[1], Main_User.getPhoneNumber());
                    cv.put(Database.USER_FIELDS[2], "0");
                    cv.put(Database.USER_FIELDS[3], "0");
                    cv.put(Database.USER_FIELDS[4], Main_User.getCarModelDescription());
                    cv.put(Database.USER_FIELDS[5], Main_User.getLicencePlateNumber());
                    cv.put(Database.USER_FIELDS[6], Main_User.getServiceModel());
                    cv.put(Database.USER_FIELDS[7], Main_User.getDriverLicenceIdNo());
                    cv.put(Database.USER_FIELDS[8],"0");

                    long checkAdd = WoyallaDriver.myDatabase.insert(Database.Table_USER, cv);
                    if (checkAdd != -1) {
                        //Toast.makeText(this,"Account has been created",Toast.LENGTH_SHORT).show();

                        myDialog.dismiss();
                        Register.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(Register.this,"Your Account has been successfully created",Toast.LENGTH_LONG).show();
                            }
                        });

                        Intent intent = new Intent(this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        myDialog.dismiss();
                        ShowDialog("An error occurred. Please try again.");
                    }
                }

            /**
            * If we get OK response & we don't get a data object with in the response json
            * This is an existing user but the account is updated with new info
            * */
                else if(myObject.get("data")==null){

                    ContentValues cv = new ContentValues();
                    cv.put(Database.USER_FIELDS[0], Main_User.getName());
                    cv.put(Database.USER_FIELDS[1], Main_User.getPhoneNumber());
                    cv.put(Database.USER_FIELDS[2], "0");
                    cv.put(Database.USER_FIELDS[3], "0");
                    cv.put(Database.USER_FIELDS[4], Main_User.getCarModelDescription());
                    cv.put(Database.USER_FIELDS[5], Main_User.getLicencePlateNumber());
                    cv.put(Database.USER_FIELDS[6], Main_User.getServiceModel());
                    cv.put(Database.USER_FIELDS[7], Main_User.getDriverLicenceIdNo());
                    cv.put(Database.USER_FIELDS[8],"0");

                    long checkAdd = WoyallaDriver.myDatabase.insert(Database.Table_USER, cv);
                    if (checkAdd != -1) {
                        //Toast.makeText(this,"Account has been created",Toast.LENGTH_SHORT).show();
                        Log.i("user","An already account is updated");
                        myDialog.dismiss();

                        Register.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(Register.this,"We found an account with this phone number. \nWe have updated your info with your new data",Toast.LENGTH_LONG).show();
                            }
                        });
                        Intent intent = new Intent(this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        myDialog.dismiss();
                        Register.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ShowDialog("An error occurred! Please try again later");
                            }
                        });
                    }

                }

            }
        /**
        * If we get error response
        *
        * */
            else if(myObject.get("status").toString().startsWith("error") ){
                final String errorMessage = myObject.get("description").toString();
                myDialog.dismiss();
                Register.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ShowDialog(errorMessage);
                    }
                });
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            myDialog.dismiss();
        } catch(JSONException e){

        }

    }

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
        builder = new AlertDialog.Builder(myContext);
        builder.setTitle(R.string.app_name)
                .setMessage(message)
                .setPositiveButton("Ok", dialogClickListener).show();
    }

    public void ShowErrorDialog(String message) {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        // Yes button clicked
                        startActivity(getIntent());
                        finish();
                        break;
                }
            }
        };
        builder = new AlertDialog.Builder(myContext);
        builder.setTitle(R.string.app_name)
                .setMessage(message)
                .setPositiveButton("Ok", dialogClickListener).show();
    }

    private boolean validatePhone() {
        if (ed_phoneNumber.getText().toString().trim().isEmpty() || ed_phoneNumber.getText().toString().length()>12 || ed_phoneNumber.getText().toString().length()<6) {
            inputLayoutPhone.setError(getString(R.string.err_msg_phone));
            requestFocus(ed_phoneNumber);
            return false;
        }
        else {
            inputLayoutPhone.setErrorEnabled(false);
        }
        return true;
    }
    private boolean validateName() {
        if (ed_name.getText().toString().trim().isEmpty()) {
            inputLayoutName.setError(getString(R.string.err_msg_name));
            requestFocus(ed_name);
            return false;
        } else {
            inputLayoutName.setErrorEnabled(false);
        }

        return true;
    }
    private boolean validateCarModel() {
        if (ed_car_model.getText().toString().trim().isEmpty()) {
            inputLayoutCarModel.setError(getString(R.string.err_msg_car_model));
            requestFocus(ed_car_model);
            return false;
        } else {
            inputLayoutCarModel.setErrorEnabled(false);
        }

        return true;
    }
    private boolean validatePlateNumber() {
        if (ed_plate_num.getText().toString().trim().isEmpty()) {
            inputLayoutPlateNumber.setError(getString(R.string.err_msg_plate));
            requestFocus(ed_plate_num);
            return false;
        } else {
            inputLayoutPlateNumber.setErrorEnabled(false);
        }

        return true;
    }

    private boolean validateLicenceNumber() {
        if (ed_licence_num.getText().toString().trim().isEmpty()) {
            inputLayoutLicenceNumber.setError(getString(R.string.err_msg_licence));
            requestFocus(ed_licence_num);
            return false;
        } else {
            inputLayoutLicenceNumber.setErrorEnabled(false);
        }

        return true;
    }
    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
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
                case R.id.login_phone:
                    validatePhone();
                    break;
                case R.id.login_name:
                    validateName();
                    break;
                case R.id.login_car_model:
                    validateCarModel();
                    break;
                case R.id.login_plate_number:
                    validatePlateNumber();
                    break;
                case R.id.login_licence_num:
                    validatePlateNumber();
                    break;
            }
        }
    }

}
