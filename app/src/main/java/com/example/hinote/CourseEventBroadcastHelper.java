package com.example.hinote;

import android.content.Context;
import android.content.Intent;

public class CourseEventBroadcastHelper {
    public static final String ACTION_COURSE_EVENT = "com.example.hinote.action.COURSE_EVENT";
    public static final String EXTRA_COURSE_ID = "com.example.hinote.extra.COURSE_ID";
    public static final String EXTRA_COURSE_MESSAGE = "com.example.hinote.extra.COURSE_MESSAGE";

    public static void sendEventBroadcast(Context context, String courseId, String message) {
        //i'm using app defined action in my implicit intent
        Intent intent = new Intent(ACTION_COURSE_EVENT);
        intent.putExtra(EXTRA_COURSE_ID,courseId);
        intent.putExtra(EXTRA_COURSE_MESSAGE,message);
        //sending broadcast about our apps course event
        context.sendBroadcast(intent);

    }

}
