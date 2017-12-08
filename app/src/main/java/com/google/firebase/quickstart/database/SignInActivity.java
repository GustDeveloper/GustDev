package com.google.firebase.quickstart.database;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.quickstart.database.models.Profile;
import com.google.firebase.quickstart.database.models.User;
import com.google.firebase.quickstart.database.models.UtilToast;

public class SignInActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "SignInActivity";

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    private EditText mEmailField;
    private EditText mPasswordField;
    private Button mSignInButton;
    private Button mSignUpButton;
    private Button mForgetPassword;

    private String message;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        // Views
        mEmailField = findViewById(R.id.field_email);
        mPasswordField = findViewById(R.id.field_password);

        mSignInButton = findViewById(R.id.button_sign_in);
        mSignUpButton = findViewById(R.id.button_sign_up);
        mForgetPassword=findViewById(R.id.button_forget_password);

        // Click listeners
        mSignInButton.setOnClickListener(this);
        mSignUpButton.setOnClickListener(this);
        mForgetPassword.setOnClickListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();

        if (getIntent().getExtras() != null) {
            Intent intent = new Intent(this, ChatActivity.class);
            Object roomkey = getIntent().getExtras().get("roomkey");
            Object  receiver = getIntent().getExtras().get("rec");
            intent.putExtra("Path",roomkey.toString());
            intent.putExtra("receiver",receiver.toString());
            startActivity(intent);
            finish();
        } else {
        // Check auth on Activity start
            if (mAuth.getCurrentUser() != null) {
                onAuthSuccess(mAuth.getCurrentUser());
            }
        }
    }

    private void signIn() {
        Log.d(TAG, "signIn");
        if (!validateForm()) {
            return;
        }

        showProgressDialog();
        String email = mEmailField.getText().toString();
        String password = mPasswordField.getText().toString();


        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signIn:onComplete:" + task.isSuccessful());
                        hideProgressDialog();

                        if (task.isSuccessful()) {
                            onAuthSuccess(task.getResult().getUser());
                        } else {
                            FirebaseAuthException e = (FirebaseAuthException)task.getException();
                            message = "Sign In Failed: " + e.getMessage();
                            UtilToast.showToast(SignInActivity.this, message);
                        }
                    }
                });
    }

    private void signUp() {
        Log.d(TAG, "signUp");
        if (!validateForm()) {
            return;
        }
        showProgressDialog();
        String email = mEmailField.getText().toString();
        String password = mPasswordField.getText().toString();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "createUser:onComplete:" + task.isSuccessful());
                        hideProgressDialog();

                        if (task.isSuccessful()) {
                            onAuthSuccess(task.getResult().getUser());
                        } else {
                            FirebaseAuthException e = (FirebaseAuthException)task.getException();
                            message = e.getMessage();
                            UtilToast.showToast(SignInActivity.this,message);
                        }
                    }
                });
    }

    public void forgetPassword(){

        if (TextUtils.isEmpty(mEmailField.getText().toString())) {
            mEmailField.setError("Required");

        } else {
            mEmailField.setError(null);
            String emailAddress = mEmailField.getText().toString();


            mAuth.sendPasswordResetEmail(emailAddress)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "Email sent.");
                                message = "Email sent.";
                            }
                        }
                    });
        }
    }


    private void onAuthSuccess(FirebaseUser user) {
        final String username = usernameFromEmail(user.getEmail());
        final String userId = user.getUid();
        final String email = user.getEmail();

        final String refreshedToken = FirebaseInstanceId.getInstance().getToken();

        // Write new user
        mDatabase.child("users").child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    writeNewUser(userId, username, email, refreshedToken);
                    writeNewUserProfile(userId, username, email);
                    createNewUserPair(userId);
                } else {
                    mDatabase.child("users").child(userId).child("token").setValue(refreshedToken);
                }

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // Go to MainActivity
        startActivity(new Intent(SignInActivity.this, MainActivity.class));
        finish();

    }

    private String usernameFromEmail(String email) {
        if (email.contains("@")) {
            return email.split("@")[0];
        } else {
            return email;
        }
    }

    private boolean validateForm() {
        boolean result = true;
        if (TextUtils.isEmpty(mEmailField.getText().toString())) {
            mEmailField.setError("Required");
            result = false;
        } else {
            mEmailField.setError(null);
        }

        if (TextUtils.isEmpty(mPasswordField.getText().toString())) {
            mPasswordField.setError("Required");
            result = false;
        } else {
            mPasswordField.setError(null);
        }

        return result;
    }

    // [START basic_write]
    private void writeNewUser(String userId, String name, String email, String token) {

        User user = new User(name, email, token);

        mDatabase.child("users").child(userId).setValue(user);
    }
    //
    private void createNewUserPair(String userId) {
        mDatabase.child("user-user").child(userId).setValue("");
    }

    // [END basic_write]

    //Todo: Something that retains the value of image/birthday/hobbies and etc


    private void writeNewUserProfile(String userId, String name, String email) {
        Profile profile  = new Profile(userId, name, email);
        mDatabase.child("profiles").child(userId).setValue(profile);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.button_sign_in) {
            signIn();
        } else if (i == R.id.button_sign_up) {
            signUp();
        }
        else if (i == R.id.button_forget_password) {
            forgetPassword();
        }
    }
}
