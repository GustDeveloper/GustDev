package com.google.firebase.quickstart.database.fragment;

import android.app.DatePickerDialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TimePicker;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.quickstart.database.ProfileActivity;
import com.google.firebase.quickstart.database.R;
import com.google.firebase.quickstart.database.models.Event;
import com.google.firebase.quickstart.database.models.UtilToast;

import co.lujun.androidtagview.ColorFactory;
import co.lujun.androidtagview.TagContainerLayout;
import co.lujun.androidtagview.TagView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_OK;
import static android.support.v4.content.PermissionChecker.checkSelfPermission;


public class EventFragment extends Fragment implements View.OnClickListener, PopupMenu.OnMenuItemClickListener {

    // [START define_database_reference]
    //private DatabaseReference mDatabase;
    //private DatabaseReference mProfileRef;

    // [END define_database_reference]

    Button btnDatePicker, btnTimePicker,addTagBtn;
    EditText txtDate, txtTime;
    TagContainerLayout mTagContainerLayout;
    int mYear, mMonth, mDay, mHour, mMinute;
    EditText phoneEditText, locationEditText,nicknameEditText,emaiEditText, titleEditText,tagEditText,descriptionEditText;
    ImageView eventImageView;
    FloatingActionButton saveFab;
    EventFragmentCallback eventFragmentCallback;
    String eventImage = "";
    String TAG = "In Event Profile";
    /*Gallery Request Code*/
    int REQUEST_IMAGE_STORAGE = 0;
    int REQUEST_IMAGE_CAPTURE = 1;

    String toastMessage;

    List<String> tags  = new ArrayList<>();

    int MAX_IMG_BYTE_SIZE = 10485760;
    private static final String IMAGE_DIRECTORY = "/GustImg";
    //String key = "";

    private Handler eventHandler = new Handler();
    private Runnable imgRunnable;

    private EventFragmentCallback mListener;

    public EventFragment() {
        // Required empty public constructor
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView =  inflater.inflate(R.layout.fragment_event, container, false);
        btnDatePicker= rootView.findViewById(R.id.datePickerBtn);
        btnTimePicker= rootView.findViewById(R.id.timePickerBtn);
        saveFab = rootView.findViewById(R.id.saveFab);
        saveFab.setOnClickListener(this);
        btnDatePicker.setOnClickListener(this);
        btnTimePicker.setOnClickListener(this);


        txtDate =  rootView.findViewById(R.id.dateEditText);
        txtTime =  rootView.findViewById(R.id.timeEditText);
        phoneEditText = rootView.findViewById(R.id.phoneEditText);
        locationEditText = rootView.findViewById(R.id.locationEditText);
        nicknameEditText = rootView.findViewById(R.id.nicknameEditText);
        emaiEditText = rootView.findViewById(R.id.emailEditText);
        titleEditText = rootView.findViewById(R.id.titleEditText);
        tagEditText = rootView.findViewById(R.id.tagEditText);
        descriptionEditText = rootView.findViewById(R.id.descriptionEditText);
        mTagContainerLayout = rootView.findViewById(R.id.tagcontainerLayout);
        mTagContainerLayout.setTags(tags);
        eventImageView = rootView.findViewById(R.id.eventImage);
        eventImageView.setOnClickListener(this);

        addTagBtn = rootView.findViewById(R.id.addTagBtn);
        addTagBtn.setOnClickListener(this);

        return rootView;
    }

