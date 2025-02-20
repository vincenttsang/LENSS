/* ======================================================================
 *  Copyright � 2014 Qualcomm Technologies, Inc. All Rights Reserved.
 *  QTI Proprietary and Confidential.
 *  =====================================================================
 *  
 * @file:   CameraSurfacePreview.java
 *
 */


package com.qualcomm.sdk.smartshutterapp;

import java.io.IOException;
import java.util.List;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CameraSurfacePreview extends SurfaceView implements SurfaceHolder.Callback{
	
	private SurfaceHolder mHolder;
    private Camera mCamera;
    Context mContext;
    private int MAX_NUM_BYTES = 1572864;		// Each image is supported for display, upto 1.5 Mb = 1572864 bytes. 
    
	@SuppressWarnings("deprecation")
	public CameraSurfacePreview(Context context, Camera camera) {
		super(context);
		mCamera = camera;
		mContext = context;
		// Install a SurfaceHolder.Callback so we get notified when the underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
    
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}	

	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// The Surface has been created, now tell the camera where to draw the preview.
		
		
        try 
        {
        	mCamera.setDisplayOrientation(90);
        	mCamera.setPreviewDisplay(holder);  
        	Camera.Parameters pm = mCamera.getParameters();
        	
        	int index = 0;
        	//pm.setPictureFormat(ImageFormat.NV21);
        	int format = pm.getPictureFormat();
        	List<Size> pictSize = pm.getSupportedPictureSizes();
        	for(int i=0; i<pictSize.size(); i++)
        	{
        		int width = pictSize.get(i).width;
        		int height = pictSize.get(i).height;
        		int size = width*height*3/2;
        		if(size<MAX_NUM_BYTES)		
        		{
        			index = i;
        			break;
        		}        		
        	}
        	//int indx = pictSize.size() - 2;
        	pm.setPictureSize(pictSize.get(index).width, pictSize.get(index).height);
        	
        	Log.e("TAG", "FORMAT"+format);
        	Log.d("CameraSurfaceView", pictSize.size()+"Picture dimension: "+pictSize.get(0).width+"x"+pictSize.get(0).height);
        	mCamera.setParameters(pm);
            mCamera.startPreview();                
        } 
        catch (IOException e) 
        {
            Log.d("TAG", "Error setting camera preview: " + e.getMessage());
        }
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {	
		
		
	}


}
