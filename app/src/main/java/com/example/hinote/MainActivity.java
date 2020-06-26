package com.example.hinote;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import android.os.PersistableBundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

import com.example.hinote.NoteKeeperDatabaseContract.CourseInfoEntry;
import com.example.hinote.NoteKeeperDatabaseContract.NoteInfoEntry;
import com.google.android.material.snackbar.Snackbar;

import static androidx.loader.app.LoaderManager.*;

//it's because our MainActivity extends the AppCompatActivity we have the advantage of restoring(only for editable widgets) through our bundle passed on the onCreate Method
//we put type cursor in LoaderManager.LoaderCallbacks<Cursor> b/c the loader manager loads data in to Cursor
public class MainActivity extends AppCompatActivity implements androidx.loader.app.LoaderManager.LoaderCallbacks<Cursor> {

    public static final String NOTE_ID = "com.example.hinote.NOTE_POSITION";
    public static final int ID_NOT_SET = -1;
    public static final int LOADRE_NOTES = 0;
    public static final String TAG = "MainActivity";
    private NoteInfo mNote;
    private boolean mIsNewNote;
    private Spinner mSpinnerCourses;
    private EditText mTextNoteTitle;
    private EditText mTextNoteText;
    private int mNoteId;
    private boolean mIsCanceling;
    private NoteActivityViewModel mViewModlel;
    private NoteKeeperOpenHelper mDBOpenHelper;
    private Cursor mNoteCursor;
    private int mCourseIdPos;
    private int mNoteTitlePos;
    private int mNoteTextPos;
    private SimpleCursorAdapter mAdapterCourses;
    private int LOADER_COURSES;
    private boolean mCoursesQueryFinished;
    private boolean mNoteQueryFinished;
    private Uri mNoteUri;


    @Override
      //BUNDLE is an object that can be used to pass data around within the android framework
     //the "onCreate" is called automatically by the android framework,so when it's called it's given a BUNDLE containing
    // all the data needed to restore it to the state it was in when it's DESTROYED.
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //"setContentView" loads the layout "activity_main" here
        setContentView(R.layout.activity_main);
        //"findViewById" request view reference to interact with it
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        mDBOpenHelper = new NoteKeeperOpenHelper(this);
        //we don't create viewModel instance directly,Instead we allow the system to mange those viewModel instances for us.
        //so us part of that we need a view modelProvider

        //In order to create a viewModel provider there are couple of things it needs to know. one thing it need to know is
        //where do you want to store your viewModels?Because remember they'r stored separately from the activity itself. well fortunately the activity
        //knows the answer to that question so we can simply ask the activity we do that by calling the activities "getViewModelStore()" method.
        //THEN we also have to tell it what factory class we want to use to create the viewModels. And we're just going to use the built in class
        //AndroidViewModelFactory that is nested within the viewModelProvider class(this class is a singleton,meaning there's only one instance throughout the entire
        //application,so we need to get that singleton instance,so we call it getInstance method ) AND NOW in order to get the instance,it need some information about the application
        //so we can call the activities getApplication method ONCE WE DO ALL THAT WE CAN GET A REFERENCE TO OUR VIEW MODEL PROVIDER.
        ViewModelProvider viewModelProvider = new ViewModelProvider(getViewModelStore(),ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication()));

        //the get method need th know what viewModel class we want
        //this gives us a reference to our viewModel
        //NOTE - WHEN OUR ACTIVITY IS INITIALLY CREATED WE'LL GET A BRAND NEW VIEW MODEL INSTANCE. BUT IF OUR ACTIVITY IS DESTROYED AND RECREATED DUE TO A CONFIGURATION CHANGE,WE GET BACK THE EXISTING INSTANCE
        //WE HAD BEFORE THE ACTIVITY WAS DESTROYED
        mViewModlel = viewModelProvider.get(NoteActivityViewModel.class);


        if (savedInstanceState != null && mViewModlel.isNewlyCreated == true){
            mViewModlel.restoreState(savedInstanceState);
        }
        mViewModlel.isNewlyCreated = false;


        mSpinnerCourses = findViewById(R.id.spinner_courses);

