package com.google.firebase.quickstart.database.viewholder;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.google.firebase.quickstart.database.R;
import com.google.firebase.quickstart.database.models.Profile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import co.lujun.androidtagview.TagContainerLayout;
import de.hdodenhof.circleimageview.CircleImageView;


public class PeopleListViewHolder extends RecyclerView.ViewHolder{
    public TextView nickname;
    public CircleImageView circleImageView;
    public TagContainerLayout tagContainerLayout;
    public CheckBox checkBox;
    public View peopleView;
    public List<String> hobbies;
    final static String TAG = "In ListView Holder";

    public PeopleListViewHolder(View peopleView){
        super(peopleView);
        this.peopleView = peopleView;
        nickname = peopleView.findViewById(R.id.nicknameTextView);
        circleImageView = peopleView.findViewById(R.id.list_profile_image);
        //tagContainerLayout = peopleView.findViewById(R.id.list_tagContainerLayout);
        checkBox = peopleView.findViewById(R.id.list_checkbox_btn);
    }

    public void bindToPeopleList (Profile profile, View.OnClickListener listListener) {
        nickname.setText(profile.username);

        String image = profile.image;
        if (image != null && image.length() != 0) {
            byte[] decodedByteArray = Base64.decode(image, Base64.DEFAULT);
            Bitmap imageEncoded = BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.length);
            circleImageView.setImageBitmap(imageEncoded);
        } else{
            circleImageView.setImageResource(R.drawable.ic_action_account_circle_40);
        }


        /*
        if (profile.hobbies != null && profile.hobbies instanceof List) {
            hobbies = profile.hobbies;
            hobbies.removeAll(Collections.singleton(null));
         } else {
            hobbies = new ArrayList<>();
        }
        */

        //tagContainerLayout.setTags(hobbies);
        checkBox.setOnClickListener(listListener);
        checkBox.setChecked(profile.isCheck);

    }
}
