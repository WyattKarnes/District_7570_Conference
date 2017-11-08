package bwastedsoftware.district_7570_conference;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;


public class ScheduleFragment extends Fragment {


    public ScheduleFragment() {
        // Required empty public constructor
    }

    View v;
    SwipeRefreshLayout mSwipeRefreshLayout;
    ArrayList<ArrayList<Event>> days;
    ViewPager viewPager;
    PagerAdapter adapter;
    Boolean isMine;
    FirebaseAuth mAuth;
    String user_id;
    DatabaseReference myRef;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_schedule, container, false);
        Bundle args = getArguments();
        isMine = args.getBoolean("IS_MY_SCHEDULE");
        mAuth = FirebaseAuth.getInstance();
        user_id = mAuth.getCurrentUser().getUid();
        myRef = mDatabase.getReference().child("Users").child(user_id).child("userEvents");
        initializeData();
        //initializeAdapters(rv);
        if(!isMine){
            refreshData();} else {
            refreshMyData();
        }
        // Locate the ViewPager in viewpager_main.xml
        viewPager = (ViewPager) v.findViewById(R.id.pager);
        // Pass results to ViewPagerAdapter Class
        initializeAdapter();

        mSwipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.swipeRefreshLayout);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Refresh items
                if(!isMine){
                    refreshData();} else {
                    refreshMyData();
                }
            }
        });

        return v;
    }


    FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    DatabaseReference allRef = mDatabase.getReference().child("Events");


    private void initializeData() {
        days = new ArrayList<>();
    }

    private void initializeAdapter()
    {
        adapter = new ViewPagerAdapter(getActivity(), days, ScheduleFragment.this);
        // Binds the Adapter to the ViewPager
        viewPager.setAdapter(adapter);
    }

    private void refreshData()
    {
        //days(0).add(new Event("EVENT  1", "LOCATION", "DATE", "TIME", "DETAILS", new Speaker("Billy", "Bio", "http://www.munkurious.com/sharex/2017.10/ghanaTempleStainedGlass_100x.png")));
        //events.add(new Event("EVENT TITLE 2", "LOCATION", "DATE", "TIME", "DETAILS", new Speaker("Sue", "Bio", "Photo")));


        final ArrayList<Event> newevents = new ArrayList<>();

        // Read from the database
        allRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot childrenSnapShot : dataSnapshot.getChildren())
                {
                    Event event = childrenSnapShot.getValue(Event.class);
                    newevents.add(new Event(event.getTitle(), event.getLocation(), event.getDate(), event.getTime(), event.getDetails(), event.getSpeaker()));
                }
                addEvents(newevents);

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("FIREBASE", "Failed to read value.", error.toException());
            }
        });

    }

    private void refreshMyData()
    {
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
        //events.addAll(newevents);
        //Log.w("PROBLEM HERE", "LIST #" + events.size());
        //newevents.add(new Event("EVENT TITLE 3", "LOCATION", "DATE", "TIME", "DETAILS", new Speaker("Aaron's Little Helper", "bio", "Photo")));

        days.clear();

        for(Event e : newevents)
        {
            //Log.w("LOOK HERE", "HEY TRYING AN EVENT");
            if(days.size() == 0)
            {
                ArrayList<Event> day2 = new ArrayList<>();
                day2.add(e);
                days.add(day2);
                //Log.w("LOOK HERE", "INIT DAY, MAKING NEW ARRAY LIST." + days.size());
            }
            else
            {
                for (int i = 0; i < days.size(); i++)
                {
                    if (days.get(i) != null && days.get(i).get(0).getDate().contains(e.getDate()))
                    {
                        days.get(i).add(e);
                        //Log.w("LOOK HERE", "SAME DAY, ADDED");
                    }
                    else
                    {
                        ArrayList<Event> day2 = new ArrayList<>();
                        day2.add(e);
                        days.add(day2);
                        //Log.w("LOOK HERE", "NEW DAY, MAKING NEW ARRAY LIST." + days.size());
                    }
                }
            }
        }
        //Log.w("LOOK HERE", "Did Events! " + days.size() + days.get(0).size());

        sortEventsByDay();

        sortEventsByTime();
        onItemsLoadComplete();
    }

    void sortEventsByDay()
    {
        Collections.sort(days, new Comparator<ArrayList<Event>>()
        {
            @Override
            public int compare(ArrayList<Event> o1, ArrayList<Event> o2)
            {
                if(getDateFromString(o1.get(0).getDate()) != null && getDateFromString(o1.get(0).getDate()) != null)
                {
                    return getDateFromString(o1.get(0).getDate()).compareTo(getDateFromString(o2.get(0).getDate()));
                }

                return 0;
            }
        });
    }

    private Date getDateFromString(String str)
    {
        DateFormat format = new SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH);
        try
        {
            Date date = format.parse(str);
            return date;
        } catch (ParseException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    void sortEventsByTime()
    {
        for(int i = 0; i < days.size(); i++)
        {
            Collections.sort(days.get(i), new Comparator<Event>()
            {
                @Override
                public int compare(Event o1, Event o2)
                {
                    try {
                        return new SimpleDateFormat("hh:mm a").parse(getStartTime(o1.getTime())).compareTo(new SimpleDateFormat("hh:mm a").parse(getStartTime(o2.getTime())));
                    } catch (ParseException e) {
                        return 0;
                    }
                }
            });
        }
    }

    private String getStartTime(String time)
    {
        String[] out = time.split(" to");

        String res = out[0];

        return res.replace("From ", "");
    }

    void onItemsLoadComplete() {
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