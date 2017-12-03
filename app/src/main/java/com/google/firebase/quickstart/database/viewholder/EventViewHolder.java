package com.google.firebase.quickstart.database.viewholder;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.quickstart.database.R;
import com.google.firebase.quickstart.database.models.Event;
import com.google.firebase.quickstart.database.models.Profile;

import de.hdodenhof.circleimageview.CircleImageView;

public class EventViewHolder extends RecyclerView.ViewHolder {
    public ImageView photoView;
    public TextView titleView;
    public CircleImageView author;
    public TextView authorView;
    public ImageView starView;
    public TextView numStarsView;
    public TextView bodyView;
    private DatabaseReference mDatabase;

    public EventViewHolder(View itemView) {
        super(itemView);
        author = itemView.findViewById(R.id.event_author_photo);
        photoView = itemView.findViewById(R.id.event_photo);
        titleView = itemView.findViewById(R.id.event_title);
        authorView = itemView.findViewById(R.id.event_author);
        starView = itemView.findViewById(R.id.star);
        numStarsView = itemView.findViewById(R.id.event_num_stars);
        bodyView = itemView.findViewById(R.id.event_description);
    }

    public void bindToEvent(Event event, View.OnClickListener starClickListener) {
        String image = event.image;
        if (image != null && image.length() != 0) {
            byte[] decodedByteArray = Base64.decode(image, Base64.DEFAULT);
            Bitmap imageEncoded = BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.length);
            photoView.setImageBitmap(imageEncoded);
        } else {
            photoView.setImageResource(R.drawable.ic_action_account_circle_40);
        }
        mDatabase = FirebaseDatabase.getInstance().getReference().child("profiles").child(event.uid);
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Profile p = dataSnapshot.getValue(Profile.class);
                String img = p.image;
                byte[] decodedByteArray = Base64.decode(img, Base64.DEFAULT);
                Bitmap imageEncoded = BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.length);
                author.setImageBitmap(imageEncoded);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                return;
            }
        });
        titleView.setText(event.title);
        authorView.setText(event.author);
        numStarsView.setText(String.valueOf(event.starCount));
        bodyView.setText(event.description);

        starView.setOnClickListener(starClickListener);
    }
}
