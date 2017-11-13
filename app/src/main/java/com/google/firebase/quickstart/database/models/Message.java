package com.google.firebase.quickstart.database.models;

/**
 * Created by tangjinhao on 11/11/17.
 */

public class Message {
    public String sender;
    public String receiver;
    public String msg;
    public String timeStamp;

    public Message() {
        // Default constructor required for calls to DataSnapshot.getValue(Comment.class)
    }

    public Message(String sender, String receiver, String msg, String timeStamp) {
        this.sender = sender;
        this.receiver = receiver;
        this.msg = msg;
        this.timeStamp = timeStamp;
    }
}
