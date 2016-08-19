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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.brainup.woyalladriver.Adapters.Service_Type_Adapter;
import com.brainup.woyalladriver.Checkups;
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
	EditText ed_phoneNumber, ed_name,ed_plate_num,ed_car_model,ed_licence_num,ed_station;
    RadioGroup rg_owner,rg_roofrack;
    RadioButton rb_owner, rb_roofrack;

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
        ed_station = (EditText) findViewById(R.id.login_station);
        sp_service_type = (Spinner) findViewById(R.id.sp_service_type);
		bt_login = (Button) findViewById(R.id.btnLogin);
        rg_owner = (RadioGroup) findViewById(R.id.register_owner);
        rg_roofrack = (RadioGroup) findViewById(R.id.register_roofrack);
        rb_owner = (RadioButton) findViewById(R.id.register_owner_yes);
        rb_owner = (RadioButton) findViewById(R.id.register_owner_no);

        inputLayoutName = (TextInputLayout) findViewById(R.id.login_inputtxt_name);
        inputLayoutPhone = (TextInputLayout) findViewById(R.id.login_txtinput_phone);
        inputLayoutLicenceNumber = (TextInputLayout) findViewById(R.id.login_txtinput_licence_num);
        inputLayoutCarModel = (TextInputLayout) findViewById(R.id.login_txtinput_car_model);
        inputLayoutPlateNumber = (TextInputLayout) findViewById(R.id.login_txtinput_plate_number);

		ed_phoneNumber.addTextChangedListener(new MyTextWatcher(ed_phoneNumber));
        ed_name.addTextChangedListener(new MyTextWatcher(ed_name));
        ed_licence_num.addTextChangedListener(new MyTextWatcher(ed_licence_num));
        ed_car_model.addTextChangedListener(new MyTextWatcher(ed_car_model));
        ed_plate_num.addTextChangedListener(new MyTextWatcher(ed_plate_num));

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

                if(Checkups.isNetworkAvailable(Register.this)){
                    submitForm();
                }
                else{
                    ShowDialog(Register.this.getResources().getString(R.string.error_connection));
                }
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
        myDialog.setMessage(Register.this.getResources().getString(R.string.dialog_creating_account));
        myDialog.setCancelable(false);
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
        Main_User.setStation(ed_station.getText().toString());

        int radio_owner = rg_owner.getCheckedRadioButtonId();
        int radio_roofRack = rg_roofrack.getCheckedRadioButtonId();
        if(radio_owner == R.id.register_owner_yes){
            Main_User.setOwner(1);
        }
        else{
            Main_User.setOwner(0);
        }

        if(radio_roofRack == R.id.register_roofrack_yes){
            Main_User.setRoofRack(1);
        }
        else{
            Main_User.setRoofRack(0);
        }

        //initialize the body object for the http post request
        body = RequestBody.create(mediaType,
                "phoneNumber="+Main_User.getPhoneNumber() +
                        "&name="+Main_User.getName()+
                        "&licencePlateNumber="+Main_User.getLicencePlateNumber() +
                        "&carModelDescription="+Main_User.getCarModelDescription() +
                        "&driverLicenceIdNo="+Main_User.getDriverLicenceIdNo()+
                        "&station="+Main_User.getStation()+
                        "&roofRack="+Main_User.getRoofRack()+
                        "&owner="+Main_User.getOwner());

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
            boolean isDataExist = false;
            try{
               isDataExist =  myObject.get("data").equals(null)? false: true;
            }catch (Exception e){
                isDataExist = false;
            }
                if(isDataExist) {
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
                        myDialog.dismiss();
                        Register.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(Register.this,Register.this.getResources().getString(R.string.toast_register_ok_new),Toast.LENGTH_LONG).show();
                            }
                        });

                        Intent intent = new Intent(this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        myDialog.dismiss();
                        ShowDialog(Register.this.getResources().getString(R.string.error_general));
                    }
                }

            /**
            * If we get OK response & we don't get a data object with in the response json
            * This is an existing user but the account is updated with new info
            * */
                else{
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
                        Log.i("user","An already account is updated");
                        myDialog.dismiss();

                        Register.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(Register.this,Register.this.getResources().getString(R.string.toast_register_ok_new),Toast.LENGTH_LONG).show();
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
                                ShowDialog(Register.this.getResources().getString(R.string.error_general));
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
            Register.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ShowDialog(Register.this.getResources().getString(R.string.error_connection));
                }
            });
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
        if (ed_phoneNumber.getText().toString().trim().isEmpty() || ed_phoneNumber.getText().toString().length()>10 || ed_phoneNumber.getText().toString().length()<10) {
            ed_phoneNumber.setError(getString(R.string.err_msg_phone));
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
            ed_name.setError(getString(R.string.err_msg_name));
            requestFocus(ed_name);
            return false;
        } else {
            inputLayoutName.setErrorEnabled(false);
        }

        return true;
    }
    private boolean validateCarModel() {
        if (ed_car_model.getText().toString().trim().isEmpty()) {
            ed_car_model.setError(getString(R.string.err_msg_car_model));
            requestFocus(ed_car_model);
            return false;
        } else {
            inputLayoutCarModel.setErrorEnabled(false);
        }

        return true;
    }
    private boolean validatePlateNumber() {
        if (ed_plate_num.getText().toString().trim().isEmpty()) {
            ed_plate_num.setError(getString(R.string.err_msg_plate));
            //requestFocus(ed_plate_num);
            return false;
        } else {
            inputLayoutPlateNumber.setErrorEnabled(false);
        }

        return true;
    }

    private boolean validateLicenceNumber() {
        if (ed_licence_num.getText().toString().trim().isEmpty()) {
            ed_licence_num.setError(getString(R.string.err_msg_licence));
            //requestFocus(ed_licence_num);
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
