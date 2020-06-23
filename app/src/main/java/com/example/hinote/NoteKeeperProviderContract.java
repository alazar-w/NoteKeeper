package com.example.hinote;

import android.net.Uri;
import android.provider.BaseColumns;

//our contract class contains classes for the tables exposed by the provider and those each contain a constant containing the URI to access that table
public final class NoteKeeperProviderContract {
    private NoteKeeperProviderContract(){};
    public static final String AUTHORITY = "com.example.hinote.provider";
    public static final Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);

    protected interface CourseIdColumn{
        public static final String COLUMN_COURSE_ID = "course_id";
    }
    protected interface CourseColumns{
//        public static final String COLUMN_COURSE_ID ="course_id";
        public static final String COLUMN_COURSE_TITLE = "course_title";
    }
    protected interface NotesColumns{
        public static final String COLUMN_NOTE_TITLE = "note_title";
        public static final String COLUMN_NOTE_TEXT = "note_text";
//        public static final String COLUMN_COURSE_ID = "course_id";
    }

    //nested classes for each of the tables we expose with our content provider
    //the BaseColumns interface contains the _ID column
    public static final class  Courses implements CourseColumns, BaseColumns,CourseIdColumn {

        //we want the data from this table to be available from our provider with the URI that includes the path courses
        public static final String PATH = "courses";

        //content://com.example.hinote.provider/courses
        //the CONTENT_URI class can now be used to access our content provider's courses table.
        public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI,PATH);
    }

    //the course related columns are available under specific uri(that is why i implemented CourseColumns),that's with the expanded uri,to access columns when i join "notes" with "courses" table
    public static final class Notes implements NotesColumns,BaseColumns,CourseIdColumn,CourseColumns{
        public static final String PATH = "notes";
        public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI,PATH);

        //THIS IS TO ACCESS OUR JOINED TABLE
        public static final String PATH_EXPANDED = "note_expanded";
        public static final Uri CONTENT_EXPANDED_URI = Uri.withAppendedPath(AUTHORITY_URI,PATH_EXPANDED);
    }

}
