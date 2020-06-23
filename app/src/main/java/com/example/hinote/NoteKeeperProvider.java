package com.example.hinote;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;

import com.example.hinote.NoteKeeperDatabaseContract.CourseInfoEntry;
import com.example.hinote.NoteKeeperDatabaseContract.NoteInfoEntry;
import com.example.hinote.NoteKeeperProviderContract.CourseIdColumn;

public class NoteKeeperProvider extends ContentProvider {

    private NoteKeeperOpenHelper mDBOpenHelper;
    private static final String MIME_VENDOR_TYPE = "vnd." + NoteKeeperProviderContract.AUTHORITY + ".";

    //matching content URI's
    //the NO_MATCH constant simply indicates that any attempt to match a URI that doesn't contain an authority or path to return back the value NO_MACH
    private static UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    public static final int COURSES = 0;
    public static final int NOTES = 1;
    public static final int NOTES_EXPANDED = 2;
    public static final int NOTES_ROW = 3;
    public static final int COURSES_ROW = 4;
    public static final int NOTES_EXPANDED_ROW = 5;

    //a static initializer allows us to run some code when a type is initially loaded.
    static {

        //the last parameter(COURSES) is the integer value what we returned when the UriMatcher matches this URI
        sUriMatcher.addURI(NoteKeeperProviderContract.AUTHORITY, NoteKeeperProviderContract.Courses.PATH, COURSES);
        sUriMatcher.addURI(NoteKeeperProviderContract.AUTHORITY, NoteKeeperProviderContract.Notes.PATH, NOTES);
        sUriMatcher.addURI(NoteKeeperProviderContract.AUTHORITY, NoteKeeperProviderContract.Notes.PATH_EXPANDED, NOTES_EXPANDED);
        // "+/#" indicates we want to match URL's that end in the Notes.PATH followed by path containing an integer value
        sUriMatcher.addURI(NoteKeeperProviderContract.AUTHORITY, NoteKeeperProviderContract.Notes.PATH + "/#", NOTES_ROW);
        sUriMatcher.addURI(NoteKeeperProviderContract.AUTHORITY, NoteKeeperProviderContract.Courses.PATH + "/#", COURSES_ROW);
        sUriMatcher.addURI(NoteKeeperProviderContract.AUTHORITY, NoteKeeperProviderContract.Notes.PATH_EXPANDED + "/#", NOTES_EXPANDED_ROW);




    }

    public NoteKeeperProvider() {
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.
        long rowId = -1;
        String rowSelection = null;
        String[] rowSelectionArgs = null;
        int nRows = -1;
        SQLiteDatabase db = mDBOpenHelper.getReadableDatabase();
        int uriMatch = sUriMatcher.match(uri);
        switch(uriMatch) {
            case COURSES:
                nRows = db.delete(CourseInfoEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case NOTES:
                nRows = db.delete(NoteInfoEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case NOTES_EXPANDED:
                // throw exception saying that this is a read-only table
            case COURSES_ROW:
                rowId = ContentUris.parseId(uri);
                rowSelection = CourseInfoEntry._ID + " = ?";
                rowSelectionArgs = new String[]{Long.toString(rowId)};
                nRows = db.delete(CourseInfoEntry.TABLE_NAME, rowSelection, rowSelectionArgs);
                break;
            case NOTES_ROW:
                rowId = ContentUris.parseId(uri);
                rowSelection = NoteInfoEntry._ID + " = ?";
                rowSelectionArgs = new String[]{Long.toString(rowId)};
                nRows = db.delete(NoteInfoEntry.TABLE_NAME, rowSelection, rowSelectionArgs);
                break;
            case NOTES_EXPANDED_ROW:
                // throw exception saying that this is a read-only table
                break;
        }
        return nRows;

    }

    @Override
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        String mimeType = null;
        int uriMatch = sUriMatcher.match(uri);
        switch(uriMatch){
            case COURSES:
                // vnd.android.cursor.dir/vnd.com.example.hinote.provider.courses
                mimeType = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" +
                        MIME_VENDOR_TYPE + NoteKeeperProviderContract.Courses.PATH;
                break;
            case NOTES:
                mimeType = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + MIME_VENDOR_TYPE + NoteKeeperProviderContract.Notes.PATH;
                break;
            case NOTES_EXPANDED:
                mimeType = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + MIME_VENDOR_TYPE + NoteKeeperProviderContract.Notes.PATH_EXPANDED;
                break;
            case COURSES_ROW:
                mimeType = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + MIME_VENDOR_TYPE + NoteKeeperProviderContract.Courses.PATH;
                break;
            case NOTES_ROW:
                mimeType = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + MIME_VENDOR_TYPE + NoteKeeperProviderContract.Notes.PATH;
                break;
            case NOTES_EXPANDED_ROW:
                mimeType = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + MIME_VENDOR_TYPE + NoteKeeperProviderContract.Notes.PATH_EXPANDED;
                break;
        }
        return mimeType;

    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // TODO: Implement this to handle requests to insert a new row.
        SQLiteDatabase db = mDBOpenHelper.getWritableDatabase();
        long rowId = -1;
        Uri rowUri = null;
        int uriMatch = sUriMatcher.match(uri);
        switch (uriMatch){
            case NOTES:
                rowId = db.insert(NoteInfoEntry.TABLE_NAME,null,values);
                //content://com.example.hinote.provider/notes/1
                rowUri = ContentUris.withAppendedId(NoteKeeperProviderContract.Notes.CONTENT_URI,rowId);
                break;
            case COURSES:
                rowId = db.insert(CourseInfoEntry.TABLE_NAME,null,values);
                rowUri = ContentUris.withAppendedId(NoteKeeperProviderContract.Courses.CONTENT_URI,rowId);
                break;
            case NOTES_EXPANDED:
                //throw exception saying this is a read-only table
                break;
        }
        return rowUri;
    }

    @Override
    public boolean onCreate() {
        // TODO: Implement this to initialize your content provider on startup.
        mDBOpenHelper = new NoteKeeperOpenHelper(getContext());
        return true;
    }

    //content provider query method returns Cursor the same as SQLite query method
    //string[](string array) is how we pass a list of columns to SQLite query methods
    //projection = receives array of column names that we need to return
    //selection and selectionArg contain our selection criteria,and sortOrder contains our sort order
    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        // TODO: Implement this to handle query requests from clients.
        Cursor cursor = null;
        SQLiteDatabase db = mDBOpenHelper.getReadableDatabase();

        //we can determine that URI parameter is for the courses table or notes table using the "MATCH" method of our UriMatcher field
        //the match method will return back an appropriate integer value for the passed URI
        int uriMatch = sUriMatcher.match(uri);
        switch (uriMatch){
            case COURSES:
                cursor = db.query(CourseInfoEntry.TABLE_NAME,projection,selection,selectionArgs,null,null,sortOrder);
                break;
            case NOTES:
                cursor = db.query(NoteInfoEntry.TABLE_NAME,projection,selection,selectionArgs,null,null,sortOrder);
                break;
            case NOTES_EXPANDED:
                cursor = noteExpandedQuery(db,projection,selection,selectionArgs,sortOrder);
                break;
            case NOTES_ROW:
                //we extract the row id value from our URI(this is past when request come from main activity) using the content URI classes method parseId method
               long rowId =  ContentUris.parseId(uri);
               String rowSelection = NoteInfoEntry._ID + " = ?";
               String[] rowSelectionArgs = new String[]{Long.toString(rowId)};
               cursor = db.query(NoteInfoEntry.TABLE_NAME,projection,rowSelection,rowSelectionArgs,null,null,null);
               break;
            case COURSES_ROW:
                rowId = ContentUris.parseId(uri);
                rowSelection = CourseInfoEntry._ID + " = ?";
                rowSelectionArgs = new String[]{Long.toString(rowId)};
                cursor = db.query(CourseInfoEntry.TABLE_NAME, projection, rowSelection,
                        rowSelectionArgs, null, null, null);
                break;
            case NOTES_EXPANDED_ROW:
                rowId = ContentUris.parseId(uri);
                rowSelection = NoteInfoEntry.getQName(NoteInfoEntry._ID) + " = ?";
                rowSelectionArgs = new String[]{Long.toString(rowId)};
                cursor = noteExpandedQuery(db, projection, rowSelection, rowSelectionArgs, null);
                break;
        }

        return cursor;
    }

