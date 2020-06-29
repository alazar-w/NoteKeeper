package com.example.hinote;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import androidx.core.app.NotificationCompat;

import static android.provider.Settings.System.getString;
import static androidx.core.content.ContextCompat.getSystemService;


public class NotificationHelper {
//    private static  String mNOTIFICATION_channel_id;
    private final int NOTIFICATION_UNIQUE_ID = 1;
//    NotificationManager mNotificationManager;

    public void NotificationDialog(Context context,String noteTitle, String noteText, int noteId) {

        // This image is used as the notification's large icon (thumbnail).
        final Bitmap picture = BitmapFactory.decodeResource(context.getResources(), R.drawable.logo);

        Intent noteActivityIntent = new Intent(context, MainActivity.class);
        noteActivityIntent.putExtra(MainActivity.NOTE_ID, noteId);
        //The setFlags() method helps preserve the user's expected navigation experience after they open your app via the notification.
        noteActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        //Normal intents are used to launch other activities internally or externally and for that, we always need to add certain permissions in manifest.
        //Now we are trying to do opposite(pending intent) i.e. launch the activity in our application from other application,
        //PendingIntent.getActivity is an a Pending intent that launches an activity
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, noteActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        //→ requestCode — an integer which is assigned to the pending intent we are working with to refer to it when we want to delete it
        Intent backupServiceIntent = new Intent(context, NoteBackupService.class);
        backupServiceIntent.putExtra(NoteBackupService.EXTRA_COURSE_ID, NoteBackup.ALL_COURSES);

        //PendingIntent.getService is an a Pending intent that launches a Service
        PendingIntent backUpPendingIntent = PendingIntent.getService(context,0,backupServiceIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, MainActivity.mNOTIFICATION_channel_id);
        notificationBuilder.setAutoCancel(true)
                .setDefaults(android.app.Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_event_note_black_24dp)
                .setLargeIcon(picture)
                .setContentTitle("Review note")
                .setContentText(noteText)
                // Set ticker text (preview) information for this notification.
                .setTicker("Review note")
                // Automatically dismiss the notification when it is touched.
                .setAutoCancel(true)
                // Set the intent that will fire when the user taps the notification
                .setContentIntent(pendingIntent)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(noteText)
                        .setBigContentTitle(noteTitle)
                        .setSummaryText("Review note"))
                .setContentInfo("Information")

                .addAction(
                        0,
                        "View all notes",
                        PendingIntent.getActivity(
                                context,
                                0,
                                new Intent(context, NoteActivity.class),
                                PendingIntent.FLAG_UPDATE_CURRENT))

                .addAction(
                        0,
                        "Backup notes",
                        backUpPendingIntent);

        MainActivity.mNotificationManager.notify(NOTIFICATION_UNIQUE_ID, notificationBuilder.build());
    }

//    public void createNotificationChannel(Context context) {
//        mNOTIFICATION_channel_id = "HI-NOTE";
//        CharSequence name = context.getString(R.string.channel_name);
//        String description = context.getString(R.string.channel_description);
//        int importance = NotificationManager.IMPORTANCE_DEFAULT;
//        NotificationChannel channel = new NotificationChannel(mNOTIFICATION_channel_id, name, importance);
//        channel.setDescription(description);
//        // Register the channel with the system; you can't change the importance
//        // or other notification behaviors after this
//        mNotificationManager = getSystemService(context,NotificationManager.class);
//        mNotificationManager.createNotificationChannel(channel);
//    }
}
