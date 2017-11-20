package com.google.firebase.quickstart.database.fragment;

import android.app.DatePickerDialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.quickstart.database.R;
import com.google.firebase.quickstart.database.models.Event;
import com.google.firebase.quickstart.database.models.UtilToast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class EventFragment extends Fragment implements View.OnClickListener {

    // [START define_database_reference]
    private DatabaseReference mDatabase;
    private DatabaseReference mProfileRef;

    // [END define_database_reference]

    Button btnDatePicker, btnTimePicker;
    EditText txtDate, txtTime;
    private int mYear, mMonth, mDay, mHour, mMinute;
    private EditText phoneEditText, locationEditText,nicknameEditText,emaiEditText;
    private FloatingActionButton saveFab;
    EventFragmentCallback eventFragmentCallback;
    String key = "";

    private EventFragmentCallback mListener;

    public EventFragment() {
        // Required empty public constructor
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView =  inflater.inflate(R.layout.fragment_event, container, false);
        btnDatePicker= rootView.findViewById(R.id.datePickerBtn);
        btnTimePicker= rootView.findViewById(R.id.timePickerBtn);
        btnDatePicker.setOnClickListener(this);
        btnTimePicker.setOnClickListener(this);
        txtDate =  rootView.findViewById(R.id.dateEditText);
        txtTime =  rootView.findViewById(R.id.timeEditText);
        phoneEditText = rootView.findViewById(R.id.phoneEditText);
        locationEditText = rootView.findViewById(R.id.locationEditText);
        nicknameEditText = rootView.findViewById(R.id.nicknameEditText);
        emaiEditText = rootView.findViewById(R.id.emailEditText);


        saveFab = rootView.findViewById(R.id.saveFab);
        saveFab.setOnClickListener(this);

        // Datebase Reference
        mDatabase = FirebaseDatabase.getInstance().getReference();
        return rootView;
    }

    public interface EventFragmentCallback {
        void sendEventToServer(Event event);
    }


    @Override
    public void onStart() {
        super.onStart();
        //set data
        nicknameEditText.setText(getDisplayName());
        emaiEditText.setText(getEmail());
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            eventFragmentCallback = (EventFragmentCallback) context;
        } catch(ClassCastException e) {
            throw new ClassCastException(context.toString() + "must implement Fragment1Callback");
        }
    }

    @Override
    public void onClick(View view) {
        if (view == btnDatePicker){
            final Calendar calendar = Calendar.getInstance();
            mYear = calendar.get(Calendar.YEAR);
            mMonth = calendar.get(Calendar.MONTH);
            mDay = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(),
                    new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
                            txtDate.setText((monthOfYear + 1) + "-" + dayOfMonth +  "-" + year);
                        }
                    }, mYear, mMonth, mDay);
            datePickerDialog.show();
        }

        if (view == btnTimePicker) {
            final Calendar calendar = Calendar.getInstance();
            mHour = calendar.get(Calendar.HOUR_OF_DAY);
            mMinute = calendar.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog;
            timePickerDialog = new TimePickerDialog(getActivity(),
                    new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
                            txtTime.setText(hourOfDay + ":" + minute);
                        }
                    }, mHour,mMinute,false);
            timePickerDialog.show();
        }

        if (view == saveFab) {
            createEventInServer();
        }
    }


    public Event createEvent() {
        Event event = new Event(getUid());
        event.author = getDisplayName();
        event.participants = new HashMap<>();
        event.participants.put("Gary", true);
        event.tags = new HashMap<>();
        event.tags.put("Programmming", true);

        if (txtTime != null) {
            event.time = txtTime.getText().toString();
        }

        if (txtDate != null) {
            event.date = txtDate.getText().toString();
        }
        event.description = "An interesting event";
        return event;
    }

    public void createEventInServer() {
        Event newEvent = createEvent();

        if (key == null || key.length() == 0) {
            key = mDatabase.child("events").push().getKey();
        }

        mDatabase.child("events").child(key).setValue(newEvent, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError != null) {
                    UtilToast.showToast(getContext(), databaseError.getMessage());
                } else {
                    UtilToast.showToast(getContext(), "An event is created");
                    passEventToActivity();
                }
            }
        });

    }

    public void passEventToActivity() {
        eventFragmentCallback.sendEventToServer(createEvent());
    }


    public String  getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public String getEmail() {
        return FirebaseAuth.getInstance().getCurrentUser().getEmail();
    }
    public String getDisplayName() {
        return FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
    }


}

