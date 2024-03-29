package bwastedsoftware.district_7570_conference;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;


public class CreateEventFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    EditText etTitle, etLocation, etDetails, etDate;
    TextView strt, end;
    TimePicker timePicker;
    DatePicker datePicker;
    Spinner speakerPick;
    Button saveEvent;
    ArrayList<Speaker> speakers;
    ArrayList<String> speakerNames;
    ArrayAdapter<String> sAdapter;
    String cName, cBio, cWebPage, cPhoto;
    Speaker chosenOne = new Speaker(cName, cBio, cWebPage, cPhoto);
    Boolean isAdmin;
    private DatabaseReference mDatabase;
    private DatabaseReference mRef;
    private int mHour;
    private int mMinute;

    public CreateEventFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        ((HomePageActivity)getActivity()).getSupportActionBar().setTitle("Create New Event");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_create_event, container, false);
        Bundle args = getArguments();
        isAdmin = args.getBoolean("IS_ADMIN");
        //Initialize the large amount of things in this fragment
        strt = (TextView) view.findViewById(R.id.pick_startTime);
        end = (TextView) view.findViewById(R.id.pick_EndTime);
        timePicker = (TimePicker) view.findViewById(R.id.timePicker);
        datePicker = (DatePicker) view.findViewById(R.id.datePicker);
        saveEvent = (Button) view.findViewById(R.id.btn_Save_Event);
        etTitle = (EditText) view.findViewById(R.id.edit_);
        etLocation = (EditText) view.findViewById(R.id.edit_Location);

        etDate = (EditText) view.findViewById(R.id.edit_Date);

        etDetails = (EditText) view.findViewById(R.id.edit_Details);
        speakerPick = (Spinner) view.findViewById(R.id.spinner_Speakers);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mRef = FirebaseDatabase.getInstance().getReference().child("Speakers");

        speakers = new ArrayList<Speaker>();
        speakerNames = new ArrayList<String>();

        //Set click listeners for all these items
        speakerPick.setOnItemSelectedListener(this);
        saveEvent.setOnClickListener(this);
        strt.setOnClickListener(this);
        end.setOnClickListener(this);
        etDate.setOnClickListener(this);

        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot childrenSnapshot : dataSnapshot.getChildren()){
                    Speaker newSpeaker = childrenSnapshot.getValue(Speaker.class);

                    sAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.select_dialog_item, speakerNames);
                    sAdapter.setDropDownViewResource(android.R.layout.select_dialog_item);
                    speakers.add(new Speaker(newSpeaker.getName(), newSpeaker.getTitle(), newSpeaker.getWebpage(), newSpeaker.getPhotoURL()));
                    speakerNames.add(newSpeaker.getName());
                }
                speakerPick.setAdapter(sAdapter);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                    // Failed to read value
                Log.w("FIREBASE", "Failed to read value.", error.toException());
            }
        });



        return view;
    }

    //This is like a click listener for the spinner
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        int index = speakerNames.indexOf(sAdapter.getItem(position));
        chosenOne = speakers.get(index);
        //Toast.makeText(view.getContext(), "Selected Speaker: " + chosenOne.getName() + chosenOne.getTitle(), Toast.LENGTH_LONG).show();
    }
    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        //TODO Error catch
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_Save_Event) {
            if(checkSpaces()){
                uploadEvent();
            }

        } else if (v.getId() == R.id.pick_startTime) { //let user pick start time
            TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(),
                    new TimePickerDialog.OnTimeSetListener() {

                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay,
                                              int minute) {
                            String am_pm = (hourOfDay < 12) ? "AM" : "PM";
                            if(hourOfDay == 00){

                                strt.setText(new StringBuilder().append("12").append(":").append(pad(minute)) + " " + am_pm);
                            }else if(hourOfDay > 12) {
                                strt.setText(new StringBuilder().append((hourOfDay-12)).append(":").append(pad(minute)) + " " + am_pm);
                            } else {
                                strt.setText(new StringBuilder().append((hourOfDay)).append(":").append(pad(minute)) + " " + am_pm);
                            }
                        }
                    }, mHour, mMinute, false);
            timePickerDialog.show();
        } else if (v.getId() == R.id.pick_EndTime) {
            TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(),
                    new TimePickerDialog.OnTimeSetListener() {

                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay,
                                              int minute) {
                            String am_pm = (hourOfDay < 12) ? "AM" : "PM";
                            if(hourOfDay == 00){
                                end.setText(new StringBuilder().append("12").append(":").append(pad(minute)) + " " + am_pm);
                            }else {
                                if(hourOfDay > 12) {
                                    end.setText(new StringBuilder().append((hourOfDay - 12)).append(":").append(pad(minute)) + " " + am_pm);
                                } else {
                                    end.setText(new StringBuilder().append((hourOfDay)).append(":").append(pad(minute)) + " " + am_pm);
                                }
                            }
                        }
                    }, mHour, mMinute, false);
            timePickerDialog.show();
        } else if(v.getId() == R.id.edit_Date){
            final Calendar calendar = Calendar.getInstance();
            int yy = calendar.get(Calendar.YEAR);
            int mm = calendar.get(Calendar.MONTH);
            int dd = calendar.get(Calendar.DAY_OF_MONTH);
            DatePickerDialog datePicker = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                    //Change the numerical value generated by android into its full month name
                    String letterMonth = null;
                    switch (monthOfYear){
                        case 0:
                            letterMonth = "January";
                            break;
                        case 1:
                            letterMonth = "February";
                            break;
                        case 2:
                            letterMonth = "March";
                            break;
                        case 3:
                            letterMonth = "April";
                            break;
                        case 4:
                            letterMonth = "May";
                            break;
                        case 5:
                            letterMonth = "June";
                            break;
                        case 6:
                            letterMonth = "July";
                            break;
                        case 7:
                            letterMonth = "August";
                            break;
                        case 8:
                            letterMonth = "September";
                            break;
                        case 9:
                            letterMonth = "October";
                            break;
                        case 10:
                            letterMonth = "November";
                            break;
                        case 11:
                            letterMonth = "December";
                            break;
                    }
                    etDate.setText(letterMonth + " " + String.valueOf(dayOfMonth)+ ", " + String.valueOf(year));
                }
            }, yy, mm, dd);
            datePicker.show();
        }
    }

    private void goToSchedulePage(String name)
    {
        getActivity().getSupportFragmentManager().popBackStack();
        Bundle bundle = new Bundle();
        bundle.putBoolean("IS_MY_SCHEDULE", false);
        bundle.putBoolean("IS_ADMIN", isAdmin);
        ScheduleFragment mFrag = new ScheduleFragment();
        mFrag.setArguments(bundle);
        FragmentTransaction t = this.getFragmentManager().beginTransaction();
        t.replace(R.id.main_container, mFrag);
        t.commit();

        Snackbar.make(getActivity().findViewById(android.R.id.content), "Event Created: " + name, Snackbar.LENGTH_LONG).show();
    }

