package com.hakim.notekeeper;

import android.support.test.espresso.contrib.DrawerActions;
import android.support.test.rule.ActivityTestRule;

import org.junit.Rule;
import org.junit.Test;

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
    }
}