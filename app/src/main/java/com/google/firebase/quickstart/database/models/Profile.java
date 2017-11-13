package com.google.firebase.quickstart.database.models;

import com.google.firebase.database.Exclude;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Profile {
    String uid;
    String email;
    String username;
    String image;
    String nickname;
    String birthday;
    String location;
    String phone;
    List<String> hobbies;

    public Profile(){
        // Default constructor required for calls to DataSnapshot.getValue(Post.class)
    }

    public Profile(String uid, String username, String email) {
        this.uid = uid;
        this.username = username;
        this.email = email;
        this.image = "";
        this.nickname = "";
        this.birthday = "";
        this.location = "";
        this.hobbies = new ArrayList<>(Arrays.asList("Add Hobby"));
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("image", image);
        result.put("nickname", nickname);
        result.put("birthday", birthday);
        result.put("hobbies", hobbies);
        result.put("image", image);
        return result;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }
    public void setLocation(String location) {
        this.location = location;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }
}
