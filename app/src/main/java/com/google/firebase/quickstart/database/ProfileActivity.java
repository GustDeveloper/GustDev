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
import com.google.firebase.quickstart.database.models.UtilToast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

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

    private String userID;

    /*Misc*/
    private String imageEncoded;
    private static String TAG = "quickstart.database.ProfileActivity";
    private boolean editEnabled = false;
    private Handler handler = new Handler();
    private Runnable runnable;


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

        CollapsingToolbarLayout collapsingToolbarLayout = findViewById(R.id.toolbar_layout);
        collapsingToolbarLayout.setTitle("John Doe");

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


        /*Add datas reference and listeners*/


        Bitmap bitmap = fetchImageFromServer();
        if (bitmap != null) {
            profileImageView.setImageBitmap(bitmap);
        }

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

    public void onCaptureImageResult(Intent data) {
       final Intent imgData = data;
       Runnable imgRunnable = new Runnable() {
           @Override
           public void run() {
               showProgressDialog();
               Bitmap bitmap = (Bitmap) imgData.getExtras().get("data");

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
    public void onSelectFromGalleryResult(Intent data) {
        final Intent imgData = data;

        Runnable imgRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    showProgressDialog();
                    Uri selectedImage = imgData.getData();
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
            //fo.write(bytes.toByteArray());
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

    public Bitmap fetchImageFromServer() {
        //TODO: this is needs to verify.

        imgRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                imageEncoded = (String) dataSnapshot.getValue();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                toastMessage = "Can not load image from server";
                UtilToast.showToast(ProfileActivity.this, toastMessage);
            }
        });

        if (imageEncoded == null || imageEncoded.length() == 0) {
            return null;
        }

        byte[] decodedByteArray = Base64.decode(imageEncoded, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.length);
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
        /*
        Map<String, Object>  = profile.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/posts/" + key, postValues);
        childUpdates.put("/user-posts/" + userId + "/" + key, postValues);

        mDatabase.updateChildren(childUpdates);
        */
    }


}
