package com.google.firebase.quickstart.database;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.quickstart.database.models.Comment;
import com.google.firebase.quickstart.database.models.Event;
import com.google.firebase.quickstart.database.models.Profile;
import com.google.firebase.quickstart.database.models.User;
import com.google.firebase.quickstart.database.models.UtilToast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import co.lujun.androidtagview.TagContainerLayout;

/**
 * Created by zhang on 2017/11/12.
 */

public class EventDetailActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "EventDetailActivity";

    public static final String EXTRA_EVENT_KEY = "event_key";

    private DatabaseReference mEventReference;
    private DatabaseReference mProfileReference;
    private DatabaseReference mCommentsReference;
    private ValueEventListener mEventListener;
    private String mEventKey;
    private EventDetailActivity.CommentAdapter mAdapter;

    private TextView mAuthorView;
    private TextView mDateView;
    private TextView mHostView;
    private TextView mTimeView;
    private TextView mLocationView;
    private TextView mTitleView;
    private TextView mDescriptionView;
    private EditText mCommentField;
    private Button mCommentButton;
    private Button mEventJoinButton;
    private Button mEventQuitButton;
    private RecyclerView mCommentsRecycler;
    private TagContainerLayout mTagContainerLayout;
    private TagContainerLayout mParticipantsContainerLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);
