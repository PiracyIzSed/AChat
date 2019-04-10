package com.android.achat.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.achat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import id.zelory.compressor.Compressor;

public class RegistrationActivity extends AppCompatActivity {

    private TextInputEditText email, name;
    private EditText pass, confirmPass;
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private Button submit;
    private Toolbar mToolbar;
    private DatabaseReference dbref;
    private ProgressBar mProgressBar;
    private ImageView mProfilePic;
    private ProgressDialog mProgressDialog;
    private Uri resultUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        mToolbar = (Toolbar) findViewById(R.id.reg_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Register");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        email = (TextInputEditText) findViewById(R.id.email_register);
        pass = (TextInputEditText) findViewById(R.id.password);
        name = (TextInputEditText) findViewById(R.id.name);
        confirmPass = (TextInputEditText) findViewById(R.id.confirm_password);
        submit = (Button) findViewById(R.id.register_button);
        mProgressBar = findViewById(R.id.progress_bar);
        mProfilePic = findViewById(R.id.profile_image);

        mProgressDialog= new ProgressDialog(this);
        mProgressDialog.setTitle("Creating Account");
        mProgressDialog.setMessage("Please Wait...");
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.setCancelable(false);


        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addUser();
                mProgressBar.setVisibility(View.VISIBLE);
            }
        });

        mProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity()
                            .setGuidelines(CropImageView.Guidelines.ON)
                            .setAspectRatio(1, 1)
                            .setMinCropWindowSize(500, 500)
                            .start(RegistrationActivity.this);
                }

        });


    }


    private void addUser() {

        final String userName = name.getText().toString().trim();
        final String e_mail = email.getText().toString().trim();
        final String password = pass.getText().toString().trim();
        String confirm = confirmPass.getText().toString().trim();


        if (!emailIsValid(e_mail)) {
            email.setError("Invalid E-Mail");
        } else if (userName.isEmpty()) {
            name.setError("Invalid Name");
        } else if (!passIsValid(password)) {
            Log.d("e", password);
            pass.setError("Password should have minimum 4 characters");
        } else if (!(confirm.equals(password))) {
            confirmPass.setError("Passwords Do Not Match");
            confirmPass.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {


                }

                @Override
                public void afterTextChanged(Editable s) {
                    String confirmpass = confirmPass.getText().toString().trim();

                    Log.d("pass", password + "\n" + confirmpass);

                    if (!(confirmpass.equals(password))) {
                        confirmPass.setError("Passwords Do Not Match");
                    }

                }
            });
        } else {

            mProgressDialog.show();

            Log.d("email", e_mail + password + userName);

            auth.createUserWithEmailAndPassword(e_mail, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {

                        final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                        final String uid = currentUser.getUid();
                        String displayName = userName;

                        String deviceToken = FirebaseInstanceId.getInstance().getToken();

                        UserProfileChangeRequest addName = new UserProfileChangeRequest.Builder().setDisplayName(displayName).build();
                        currentUser.updateProfile(addName);
                        dbref = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

                        HashMap<String, String> userDetails = new HashMap<String, String>();
                        userDetails.put("name", displayName);
                        userDetails.put("email", e_mail);
                        userDetails.put("status", "Hey There! I am using AChat");
                        userDetails.put("image", "default");
                        userDetails.put("thumbnail", "default");
                        userDetails.put("device_token", deviceToken);
                        dbref.setValue(userDetails).addOnCompleteListener(RegistrationActivity.this, new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    if (resultUri != null) {
                                        uploadImage(resultUri, uid);
                                    }
                                    auth.getCurrentUser().sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(RegistrationActivity.this, "Verification Mail Sent to : "+auth.getCurrentUser().getEmail(), Toast.LENGTH_SHORT).show();
                                            mProgressDialog.dismiss();
                                            startActivity(new Intent(RegistrationActivity.this, MainActivity.class));
                                            finish();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(RegistrationActivity.this, "Failed to send verification mail to:"+currentUser.getEmail()+"\n"+e.getMessage(), Toast.LENGTH_SHORT).show();
                                            mProgressDialog.dismiss();
                                            startActivity(new Intent(RegistrationActivity.this, MainActivity.class));
                                            finish();
                                        }
                                    });

                                } else {
                                    Toast.makeText(RegistrationActivity.this, "Error Registering Your Details... Try Again Later", Toast.LENGTH_SHORT).show();
                                    mProgressDialog.dismiss();
                                }
                            }


                        });

                    } else {
                        Toast.makeText(RegistrationActivity.this, "Error Registering Your Account... Try Again Later", Toast.LENGTH_SHORT).show();
                        mProgressDialog.dismiss();

                    }
                }
            });

        }
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
                                    }
                                });
                            } else {
                                Toast.makeText(RegistrationActivity.this, "Error Uploading Thumbnail", Toast.LENGTH_SHORT).show();
                                mProgressDialog.dismiss();
                            }

                        }
                    });
                } else {
                    Toast.makeText(RegistrationActivity.this, "Error Uploading Image", Toast.LENGTH_SHORT).show();
                    mProgressDialog.dismiss();
                }
            }
        });

    }


    private boolean emailIsValid(String email) {
        String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();

    }

    private boolean passIsValid(String pass) {
        if (pass.length() > 4) return true;
        else return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                resultUri = result.getUri();
                mProfilePic.setImageURI(resultUri);
            }
        }
    }

}
