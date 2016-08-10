package com.brainup.woyalladriver;


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
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.brainup.woyalladriver.Adapters.Service_Type_Adapter;
import com.brainup.woyalladriver.Database.Database;
import com.brainup.woyalladriver.Model.User;

import java.util.ArrayList;


public class Login extends AppCompatActivity {

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
    ArrayList<String> countryList;

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

        Main_User = new User();
        countryList = new ArrayList<String>();
        countryList.add("Taxi Service");
        countryList.add("Daily Contract");
        countryList.add("Rental");

        spinnerInit();   //populate the service type spinner
	}

    public void spinnerInit() {
        sp_service_type.setAdapter(new Service_Type_Adapter(this, R.layout.spinner_service_type, countryList));
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

        if (!validatePhone()) {
            return;
        }

        if (!validateName()) {
            return;
        }
        if (!validateCarModel()) {
            return;
        }
        if (!validatePlateNumber()) {
            return;
        }
        if (!validateLicenceNumber()) {
            return;
        }

        myDialog = new ProgressDialog(this);
        myDialog.setTitle(R.string.app_name);
        myDialog.setMessage("Creating the Account ....");
        myDialog.show();


        Thread account = new Thread(){
            @Override
            public void run() {
                try {
                    sleep( 2000);
                } catch(InterruptedException e){
                } finally {
                    createAccount();
                }
            }
        };

        account.start();

    }


    public void createAccount(){
        Main_User.setName(ed_name.getText().toString());
        Main_User.setPhone(ed_phoneNumber.getText().toString());
        Main_User.setCar_model(ed_car_model.getText().toString());
        Main_User.setPlate_number(ed_plate_num.getText().toString());
        Main_User.setLicence_number(ed_licence_num.getText().toString());
        Main_User.setService_type(sp_service_type.getSelectedItem().toString());

        ContentValues cv = new ContentValues();
        cv.put(Database.USER_FIELDS[0], Main_User.getName());
        cv.put(Database.USER_FIELDS[1], Main_User.getPhone());
        cv.put(Database.USER_FIELDS[2], "");
        cv.put(Database.USER_FIELDS[3], "");
        cv.put(Database.USER_FIELDS[4], Main_User.getCar_model());
        cv.put(Database.USER_FIELDS[5], Main_User.getPlate_number());
        cv.put(Database.USER_FIELDS[6], Main_User.getService_type());
        cv.put(Database.USER_FIELDS[7], Main_User.getLicence_number());


        long checkAdd = WoyallaDriver.myDatabase.insert(Database.Table_USER,cv);
        if(checkAdd!=-1){
            //Toast.makeText(this,"Account has been created",Toast.LENGTH_SHORT).show();
            myDialog.dismiss();
            Intent intent = new Intent(this,MainActivity.class);
            startActivity(intent);
            finish();
        }
        else{
           // Toast.makeText(this,"An error occured! Please try again",Toast.LENGTH_LONG).show();
            myDialog.dismiss();
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
