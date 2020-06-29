package com.example.hinote;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NoteReminderReceiver extends BroadcastReceiver {
    public static final String EXTRA_NOTE_TITLE = "com.example.hinote.extra.NOTE_TITLE";
    public static final String EXTRA_NOTE_TEXT = "com.example.hinote.extra.NOTE_TEXT";
    public static final String EXTRA_NOTE_ID = "com.example.hinote.extra.NOTE_ID";

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        String noteTitle = intent.getStringExtra(EXTRA_NOTE_TITLE);
        String noteText = intent.getStringExtra(EXTRA_NOTE_TEXT);
        int noteId = intent.getIntExtra(EXTRA_NOTE_ID,0);

//        MainActivity mainActivity = new MainActivity();
//        mainActivity.NotificationDialog(noteTitle,noteText,noteId);

       NotificationHelper notificationHelper = new NotificationHelper();
       notificationHelper.NotificationDialog(context,noteTitle,noteText,noteId);



    }
}
