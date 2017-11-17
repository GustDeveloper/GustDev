package com.google.firebase.quickstart.database.fragment;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

public class MyTopEventsFragment extends PostListFragment {

    public MyTopEventsFragment() {}

    @Override
    public Query getQuery(DatabaseReference databaseReference) {
        // [START my_top_posts_query]
        // My top posts by number of stars
        String myUserId = getUid();
        Query myTopEventsQuery = databaseReference.child("user-events").child(myUserId)
                .orderByChild("starCount");
        // [END my_top_posts_query]

        return myTopEventsQuery;
    }
}
