package com.android.achat.Activities;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.BoringLayout;
import android.util.Log;

import com.android.achat.R;
import com.android.achat.SharedPrefs;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by Gaurav on 27-01-2018.
 */

public class BaseActivity extends AppCompatActivity {
    SharedPrefs sharedPrefs;
    FirebaseAuth auth = FirebaseAuth.getInstance();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        if(auth.getCurrentUser()!=null) {
            String currentUser = auth.getCurrentUser().getUid();
            sharedPrefs = new SharedPrefs(this, currentUser);
            setDayNight();
        }
        else{
            startActivity(new Intent(this,LoginActivity.class));
        }
        super.onCreate(savedInstanceState);
    }
    private void setDayNight() {
        if(sharedPrefs.isNightModeEnabled()){
            setTheme(R.style.DarkTheme);
        }
        else{
            setTheme(R.style.LightTheme);
        }

    }
}
