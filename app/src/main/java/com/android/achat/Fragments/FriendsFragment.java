package com.android.achat.Fragments;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.achat.Activities.ChatActivity;
import com.android.achat.Activities.ProfileActivity;
import com.android.achat.DataModels.User;
import com.android.achat.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {

    RecyclerView friendList;
    DatabaseReference dbrefRoot;
    RelativeLayout placeholderLayout;
    FirebaseRecyclerAdapter adapter;
    public FriendsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbrefRoot= FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v= inflater.inflate(R.layout.fragment_friends, container, false);
        friendList = v.findViewById(R.id.friend_list);
        placeholderLayout = v.findViewById(R.id.placeholder_friend);
        friendList.setLayoutManager(new LinearLayoutManager(getContext()));
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        fetchFriends();
        adapter.startListening();
        friendList.setAdapter(adapter);

    }

    private void fetchFriends() {
        Query q = dbrefRoot.child("Friends").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        q.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChildren()){
                    placeholderLayout.setVisibility(View.GONE);
                    friendList.setVisibility(View.VISIBLE);
                }
                else{
                    placeholderLayout.setVisibility(View.VISIBLE);
                    friendList.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        FirebaseRecyclerOptions<User> options = new FirebaseRecyclerOptions.Builder().setQuery(q,User.class).build();

        adapter = new FirebaseRecyclerAdapter<User, FriendViewHolder>(options) {
            @Override
            public FriendViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_layout,parent,false);
                return new FriendViewHolder(v);
            }

            @Override
            protected void onBindViewHolder(@NonNull final FriendViewHolder viewHolder, int position, @NonNull User model) {
                final String user_id = getRef(position).getKey();
                Log.d("friend id :",user_id);
                dbrefRoot.child("Users").child(user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        final User u = dataSnapshot.getValue(User.class);
                        u.setUid(user_id);
                        String online =dataSnapshot.child("online").getValue().toString();
                        viewHolder.setName(u.getName());
                        viewHolder.setImage(u.getThumbnail(), getContext());
                        viewHolder.setStatus(u.getStatus());
                        viewHolder.setOnline(online);
                        viewHolder.v.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                CharSequence[] options = new CharSequence[]{
                                        "Open Profile", "Send Message"
                                };

                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                builder.setTitle("Select Option");
                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (which == 0) {
                                            Intent i = new Intent(getContext(), ProfileActivity.class);
                                            i.putExtra("user_object", u);
                                            startActivity(i);
                                        } else if (which == 1) {
                                            Intent i = new Intent(getContext(), ChatActivity.class);
                                            i.putExtra("user_object", u);
                                            startActivity(i);
                                        }
                                    }
                                });
                                builder.show();
                            }
                        });
                    }
                });
            }
        };
    }

    public static class FriendViewHolder extends RecyclerView.ViewHolder{
        View v;
        public FriendViewHolder(View itemView) {
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

        public void setStatus(String status){
            TextView statusView = (TextView) v.findViewById(R.id.status);
            statusView.setText(status);
        }

        public void setOnline(String onlineStatus) {
            ImageView online = (ImageView) v.findViewById(R.id.online);
            if (onlineStatus.equals("true")) {
                online.setVisibility(View.VISIBLE);
            } else {
                online.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }


}
