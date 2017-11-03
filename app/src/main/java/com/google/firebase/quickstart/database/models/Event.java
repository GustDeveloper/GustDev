package com.google.firebase.quickstart.database.models;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by vanne on 10/27/2017.
 */

public class Event {


    public String uid;
    public String author;
    public String title;
    public String time;
    public String location;
    //public LatLng latLng;

    public int starCount = 0;
    public Map<String, Boolean> stars = new HashMap<>();

    public Event() {
        // Default constructor required for calls to DataSnapshot.getValue(Post.class)
    }

    public Event(String uid, String time, String title, String body) {
        this.uid = uid;
        this.author = author;
        this.title = title;
        this.time = time;

    }

    // [START post_to_map]
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("author", author);
        result.put("title", title);
        result.put("starCount", starCount);
        result.put("stars", stars);

        return result;
    }
    // [END post_to_map]

}
