package com.example.hinote;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;

import com.example.hinote.NoteKeeperDatabaseContract.NoteInfoEntry;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

import static androidx.loader.app.LoaderManager.getInstance;

public class NoteActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,androidx.loader.app.LoaderManager.LoaderCallbacks<Cursor>{

    private static final int LOADRE_NOTES = 0;
    private AppBarConfiguration mAppBarConfiguration;
    private NoteRecyclerAdapter mNoteRecyclerAdapter;
    private LinearLayoutManager mNoteLayoutManager;
    private RecyclerView mRecyclerItems;
    private CourseRecyclerAdapter mCourseRecyclerAdapter;
    private GridLayoutManager mCourseLayoutManager;
    //we assign open helper  reference as a member field to make the live around  for teh life of the activity.
    private NoteKeeperOpenHelper mDBOpenHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //instance of our NoteKeeperOpenHelper and assigning it to our member field
        mDBOpenHelper = new NoteKeeperOpenHelper(this);





        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(NoteActivity.this,MainActivity.class);
                startActivity(intent);
            }
        });
        DrawerLayout drawer = findViewById(R.id.drawer_layout);

        NavigationView navigationView = findViewById(R.id.nav_view);
        //our activity will be notified when a user makes selection from our navigation view
        navigationView.setNavigationItemSelectedListener(this);

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_share, R.id.nav_courses, R.id.nav_notes)
                .setDrawerLayout(drawer)
                .build();
