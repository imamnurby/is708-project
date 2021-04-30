package com.example.is708_android;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.PixelCopy;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

import com.androidnetworking.*;
import com.google.ar.core.Anchor;
import com.google.ar.core.Frame;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.Camera;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.Sun;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

public class MainActivity extends AppCompatActivity {
    private static final String[] PERMISSIONS = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.INTERNET,
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };
    public static final Integer MULTIPLE_PERMISSIONS = 10;
    private SpeechRecognizer speechRecognizer;
    private EditText editText;
    private ImageView upArrow;
    private ImageView downArrow;
    private ImageButton micButton;
    private int gestureChoice;
    // To help you see what's going on from the UI itself
    private TextView sysMsgTextView;

    // AR
    private ArFragment fragment;
    private TransformableNode responseObjectNode;
    private String targetScreenArea = null;
    private Session session;
    private Frame frame;
    private CompletableFuture<Void> dummy;
    private AnchorNode anchorNode;
    private static final String GLTF_ASSET = "https://github.com/KhronosGroup/glTF-Sample-Models/raw/master/2.0/Duck/glTF/Duck.gltf";
    private float[] finalpos;
    private ModelRenderable model;



    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        for(String p:PERMISSIONS) {
            if(ContextCompat.checkSelfPermission(this,p) != PackageManager.PERMISSION_GRANTED){
                checkPermission();
            }
        }

        sysMsgTextView = findViewById(R.id.sysMessageTextView);
        sysMsgTextView.setText("");

        // Is SpeechRecognizer available?
        if (SpeechRecognizer.isRecognitionAvailable(this)){
            Log.d(String.valueOf(this),"Recognition is available");
            sysMsgTextView.setText("Speech recognition is available");
        } else {
            Log.d(String.valueOf(this),"Recognition is *NOT* available");
            sysMsgTextView.setText("Speech recognition is *NOT* available");
        }
        // Initialize AndroidNetworking
        AndroidNetworking.initialize(getApplicationContext());

        editText = findViewById(R.id.text);
        upArrow = findViewById(R.id.upArrowImage);
        downArrow = findViewById(R.id.downArrowImage);
        micButton = findViewById(R.id.micButton);
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);


        final Intent speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {
            }

            @Override
            public void onBeginningOfSpeech() {
                editText.setHint("...Listening...");
            }

            @Override
            public void onRmsChanged(float v) {
            }

            @Override
            public void onBufferReceived(byte[] bytes) {
            }

            @Override
            public void onEndOfSpeech() {
            }

            @Override
            public void onError(int i) {
            }

            @Override
            public void onResults(Bundle bundle) {
                ArrayList<String> data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                String commandText = String.join(" ",data);
                editText.setText(commandText);
                Log.d(String.valueOf(this), "Result: " + commandText);
                sysMsgTextView.setText(sysMsgTextView.getText() + "\n" + "Speech recognition done");

                takePhoto();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        CommModule.callTargetDetectionApi(commandText, MainActivity.this);
                        // OR yourTV.setVisibility(View.GONE) to reclaim the space
                    }
                }, 5000);
            }

            @Override
            public void onPartialResults(Bundle bundle) {

            }

            @Override
            public void onEvent(int i, Bundle bundle) {

            }
        });

        micButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    speechRecognizer.stopListening();
                    Log.d(String.valueOf(this), "Stopped listening");
                }
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    speechRecognizer.startListening(speechRecognizerIntent);
                    Log.d(String.valueOf(this),"Started listening");
                    return true;
                }
                return false;
            }
        });

        // AR
        fragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.sceneform_fragment);

        // Hide instruction animation
        fragment.getPlaneDiscoveryController().hide();
        fragment.getPlaneDiscoveryController().setInstructionView(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        speechRecognizer.destroy();
    }

    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this,PERMISSIONS,MULTIPLE_PERMISSIONS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MULTIPLE_PERMISSIONS && grantResults.length > 0 ){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void clickUpArrow(View view) {
        gestureChoice=1;
        CommModule.callGestureDetectionApi(gestureChoice, MainActivity.this);
    }

    public void clickDownArrow(View view) {
        gestureChoice=2;
        CommModule.callGestureDetectionApi(gestureChoice,MainActivity.this);
    }

    public void selectTargetScreenArea(String boundingBoxJson) {
        /*
         TODO: You should implement this method.
         Also consider updating action for "NULL" / "Reset" button to remove
         rendered bounding box to facilitate multiple runs without creating clutter
        on camera preview
         */
        sysMsgTextView.setText("Target bounding box:\n" + boundingBoxJson);

        String temp1 = boundingBoxJson.replace("[", "");
        String temp2 = temp1.replace("]", "");
        String temp3 = temp2.replace(" ", "");
        String[] boundingBox = temp3.split("\\,");

        float xLeft = Float.parseFloat((boundingBox[1]));
        float xRight = Float.parseFloat(boundingBox[3]);
        float yTop = Float.parseFloat(boundingBox[2]);
        float yBot = Float.parseFloat(boundingBox[4]);

        Log.d("XXX Left", Float.toString(xLeft));
        Log.d("XXX Right", Float.toString(xRight));
        Log.d("XXX Top", Float.toString(yTop));
        Log.d("XXX Bot", Float.toString(yBot));

        ArSceneView arSceneView = fragment.getArSceneView();
        final Bitmap bitmap = Bitmap.createBitmap(arSceneView.getWidth(), arSceneView.getHeight(), Bitmap.Config.ARGB_8888);

        float middlePoint = arSceneView.getWidth()/2;
        float centroidX = (xLeft + xRight)/2;

        if(centroidX < middlePoint) {
            targetScreenArea = "LEFT";
        } else {
            targetScreenArea = "RIGHT";
        }
        Log.d("targetScreenArea", targetScreenArea);

        ImageView bbox=(ImageView) findViewById(R.id.bbox);

        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);
        paint.setColor(Color.parseColor("#00FF00"));

        Rect rec = new Rect();
        rec.top=  (int) yTop;
        rec.left= (int) xLeft;
        rec.bottom = (int) yBot;
        rec.right = (int) xRight;

        canvas.drawRect(rec,paint);
        bbox.setImageBitmap(bitmap);

    }

    public void respondToGesture(String gesture) {
        /* TODO: You should implement this method */
        String gest = gesture.substring(1, gesture.length() - 1);
        sysMsgTextView.setText("Gesture:" + gest);

        // load model
        float[] leftInit;
        float[] rightInit;
        float[] finalpos;
        if (gest.equals("Nodding")){
            ModelRenderable.builder()
                    .setSource(this, R.raw.export_71)
                    .build()
                    .thenAccept(renderable -> model = renderable)
                    .exceptionally(
                            throwable -> {
                                Toast toast =
                                        Toast.makeText(this, "Unable to load andy renderable", Toast.LENGTH_LONG);
                                toast.setGravity(Gravity.CENTER, 0, 0);
                                toast.show();
                                return null;
                            });
            finalpos = new float[]{0, 0, -2f};
            leftInit = new float[]{-0.5f, 0, -2f};
            rightInit = new float[]{0.3f, 0, -2f};
        } else {
            ModelRenderable.builder()
                    .setSource(this, R.raw.thumbsup)
                    .build()
                    .thenAccept(renderable -> model = renderable)
                    .exceptionally(
                            throwable -> {
                                Toast toast =
                                        Toast.makeText(this, "Unable to load andy renderable", Toast.LENGTH_LONG);
                                toast.setGravity(Gravity.CENTER, 0, 0);
                                toast.show();
                                return null;
                            });
            finalpos = new float[]{0, 0, -6f};
            leftInit = new float[]{-1.5f, 0, -6f};
            rightInit = new float[]{1f, 0, -6f};
        }

        float[] rotation = {0, 0, 0, 0};

        // get current frame and translate the initial coordinate to the current frame
        session = fragment.getArSceneView().getSession();
        try {
            frame = session.update();
        } catch (CameraNotAvailableException e) {
            e.printStackTrace();
        }
        Pose pose = frame.getAndroidSensorPose();
        if(targetScreenArea == "LEFT"){
            finalpos = pose.rotateVector(leftInit);
        } else if(targetScreenArea == "RIGHT"){
            finalpos = pose.rotateVector(rightInit);
        } else {
            finalpos = pose.rotateVector(finalpos);
        }

        Anchor anchor =  session.createAnchor(new Pose(finalpos, rotation));
        anchorNode = new AnchorNode(anchor);
        anchorNode.setParent(fragment.getArSceneView().getScene());
        TransformableNode transformableNode = new TransformableNode(fragment.getTransformationSystem());
        transformableNode.setParent(anchorNode);
        fragment.getArSceneView().getScene().addChild(anchorNode);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                transformableNode.setRenderable(model);
                transformableNode.select();
            }
        }, 1000);

    }

    public void resetAppUi(View view){
        if(null != responseObjectNode) {
            responseObjectNode.setParent(null);
            responseObjectNode = null;
        }

        editText.setText(R.string.tap_button_to_speak);
        sysMsgTextView.setText("");
        targetScreenArea = null;
        ImageView bbox=(ImageView) findViewById(R.id.bbox);
        bbox.setImageResource(0);
        onClear();

    }

    private void onClear() {
        List<Node> children = new ArrayList<>(fragment.getArSceneView().getScene().getChildren());
        for (Node node : children) {
            if (node instanceof AnchorNode) {
                if (((AnchorNode) node).getAnchor() != null) {
                    ((AnchorNode) node).getAnchor().detach();
                }
            }
            if (!(node instanceof Camera) && !(node instanceof Sun)) {
                node.setParent(null);
            }
        }
    }

    // Camera
    private void takePhoto() {
        String targetFileFullPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/temp_image.jpeg";
        ArSceneView arSceneView = fragment.getArSceneView();

        // Create a bitmap the size of the scene view.
        final Bitmap bitmap = Bitmap.createBitmap(arSceneView.getWidth(), arSceneView.getHeight(), Bitmap.Config.ARGB_8888);

        // Create a handler thread to offload the processing of the image.
        final HandlerThread handlerThread = new HandlerThread("PixelCopier");
        handlerThread.start();
        
        // Make the request to copy.
        PixelCopy.request(arSceneView, bitmap, (copyResult) -> {
            if (copyResult == PixelCopy.SUCCESS) {
                try {
                    saveImageBitmapToDisk(bitmap, targetFileFullPath);
                } catch (IOException e) {
                    Toast toast = Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_LONG);
                    toast.show();
                    return;
                }
                Log.d("takePhoto()", "Screenshot saved in " + targetFileFullPath);
                sysMsgTextView.setText(sysMsgTextView.getText() + "\n" + "Screenshot saved to"  + targetFileFullPath);
            } else {
                Log.e("takePhoto()","Failed to take screenshot");
                sysMsgTextView.setText(sysMsgTextView.getText() + "Failed to take screenshot");
            }
            handlerThread.quitSafely();
        }, new Handler(handlerThread.getLooper()));
    }


    public void saveImageBitmapToDisk(Bitmap bitmap, String targetFileFullPath) throws IOException {
        ByteArrayOutputStream binaryArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, binaryArrayOutputStream); // bm is the bitmap object
        byte[] b = binaryArrayOutputStream.toByteArray();
        String encodedImage = Base64.encodeToString(b, Base64.DEFAULT);
        Log.d("saveImageBitmapToDisk()", encodedImage);

        String path = String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES));
        File encodedText = new File(path, "encoded.txt");
        FileWriter writer = new FileWriter(encodedText);
        writer.append(encodedImage);
        writer.flush();
        writer.close();
    }

    public void displayMessage(String s) {
        sysMsgTextView.setText(s);
    }
}
