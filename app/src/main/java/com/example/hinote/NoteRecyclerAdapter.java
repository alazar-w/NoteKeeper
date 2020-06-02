package com.example.hinote;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hinote.NoteKeeperDatabaseContract.NoteInfoEntry;

import java.util.List;

//our class NoteRecyclerAdapter,extends the class RecyclerView.Adapter,and we're going to use our class NoteRecyclerAdapter.ViewHolder
//to hold the information for our individual views.
public class NoteRecyclerAdapter extends RecyclerView.Adapter<NoteRecyclerAdapter.ViewHolder> {

    private final Context mContext;
//    private final List<NoteInfo> mNotes;
    private Cursor mCursor;
    private final LayoutInflater mLayoutInflater;
    private int mCoursePos;
    private int mNoteTitlePos;
    private int mIdPos;

    //the cursor in the constructor is added instead of 'List<NoteInfo> notes'
    public NoteRecyclerAdapter(Context context, Cursor cursor) {
        mContext = context;
        //inorder to create view from layout resource,we need to use the class that android provides called
        //LayoutInflater. and when we create layoutInflater,we actually create from a context
        //we use the layoutInflater to then inflate those layout resources into actual view hierarchies
        mLayoutInflater = LayoutInflater.from(context);

        //mNotes = notes;
        mCursor = cursor;
        //to get the values from our cursor we need the positions of the columns we r interested in
        populateColumnPositions();

    }

    private void populateColumnPositions() {
        if (mCursor == null){
            return;
        }
        //Get column indexes from mCursor
        mCoursePos = mCursor.getColumnIndex(NoteInfoEntry.COLUMN_COURSE_ID);
        mNoteTitlePos = mCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TITLE);
        //this is the id we sent for the main activity to display the details from our recycler view
        mIdPos = mCursor.getColumnIndex(NoteInfoEntry._ID);




    }
    public void changeCursor(Cursor cursor){
        //check if we have an existing cursor
        if (mCursor != null){
            mCursor.close();
        }
        mCursor = cursor;
        populateColumnPositions();
        //notify our recycler view our data is changed
        notifyDataSetChanged();
    }


    //onCreateViewHolder is responsible to create our ViewHolder instances
    //it also have to create the views themselves
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //before we create the view holder we create the view
        //View itemView = mLayoutInflater.inflate(layout resource,the view group that will be inflated within,false(means we don't want this newly inflated view automatically attached to it's parent. Instead we're going to do it through the adapter and recycler view)
        //the View itemVew point to the root of the view that's created when that layout resource,item_note_list, is inflated
        View itemView = mLayoutInflater.inflate(R.layout.item_note_list,parent,false);
        //the onCreateViewHolder returns a viewHolder(accepts view as a parameter)

        //this is what the recyclerView will use to start creating the pool of views
        return new ViewHolder(itemView);
    }

    //onBindViewHolder is responsible for associating data with our views,
    //int position - is the position of the data we want to associate(bind) on our view
    //the purpose of this method is to display data at specific position
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        //move our cursor to the correct row
        mCursor.moveToPosition(position);
        //then get the actual values
        String course = mCursor.getString(mCoursePos);
        String noteTitle = mCursor.getString(mNoteTitlePos);
        int id = mCursor.getInt(mIdPos);

//       NoteInfo note = mNotes.get(position);
//       holder.mTextCourse.setText(note.getCourse().getTitle());
//       holder.mTextTitle.setText(note.getTitle());
//       holder.mId = note.getId();
        holder.mTextCourse.setText(course);
        holder.mTextTitle.setText(noteTitle);
        holder.mId = id;

    }

    //getItemCount indicate the number of data items we have
    @Override
    public int getItemCount() {
//        return mNotes.size();
        return mCursor == null ? 0 : mCursor.getCount();
    }


    public class ViewHolder extends RecyclerView.ViewHolder{

        public final TextView mTextCourse;
        public final TextView mTextTitle;
        public int mId;

        //this ViewHolder is supposed to keep references to any of the views that we're going have to set at runtime for each item
        //so what we need to do is go ahead and get references to each of the TextViews within our layout.
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mTextCourse = (TextView) itemView.findViewById(R.id.text_course);
            mTextTitle = (TextView) itemView.findViewById(R.id.text_title);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent  intent = new Intent(mContext,MainActivity.class);
                    intent.putExtra(MainActivity.NOTE_ID, mId);
                    mContext.startActivity(intent);
                }
            });
        }
    }
}
