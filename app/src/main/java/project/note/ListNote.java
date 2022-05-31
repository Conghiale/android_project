package project.note;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import Adapter.NoteAdapter;
import project.note.databinding.ListNotesBinding;
import Model.Note;
import project.note.databinding.ListNotesBinding;

public class ListNote extends AppCompatActivity implements NoteAdapter.setOnItemClickListener{

    private ListNotesBinding listNotesBinding;

    private ArrayList<Note> listData;
    private RecyclerView recyclerView;
    private NoteAdapter adapter;


    private FirebaseFirestore firestore = FirebaseFirestore.getInstance ();
    private CollectionReference ref = firestore.collection ("Notes");

    private int pos = -1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        listNotesBinding = ListNotesBinding.inflate (getLayoutInflater ());
        setContentView (listNotesBinding.getRoot ());

        adapter = initViews();
        getDataFromFirebase();
    }

    private NoteAdapter initViews() {
        listData = new ArrayList<> ();
        NoteAdapter myAdapter = new NoteAdapter (this, listData);
        myAdapter.setListener (this);

        recyclerView = findViewById (R.id.recyclerView);

        LinearLayoutManager layoutManager = new LinearLayoutManager (this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager (layoutManager);
        recyclerView.setHasFixedSize (true);
        recyclerView.setItemAnimator (new DefaultItemAnimator ());
        recyclerView.addItemDecoration (new DividerItemDecoration (this, DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter (myAdapter);

        return myAdapter;
    }

    private void getDataFromFirebase() {
        ref.get ().addOnCompleteListener (task -> {
            if(task.isSuccessful () && !task.getResult ().isEmpty ()){
                for(QueryDocumentSnapshot documentSnapshot : task.getResult ()){
                    Note note = new Note ();
                    note.setTitle (documentSnapshot.get ("Title").toString ());
                    note.setDay (documentSnapshot.get ("Day").toString ());
                    note.setTime (documentSnapshot.get ("Time").toString ());
                    note.setContent (documentSnapshot.get ("Content").toString ());
                    note.setCheck ((Boolean) documentSnapshot.get ("Check"));
                    note.setId (documentSnapshot.getId ());

                    listData.add (note);
                    adapter.notifyDataSetChanged ();
                }
            }
        })
                .addOnSuccessListener (queryDocumentSnapshots -> Log.e ("TAG", "Get Data Success."))
                .addOnFailureListener (new OnFailureListener () {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e ("TAG", "Get Data Failure: " + e);
                    }
                });
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.option_menu, menu);

        MenuItem item = menu.findItem(R.id.showDone);
        item.setActionView(R.layout.activity_switch);

        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch displayDoneSwitch = item.getActionView().findViewById(R.id.displayDoneSwitch);

        displayDoneSwitch.setOnCheckedChangeListener((compoundButton, b) -> adapter.show(displayDoneSwitch.isChecked()));
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.addNote:
                Intent intent = new Intent(this, project.note.AddNote.class);
                intent.putExtra ("Function", "SAVE");
                startActivityForResult(intent, 100);
                return true;
            case R.id.searchNote:
                adapter.showDialog(1);
                return true;
            case R.id.deleteDoneItem:
                adapter.showDialog (1);
                return true;
            case R.id.deleteAllItem:
                adapter.showDialog (-1);
                return true;
            default:
                Toast.makeText(this, "default", Toast.LENGTH_SHORT).show();
                return false;
        }
    }

    @Override
    public void onItemClickListener(Note note, int position, View view) {
        Log.e ("TAG10", "note: " + note.toString ());
        pos = position;
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.inflate(R.menu.popup_menu);
        popupMenu.show();

        popupMenu.setOnMenuItemClickListener(menuItem -> {
            if(menuItem.getItemId () == R.id.Remove){
                adapter.remove (position);
            }
            if(menuItem.getItemId () == R.id.Edit){
                Intent intent = new Intent(this, project.note.AddNote.class);
                intent.putExtra ("Function", "UPDATE");
                intent.putExtra ("OldNote", note);
                startActivityForResult(intent, 200);
            }
            return false;
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent intent) {
        super.onActivityResult (requestCode, resultCode, intent);
        if(requestCode == 100){
            if(resultCode == RESULT_OK){
                Note myNote = intent.getParcelableExtra ("NOTE");
                if(myNote != null){
                    adapter.add (myNote);
                }
            }
        }
        if(requestCode == 200){
            if(resultCode == RESULT_OK){
                Note upNote = intent.getParcelableExtra ("NOTE");
                if(upNote != null){
                    adapter.updateData (upNote, pos);
                }
            }
        }
    }
}