//        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
//        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
//        NavigationUI.setupWithNavController(navigationView, navController);

        initializeDisplayContent();
    }

    //whenever we get back to the NoteListActivity we need to notify the ArrayAdapter that if any data is changed
    @Override
    protected void onResume() {
        super.onResume();
//        mAdapterNotes.notifyDataSetChanged();

        //when we populate our recycler adapter with array adapter we use notifyDataSetChanged() to let the array adapter know data in the list may have been changed and need to update the recycler view
        //mNoteRecyclerAdapter.notifyDataSetChanged();
        getInstance(this).restartLoader(LOADRE_NOTES,null,this);
        //updateNavHeader()  -- not worked,it is related to navigation View
    }

    //not used b/c of the use of Data Loading with Loaders
    private void loadNotes() {
        SQLiteDatabase db = mDBOpenHelper.getReadableDatabase();
        String[] noteColumns = {
                NoteInfoEntry.COLUMN_NOTE_TITLE,
                NoteInfoEntry.COLUMN_COURSE_ID,
                NoteInfoEntry._ID
        };

        //we want the notes sorted by the course that they apply to,then within each course we want to sort the titles
        String noteOrderBy = NoteInfoEntry.COLUMN_COURSE_ID + "," + NoteInfoEntry.COLUMN_NOTE_TITLE;

        Cursor noteCursor = db.query(NoteInfoEntry.TABLE_NAME, noteColumns, null, null, null, null, noteOrderBy);
        //we associate the cursor with the note recycler adapter
        mNoteRecyclerAdapter.changeCursor(noteCursor);


    }

    @Override
    protected void onDestroy() {
        mDBOpenHelper.close();
        super.onDestroy();
    }

    private void initializeDisplayContent() {
        //final it for our listNotes to be accessible in the anonymous class below
//        final ListView listNotes = findViewById(R.id.list_notes);
//
//        List<NoteInfo> notes = DataManager.getInstance().getNotes();
//        mAdapterNotes = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,notes);
//        listNotes.setAdapter(mAdapterNotes);
//
//        listNotes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> listNotes, View view, int position, long id) {
//                Intent intent = new Intent(NoteListActivity.this,MainActivity.class);
//                //here we can send the noteInfo in the intent to mainActivity,but we can also just send the position and main
//                //activity can get the notInfo like the method we wrote below.
//                //NoteInfo note = (NoteInfo) listNotes.getItemAtPosition(position);
//
//                intent.putExtra(MainActivity.NOTE_POSITION,position);
//                startActivity(intent);
//            }
//        });

        //our loadFromDatabase use the openHelper to get to the connection to the database
        DataManager.loadFromDatabase(mDBOpenHelper);

        mRecyclerItems = (RecyclerView) findViewById(R.id.list_item);
        mNoteLayoutManager = new LinearLayoutManager(this);
        mCourseLayoutManager = new GridLayoutManager(this,getResources().getInteger(R.integer.course_girid_span));

//        List<NoteInfo> notes = DataManager.getInstance().getNotes();
        mNoteRecyclerAdapter = new NoteRecyclerAdapter(this,null);

        List<CourseInfo> courses = DataManager.getInstance().getCourses();
        mCourseRecyclerAdapter = new CourseRecyclerAdapter(this,courses);
        displayNotes();

    }

    private void displayNotes() {
        mRecyclerItems.setLayoutManager(mNoteLayoutManager);
        mRecyclerItems.setAdapter(mNoteRecyclerAdapter);

        //connect to the database,get the rows out of our note_info table and display them to the user
        //getReadableDatabase returns back SQLiteDatabase reference
        // calling getReadableDatabase will do the work to check to see if the database exists and create it if it doesn't exist
        //SQLiteDatabase db = mDBOpenHelper.getReadableDatabase();

        selectNavigationMenuItem(R.id.nav_notes);


    }

    private void selectNavigationMenuItem(int id) {
        //whenever we display note we will check the notes item in our navigation drawer whether we select it or note
        NavigationView navigationView = findViewById(R.id.nav_view);
        Menu menu = navigationView.getMenu();
        menu.findItem(id).setChecked(true);
    }

    private void displayCourses(){
        mRecyclerItems.setLayoutManager(mCourseLayoutManager);
        mRecyclerItems.setAdapter(mCourseRecyclerAdapter);

        selectNavigationMenuItem(R.id.nav_courses);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        //GravityCompat.START - shows the drawer in the start age if in case there are multiple drawers
        if (drawer.isDrawerOpen(GravityCompat.START)){
            drawer.closeDrawer(GravityCompat.START);
        }else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.note, menu);
        return true;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        //Handle navigation view item clicks here
        int id = item.getItemId();
        if (id == R.id.nav_notes){
            displayNotes();

        }else if (id == R.id.nav_courses){
            displayCourses();

        }else if (id == R.id.nav_share){
            handleSelection(R.string.nav_share_message);

        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //launching settings
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings){
            startActivity(new Intent(this,SettingsActivity.class));
            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    private void handleSelection(int message_id) {
        View view = findViewById(R.id.list_item);
        Snackbar.make(view,message_id,Snackbar.LENGTH_LONG).show();
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        CursorLoader loader = null;
        if (id == LOADRE_NOTES)
            loader = createLoaderNotes();
        return  loader;
    }

    private CursorLoader createLoaderNotes() {
       return new CursorLoader(this){
           @Override
           public Cursor loadInBackground() {
               SQLiteDatabase db = mDBOpenHelper.getReadableDatabase();
               String[] noteColumns = {
                       NoteInfoEntry.COLUMN_NOTE_TITLE,
                       NoteInfoEntry.COLUMN_COURSE_ID,
                       NoteInfoEntry._ID
               };

               //we want the notes sorted by the course that they apply to,then within each course we want to sort the titles
               String noteOrderBy = NoteInfoEntry.COLUMN_COURSE_ID + "," + NoteInfoEntry.COLUMN_NOTE_TITLE;

               return db.query(NoteInfoEntry.TABLE_NAME, noteColumns, null, null, null, null, noteOrderBy);
           }
       };

    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == LOADRE_NOTES)
            //we associate the cursor with the note recycler adapter
            mNoteRecyclerAdapter.changeCursor(data);

    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        if (loader.getId() == LOADRE_NOTES)
            mNoteRecyclerAdapter.changeCursor(null);

    }

//    @Override
//    public boolean onSupportNavigateUp() {
//        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
//        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
//                || super.onSupportNavigateUp();
//    }
}
