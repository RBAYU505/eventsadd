package id.restabayu.eventsadd;

import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;


public class EventRecyclerView extends RecyclerView.Adapter<EventRecyclerView.ViewHolder>{
    private List<ClassifiedEvent> eventsList;
    private Context context;

    public EventRecyclerView(List<ClassifiedEvent> list, Context ctx) {
        eventsList = list;
        context = ctx;
    }
    @Override
    public int getItemCount() {
        return eventsList.size();
    }

    @Override
    public EventRecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.event_item_layout, parent, false);

        EventRecyclerView.ViewHolder viewHolder =
                new EventRecyclerView.ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(EventRecyclerView.ViewHolder holder, int position) {
        final int itemPos = position;
        final ClassifiedEvent classifiedEvent = eventsList.get(position);
        holder.nama.setText(classifiedEvent.getNama());
        holder.tanggal.setText(classifiedEvent.getTanggal());
        holder.waktu.setText(classifiedEvent.getWaktu());

        holder.edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editClassifiedEvent(classifiedEvent.getEventId());
            }
        });

        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteClassifiedEvent(classifiedEvent.getEventId(), itemPos);
            }
        });
    }
    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView nama;
        public TextView tanggal;
        public TextView waktu;
        public Button edit;
        public Button delete;

        public ViewHolder(View view) {
            super(view);
            nama = (TextView) view.findViewById(R.id.nama_i);
            tanggal = (TextView) view.findViewById(R.id.tanggal_i);
            waktu = (TextView) view.findViewById(R.id.waktu_i);
            edit = view.findViewById(R.id.edit_ad_b);
            delete = view.findViewById(R.id.delete_ad_b);
        }
    }
    private void editClassifiedEvent(String eventId){
        FragmentManager fm = ((MainActivity)context).getFragmentManager();

        Bundle bundle=new Bundle();
        bundle.putString("eventId", eventId);

        AddClassifiedFragment addFragment = new AddClassifiedFragment();
        addFragment.setArguments(bundle);

        fm.beginTransaction().replace(R.id.adds_frame, addFragment).commit();
    }
    private void deleteClassifiedEvent(String eventId, final int position){
        FirebaseDatabase.getInstance().getReference()
                .child("classified").child(eventId).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            //remove item from list alos and refresh recyclerview
                            eventsList.remove(position);
                            notifyItemRemoved(position);
                            notifyItemRangeChanged(position, eventsList.size());

                            Log.d("Delete Event", "Classified has been deleted");
                            Toast.makeText(context,
                                    "Classified has been deleted",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Log.d("Delete Event", "Classified couldn't be deleted");
                            Toast.makeText(context,
                                    "Classified could not be deleted",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
