package com.google.firebase.quickstart.database.fragment;

import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.quickstart.database.R;
import com.google.firebase.quickstart.database.models.Profile;


public class PeopleListDialogFragment extends DialogFragment {

    private static final String TAG = "PeopleListDialogFragment";

    // [START define_database_reference]
    private DatabaseReference mDatabase;
    // [END define_database_reference]

    private FirebaseRecyclerAdapter<Profile, PeopleListViewHolder> mAdapter;
    private RecyclerView mRecyler;
    private LinearLayoutManager mManager;

    public PeopleListDialogFragment() {
        // Required empty public constructor

    }


    /*
    public static PeopleListDialogFragment newInstance(String param1, String param2) {
        PeopleListDialogFragment fragment = new PeopleListDialogFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }
    */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_people_list_dialog, container, false);
        mDatabase  = FirebaseDatabase.getInstance().getReference();
        mRecyler = rootView.find


        return rootView;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {




    }
}
