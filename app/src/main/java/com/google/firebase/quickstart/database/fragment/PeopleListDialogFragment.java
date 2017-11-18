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
import android.view.Window;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.quickstart.database.R;
import com.google.firebase.quickstart.database.models.Profile;
import com.google.firebase.quickstart.database.viewholder.PeopleListViewHolder;

import java.util.Map;


public class PeopleListDialogFragment extends DialogFragment {

    private static final String TAG = "PeopleListDialogFragment";

    // [START define_database_reference]
    private DatabaseReference mDatabase;
    // [END define_database_reference]

    private FirebaseRecyclerAdapter<Profile, PeopleListViewHolder> mAdapter;
    private RecyclerView mRecyler;
    private LinearLayoutManager mManager;
    private Map<String, Boolean> participantsMap;

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
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        View rootView = inflater.inflate(R.layout.fragment_people_list_dialog, container, false);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mRecyler = rootView.findViewById(R.id.people_recyclerList);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //Obtain all list of profile when the fragment is created;
        mManager = new LinearLayoutManager(getActivity());
        mRecyler.setLayoutManager(mManager);

        final Query peopleQuery = mDatabase.child("profiles").limitToFirst(100);
        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Profile>().setQuery(peopleQuery, Profile.class).build();

        mAdapter = new FirebaseRecyclerAdapter<Profile, PeopleListViewHolder>(options) {
            @Override
            protected void onBindViewHolder(final PeopleListViewHolder holder, int position, Profile profile) {
                final DatabaseReference userRef = getRef(position);
                // not so sure what I should do here.
                holder.bindToPeopleListDialog(profile, new View.OnClickListener(){
                    @Override
                    public void onClick(View view) {
                        if (holder.checkBox.isChecked()) {
                            holder.checkBox.setChecked(false);
                            participantsMap.remove(userRef.toString());
                        } else {
                            holder.checkBox.setChecked(true);
                            participantsMap.put(userRef.toString(), true);
                        }
                    }
                });
            }
            @Override
            public PeopleListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                LayoutInflater inflater = LayoutInflater.from(parent.getContext());
                View peopleListViewHolder=  inflater.inflate(R.layout.people_list_contact, parent,false);
                return new PeopleListViewHolder(peopleListViewHolder);
            }
        };
    }


}
