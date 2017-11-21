package com.google.firebase.quickstart.database;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.quickstart.database.fragment.EventFragment;
import com.google.firebase.quickstart.database.fragment.PeopleListFragment;
import com.google.firebase.quickstart.database.models.Event;
import com.google.firebase.quickstart.database.models.UtilToast;
import java.util.Map;

public class NewEventActivity extends BaseActivity implements EventFragment.EventFragmentCallback,
                                                                      PeopleListFragment.PeopleListFragmentCallback {

    /*UI Elements*/
    private static final String TAG = "NewEventActivity";
    private FragmentPagerAdapter mPagerAdapter;
    private ViewPager mViewPager;

    /*Databse Reference*/
    private Event event;
    private DatabaseReference mDatabase;
    private String key;

    FragmentManager mFragmentManager = getSupportFragmentManager();
    private static Fragment[] mFragments = new Fragment[] {
            new EventFragment(),
            new PeopleListFragment()
    };

    private static String[] mFragmentNames = new String[] {
            //getString(R.string.fragment_event),
            //getString(R.string.fragment_part),
            "Event Detail",
            "Invitation"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);
        setTitle("Create An Event");

        mPagerAdapter = new FragmentPagerAdapter(mFragmentManager) {

            /*
            private final Fragment[] mFragments = new Fragment[] {
                    new EventFragment(),
                    new PeopleListFragment()
            };
            */

            private final Fragment[] mFragments = NewEventActivity.mFragments;

            /*
            private final String[] mFragmentNames = new String[] {
                    getString(R.string.fragment_event),
                    getString(R.string.fragment_part),
            };
            */

            private final String[] mFragmentNames = NewEventActivity.mFragmentNames;

            @Override
            public Fragment getItem(int position) {
                return mFragments[position];
            }
            @Override
            public int getCount() {
                return mFragments.length;
            }
            @Override
            public CharSequence getPageTitle(int position) {
                return mFragmentNames[position];
            }
        };

        // Set up the ViewPager with the sections adapter.
        mViewPager = findViewById(R.id.eventContainer);
        mViewPager.setAdapter(mPagerAdapter);
        TabLayout tabLayout = findViewById(R.id.eventTabs);
        tabLayout.setupWithViewPager(mViewPager);

        //Database reference
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public void sendEventToServer(Event newEvent) {
        //send it to server;
        this.event = newEvent;
        if (key == null || key.length() == 0) {
            key = mDatabase.child("events").push().getKey();
        }

        mDatabase.child("events").child(key).setValue(newEvent, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError != null) {
                    UtilToast.showToast(getApplicationContext(), databaseError.getMessage());
                } else {
                    UtilToast.showToast(getApplicationContext(), "An event is created");
                    Log.e(TAG, "calling event fragment");
                }
            }
        });
    }

    @Override
    public void invitePeopleToEvent(Map<String, Boolean> participantsMap) {
        if (this.event == null || this.key == null || this.key.length() == 0) {
            UtilToast.showToast(getApplicationContext(), "You have not set up an event yet");
        } else {
            this.event.participants = participantsMap;
            mDatabase.child("events").child(key).child("participants").setValue(participantsMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError != null) {
                        UtilToast.showToast(getApplicationContext(), databaseError.getMessage());
                    } else {
                        UtilToast.showToast(getApplicationContext(),"Invitations have been sent ");
                    }
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        key = "";
    }
}
