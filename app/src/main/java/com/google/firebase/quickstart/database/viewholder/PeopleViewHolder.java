package com.google.firebase.quickstart.database.viewholder;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.quickstart.database.R;
import com.google.firebase.quickstart.database.models.Info;
import com.google.firebase.quickstart.database.models.Post;
import com.google.firebase.quickstart.database.models.Profile;

public class PeopleViewHolder extends RecyclerView.ViewHolder {

    
    public TextView authorView;
    public ImageView picView;
    public TextView hobbyView;
    public Button msg;

    public PeopleViewHolder(View itemView) {
        super(itemView);
        authorView = itemView.findViewById(R.id.people_name);
        picView = itemView.findViewById(R.id.info_photo);
        hobbyView = itemView.findViewById(R.id.people_hobby);
        msg = itemView.findViewById(R.id.msg);
    }

    public void bindToPeople(Profile profile, View.OnClickListener messageListener) {
        Log.d("ViewHold","Sucess");
        authorView.setText(profile.nickname);
        msg.setOnClickListener(messageListener);
    }
}
