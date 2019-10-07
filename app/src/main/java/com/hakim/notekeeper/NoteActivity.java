package com.hakim.notekeeper;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.content.ContentUris;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.hakim.notekeeper.NoteKeeperDatabaseContract.CourseInfoEntry;
import com.hakim.notekeeper.NoteKeeperDatabaseContract.NoteInfoEntry;
import com.hakim.notekeeper.NoteKeeperProviderContract.Courses;
import com.hakim.notekeeper.NoteKeeperProviderContract.Notes;

public class NoteActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final int LOADER_NOTES = 0;
    public static final int LOADER_COURSES = 1;
    private final String TAG = getClass().getSimpleName();
    public static final String NOTE_ID = "com.hakim.notekeeper.NOTE_ID";
    public static final String ORIGNAL_NOTE_COURSE_ID = "com.hakim.notekeeper.ORIGNAL_NOTE_COURSE_ID";
    public static final String ORIGNAL_NOTE_COURSE_TITLE = "com.hakim.notekeeper.ORIGNAL_NOTE_COURSE_TITLE";
    public static final String ORIGNAL_NOTE_COURSE_TEXT = "com.hakim.notekeeper.ORIGNAL_NOTE_COURSE_TEXT";

    public static final int ID_NOT_SET = -1;
    private NoteInfo mNote = new NoteInfo(DataManager.getInstance().getCourses().get(0), "", "");
