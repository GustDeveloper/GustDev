package com.google.firebase.quickstart.database.models;

import com.google.firebase.database.Exclude;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Profile {
    public String uid;
    public String email;
    public String username;
    public String image;
    public String nickname;
    public String birthday;
    public String location;
    public String phone;
    public String description;
    public List<String> hobbies;

    public Profile(){
        // Default constructor required for calls to DataSnapshot.getValue(Profile.class)
    }

    public Profile(String uid, String username, String email) {
        this.uid = uid;
        this.username = username;
        this.email = email;
        this.image = "";
        this.nickname = "";
        this.birthday = "";
        this.location = "";
        this.phone = "";
        this.hobbies = new ArrayList<>();
        this.phone = "";
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("image", image);
        result.put("nickname", nickname);
        result.put("birthday", birthday);
        result.put("hobbies", hobbies);
        result.put("image", image);
        result.put("description", description);
        result.put("phone", phone);
        result.put("location", location);
        return result;
    }
}
