package io.vec.demo.mediacodec;

import android.graphics.SurfaceTexture;
import android.view.Surface;

/**
 * Created by yukun on 11/20/2016.
 */

public class SurfaceWrapper extends Surface {
    public SurfaceWrapper(SurfaceTexture surfaceTexture){
        super(surfaceTexture);
    }

    public SurfaceTexture getSurfaceTexture(){
        return this.getSurfaceTexture();
    }
}
