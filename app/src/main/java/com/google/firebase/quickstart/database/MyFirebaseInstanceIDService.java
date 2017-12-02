package com.google.firebase.quickstart.database;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import java.util.HashMap;

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {
    public MyFirebaseInstanceIDService() {
    }


    @Override
    public void onTokenRefresh() {
        HashMap<String,String> params = new HashMap<String,String>();
        String refershedToken = FirebaseInstanceId.getInstance().getToken();
        params.put("regid",refershedToken );
        Log.d("Service", refershedToken);
        sendRegistrationToServer(refershedToken);
        // send the token to the server!!
    }

    private void sendRegistrationToServer(String token) {
        // Add custom implementation, as needed.
    }
}
