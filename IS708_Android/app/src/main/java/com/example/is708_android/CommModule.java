package com.example.is708_android;

import android.os.Environment;
import android.util.Log;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.StringRequestListener;
import java.io.File;


public class CommModule {
    private final static String SERVER_IP = "http://192.168.2.45:5000";

    public static void callTargetDetectionApi(String command, MainActivity sourceActivity) {
        String targetFileFullPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/encoded.txt";
        sourceActivity.displayMessage("Uploading " + targetFileFullPath);
        File sceneImageFile = new File(targetFileFullPath);

        String completeUrl = SERVER_IP + "/detect_target";
        Log.d("CommModule", "Calling target detection API on " + completeUrl);

        AndroidNetworking.upload(completeUrl)
                .setPriority(Priority.MEDIUM)
                .addMultipartFile("scene_image_file", sceneImageFile)
                .addMultipartParameter("command_text", command)
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("CommModule", "API response: " + response);
                        sourceActivity.selectTargetScreenArea(response);
                    }

                    @Override
                    public void onError(ANError error) {
                        Log.d("CommModule", "Error: " + error.getErrorDetail());
                        sourceActivity.displayMessage(error.getErrorDetail());
                    }
                });

    }

    public static void callGestureDetectionApi(int gesture_code, MainActivity sourceActivity) {
        String completeUrl = SERVER_IP + "/detect_gesture";
        Log.d("CommModule", "Calling gesture detection API on " + completeUrl);
        sourceActivity.displayMessage("Sending gesture code " + gesture_code);

        AndroidNetworking.post(completeUrl)
                .setPriority(Priority.IMMEDIATE)
                .addBodyParameter("gesture_code", "" + gesture_code)
                .build()
                .getAsString(new StringRequestListener(){
                    @Override
                    public void onResponse(String response){
                        Log.d("CommModule", "API response: " + response);
                        sourceActivity.respondToGesture(response);
                    }

                    @Override
                    public void onError(ANError error){
                        Log.d("CommModule", "Error: " + error.getErrorDetail());
                        sourceActivity.displayMessage(error.getErrorDetail());
                    }
                });
    }
}
