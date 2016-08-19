package com.brainup.woyalladriver.Activities;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;

import com.brainup.woyalladriver.Database.Database;
import com.brainup.woyalladriver.Language;
import com.brainup.woyalladriver.R;
import com.brainup.woyalladriver.WoyallaDriver;


public class Splash extends AppCompatActivity {
	Language language;
	@Override
    protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);

		language = new Language(this);
		language.init();

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
				WoyallaDriver.myDatabase.Delete_All(Database.Table_USER);
				Intent intent = new Intent(this, Register.class);
				startActivity(intent);
				finish();
			} else {
				Intent intent = new Intent(this, Register.class);
				startActivity(intent);
				finish();
			}
	}


	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
}
