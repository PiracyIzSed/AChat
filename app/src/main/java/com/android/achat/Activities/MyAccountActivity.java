package com.android.achat.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.achat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class MyAccountActivity extends BaseActivity {

    private final String TAG =MyAccountActivity.class.getName();
    ImageButton settings;
    Uri resultUri;
    private DatabaseReference dbref;
    private FirebaseUser currentUser;
    private TextView displayName,displayStatus;
    private CircleImageView profilePic;
    private Button changeStatus,changeProfilePic;

    StorageReference mStorageRef;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_account);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        dbref = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUser.getUid());
        dbref.keepSynced(true);

        mStorageRef = FirebaseStorage.getInstance().getReference();

        settings=findViewById(R.id.account_settings);
        profilePic = (CircleImageView) findViewById(R.id.profile_picture);
        displayName = (TextView) findViewById(R.id.display_name);
        displayStatus = (TextView) findViewById(R.id.profile_status);
        changeStatus=(Button) findViewById(R.id.change_status);
        changeProfilePic=(Button) findViewById(R.id.change_profile_pic);

        mProgressDialog= new ProgressDialog(this);
        mProgressDialog.setTitle("Uploading Image");
        mProgressDialog.setMessage("Please Wait...");
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.setCancelable(false);

        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            getWindow().setStatusBarColor(Color.parseColor("#00000000"));
        }

        if(auth.getCurrentUser()!=null)
        getDetails();

        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MyAccountActivity.this,AccountSettingsActivity.class));
                finish();
            }
        });
        

        changeStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String status = displayStatus.getText().toString();
                Intent i = new Intent(MyAccountActivity.this,StatusActivity.class);

                i.putExtra("current_status",status);
                startActivity(i);
            }
        });

        changeProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    CropImage.activity()
                            .setGuidelines(CropImageView.Guidelines.ON)
                            .setAspectRatio(1, 1)
                            .setMinCropWindowSize(500, 500)
                            .start(MyAccountActivity.this);
                }

        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                resultUri = result.getUri();
                profilePic.setImageURI(resultUri);
                mProgressDialog.show();
                uploadImage(resultUri,currentUser.getUid());
            }else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Log.d(TAG,error.getMessage());
            }
        }
    }

    private void getDetails() {
        dbref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (auth.getCurrentUser()!=null) {
                    String name = dataSnapshot.child("name").getValue().toString();
                    String status = dataSnapshot.child("status").getValue().toString();
                    final String image = dataSnapshot.child("image").getValue().toString();

                    displayName.setText(name);
                    displayStatus.setText(status);
                    if (!image.equals("default")) {
                        Picasso.with(MyAccountActivity.this).load(image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.default_avatar_profile).into(profilePic, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError() {
                                Picasso.with(MyAccountActivity.this).load(image).placeholder(R.drawable.default_avatar_profile).into(profilePic);
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG,databaseError.getMessage());
            }
        });
    }

    private void uploadImage(Uri resultUri, String uid) {

        StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();
        File thumb_file = new File(resultUri.getPath());
        Bitmap thumb_bmp = null;
        Log.d("Uri :", resultUri.getPath());

        try {
            thumb_bmp = new Compressor(this).
                    setMaxWidth(200).
                    setMaxHeight(200).
                    setQuality(75).
                    compressToBitmap(thumb_file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        thumb_bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        final byte[] thumb_byte = baos.toByteArray();


        StorageReference filePath = mStorageRef.child("profile_imgs").child(uid + ".jpg");
        final StorageReference thumbFilepath = mStorageRef.child("profile_imgs").child("profile_thumbs").child(uid.toString() + ".jpg");

        filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    final String imageURL = task.getResult().getDownloadUrl().toString();

                    UploadTask uploadTask = thumbFilepath.putBytes(thumb_byte);
                    uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) {
                            String thumbURL = thumb_task.getResult().getDownloadUrl().toString();
                            if (thumb_task.isSuccessful()) {

                                Map uploadMap = new HashMap();
                                uploadMap.put("image", imageURL);
                                uploadMap.put("thumbnail", thumbURL);

                                dbref.updateChildren(uploadMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Toast.makeText(MyAccountActivity.this, "Image successfully uploaded", Toast.LENGTH_SHORT).show();
                                        mProgressDialog.dismiss();
                                    }
                                });
                            } else {
                                Toast.makeText(MyAccountActivity.this, "Error Uploading Thumbnail", Toast.LENGTH_SHORT).show();
                                mProgressDialog.dismiss();
                            }

                        }
                    });
                } else {
                    Toast.makeText(MyAccountActivity.this, "Error Uploading Image", Toast.LENGTH_SHORT).show();
                    mProgressDialog.dismiss();
                }
            }
        });

    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(MyAccountActivity.this,MainActivity.class));
        finish();
    }
}
