package com.android.achat.Activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.achat.R;
import com.android.achat.SharedPrefs;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;


public class AccountSettingsActivity extends BaseActivity {

    TextInputLayout newPassword;
    FirebaseAuth auth=FirebaseAuth.getInstance();
    Switch darkSwitch;
    SharedPrefs sharedPrefs;
    TextView verifyText,emailAddress;
    ImageView refresh,verifyStatus;
    Button resendVerify,resetPass;
    ProgressBar[] mProgressBar = new ProgressBar[2];
    int passWizStatus=0;
    Toolbar mToolbar;
    String oldPass=null,newPass=null;
    LinearLayout disclaimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

       sharedPrefs = new SharedPrefs(this,auth.getCurrentUser().getUid());

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_settings);

        darkSwitch = findViewById(R.id.dark_switch);
        verifyText = findViewById(R.id.text_verify);
        refresh=findViewById(R.id.refresh);
        verifyStatus=findViewById(R.id.verify_status);
        resendVerify=findViewById(R.id.resend_verification);
        mProgressBar[0]=findViewById(R.id.refresh_bar);
        mProgressBar[1]=findViewById(R.id.reset_bar);
        resetPass=findViewById(R.id.reset_pass);
        newPassword=findViewById(R.id.password);
        mToolbar=findViewById(R.id.account_settings_appbar);
        emailAddress=findViewById(R.id.email_address);
        disclaimer=findViewById(R.id.verify_disclaimer);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Account Settings");

        emailAddress.setText(auth.getCurrentUser().getEmail());
        checkVerification();

        if(sharedPrefs.isNightModeEnabled()){
            darkSwitch.setChecked(true);
        }

        darkSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (darkSwitch.isEnabled()) {
                    if (b) {

                        sharedPrefs.setNightMode(true);
                        restart();
                    } else {
                        sharedPrefs.setNightMode(false);
                        restart();
                    }
                }
                else{
                    Toast.makeText(AccountSettingsActivity.this,"Please verify your e-mail to enable this feature",Toast.LENGTH_SHORT).show();
                }
            }

        });

        resendVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mProgressBar[0].setVisibility(View.VISIBLE);

                auth.getCurrentUser().sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        mProgressBar[0].setVisibility(View.GONE);
                        Toast.makeText(AccountSettingsActivity.this,"Email Resent ",Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        mProgressBar[0].setVisibility(View.GONE);
                        Toast.makeText(AccountSettingsActivity.this,"An error occured while sending verification mail\n "+e.getMessage(),Toast.LENGTH_SHORT).show();
                        Log.d("mail verify error:",e.getMessage());
                    }
                });

            }
        });

        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mProgressBar[0].setVisibility(View.VISIBLE);
                auth.getCurrentUser().reload().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        checkVerification();
                        mProgressBar[0].setVisibility(View.GONE);
                    }
                });
            }
        });

        resetPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(resetPass.isEnabled()){
                    if(passWizStatus==0){
                        resetPass.setText("Next");
                        newPassword.setVisibility(View.VISIBLE);
                        passWizStatus=1;
                    }
                    else if(passWizStatus==1){
                        mProgressBar[1].setVisibility(View.VISIBLE);
                        oldPass =newPassword.getEditText().getText().toString();
                        if(!TextUtils.isEmpty(oldPass)) {
                            AuthCredential authCredential = EmailAuthProvider.getCredential(auth.getCurrentUser().getEmail(), oldPass);
                            auth.getCurrentUser().reauthenticate(authCredential).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    newPassword.getEditText().setText("");
                                    mProgressBar[1].setVisibility(View.GONE);
                                    newPassword.setHint("Enter New Password");
                                    resetPass.setText("Confirm New Password");
                                    passWizStatus = 2;

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    mProgressBar[1].setVisibility(View.GONE);

                                    Toast.makeText(AccountSettingsActivity.this, "wrong credentials", Toast.LENGTH_SHORT).show();

                                }
                            });
                        }else {
                            mProgressBar[1].setVisibility(View.GONE);
                            newPassword.setError("Old Password cannot be empty");
                        }
                    } else if (passWizStatus == 2) {
                        mProgressBar[1].setVisibility(View.VISIBLE);
                        newPass=newPassword.getEditText().getText().toString();
                        if(!TextUtils.isEmpty(newPass)){
                            Log.d("new , old pass:",newPass+" "+oldPass);
                            if(!newPass.equals(oldPass)){
                                auth.getCurrentUser().updatePassword(newPass).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        mProgressBar[1].setVisibility(View.GONE);
                                        passWizStatus=0;
                                        Toast.makeText(AccountSettingsActivity.this,"Password Reset Successful.Please Re-Login !",Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(AccountSettingsActivity.this,LoginActivity.class));
                                        auth.signOut();
                                        finish();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        mProgressBar[1].setVisibility(View.GONE);
                                        Toast.makeText(AccountSettingsActivity.this,"An error occurred while resetting password",Toast.LENGTH_SHORT).show();
                                    }
                                });

                            }
                            else{
                                mProgressBar[1].setVisibility(View.GONE);
                                newPassword.setError("Old Password And New Password Cannot Be Same");
                            }
                        }
                        else {
                            mProgressBar[1].setVisibility(View.GONE);
                            newPassword.setError("New Password Cannot Be Empty");
                        }

                    }

                }
                else{
                    Toast.makeText(AccountSettingsActivity.this,"Please verify your e-mail to enable this feature",Toast.LENGTH_SHORT).show();
                }
            }
        });

       /* deactivateAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder= new AlertDialog.Builder(AccountSettingsActivity.this);
                builder.setTitle("Deactivate Account").setMessage("Disclaimer :  THIS ACTION CANNOT BE UNDONE \n All your chats and records will be deleted. \n Are you sure you want to Deactivate ? ");
                builder.setPositiveButton("DEACTIVATE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        String uid = auth.getCurrentUser().getUid();
                        DatabaseReference dbrefRoot = FirebaseDatabase.getInstance().getReference();
                        Map deleteDetails = new HashMap();
                        deleteDetails.put("Users/" + uid+"/", null);
                        deleteDetails.put("Friend_Requests/" + uid+"/", null);
                        deleteDetails.put("Chats/" + uid+"/", null);
                        deleteDetails.put("Friends/" + uid+"/", null);

                        dbrefRoot.updateChildren(deleteDetails);
                        auth.getCurrentUser().delete();
                        Toast.makeText(AccountSettingsActivity.this, "Account Deactivated Successfully ", Toast.LENGTH_SHORT).show();
                        auth.signOut();
                        startActivity(new Intent(AccountSettingsActivity.this,LoginActivity.class));
                        finish();

                    }
                }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                AlertDialog ad= builder.create();
                ad.show();
            }
        });*/
    }

    private void checkVerification() {
        if(auth.getCurrentUser().isEmailVerified()){
            verifyText.setText("Your Account Is Verified");
            verifyStatus.setImageResource(R.drawable.ic_confirm);
            refresh.setVisibility(View.INVISIBLE);
            resendVerify.setVisibility(View.GONE);
            darkSwitch.setEnabled(true);
            resetPass.setEnabled(true);
            disclaimer.setVisibility(View.GONE);
        }
        else{
            darkSwitch.setEnabled(false);
            resetPass.setEnabled(false);
            disclaimer.setVisibility(View.VISIBLE);
        }
    }

    private void restart() {
        Intent i = new Intent(getApplicationContext(),AccountSettingsActivity.class);
        startActivity(i);
        finish();
    }



    @Override
    public void onBackPressed() {
        Intent i = new Intent(this,MainActivity.class);
        startActivity(i);
        finish();
    }
}
