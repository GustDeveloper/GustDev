package com.google.firebase.quickstart.database;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.quickstart.database.models.Comment;
import com.google.firebase.quickstart.database.models.Message;
import com.google.firebase.quickstart.database.models.Post;
import com.google.firebase.quickstart.database.models.User;
import com.google.firebase.quickstart.database.viewholder.PostViewHolder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static android.widget.RelativeLayout.ALIGN_RIGHT;

public class chatActivity extends BaseActivity {
    private Button btn_send_msg;
    private EditText input_msg;
    private TextView chat_conversation;
    private DatabaseReference chat_room;
    private MessageAdapter mAdapter;
    private RecyclerView mRecyclers;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);


        btn_send_msg = (Button) findViewById(R.id.btn_send);
        input_msg = (EditText) findViewById(R.id.msg_input);
        chat_conversation = (TextView) findViewById(R.id.chat_view);
        mRecyclers = findViewById(R.id.messages_list);
        mRecyclers.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);

        mRecyclers.setLayoutManager(linearLayoutManager);

        chat_room = FirebaseDatabase.getInstance().getReference();
        userId = getUid();
        btn_send_msg.setOnClickListener(new View.OnClickListener(){


            @Override
            public void onClick(View v) {
                // Get current Time Stamp
                Calendar c = Calendar.getInstance();
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String formattedDate = df.format(c.getTime());

                // Update the message to the database
                Message m = new Message(userId, "TJ", input_msg.getText().toString(), formattedDate);
                String key = chat_room.child("chat-room").push().getKey();
                input_msg.setText("");
                chat_room.child("chat-room").child("Jinhao").child(key).setValue(m);


            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        // call the recyclerview adapter
        mAdapter = new MessageAdapter(this, chat_room.child("chat-room").child("Jinhao"));
        mRecyclers.setAdapter(mAdapter);

    }



    private static class MessageViewHolder extends RecyclerView.ViewHolder {

        public TextView authorView;
        public TextView bodyView;
        public ImageView photoView;
        public LinearLayout linearView;

        public MessageViewHolder(View itemView) {
            super(itemView);

            authorView = itemView.findViewById(R.id.comment_author);
            bodyView = itemView.findViewById(R.id.comment_body);
            photoView = itemView.findViewById(R.id.comment_photo);
            linearView = itemView.findViewById(R.id.linear);
        }
    }

    private  class MessageAdapter extends RecyclerView.Adapter<MessageViewHolder> {

        private Context mContext;
        private DatabaseReference mDatabaseReference;
        private ChildEventListener mChildEventListener;

        private List<String> mMessageIds = new ArrayList<>();

        private List<Message> mMessages = new ArrayList<>();


        public MessageAdapter(final Context context, DatabaseReference ref) {
            mContext = context;
            mDatabaseReference = ref;
            ChildEventListener childEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {

                    // A new comment has been added, add it to the displayed list
                    Message message = dataSnapshot.getValue(Message.class);

                    // Update RecyclerView
                    mMessageIds.add(dataSnapshot.getKey());
                    mMessages.add(message);
                    notifyItemInserted(mMessages.size() - 1);
                    // make the newest message appear at the bottom
                    mRecyclers.scrollToPosition(mMessages.size()-1);
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                    Message newMessage = dataSnapshot.getValue(Message.class);
                    String messageKey = dataSnapshot.getKey();

                    int messageIndex = mMessages.indexOf(messageKey);
                    if (messageIndex > -1) {
                        // Replace with the new data
                        mMessages.set(messageIndex, newMessage);
                        // Update the RecyclerView
                        notifyItemChanged(messageIndex);
                    } else {

                    }
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            };
            ref.addChildEventListener(childEventListener);

            mChildEventListener = childEventListener;
        }

        @Override
        public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            View view = inflater.inflate(R.layout.left_message, parent, false);
            return new MessageViewHolder(view);
        }

        @Override
        public void onBindViewHolder(MessageViewHolder holder, int position) {

            Message message = mMessages.get(position);
            holder.authorView.setText(message.timeStamp);
            holder.bodyView.setText(message.msg);

            // for own message, appears right. for others' message, appear left
            if (message.sender.equals(userId)){
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                //rparams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, holder.linearView.getId());
                params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                holder.photoView.setLayoutParams(params);
                params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                params.addRule(RelativeLayout.LEFT_OF, holder.photoView.getId());
                holder.linearView.setLayoutParams(params);
                LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                p.weight = 1.0f;
                p.gravity = Gravity.RIGHT;
                holder.bodyView.setLayoutParams(p);
                holder.authorView.setLayoutParams(p);
            } else {
                RelativeLayout.LayoutParams rparams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                rparams.addRule(RelativeLayout.RIGHT_OF, holder.photoView.getId());
                holder.linearView.setLayoutParams(rparams);
            }
        }

        @Override
        public int getItemCount() {
            return mMessages.size();
        }

        public void cleanupListener() {
            if (mChildEventListener != null) {
                mDatabaseReference.removeEventListener(mChildEventListener);
            }
        }

    }

}

