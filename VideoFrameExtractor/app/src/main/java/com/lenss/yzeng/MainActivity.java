package com.lenss.yzeng;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String SrcPath="/sdcard/videoplayback.mp4";

        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(SrcPath, new HashMap<String, String>());
        String METADATA_KEY_DURATION = mediaMetadataRetriever
                .extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);

        Bitmap bmpOriginal = mediaMetadataRetriever.getFrameAtTime(0);
        int bmpVideoHeight = bmpOriginal.getHeight();
        int bmpVideoWidth = bmpOriginal.getWidth();

        Log.d("LOGTAG", "bmpVideoWidth:'" + bmpVideoWidth + "'  bmpVideoHeight:'" + bmpVideoHeight + "'");

        byte [] lastSavedByteArray = new byte[0];

        float factor = 0.20f;
        int scaleWidth = (int) ( (float) bmpVideoWidth * factor );
        int scaleHeight = (int) ( (float) bmpVideoHeight * factor );
        int max = (int) Long.parseLong(METADATA_KEY_DURATION);
        for ( int index = 0 ; index < max ; index++ )
        {
            bmpOriginal = mediaMetadataRetriever.getFrameAtTime(index * 1000, MediaMetadataRetriever.OPTION_CLOSEST);
            if (bmpOriginal == null){
                continue;
            }
            bmpVideoHeight = bmpOriginal == null ? -1 : bmpOriginal.getHeight();
            bmpVideoWidth = bmpOriginal == null ? -1 : bmpOriginal.getWidth();
            int byteCount = bmpOriginal.getWidth() * bmpOriginal.getHeight() * 4;
            ByteBuffer tmpByteBuffer = ByteBuffer.allocate(byteCount);
            bmpOriginal.copyPixelsToBuffer(tmpByteBuffer);
            byte [] tmpByteArray = tmpByteBuffer.array();

            if ( !Arrays.equals(tmpByteArray, lastSavedByteArray))
            {
                int quality = 100;
                String mediaStorageDir="/sdcard/Pictures/";
                File outputFile = new File(mediaStorageDir , "IMG_" + ( index + 1 )
                        + "_" + max + "_quality_" + quality + "_w" + scaleWidth + "_h" + scaleHeight + ".png");
                Log.e("Output Files::>>",""+outputFile);
                OutputStream outputStream = null;
                try {
                    outputStream = new FileOutputStream(outputFile);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                Bitmap bmpScaledSize = Bitmap.createScaledBitmap(bmpOriginal, scaleWidth, scaleHeight, false);

                bmpScaledSize.compress(Bitmap.CompressFormat.PNG, quality, outputStream);

                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                lastSavedByteArray = tmpByteArray;
            }
        }
        ImageView capturedImageView = new ImageView(this);
        //setting image resource
        //capturedImageView.setImageResource(R.drawable.play);

        //setting image position
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        capturedImageView.setLayoutParams(params);

        //adding view to layout
        LinearLayout linearLayout = (LinearLayout) this.findViewById(R.id.linearLayout);
        linearLayout.addView(capturedImageView);

        capturedImageView.setImageBitmap(bmpOriginal);
        mediaMetadataRetriever.release();
    }
}
