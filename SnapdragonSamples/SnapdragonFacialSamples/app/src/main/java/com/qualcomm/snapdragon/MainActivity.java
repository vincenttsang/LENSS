package com.qualcomm.snapdragon;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.qualcomm.snapdragon.sdk.sample.CameraPreviewActivity;
import com.qualcomm.snapdragon.sdk.sample.R;

/**
 * Created by yukun on 12/1/2016.
 */

public class MainActivity extends Activity {
    public final static String EXTRA_MSG = "com.qualcomm.snapdragon.sdk.sample.MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        Button facialProcBtn = (Button) this.findViewById(R.id.button_processing);
        facialProcBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CameraPreviewActivity.class);
                intent.putExtra(EXTRA_MSG, "call facial proc sample");
                startActivity(intent);
            }
        });

//        Button facialRecoBtn = (Button) this.findViewById(R.id.btn_recog);
//        facialRecoBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(MainActivity.this, )
//            }
//        });
    }
}
