package com.brainup.woyalladriver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.brainup.woyalladriver.Activities.MainActivity;

/**
 * Created by Roger on 8/15/2016.
 */
public class Notifications {
    Context context;
    int id;

    public Notifications(Context context,int id){
        this.context = context;
        this.id = id;
    }

    public void buildNotification() {
        NotificationManager mgr = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        Intent notificationIntent = new Intent(context, MainActivity.class);
        Bundle b = new Bundle();
        b.putString("newDriver","ok");
        notificationIntent.putExtras(b);

        PendingIntent pi = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_ONE_SHOT);
        //Uri uri = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.ringtone);
        Notification.Builder builder = new Notification.Builder(context)
                .setContentTitle("You have new Client")
                .setContentIntent(pi)
                .setContentText("Client here to view the client location")
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_client_map);

        Notification notification = builder.build();
        mgr.notify(id,notification);

    }
}
