package com.google.firebase.quickstart.database;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.quickstart.database.models.Comment;
import com.google.firebase.quickstart.database.models.Message;
import com.google.firebase.quickstart.database.models.Post;
import com.google.firebase.quickstart.database.models.Profile;
import com.google.firebase.quickstart.database.models.User;
import com.google.firebase.quickstart.database.viewholder.PostViewHolder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.widget.RelativeLayout.ALIGN_RIGHT;

public class ChatActivity extends BaseActivity {
    private Button btn_send_msg;
    private EditText input_msg;
    private TextView chat_conversation;
    private DatabaseReference chat_room;
    private MessageAdapter mAdapter;
    private RecyclerView mRecyclers;
    private String userId;
    private String path;
    private String receiver;
    private String ReceiverName;
    private Bitmap selfImg = null;
    private Bitmap otherImg = null;
    private int step = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        step++;
        Log.d("ChatCreate","onCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        path = getIntent().getStringExtra("Path");
        receiver = getIntent().getStringExtra("receiver");
        Log.d("Path", path);
        btn_send_msg = (Button) findViewById(R.id.btn_send);
        input_msg = (EditText) findViewById(R.id.msg_input);
        mRecyclers = findViewById(R.id.messages_list);
        mRecyclers.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);

        mRecyclers.setLayoutManager(linearLayoutManager);
        chat_room = FirebaseDatabase.getInstance().getReference();
        setTitle("");
        userId = getUid();

        // get friend's image and name

        if (getIntent().getStringExtra("ReceiverName") != null) {
            ReceiverName = getIntent().getStringExtra("ReceiverName");
            setTitle(ReceiverName);
        }


        btn_send_msg.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                // Get current Time Stamp
                Calendar c = Calendar.getInstance();
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String formattedDate = df.format(c.getTime());

                //String sender, String receiver, String msg, String timeStamp
                // Update the message to the database
                Message m = new Message(userId, receiver, input_msg.getText().toString(), formattedDate);
                String key = chat_room.child(path).push().getKey();
                input_msg.setText("");
                chat_room.child(path).child(key).setValue(m);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();


        Log.d("ChatCreate","onStart");


        // call the recyclerview adapter
        chat_room.child("profiles").child(receiver).addListenerForSingleValueEvent(new ValueEventListener(){

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Profile p = dataSnapshot.getValue(Profile.class);
                setTitle(p.username);
                byte[] decodedByteArray = Base64.decode(p.image, Base64.DEFAULT);
                Bitmap imageEncoded = BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.length);
                otherImg = imageEncoded;
                mAdapter.notifyDataSetChanged();
                mRecyclers.scrollToPosition(mAdapter.returnSize());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        // get your own image
        chat_room.child("profiles").child(userId).addListenerForSingleValueEvent(new ValueEventListener(){

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Profile p = dataSnapshot.getValue(Profile.class);
                byte[] decodedByteArray = Base64.decode(p.image, Base64.DEFAULT);
                Bitmap imageEncoded = BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.length);
                selfImg = imageEncoded;;
                mAdapter.notifyDataSetChanged();
                mRecyclers.scrollToPosition(mAdapter.returnSize());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        mAdapter = new MessageAdapter(this, chat_room.child(path));
        mRecyclers.setAdapter(mAdapter);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == R.id.action_profile) {
            Intent profileActivity = new Intent(this,ProfileActivity.class);
            profileActivity.putExtra("intentUserID", receiver);
            startActivity(profileActivity);
            return super.onOptionsItemSelected(item);
        } else {
            return super.onOptionsItemSelected(item);
        }
    }


    private static class MessageViewHolder extends RecyclerView.ViewHolder {

        public TextView authorView;
        public TextView bodyView;
        public CircleImageView photoView;
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
        private RelativeLayout.LayoutParams params;
        private RelativeLayout.LayoutParams params1;
        private LinearLayout.LayoutParams p;
        private RelativeLayout.LayoutParams rparams;

        public int returnSize(){
            return mMessages.size() - 1;
        }

        public MessageAdapter(final Context context, DatabaseReference ref) {
            mContext = context;
            mDatabaseReference = ref;
            params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            params1 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            p.weight = 1.0f;
            p.gravity = Gravity.RIGHT;
            rparams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);



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
            //Log.d("ChatViewHolder", "Create");
            Log.d("ChatCreate", "ViewHolder");
            LayoutInflater inflater = LayoutInflater.from(mContext);
            View view = inflater.inflate(R.layout.left_message, parent, false);
            return new MessageViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final MessageViewHolder holder, int position) {

            Message message = mMessages.get(position);
            holder.authorView.setText(message.timeStamp);
            holder.bodyView.setText(message.msg);

            // for own message, appears right. for others' message, appear left
            if (message.sender.equals(userId)){
                holder.photoView.setLayoutParams(params);
                params1.addRule(RelativeLayout.LEFT_OF, holder.photoView.getId());
                holder.linearView.setLayoutParams(params1);
                holder.bodyView.setLayoutParams(p);
                holder.authorView.setLayoutParams(p);
                if (selfImg != null) {holder.photoView.setImageBitmap(selfImg);}
            } else {
                RelativeLayout.LayoutParams paramsl = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                paramsl.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                holder.photoView.setLayoutParams(paramsl);
                rparams.addRule(RelativeLayout.RIGHT_OF, holder.photoView.getId());
                holder.linearView.setLayoutParams(rparams);
                LinearLayout.LayoutParams pl = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                pl.weight = 1.0f;
                pl.gravity = Gravity.LEFT;
                holder.bodyView.setLayoutParams(pl);
                holder.authorView.setLayoutParams(pl);
                if (otherImg != null) {holder.photoView.setImageBitmap(otherImg);}//holder.photoView.setImageBitmap(otherImg);
                else {
                    Log.d("ChatService", "NULL");
                }
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