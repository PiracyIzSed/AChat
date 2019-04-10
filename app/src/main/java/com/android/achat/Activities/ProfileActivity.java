package com.android.achat.Activities;

import android.graphics.Color;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.icu.util.TimeZone;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.achat.DataModels.User;
import com.android.achat.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends BaseActivity {


    private User u;
    private ImageView profilePicture;
    private TextView displayName, displayStatus, totalFriends,displayEmail;
    private Button sendRequest,declineRequest;
    private String userId,currentId;
    private DatabaseReference dbrefRoot;

    private int friendshipStatus = -1; //-1= unrelated 0=request sent 1=received/pending request 2=friends
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setStatusBarColor(Color.parseColor("#ff000000"));
        }

        u = getIntent().getParcelableExtra("user_object");
        currentId= FirebaseAuth.getInstance().getCurrentUser().getUid();
        userId=u.getUid();

        profilePicture = (ImageView) findViewById(R.id.profile_photo);
        displayName = (TextView) findViewById(R.id.name);
        displayStatus = (TextView) findViewById(R.id.status);
        displayEmail=findViewById(R.id.email);
        totalFriends = (TextView) findViewById(R.id.total_friends);
        sendRequest = (Button) findViewById(R.id.send_request);
        declineRequest = (Button) findViewById(R.id.decline_request);

        dbrefRoot= FirebaseDatabase.getInstance().getReference();

        displayName.setText(u.getName());
        displayStatus.setText(u.getStatus());
        displayEmail.setText(u.getEmail());
        Picasso.with(this).load(u.getImage()).placeholder(R.drawable.default_avatar_profile).networkPolicy(NetworkPolicy.OFFLINE).into(profilePicture);

        getFriendshipStatus();

        declineRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                declineCurrentRequest();
            }
        });

        sendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendCurrentRequest();
            }
        });

        //get total friends

        dbrefRoot.child("Friends/"+userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChildren()) {
                    Long total = dataSnapshot.getChildrenCount();
                    totalFriends.setText(total.toString()+ " Friends");
                }else{
                    totalFriends.setText("0 Friends");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void sendCurrentRequest() {
        Map sendReq = new HashMap();

        sendRequest.setEnabled(false);

        switch (friendshipStatus) {
            case -1:
                DatabaseReference mNotifRef = dbrefRoot.child("Notifications/Request_Notifications").child(userId).push();
                String notificationId = mNotifRef.getKey();

                HashMap<String, String> notificationMap = new HashMap<>();
                notificationMap.put("from", currentId);
                notificationMap.put("type", "friend_request");


                sendReq.put("Friend_Requests/" + currentId + "/" + userId + "/request_type", "sent");
                sendReq.put("Friend_Requests/" + userId + "/" + currentId + "/request_type", "received");
                sendReq.put("Notifications/Request_Notifications/" + userId + "/" + notificationId, notificationMap);

                dbrefRoot.updateChildren(sendReq, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if (databaseError == null) {
                            sendRequest.setText("CANCEL FRIEND REQUEST");
                            friendshipStatus = 0;
                            Toast.makeText(ProfileActivity.this, "Friend Request Sent", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ProfileActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    }
                });
                break;
            case 0 :
                sendReq.put("Friend_Requests/" + currentId + "/" + userId + "/request_type", null);
                sendReq.put("Friend_Requests/" + userId + "/" + currentId + "/request_type", null);
                dbrefRoot.updateChildren(sendReq, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if (databaseError == null) {
                            sendRequest.setText("SEND FRIEND REQUEST");
                            friendshipStatus = -1;
                            Toast.makeText(ProfileActivity.this, "Friend Request Cancelled", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ProfileActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();

                        }

                    }
                });
            break;
            case 1 :final String currentDate = getToday();
                Map acceptReq = new HashMap();
                acceptReq.put("Friends/"+currentId+"/"+userId+"/date",currentDate);
                acceptReq.put("Friends/"+userId+"/"+currentId+"/date",currentDate);
                acceptReq.put("Friend_Requests/"+currentId+"/"+userId+"/request_type",null);
                acceptReq.put("Friend_Requests/"+userId+"/"+currentId+"/request_type",null);
                dbrefRoot.updateChildren(acceptReq, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if(databaseError==null) {
                            sendRequest.setText("Remove This Friend");
                            declineRequest.setVisibility(View.INVISIBLE);
                            declineRequest.setEnabled(false);
                            friendshipStatus = 2;
                            Toast.makeText(ProfileActivity.this, "Friend Added", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            Toast.makeText(ProfileActivity.this,databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                break;
            case 2 :Map removeReq = new HashMap();
                removeReq.put("Friends/"+currentId+"/"+userId,null);
                removeReq.put("Chat/"+currentId+"/"+userId,null);
                removeReq.put("Friends/"+userId+"/"+currentId,null);
                removeReq.put("Chat/"+userId+"/"+currentId,null);
                dbrefRoot.updateChildren(removeReq, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if (databaseError == null) {
                            friendshipStatus = -1;
                            sendRequest.setText("SEND FRIEND REQUEST");
                            Toast.makeText(ProfileActivity.this, "Friend Removed", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ProfileActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                break;
        }
        sendRequest.setEnabled(true);

    }

    private String getToday() {
        Date presentTime_Date = Calendar.getInstance().getTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("IST"));
        return dateFormat.format(presentTime_Date);
    }

    private void declineCurrentRequest() {
        Map declineReq = new HashMap();
        declineReq.put("Friend_Requests/"+currentId+"/"+userId+"/request_type",null);
        declineReq.put("Friend_Requests/"+userId+"/"+currentId+"/request_type",null);
        dbrefRoot.updateChildren(declineReq, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError == null) {
                    declineRequest.setVisibility(View.INVISIBLE);
                    declineRequest.setEnabled(false);
                    sendRequest.setText("SEND FRIEND REQUEST");
                    friendshipStatus = -1;
                    Toast.makeText(ProfileActivity.this, "Friend Request Declined", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(ProfileActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void getFriendshipStatus() {
        dbrefRoot.child("Friend_Requests").child(currentId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("ds:",dataSnapshot.toString()+"\n"+userId);

                if(dataSnapshot.hasChild(userId)){

                    String request_type = dataSnapshot.child(userId).child("request_type").getValue().toString();
                    if (request_type.equals("received")) {
                        friendshipStatus = 1;
                        sendRequest.setText("ACCEPT FRIEND REQUEST");
                        declineRequest.setVisibility(View.VISIBLE);
                        declineRequest.setEnabled(true);
                    } else if (request_type.equals("sent")) {
                        friendshipStatus = 0;
                        sendRequest.setText("CANCEL FRIEND REQUEST");
                    }
                } else {
                    dbrefRoot.child("Friends").child(currentId).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChild(userId)) {
                                sendRequest.setText("Remove This Friend");
                                friendshipStatus = 2;
                            }else {
                                friendshipStatus =-1;
                                sendRequest.setText("Send Friend Request");
                                declineRequest.setVisibility(View.INVISIBLE);
                            }
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
}
