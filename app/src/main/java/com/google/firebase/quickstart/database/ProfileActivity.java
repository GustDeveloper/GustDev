package com.google.firebase.quickstart.database;

import android.*;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.quickstart.database.models.UtilToast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
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

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener,PopupMenu.OnMenuItemClickListener {

    /*UI elements*/
    ImageView profileImageView;
    FloatingActionButton fab;
    EditText tvNumber1;

    private static String TAG = "quickstart.database.ProfileActivity";
    private boolean editMode = false;

    /*Maximum Byte Size Is 10MB*/
    int MAX_IMG_BYTE_SIZE = 10485760;
    private static final String IMAGE_DIRECTORY = "/GustImg";

    /*Gallery Request Code*/
    int REQUEST_IMAGE_STORAGE = 1;
    int REQUEST_IMAGE_CAPTURE = 0;

    /*String Messages*/
    String toastMessage;

    /*Databse Reference*/
    private DatabaseReference mDatabase;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Toolbar toolbar =  findViewById(R.id.toolbar);
        setSupportActionBar(toolbar); //replace the old action bar with toolbar

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(this);
        //Todo: grab image from database
        profileImageView = findViewById(R.id.profileImageView);
        profileImageView.setOnClickListener(this);
        tvNumber1 = findViewById(R.id.tvNumber1);
        setTitle("Gus");


    }


    @Override
    public void onClick(View view){
        switch (view.getId()) {
            case R.id.profileImageView:
                showPopup(view);
                break;
            case R.id.fab:
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                if (!editMode) {
                    tvNumber1.setEnabled(true);
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
                        requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_IMAGE_CAPTURE);
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
        if(requestCode == REQUEST_IMAGE_STORAGE && resultCode == RESULT_OK && data != null){
            Uri selectedImage = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);

                //Todo: what is blocking the UI thread, do it in the background thread
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                //bitmap.compress(Bitmap.CompressFormat.PNG,100,stream);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);

                byte[] byteArray = stream.toByteArray();

                /*If image is way too large, reject it*/
                if(byteArray.length >= MAX_IMG_BYTE_SIZE){
                    toastMessage  = "The image selected exceeds size limit";
                    UtilToast.showToast(ProfileActivity.this, toastMessage);
                } else {
                    profileImageView.setImageBitmap(bitmap);
                    UtilToast.showToast(ProfileActivity.this, toastMessage);
                }

            }catch(IOException e){
                e.printStackTrace();
            }
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && data != null) {
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");

            if (bitmap == null) {
                toastMessage  = "Failed to process camera image data";
                UtilToast.showToast(ProfileActivity.this, toastMessage);
                return;
            }

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);

            if (bitmap == null) {
                toastMessage  = "Failed to compress camera image data";
                UtilToast.showToast(ProfileActivity.this, toastMessage);
                return;
            }

            profileImageView.setImageBitmap(bitmap);

            byte[] byteArray = stream.toByteArray();
            String path = saveImageIntoFolder(byteArray);
            //upLoadImageToServer(byteArray);

            toastMessage  = "Image is uploaded and saved to " + path;
            UtilToast.showToast(ProfileActivity.this, toastMessage);

        }
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
        DatabaseReference userImgRef = FirebaseDatabase.getInstance()
                .getReference()
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("ProfileImage")
                .child("imageUrl");
        //TODO: Might change it to setValueAsync with Completion Callback, return boolean to indicate the status of uploading.
        userImgRef.setValue(imageEncoded);
    }

    public Bitmap fetchImageFromServer(String imageUrl) {
        //TODO: this is needs to verify.
        byte[] decodedByteArray = android.util.Base64.decode(imageUrl, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.length);
    }
}
