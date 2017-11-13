package com.google.firebase.quickstart.database;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.quickstart.database.models.Profile;
import com.google.firebase.quickstart.database.models.UtilToast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/*
    1. load one image from gallery (verified)
    2. capture one image from camera (verified)
    3. upload one image to Firebase
    4. Handle image upload/load/capture in the background
    5. modify user profile by clicking FAB.
    6. download profile data (images, strings from server)
    7. check if data is stored locally.  * optional
 */

public class ProfileActivity extends BaseActivity implements View.OnClickListener,PopupMenu.OnMenuItemClickListener {

    /*UI elements*/
    ImageView profileImageView;
    FloatingActionButton editFab;
    EditText nicknameEditText;
    EditText phoneEditText;
    EditText locationEditText;
    EditText birthdayEditText;

    String nickname;
    String phone;
    String location;
    String birthday;
    String image;

    CollapsingToolbarLayout collapsingToolbarLayout;


    /*Maximum Byte Size Is 10MB*/
    int MAX_IMG_BYTE_SIZE = 10485760;
    private static final String IMAGE_DIRECTORY = "/GustImg";

    /*Gallery Request Code*/
    int REQUEST_IMAGE_STORAGE = 3;
    int REQUEST_IMAGE_CAPTURE = 2;

    /*String Messages*/
    String toastMessage;

    /*Databse Reference*/
    private DatabaseReference mDatabase;
    private DatabaseReference profileRef;
    private DatabaseReference imgRef;

    /*Profile*/
    private String userID;
    private Profile profile;


    /*Misc*/
    private static String TAG = "quickstart.database.ProfileActivity";
    private boolean editEnabled = false;
    private Handler handler = new Handler();
    private Handler profileHandler = new Handler();
    private Runnable runnable;
    private Runnable profileRunnable;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Toolbar toolbar =  findViewById(R.id.toolbar);
        setSupportActionBar(toolbar); //replace the old action bar with toolbar

        editFab = findViewById(R.id.editFab);
        editFab.setOnClickListener(this);

        profileImageView = findViewById(R.id.profileImageView);
        profileImageView.setOnClickListener(this);

        collapsingToolbarLayout = findViewById(R.id.toolbar_layout);


        nicknameEditText = findViewById(R.id.nicknameEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        locationEditText = findViewById(R.id.locationEditText);
        birthdayEditText = findViewById(R.id.birthdayEditText);
    }

    @Override
    protected void onStart() {
        super.onStart();
        /*get database reference*/
        mDatabase = FirebaseDatabase.getInstance().getReference();
        userID = getUid();
        profileRef = mDatabase.child("profiles").child(userID);
        imgRef = profileRef.child("image");

        getProfileFromServer(profileRef);
    }

    @Override
    public void onClick(View view){
        switch (view.getId()) {
            case R.id.profileImageView:
                showPopup(view);
                break;
            case R.id.editFab:
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                editEnabled = !editEnabled;
                if (editEnabled) {
                    enabledEdit();
                } else {
                    disableEditAndSaveToServer();
                }
                break;
            default:
                break;
        }
    }

