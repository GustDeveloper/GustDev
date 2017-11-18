package com.google.firebase.quickstart.database.fragment;

import android.app.DatePickerDialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.quickstart.database.R;

import java.util.Calendar;

public class EventFragment extends Fragment implements View.OnClickListener {

    // [START define_database_reference]
    private DatabaseReference mDatabase;
    // [END define_database_reference]

    Button btnDatePicker, btnTimePicker;
    EditText txtDate, txtTime;
    private int mYear, mMonth, mDay, mHour, mMinute;
    private Button inviteBtn;


    //private OnFragmentInteractionListener mListener;

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
        inviteBtn = rootView.findViewById(R.id.inviteBtn);
        inviteBtn.setOnClickListener(this);

        // Datebase Reference
        mDatabase = FirebaseDatabase.getInstance().getReference();
        return rootView;
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

        if (view == inviteBtn) {
            PeopleListDialogFragment peopleListDialogFragment = new PeopleListDialogFragment();
            peopleListDialogFragment.show(getFragmentManager(),"friends");
        }
    }
}

