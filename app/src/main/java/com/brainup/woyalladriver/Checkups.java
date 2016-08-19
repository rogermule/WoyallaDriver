package com.brainup.woyalladriver;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;

/**
 * Created by Roger on 8/13/2016.
 */
public class Checkups {

    /**
     * Check network state
     * @param context
     * @return boolean (network state)
     */
    public static boolean isNetworkAvailable(Context context){
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        return activeNetworkInfo!=null && activeNetworkInfo.isConnected();
    }


    /**
     * show any message
     * @param message
     * @param context
     */
    public static void showDialog(String message, final Context context) {
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
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(context);
        builder.setTitle(R.string.app_name)
                .setMessage(message)
                .setPositiveButton(context.getResources().getString(R.string.ok), dialogClickListener).show();
    }


    /**
     * If gps is not enabled, ask the user to enable it.
     */
    public static void showSettingsAlert(final Context context){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);

        // Setting Dialog Title
        alertDialog.setTitle(R.string.app_name);

        // Setting Dialog Message
        alertDialog.setMessage(context.getResources().getString(R.string.no_gps_found));

        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                context.startActivity(intent);
            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton(context.getResources().getString(R.string.nav_exit), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        // Showing Alert Message
        alertDialog.show();
    }
}

