package com.hakim.notekeeper;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.List;

public class NoteActivity extends AppCompatActivity {
    private final String TAG = getClass().getSimpleName();
    public static final String NOTE_POSITION = "com.hakim.notekeeper.NOTE_POSITION";
    public static final String ORIGNAL_NOTE_COURSE_ID = "com.hakim.notekeeper.ORIGNAL_NOTE_COURSE_ID";
    public static final String ORIGNAL_NOTE_COURSE_TITLE = "com.hakim.notekeeper.ORIGNAL_NOTE_COURSE_TITLE";
    public static final String ORIGNAL_NOTE_COURSE_TEXT = "com.hakim.notekeeper.ORIGNAL_NOTE_COURSE_TEXT";

    public static final int POSITION_NOT_SET = -1;
    private NoteInfo mNote;
    private boolean isNewNote;
    private Spinner mSpinnerCourses;
    private EditText mTextNoteTitle;
    private EditText mTextNoteText;
    private int mNotePosition;
    private boolean mIsCancelling;
    private String mOrignalNoteCourseId;
    private String mOrignalNoteTitle;
    private String mOrignalNoteText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mSpinnerCourses = (Spinner) findViewById(R.id.spinner_course);

        List<CourseInfo> course = DataManager.getInstance().getCourses();

//        Array Adaptor class
        ArrayAdapter<CourseInfo> adapterCourse = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, course);
        adapterCourse.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mSpinnerCourses.setAdapter(adapterCourse);

        readDisplayStateValues();
        if (savedInstanceState == null){
            saveOrignalNoteValues();
        } else {
            restoreOrignalNoteValues(savedInstanceState);
        }


        mTextNoteTitle = (EditText) findViewById(R.id.text_note_title);
        mTextNoteText = (EditText) findViewById(R.id.text_note_content);

        if (!isNewNote){
            displayNote(mSpinnerCourses, mTextNoteText, mTextNoteTitle);
        }

        // add log cat msg
        Log.d(TAG, "onCreate");

    }

    private void restoreOrignalNoteValues(Bundle savedInstanceState) {
        mOrignalNoteCourseId = savedInstanceState.getString(ORIGNAL_NOTE_COURSE_ID);
        mOrignalNoteTitle = savedInstanceState.getString(ORIGNAL_NOTE_COURSE_TITLE);
        mOrignalNoteText = savedInstanceState.getString(ORIGNAL_NOTE_COURSE_TEXT);
    }

    private void saveOrignalNoteValues() {
        if (isNewNote){
            return;
        }
        mOrignalNoteCourseId = mNote.getCourse().getCourseId();
        mOrignalNoteTitle = mNote.getTitle();
        mOrignalNoteText = mNote.getText();
    }


    @Override
    protected void onPause() {
        super.onPause();
        if(mIsCancelling){
            Log.i(TAG, "Cancelling the note at position " + mNotePosition );
            if(isNewNote){
                DataManager.getInstance().removeNote(mNotePosition);
            } else {
                storePreviousNoteValues();
            }

        } else {
            saveNote();
        }

        Log.d(TAG, "onPause");
    }

    private void storePreviousNoteValues() {
        CourseInfo course = DataManager.getInstance().getCourse(mOrignalNoteCourseId);
        mNote.setCourse(course);
        mNote.setTitle(mOrignalNoteTitle);
        mNote.setText(mOrignalNoteText);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(ORIGNAL_NOTE_COURSE_ID, mOrignalNoteCourseId);
        outState.putString(ORIGNAL_NOTE_COURSE_TITLE, mOrignalNoteTitle);
        outState.putString(ORIGNAL_NOTE_COURSE_TEXT, mOrignalNoteText);
    }

    private void saveNote() {
        mNote.setCourse((CourseInfo) mSpinnerCourses.getSelectedItem());
        mNote.setTitle(mTextNoteTitle.getText().toString());
        mNote.setText(mTextNoteText.getText().toString());
    }

    private void displayNote(Spinner spinnerCourses, EditText textNoteText, EditText textNoteTitle) {
        List<CourseInfo> courses = DataManager.getInstance().getCourses();
        int courseIndex = courses.indexOf(mNote.getCourse());
        spinnerCourses.setSelection(courseIndex);
        textNoteTitle.setText(mNote.getTitle());
        textNoteText.setText(mNote.getText());

    }

    private void readDisplayStateValues() {
        Intent intent = getIntent();
        mNotePosition = intent.getIntExtra(NOTE_POSITION, POSITION_NOT_SET);
        isNewNote = mNotePosition == POSITION_NOT_SET;
        if(isNewNote){
            createNewNote();
        }
        Log.i(TAG, "MnotePosition " + mNotePosition);

        mNote = DataManager.getInstance().getNotes().get(mNotePosition);

    }

    private void createNewNote() {
        DataManager dm = DataManager.getInstance();
        mNotePosition = dm.createNewNote();

//        mNote = dm.getNotes().get(mNotePosition);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_send_email) {
            sendEmail();
            return true;
        } else if (id == R.id.action_cancel){
            mIsCancelling = true;
            finish();
        } else if(id == R.id.action_next){
            moveNext();

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.action_next);
        int lastNoteIndex = DataManager.getInstance().getNotes().size() -1;
        item.setEnabled(mNotePosition<lastNoteIndex);
        return super.onPrepareOptionsMenu(menu);
    }

    private void moveNext() {
        saveNote();
        ++mNotePosition;
        mNote = DataManager.getInstance().getNotes().get(mNotePosition);
        saveOrignalNoteValues();
        displayNote(mSpinnerCourses, mTextNoteTitle, mTextNoteText);
        invalidateOptionsMenu();
    }

    private void sendEmail() {
        CourseInfo course = (CourseInfo) mSpinnerCourses.getSelectedItem();
        String subject = mTextNoteTitle.getText().toString();
        String text = "Check out what I learnt at the pluralsite course \"" +
                course.getTitle() + "\" \n" + mTextNoteText.getText().toString();

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, text);
        startActivity(intent);
//        startActivity(Intent.createChooser(intent, "Choose am email client"));
    }
}
