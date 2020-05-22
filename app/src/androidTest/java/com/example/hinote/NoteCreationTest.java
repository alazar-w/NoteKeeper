package com.example.hinote;

import androidx.test.espresso.ViewInteraction;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static org.junit.Assert.*;
import static androidx.test.espresso.Espresso.*;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static org.hamcrest.Matchers.*;


@RunWith(AndroidJUnit4.class)
public class NoteCreationTest {

    private static DataManager sDataManager;

    @BeforeClass
    public static void classSetUp(){
        sDataManager = DataManager.getInstance();
    }

    @Rule
    public ActivityTestRule<NoteListActivity> mNoteListActivityActivityTestRule = new
            ActivityTestRule<>(NoteListActivity.class);
    @Test
    public void createNewNote(){
        final CourseInfo course = sDataManager.getCourse("java_lang");
        final String noteTitle = "Test Note Title";
        final String noteText = "This is the body of test note";
//        ViewInteraction fabNewNote = onView(withId(R.id.fab));
//        fabNewNote.perform(click());

        onView(withId(R.id.fab)).perform(click());

        //click on the spinner
        onView(withId(R.id.spinner_courses)).perform(click());
        //then select from spinner
        onData(allOf(instanceOf(CourseInfo.class),equalTo(course))).perform(click());
        onView(withId(R.id.spinner_courses)).check(matches(withSpinnerText(
                containsString(course.getTitle())
        )));


        onView(withId(R.id.text_note_title)).perform(typeText(noteTitle)).check(matches(withText(containsString(noteTitle))));
        onView(withId(R.id.text_note_text)).perform(typeText(noteText),closeSoftKeyboard()).check(matches(withText(containsString(noteText))));

        pressBack();

        //get the last index of the note list
        int noteIndex = sDataManager.getNotes().size()-1;
        //use that index to find the last added note
        NoteInfo note = sDataManager.getNotes().get(noteIndex);

        //we also verify the logic is working correctly alongside with the ui
        assertEquals(course,note.getCourse());
        assertEquals(noteTitle,note.getTitle());
        assertEquals(noteText,note.getText());
    }

}