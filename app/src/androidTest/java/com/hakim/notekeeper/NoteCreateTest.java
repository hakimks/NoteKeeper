package com.hakim.notekeeper;

import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.action.ViewActions;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import static android.support.test.espresso.Espresso.*;
import static android.support.test.espresso.action.ViewActions.*;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.*;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class NoteCreateTest {
    @Rule
    public ActivityTestRule<NoteListActivity> mNoteListActivityRule = new ActivityTestRule<>(NoteListActivity.class);

    @Test
    public void createNewNote(){
//        ViewInteraction fabNewNote = onView(withId(R.id.fab));
//        fabNewNote.perform(click());

        onView(withId(R.id.fab)).perform(click());
        onView(withId(R.id.text_note_title)).perform(typeText("Test Note Title"));
        onView(withId(R.id.text_note_content)).perform(typeText("Test Note Text body content"), ViewActions.closeSoftKeyboard());

    }
}