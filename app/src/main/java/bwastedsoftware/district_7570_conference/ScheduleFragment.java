package bwastedsoftware.district_7570_conference;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class ScheduleFragment extends Fragment {


    public ScheduleFragment() {
        // Required empty public constructor
    }

    View v;
    SwipeRefreshLayout mSwipeRefreshLayout;
    RecyclerView rv;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_schedule, container, false);

        ////followed tutorial on creating cards, found here: https://code.tutsplus.com/tutorials/getting-started-with-recyclerview-and-cardview-on-android--cms-23465
        rv = (RecyclerView)v.findViewById(R.id.rv);
        rv.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        rv.setLayoutManager(llm);

        initializeData();
        initializeAdapter(rv);
        refreshData();

        mSwipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.swipeRefreshLayout);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Refresh items
                refreshData();
            }
        });

        return v;
    }

    RVAdapter adapter;

    private void initializeAdapter(RecyclerView rv)
    {
        adapter = new RVAdapter(events, getActivity(), ScheduleFragment.this);
        rv.setAdapter(adapter);
    }




    ArrayList<Event> events;
    FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    DatabaseReference myRef = mDatabase.getReference().child("Events");

    private void initializeData() {
        events = new ArrayList<>();
    }

    private void refreshData()
    {
        adapter.clear();
        events.add(new Event("EVENT  1", "LOCATION", "DATE", "TIME", "DETAILS", new Speaker("Billy", "Bio", "http://www.munkurious.com/sharex/2017.10/ghanaTempleStainedGlass_100x.png")));
        events.add(new Event("EVENT TITLE 2", "LOCATION", "DATE", "TIME", "DETAILS", new Speaker("Sue", "Bio", "Photo")));


        final ArrayList<Event> newevents = new ArrayList<>();

        // Read from the database
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                //String value = dataSnapshot.getValue(String.class);
                //Event = dataSnapshot.getValue(Post.class);
                //Post post = dataSnapshot.getValue(Post.class);
                for(DataSnapshot childrenSnapShot : dataSnapshot.getChildren())
                {
                    Event event = childrenSnapShot.getValue(Event.class);
                    newevents.add(new Event(event.getTitle(), event.getLocation(), event.getDate(), event.getTime(), event.getDetails(), event.getSpeaker()));
                    //Log.w("GETTING CARDS", "value is" + event.getDate() + event.getLocation() + childrenSnapShot.getKey());
                }
                //Log.d("FIREBASE", "Value is: " + post);
                addEvents(newevents);

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("FIREBASE", "Failed to read value.", error.toException());
            }
        });

    }

    private void addEvents(ArrayList<Event> newevents)
    {
        events.addAll(newevents);
        Log.w("PROBLEM HERE", "LIST #" + events.size());
        newevents.add(new Event("EVENT TITLE 3", "LOCATION", "DATE", "TIME", "DETAILS", new Speaker("Aaron's Little Helper", "bio", "Photo")));
        onItemsLoadComplete();

    }

    void onItemsLoadComplete() {
        // Update the adapter and notify data set changed
        // ...

        // Stop refresh animation
        adapter.notifyDataSetChanged();
        mSwipeRefreshLayout.setRefreshing(false);
    }

    public void loadEventDetails(Event event)
    {
        FragmentTransaction t = this.getFragmentManager().beginTransaction();
        EventFragment mFrag = new EventFragment();
        mFrag.passEvent(getActivity(),event);
        t.replace(R.id.main_container, mFrag);
        t.commit();
    }

}

