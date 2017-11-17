package com.google.firebase.quickstart.database.models;

import com.google.firebase.database.Exclude;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Event {

    public String eid;
    public String uid;
    public String author;
    public String title;
    public String time;
    public String location;
    public String description;
    public String status;
    public List<String> participants;
    public List<String> tags;
    public int starCount = 0;
    public Map<String, Boolean> stars = new HashMap<>();

    public Event() {
        // Default constructor required for calls to DataSnapshot.getValue(Post.class)
    }

    public Event(String uid, String eid) {
        this.uid = uid;
        this.author = "";
        this.title = "";
        this.time = "";
        this.location = "";
        this.description = "";
        this.status = "Upcoming";
        this.tags = new ArrayList<>();
        this.participants = new ArrayList<>();
        this.starCount = 0;
//        this.stars = new HashMap<>();

    }

    public Event(String uid, String eid, String author, String title, String location, String description, List<String> tags) {
        this.uid = uid;
        this.author = author;
        this.title = title;
        this.time = time;
        this.location = location;
        this.description = description;
        this.tags = tags;
        this.status = "Upcoming";
        this.starCount = 0;
        this.stars = new HashMap<>();
    }

    // [START post_to_map]
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("author", author);
        result.put("title", title);
        result.put("time", time);
        result.put("location", location);
        result.put("description", description);
        result.put("tags",tags);
        result.put("eid", eid);
        result.put("status", status);
        result.put("starCount", starCount);
        result.put("stars", stars);
        return result;
    }
    // [END post_to_map]

}