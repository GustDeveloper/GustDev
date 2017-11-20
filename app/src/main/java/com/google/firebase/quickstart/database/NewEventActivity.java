package com.google.firebase.quickstart.database;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.google.firebase.quickstart.database.fragment.EventFragment;
import com.google.firebase.quickstart.database.fragment.PeopleListFragment;
import com.google.firebase.quickstart.database.models.Event;

import java.util.Map;

public class NewEventActivity extends AppCompatActivity implements EventFragment.EventFragmentCallback,
                                                                      PeopleListFragment.PeopleListFragmentCallback {

    private static final String TAG = "firebase.quickstart.database.NewEventActivity";
    private FragmentPagerAdapter mPagerAdapter;
    private ViewPager mViewPager;
    public Event event;
    FloatingActionButton saveFab;
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
        /*

        saveFab = (FloatingActionButton) findViewById(R.id.editFab);
        saveFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        */

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
    }

    @Override
    public void sendEventToServer(Event event) {
        //send it to server;
        this.event = event;
        //PeopleListFragment peopleListFragment = new PeopleListFragment();
    }

    @Override
    public void sendPeopleToEvent(Map<String, Boolean> participantsMap) {
        this.event.participants = participantsMap;
    }
}
