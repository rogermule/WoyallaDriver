package com.brainup.woyalladriver.Activities;


import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;

import com.brainup.woyalladriver.Database.Database;
import com.brainup.woyalladriver.R;
import com.brainup.woyalladriver.WoyallaDriver;

import java.util.Locale;


public class Splash extends AppCompatActivity {

	@Override
    protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);



		Thread splash = new Thread(){
        	@Override
        	public void run() {
        		try {
        			sleep( 2000);
        		} catch(InterruptedException e){
        		} finally {
        			getNextActivity();
        		}
        	}
        };
        
        splash.start();
	}


	public synchronized void getNextActivity() {


			int count = WoyallaDriver.myDatabase.count(Database.Table_USER);
			if (count == 1) {
				Intent intent = new Intent(this, MainActivity.class);
				startActivity(intent);
				finish();
			} else if (count > 1) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						selectLanguage();
					}
				});
				WoyallaDriver.myDatabase.Delete_All(Database.Table_USER);
				Intent intent = new Intent(this, Register.class);
				startActivity(intent);
				finish();
			} else {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						selectLanguage();
					}
				});
				Intent intent = new Intent(this, Register.class);
				startActivity(intent);
				finish();
			}
	}


	/**
	 * Show message
	 * */
	public void selectLanguage() {
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
					case DialogInterface.BUTTON_POSITIVE:

						break;
					case DialogInterface.BUTTON_NEGATIVE:
						setLanguage();
						break;
				}
			}
		};
		android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(Splash.this);
		builder.setTitle(R.string.app_name)
				.setMessage(getResources().getString(R.string.dialog_select_language))
				.setPositiveButton("English", dialogClickListener)
				.setNegativeButton("አማርኛ",dialogClickListener).show();
	}

	public void setLanguage(){
		SharedPreferences settings = getSharedPreferences(WoyallaDriver.PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("lang","amh");
		editor.commit();

		Locale locale = new Locale("am");
		Locale.setDefault(locale);
		Configuration configuration = new Configuration();
		configuration.locale = locale;
		getBaseContext().getResources().updateConfiguration(configuration,getBaseContext().getResources().getDisplayMetrics());

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
}
