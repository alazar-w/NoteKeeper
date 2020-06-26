package com.example.hinote;

import android.app.IntentService;
import android.content.Intent;


public class NoteBackupService extends IntentService {

    public static final String EXTRA_COURSE_ID = "com.example.hinote.extra.COURSE_ID";

    public NoteBackupService() {
        //passing the name of our service in our constructor---("super("NoteBackupService")--- is mainly used for debugging purpose
        super("NoteBackupService");
    }

    //the intent service base class is doing all the service-related housekeeping work for us and as a part of that work,that the IntentService
    // class takes care of running our onHandlerIntent method on a background thread
    //this mean even though our doBackup method may run for an extended period of time, we can safely call it from within onHandleIntent,
    // knowing that it won't interfere with our main application thread
    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            String backupCourseId = intent.getStringExtra(EXTRA_COURSE_ID);
            NoteBackup.doBackup(this,backupCourseId);

        }
    }


}