    public void showPopup(View view) {
        PopupMenu popupMenu = new PopupMenu(this,view);
        MenuInflater menuInflater = popupMenu.getMenuInflater();
        menuInflater.inflate(R.menu.popup_userprofileimage,popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(this);
        popupMenu.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        switch (menuItem.getItemId()){
            case R.id.action_camera:
                //do stuff
                Log.e(TAG, "You clicked camera");
                if (hasCamera()) {
                    if (checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                            checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ) {
                        requestPermissions(new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_IMAGE_CAPTURE);
                    } else {
                        launchCamera();
                    }
                }

                return true;
            case R.id.action_storage:
                Log.e(TAG, "You clicked storage");
                if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_IMAGE_STORAGE);
                } else {
                    getImageFromStorage();
                }
                return true;
            default:
                return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_IMAGE_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getImageFromStorage();
            }
        } else if (requestCode == REQUEST_IMAGE_CAPTURE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchCamera();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        final Intent imgData = data;
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == REQUEST_IMAGE_STORAGE) {
                runnable = new Runnable() {
                    @Override
                    public void run() {
                        onSelectFromGalleryResult(imgData);
                    }
                };
                new Thread(runnable).start();
            } else if (requestCode == REQUEST_IMAGE_CAPTURE) {
                runnable = new Runnable() {
                    @Override
                    public void run() {
                        onCaptureImageResult(imgData);
                    }
                };
                new Thread(runnable).start();
            }
        }
    }

    public void onCaptureImageResult(final Intent data) {
       Runnable imgRunnable = new Runnable() {
           @Override
           public void run() {
               showProgressDialog();
               Bitmap bitmap = (Bitmap) data.getExtras().get("data");

               if (bitmap == null) {
                   toastMessage  = "Failed to process camera image data";
                   UtilToast.showToast(ProfileActivity.this, toastMessage);
                   return;
               }

               ByteArrayOutputStream stream = new ByteArrayOutputStream();
               bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);


               profileImageView.setImageBitmap(null);
               profileImageView.setImageBitmap(bitmap);
               hideProgressDialog();

               byte[] byteArray = stream.toByteArray();
               String path = saveImageIntoFolder(byteArray);
               upLoadImageToServer(byteArray);

               toastMessage  = "Image is uploaded and saved to " + path;
               UtilToast.showToast(ProfileActivity.this, toastMessage);
           }
       };

       handler.post(imgRunnable);

    }
    public void onSelectFromGalleryResult(final Intent data) {

        Runnable imgRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    showProgressDialog();
                    Uri selectedImage = data.getData();
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(ProfileActivity.this.getContentResolver(), selectedImage);

                    //Todo: what is blocking the UI thread, do it in the background thread
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);


                    profileImageView.setImageBitmap(null);
                    profileImageView.setImageBitmap(bitmap);

                    byte[] byteArray = stream.toByteArray();

                    /*If image is way too large, reject it*/
                    if(byteArray.length >= MAX_IMG_BYTE_SIZE){
                        toastMessage  = "The image selected exceeds size limit";
                        UtilToast.showToast(ProfileActivity.this, toastMessage);
                    } else {
                        upLoadImageToServer(byteArray);
                        saveImageIntoFolder(byteArray);
                    }
                    hideProgressDialog();

                }catch(IOException e){
                    e.printStackTrace();
                }
            }
        };

        handler.post(imgRunnable);
    }


    public void getImageFromStorage(){
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_IMAGE_STORAGE);
    }

    private boolean hasCamera(){
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY);
    }

    public void launchCamera(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //Take a picture and pass results along to onActivityResult
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
    }

    public String saveImageIntoFolder(byte[] byteArray) {

        File wallpaperDirectory = new File(
                Environment.getExternalStorageDirectory() + IMAGE_DIRECTORY);
        if (!wallpaperDirectory.exists()) {
            wallpaperDirectory.mkdirs();
        }

        try {
            File f = new File(wallpaperDirectory, Calendar.getInstance()
                    .getTimeInMillis() + ".jpg");

            f.createNewFile();
            FileOutputStream fo = new FileOutputStream(f);
            fo.write(byteArray);

            MediaScannerConnection.scanFile(this,
                    new String[]{f.getPath()},
                    new String[]{"image/jpeg"}, null);
            fo.close();
            Log.d("TAG", "File Saved::--->" + f.getAbsolutePath());
            return f.getAbsolutePath();

        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return "";
    }

    public void upLoadImageToServer(byte[] byteArray) {
        //TODO: this is needs to verify.
        String imageEncoded = Base64.encodeToString(byteArray, Base64.DEFAULT);
        imgRef = profileRef.child("image");
        imgRef.setValue(imageEncoded);
        //TODO: Might change it to setValueAsync with Completion Callback, return boolean to indicate the status of uploading.
    }

    public void enabledEdit() {
        editFab.setImageResource(R.drawable.content_save_settings);
        nicknameEditText.setEnabled(true);
        phoneEditText.setEnabled(true);
        locationEditText.setEnabled(true);
        birthdayEditText.setEnabled(true);
    }

    public void disableEditAndSaveToServer() {
        editFab.setImageResource(R.drawable.ic_image_edit);
        nicknameEditText.setEnabled(false);
        phoneEditText.setEnabled(false);
        locationEditText.setEnabled(false);
        birthdayEditText.setEnabled(false);

        //Todo: update to the server;
        nickname = nicknameEditText.getText().toString();
        location = locationEditText.getText().toString();
        birthday = birthdayEditText.getText().toString();
        phone = phoneEditText.getText().toString();

        if (validateString(nickname)) {
            profile.setNickname(nickname);
        }

        if (validateString(phone)) {
            profile.setLocation(phone);
        }

        if (validateString(location)) {
            profile.setPhone(location);
        }

        if (validateString(birthday)) {
            profile.setBirthday(birthday);
        }

        updateProfileView();

        /*Update to Firebase*/
        profileRef.setValue(profile, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError != null) {
                    Log.e(TAG, "Failed to update user information, please try again");
                    toastMessage = "Failed to update user information, please try again";
                    UtilToast.showToast(ProfileActivity.this, toastMessage);
                }
            }

        });

        /*Optional: Update to local DB*/
    }

    public void getProfileFromServer(final DatabaseReference profileRef) {
        profileRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                profile = dataSnapshot.getValue(Profile.class);
                profileRunnable = new Runnable() {
                    @Override
                    public void run() {
                        updateProfileView();
                    }
                };
                new Thread(profileRunnable).start();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                toastMessage = "Can not load profile from server";
                UtilToast.showToast(ProfileActivity.this, toastMessage);
            }
        });
    }

    public void updateProfileView() {
        Runnable populateRunnable = new Runnable() {
            @Override
            public void run() {
                Map<String, Object> map = profile.toMap();
                nickname = (String) map.get("nickname");
                phone = (String) map.get("phone");
                location = (String) map.get("location");
                birthday = (String) map.get("birthday");
                image = (String) map.get("image");

                if (validateString(nickname)) {
                    nicknameEditText.setText(nickname);
                    collapsingToolbarLayout.setTitle(nickname);
                }

                if (validateString(phone)) {
                    phoneEditText.setText(phone);
                }

                if (validateString(location)) {
                    locationEditText.setText(location);
                }

                if (validateString(birthday)) {
                    birthdayEditText.setText(birthday);
                }

                if (validateString(image)) {
                    byte[] decodedByteArray = Base64.decode(image, Base64.DEFAULT);
                    Bitmap imageEncoded = BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.length);
                    profileImageView.setImageBitmap(imageEncoded);
                }



            }
        };
        profileHandler.post(populateRunnable);
    }

    private boolean validateString(String str) {
        if (str == null || str.length() == 0) {
            return false;
        }
        return true;
    }
}
