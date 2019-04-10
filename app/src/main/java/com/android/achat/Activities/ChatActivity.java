package com.android.achat.Activities;

import android.content.Context;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.android.achat.DataModels.Message;
import com.android.achat.DataModels.User;
import com.android.achat.DateTime;
import com.android.achat.FirebaseCustomScrollListener;
import com.android.achat.MessageAdapter;
import com.android.achat.R;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends BaseActivity {

    RecyclerView messageList;
    EditText messageText;
    TextView lastSeen;
    ImageButton sendMessageButton;
    Toolbar mToolbar;
    User u;
    DatabaseReference dbrefRoot;
    String mCurrentUid;
    ValueEventListener seenListener;
    ArrayList<Message> messageArrayList = new ArrayList<>();
    MessageAdapter adapter2;
    LinearLayoutManager mLinearLayoutManager;
    private final static int noOfMessagesPerPage=20;
    private  int pageNo=1;
    String lastChildKey;

    FirebaseRecyclerOptions options = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        dbrefRoot = FirebaseDatabase.getInstance().getReference();
        messageList=findViewById(R.id.message_list);
        messageText=findViewById(R.id.message_text);
        sendMessageButton=findViewById(R.id.send_button);
        mToolbar=findViewById(R.id.chat_toolbar);

        u=getIntent().getParcelableExtra("user_object");
        mCurrentUid= FirebaseAuth.getInstance().getCurrentUser().getUid();

        setSupportActionBar(mToolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayShowTitleEnabled(false);
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setDisplayShowCustomEnabled(true);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View mChatBar = inflater.inflate(R.layout.chat_toolbar_layout,null);
        TextView chatBarDisplayName =  mChatBar.findViewById(R.id.chat_appbar_display_name);
        chatBarDisplayName.setText(u.getName());
        CircleImageView mdisplayImage= mChatBar.findViewById(R.id.chat_appbar_display_image);
        Picasso.with(this).load(u.getThumbnail()).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.default_avatar_profile).into(mdisplayImage);
        lastSeen = mChatBar.findViewById(R.id.chat_appbar_last_seen);
        ab.setCustomView(mChatBar);

        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setStackFromEnd(true);
        messageList.setLayoutManager(mLinearLayoutManager);
        adapter2 = new MessageAdapter(messageArrayList,this);
        fetchMessages();
        messageList.setAdapter(adapter2);
        messageList.addOnScrollListener(new FirebaseCustomScrollListener(mLinearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                fetchMoreMessages(view);

            }
        });

        dbrefRoot.child("Chat").child(mCurrentUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChild(u.getUid())){
                    Map chatAddMap = new HashMap();
                    chatAddMap.put("online",false);
                    chatAddMap.put("last_seen", ServerValue.TIMESTAMP);

                    Map chatUserMap = new HashMap();
                    chatUserMap.put("Chat/"+mCurrentUid+"/"+u.getUid(),chatAddMap);
                    chatUserMap.put("Chat/"+u.getUid()+"/"+mCurrentUid,chatAddMap);

                    dbrefRoot.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError != null) {
                                Log.w("CHAT_LOG",databaseError.getMessage());
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = messageText.getText().toString().trim();
                if(!TextUtils.isEmpty(message)) {
                    sendMessage(message);
                    messageText.setText("");
                }else{
                    messageText.setError("No Message");
                }
            }
        });


    }

    private void fetchMoreMessages(final RecyclerView view) {
        final ArrayList<Message> newMessagesList = new ArrayList<>();
        lastChildKey=messageArrayList.get(0).getMessageId();
        Log.d("prev count & last key",messageArrayList.size()+" "+lastChildKey);
        dbrefRoot.child("Messages/" + mCurrentUid + "/" + u.getUid()).orderByKey().limitToLast(noOfMessagesPerPage).endAt(lastChildKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    Message m = ds.getValue(Message.class);
                    m.setMessageId(ds.getKey());
                    newMessagesList.add(m);
                }
                if (!lastChildKey.equals(newMessagesList.get(0).getMessageId())) {
                    messageArrayList.addAll(0, newMessagesList);
                    view.post(new Runnable() {
                        @Override
                        public void run() {
                            adapter2.notifyDataSetChanged();

                        }
                    });
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        Log.d("message count(new)",messageArrayList.size()+"");
    }


    private void sendMessage(final String message) {

        final String currentUserRef  = "Messages/"+mCurrentUid+"/"+u.getUid();
        final String chatUserRef  = "Messages/"+u.getUid()+"/"+mCurrentUid;

        DatabaseReference dbrefPush = dbrefRoot.child(currentUserRef).push();
        final String push_id  = dbrefPush.getKey();
        Map messageMap = new HashMap();

        messageMap.put("message",message);
        String online = lastSeen.getText().toString().trim();

        if(!(online.contains("Online"))){
            messageMap.put("seen", "false");
        }else{
            messageMap.put("seen","true");
        }
        messageMap.put("type","text");
        messageMap.put("time",ServerValue.TIMESTAMP);
        messageMap.put("from",mCurrentUid);
        Map messageUserMap = new HashMap();
        messageUserMap.put(currentUserRef+"/"+push_id,messageMap);
        messageUserMap.put(chatUserRef+"/"+push_id,messageMap);

        dbrefRoot.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                if (databaseError != null) {

                    Log.w("CHAT_LOG",databaseError.getMessage());
                }
            }
        });

        ValueEventListener checkOnline = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    //    dbrefRoot.child("Chat/"+u.getUid()+"/"+mCurrentUid).addListenerForSingleValueEvent(checkOnline);
        Map notificationMap = new HashMap();
        notificationMap.put("Notifications/Message_Notifications/"+u.getUid()+"/"+push_id+"/from",mCurrentUid);
        notificationMap.put("Notifications/Message_Notifications/"+u.getUid()+"/"+push_id+"/type","text");
        dbrefRoot.updateChildren(notificationMap);
    }

    private void fetchMessages() {

        dbrefRoot.child("Messages/" + mCurrentUid + "/" + u.getUid()).limitToLast(pageNo*noOfMessagesPerPage).startAt(null).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Message m =dataSnapshot.getValue(Message.class);
                m.setMessageId(dataSnapshot.getKey());
                messageArrayList.add(m);

                adapter2.notifyDataSetChanged();
                if(m.getFrom().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                    messageList.scrollToPosition(adapter2.getItemCount()-1);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                for(int i = 0;i<messageArrayList.size();i++){
                    Message message = messageArrayList.get(i);
                    if(message.getMessageId().equals(dataSnapshot.getKey())){
                        Message m = dataSnapshot.getValue(Message.class);
                        m.setMessageId(dataSnapshot.getKey());

                        messageArrayList.set(i,m);
                        adapter2.notifyDataSetChanged();
                    }
                }

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                for(int i = 0;i<messageArrayList.size();i++){
                    Message message = messageArrayList.get(i);
                    Log.d("removed child :", dataSnapshot.getKey());
                    if(message.getMessageId().equals(dataSnapshot.getKey())){
                        Message m = dataSnapshot.getValue(Message.class);
                        m.setMessageId(dataSnapshot.getKey());

                        messageArrayList.remove(i);
                        adapter2.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        Map userSeen = new HashMap();
        userSeen.put("online","true");
        userSeen.put("last_seen",ServerValue.TIMESTAMP);
        dbrefRoot.child("Chat").child(mCurrentUid).child(u.getUid()).updateChildren(userSeen);
    }


    @Override
    protected void onResume() {
        super.onResume();

        seenListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.hasChildren()) {
                    String online = dataSnapshot.child("online").getValue().toString();
                    String timeStamp = dataSnapshot.child("last_seen").getValue().toString();

                    if (online.equals("true")) {
                        lastSeen.setText("Online");
                    } else {
                        DateTime dateTime = new DateTime();
                        long lastTime = Long.parseLong(timeStamp);
                        String time = dateTime.getTimeAgo(lastTime, getApplicationContext());
                        lastSeen.setText("Last Seen " + time);
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        dbrefRoot.child("Chat").child(u.getUid()).child(mCurrentUid).addValueEventListener(seenListener);
    }
    @Override
    protected void onPause() {
        super.onPause();
        dbrefRoot.child("Chat").child(u.getUid()).child(mCurrentUid).removeEventListener(seenListener);
        dbrefRoot.child("Chat").child(mCurrentUid).child(u.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChildren()){
                    Map userSeen = new HashMap();
                    userSeen.put("online","false");
                    userSeen.put("last_seen",ServerValue.TIMESTAMP);
                    dbrefRoot.child("Chat").child(mCurrentUid).child(u.getUid()).updateChildren(userSeen);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}
