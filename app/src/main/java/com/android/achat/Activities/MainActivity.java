package com.android.achat.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.android.achat.Fragments.FragmentAdapter;
import com.android.achat.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends BaseActivity {

    DatabaseReference dbrefUser;
    FirebaseAuth auth;
    Toolbar mToolbar;
    FragmentAdapter fgAdapter;
    ViewPager viewPager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dbrefUser = FirebaseDatabase.getInstance().getReference("Users");
        auth=FirebaseAuth.getInstance();
        if(auth.getCurrentUser()==null){
            startActivity(new Intent(MainActivity.this,LoginActivity.class));
            finish();
        }else {
            setContentView(R.layout.activity_main);

            if (!auth.getCurrentUser().isEmailVerified()) {
                Toast.makeText(this, "Email Unverified Please Verify It!!\n You will not be able avail all the features ", Toast.LENGTH_LONG).show();
            }

            mToolbar = findViewById(R.id.main_app_bar);
            setSupportActionBar(mToolbar);

            fgAdapter=new FragmentAdapter(getSupportFragmentManager());
            viewPager = findViewById(R.id.tab_pager);
            viewPager.setAdapter(fgAdapter);
            viewPager.setCurrentItem(1,true);
            getSupportActionBar().setTitle("Chats");
            viewPager.setOffscreenPageLimit(2);
            viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    switch(position){
                        case 0: getSupportActionBar().setTitle("Requests");
                                break;
                        case 1: getSupportActionBar().setTitle("Chats");
                                break;
                        case 2: getSupportActionBar().setTitle("Friends");
                                break;
                        default: getSupportActionBar().setTitle("Chats");
                    }
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });

                   }

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.sign_out :
                startActivity(new Intent(getApplicationContext(),LoginActivity.class));
                Map delToken = new HashMap<>();
                delToken.put("device_token",null);
                dbrefUser.child(auth.getCurrentUser().getUid()).updateChildren(delToken).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        auth.signOut();
                        finish();
                    }
                });
                break;
            case R.id.settings:
                startActivity(new Intent(getApplicationContext(),MyAccountActivity.class));
                finish();
                break;
            case R.id.search_people:
                startActivity(new Intent(getApplicationContext(),SearchActivity.class));
                break;
        }
        return  true;
    }

    @Override
    public void onBackPressed() {

        finish();
    }

    public void findFriends(View v){
        Intent i = new Intent(MainActivity.this,SearchActivity.class);
        startActivity(i);

    }


}