//If a number is less than 10, give it padding of a zero.
private static String pad(int c) {
    if (c >= 10) {
        return String.valueOf(c);
    } else {
        return "0" + String.valueOf(c);
    }
}

//this will enable using the back button to pop the stack, which will go to previous fragment instead of the login screen.
    @Override
    public void onResume() {

        super.onResume();

        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK){

                    getActivity().getSupportFragmentManager().popBackStack();

                    return true;

                }

                return false;
            }
        });
    }

    private void uploadEvent(){

        //create a new User using information put in by the *ahem* user.

        String Title = etTitle.getText().toString().trim();
        String Location = etLocation.getText().toString().trim();
        String timeToString = ("From " + strt.getText() + " to " + end.getText());
        String Date = etDate.getText().toString().trim();
        String Details = etDetails.getText().toString().trim();

        String key = mDatabase.child("Speakers").push().getKey();

        Event event = new Event(Title, Location, Date, timeToString, Details, chosenOne, 0, 0);

        Map<String, Object> eventValues = event.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/Events/" + key, eventValues);
        //childUpdates.put("/user-posts/" + title + "/" + key, postValues);

        mDatabase.updateChildren(childUpdates);

        goToSchedulePage(etTitle.getText().toString());
    }

    private boolean checkSpaces(){

        if(etTitle.getText().length()==0) {
            Toast.makeText(getContext(), "Please enter a title for the event", Toast.LENGTH_SHORT).show();
            return false;}

        if(etLocation.getText().length()==0) {
            Toast.makeText(getContext(), "Please enter a location for the event", Toast.LENGTH_SHORT).show();
            return false;}

        if (etDate.getText().length() == 0) {
            Toast.makeText(getContext(), "Please choose a date for the event", Toast.LENGTH_SHORT).show();
            return false;}
        if(strt.getText().equals("Start Time")){
            Toast.makeText(getContext(), "Please choose a start time", Toast.LENGTH_SHORT).show();
            return false;}
        if(end.getText().equals("End Time")){
            Toast.makeText(getContext(), "Please choose an end time", Toast.LENGTH_SHORT).show();
            return false;}
        if(chosenOne.getName().length() == 0){
            Toast.makeText(getContext(), "Please choose a speaker", Toast.LENGTH_SHORT).show();
            return false;}
        if(etDetails.getText().length() == 0){
            Toast.makeText(getContext(), "Please enter some details about the event", Toast.LENGTH_SHORT).show();
            return false;}
        return true;
    }
}
