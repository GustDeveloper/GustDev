package com.google.firebase.quickstart.database.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.quickstart.database.ProfileActivity;
import com.google.firebase.quickstart.database.R;
import com.google.firebase.quickstart.database.models.Profile;
import com.google.firebase.quickstart.database.viewholder.PeopleViewHolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ParticipantListFragment extends Fragment {

    private static final String TAG = "ParticipantListFragment";
    // [START define_database_reference]
    private DatabaseReference mDatabase;
    // [END define_database_reference]

    private FirebaseRecyclerAdapter<Profile, PeopleViewHolder> mAdapter;
    private RecyclerView mRecycler;
    private LinearLayoutManager mManager;



    public ParticipantListFragment() {
    }

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_all_people, container, false);

        // [START create_database_reference]
        mDatabase = FirebaseDatabase.getInstance().getReference();
        // [END create_database_reference]
        mRecycler = rootView.findViewById(R.id.people_list);
        mRecycler.setHasFixedSize(true);


        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Set up Layout Manager, reverse layout
        mManager = new LinearLayoutManager(getActivity());
        //mManager.setReverseLayout(true);
        //mManager.setStackFromEnd(true);
        mRecycler.setLayoutManager(mManager);

        // Set up FirebaseRecyclerAdapter with the Query
        //Query peopleQuery = mDatabase.child("profiles").child(getUid());
        Query peopleQuery = mDatabase.child("profiles").limitToFirst(100);
        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Profile>()
                .setQuery(peopleQuery, Profile.class)
                .build();

        mAdapter = new FirebaseRecyclerAdapter<Profile, PeopleViewHolder>(options) {
            @Override
            public PeopleViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
                LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
                return new PeopleViewHolder(inflater.inflate(R.layout.people_contact, viewGroup, false));
            }


            @Override
            protected void onBindViewHolder(PeopleViewHolder viewHolder, int position, final Profile model) {
                final DatabaseReference peopleRef = getRef(position);
                // Set click listener for the whole post view
                final String infoKey = peopleRef.getKey();
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Toggle the toggle button.
                    }
                });

                viewHolder.bindToPeople(model, new View.OnClickListener() {
                    @Override
                    public void onClick(View starView) {
                        // find the user Uid
                        final String Uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        final DatabaseReference userhash = mDatabase.child("user-user");
                    }
                });
            }
        };
        mRecycler.setAdapter(mAdapter);
    }



    @Override
    public void onStart() {
        super.onStart();
        if (mAdapter != null) {
            mAdapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAdapter != null) {
            mAdapter.stopListening();
        }
    }


    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

}

