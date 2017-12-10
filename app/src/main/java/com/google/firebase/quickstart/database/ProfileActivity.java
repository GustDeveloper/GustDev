package com.google.firebase.quickstart.database;

import android.content.DialogInterface;
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
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;

import com.google.firebase.auth.FirebaseAuth;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.lujun.androidtagview.ColorFactory;
import co.lujun.androidtagview.TagContainerLayout;
import co.lujun.androidtagview.TagView;

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
    FloatingActionButton messageFab;
    EditText nicknameEditText;
    EditText phoneEditText;
    EditText locationEditText;
    EditText birthdayEditText;
    Toolbar toolbar;
    EditText descriptionEditText;
    TagContainerLayout mTagContainerLayout;
    EditText tagEditText;
    Button addTagBtn;


    Map<String, Object> profileMap;
    String nickname;
    String phone;
    String location;
    String birthday;
    String description;

    String image;
    List<String> hobbies = new ArrayList<>();

    CollapsingToolbarLayout collapsingToolbarLayout;


    /*Maximum Byte Size Is 10MB*/
    int MAX_IMG_BYTE_SIZE = 10485760;
    private static final String IMAGE_DIRECTORY = "/GustImg";

    /*Gallery Request Code*/
    int REQUEST_IMAGE_STORAGE = 0;
    int REQUEST_IMAGE_CAPTURE = 1;

    /*String Messages*/
    String toastMessage;

    /*Databse Reference*/
    private DatabaseReference mDatabase;
    private DatabaseReference profileRef;
    private DatabaseReference imgRef;
    private DatabaseReference tagRef;

    /*Profile*/
    private String userID;
    private Profile profile;


    /*Misc*/
    private static String TAG = "quickstart.database.ProfileActivity";

    private boolean editEnabled = false;
    //private boolean isUser;
    private String intentUserID;


    /*Handlers and Runnables*/
    private Handler handler = new Handler();
    private Handler profileHandler = new Handler();
    private Runnable imgRunnable;
    private Runnable profileRunnable;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        toolbar =  findViewById(R.id.toolbar);
        setSupportActionBar(toolbar); //replace the old action bar with toolbar

        Intent intent = getIntent();
        intentUserID = intent.getStringExtra("intentUserID");
        Log.e(TAG, intentUserID);

        /*get database reference*/
        mDatabase = FirebaseDatabase.getInstance().getReference();
        userID = getUid();

        editFab = findViewById(R.id.editFab);
        editFab.setOnClickListener(this);

        messageFab = findViewById(R.id.messageFab);
        messageFab.setOnClickListener(this);
        profileImageView = findViewById(R.id.profileImageView);

        profileImageView.setOnClickListener(this);

        mTagContainerLayout = findViewById(R.id.tagcontainerLayoutProfile);
        mTagContainerLayout.setOnTagClickListener(new TagView.OnTagClickListener() {
            @Override
            public void onTagClick(int position, String text) {
            }

            @Override
            public void onTagLongClick(final int position, String text) {
                AlertDialog dialog = new AlertDialog.Builder(ProfileActivity.this)
                        .setTitle("long click")
                        .setMessage("You will delete this tag!")
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (position < mTagContainerLayout.getChildCount()) {
                                    mTagContainerLayout.removeTag(position);
                                }

                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .create();
                dialog.show();
            }

            @Override
            public void onTagCrossClick(int position) {
                toastMessage = "Please do not cross click buttons";
                UtilToast.showToast(ProfileActivity.this, toastMessage);
            }
        });
        mTagContainerLayout.setBackgroundColor(ColorFactory.NONE);
        mTagContainerLayout.setTheme(ColorFactory.RANDOM);

        tagEditText = findViewById(R.id.tagEditTextProfile);
        tagEditText.setVisibility(View.INVISIBLE);

        addTagBtn = findViewById(R.id.addTagButtonProfile);
        addTagBtn.setVisibility(View.INVISIBLE);
        addTagBtn.setOnClickListener(this);

        if (intentUserID.matches(userID)) {
            messageFab.setVisibility(View.INVISIBLE);
        }
        else {
            editFab.setVisibility(View.INVISIBLE);
            tagEditText.setVisibility(View.INVISIBLE);
            addTagBtn.setVisibility(View.INVISIBLE);

            profileImageView.setOnClickListener(null);
            mTagContainerLayout.setOnTagClickListener(null);
            mTagContainerLayout.setDragEnable(false);
        }


        collapsingToolbarLayout = findViewById(R.id.toolbar_layout);
        collapsingToolbarLayout.setTitle("");
        nicknameEditText = findViewById(R.id.nicknameEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        locationEditText = findViewById(R.id.locationEditText);
        birthdayEditText = findViewById(R.id.birthdayEditText);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        descriptionEditText.setText("");

        profileRef = mDatabase.child("profiles").child(intentUserID);
        imgRef = profileRef.child("image");
        tagRef = profileRef.child("hobbies");

        getProfileFromServer(profileRef);
    }


    @Override
    public void onClick(View view){
        switch (view.getId()) {
            case R.id.profileImageView:
                showPopup(view);
                break;
            case R.id.editFab:
                editEnabled = !editEnabled;
                if (editEnabled) {
                    enabledEdit();
                } else {
                    disableEditAndSaveToServer();
                }
                break;
            case R.id.messageFab:
                final String Uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                final DatabaseReference userhash = mDatabase.child("user-user");
                final String infoKey =intentUserID;
                // determine if the user-user pair exist
                Log.d("Chat","Success");

                userhash.child(Uid).addListenerForSingleValueEvent(new ValueEventListener(){
                    public void onDataChange(DataSnapshot snapshot1) {
                        boolean Create = true;
                        for (DataSnapshot peopleSnapshot: snapshot1.getChildren()) {

                            if (peopleSnapshot.getKey().equals(infoKey)) {
                                Create = false;
                                Log.d("ChatChat", peopleSnapshot.getValue().toString());
                                Intent chatActivity = new Intent(getApplicationContext() , ChatActivity.class);
                                chatActivity.putExtra("Path","/chat-room/" + peopleSnapshot.getValue().toString());
                                chatActivity.putExtra("ReceiverName", profile.username);
                                chatActivity.putExtra("receiver",infoKey);
                                startActivity(chatActivity);
                            }
                        }
                        if (Create) {
                            Log.d("ChatChat", "Create");
                            Map<String, Object> childUpdates = new HashMap<>();
                            String roomkey = mDatabase.child("chat-room").push().getKey();
                            childUpdates.put("/user-user/" + Uid + "/" + infoKey, roomkey);
                            childUpdates.put("/user-user/" + infoKey + "/" + Uid, roomkey);
                            mDatabase.updateChildren(childUpdates);
                            Intent chatActivity = new Intent(getApplicationContext() , ChatActivity.class);
                            chatActivity.putExtra("Path", "/chat-room/" + roomkey);
                            chatActivity.putExtra("receiver",infoKey);
                            chatActivity.putExtra("ReceiverName", profile.username);
                            startActivity(chatActivity);
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });

                break;
            case R.id.addTagButtonProfile:
                String newHobby = tagEditText.getText().toString();
                if (newHobby == null || newHobby.length() == 0) {
                    UtilToast.showToast(getApplicationContext(), "You haven't enter any tag");
                    break;
                }
                hobbies.add(newHobby);
                mTagContainerLayout.addTag(newHobby);
                Log.e(TAG, hobbies.toString());
                nickname = nicknameEditText.getText().toString();
                location = locationEditText.getText().toString();
                birthday = birthdayEditText.getText().toString();
                phone = phoneEditText.getText().toString();
                description = descriptionEditText.getText().toString();

                if (validateString(nickname)) {
                    //profile.setNickname(nickname);
                    profile.nickname = nickname;
                }

                if (validateString(phone)) {
                    //profile.setLocation(phone);
                    profile.phone = phone;
                }

                if (validateString(location)) {
                    // profile.setPhone(location);
                    profile.location = location;
                }

                if (validateString(birthday)) {
                    //profile.setBirthday(birthday);
                    profile.birthday = birthday;
                }

                if (validateString(description)) {
                    profile.description = description;
                }


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

                tagRef.setValue(hobbies, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if (databaseError != null) {
                            toastMessage = "Failed to add tags to the server";
                            UtilToast.showToast(ProfileActivity.this, toastMessage);
                        } else {
                            tagEditText.setText("");
                        }
                    }
                });
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
                imgRunnable = new Runnable() {
                    @Override
                    public void run() {
                        onSelectFromGalleryResult(imgData);
                    }
                };
                new Thread(imgRunnable).start();
            } else if (requestCode == REQUEST_IMAGE_CAPTURE) {
                imgRunnable = new Runnable() {
                    @Override
                    public void run() {
                        onCaptureImageResult(imgData);
                    }
                };
                new Thread(imgRunnable).start();
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
              // bitmap = Bitmap.createScaledBitmap(bitmap, 300, 300, true);

               profileImageView.setImageBitmap(null);
               profileImageView.setImageBitmap(bitmap);
               hideProgressDialog();

               byte[] byteArray = stream.toByteArray();
               String path = saveImageIntoFolder(byteArray);
               uploadImageToServer(byteArray);

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

                    ByteArrayOutputStream stream = new ByteArrayOutputStream();

                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);
                   // bitmap = Bitmap.createScaledBitmap(bitmap, 300, 300, true);

                    profileImageView.setImageBitmap(null);
                    profileImageView.setImageBitmap(bitmap);

                    byte[] byteArray = stream.toByteArray();

                    /*If image is way too large, reject it*/
                    if(byteArray.length >= MAX_IMG_BYTE_SIZE){
                        toastMessage  = "The image selected exceeds size limit";
                        UtilToast.showToast(ProfileActivity.this, toastMessage);
                    } else {
                        uploadImageToServer(byteArray);
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

    public void uploadImageToServer(byte[] byteArray) {
        String imageEncoded = Base64.encodeToString(byteArray, Base64.DEFAULT);
        imgRef = profileRef.child("image");
        imgRef.setValue(imageEncoded, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError != null) {
                    Log.e(TAG, "Failed to update profile image, please try again");
                    toastMessage = "Failed to update profile image, please try again";
                    UtilToast.showToast(ProfileActivity.this, toastMessage);
                }
            }
        });
    }

    public void enabledEdit() {
        editFab.setImageResource(R.drawable.content_save_settings);
        nicknameEditText.setEnabled(true);
        phoneEditText.setEnabled(true);
        locationEditText.setEnabled(true);
        birthdayEditText.setEnabled(true);
        descriptionEditText.setEnabled(true);
        addTagBtn.setVisibility(View.VISIBLE);
        tagEditText.setVisibility(View.VISIBLE);
        mTagContainerLayout.setDragEnable(true);

    }

    public void disableEditAndSaveToServer() {
        editFab.setImageResource(R.drawable.ic_image_edit);
        nicknameEditText.setEnabled(false);
        phoneEditText.setEnabled(false);
        locationEditText.setEnabled(false);
        birthdayEditText.setEnabled(false);
        descriptionEditText.setEnabled(false);
        addTagBtn.setVisibility(View.INVISIBLE);
        tagEditText.setVisibility(View.INVISIBLE);
        mTagContainerLayout.setDragEnable(false);

        nickname = nicknameEditText.getText().toString();
        location = locationEditText.getText().toString();
        birthday = birthdayEditText.getText().toString();
        phone = phoneEditText.getText().toString();
        description = descriptionEditText.getText().toString();


        if (validateString(nickname)) {
            //profile.setNickname(nickname);
            profile.nickname = nickname;
        }

        if (validateString(phone)) {
            //profile.setLocation(phone);
            profile.phone = phone;
        }

        if (validateString(location)) {
           // profile.setPhone(location);
            profile.location = location;
        }

        if (validateString(birthday)) {
            //profile.setBirthday(birthday);
            profile.birthday = birthday;
        }

        if (validateString(description)) {
            profile.description = description;
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
        profileRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.e(TAG, "profile change");
                profile = dataSnapshot.getValue(Profile.class);
                if (profile != null) {
                    profileRunnable = new Runnable() {
                        @Override
                        public void run() {
                            updateProfileView();
                        }
                    };
                    new Thread(profileRunnable).start();
                }
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
                profileMap = profile.toMap();
                nickname = (String) profileMap.get("nickname");
                phone = (String) profileMap.get("phone");
                location = (String) profileMap.get("location");
                birthday = (String) profileMap.get("birthday");
                image = (String) profileMap.get("image");
                description = (String) profileMap.get("description");

                if (profileMap.get("hobbies") != null) {
                    hobbies  = (List)profileMap.get("hobbies");
                    hobbies.removeAll(Collections.singleton(null));
                    hobbies.removeAll(Arrays.asList("", null));
                    mTagContainerLayout.setTags(hobbies);
                }


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

                if (validateString(image) && image.length() != 0) {
                    byte[] decodedByteArray = Base64.decode(image, Base64.DEFAULT);
                    Bitmap imageEncoded = BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.length);
                    profileImageView.setImageBitmap(imageEncoded);
                }

                if (validateString(description)) {
                    descriptionEditText.setText(description);
                }

                if (hobbies != null) {
                    hobbies.removeAll(Collections.singleton(null)); //Time complexity is n^2, not the best implementation
                    hobbies.removeAll(Arrays.asList("", null));
                    mTagContainerLayout.setTags(hobbies);
                } else {

                    Log.e(TAG, "null");
                }

            }
        };
        profileHandler.post(populateRunnable);
    }

    private boolean validateString(String str) {
        return !(str == null);
    }
}
