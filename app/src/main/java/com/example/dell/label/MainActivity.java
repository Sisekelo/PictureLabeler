package com.example.dell.label;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.cloud.FirebaseVisionCloudDetectorOptions;
import com.google.firebase.ml.vision.cloud.label.FirebaseVisionCloudLabel;
import com.google.firebase.ml.vision.cloud.label.FirebaseVisionCloudLabelDetector;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.label.FirebaseVisionLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionLabelDetector;
import com.google.firebase.ml.vision.label.FirebaseVisionLabelDetectorOptions;
import com.google.firebase.ml.vision.text.FirebaseVisionCloudTextRecognizerOptions;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.util.List;

public class MainActivity extends BaseActivity implements View.OnClickListener {
    private Bitmap mBitmap;
    private ImageView mImageView;
    private TextView mTextView;
    String status,text,combo;

    private static final int CAMERA_REQUEST = 1888;
    private ImageView imageView;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = findViewById(R.id.textView);
        mImageView = findViewById(R.id.imageView);
        //findViewById(R.id.btn_device).setOnClickListener(this);
        findViewById(R.id.btn_cloud).setOnClickListener(this);

        this.imageView = (ImageView) this.findViewById(R.id.imageView1);
        Button photoButton = (Button) this.findViewById(R.id.button1);

        photoButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) ==
                        PackageManager.PERMISSION_DENIED) {

                    ActivityCompat.requestPermissions(MainActivity.this, new String[] {
                            Manifest.permission.CAMERA
                    }, MY_CAMERA_PERMISSION_CODE);
                }

                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);


            }
        });
    }

    @Override
    public void onClick(View view) {
        mTextView.setText(null);
        combo="";
        switch (view.getId()) {
            case R.id.btn_cloud:
                if (mBitmap != null) {
                    //MyHelper.showDialog(this);

                    //check if it has the Heineken label

                    FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(mBitmap);

                    FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance()
                            .getCloudTextRecognizer();

                    final Context home = this;

                    Task < FirebaseVisionText > result =
                            detector.processImage(image)
                                    .addOnSuccessListener(new OnSuccessListener < FirebaseVisionText > () {
                                        @Override
                                        public void onSuccess(FirebaseVisionText firebaseVisionText) {



                                            for (FirebaseVisionText.TextBlock block: firebaseVisionText.getTextBlocks()) {
                                                Rect boundingBox = block.getBoundingBox();
                                                Point[] cornerPoints = block.getCornerPoints();

                                                text = block.getText().toLowerCase().trim();


                                                if (text.equalsIgnoreCase("heineken")) {
                                                    Toast.makeText(MainActivity.this, "This is a Heineken", Toast.LENGTH_SHORT).show();
                                                    combo+=text;

                                                }

                                                mTextView.append(text + "\n\n");
                                                //MyHelper.dismissDialog();

                                            }
                                        }

                                    })
                                    .addOnFailureListener(
                                            new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    // Task failed with an exception
                                                    // ...
                                                }
                                            });


                    //check if it is a bottle
                    //MyHelper.showDialog(this);
                    FirebaseVisionCloudDetectorOptions options = new FirebaseVisionCloudDetectorOptions.Builder()
                            .setModelType(FirebaseVisionCloudDetectorOptions.LATEST_MODEL)
                            .setMaxResults(20)
                            .build();

                    FirebaseVisionImage image2 = FirebaseVisionImage.fromBitmap(mBitmap);
                    FirebaseVisionCloudLabelDetector detector2 = FirebaseVision.getInstance().getVisionCloudLabelDetector(options);
                    detector2.detectInImage(image2).addOnSuccessListener(new OnSuccessListener < List < FirebaseVisionCloudLabel >> () {
                        @Override
                        public void onSuccess(List < FirebaseVisionCloudLabel > labels) {

                            mTextView.append("\n\nLabels\n\n");
                            for (FirebaseVisionCloudLabel label: labels) {
                                mTextView.append(label.getLabel() + ": " + label.getConfidence() + "\n\n");
                                //mTextView.append(label.getEntityId() + "\n");

                                status = label.getLabel().trim();

                                if (status.equalsIgnoreCase("bottle")) {
                                    Toast.makeText(MainActivity.this, "This is a Bottle", Toast.LENGTH_SHORT).show();
                                    combo+=status;

                                    Toast.makeText(MainActivity.this, ""+combo, Toast.LENGTH_SHORT).show();

                                    if(combo.equalsIgnoreCase( "bottleheineken") || combo.equalsIgnoreCase( "heinekenbottle" ) ){

                                        notification( "great","you get 2 points" );
                                    }
                                }

                            }

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //MyHelper.dismissDialog();
                            mTextView.setText(e.getMessage());
                        }
                    });



                }
                break;
        }
    }
    
    public boolean checkType(){
        
        if(text.equalsIgnoreCase( "heineken") && status.equalsIgnoreCase( "bottle" ) ){
            return true;
        }
        else if(!text.equalsIgnoreCase( "heineken") && status.equalsIgnoreCase( "bottle" )){
            notification( "Error","This is not a Heineken bottle" );
            return false;
        }
        else if(text.equalsIgnoreCase( "heineken") && !status.equalsIgnoreCase( "bottle" )){
            notification( "Error","This is not a bottle" );
            return false;
        }
        else {
            notification( "Error","This is not a bottle or a Heineken product" );
            return false;
        }
    }

    public void notification(String title,String message){

        new AlertDialog.Builder(MainActivity.this)
                .setTitle(title)
                .setMessage(message)
                // Specifying a listener allows you to take an action before dismissing the dialog.
                // The dialog is automatically dismissed when a dialog button is clicked.
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Continue with delete operation
                    }
                })
                .show();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case RC_STORAGE_PERMS1:
                    checkStoragePermission(requestCode);
                    break;
                case RC_SELECT_PICTURE:
                    Uri dataUri = data.getData();
                    String path = MyHelper.getPath(this, dataUri);
                    if (path == null) {
                        mBitmap = MyHelper.resizeImage(imageFile, this, dataUri, mImageView);
                    } else {
                        mBitmap = MyHelper.resizeImage(imageFile, path, mImageView);
                    }
                    if (mBitmap != null) {
                        mTextView.setText(null);
                        mImageView.setImageBitmap(mBitmap);
                    }

            }
        }

        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            mImageView.setImageBitmap(photo);
            mBitmap = photo;
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            } else {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }

    }




}