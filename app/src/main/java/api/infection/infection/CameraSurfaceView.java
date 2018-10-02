package api.infection.infection;

import android.content.Context;
import android.hardware.Camera;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Initializes the Camera Surface
 * CODE REFERENCE: https://www.youtube.com/watch?v=QZp2gt9XmcQ
 */
public class CameraSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder surface_holder;
    private Camera camera = null;

    CameraSurfaceView(Context context) {
        super(context);

        surface_holder = getHolder();
        surface_holder.addCallback(this);
    }

    public void surface_createcamera() {
        try {
            camera = Camera.open();
            camera.setPreviewDisplay(surface_holder);
        } catch (Exception e) {

        }
    }

    public void surface_changecamera() {
        Camera.Parameters param = camera.getParameters();
        camera.setParameters(param);
        camera.setDisplayOrientation(90);
        camera.startPreview();
    }

    public void surface_closecamera() {
        camera.stopPreview();
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        surface_createcamera();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        surface_changecamera();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        surface_closecamera();
        camera = null;
    }

    public Camera get_camera() {
        return camera;
    }

    public void set_camera(Camera c) {
        camera = c;
    }
}
