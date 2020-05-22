package com.example.hinote;

import android.os.Bundle;

import androidx.lifecycle.ViewModel;

public class NoteActivityViewModel extends ViewModel {
    public static final String ORIGINAL_NOTE_COURSE_ID = "com.example.hinote.ORIGINAL_NOTE_COURSE_ID";
    public static final String ORIGINAL_NOTE_TITLE = "com.example.hinote.ORIGINAL_NOTE_TITLE";
    public static final String ORIGINAL_NOTE_TEXT = "com.example.hinote.ORIGINAL_NOTE_TEXT";
    public boolean isNewlyCreated = true;

    public String mOriginalNoteCourseId;
    public String mOriginalNoteTitle;
    public String mOriginalNoteText;


    public void saveState(Bundle outState) {
        outState.putString(ORIGINAL_NOTE_COURSE_ID,mOriginalNoteCourseId);
        outState.putString(ORIGINAL_NOTE_TITLE,mOriginalNoteTitle);
        outState.putString(ORIGINAL_NOTE_TEXT,mOriginalNoteText);
    }

    public void restoreState(Bundle instate){
        mOriginalNoteCourseId = instate.getString(ORIGINAL_NOTE_COURSE_ID);
        mOriginalNoteTitle = instate.getString(ORIGINAL_NOTE_TITLE);
        mOriginalNoteText = instate.getString(ORIGINAL_NOTE_TEXT);
    }
}
