package com.example.hinote;

import android.provider.BaseColumns;

public final class NoteKeeperDatabaseContract {
    private NoteKeeperDatabaseContract() {} //make non-creatable

    //BaseColumns interface is an interface we used to name our primary key so that our assigned primary key
    //get associated with the SQLite default primary key
    public static final class CourseInfoEntry implements BaseColumns {
        public static final String TABLE_NAME = "course_info";
        public static final String COLUMN_COURSE_ID = "course_id";
        public static final String COLUMN_COURSE_TITLE = "course_title";

        public static final String getQName(String columnName){
            return TABLE_NAME + "." + columnName;

        }


        //CREATE TABLE course_info (course_id,course_title)
        public static final String SQL_CREATE_TABLE =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        _ID + " INTEGER PRIMARY KEY, " +
                        COLUMN_COURSE_ID + " TEXT UNIQUE NOT NULL, " +
                        COLUMN_COURSE_TITLE + " TEXT NOT NULL)";
    }

    public static final class NoteInfoEntry implements BaseColumns {
        public static final String TABLE_NAME = "note_info";
        public static final String COLUMN_NOTE_TITLE =  "note_title";
        public static final String COLUMN_NOTE_TEXT = "note_text";
        public static final String COLUMN_COURSE_ID = "course_id";

        public static final String getQName(String columnName){
            return TABLE_NAME + "." + columnName;
        }

        public static final String SQL_CREATE_TABLE =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        _ID + " INTEGER PRIMARY KEY, " +
                        COLUMN_NOTE_TITLE + " TEXT NOT NULL, " +
                        COLUMN_NOTE_TEXT + " TEXT, "+
                        COLUMN_COURSE_ID + " TEXT NOT NULL)";




    }


}
