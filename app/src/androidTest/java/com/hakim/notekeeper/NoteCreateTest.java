package com.hakim.notekeeper;

import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.action.ViewActions;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import static androidx.test.espresso.Espresso.*;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static org.hamcrest.Matchers.*;
import static androidx.test.espresso.assertion.ViewAssertions.*;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class NoteCreateTest {
    static DataManager sDataManager;

    @BeforeClass
    public static void classSetUp() throws Exception {
        sDataManager = DataManager.getInstance();
    }

    @Rule
    public ActivityTestRule<NoteListActivity> mNoteListActivityRule = new ActivityTestRule<>(NoteListActivity.class);

    @Test
    public void createNewNote(){
//        ViewInteraction fabNewNote = onView(withId(R.id.fab));
//        fabNewNote.perform(click());
        final CourseInfo course = sDataManager.getCourse("java_lang");
        final String noteTile = "Test Note Title";
        final String noteText = "Test Note Text body content";


        onView(withId(R.id.fab)).perform(click());
        onView(withId(R.id.spinner_course)).perform(click());

        onData(allOf(instanceOf(CourseInfo.class), equalTo(course))).perform(click());

        onView(withId(R.id.spinner_course)).check(matches(withSpinnerText(
                containsString(course.getTitle()))));

        onView(withId(R.id.text_note_title)).perform(typeText(noteTile))
            .check(matches(withText(noteTile)));

        onView(withId(R.id.text_note_content)).perform(typeText(noteText), ViewActions.closeSoftKeyboard());
        onView(withId(R.id.text_note_content)).check(matches(withText(containsString(noteText))));

        pressBack();

        int noteIndex = sDataManager.getNotes().size() -1;
        NoteInfo note = sDataManager.getNotes().get(noteIndex);

        assertEquals(course, note.getCourse());
        assertEquals(noteTile, note.getTitle());
        assertEquals(noteText, note.getText());

    }
}