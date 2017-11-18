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

import java.util.List;

import co.lujun.androidtagview.TagContainerLayout;
import de.hdodenhof.circleimageview.CircleImageView;


public class PeopleListViewHolder extends RecyclerView.ViewHolder {
    public TextView nickname;
    public CircleImageView circleImageView;
    public TagContainerLayout tagContainerLayout;
    public CheckBox checkBox;
    public View peopleView;

    public PeopleListViewHolder(View peopleView){
        super(peopleView);
        this.peopleView = peopleView;
        nickname = peopleView.findViewById(R.id.nicknameTextView);
        circleImageView = peopleView.findViewById(R.id.list_profile_image);
        tagContainerLayout = peopleView.findViewById(R.id.list_tagContainerLayout);
        checkBox = peopleView.findViewById(R.id.list_checkbox_btn);
    }

    public void bindToPeopleListDialog (Profile profile, View.OnClickListener listListener ) {
        Log.e("List view hold", "Success");
        nickname.setText(profile.username);

        String image = profile.image;
        byte[] decodedByteArray = Base64.decode(image, Base64.DEFAULT);
        Bitmap imageEncoded = BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.length);
        circleImageView.setImageBitmap(imageEncoded);

        List<String> hobbies = profile.hobbies;
        tagContainerLayout.setTags(hobbies);
        checkBox.setOnClickListener(listListener);
    }
}
