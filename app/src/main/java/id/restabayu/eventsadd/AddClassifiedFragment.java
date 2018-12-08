package id.restabayu.eventsadd;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;


public class AddClassifiedFragment extends Fragment {
    private static final String TAG = "AddEventFragment";

    private DatabaseReference dbRef;
    private int nextClassifiedID;
    private boolean isEdit;
    private String eventId;
    private Button button;
    private TextView headTxt;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.event_add_layout,
                container, false);

        button = (Button) view.findViewById(R.id.post_add);
        headTxt = view.findViewById(R.id.add_head_tv);

        dbRef = FirebaseDatabase.getInstance().getReference();

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isEdit) {
                    addEvent();
                } else {
                    updateEvent();
                }

            }
        });

        //add or update depending on existence of eventId in arguments
        if (getArguments() != null) {
            eventId = getArguments().getString("eventId");
        }
        if (eventId != null) {
            populateUpdateEvent(); //kiem tra neu eventId != null thi se la layout edit event
        }

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    public void addEvent() {
        ClassifiedEvent classifiedEvent = createClassifiedEventObj();
        addClassifiedToDB(classifiedEvent);
    }

    public void updateEvent() {
        ClassifiedEvent classifiedEvent = createClassifiedEventObj();
        updateClassifiedToDB(classifiedEvent);
    }

    private void addClassifiedToDB(final ClassifiedEvent classifiedEvent) {
        final DatabaseReference idDatabaseRef = FirebaseDatabase.getInstance()
                .getReference("ClassifiedIDs").child("id");

        idDatabaseRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                //create id node if it doesn't exist
                //this code runs only once
                if (mutableData.getValue(int.class) == null) {
                    idDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            //set initial value
                            if(dataSnapshot != null && dataSnapshot.getValue() == null){
                                idDatabaseRef.setValue(1);
                                Log.d(TAG, "Initial id is set");
                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                    Log.d(TAG, "Classified id null so " +
                            " transaction aborted, " );

                    return Transaction.abort();
                }

                nextClassifiedID = mutableData.getValue(int.class);
                mutableData.setValue(nextClassifiedID + 1);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean state,
                                   DataSnapshot dataSnapshot) {
                if (state) {
                    Log.d(TAG, "Classified id retrieved ");
                    addClassified(classifiedEvent, ""+nextClassifiedID);
                } else {
                    Log.d(TAG, "Classified id retrieval unsuccessful " + databaseError);
                    Toast.makeText(getActivity(),
                            "There is a problem, please submit event post again",
                            Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    private void addClassified(ClassifiedEvent classifiedEvent, String cEventId) {
        classifiedEvent.setEventId(cEventId);
        dbRef.child("classified").child(cEventId)
                .setValue(classifiedEvent)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            if(isEdit){
                                addClassifieds();
                            }else{
                                restUi();
                            }
                            Log.d(TAG, "Classified has been added to db");
                            Toast.makeText(getActivity(),
                                    "Classified has been posted",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Log.d(TAG, "Classified couldn't be added to db");
                            Toast.makeText(getActivity(),
                                    "Classified could not be added",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    private void populateUpdateEvent() {
        headTxt.setText("Edit Acara");
        button.setText("Edit Acara");
        isEdit = true;

        dbRef.child("classified").child(eventId).
                addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        ClassifiedEvent cEvent = dataSnapshot.getValue(ClassifiedEvent.class);
                        displayEventForUpdate(cEvent);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d(TAG, "Error trying to get classified event for update " +
                                ""+databaseError);
                        Toast.makeText(getActivity(),
                                "Please try classified edit action again",
                                Toast.LENGTH_SHORT).show();
                    }
                });

    }
    private void displayEventForUpdate(ClassifiedEvent cEvent){// getActivity().findViewById
        ((EditText) getActivity()
                .findViewById(R.id.nama_a)).setText(cEvent.getNama());
        ((EditText) getActivity()
                .findViewById(R.id.category_a)).setText(cEvent.getKategori());
        ((EditText) getActivity()
                .findViewById(R.id.desc_a)).setText(cEvent.getDeskripsi());
        ((EditText) getActivity()
                .findViewById(R.id.tanggal_a)).setText(cEvent.getTanggal());
        ((EditText) getActivity()
                .findViewById(R.id.waktu_a)).setText(cEvent.getWaktu());
        ((EditText) getActivity()
                .findViewById(R.id.tempat_a)).setText(cEvent.getTempat());
    }
    private void updateClassifiedToDB(ClassifiedEvent classifiedEvent) {
        addClassified(classifiedEvent, eventId);
    }

    private ClassifiedEvent createClassifiedEventObj() {
        final ClassifiedEvent event = new ClassifiedEvent();
        event.setNama(((EditText) getActivity()
                .findViewById(R.id.nama_a)).getText().toString());
        event.setKategori(((EditText) getActivity()
                .findViewById(R.id.category_a)).getText().toString());
        event.setDeskripsi(((EditText) getActivity()
                .findViewById(R.id.desc_a)).getText().toString());
        event.setTanggal(((EditText) getActivity()
                .findViewById(R.id.tanggal_a)).getText().toString());
        event.setWaktu(((EditText) getActivity()
                .findViewById(R.id.waktu_a)).getText().toString());
        event.setTempat(((EditText) getActivity()
                .findViewById(R.id.tempat_a)).getText().toString());
        return event;
    }

    private void restUi() {
        ((EditText) getActivity()
                .findViewById(R.id.nama_a)).setText("");
        ((EditText) getActivity()
                .findViewById(R.id.category_a)).setText("");
        ((EditText) getActivity()
                .findViewById(R.id.desc_a)).setText("");
        ((EditText) getActivity()
                .findViewById(R.id.tanggal_a)).setText("");
        ((EditText) getActivity()
                .findViewById(R.id.waktu_a)).setText("");
        ((EditText) getActivity()
                .findViewById(R.id.tempat_a)).setText("");
    }

    private void addClassifieds() {
        Intent i = new Intent();
        i.setClass(getActivity(), MainActivity.class);
        startActivity(i);
    }

}
