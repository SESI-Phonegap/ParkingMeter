package com.sesi.parkingmeter.jobservice;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.Handler;
import android.util.Log;

import com.sesi.parkingmeter.view.utilities.NotificationUtils;


public class JobServiceOreo extends JobService{



    @Override
    public boolean onStartJob(JobParameters jobParameters) {

        NotificationUtils.remindUserBecauseCharging(getApplicationContext());

        Long lDuration = 100l;
        new Handler().postDelayed(() -> {
            jobFinished(jobParameters,false);
                    Log.d("OnStartJob", String.valueOf(jobParameters.getJobId()));
                }
                , lDuration);
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        Log.d("OnStopJob", String.valueOf(jobParameters.getJobId()));
        return false;
    }
}
