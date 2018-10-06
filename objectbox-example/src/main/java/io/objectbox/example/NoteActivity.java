package io.objectbox.example;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import io.objectbox.Box;
import io.objectbox.BoxStore;
import io.objectbox.query.Query;

public class NoteActivity extends Activity {

    private EditText editText;
    private Button addNoteButton;

    private Box<Note> notesBox;
    private Box<Animal> animalBox;
    private Box<Zoo> zooBox;
    private Query<Note> notesQuery;
    private NotesAdapter notesAdapter;
    private Note noteToUpdate;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        setUpViews();

        BoxStore boxStore = ((App) getApplication()).getBoxStore();
        notesBox = boxStore.boxFor(Note.class);
        animalBox = boxStore.boxFor(Animal.class);
        zooBox = boxStore.boxFor(Zoo.class);

        testingObjectBox();

        // query all notes, sorted a-z by their text (https://docs.objectbox.io/queries)
        notesQuery = notesBox.query().order(Note_.text).build();
        updateNotes();
    }

    /** Manual trigger to re-query and update the UI. For a reactive alternative check {@link ReactiveNoteActivity}. */
    private void updateNotes() {
        List<Note> notes = notesQuery.find();
        notesAdapter.setNotes(notes);
    }

    protected void setUpViews() {
        ListView listView = findViewById(R.id.listViewNotes);
        listView.setOnItemClickListener(noteClickListener);
        listView.setOnItemLongClickListener(noteLongClickListener);

        notesAdapter = new NotesAdapter();
        listView.setAdapter(notesAdapter);

        addNoteButton = findViewById(R.id.buttonAdd);
        addNoteButton.setEnabled(false);

        editText = findViewById(R.id.editTextNote);
        editText.setOnEditorActionListener(new OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if (addNoteButton.getText().equals("UPDATE")) {
                        noteToUpdate.setText(String.valueOf(editText.getText()));
                        notesBox.put(noteToUpdate);
                        editText.setText("");
                        updateNotes();
                        return true;
                    } else {
                        addNote();
                        return true;
                    }
                }
                return false;
            }
        });
        editText.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() != 0) {
                    addNoteButton.setEnabled(true);
                } else {
                    addNoteButton.setEnabled(false);
                    addNoteButton.setText("ADD");
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

    }

    public void onAddButtonClick(View view) {
        if (addNoteButton.getText().equals("UPDATE")) {
            noteToUpdate.setText(String.valueOf(editText.getText()));
            notesBox.put(noteToUpdate);
            editText.setText("");
            updateNotes();
        } else {
            addNote();
        }
    }

    private void addNote() {
        String noteText = editText.getText().toString();
        editText.setText("");

        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
        String comment = "Added on " + df.format(new Date());

        Note note = new Note();
        note.setText(noteText);
        note.setComment(comment);
        note.setDate(new Date());
        notesBox.put(note);
        Log.d(App.TAG, "Inserted new note, ID: " + note.getId());

        updateNotes();
    }

    OnItemClickListener noteClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            noteToUpdate = notesAdapter.getItem(position);
            editText.setText(noteToUpdate.getText());
            addNoteButton.setText("UPDATE");
        }
    };

    AdapterView.OnItemLongClickListener noteLongClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int position, long l) {
            new AlertDialog.Builder(NoteActivity.this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("Are you sure?")
                    .setMessage("Do you want to delete this note?")
                    .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Note note = notesAdapter.getItem(position);
                            notesBox.remove(note);
                            updateNotes();
                        }
                    })
                    .setNegativeButton("NO", null)
                    .show();
            return true; // if we said false, it will assume that we want to do a short click
        }
    };

    private void testingObjectBox() {
        /* CRUD METHODS *///////////////////////////////////////////////////////////////////////////
        // Loads all animals
        List<Animal> animals = animalBox.getAll();
        Log.i("getAll()", animals.toString());

        // Find a specific animal in the database
        long myAnimal = 12;
        try {
            Animal animal = animalBox.get(myAnimal);
            Log.i("get()", animal.toString());
        }catch (NullPointerException e) {
            Log.i("get()", "Animal doesn't exist");
        }

        // Insert/Update an animal into the database
        Animal newAnimal = new Animal("Manolo", true, false, false);
        animalBox.put(newAnimal);

        // Query for all the walking animals
        List<Animal> walkingAnimals = animalBox.query().equal(Animal_.walking, true).build().find();
        Log.i("query()", walkingAnimals.toString());

        /*// Delete all animals
        animalBox.removeAll();*/

        // Delete a specific animal in the database
        long myAnimal2 = 10;
        animalBox.remove(myAnimal2);

        // Returns the number of objects stored in this box
        Log.i("count()", String.valueOf(animalBox.count()));


        /* RELATIONS *//////////////////////////////////////////////////////////////////////////////
        Zoo myZoo = new Zoo("La Huasteca Potosina");

        Animal elephant = new Animal("Elefante", false, false, true);
        Animal fish = new Animal("Pescado", false, true, false);


        // To-one relation: Set the Zoo that an animal belongs to and save it to the database
        elephant.zoo.setTarget(myZoo);
        animalBox.put(elephant);

        // To-one relation: Get the Zoo that an animal belongs to
        Zoo elephantZoo = elephant.zoo.getTarget();
        Log.i("elephantZoo", String.valueOf(elephantZoo));


        // To-many relation: Add Animals to the Zoo and save it to the database
        myZoo.animals.add(elephant);
        myZoo.animals.add(fish);
        zooBox.put(myZoo);

        // To-many relation: Get Animals that belongs to a Zoo
        List<Animal> zooAnimals = myZoo.animals;
        //Toast.makeText(getApplicationContext(), String.valueOf(zooAnimals.size()), Toast.LENGTH_SHORT).show();
        for (Animal animal : zooAnimals) {
            Log.i("forOne", animal.getId() + " --> " + animal.getName());
        }
        for (int i=0; i<zooAnimals.size(); i++) {
            Log.i("forTwo", zooAnimals.get(i).getId() + " --> " + zooAnimals.get(i).getName());
        }
    }

}