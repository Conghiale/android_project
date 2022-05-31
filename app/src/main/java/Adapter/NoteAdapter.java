package Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;

import project.note.ListNote;
import project.note.R;
import Model.Note;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.MyViewHolder> {

    public interface setOnItemClickListener{
        void onItemClickListener(Note note, int position, View view);
    }

    private Context context;
    private ArrayList<Note> listNote;
    private ArrayList<Note> tmp;

    //private FirebaseFirestore firestore = FirebaseFirestore.getInstance ();
    //private CollectionReference ref = firestore.collection ("Notes");

    private setOnItemClickListener listener;

    public NoteAdapter(Context context, ArrayList<Note> listNote) {
        this.context = context;
        this.listNote = listNote;
        this.tmp = new ArrayList<> ();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from (context);
        View root = inflater.inflate (R.layout.note, parent, false);
        return new MyViewHolder (root);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Note note = listNote.get (position);

        holder.tvTitle.setText (note.getTitle ());
        holder.tvDayTime.setText (note.getDay () + " " + note.getTime ());
        holder.tvContent.setText (note.getContent ());
        holder.cbDone.setChecked (note.isCheck ());

        //ImageView
    }

    @Override
    public int getItemCount() {
        return listNote.size ();
    }

    public void setListener(setOnItemClickListener listener) {
        this.listener = listener;
    }

    public void add(@Nullable Note note) {
        //Push Data to Firebase
        pushDataToFirebase(note);
        listNote.add(note);
        notifyItemInserted(listNote.size() - 1);
    }

    public void remove(int i) {
        String id = listNote.get (i).getId ();
        ListNote.ref.document (id).delete ();

        listNote.remove(i);
        notifyItemRemoved(i);
    }

    public void removeDone() {
        int size = listNote.size();
        for (int i = size - 1; i >= 0; i--) {
            if (listNote.get(i).isCheck ())
                remove(i);
        }
    }

    public void removeAll() {
        listNote.clear ();
        notifyDataSetChanged ();
        Log.e ("TAG30", "removeAll: ");

        ListNote.ref.get ()
                .addOnSuccessListener (queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        ListNote.ref.document (documentSnapshot.getId ()).delete ();
                    }
                })
        .addOnFailureListener (e -> Log.e ("TAG", "Remove Data Failure." + e));
    }

    public void showClear() {
        while (listNote.size() != 0) {
            listNote.remove(0);
            notifyItemRemoved(0);
        }
    }

    public void show(boolean o) {
        if (o) {
            for(Note td : listNote)
                tmp.add (td);

            for (int i = listNote.size() - 1; i >= 0; i--) {
                if (listNote.get(i).isCheck ()) {
                    listNote.remove(i);
                    notifyItemRemoved(i);
                }
            }
        }
        else {
            showClear();
            int size = tmp.size();

            for (int i = 0; i < size; i++) {
                listNote.add(tmp.get(i));
                notifyItemInserted(i);
            }

            tmp.clear ();
        }
    }

    public void updateData(Note upNote, int pos){
        listNote.get (pos).setTitle (upNote.getTitle ());
        listNote.get (pos).setDay (upNote.getDay ());
        listNote.get (pos).setTime (upNote.getTime ());
        listNote.get (pos).setContent (upNote.getContent ());
        //ImageView

        notifyItemChanged (pos);
        updateDataToFirebase (upNote);
    }

    private void pushDataToFirebase(Note note) {
        Log.e ("TAG", "pushDataToFirebase: ");
        HashMap<String, Object> itemNote = new HashMap<> ();
        itemNote.put ("Title", note.getTitle ());
        itemNote.put ("Day", note.getDay ());
        itemNote.put ("Time", note.getTime ());
        itemNote.put ("Content", note.getContent ());
        itemNote.put ("Check", note.isCheck ());

        ListNote.ref.add (itemNote)
        .addOnSuccessListener (documentReference -> {
            Log.e ("TAG14", "Push Data Success. ID: " + documentReference.getId ());
            note.setId (documentReference.getId ());
        })
        .addOnFailureListener (e -> Log.e ("TAG", "Push Data Failure." + e));
    }

    public void showDialog(int position) {
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle("REMOVE DATA")
                .setMessage("Are you sure you want to Remove this data?")
                .setPositiveButton("Remove", (dialogInterface, i) -> {
                    if (position == -1) {
                        removeAll ();
                    } else {
                        removeDone();
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();
        dialog.show();
    }

    private void updateDataToFirebase(Note note) {
        HashMap<String, Object> upNote = new HashMap<> ();
        upNote.put ("Title", note.getTitle ());
        upNote.put ("Day", note.getDay ());
        upNote.put ("Time", note.getTime ());
        upNote.put ("Content", note.getContent ());
        //ImageView

        ListNote.ref.document (note.getId ()).update (upNote)
        .addOnSuccessListener (unused -> Log.e ("TAG", "Update Data Success."))
        .addOnFailureListener (e -> Log.e ("TAG", "Update Data Failure: " + e));

    }

    private void updateDataToFirebase(int position) {
        //ImageView
        Log.e ("TAG13", "ID: " + listNote.get (position).getId ());
        ListNote.ref.document (listNote.get (position).getId ()).update ("Check", listNote.get (position).isCheck ());
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        TextView tvTitle, tvDayTime, tvContent;
        ImageView ivPicture;
        CheckBox cbDone;
        public MyViewHolder(@NonNull View itemView) {
            super (itemView);

            tvTitle = itemView.findViewById (R.id.tvTitle);
            tvDayTime = itemView.findViewById (R.id.tvDayTime);
            tvContent = itemView.findViewById (R.id.tvContent);
            ivPicture = itemView.findViewById (R.id.ivPicture);
            cbDone = itemView.findViewById (R.id.cbDone);

            cbDone.setOnCheckedChangeListener ((compoundButton, b) -> {
                listNote.get (getAdapterPosition ()).setCheck (b);
                Log.e ("TAG20", "Note: " + listNote.get (getAdapterPosition()));
                updateDataToFirebase(getAdapterPosition ());
            });

            itemView.setOnClickListener (view -> {
                if(listener != null)
                    listener.onItemClickListener (listNote.get (getAdapterPosition ()), getAdapterPosition (), view);
            });
        }
    }
}
