/*
 *************** ASSIGNMENT# - In Class Assignment 09 ***************
 *************** FILE NAME - SignUp.java ***************
 *************** FULL NAME - Aishwarya Nandkumar Pachange & Janani Krishnan (Group 18) ***************
 */
package com.mymobileapps.firebasemodule10;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.FirebaseDatabase;

import java.io.BufferedReader;

public class SignUp extends AppCompatActivity {

    private Button btn_SignUp;
    private FirebaseAuth mAuth;
    private EditText txt_fname;
    private EditText txt_lname;
    private EditText txt_email;
    private Button btn_Cancel;
    private EditText txt_pwd;
    private EditText txt_repeatpwd;
    private ProgressBar pb_Load;
    FirebaseDatabase database = FirebaseDatabase.getInstance();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        setTitle("Sign Up");
        mAuth = FirebaseAuth.getInstance();
        txt_email = (EditText) findViewById(R.id.emailEditText);

        txt_pwd = (EditText) findViewById(R.id.passEditText);

        txt_repeatpwd = (EditText) findViewById(R.id.confirmPassEditText);
        txt_fname = (EditText) findViewById(R.id.fnameEditText);

        txt_lname = (EditText) findViewById(R.id.lnameEditText);

        btn_SignUp = (Button) findViewById(R.id.realSignupButton);
        btn_SignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAccount(txt_email.getText().toString(),txt_pwd.getText().toString());
            }
        });

        btn_Cancel = (Button) findViewById(R.id.cancelButton);
        btn_Cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth = null;
                finish();
                Intent loginIntent = new Intent(SignUp.this,MainActivity.class);
                startActivity(loginIntent);
            }
        });

        pb_Load = (ProgressBar) findViewById(R.id.pbLoad);
    }

    private void createAccount(String email, String password) {

        Log.d("demo", "createAccount:" + email);

        if (!validateForm()) {

            return;

        }

//updateprofilechangerequest

       // showProgressDialog();

pb_Load.setVisibility(View.VISIBLE);

        // [START create_user_with_email]

        mAuth.createUserWithEmailAndPassword(email, password)

                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {

                    @Override

                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {

                            // Sign in success, update UI with the signed-in user's information

                            Log.d("demo", "createUserWithEmail:success");

                            FirebaseUser user = mAuth.getCurrentUser();
                            UserProfileChangeRequest objProfile = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(txt_fname.getText().toString() + " " + txt_lname.getText().toString())
//                                    .setPhotoUri("")
                                    .build();
                            user.updateProfile(objProfile).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Log.d("demo", "onComplete: " + mAuth.getCurrentUser().getDisplayName());
                                        //Toast.makeText(SignUp.this, "Profile Updated", Toast.LENGTH_SHORT).show();
                                        finish();
                                        Intent myIntent = new Intent(SignUp.this, ChatRoom.class);
                                        startActivity(myIntent);
                                    }
                                }
                            });
                           /* FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                String name = user.getDisplayName();
                                String email = user.getEmail();
                                Uri photoUrl = user.getPhotoUrl();
                               //String emailVerified = user.ver;
                                String uid = user.getUid();  // The user's ID, unique to the Firebase project. Do NOT use
                                // this value to authenticate with your backend server, if
                                // you have one. Use User.getToken() instead.

                                finish();
                                Intent myIntent = new Intent(SignUp.this, ChatRoom.class);
                                startActivity(myIntent);
                            }*/
                            //updateUI(user);

                        } else {

                            // If sign in fails, display a message to the user.

                            Log.w("demo", "createUserWithEmail:failure", task.getException());

                            Toast.makeText(SignUp.this, "Authentication failed.",

                                    Toast.LENGTH_SHORT).show();

                          //  updateUI(null);

                        }



                        // [START_EXCLUDE]

                 //       hideProgressDialog();
                        pb_Load.setVisibility(View.INVISIBLE);

                        // [END_EXCLUDE]

                    }

                });

        // [END create_user_with_email]

    }

    @Override
    protected void onStart() {
        super.onStart();
        pb_Load.setVisibility(View.INVISIBLE);
        if(mAuth.getCurrentUser() == null) {

            finish();
            Intent myIntent = new Intent(SignUp.this, MainActivity.class);
            startActivity(myIntent);
        }
    }

    private boolean validateForm() {

        boolean valid = true;
        String email = txt_email.getText().toString();
        if (TextUtils.isEmpty(email)) {
            txt_email.setError("Required.");
            valid = false;
        } else {
            txt_email.setError(null);
        }

        String first = txt_fname.getText().toString();

        if (TextUtils.isEmpty(first)) {
            txt_fname.setError("Required.");
            valid = false;
        } else {
            txt_fname.setError(null);
        }

        String last = txt_lname.getText().toString();

        if (TextUtils.isEmpty(last)) {
            txt_lname.setError("Required.");
            valid = false;
        } else {
            txt_lname.setError(null);
        }

        String confirmPswd = txt_repeatpwd.getText().toString();


        if (TextUtils.isEmpty(confirmPswd)) {
            txt_repeatpwd.setError("Required.");
            valid = false;
        } else {
            txt_repeatpwd.setError(null);
        }

        String password = txt_pwd.getText().toString();
        if (TextUtils.isEmpty(password)) {
            txt_pwd.setError("Required.");
            valid = false;
        } else {
            txt_pwd.setError(null);
        }
        if(password.trim().length() < 6 || confirmPswd.trim().length() <6)
        {            txt_pwd.setError("Minimum 6 charecters.");
            valid = false;
        }else {
            txt_pwd.setError(null);
        }

        if(!(password.trim().equals(confirmPswd.trim())))
        {            txt_pwd.setError("Passwords mismatch!");
            valid = false;
        }else {
            txt_pwd.setError(null);
        }
        return valid;
    }
}
