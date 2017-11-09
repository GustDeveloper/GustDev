package com.google.firebase.quickstart.database;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.NumberFormat;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener,PopupMenu.OnMenuItemClickListener {

    /*UI elements*/
    ImageView profileImageView;
    FloatingActionButton fab;
    EditText tvNumber1;

    private static String TAG = "quickstart.database.ProfileActivity";
    private boolean editMode = false;

    /*Maximum Byte Size Is 10MB*/
    int MAX_IMG_BYTE_SIZE = 10485760;
    int REQUEST_IMAGE_CAPTURE = 1;
    int REQUEST_IMAGE_STORAGE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(this);
        profileImageView = (ImageView) findViewById(R.id.profileImageView);
        profileImageView.setOnClickListener(this);
        tvNumber1 = (EditText) findViewById(R.id.tvNumber1);

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

                return true;
            case R.id.action_storage:
                Log.e(TAG, "You click storage");
                if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
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
        if(requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getImageFromStorage();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1 && resultCode == RESULT_OK && data != null){
            Uri selectedImage = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                //Todo: See if you can render this image on background, with handler or something
                //ImageView imageView = (ImageView)findViewById(R.id.profileImageView);


                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG,100,stream);
                byte[] byteArray = stream.toByteArray();

                /*If image is way too large, reject it*/
                if(byteArray.length >= MAX_IMG_BYTE_SIZE){
                    //Todo: Try wrap toast in a class for better user experience or use
                    Toast.makeText(ProfileActivity.this,"Your image is too large, Please upload again!", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    Toast.makeText(ProfileActivity.this,"Image Upload is Successful", Toast.LENGTH_SHORT).show();
                    profileImageView.setImageBitmap(bitmap);
                }

                //Todo: Upload image to firebase
            }catch(IOException e){
                e.printStackTrace();
            }
        }
    }

    public void getImageFromStorage(){
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
    }

    private boolean hasCamera(){
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY);
    }

    public void launchCamera(View view){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //Take a picture and pass results along to onActivityResult
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
    }

    /*
    private void onCaptureImageResult(Intent data) {
      Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
      ByteArrayOutputStream bytes = new ByteArrayOutputStream();
      thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
      File destination = new File(Environment.getExternalStorageDirectory(),
            System.currentTimeMillis() + ".jpg");
      FileOutputStream fo;
      try {
         destination.createNewFile();
         fo = new FileOutputStream(destination);
         fo.write(bytes.toByteArray());
         fo.close();
      } catch (FileNotFoundException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      }
      ivImage.setImageBitmap(thumbnail);
    }
     */

    /*
        @SuppressWarnings("deprecation")
    	private void onSelectFromGalleryResult(Intent data) {

		Bitmap bm=null;
		if (data != null) {
			try {
				bm = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		ivImage.setImageBitmap(bm);
	}
     */



}
