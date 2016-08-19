package com.brainup.woyalladriver;

import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.content.res.Configuration;

import java.util.Locale;

/**
 * Created by Roger on 8/19/2016.
 */
public class Language {

    ContextWrapper context;
    public Language(ContextWrapper context){
        this.context = context;
    }

    public void init(){

        SharedPreferences settings = context.getSharedPreferences(WoyallaDriver.PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        String currentLang = settings.getString("lang","en");

        Locale locale = new Locale(currentLang);
        Locale.setDefault(locale);
        Configuration configuration = new Configuration();
        configuration.locale = locale;
        context.getBaseContext().getResources().updateConfiguration(configuration,context.getBaseContext().getResources().getDisplayMetrics());

    }

}
