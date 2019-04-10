package com.android.achat.Activities;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.design.widget.TextInputEditText;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.achat.R;
import com.android.achat.DataModels.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class SearchActivity extends BaseActivity {

    TextInputEditText searchEditText;
    RecyclerView searchResultView;
    DatabaseReference dbrefUsers;
    String searchString;
    ArrayList<User> users = new ArrayList<>();
    SearchAdapter searchAdapter;
    Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        searchEditText = findViewById(R.id.search);
        searchResultView = findViewById(R.id.search_results);

        mToolbar = (Toolbar) findViewById(R.id.search_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Search People");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        dbrefUsers = FirebaseDatabase.getInstance().getReference("Users");

        searchResultView.setLayoutManager(new LinearLayoutManager(this));

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(!editable.toString().isEmpty()){
                    searchString = editable.toString().trim();
                    fetchUsers(searchString);
                }else{
                    users.clear();
                    searchResultView.removeAllViews();
                }
            }
        });


    }

    private void fetchUsers(final String searchString) {

        dbrefUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                users.clear();
                searchResultView.removeAllViews();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {

                    String name = ds.child("name").getValue().toString();
                    String uid = ds.getKey().toString();

                    if (name.toLowerCase().contains(searchString.toLowerCase()) && !uid.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                        Log.d("user id :",uid);
                        String status = ds.child("status").getValue().toString();
                        String pic = ds.child("image").getValue().toString();
                        String email = ds.child("email").getValue().toString();
                        User u = new User(uid,email,name, pic, status);
                        users.add(u);
                    }
                }
                searchAdapter = new SearchAdapter(SearchActivity.this,users);
                searchResultView.setAdapter(searchAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public class SearchAdapter extends RecyclerView.Adapter<SearchViewHolder> {
        Context context;
        ArrayList<User> users = new ArrayList<>();

        public SearchAdapter(Context ctx, ArrayList<User> users) {
            this.context = ctx;
            this.users = users;
        }

        @Override
        public SearchViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v  = LayoutInflater.from(context).inflate(R.layout.user_list_layout,parent,false);
            return new SearchViewHolder(v);
        }

        @Override
        public void onBindViewHolder(final SearchViewHolder holder, int position) {
                final User u = users.get(position);
                holder.setImage(u.getImage(),context);
                holder.setName(u.getName());

                holder.setEmail(u.getEmail());
                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void onClick(View view) {
                        Intent i =new Intent(SearchActivity.this,ProfileActivity.class);
                        Pair[] pairs = new Pair[3];
                        pairs[0] = new Pair<View,String>(holder.circleImageView,"imageTransition");
                        pairs[1] = new Pair<View,String>(holder.userName,"nameTransition");
                        pairs[2] = new Pair<View,String>(holder.statusView,"emailTransition");
                        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(SearchActivity.this,pairs);
                        i.putExtra("user_object",u);

                        startActivity(i,options.toBundle());
                    }
                });
        }

        @Override
        public int getItemCount() {
            return users.size();
        }
    }

    public static class SearchViewHolder extends RecyclerView.ViewHolder {

        View mView;
        TextView userName;
        TextView statusView;
        CircleImageView circleImageView;
        public SearchViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
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
