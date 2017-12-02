package com.google.firebase.quickstart.database.fragment;


import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.quickstart.database.NewEventActivity;
import com.google.firebase.quickstart.database.ProfileActivity;
import com.google.firebase.quickstart.database.R;
import com.google.firebase.quickstart.database.models.Profile;
import com.google.firebase.quickstart.database.models.UtilToast;
import com.google.firebase.quickstart.database.viewholder.PeopleListViewHolder;

import java.util.HashMap;
import java.util.Map;


public class PeopleListFragment extends android.support.v4.app.Fragment {

    private static final String TAG = "PeopleListFrag";

    // [START define_database_reference]
    private DatabaseReference mDatabase;
    // [END define_database_reference]



    private FirebaseRecyclerAdapter<Profile, PeopleListViewHolder> mAdapter;
    private RecyclerView mRecycler;
    private LinearLayoutManager mManager;
    private Map<String, Boolean> participantsMap = new HashMap<>();
    PeopleListFragmentCallback peopleListFragmentCallback;

    public PeopleListFragment() {
        // Required empty public constructor
    }


    public interface PeopleListFragmentCallback{
        void invitePeopleToEvent(Map<String,Boolean> participantsMap);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            peopleListFragmentCallback = (PeopleListFragmentCallback) context;
        } catch(ClassCastException e) {
            throw new ClassCastException(context.toString() + "must implement Fragment1Callback");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_people_list_dialog,container,false);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mRecycler = rootView.findViewById(R.id.people_recyclerList);
        mRecycler.setHasFixedSize(true);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //Obtain all list of profile when the fragment is created;
        mManager = new LinearLayoutManager(getActivity());
        mRecycler.setLayoutManager(mManager);

        final Query peopleQuery = mDatabase.child("profiles").limitToFirst(20);
        Log.e(TAG, peopleQuery.toString());


        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Profile>()
                .setQuery(peopleQuery, Profile.class).build();

        mAdapter = new FirebaseRecyclerAdapter<Profile, PeopleListViewHolder>(options) {
            //Create view holder;
            @Override
            public PeopleListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                LayoutInflater inflater = LayoutInflater.from(parent.getContext());
                return new PeopleListViewHolder(inflater.inflate(R.layout.people_list_contact, parent,false));
            }


            @Override
            protected void onBindViewHolder(final PeopleListViewHolder holder, final int position, final Profile profile) {
                final DatabaseReference userRef = getRef(position);

                holder.itemView.setOnLongClickListener(new View.OnLongClickListener(){
                    @Override
                    public boolean onLongClick(View view){
                        peopleListFragmentCallback.invitePeopleToEvent(participantsMap);
                        return true;
                    }
                });

                holder.itemView.setOnClickListener((view)-> {
                    Intent profileActivity = new Intent(getContext(),ProfileActivity.class);
                    profileActivity.putExtra("intentUserID", userRef.getKey().toString());
                    startActivity(profileActivity);
                });

                holder.bindToPeopleList(profile, new View.OnClickListener(){
                    @Override
                    public void onClick(View view) {
                        if (profile.isCheck) {
                            profile.isCheck = false;
                            mAdapter.notifyDataSetChanged();
                            participantsMap.remove(userRef.getKey().toString());
                        } else {
                            profile.isCheck = true;
                            mAdapter.notifyItemChanged(position);
                            participantsMap.put(userRef.getKey().toString(), false);
                        }

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
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAdapter != null) {
            mAdapter.stopListening();
        }
    }

    public String  getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }
}
