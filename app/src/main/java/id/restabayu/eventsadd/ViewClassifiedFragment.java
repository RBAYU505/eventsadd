package id.restabayu.eventsadd;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class ViewClassifiedFragment extends Fragment {
    private static final String TAG = "ViewEventsFragment";
    private DatabaseReference databaseReference;
    private RecyclerView eventsRecyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.event_view_layout,
                container, false);

        databaseReference = FirebaseDatabase.getInstance().getReference();

        Button button = (Button) view.findViewById(R.id.tampilkan_b);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getClassifiedEvents();
            }
        });

        eventsRecyclerView = (RecyclerView) view.findViewById(R.id.ads_lst);

        LinearLayoutManager recyclerLayoutManager =
                new LinearLayoutManager(getActivity().getApplicationContext());
        eventsRecyclerView.setLayoutManager(recyclerLayoutManager);

        DividerItemDecoration dividerItemDecoration =
                new DividerItemDecoration(eventsRecyclerView.getContext(),
                        recyclerLayoutManager.getOrientation());
        eventsRecyclerView.addItemDecoration(dividerItemDecoration);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    public void getClassifiedEvents() {
        String kategori = ((TextView) getActivity()
                .findViewById(R.id.category_v)).getText().toString();
        getClassifiedsFromDb(kategori);
    }

    private void getClassifiedsFromDb(final String kategori) {
        databaseReference.child("classified").orderByChild("kategori")
                .equalTo(kategori).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<ClassifiedEvent> eventsList = new ArrayList<ClassifiedEvent>();
                for (DataSnapshot eventSnapshot: dataSnapshot.getChildren()) {
                    eventsList.add(eventSnapshot.getValue(ClassifiedEvent.class));
                }
                Log.d(TAG, "no of events for search is "+eventsList.size());
                EventRecyclerView recyclerViewAdapter = new
                        EventRecyclerView(eventsList, getActivity());
                eventsRecyclerView.setAdapter(recyclerViewAdapter);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "Error trying to get classified events for " +kategori+
                        " "+databaseError);
                Toast.makeText(getActivity(),
                        "Error trying to get classified events for " +kategori,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
