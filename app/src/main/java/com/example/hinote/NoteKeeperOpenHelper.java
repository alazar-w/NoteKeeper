package com.example.hinote;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.example.hinote.NoteKeeperDatabaseContract.CourseInfoEntry;
import com.example.hinote.NoteKeeperDatabaseContract.NoteInfoEntry;

public class NoteKeeperOpenHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "NoteKeeper.db";
    public static final int DATABASE_VERSION = 2;

    //factory = is a mechanism we use to customize behavior of our database interaction,if don't really want to do that we put null and remove it from our constructors list
    //name = is the file name that contains our database
    //version = is the version number of our database
    public NoteKeeperOpenHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null , DATABASE_VERSION);
    }

    //THE SQLiteDatabase class allows us to interact with the database. So that'll actually allow us to execute the SQL statements that create our tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CourseInfoEntry.SQL_CREATE_TABLE);
        db.execSQL(NoteInfoEntry.SQL_CREATE_TABLE);

        //CREATING OUR INDEXES
        db.execSQL(CourseInfoEntry.SQL_CREATE_INDEX1);
        db.execSQL(NoteInfoEntry.SQL_CREATE_INDEX1);

        DatabaseDataWorker worker = new DatabaseDataWorker(db);
        worker.insertCourses();
        worker.insertSampleNotes();
    }

    //the purpose this method is to transition the database from an older version up to whatever version an app might currently need.
    //old version is the database that's currently in and newVersion is the version we're updating in
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //here i'm about to upgrade my database from version 1 to 2 after i added indexes in some of my columns
        if (oldVersion < 2){
            db.execSQL(CourseInfoEntry.SQL_CREATE_INDEX1);
            db.execSQL(NoteInfoEntry.SQL_CREATE_INDEX1);
        }

    }
}