//        List<CourseInfo> courses = DataManager.getInstance().getCourses();
//        ArrayAdapter<CourseInfo> adapterCourses = new ArrayAdapter<>(
//                this,android.R.layout.simple_spinner_item,courses
//        );
//        //associate the resource we want to use for drop-down list of courses
//        adapterCourses.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        //we attach the array adapter with the spinner
//        mSpinnerCourses.setAdapter(adapterCourses);

        //USING SIMPLE CURSOR ADAPTER INSTEAD OF ARRAY ADAPTER
        //here instead of populating our spinner from our DataManager we populate it directly from our database (decoupling our spinner and list view from
        //our dataManager and getting direct access from the database)
        mAdapterCourses = new SimpleCursorAdapter(
                this,android.R.layout.simple_spinner_item,null,
                //the name of the data we want to pull from our cursor
                new String[] {CourseInfoEntry.COLUMN_COURSE_TITLE},
                //the id's of the views we want to set,in our case we r using the built in layout resources which is simple spinner item,
                //and in the case of built in layout resource we tend to follow standard naming,
                //if text view in it - the id will be android.R.id.text1 and there are two text views the second will be named text2
                //below we're just saying we want to populate the view with the id with the value that came from the cursor
                new int[]{android.R.id.text1},0
        );

        //associate the resource we want to use for drop-down list of courses
        mAdapterCourses.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //we attach the array adapter with the spinner
        mSpinnerCourses.setAdapter(mAdapterCourses);
        LOADER_COURSES = 1;
        getInstance(this).initLoader(LOADER_COURSES,null,this);

        readDisplayStateValues();
//      saveOriginalNoteValues();

        mTextNoteTitle = findViewById(R.id.text_note_title);
        mTextNoteText = findViewById(R.id.text_note_text);

        if(!mIsNewNote) {
//            displayNote();
            //loadNoteData directly loads data from database,so it have a very potential to interfere with the user interface so it's better to use loaderManager to load our data
//            loadNoteData();
            //we pass integer value to identify the loader,the last parameter is reference to class we want to receive the loader call back,
            // we want it to come directly to MainActivity class so we put 'this'(we want the loader manager to 'NOTIFY' the MainActivity class of the loader event)
//            getLoaderManager().initLoader(LOADER_NOTES,null,this);
            getInstance(this).initLoader(LOADRE_NOTES,null,this);
        }
    }

    private void loadCourseData() {
        SQLiteDatabase db = mDBOpenHelper.getReadableDatabase();
        String[] courseColumns = {
                CourseInfoEntry.COLUMN_COURSE_TITLE,
                CourseInfoEntry.COLUMN_COURSE_ID,
                CourseInfoEntry._ID
        };

        Cursor cursor = db.query(CourseInfoEntry.TABLE_NAME,courseColumns,
                null,null,null,null,CourseInfoEntry.COLUMN_COURSE_TITLE);
        //here our spinner will be populated with all the rows from courseInfoTable
        mAdapterCourses.changeCursor(cursor);
    }

