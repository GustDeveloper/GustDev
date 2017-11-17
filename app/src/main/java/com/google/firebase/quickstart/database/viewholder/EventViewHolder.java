package com.google.firebase.quickstart.database.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.quickstart.database.R;
import com.google.firebase.quickstart.database.models.Event;
import com.google.firebase.quickstart.database.models.Post;

public class EventViewHolder extends RecyclerView.ViewHolder {

    public TextView titleView;
    public TextView authorView;
    public ImageView starView;
    public TextView numStarsView;
    public TextView bodyView;

    public EventViewHolder(View itemView) {
        super(itemView);

        titleView = itemView.findViewById(R.id.event_title);
        authorView = itemView.findViewById(R.id.event_author);
        starView = itemView.findViewById(R.id.star);
        numStarsView = itemView.findViewById(R.id.event_num_stars);
        bodyView = itemView.findViewById(R.id.event_description);
    }

    public void bindToEvent(Event event, View.OnClickListener starClickListener) {
        titleView.setText(event.title);
        authorView.setText(event.author);
        numStarsView.setText(String.valueOf(event.starCount));
        bodyView.setText(event.description);

        starView.setOnClickListener(starClickListener);
    }
}
