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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class AddClassifiedFragment extends Fragment {
    private static final String TAG = "AddEventFragment";
    private DatabaseReference dbRef;
    private int nextClassifiedID;
    private boolean isEdit;
    private String eventId, intervalID = "1";
    private Button button;
    private TextView headTxt;
    private List<IntervalModel> dataInterval = Arrays.asList(
            new IntervalModel("1", "30 menit"),
            new IntervalModel("2", "1 jam"),
            new IntervalModel("3", "3 jam"),
            new IntervalModel("4", "6 jam"),
            new IntervalModel("5", "12 jam"),
            new IntervalModel("6", "24 jam")
            
    );
    private ArrayAdapter<IntervalModel> adapterInterval;

    //nyobo volley
    private RequestQueue mRequestQue;
    private String URL = "https://fcm.googleapis.com/fcm/send";
    private Spinner spInterval;
    //nyobo volley tutup

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.event_add_layout,
                container, false);

        button = (Button) view.findViewById(R.id.post_add);
        headTxt = view.findViewById(R.id.add_head_tv);
        dbRef = FirebaseDatabase.getInstance().getReference();
        //nyobo volley
        mRequestQue = Volley.newRequestQueue(getActivity());
        //nyobo volley tutup

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

        spInterval = view.findViewById(R.id.sp_interval);
        adapterInterval = new ArrayAdapter<>(getActivity(), R.layout.support_simple_spinner_dropdown_item, dataInterval);
        spInterval.setAdapter(adapterInterval);

        spInterval.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
                intervalID = dataInterval.get(pos).getId().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

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
                            "inisasi id berhasil dibuat, silahkan tekan tambahkan",
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
                            Log.d(TAG, "Classified berhasil di tambahkan ke database");
                            Toast.makeText(getActivity(),
                                    "Acara berhasil di posting",
                                    Toast.LENGTH_SHORT).show();
                            //nyobo volley
                            sendNotification();

                            //nyobo volley tutup
                        } else {
                            Log.d(TAG, "Classified gagal di tambahkan ke database");
                            Toast.makeText(getActivity(),
                                    "Acara gagal ditambahkan",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    private void sendNotification() {

                        JSONObject json = new JSONObject();
                        try {
                            json.put("to","/topics/"+"news");
                            JSONObject notificationObj = new JSONObject();
                            notificationObj.put("title","UKM IK EVENT");
                            notificationObj.put("body","ada acara baru nih guys, ayo silahkan dicek !");
                            json.put("notification",notificationObj);

                            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, URL,
                                    json,
                                    new Response.Listener<JSONObject>() {
                                        @Override
                                        public void onResponse(JSONObject response) {

                                            Log.d("MUR", "onResponse: ");
                                        }
                                    }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Log.d("MUR", "onError: "+error.networkResponse);
                                }
                            }
                            ){
                                @Override
                                public Map<String, String> getHeaders() throws AuthFailureError {
                                    Map<String,String> header = new HashMap<>();
                                    header.put("content-type","application/json");
                                    header.put("authorization","key=AIzaSyB1C7zEeYTlRI52w2lhcxl_JGuebql9S3Q");
                                    return header;
                                }
                            };
                            mRequestQue.add(request);
                        }
                        catch (JSONException e)

                        {
                            e.printStackTrace();
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
        spInterval.setSelection(adapterInterval.getPosition(adapterInterval.getItem(Integer.parseInt(cEvent.getInterval())-1)));
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
        event.setInterval(((EditText) getActivity()
                .findViewById(R.id.interval_a)).getText().toString());
        event.setTempat(((EditText) getActivity()
                .findViewById(R.id.tempat_a)).getText().toString());
        event.setInterval(intervalID);
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
                .findViewById(R.id.interval_a)).setText("");
        ((EditText) getActivity()
                .findViewById(R.id.tempat_a)).setText("");
    }

    private void addClassifieds() {
        Intent i = new Intent();
        i.setClass(getActivity(), MainActivity.class);
        startActivity(i);
    }

}
