package com.android.achat;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentCallbacks2;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by Gaurav on 07-03-2018.
 */

public class AcitvityLifeCycleHandler implements Application.ActivityLifecycleCallbacks,ComponentCallbacks2 {

    FirebaseAuth auth = FirebaseAuth.getInstance();
    DatabaseReference dbref = FirebaseDatabase.getInstance().getReference();

    @Override
    public void onConfigurationChanged(Configuration configuration) {

    }

    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {

    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {
        //app in foreground
        if(auth.getCurrentUser()!=null) {
            dbref.child(".info/connected").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.d("connection status",dataSnapshot.toString());

                    if (dataSnapshot.getValue(Boolean.class)) {
                        dbref.child("Users").child(auth.getCurrentUser().getUid()).child("online").onDisconnect().setValue("false");
                        dbref.child("Users").child(auth.getCurrentUser().getUid()).child("online").setValue("true");
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }

    @Override
    public void onTrimMemory(int i) {
        if(i==TRIM_MEMORY_UI_HIDDEN && auth.getCurrentUser()!=null){
            dbref.child("Users").child(auth.getCurrentUser().getUid()).child("online").setValue("false");
        }
    }

    @Override
    public void onLowMemory() {

    }
}
