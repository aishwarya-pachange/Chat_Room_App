
package com.mymobileapps.firebasemodule10;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private FirebaseStorage storage;
    private TextView lbl_Text;
    private EditText txt_userName;
    private EditText txt_Password;
    private ProgressBar pb_Loading;

    private ImageView img_Upload;
    private Button btn_Login;

    private Button btn_SignUp;
    StorageReference ref;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // FirebaseApp.initializeApp(MainActivity.this);
        storage = FirebaseStorage.getInstance();
        mAuth = FirebaseAuth.getInstance();
        txt_userName = (EditText) findViewById(R.id.txtEmail);
        txt_Password = (EditText) findViewById(R.id.txtPassword);


        btn_Login = (Button) findViewById(R.id.btnLogin);
        btn_Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn(txt_userName.getText().toString(), txt_Password.getText().toString());
            }
        });

        btn_SignUp = (Button) findViewById(R.id.btnSignUp);
        btn_SignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Intent signupIntent = new Intent(MainActivity.this, SignUp.class);
                Log.d("demo", "signup intent");
                startActivity(signupIntent);
            }
        });

        pb_Loading = (ProgressBar) findViewById(R.id.pbLoading);
    }

    private void signIn(String email, String password) {

        Log.d("demo", "signIn:" + email);

        if (!validateForm()) {

            return;

        }


        //  showProgressDialog();
        pb_Loading.setVisibility(View.VISIBLE);

        // [START sign_in_with_email]

        mAuth.signInWithEmailAndPassword(email, password)

                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {

                    @Override

                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {

                            // Sign in success, update UI with the signed-in user's information
                            Log.d("demo", "signInWithEmail:success");


                            FirebaseUser user = mAuth.getCurrentUser();
                            UserProfileChangeRequest objProfile = new UserProfileChangeRequest.Builder()
                                    .build();
                            user.updateProfile(objProfile).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        //Toast.makeText(MainActivity.this, "Profile Updated", Toast.LENGTH_SHORT).show();
                                        finish();
                                        Intent myIntent = new Intent(MainActivity.this, ChatRoom.class);
                                        startActivity(myIntent);
                                    }
                                }
                            });

                            //  user.updateProfile({})
                            if (user != null) {

                                String name = user.getDisplayName();
                                String email = user.getEmail();
                                Uri photoUrl = user.getPhotoUrl();

                                String uid = user.getUid();  // The user's ID, unique to the Firebase project. Do NOT use
                                // this value to authenticate with your backend server, if
                                // you have one. Use User.getToken() instead.
                                Log.d("demo", "onComplete: " + name + " " + email);
                            }
                            // updateUI(user);

                        } else {

                            // If sign in fails, display a message to the user.

                            Log.w("demo", "signInWithEmail:failure", task.getException());

                            Toast.makeText(MainActivity.this, "Authentication failed.",

                                    Toast.LENGTH_SHORT).show();

                            //   updateUI(null);

                        }


                        // [START_EXCLUDE]

                        if (!task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "Authentication failed.",

                                    Toast.LENGTH_SHORT).show();
                            // mStatusTextView.setText(R.string.auth_failed);

                        }

                        // hideProgressDialog();
                        pb_Loading.setVisibility(View.INVISIBLE);

                        // [END_EXCLUDE]

                    }

                });

        // [END sign_in_with_email]


    }

    @Override
    protected void onStart() {
        super.onStart();
        pb_Loading.setVisibility(View.INVISIBLE);
        /*if (mAuth.getCurrentUser() != null) {
            finish();
            Intent myIntent = new Intent(MainActivity.this, ChatRoom.class);
            startActivity(myIntent);
        }*/
    }

    private boolean validateForm() {

        boolean valid = true;


        String email = txt_userName.getText().toString();

        if (TextUtils.isEmpty(email)) {

            txt_userName.setError("Required.");

            valid = false;

        } else {

            txt_userName.setError(null);

        }


        String password = txt_Password.getText().toString();

        if (TextUtils.isEmpty(password)) {

            txt_Password.setError("Required.");

            valid = false;

        } else {

            txt_Password.setError(null);

        }


        return valid;

    }

    //****** Reference

       /* lbl_Text = (TextView) findViewById(R.id.lblOverlayText);
        txt_Name = (EditText) findViewById(R.id.txtName);
        img_Upload = (ImageView) findViewById(R.id.imgUpload);
        btn_Upload = (Button) findViewById(R.id.btnUpload);

        txt_Name.addTextChangedListener(new InputTextWatcher());
        btn_Upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                img_Upload.setDrawingCacheEnabled(true);
                img_Upload.buildDrawingCache();

                Bitmap bitmap = img_Upload.getDrawingCache();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                img_Upload.setDrawingCacheEnabled(false);
                byte[] data = baos.toByteArray();

                String path = "myimages/" + UUID.randomUUID() + ".png";
                ref = storage.getReference(path);


                StorageMetadata mdata = new StorageMetadata.Builder()
                        .setCustomMetadata("txt", lbl_Text.getText().toString())
                        .build();
                UploadTask uploadTask = ref.putBytes(data, mdata);
                uploadTask.addOnSuccessListener(MainActivity.this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> u = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                        //ref.getDownloadUrl();
                        Log.d("demo", "onSuccess: " + u.toString());
                    }
                });
            }
        });*/


}

  /*  private class InputTextWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            lbl_Text.setText(s.toString());

        }
    }*/
//end reference


