package com.google.firebase.quickstart.database.models;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.List;

// [START blog_user_class]
@IgnoreExtraProperties
public class User {

    public String username;
    public String email;
    public String nickname;
    public String birthday;
    public List<String> hobbies;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String username, String email) {
        this.username = username;
        this.email = email;
    }


}
// [END blog_user_class]
