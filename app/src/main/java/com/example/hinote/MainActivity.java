package com.example.hinote;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.List;

//it's because our MainActivity extends the AppCompatActivity we have the advantage of restoring(only for editable widgets) through our bundle passed on the onCreate Method
public class MainActivity extends AppCompatActivity {

    public static final String NOTE_POSITION = "com.example.hinote.NOTE_POSITION";
    public static final int POSITION_NOT_SET = -1;
    private NoteInfo mNote;
    private boolean mIsNewNote;
    private Spinner mSpinnerCourses;
    private EditText mTextNoteTitle;
    private EditText mTextNoteText;
    private int mNotePosition;
    private boolean mIsCanceling;
    private NoteActivityViewModel mViewModlel;


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
        List<CourseInfo> courses = DataManager.getInstance().getCourses();

        ArrayAdapter<CourseInfo> adapterCourses = new ArrayAdapter<>(
                this,android.R.layout.simple_spinner_item,courses
        );
        //associate the resource we want to use for drop-down list of courses
        adapterCourses.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //we attach the array adapter with the spinner
        mSpinnerCourses.setAdapter(adapterCourses);

        readDisplayStateValues();
        saveOriginalNoteValues();

        mTextNoteTitle = findViewById(R.id.text_note_title);
        mTextNoteText = findViewById(R.id.text_note_text);

        if(!mIsNewNote) {
            displayNote(mSpinnerCourses, mTextNoteTitle, mTextNoteText);
        }




    }

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
                DataManager.getInstance().removeNote(mNotePosition);
            }else {
                storePreviousNoteValues();
            }
        }else{
            saveNote();
        }
    }

    private void storePreviousNoteValues() {
        CourseInfo course = DataManager.getInstance().getCourse(mViewModlel.mOriginalNoteCourseId);
        mNote.setCourse(course);
        mNote.setTitle(mViewModlel.mOriginalNoteTitle);
        mNote.setText(mViewModlel.mOriginalNoteText);
    }

    private void saveNote() {
        mNote.setCourse((CourseInfo) mSpinnerCourses.getSelectedItem());
        mNote.setTitle(mTextNoteTitle.getText().toString());
        mNote.setText(mTextNoteText.getText().toString());

    }

    private void displayNote(Spinner spinnerCourses, EditText textNoteTitle, EditText textNoteText) {
        List<CourseInfo> courses = DataManager.getInstance().getCourses();
        int courseIndex = courses.indexOf(mNote.getCourse());
        spinnerCourses.setSelection(courseIndex);
        textNoteTitle.setText(mNote.getTitle());
        textNoteText.setText(mNote.getText());

    }

    private void readDisplayStateValues() {
        Intent intent = getIntent();
        //getExtras are not reference types they are value types,
        //Extras that are value-types require a second argument that provides a default value
        mNotePosition = intent.getIntExtra(NOTE_POSITION, POSITION_NOT_SET);
        mIsNewNote = mNotePosition == POSITION_NOT_SET;
        if (mIsNewNote){
            createNewNote();
        }else {
            mNote = DataManager.getInstance().getNotes().get(mNotePosition);
        }
    }
    //to create a brand new note
    private void createNewNote() {
        DataManager dm = DataManager.getInstance();
        //show us what is the position of the newly created note
        mNotePosition = dm.createNewNote();
        mNote = dm.getNotes().get(mNotePosition);

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

        return super.onOptionsItemSelected(item);
    }

    //called before the menu is initially displayed
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        //we get a reference to the menu we r interested in
        MenuItem item = menu.findItem(R.id.action_next);
        int lastNoteIndex = DataManager.getInstance().getNotes().size() -1;

        item.setEnabled(mNotePosition < lastNoteIndex);
        return super.onPrepareOptionsMenu(menu);
    }

    private void moveNext() {
        saveNote();
        ++mNotePosition;

        mNote = DataManager.getInstance().getNotes().get(mNotePosition);

        //we save the original values of the next selected items
        saveOriginalNoteValues();
        displayNote(mSpinnerCourses,mTextNoteTitle,mTextNoteText);

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
}
