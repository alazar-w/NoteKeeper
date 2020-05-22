package com.example.hinote;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

//our class NoteRecyclerAdapter,extends the class RecyclerView.Adapter,and we're going to use our class NoteRecyclerAdapter.ViewHolder
//to hold the information for our individual views.
public class NoteRecyclerAdapter extends RecyclerView.Adapter<NoteRecyclerAdapter.ViewHolder> {

    private final Context mContext;
    private final List<NoteInfo> mNotes;
    private final LayoutInflater mLayoutInflater;

    public NoteRecyclerAdapter(Context context, List<NoteInfo> notes) {
        mContext = context;
        //inorder to create view from layout resource,we need to use the class that android provides called
        //LayoutInflater. and when we create layoutInflater,we actually create from a context
        //we use the layoutInflater to then inflate those layout resources into actual view hierarchies
        mLayoutInflater = LayoutInflater.from(context);
        mNotes = notes;
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
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
       NoteInfo note = mNotes.get(position);
       holder.mTextCourse.setText(note.getCourse().getTitle());
       holder.mTextTitle.setText(note.getTitle());
       holder.mCurrentPosition = position;

    }

    //getItemCount indicate the number of data items we have
    @Override
    public int getItemCount() {
        return mNotes.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder{

        public final TextView mTextCourse;
        public final TextView mTextTitle;
        public int mCurrentPosition;

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
                    intent.putExtra(MainActivity.NOTE_POSITION,mCurrentPosition);
                    mContext.startActivity(intent);
                }
            });
        }
    }
}