//        setContentView(R.layout.activity_event_detail);

        // Get post key from intent
        mEventKey = getIntent().getStringExtra(EXTRA_EVENT_KEY);
        if (mEventKey == null) {
            throw new IllegalArgumentException("Must pass EXTRA_EVENT_KEY");
        }

        // Initialize Database
        mEventReference = FirebaseDatabase.getInstance().getReference()
                .child("events").child(mEventKey);
        mProfileReference = FirebaseDatabase.getInstance().getReference().child("profiles");
        mCommentsReference = FirebaseDatabase.getInstance().getReference()
                .child("event-comments").child(mEventKey);

        // Initialize Views
        mAuthorView = findViewById(R.id.event_author);
        mHostView = findViewById(R.id.event_host);
        mTitleView = findViewById(R.id.event_title_title);
        mDescriptionView = findViewById(R.id.descriptionView);
        mDateView = findViewById(R.id.event_date);
        mTimeView = findViewById(R.id.event_time);
        mLocationView = findViewById(R.id.event_location);
        mCommentField = findViewById(R.id.field_comment_text);
        mCommentButton = findViewById(R.id.button_event_comment);
        mEventQuitButton =  findViewById(R.id.event_quit);
        mCommentsRecycler = findViewById(R.id.recycler_comments);
        mTagContainerLayout = findViewById(R.id.tagcontainerLayout);
        mParticipantsContainerLayout = findViewById(R.id.participantscontainerLayout);
        mEventJoinButton = findViewById(R.id.event_join);

        mEventJoinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Map<String, Object> childUpdate = new HashMap<>();
                String uid = getUid();
                //childUpdate.put("/events/" + mEventKey + "/participants/" + uid, true);
                childUpdate.put("/participants/" + uid, true);
                // Keep copy of post listener so we can remove it when app stops
                mEventReference.updateChildren(childUpdate);
                UtilToast.showToast(getApplicationContext(),"Just joined the event!");
            }
        });

        mEventQuitButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Map<String, Object> childUpdate = new HashMap<>();
                String uid = getUid();
                //childUpdate.put("/events/" + mEventKey + "/participants/" + uid, true);
                childUpdate.put("/participants/" + uid, false);
                // Keep copy of post listener so we can remove it when app stops
                mEventReference.updateChildren(childUpdate);
                UtilToast.showToast(getApplicationContext(),"Just cancelled the event!");
            }
        });

        mCommentButton.setOnClickListener(this);
        mCommentsRecycler.setLayoutManager(new LinearLayoutManager(this));

    }

    @Override
    public void onStart() {
        super.onStart();

        // Add value event listener to the post
        // [START post_value_event_listener]


        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Event event = dataSnapshot.getValue(Event.class);
                // [START_EXCLUDE]
                List<String> tags  = new ArrayList<>(event.tags.keySet());
                Set<String> participants  = new HashSet<>(event.participants.keySet());
                List<String> nicknames  = new LinkedList<>();

                mProfileReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snap: dataSnapshot.getChildren()) {
                            if(participants.contains(snap.getKey())){
                                Profile p = snap.getValue(Profile.class);
                                nicknames.add(p.nickname);
                            }
                        }
                        mParticipantsContainerLayout.setTags(nicknames);
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                mAuthorView.setText(event.author);
                mTitleView.setText(event.title);
                mDescriptionView.setText(event.description);
                mDateView.setText(event.date);
                mTimeView.setText(event.time);
                mHostView.setText(event.author);
                mLocationView.setText(event.location);
                mTagContainerLayout.setTags(tags);

                // [END_EXCLUDE]
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadEvent:onCancelled", databaseError.toException());
                // [START_EXCLUDE]
                Toast.makeText(EventDetailActivity.this, "Failed to load event.",
                        Toast.LENGTH_SHORT).show();
                // [END_EXCLUDE]
            }
        };
        mEventReference.addValueEventListener(eventListener);
        // [END post_value_event_listener]


        mEventListener = eventListener;

        // Listen for comments
        mAdapter = new EventDetailActivity.CommentAdapter(this, mCommentsReference);
        mCommentsRecycler.setAdapter(mAdapter);
    }

    @Override
    public void onStop() {
        super.onStop();

        // Remove post value event listener
        if (mEventListener != null) {
            mEventReference.removeEventListener(mEventListener);
        }

        // Clean up comments listener
        mAdapter.cleanupListener();
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.button_event_comment) {
            eventComment();
        }
    }

    private void eventComment() {
        final String uid = getUid();
        FirebaseDatabase.getInstance().getReference().child("users").child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Get user information
                        User user = dataSnapshot.getValue(User.class);
                        String authorName = user.username;

                        // Create new comment object
                        String commentText = mCommentField.getText().toString();
                        Comment comment = new Comment(uid, authorName, commentText);

                        // Push the comment, it will appear in the list
                        mCommentsReference.push().setValue(comment);

                        // Clear the field
                        mCommentField.setText(null);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private static class CommentViewHolder extends RecyclerView.ViewHolder {

        public TextView authorView;
        public TextView bodyView;

        public CommentViewHolder(View itemView) {
            super(itemView);

            authorView = itemView.findViewById(R.id.comment_author);
            bodyView = itemView.findViewById(R.id.comment_body);
        }
    }

    private static class CommentAdapter extends RecyclerView.Adapter<EventDetailActivity.CommentViewHolder> {

        private Context mContext;
        private DatabaseReference mDatabaseReference;
        private ChildEventListener mChildEventListener;

        private List<String> mCommentIds = new ArrayList<>();
        private List<Comment> mComments = new ArrayList<>();

        public CommentAdapter(final Context context, DatabaseReference ref) {
            mContext = context;
            mDatabaseReference = ref;

            // Create child event listener
            // [START child_event_listener_recycler]
            ChildEventListener childEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                    Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());

                    // A new comment has been added, add it to the displayed list
                    Comment comment = dataSnapshot.getValue(Comment.class);

                    // [START_EXCLUDE]
                    // Update RecyclerView
                    mCommentIds.add(dataSnapshot.getKey());
                    mComments.add(comment);
                    notifyItemInserted(mComments.size() - 1);
                    // [END_EXCLUDE]
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                    Log.d(TAG, "onChildChanged:" + dataSnapshot.getKey());

                    // A comment has changed, use the key to determine if we are displaying this
                    // comment and if so displayed the changed comment.
                    Comment newComment = dataSnapshot.getValue(Comment.class);
                    String commentKey = dataSnapshot.getKey();

                    // [START_EXCLUDE]
                    int commentIndex = mCommentIds.indexOf(commentKey);
                    if (commentIndex > -1) {
                        // Replace with the new data
                        mComments.set(commentIndex, newComment);

                        // Update the RecyclerView
                        notifyItemChanged(commentIndex);
                    } else {
                        Log.w(TAG, "onChildChanged:unknown_child:" + commentKey);
                    }
                    // [END_EXCLUDE]
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    Log.d(TAG, "onChildRemoved:" + dataSnapshot.getKey());

                    // A comment has changed, use the key to determine if we are displaying this
                    // comment and if so remove it.
                    String commentKey = dataSnapshot.getKey();

                    // [START_EXCLUDE]
                    int commentIndex = mCommentIds.indexOf(commentKey);
                    if (commentIndex > -1) {
                        // Remove data from the list
                        mCommentIds.remove(commentIndex);
                        mComments.remove(commentIndex);

                        // Update the RecyclerView
                        notifyItemRemoved(commentIndex);
                    } else {
                        Log.w(TAG, "onChildRemoved:unknown_child:" + commentKey);
                    }
                    // [END_EXCLUDE]
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                    Log.d(TAG, "onChildMoved:" + dataSnapshot.getKey());

                    // A comment has changed position, use the key to determine if we are
                    // displaying this comment and if so move it.
                    Comment movedComment = dataSnapshot.getValue(Comment.class);
                    String commentKey = dataSnapshot.getKey();

                    // ...
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.w(TAG, "eventComments:onCancelled", databaseError.toException());
                    Toast.makeText(mContext, "Failed to load comments.",
                            Toast.LENGTH_SHORT).show();
                }
            };
            ref.addChildEventListener(childEventListener);
            // [END child_event_listener_recycler]

            // Store reference to listener so it can be removed on app stop
            mChildEventListener = childEventListener;
        }

        @Override
        public EventDetailActivity.CommentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            View view = inflater.inflate(R.layout.item_comment, parent, false);
            return new EventDetailActivity.CommentViewHolder(view);
        }

        @Override
        public void onBindViewHolder(EventDetailActivity.CommentViewHolder holder, int position) {
            Comment comment = mComments.get(position);
            holder.authorView.setText(comment.author);
            holder.bodyView.setText(comment.text);
        }

        @Override
        public int getItemCount() {
            return mComments.size();
        }

        public void cleanupListener() {
            if (mChildEventListener != null) {
                mDatabaseReference.removeEventListener(mChildEventListener);
            }
        }

    }
}
