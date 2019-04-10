package com.android.achat.Activities;

import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.achat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusActivity extends BaseActivity {

    private Toolbar mToolbar;
    private TextInputLayout statusText;
    private Button saveStatus;
    private FirebaseUser currentUser= FirebaseAuth.getInstance().getCurrentUser();
    private ProgressBar mProgressBar;

    private DatabaseReference dbref = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUser.getUid());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        mToolbar = (Toolbar) findViewById(R.id.appbar_status);
        statusText = (TextInputLayout) findViewById(R.id.status_text);
        saveStatus = (Button) findViewById(R.id.save_status);
        mProgressBar=findViewById(R.id.status_change_bar);


        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String currentStatus = getIntent().getStringExtra("current_status");

        statusText.getEditText().setText(currentStatus);

        saveStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgressBar.setVisibility(View.VISIBLE);
                String status = statusText.getEditText().getText().toString();

                dbref.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            mProgressBar.setVisibility(View.GONE);
                            Toast.makeText(StatusActivity.this,"Status Changed",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }
}
