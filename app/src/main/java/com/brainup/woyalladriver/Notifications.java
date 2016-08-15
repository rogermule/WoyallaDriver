package com.brainup.woyalladriver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.brainup.woyalladriver.Activities.MainActivity;

/**
 * Created by Roger on 8/15/2016.
 */
public class Notifications {
    Context context;

    public Notifications(Context context){
        this.context = context;
    }

    NotificationManager mgr = (NotificationManager)context.getSystemService(context.NOTIFICATION_SERVICE);
    Intent notificationIntent = new Intent(context, MainActivity.class);
    PendingIntent pi = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_ONE_SHOT);
    Uri uri = Uri.parse("android.resource://"+context.getPackageName()+"/"+R.raw.ringtone);
    Notification.Builder builder = new Notification.Builder(context)
            .setContentTitle("You have new Client")
            .setContentIntent(pi)
            .setContentText("Client here to view the client location")
            .setAutoCancel(true)
            .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE)
            .setWhen(System.currentTimeMillis())
            .setSound(uri)
            .setSmallIcon(R.drawable.nav_icon);

    builder.setSmallIcon(R.drawable.logoicon_ldpi);

    Notification note = builder.build();

    int id = (int)((long)rowId);
    mgr.notify(id, note);
}