    private Cursor noteExpandedQuery(SQLiteDatabase db, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        //making our table columns  qualified table columns
        String[] columns = new String[projection.length];
        for (int x=0; x < projection.length; x++){
            columns[x] = projection[x].equals(BaseColumns._ID) || projection[x].equals(CourseIdColumn.COLUMN_COURSE_ID)?
                    NoteInfoEntry.getQName(projection[x]):projection[x];

        }

        String tablesWithJoin = NoteInfoEntry.TABLE_NAME + " JOIN " +
                CourseInfoEntry.TABLE_NAME + " ON " +
                NoteInfoEntry.getQName(NoteInfoEntry.COLUMN_COURSE_ID) + " = "+
                CourseInfoEntry.getQName(CourseInfoEntry.COLUMN_COURSE_ID);


        return db.query(tablesWithJoin, columns, selection, selectionArgs, null, null, sortOrder);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // TODO: Implement this to handle requests to update one or more rows.
        long rowId = -1;
        String rowSelection = null;
        String[] rowSelectionArgs = null;
        int nRows = -1;
        SQLiteDatabase db = mDBOpenHelper.getReadableDatabase();

        int uriMatch = sUriMatcher.match(uri);
        switch (uriMatch){
            case COURSES:
                nRows = db.update(CourseInfoEntry.TABLE_NAME,values,selection,selectionArgs);
                break;
            case NOTES:
                nRows = db.update(NoteInfoEntry.TABLE_NAME,values,selection,selectionArgs);
                break;
            case NOTES_EXPANDED:
                // throw exception saying that this is a read-only table
            case COURSES_ROW:
                rowId = ContentUris.parseId(uri);
                rowSelection = CourseInfoEntry._ID + " = ?";
                rowSelectionArgs = new String[]{Long.toString(rowId)};
                nRows = db.update(CourseInfoEntry.TABLE_NAME, values, rowSelection, rowSelectionArgs);
                break;
            case NOTES_ROW:
                rowId = ContentUris.parseId(uri);
                rowSelection = NoteInfoEntry._ID + " = ?";
                rowSelectionArgs = new String[]{Long.toString(rowId)};
                nRows = db.update(NoteInfoEntry.TABLE_NAME, values, rowSelection, rowSelectionArgs);
                break;
            case NOTES_EXPANDED_ROW:
                // throw exception saying that this is a read-only table
                break;
        }
        return nRows;
    }
}
