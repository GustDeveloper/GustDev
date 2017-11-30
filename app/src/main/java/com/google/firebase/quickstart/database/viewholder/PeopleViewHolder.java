package com.google.firebase.quickstart.database.viewholder;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.quickstart.database.R;
import com.google.firebase.quickstart.database.models.Info;
import com.google.firebase.quickstart.database.models.Post;
import com.google.firebase.quickstart.database.models.Profile;

import de.hdodenhof.circleimageview.CircleImageView;

public class PeopleViewHolder extends RecyclerView.ViewHolder {

    
    public TextView authorView;
    //public ImageView picView;
    public CircleImageView picView;
    public TextView hobbyView;
    public Button msg;

    public PeopleViewHolder(View itemView) {
        super(itemView);
        Log.e("in PEOPLE LIST FRAG", "created vh");
        authorView = itemView.findViewById(R.id.people_name);
        picView = itemView.findViewById(R.id.info_photo);
        hobbyView = itemView.findViewById(R.id.people_hobby);
        msg = itemView.findViewById(R.id.msg);
    }

    public void bindToPeople(Profile profile, View.OnClickListener messageListener) {
        authorView.setText(profile.username);

        String image = profile.image;
        if (image != null && image.length() != 0) {
            byte[] decodedByteArray = Base64.decode(image, Base64.DEFAULT);
            Bitmap imageEncoded = BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.length);
            picView.setImageBitmap(imageEncoded);
        } else{
            picView.setImageResource(R.drawable.ic_action_account_circle_40);
        }

        msg.setOnClickListener(messageListener);
    }
}