    public interface EventFragmentCallback {
        void sendEventToServer(Event event);
    }



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            eventFragmentCallback = (EventFragmentCallback) context;
        } catch(ClassCastException e) {
            throw new ClassCastException(context.toString() + "must implement Fragment1Callback");
        }
    }

    @Override
    public void onClick(View view) {
        if (view == btnDatePicker){
            final Calendar calendar = Calendar.getInstance();
            mYear = calendar.get(Calendar.YEAR);
            mMonth = calendar.get(Calendar.MONTH);
            mDay = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(),
                    new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
                            txtDate.setText((monthOfYear + 1) + "-" + dayOfMonth +  "-" + year);
                        }
                    }, mYear, mMonth, mDay);
            datePickerDialog.show();
        }

        if (view == btnTimePicker) {
            final Calendar calendar = Calendar.getInstance();
            mHour = calendar.get(Calendar.HOUR_OF_DAY);
            mMinute = calendar.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog;
            timePickerDialog = new TimePickerDialog(getActivity(),
                    new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
                            txtTime.setText(hourOfDay + ":" + minute);
                        }
                    }, mHour,mMinute,false);
            timePickerDialog.show();
        }

        if (view == saveFab) {
            if (validateForm()) {
                passEventToActivity();
            } else {
                return;
            }
        }

        if (view == addTagBtn) {
            String newTag = tagEditText.getText().toString();
            mTagContainerLayout.addTag(newTag);
            tags.add(newTag);
        }

        if (view == eventImageView) {
            showPopup(view);
        }
    }

    public void showPopup(View view) {
        PopupMenu popupMenu = new PopupMenu(getContext(),view);
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
                    if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                            ActivityCompat.checkSelfPermission(getActivity(),android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ) {
                        requestPermissions(new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_IMAGE_CAPTURE);
                    } else {
                        launchCamera();
                    }
                }

                return true;
            case R.id.action_storage:
                Log.e(TAG, "You clicked storage");
                if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(),new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_IMAGE_STORAGE);
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
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
                Bitmap bitmap = (Bitmap) data.getExtras().get("data");

                if (bitmap == null) {
                    toastMessage  = "Failed to process camera image data";
                    UtilToast.showToast(getContext(), toastMessage);
                    return;
                }

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);
                // bitmap = Bitmap.createScaledBitmap(bitmap, 300, 300, true);

                eventImageView.setImageBitmap(null);
                eventImageView.setImageBitmap(bitmap);

                byte[] byteArray = stream.toByteArray();
                String path = saveImageIntoFolder(byteArray);
                encodeAndPopulateImage(byteArray);

                toastMessage  = "Image is uploaded and saved to " + path;
                UtilToast.showToast(getContext(), toastMessage);
            }
        };

        eventHandler.post(imgRunnable);

    }
    public void onSelectFromGalleryResult(final Intent data) {

        Runnable imgRunnable = new Runnable() {
            @Override
            public void run() {
                try {

                    Uri selectedImage = data.getData();
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), selectedImage);

                    ByteArrayOutputStream stream = new ByteArrayOutputStream();

                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);
                    // bitmap = Bitmap.createScaledBitmap(bitmap, 300, 300, true);

                    eventImageView.setImageBitmap(null);
                    eventImageView.setImageBitmap(bitmap);

                    byte[] byteArray = stream.toByteArray();

                    /*If image is way too large, reject it*/
                    if(byteArray.length >= MAX_IMG_BYTE_SIZE){
                        toastMessage  = "The image selected exceeds size limit";
                        UtilToast.showToast(getContext(), toastMessage);
                    } else {
                        encodeAndPopulateImage(byteArray);
                        saveImageIntoFolder(byteArray);
                    }

                }catch(IOException e){
                    e.printStackTrace();
                }
            }
        };

        eventHandler.post(imgRunnable);
    }


    public void getImageFromStorage(){
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_IMAGE_STORAGE);
    }

    private boolean hasCamera(){
        return getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY);
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

            MediaScannerConnection.scanFile(getContext(),
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


    public void encodeAndPopulateImage(byte[] byteArray) {
        String imageEncoded = Base64.encodeToString(byteArray, Base64.DEFAULT);
        eventImage = imageEncoded;
    }



    public Event createEvent() {
        Event event = new Event(getUid());
        event.author = nicknameEditText.getText().toString();
        event.participants = new HashMap<>();

        event.tags = new HashMap<>();
        for (String tag: tags) {
            event.tags.put(tag, true);
        }

        event.title = titleEditText.getText().toString();
        event.time = txtTime.getText().toString();
        event.date = txtDate.getText().toString();
        event.description = descriptionEditText.getText().toString();
        event.location = locationEditText.getText().toString();
        event.email = emaiEditText.getText().toString();
        event.phone = phoneEditText.getText().toString();
        event.image = eventImage;
        return event;
    }

    private boolean validateForm() {
        boolean result = true;
        if (TextUtils.isEmpty(descriptionEditText.getText().toString())) {
            descriptionEditText.setError("Required");
            result = false;
        } else {
            descriptionEditText.setError(null);
        }
        if (TextUtils.isEmpty(titleEditText.getText().toString())) {
            titleEditText.setError("Required");
            result = false;
        } else {
            titleEditText.setError(null);
        }

        if (TextUtils.isEmpty(txtDate.getText().toString())) {
            txtDate.setError("Required");
            result = false;
        } else {
            txtDate.setError(null);
        }

        if (TextUtils.isEmpty(txtTime.getText().toString())) {
            txtTime.setError("Required");
            result = false;
        } else {
            txtTime.setError(null);
        }

        if (TextUtils.isEmpty(nicknameEditText.getText().toString())) {
            nicknameEditText.setError("Required");
            result = false;
        } else {
            nicknameEditText.setError(null);
        }

        if (TextUtils.isEmpty(emaiEditText.getText().toString())) {
            emaiEditText.setError("Required");
            result = false;
        } else {
            emaiEditText.setError(null);
        }

        if (TextUtils.isEmpty(locationEditText.getText().toString())) {
            locationEditText.setError("Required");
            result = false;
        } else {
            locationEditText.setError(null);
        }

        if (TextUtils.isEmpty(phoneEditText.getText().toString())) {
            phoneEditText.setError("Required");
            result = false;
        } else {
            phoneEditText.setError(null);
        }

        return result;
    }

    public void passEventToActivity() {
        Event newEvent = createEvent();
        eventFragmentCallback.sendEventToServer(newEvent);
    }

    @Override
    public void onStop() {
        super.onStop();

    }

    public String  getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

}

