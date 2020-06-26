package com.example.hinote;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.net.Uri;
import android.os.AsyncTask;

public class NoteUploaderJobService extends JobService {
    public static final String EXTRA_DATA_URI= "com.example.hinote.extras.DATA_URI";
    private NoteUploader mNoteUploader;

    public NoteUploaderJobService() {
    }

    //this is the method that the JobScheduler calls to indicate that our job should start doing its work.
    @Override
    public boolean onStartJob(JobParameters params) {
        AsyncTask<JobParameters,Void,Void> task = new AsyncTask<JobParameters, Void, Void>() {
            @Override
            protected Void doInBackground(JobParameters... backgroundParams) {
                //JobParameters contains a variety of configuration and identification data about the job and as part of
                //that,JobParameters includes the PersistableBundle we associated with the JobInfo class' extras when scheduling the job
                JobParameters jobParams = backgroundParams[0];
                String stringDataUri =  jobParams.getExtras().getString(EXTRA_DATA_URI);
                //inorder to use the URI's value,we need to convert that string back into URI
                Uri dataUri = Uri.parse(stringDataUri);
                mNoteUploader.doUpload(dataUri);
                //tells the JobScheduler when the background work is finished but in the case where the jobScheduler tells our job to stop,
                //we don't want to call jobFinished
                if (!mNoteUploader.isCanceled()){
                    jobFinished(jobParams,false);
                }

                return null;
            }
        };
        mNoteUploader = new NoteUploader(this);
        //once we call the execute method the onStartJob method will immediately return,we need to let the jobScheduler know that we've started
        //work that will run in the background,the way we do that is by making onStart job return true(so by returning true we're letting the jobScheduler know the process needs to be
        //allowed to keep running until our background work finishes,Now this of course means that we need to include code that tells the JobScheduler when the background work is finished)
        task.execute(params);
        return true;
    }

    //onStopJob is called  if one or more of the criteria we specified is no longer being met.
    //if we have onStopJob return false,the work will not be rescheduled, if onStopJob returns true,the work will be rescheduled.
    @Override
    public boolean onStopJob(JobParameters params) {
        mNoteUploader.cancel();
        return true;
    }
}
