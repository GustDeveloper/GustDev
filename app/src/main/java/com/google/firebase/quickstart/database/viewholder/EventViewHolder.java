package com.google.firebase.quickstart.database.viewholder;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.quickstart.database.R;
import com.google.firebase.quickstart.database.models.Event;

public class EventViewHolder extends RecyclerView.ViewHolder {
    public ImageView photoView;
    public TextView titleView;
    public TextView authorView;
    public ImageView starView;
    public TextView numStarsView;
    public TextView bodyView;

    public EventViewHolder(View itemView) {
        super(itemView);
        //photoView = itemView.findViewById(R.id.event_author_photo);
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
        }
        titleView.setText(event.title);
        authorView.setText(event.author);
        numStarsView.setText(String.valueOf(event.starCount));
        bodyView.setText(event.description);

        starView.setOnClickListener(starClickListener);
    }
}
