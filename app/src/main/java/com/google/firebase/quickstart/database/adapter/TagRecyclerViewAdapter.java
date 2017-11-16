package com.google.firebase.quickstart.database.adapter;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.quickstart.database.R;
import com.google.firebase.quickstart.database.viewholder.TagViewHolder;

public class TagRecyclerViewAdapter  extends RecyclerView.Adapter<TagViewHolder> {
    private Context mContext;
    private String[] mData;
    private View.OnClickListener mOnClickListener;

    public TagRecyclerViewAdapter(Context context, String[] data) {
        this.mContext = context;
        this.mData = data;
    }

    @Override
    public int getItemCount() {
        return 10;
    }

    @Override
    public TagViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new TagViewHolder(LayoutInflater.from(mContext)
                .inflate(R.layout.view_recyclerview_item, parent, false), mOnClickListener);
    }

    @Override
    public void onBindViewHolder(TagViewHolder holder, int position) {
        holder.tagContainerLayout.setTags(mData);
        holder.button.setOnClickListener(mOnClickListener);
    }

    public void setOnClickListener(View.OnClickListener listener) {
        this.mOnClickListener = listener;
    }
}
