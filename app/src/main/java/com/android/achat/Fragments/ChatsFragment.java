package com.android.achat.Fragments;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.achat.Activities.ChatActivity;
import com.android.achat.DataModels.Message;
import com.android.achat.DataModels.User;
import com.android.achat.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;


public class ChatsFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    RecyclerView chatList;
    FirebaseRecyclerAdapter<User,ChatViewHolder> adapter;
    String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    LinearLayout placeholder;
    DatabaseReference dbrefRoot = FirebaseDatabase.getInstance().getReference();

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    public ChatsFragment() {

    }

    public static ChatsFragment newInstance(String param1, String param2) {
        ChatsFragment fragment = new ChatsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_chats, container, false);
        placeholder = v.findViewById(R.id.placeholder_chat);
        chatList = v.findViewById(R.id.chat_list);
        chatList.setLayoutManager(new LinearLayoutManager(getContext()));
        return v;
    }


    @Override
    public void onStart() {
        super.onStart();
        fetchChats();
        chatList.setAdapter(adapter);
        adapter.startListening();
    }

    private void fetchChats() {
        final Query q = dbrefRoot.child("Chat/"+currentUid);
        q.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChildren()){
                    chatList.setVisibility(View.VISIBLE);
                    placeholder.setVisibility(View.GONE);

                }else{
                    chatList.setVisibility(View.GONE);
                    placeholder.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        FirebaseRecyclerOptions<User> options = new FirebaseRecyclerOptions.Builder<User>().setQuery(q, User.class).build();
        adapter = new FirebaseRecyclerAdapter<User, ChatViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ChatViewHolder holder, int position, @NonNull User model) {
                final String uId = getRef(position).getKey();

                dbrefRoot.child("Friends/"+currentUid+"/"+uId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChildren()){
                            dbrefRoot.child("Users/"+uId).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    final User u = dataSnapshot.getValue(User.class);
                                    u.setUid(uId);
                                    holder.setImage(u.getThumbnail(),getContext());
                                    holder.setName(u.getName());
                                    holder.setOnline("false");
                                    dbrefRoot.child("Messages").child(currentUid).child(uId).orderByKey().limitToLast(1).addChildEventListener(new ChildEventListener() {
                                        @Override
                                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                            Message m = dataSnapshot.getValue(Message.class);
                                            m.setMessageId(dataSnapshot.getKey());

                                            holder.setLastMessage(m.getMessage());
                                            holder.setMessageTime(m.getTime());

                                        }

                                        @Override
                                        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                                        }

                                        @Override
                                        public void onChildRemoved(DataSnapshot dataSnapshot) {

                                        }

                                        @Override
                                        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });


                                    holder.v.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            Intent i = new Intent(getContext(), ChatActivity.class);
                                            i.putExtra("user_object",u);
                                            startActivity(i);
                                        }
                                    });

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }

            @Override
            public ChatViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(getContext()).inflate(R.layout.friend_layout,parent,false);
                return new ChatViewHolder(v);
            }
        };

    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder{
        View v;
        public ChatViewHolder(View itemView) {
            super(itemView);
            v=itemView;
        }

        public void setName(String name ){
            TextView userName = (TextView) v.findViewById(R.id.name);
            userName.setText(name);

        }
        public void setImage(String image, Context mContext){
            CircleImageView circleImageView = (CircleImageView) v.findViewById(R.id.profile_image);
            Picasso.with(mContext).load(image).placeholder(R.drawable.default_avatar_profile).into(circleImageView);
        }

        public void setLastMessage(String message){
            TextView statusView = (TextView) v.findViewById(R.id.status);
            statusView.setText(message);
        }

        public void setOnline(String onlineStatus) {
            ImageView online = (ImageView) v.findViewById(R.id.online);
            if (onlineStatus.equals("true")) {
                online.setVisibility(View.VISIBLE);
            } else {
                online.setVisibility(View.INVISIBLE);
            }
        }

        public void setMessageTime(String time){
            TextView messageTime = v.findViewById(R.id.message_time);
            messageTime.setVisibility(View.VISIBLE);
            messageTime.setText(time);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}
