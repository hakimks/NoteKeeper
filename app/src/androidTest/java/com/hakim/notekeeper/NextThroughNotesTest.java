package com.hakim.notekeeper;

import android.support.test.espresso.contrib.DrawerActions;
import android.support.test.espresso.contrib.NavigationViewActions;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.rule.ActivityTestRule;

import org.junit.Rule;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

import static android.support.test.espresso.Espresso.*;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.*;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.*;
import static org.hamcrest.Matchers.*;
import static android.support.test.espresso.assertion.ViewAssertions.*;

public class NextThroughNotesTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityActivityTestRule =
            new ActivityTestRule<>(MainActivity.class);

    @Test
    public void nextThroughNotes(){
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open());
        onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.nav_notes));

        onView(withId(R.id.list_items)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

        List<NoteInfo> notes = DataManager.getInstance().getNotes();

        for (int index = 0; index < notes.size(); index++) {

            NoteInfo note = notes.get(index);

            onView(withId(R.id.spinner_course)).check(matches(withSpinnerText(note.getCourse().getTitle())));
//            onView(withId(R.id.text_note_title)).check(matches(withText(note.getTitle())));
//            onView(withId(R.id.text_note_content)).check(matches(withText(note.getText())));

//            onView(withId(R.id.text_note_title)).check(matches(withText(note.getTitle())));
//            onView(withId(R.id.text_note_content)).check(matches(withText(note.getText())));

//            onView(withId(R.id.action_next)).perform(click());
            if(index < notes.size() -1){
                onView(allOf(withId(R.id.action_next), isEnabled())).perform(click());
            }
        }

        onView(withId(R.id.action_next)).check(matches(not(isEnabled())));
        pressBack();
    }
}