//    private NoteInfo mNote;
    private boolean isNewNote;
    private Spinner mSpinnerCourses;
    private EditText mTextNoteTitle;
    private EditText mTextNoteText;
    private int mNotePosition;
    private boolean mIsCancelling;
    private String mOrignalNoteCourseId;
    private String mOrignalNoteTitle;
    private String mOrignalNoteText;
    private NoteKeeperOpenHelper mOpenHelper;
    private Cursor mNotesCursor;
    private int mCourseIdPos;
    private int mNoteTitlePos;
    private int mNoteTextPos;
    private int mNoteId;
    private SimpleCursorAdapter mAdapterCourse;
    private boolean mCoursesQueryFinished;
    private boolean mMNotesQueryFinished;
    private Uri mNotesUri;

    @Override
    protected void onDestroy() {
        mOpenHelper.close();
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mOpenHelper = new NoteKeeperOpenHelper(this);

        mSpinnerCourses = (Spinner) findViewById(R.id.spinner_course);

//        change from ArrayAdapter to CursorAdapter
//        List<CourseInfo> course = DataManager.getInstance().getCourses();

//        Array Adaptor class
//        ArrayAdapter<CourseInfo> adapterCourse = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, course);
        mAdapterCourse = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, null,
                new String[] {CourseInfoEntry.COLUMN_COURSE_TITLE},
                new int[] {android.R.id.text1}, 0);

        mAdapterCourse.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mSpinnerCourses.setAdapter(mAdapterCourse);


        getLoaderManager().initLoader(LOADER_COURSES,null, this);

        readDisplayStateValues();


        if (savedInstanceState == null){
            saveOrignalNoteValues();
        } else {
            restoreOrignalNoteValues(savedInstanceState);
        }


        mTextNoteTitle = (EditText) findViewById(R.id.text_note_title);
        mTextNoteText = (EditText) findViewById(R.id.text_note_content);

        if (!isNewNote){
            getLoaderManager().initLoader(LOADER_NOTES,null, this);
        }

        // add log cat msg
        Log.d(TAG, "onCreate");

    }

    private void loadCourseData() {
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();

        String [] courseColumns = {
                CourseInfoEntry.COLUMN_COURSE_TITLE,
                CourseInfoEntry.COLUMN_COURSE_ID,
                CourseInfoEntry._ID
        };

        Cursor cursor = db.query(CourseInfoEntry.TABLE_NAME, courseColumns,
                null, null,null,null, CourseInfoEntry.COLUMN_COURSE_TITLE);

        mAdapterCourse.changeCursor(cursor);
    }

    private void loadNoteData() {
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();

        String selection = NoteInfoEntry._ID + "= ?";

        String[] selectionArgs = {Integer.toString(mNoteId)};

        String[] noteColumns = {
          NoteInfoEntry.COLUMN_COURSE_ID,
                NoteInfoEntry.COLUMN_NOTE_TITLE,
                NoteInfoEntry.COLUMN_NOTE_TEXT
        };

        mNotesCursor = db.query(NoteInfoEntry.TABLE_NAME, noteColumns, selection, selectionArgs, null, null, null);

        mCourseIdPos = mNotesCursor.getColumnIndex(NoteInfoEntry.COLUMN_COURSE_ID);
        mNoteTitlePos = mNotesCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TITLE);
        mNoteTextPos = mNotesCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TEXT);

        mNotesCursor.moveToNext();
        displayNote();


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
                deleteNoteFromDatabase();
            } else {
                storePreviousNoteValues();
            }

        } else {
            saveNote();
        }

        Log.d(TAG, "onPause");
    }

    private void deleteNoteFromDatabase() {
       final String selection = NoteInfoEntry._ID + " = ? ";
       final String[] selectionArgs =  { Integer.toString(mNoteId)};

        AsyncTask tast = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                getContentResolver().delete(mNotesUri, null, null);
                return null;
            }
        };

        tast.execute();
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

       String courseId = selectedCourseId();

        String noteTitle =  mTextNoteTitle.getText().toString();
        String noteText =  mTextNoteText.getText().toString();

        saveNoteToDatabase(courseId, noteTitle, noteText);
    }

    private String selectedCourseId() {
        int selectedPosition = mSpinnerCourses.getSelectedItemPosition();
        Cursor cursor = mAdapterCourse.getCursor();
        cursor.moveToPosition(selectedPosition);
        int courseIdPos = cursor.getColumnIndex(NoteInfoEntry.COLUMN_COURSE_ID);
        String courseId = cursor.getString(courseIdPos);
        return courseId;
    }

    private void saveNoteToDatabase(String courseId, String noteTitle, String noteText){
        ContentValues values = new ContentValues();
        values.put(Notes.COLUMN_COURSE_ID, courseId);
        values.put(Notes.COLUMN_NOTE_TITLE, noteTitle);
        values.put(Notes.COLUMN_NOTE_TEXT, noteText);

        getContentResolver().update(mNotesUri, values, null, null);


    }

    private void displayNote() {

        String courseId = mNotesCursor.getString(mCourseIdPos);
        String noteTitle = mNotesCursor.getString(mNoteTitlePos);
        String noteText = mNotesCursor.getString(mNoteTextPos);


        int courseIndex = getIndexOfCourseId(courseId);
        mSpinnerCourses.setSelection(courseIndex);
        mTextNoteTitle.setText(noteTitle);
        mTextNoteText.setText(noteText);

    }

    private int getIndexOfCourseId(String courseId) {
        Cursor cursor = mAdapterCourse.getCursor();

        int courseIdPos = cursor.getColumnIndex(CourseInfoEntry.COLUMN_COURSE_ID);
        int courseRowIndex = 0;
        boolean more = cursor.moveToFirst();

        while (more){
            String cursorCourseId = cursor.getString(courseIdPos);

            if (courseId.equals(cursorCourseId))
                break;

            courseRowIndex++;
            more = cursor.moveToNext();
        }

        return courseRowIndex;
    }

    private void readDisplayStateValues() {
        Intent intent = getIntent();
//        mNotePosition = intent.getIntExtra(NOTE_ID, ID_NOT_SET);
        mNoteId = intent.getIntExtra(NOTE_ID, ID_NOT_SET);

        isNewNote = mNoteId == ID_NOT_SET;
        if(isNewNote){
            createNewNote();
        }
        Log.i(TAG, "MnoteId " + mNoteId);
//        mNote = new NoteInfo(DataManager.getInstance().getCourses().get(mNoteId), "", "");
//        mNote = DataManager.getInstance().getNotes().get(mNoteId);

    }

    private void createNewNote() {
       AsyncTask<ContentValues, Void, Uri> task = new AsyncTask<ContentValues, Void, Uri>() {
           @Override
           protected Uri doInBackground(ContentValues... contentValues) {
               Log.d(TAG, "doInBackground Thread- " + Thread.currentThread().getId());
               ContentValues insertValues = contentValues[0];
               Uri rowUri = getContentResolver().insert(Notes.CONTENT_URI, insertValues);
               return rowUri;
           }

           @Override
           protected void onPostExecute(Uri uri) {
               Log.d(TAG, "onPostExcecute Thread- " + Thread.currentThread().getId());
               mNotesUri = uri;
               displaySnackBar(mNotesUri.toString());

           }
       };

        ContentValues values = new ContentValues();
        values.put(Notes.COLUMN_COURSE_ID, "");
        values.put(Notes.COLUMN_NOTE_TITLE, "");
        values.put(Notes.COLUMN_NOTE_TEXT, "");

        Log.d(TAG, "Call to execute Thread- " + Thread.currentThread().getId());
       task.execute(values);


    }

    private  void displaySnackBar(String txt){
        Toast.makeText(getApplicationContext(), txt, Toast.LENGTH_SHORT).show();

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

        } else if (id == R.id.action_set_reminder){
            showReminderNotification();
        }

        return super.onOptionsItemSelected(item);
    }

    private void showReminderNotification() {
        String noteText = mTextNoteText.getText().toString();
        String noteTitle = mTextNoteTitle.getText().toString();
        int noteId = (int)ContentUris.parseId(mNotesUri);
        NoteReminderNotification.notify(this,noteTitle, noteText, noteId);

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
        displayNote();
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


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader loader = null;
        if(id == LOADER_NOTES)
            loader = createLoaderNotes();
        else if (id == LOADER_COURSES)
            loader = createLoaderCourses();

        return loader;
    }

    private CursorLoader createLoaderCourses() {
        mCoursesQueryFinished = false;
        Uri uri = NoteKeeperProviderContract.Courses.CONTENT_URI;
        String [] courseColumns = {
                Courses.COLUMN_COURSE_TITLE,
                Courses.COLUMN_COURSE_ID,
                Courses._ID
        };

        return new CursorLoader(this, uri, courseColumns, null, null,
                Courses.COLUMN_COURSE_TITLE);

    }

    private CursorLoader createLoaderNotes() {
        mMNotesQueryFinished = false;

        String[] noteColumns = {
                Notes.COLUMN_COURSE_ID,
                Notes.COLUMN_NOTE_TITLE,
                Notes.COLUMN_NOTE_TEXT
        };
        mNotesUri = ContentUris.withAppendedId(Notes.CONTENT_URI, mNoteId);

        return new CursorLoader(this, mNotesUri, noteColumns, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == LOADER_NOTES)
            loadFinishedNotes(data);
        else if (loader.getId() == LOADER_COURSES){
            mAdapterCourse.changeCursor(data);
            mCoursesQueryFinished = true;
            displayNoteWhenQueriesFinished();
        }

    }

    private void loadFinishedNotes(Cursor data) {
        mNotesCursor = data;
        mCourseIdPos = mNotesCursor.getColumnIndex(NoteInfoEntry.COLUMN_COURSE_ID);
        mNoteTitlePos = mNotesCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TITLE);
        mNoteTextPos = mNotesCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TEXT);

        mNotesCursor.moveToNext();
        mMNotesQueryFinished = true;
        displayNoteWhenQueriesFinished();
    }

    private void displayNoteWhenQueriesFinished() {
        if (mMNotesQueryFinished && mCoursesQueryFinished)
            displayNote();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() == LOADER_NOTES){
            if (mNotesCursor != null)
                mNotesCursor.close();
        } else if (loader.getId() == LOADER_COURSES){
            mAdapterCourse.changeCursor(null);
        }

    }

}
