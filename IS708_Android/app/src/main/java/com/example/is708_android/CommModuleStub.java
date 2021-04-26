package com.example.is708_android;

public class CommModuleStub {
    private final static String SERVER_IP = "http://localhost:5000";

    public static void callTargetDetectionApi(String command, MainActivity sourceActivity){
        sourceActivity.selectTargetScreenArea("{'remote',[0,0,100,100]}");
    }

    public static void callGestureDetectionApi(int segment_code, MainActivity sourceActivity) {
        if(segment_code == 1) {
            sourceActivity.respondToGesture("Nodding");
        } else {
            sourceActivity.respondToGesture("Shaking");
        }
    }
}
