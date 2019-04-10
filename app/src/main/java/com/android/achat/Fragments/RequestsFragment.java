package com.android.achat.Fragments;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestsFragment extends Fragment {

    RecyclerView receivedList,sentList;
    DatabaseReference dbrefRoot;
    FirebaseRecyclerAdapter adapterSent,adapterReceived;
    TextView placeholderSent,placeholderReceived;

    public RequestsFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v =inflater.inflate(R.layout.fragment_requests, container, false);

        receivedList = v.findViewById(R.id.received_list);
        sentList = v.findViewById(R.id.sent_list);
        receivedList.setLayoutManager(new LinearLayoutManager(getContext()));
        sentList.setLayoutManager(new LinearLayoutManager(getContext()));
        dbrefRoot= FirebaseDatabase.getInstance().getReference();
        placeholderReceived=v.findViewById(R.id.placeholder_received);
        placeholderSent=v.findViewById(R.id.placeholder_sent);
        return  v;
    }

    @Override
    public void onStart() {
        super.onStart();
       adapterSent=fetchRequests("sent");
       adapterSent.startListening();
       sentList.setAdapter(adapterSent);
       adapterReceived=fetchRequests("received");
       adapterReceived.startListening();
       receivedList.setAdapter(adapterReceived);

    }

    @Override
    public void onStop() {
        super.onStop();
        adapterReceived.stopListening();
        adapterSent.stopListening();
    }

    private FirebaseRecyclerAdapter fetchRequests(final String type) {

        final Query q = dbrefRoot.child("Friend_Requests").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).orderByChild("request_type").equalTo(type);
        q.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChildren()) {
                    if (type.equals("sent")) {
                        sentList.setVisibility(View.VISIBLE);
                        placeholderSent.setVisibility(View.INVISIBLE);
                    }
                    if (type.equals("sent")) {
                        receivedList.setVisibility(View.VISIBLE);
                        placeholderReceived.setVisibility(View.INVISIBLE);
                    }
                }else{

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        FirebaseRecyclerOptions<User> options = new FirebaseRecyclerOptions.Builder<User>().setQuery(q,User.class).build();
        FirebaseRecyclerAdapter adapter = new FirebaseRecyclerAdapter<User, RequestViewHolder>(options) {
            @Override
            public RequestViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                return new RequestViewHolder(LayoutInflater.from(parent.getContext()).
                        inflate(R.layout.user_list_layout,parent,false));
            }

            @Override
            protected void onBindViewHolder(@NonNull final RequestViewHolder viewHolder, int position, @NonNull User model) {
                final String  id = getRef(position).getKey();

                dbrefRoot.child("Users").child(id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final User u = dataSnapshot.getValue(User.class);
                        u.setUid(id);
                        viewHolder.setName(u.getName());
                        viewHolder.setImage(u.getImage(),getContext());
                        viewHolder.setEmail(u.getEmail());
                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                startActivity(new Intent(getActivity(), ProfileActivity.class).putExtra("user_object",u));
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        };


        return adapter;
    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder {

        public View mView;
        TextView userName;
        TextView statusView;
        CircleImageView circleImageView;

        public RequestViewHolder(View itemView) {
            super(itemView);
            mView=itemView;

        }

        public void setName(String name) {
            userName = (TextView) mView.findViewById(R.id.name);
            userName.setText(name);

        }

        public void setImage(String image, Context mContext) {
            circleImageView = (CircleImageView) mView.findViewById(R.id.profile_image);
            if(!image.equals("default"))
                Picasso.with(mContext).load(image).placeholder(R.drawable.default_avatar_profile).into(circleImageView);
        }

        public void setEmail(String email) {
            statusView = (TextView) mView.findViewById(R.id.email);
            statusView.setText(email);
        }

    }
}
