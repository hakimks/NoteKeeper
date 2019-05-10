package com.hakim.notekeeper;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

public class NoteListActivity extends AppCompatActivity {

//    private ArrayAdapter<NoteInfo> mAdaptorNotes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_list);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(NoteListActivity.this, NoteActivity.class));
            }
        });

        initialiseDisplayContent();
    }

    @Override
    protected  void onResume(){
        super.onResume();
//        mAdaptorNotes.notifyDataSetChanged();
    }

    private void initialiseDisplayContent() {
//        final ListView listNotes = (ListView) findViewById(R.id.list_notes);
//
//        List<NoteInfo> notes = DataManager.getInstance().getNotes();
//        mAdaptorNotes = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, notes);
//
//        listNotes.setAdapter(mAdaptorNotes);
//
//        listNotes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                Intent intent = new Intent(NoteListActivity.this, NoteActivity.class);
////                NoteInfo note = (NoteInfo) listNotes.getItemAtPosition(i);
//                intent.putExtra(NoteActivity.NOTE_POSITION, i); // i is the position of the selection
//                startActivity(intent);
//            }
//        });

        final RecyclerView recyclerNotes = (RecyclerView) findViewById(R.id.list_notes);
        final LinearLayoutManager notesLayoutManager = new LinearLayoutManager(this);
    }

}
