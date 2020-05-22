package com.example.hinote;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

public class DataManagerTest {
    static DataManager sDataManager;

    //will run once before all tests in the class,method must be static
    @BeforeClass
    public static void classSetup(){
        sDataManager = DataManager.getInstance();
    }
    //this method will run before each test in this class
    //we make sure that my list of notes always starts in consistent state
    @Before
    public  void setUp(){

        //each time before a test is run,any notes that are already in the note list are cleared out,
        sDataManager.getNotes().clear();
        //and a consistent set of example notes are then added to that list. so my test in this class start out with
        //exact same set of notes whether the test is run by itself or as a part of a larger set of test
        sDataManager.initializeExampleNotes();

    }

    @Test
    public void createNewNote() {
        DataManager dm = DataManager.getInstance();
        final CourseInfo course = dm.getCourse("android_async");
        final String noteTitle = "Test note title";
        final String noteText = "This is the body text of my test note";

        int noteIndex = dm.createNewNote();
        NoteInfo newNote = dm.getNotes().get(noteIndex);
        newNote.setCourse(course);
        newNote.setTitle(noteTitle);
        newNote.setText(noteText);

        NoteInfo compareNote = dm.getNotes().get(noteIndex);
        //checks to see if two references point to the same object,
        //assertSame if not good from point of functionality b/c we do't know whether our references are equal or not
//        assertSame(newNote,compareNote);

        //check to see if the two reference points have the same value at that position
        //assertEquals(expected(what  we put manually , actual(what we found))
        assertEquals(course,compareNote.getCourse());
        assertEquals(noteTitle,compareNote.getTitle());
        assertEquals(noteText,compareNote.getText());
    }
    @Test
    public void findSimilarNotes() {
        final CourseInfo course = sDataManager.getCourse("android_async");
        final String noteTitle = "Test note title";
        final String noteText1 = "This is the body text of my test note";
        final String noteText2  = "This is the body of my second test note";

        int noteIndex1 = sDataManager.createNewNote();
        NoteInfo newNote1 = sDataManager.getNotes().get(noteIndex1);
        newNote1.setCourse(course);
        newNote1.setTitle(noteTitle);
        newNote1.setText(noteText1);

        int noteIndex2 = sDataManager.createNewNote();
        NoteInfo newNote2 = sDataManager.getNotes().get(noteIndex2);
        newNote2.setCourse(course);
        newNote2.setTitle(noteTitle);
        newNote2.setText(noteText2);

        int foundIndex1 = sDataManager.findNote(newNote1);
        assertEquals(noteIndex1, foundIndex1);

        int foundIndex2 = sDataManager.findNote(newNote2);
        assertEquals(noteIndex2, foundIndex2);
    }
}