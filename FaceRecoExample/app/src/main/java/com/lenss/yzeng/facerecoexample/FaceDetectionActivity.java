package com.lenss.yzeng.facerecoexample;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.lenss.yzeng.facerecoexample.R;

import org.opencv.android.Utils;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.opencv.imgcodecs.Imgcodecs.imread;

/**
 * Created by yukun on 10/7/2016.
 */

public class FaceDetectionActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_facedetection);
        faceDetection();
    }

    private void faceDetection(){

//        String haarPath = "android.resource://" +
//                "com.lenss.example.myapplication" +
//                "/" + R.xml.haarcascade_frontalface_default;
//        String testPicPath = "android.resource://" +
//                "com.lenss.example.myapplication" +
//                "/" + R.drawable.test;
//        String sdPath = Environment.getExternalStorageDirectory().getAbsolutePath();
//        String haarPath = sdPath + "/workspace/haarcascade_frontalface_default.xml";
//        String testPicPath = sdPath + "/workspace/test.jpg";
//        boolean isLoaded = faceDetector.load(haarPath);

        // load cascade file from application resources

        String haarPath = resToFile(R.raw.haarcascade_frontalface_default, "haarcascade_frontalface_default.xml");
        String testPicPath = resToFile(R.drawable.test_me, "test_me.jpg");

        CascadeClassifier faceDetector = new CascadeClassifier();
        Mat image = imread(testPicPath);
        boolean isEmpty = image.empty();

        faceDetector = new CascadeClassifier(haarPath);
        if(faceDetector.empty())
        {
            Log.v("MyActivity","--(!)Error loading A\n");
            return;
        }
        else
        {
            Log.v("MyActivity", "Loaded cascade classifier from " + haarPath);
        }

        //My Code
        MatOfRect faceDetections = new MatOfRect();
        faceDetector.detectMultiScale(image, faceDetections);

        System.out.println(String.format("Detected %s faces", faceDetections.toArray().length));

        for (Rect rect : faceDetections.toArray()) {
            Imgproc.rectangle(image, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height),
                    new Scalar(0, 255, 0));
//            Core.rectangle(image, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height),
//                    new Scalar(0, 255, 0));
        }

        Bitmap bm = Bitmap.createBitmap(image.cols(), image.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(image, bm);

        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        imageView.setImageBitmap(bm);
    }

    public String resToFile(final int rId, String fileName) {
        try {
            //input stream of raw resource we want
            InputStream is = getResources().openRawResource(rId);
            //create the res data file in resData directory
            File resDataDir = getDir("resData", Context.MODE_PRIVATE);
            File resDataFile = new File(resDataDir, fileName);
            //write to the res data file
            FileOutputStream os = new FileOutputStream(resDataFile);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            is.close();
            os.close();

            return resDataFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            Log.v("MyActivity", "Failed to load cascade. Exception thrown: " + e);
        }
        return null;
    }
}
