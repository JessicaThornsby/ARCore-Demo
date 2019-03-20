package com.jessicathornsby.arcoredemo;

import android.app.Activity;
import android.app.ActivityManager;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;

import androidx.annotation.RequiresApi;

import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;

import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final double MIN_OPENGL_VERSION = 3.0;

    private ModelRenderable dinoRenderable;
    private ArFragment arCoreFragment;

    @RequiresApi(api = VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!checkDevice((this))) {
            return;
        }

        setContentView(R.layout.activity_main);
        arCoreFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.main_fragment);

        if (Build.VERSION.SDK_INT >= VERSION_CODES.N) {
            ModelRenderable.builder()
                    .setSource(this, R.raw.dinosaur)
                    .build()
                    .thenAccept(renderable -> dinoRenderable = renderable)
                    .exceptionally(
                            throwable -> {
                                Log.e(TAG, "Unable to load renderable");
                                return null;
                            });
        }

        arCoreFragment.setOnTapArPlaneListener(
                (HitResult hitResult, Plane plane, MotionEvent motionEvent) -> {
                    if (dinoRenderable == null) {
                        return;
                    }

                    Anchor anchor = hitResult.createAnchor();
                    AnchorNode anchorNode = new AnchorNode(anchor);
                    anchorNode.setParent(arCoreFragment.getArSceneView().getScene());

                    TransformableNode transformableNode = new TransformableNode(arCoreFragment.getTransformationSystem());
                    transformableNode.setParent(anchorNode);
                    transformableNode.setRenderable(dinoRenderable);
                    transformableNode.select();
                });
    }

    public static boolean checkDevice(final Activity activity) {
        if (Build.VERSION.SDK_INT < VERSION_CODES.N) {
            Log.e(TAG, "Sceneform requires Android N or higher");
            activity.finish();
            return false;
        }
        String openGlVersionString =
                ((ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE))
                        .getDeviceConfigurationInfo()
                        .getGlEsVersion();
        if (Double.parseDouble(openGlVersionString) < MIN_OPENGL_VERSION) {
            Log.e(TAG, "Requires OpenGL ES 3.0 or higher");
            activity.finish();
            return false;
        }
        return true;
    }
}
