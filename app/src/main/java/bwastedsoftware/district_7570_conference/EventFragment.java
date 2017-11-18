package bwastedsoftware.district_7570_conference;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;




public class EventFragment extends Fragment implements View.OnClickListener {

    FloatingActionButton rsvp;
    private Event mEvent;
    private Context mContext;
    private String user_id;
    protected View mView;
    private FirebaseAuth mAuth;
    private DatabaseReference nDatabase;
    Boolean Current, Over;

    public EventFragment() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_event, container, false);

        mAuth = FirebaseAuth.getInstance();
        nDatabase = FirebaseDatabase.getInstance().getReference();

        rsvp = (FloatingActionButton) mView.findViewById(R.id.eventView_attendingButton);
        rsvp.setOnClickListener(this);
        Over = mEvent.isOver();
        Current = mEvent.isCurrent();
        mEvent.getCalendarStartTime();
        Toast.makeText(getContext(), Over.toString() + " " + Current.toString(), Toast.LENGTH_LONG).show();



        if(Over){
            askForRating();
        }
        return mView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        //this.mView = view;
        updateEventDetails();
    }

    public void passEvent(Context context, Event event) {
        mContext = context;
        mEvent = event;

    }

    private void updateEventDetails()
    {
        TextView mTitleView = (TextView) mView.findViewById(R.id.eventView_titleText);
        TextView mLocationView = (TextView) mView.findViewById(R.id.eventView_locationText);
        TextView mTimeView = (TextView) mView.findViewById(R.id.eventView_timeText);

        //add speakers code here
        ImageView sImageView = (ImageView) mView.findViewById(R.id.speakerLayout_avatarView);
        TextView sSpeakerName = (TextView) mView.findViewById(R.id.speakerLayout_textViewContainer_titleText);
        TextView sSpeakerBio = (TextView) mView.findViewById(R.id.speakerLayout_textViewContainer_subTitleText);

        Picasso.with(mContext).load(mEvent.getSpeaker().getPhotoURL()).fit().centerCrop().transform(new PicassoCircleTransform()).placeholder(R.drawable.ic_account_circle_black_24dp).into(sImageView);
        sSpeakerName.setText(mEvent.getSpeaker().getName());
        sSpeakerBio.setText(mEvent.getSpeaker().getBio());


        TextView mBodyView = (TextView) mView.findViewById(R.id.eventView_detailsBody);

        mTitleView.setText(mEvent.getTitle());
        mLocationView.setText(mEvent.getLocation());
        mTimeView.setText(mEvent.getDate() + ", " + mEvent.getTime().replace("F", "f"));
        mBodyView.setText(mEvent.getDetails());

        SimpleDateFormat formatter = new SimpleDateFormat("MMM dd, yyyy");
        Date current = Calendar.getInstance().getTime();
        Date date = null;
        try {
            date = (Date) formatter.parse(mEvent.getDate().toString().trim());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        long eventDate = date.getTime();
        long currentDate = current.getTime();

    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.eventView_attendingButton){

            //add to their calendar
            addEventToCalendar();

            user_id = mAuth.getCurrentUser().getUid();
            FirebaseDatabase mydb = FirebaseDatabase.getInstance();
            DatabaseReference mDatabase = mydb.getReference().child("Users").child(user_id);

            String key = nDatabase.child("Speakers").push().getKey();

            Map<String, Object> eventValues = mEvent.toMap();

            Map<String, Object> childUpdates = new HashMap<>();
            childUpdates.put("/userEvents/" + key, eventValues);
            //childUpdates.put("/user-posts/" + title + "/" + key, postValues);

            mDatabase.updateChildren(childUpdates);


        } else {

        }
    }

    private void addEventToCalendar()
    {
        //special thanks to https://code.tutsplus.com/tutorials/android-essentials-adding-events-to-the-users-calendar--mobile-8363 !!
        Intent calIntent = new Intent(Intent.ACTION_EDIT, CalendarContract.Events.CONTENT_URI);
        calIntent.setType("vnd.android.cursor.item/event");
        calIntent.putExtra(CalendarContract.Events.TITLE, mEvent.getTitle());
        calIntent.putExtra(CalendarContract.Events.EVENT_LOCATION, mEvent.getLocation());
        calIntent.putExtra(CalendarContract.Events.DESCRIPTION, mEvent.getDetails() + " SPEAKING: " + mEvent.getSpeakerString());

        Calendar calendar = mEvent.getCalendarStartTime();
        calIntent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME,
                calendar.getTimeInMillis());
        DateFormat endtoMilis = new SimpleDateFormat("HH:MM a", Locale.ENGLISH);
        Calendar c2 = Calendar.getInstance();
        try
        {
            c2.setTime(endtoMilis.parse(mEvent.getEndTime()));
        } catch (ParseException e)
        {
            e.printStackTrace();
        }
        calIntent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME,
                    c2.getTimeInMillis());

        startActivity(calIntent);
    }

    private void askForRating(){
              final Dialog rateDialog = new Dialog(getContext(), R.style.FullHeightDialog);
                rateDialog.setContentView(R.layout.rate_dialog);
                rateDialog.setCancelable(true);
                final RatingBar ratingBar = (RatingBar)rateDialog.findViewById(R.id.dialog_ratingbar);
                //ratingBar.setRating(userRankValue);

                TextView text = (TextView) rateDialog.findViewById(R.id.rank_dialog_text1);
                text.setText("Please rate this event.");

                Button updateButton = (Button) rateDialog.findViewById(R.id.rank_dialog_button);
                updateButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        rateDialog.dismiss();
                       Toast.makeText(getContext(), "Your rating of " + ratingBar.getRating() + " stars has been recorded.", Toast.LENGTH_LONG).show();
                    }
                });
                //now that the dialog is set up, it's time to show it
                rateDialog.show();
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
                    ((HomePage)getActivity()).getSupportActionBar().setTitle("Schedule");
                    ((HomePage)getActivity()).toolbarBackground(true);
                    return true;

                }

                return false;
            }
        });
    }

}