//    private void loadNoteData() {
//        SQLiteDatabase db = mDBOpenHelper.getReadableDatabase();
////        String courseId = "android_intents";
////        String titleStart = "dynamic";
//
////        String selection = NoteInfoEntry.COLUMN_COURSE_ID + " = ? AND "
////                + NoteInfoEntry.COLUMN_NOTE_TITLE + " LIKE ?";
////        String[] selectionArgs = {courseId,titleStart + "%"};
//
//        String selection = NoteInfoEntry._ID + " = ?";
//        String[] selectionArgs = {Integer.toString(mNoteId)};
//
//        String[] noteColumns = {
//                NoteInfoEntry.COLUMN_COURSE_ID,
//                NoteInfoEntry.COLUMN_NOTE_TITLE,
//                NoteInfoEntry.COLUMN_NOTE_TEXT
//        };
//        mNoteCursor = db.query(NoteInfoEntry.TABLE_NAME,noteColumns,selection,selectionArgs,null,null,null);
//
//        mCourseIdPos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_COURSE_ID);
//        mNoteTitlePos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TITLE);
//        mNoteTextPos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TEXT);
//        mNoteCursor.moveToNext();
//        displayNote();
//
//    }


    //hear all the original note values are stored so we can use them later if the user cancels

    private void saveOriginalNoteValues() {
        if(mIsNewNote){
            return;
        }
        mViewModlel.mOriginalNoteCourseId = mNote.getCourse().getCourseId();
        mViewModlel.mOriginalNoteTitle = mNote.getTitle();
        mViewModlel.mOriginalNoteText = mNote.getText();

    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if(outState != null){
            mViewModlel.saveState(outState);
        }
    }

    //"onPause()" method is called when our user leaves the note
    //here we want to save our not as soon as we hit the back button
    @Override
    protected void onPause() {
        super.onPause();
        if (mIsCanceling){
            if (mIsNewNote){
                //this was to remove our new note from our InMemory Data
                //DataManager.getInstance().removeNote(mNoteId);
                deleteNoteFromDatabase();

            }else {
                storePreviousNoteValues();
            }
        }else{
            saveNote();
        }

    }

    private void deleteNoteFromDatabase() {
        //when ever local variables used in the body of method  marked anonymous class(the ASYNC TASK class) these variables must be marked as final
//        final String selection = NoteInfoEntry._ID + " = ? ";
//        final String[] selectionArgs = {Integer.toString(mNoteId)};

        //######################################################################################################################################################
        //------------------------------------DELETING USING CONTENT PROVIDER----------------------------------------------------------------------------------
        //######################################################################################################################################################
        AsyncTask task = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {

//                SQLiteDatabase db = mDBOpenHelper.getWritableDatabase();
//                db.delete(NoteInfoEntry.TABLE_NAME,selection,selectionArgs);
                getContentResolver().delete(mNoteUri,null,null);
                return null;
            }
        };

        //the async task class will tack care of the details of running the code in doInBackground method on a non-UI thread.
        task.execute();




    }

    @Override
    protected void onDestroy() {
        mDBOpenHelper.close();
        super.onDestroy();
    }

    private void storePreviousNoteValues() {
        CourseInfo course = DataManager.getInstance().getCourse(mViewModlel.mOriginalNoteCourseId);
        mNote.setCourse(course);
        mNote.setTitle(mViewModlel.mOriginalNoteTitle);
        mNote.setText(mViewModlel.mOriginalNoteText);
    }

    private void saveNote() {
//        mNote.setCourse((CourseInfo) mSpinnerCourses.getSelectedItem());
//        mNote.setTitle(mTextNoteTitle.getText().toString());
//        mNote.setText(mTextNoteText.getText().toString());
        String courseId = selectedCourseId();
        String noteTitle = mTextNoteTitle.getText().toString();
        String noteText = mTextNoteText.getText().toString();
        saveNoteToDatabase(courseId,noteTitle,noteText);
    }

    private String selectedCourseId() {
        int selectedPosition = mSpinnerCourses.getSelectedItemPosition();
        Cursor cursor = mAdapterCourses.getCursor();
        cursor.moveToPosition(selectedPosition);
        int courseIdPos = cursor.getColumnIndex(CourseInfoEntry.COLUMN_COURSE_ID);
        String courseId = cursor.getString(courseIdPos);
        return courseId;
    }

    private void saveNoteToDatabase(String courseId, String noteTitle, final String noteText){
//        final String selection = NoteInfoEntry._ID + " = ? ";
//        final String[] selectionArgs = {Integer.toString(mNoteId)};

        //###############################################################################################################################################
        //------------------------------UPDATING TABLE USING CONTENT PROVIDER----------------------------------------------------------------------------
        //###############################################################################################################################################
        final ContentValues values = new ContentValues();
        values.put(NoteInfoEntry.COLUMN_COURSE_ID,courseId);
        values.put(NoteInfoEntry.COLUMN_NOTE_TITLE,noteTitle);
        values.put(NoteInfoEntry.COLUMN_NOTE_TEXT,noteText);

        AsyncTask task = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
//                SQLiteDatabase db = mDBOpenHelper.getWritableDatabase();
//                db.update(NoteInfoEntry.TABLE_NAME,values,selection,selectionArgs);
//                return null;
                 getContentResolver().update(mNoteUri,values,null,null);
                 return null;

            }
        };
        task.execute();



    }

    private void displayNote() {
        String courseId = mNoteCursor.getString(mCourseIdPos);
        String noteTitle = mNoteCursor.getString(mNoteTitlePos);
        String noteText = mNoteCursor.getString(mNoteTextPos);

//        SINCE I STARTED USING SIMPLE CURSOR ADAPTOR(communicants with cursor only) we don't need the below lines with the DataManager
//        List<CourseInfo> courses = DataManager.getInstance().getCourses();
//        CourseInfo course = DataManager.getInstance().getCourse(courseId);

        int courseIndex = getIndexOfCourseId(courseId);
        mSpinnerCourses.setSelection(courseIndex);
        mTextNoteTitle.setText(noteTitle);
        mTextNoteText.setText(noteText);
    }

    private int getIndexOfCourseId(String courseId) {
        Cursor cursor = mAdapterCourses.getCursor();
        int courseIdPos = cursor.getColumnIndex(CourseInfoEntry.COLUMN_COURSE_ID);
        int courseRowIndex = 0;

        //because this cursor is already used to populate the spinner we don't know where the cursor is positioned
        //so we call cursor.moveToFirst() to assure it's at the first row
        boolean more = cursor.moveToFirst();
        while (more){
            String cursorCourseId = cursor.getString(courseIdPos);
            if (courseId.equals(cursorCourseId)){
                break;
            }
            courseRowIndex ++;
            more = cursor.moveToNext();
        }
        return courseRowIndex;
    }

    private void readDisplayStateValues() {
        Intent intent = getIntent();
        //getExtras are not reference types they are value types,
        //Extras that are value-types require a second argument that provides a default value
        mNoteId = intent.getIntExtra(NOTE_ID, ID_NOT_SET);
        mIsNewNote = mNoteId == ID_NOT_SET;
        if (mIsNewNote){
            createNewNote();
        }else {
//            mNote = DataManager.getInstance().getNotes().get(mNoteId);
        }
    }
    //to create a brand new note
    private void createNewNote() {
//        DataManager dm = DataManager.getInstance();
//        //show us what is the position of the newly created note
//        mNoteId = dm.createNewNote();
//        mNote = dm.getNotes().get(mNoteId);

//        final ContentValues values = new ContentValues();
//        values.put(NoteInfoEntry.COLUMN_COURSE_ID,"");
//        values.put(NoteInfoEntry.COLUMN_NOTE_TITLE,"");
//        values.put(NoteInfoEntry.COLUMN_NOTE_TEXT,"");

//        AsyncTask task = new AsyncTask() {
//            @Override
//            protected Object doInBackground(Object[] objects) {
//                SQLiteDatabase db = mDBOpenHelper.getWritableDatabase();
//                //we assign it to mNoteId b/c we want to update or remove this null values when the user gets back from this activity
//                mNoteId = (int) db.insert(NoteInfoEntry.TABLE_NAME,null,values);
//                return null;
//            }
//        };
//
//       task.execute();

        //####################################################################################################################################################
        //USING CONTENT PROVIDER TO CREATE A MEW NOTE
        //#####################################################################################################################################################
        //####################################################################################################################################################
        //---------------------------------------- INSERTING NEW NOTE USING CONTENT PROVIDER -----------------------------------------------------------------
        //####################################################################################################################################################

        //##########################################################################################################################################################
        //---------------------------------------------CREATING NEW NOTE USING CONTENT PROVIDER--------------------------------------------------------------------
        //##########################################################################################################################################################
        final ContentValues values = new ContentValues();
        values.put(NoteKeeperProviderContract.Notes.COLUMN_COURSE_ID,"");
        values.put(NoteKeeperProviderContract.Notes.COLUMN_NOTE_TITLE,"");
        values.put(NoteKeeperProviderContract.Notes.COLUMN_NOTE_TEXT,"");

        //AsyncTask accepts three type  parameters TYPE1 = BELOW is ContentValues => is the type we want to pass to doInBackground method TYPE3 BELOW is Uri => is the type that's
        //returned from doInBackground method and is the one we pass to onPostExecute method to use it in the main thread
        //TYPE2 BELOW is Integer => is used to pass progress information from doInBackground or other thread to main Thread
        AsyncTask<ContentValues,Integer,Uri> task = new AsyncTask<ContentValues, Integer, Uri>() {
            private ProgressBar mProgressBar;

            //runs before doInBackground method
            @Override
            protected void onPreExecute() {
                mProgressBar = findViewById(R.id.progress_Bar);
                mProgressBar.setVisibility(View.VISIBLE);
                mProgressBar.setProgress(1);
            }

            @Override
            protected Uri doInBackground(ContentValues... contentValues) {
                Log.d(TAG, "doInBackground: thread:" + Thread.currentThread().getId());
               ContentValues insertValues = contentValues[0];
               Uri rowUri = getContentResolver().insert(NoteKeeperProviderContract.Notes.CONTENT_URI,insertValues);
               simulateLongRunningWork();
               //the values we pass to publishProgress are passed in to onProgressUpdate()
               publishProgress(2);
               simulateLongRunningWork();
               publishProgress(3);

               return rowUri;
            }
            //we call this method and pass values to it with  publishProgress(--)
            //runs on the mainThread
            @Override
            protected void onProgressUpdate(Integer... values) {
                int progressValue = values[0];
                mProgressBar.setProgress(progressValue);
            }

            //onPostExecute provides the result of doInBackground
            @Override
            protected void onPostExecute(Uri uri) {
                Log.d(TAG, "onPostExecute: thread:" + Thread.currentThread().getId());
                mNoteUri = uri;
                displaySnackbar(mNoteUri.toString());
                mProgressBar.setVisibility(View.GONE);

            }
        };
        Log.d(TAG, "call to execute:" + Thread.currentThread().getId());
        task.execute(values);



    }

    private void displaySnackbar(String message) {
        View view = findViewById(R.id.spinner_courses);
        Snackbar.make(view, message, Snackbar.LENGTH_LONG).show();
    }

    private void simulateLongRunningWork() {
        try {
            Thread.sleep(2000);
        } catch(Exception ex) {}
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_send_mail) {
            sendEmail();
            return true;
        }else if (id == R.id.action_cancel){
            mIsCanceling = true;
            //when an activity calls finish it signals that it should actually end,so in this process the onPause() will be called
            finish();
        }else if (id == R.id.action_next){
            moveNext();
        }
        else if (id == R.id.action_set_reminder){
            showRemainderNotification();
        }


        return super.onOptionsItemSelected(item);
    }


    private void showRemainderNotification() {
        String noteTitle = mTextNoteTitle.getText().toString();
        String noteText = mTextNoteText.getText().toString();
        int noteId = (int)ContentUris.parseId(mNoteUri);
//        NoteReminderNotification.notify(this,noteTitle,noteText,noteId);

        notificationDialog();
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void notificationDialog() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String NOTIFICATION_CHANNEL_ID = "tutorialspoint_01";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            @SuppressLint("WrongConstant") NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "My Notifications", NotificationManager.IMPORTANCE_MAX);
            // Configure the notification channel.
            notificationChannel.setDescription("Sample Channel description");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel);
        }
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        notificationBuilder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker("Tutorialspoint")
                //.setPriority(Notification.PRIORITY_MAX)
                .setContentTitle("sample notification")
                .setContentText("This is sample notification")
                .setContentInfo("Information");
        notificationManager.notify(1, notificationBuilder.build());
    }

    //called before the menu is initially displayed
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        //we get a reference to the menu we r interested in
        MenuItem item = menu.findItem(R.id.action_next);
        int lastNoteIndex = DataManager.getInstance().getNotes().size() -1;

        item.setEnabled(mNoteId < lastNoteIndex);
        return super.onPrepareOptionsMenu(menu);
    }

    private void moveNext() {
        saveNote();
        ++mNoteId;

        mNote = DataManager.getInstance().getNotes().get(mNoteId);

        //we save the original values of the next selected items
        saveOriginalNoteValues();
        displayNote();

        //schedules call to onPrepareOptionsMenu
        invalidateOptionsMenu();
    }

    //IMPLICIT INTENT DEMONSTRATION
    private void sendEmail() {
        CourseInfo course = (CourseInfo) mSpinnerCourses.getSelectedItem();
        String subject = mTextNoteTitle.getText().toString();
        String text = "check what i have learned in pluralsight course \"" +
                course.getTitle() + "\"\n" + mTextNoteText.getText();
        Intent intent = new Intent(Intent.ACTION_SEND);
        //In order to send a characteristics that says i want to send an email,
        //we associate a type(MIMI TYPE) with this intent
        //"message/rfc2822"- is a standard internet MIMI type for sending email

        intent.setType("message/rfc2822");
        intent.putExtra(Intent.EXTRA_SUBJECT,subject);
        intent.putExtra(Intent.EXTRA_TEXT,text);
        startActivity(intent);
    }



    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        //we load our not data using special kind of loader called a cursor loader
        CursorLoader loader = null;
        //an activity can have multiple loaders and each have an id SO the first thing we should do after declaring a loader variable is
        //check to see if the id passed on the onCreateLoader has the value we looking for which is the constant 'LOADER_NOTES'
        if (id == LOADRE_NOTES)
            loader = createLoaderNotes();
        else if (id == LOADER_COURSES)
            loader = createLoaderCourses();

        return loader;
    }

    //######################################################################################################################################################
    //HERE WE GET OUR COURSES FROM OUR CONTENT PROVIDER NOT DIRECTLY ---------QUERYING--------- SQLite
    //######################################################################################################################################################
    private CursorLoader createLoaderCourses() {
        mCoursesQueryFinished = false;
        //querying using content provider
        //Uri uri = Uri.parse("content://com.example.hinote.provider");
        Uri uri = NoteKeeperProviderContract.Courses.CONTENT_URI;
        String[] courseColumns = {
                //anytime we're interacting with the content or cursor return from a content provider we want to make sure
                //we use constants from the content provider's contract class
                NoteKeeperProviderContract.Courses.COLUMN_COURSE_TITLE,
                NoteKeeperProviderContract.Courses.COLUMN_COURSE_ID,
                NoteKeeperProviderContract.Courses._ID
        };
        return new CursorLoader(this,uri,courseColumns,null,null, NoteKeeperProviderContract.Courses.COLUMN_COURSE_TITLE);


//        return new CursorLoader(this){
//            @Override
//            public Cursor loadInBackground() {
//                SQLiteDatabase db = mDBOpenHelper.getReadableDatabase();
//                String[] courseColumns = {
//                        CourseInfoEntry.COLUMN_COURSE_TITLE,
//                        CourseInfoEntry.COLUMN_COURSE_ID,
//                        CourseInfoEntry._ID
//                };
//
//                return db.query(CourseInfoEntry.TABLE_NAME,courseColumns,
//                        null,null,null,null,CourseInfoEntry.COLUMN_COURSE_TITLE);
//            }
//        };
    }

    //loader in this method is a reference to a loader,a loader we created inside createLoaderNotes,the second parameter is the actual Cursor we have returned
    @Override
    public void onLoadFinished(@NonNull androidx.loader.content.Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == LOADRE_NOTES){
            loadFinishedNotes(data);
        }else if (loader.getId() == LOADER_COURSES){
            //here our spinner will be populated with all the rows from courseInfoTable
            mAdapterCourses.changeCursor(data);
            mCoursesQueryFinished = true;
            displayNoteWhenQueryFinished();
        }

    }

    @Override
    public void onLoaderReset(@NonNull androidx.loader.content.Loader<Cursor> loader) {
        if (loader.getId() == LOADRE_NOTES)
            if (mNoteCursor != null)
                mNoteCursor.close();
        else if (loader.getId() == LOADER_COURSES)
            mAdapterCourses.changeCursor(null);

    }

    private CursorLoader createLoaderNotes() {
        mNoteQueryFinished = false;
//        return new CursorLoader(this){
//            @Override
//            public Cursor loadInBackground() {
//
//                SQLiteDatabase db = mDBOpenHelper.getReadableDatabase();
//                String selection = NoteInfoEntry._ID + " = ?";
//                String[] selectionArgs = {Integer.toString(mNoteId)};
//
//                String[] noteColumns = {
//                        NoteInfoEntry.COLUMN_COURSE_ID,
//                        NoteInfoEntry.COLUMN_NOTE_TITLE,
//                        NoteInfoEntry.COLUMN_NOTE_TEXT
//                };
//                return db.query(NoteInfoEntry.TABLE_NAME,noteColumns,selection,selectionArgs,null,null,null);
//            }
//        };


        //###########################################################################################################################################################
        //------------------------------------------DISPLAYING SPECIFIC NOTE USING CONTENT PROVIDER-----------------------------------------------------------------
        //############################################################################################################################################################
        String[] noteColumns = {
                NoteKeeperProviderContract.Notes.COLUMN_COURSE_ID,
                NoteKeeperProviderContract.Notes.COLUMN_NOTE_TITLE,
                NoteKeeperProviderContract.Notes.COLUMN_NOTE_TEXT
        };
       mNoteUri =  ContentUris.withAppendedId(NoteKeeperProviderContract.Notes.CONTENT_URI,mNoteId);
       return new CursorLoader(this,mNoteUri,noteColumns,null,null,null);

    }


    private void loadFinishedNotes(Cursor data) {
        mNoteCursor = data;

        mCourseIdPos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_COURSE_ID);
        mNoteTitlePos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TITLE);
        mNoteTextPos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TEXT);
        mNoteCursor.moveToFirst();
        mNoteQueryFinished = true;
        displayNoteWhenQueryFinished();

    }

    private void displayNoteWhenQueryFinished() {
        if (mNoteQueryFinished && mCoursesQueryFinished){
            displayNote();
        }

    }

}
