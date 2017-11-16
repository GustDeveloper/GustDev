package com.google.firebase.quickstart.database.viewholder;


import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

import com.google.firebase.quickstart.database.R;

import co.lujun.androidtagview.TagContainerLayout;

public class TagViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TagContainerLayout tagContainerLayout;
    View.OnClickListener clickListener;
    public Button button;

    public TagViewHolder(View v, View.OnClickListener listener) {
        super(v);
        this.clickListener = listener;
        tagContainerLayout = (TagContainerLayout) v.findViewById(R.id.tagcontainerLayout);
        button = (Button) v.findViewById(R.id.button);
        //v.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (clickListener != null) {
            clickListener.onClick(v);
        }
    }
}