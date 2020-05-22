package com.example.hinote;

import android.os.Parcel;
import android.os.Parcelable;

public final class NoteInfo implements Parcelable {
    private CourseInfo mCourse;
    private String mTitle;
    private String mText;

    public NoteInfo(CourseInfo course, String title, String text) {
        mCourse = course;
        mTitle = title;
        mText = text;
    }

    private NoteInfo(Parcel in) {
        //class loader provides information on how to create instances of a type
        //in.readParcelable(class loader info for that type)
        mCourse = in.readParcelable(CourseInfo.class.getClassLoader());
        mTitle = in.readString();
        mText = in.readString();
    }



    public CourseInfo getCourse() {
        return mCourse;
    }

    public void setCourse(CourseInfo course) {
        mCourse = course;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getText() {
        return mText;
    }

    public void setText(String text) {
        mText = text;
    }

    private String getCompareKey() {
        return mCourse.getCourseId() + "|" + mTitle + "|" + mText;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NoteInfo that = (NoteInfo) o;

        return getCompareKey().equals(that.getCompareKey());
    }

    @Override
    public int hashCode() {
        return getCompareKey().hashCode();
    }

    @Override
    public String toString() {
        return getCompareKey();
    }

    //this is used when special handling is needs
    @Override
    public int describeContents() {
        return 0;
    }
    //is responsible to write the member information for the type instance into the parcel
    //so we write each member of "noteinfo" into the parcel
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        //course is reference type(it means the course is going have to be parsable)
        //so to write parceable type in to our parcel we use writeparcable method
        //the parceableFlags = 0 is just because we don't need special handling
        dest.writeParcelable(mCourse,0);
        dest.writeString(mTitle);
        dest.writeString(mText);


    }

    public static final Creator<NoteInfo> CREATOR = new Creator<NoteInfo>() {
        @Override
        public NoteInfo createFromParcel(Parcel in) {
            return new NoteInfo(in);
        }

        @Override
        public NoteInfo[] newArray(int size) {
            return new NoteInfo[size];
        }
    };

}
