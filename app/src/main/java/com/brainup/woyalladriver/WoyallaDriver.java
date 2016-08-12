package com.brainup.woyalladriver;

import android.app.Application;

import com.brainup.woyalladriver.Database.Database;

import me.tatarka.support.job.JobScheduler;

/**
 * Created by Roger on 6/10/16.
 */
public class WoyallaDriver extends Application {

//    public static final String API_URL  = "http://weyala.net/api/";
    public static final String API_URL  = "http://192.168.137.1/api.weyala.net/";
    private static final int JOB_ID = 100;

    private JobScheduler myJobScheduler;
    public static Database myDatabase;

    @Override
    public void onCreate() {
        super.onCreate();

        myDatabase = new Database(this);
/*        Intent intent = new Intent(this,SyncService.class);
        startService(intent);
        myJobScheduler  = JobScheduler.getInstance(this);*/

        JobConstr();
    }

    public void JobConstr(){
/*        JobInfo.Builder builder = new JobInfo.Builder(JOB_ID, new ComponentName(this,SyncService.class));
        builder.setPeriodic(10000);
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED);
        myJobScheduler.schedule(builder.build());*/
    }

}
