package com.example.androidrecorder.Camera;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.androidrecorder.Constants;
import com.example.androidrecorder.R;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;

public class CameraXActivity extends AppCompatActivity {

    private static final String TAG = "CameraxActivity";
    private PreviewView mViewFinder;//一种可以裁剪缩放和旋转以确保正确显示的view
    private ImageCapture mImageCapture;
    private int mFacing = CameraSelector.LENS_FACING_BACK;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_xactivity);

        //PreviewView,这是一种可以裁剪，缩放和旋转以确保正确显示的View
        mViewFinder = findViewById(R.id.viewFinder);

    }

    private void startCamera() {
        //返回当前可以绑定的生命周期ProcessCameraProvider
        //ProcessCameraProvider会和宿主绑定生命周期，不用担心打开相机和关闭问题
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        //向cameraProviderFuture注册一个监听，第一个参数是一个Runnable
        // 第二个参数是一个线程池，即当前Runnable运行到那个线程
        cameraProviderFuture.addListener(new Runnable() {
            @SuppressLint("RestrictedApi")
            @Override
            public void run() {

                try {

                    //将相机的生命周期和activity的生命周期进行绑定，camerax会自己进行释放
                    ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                    //预览的capture,它里面支持角度换算
                    Preview preview = new Preview.Builder().build();

                    //创建图片的capture
                    mImageCapture = new ImageCapture.Builder()
                            .setFlashMode(ImageCapture.FLASH_MODE_AUTO)
                            .build();

                    //选择后置摄像头
                    CameraSelector cameraSelector = new CameraSelector.Builder()
                            .requireLensFacing(mFacing)
                            .build();

                    //预览之前先解绑
                    cameraProvider.unbindAll();

                    //将数据绑定到相机的生命周期中
                    Camera camera = cameraProvider.bindToLifecycle(CameraXActivity.this
                            ,cameraSelector, preview, mImageCapture);

                    //将PreviewView的Surface给相机预览
                    preview.setSurfaceProvider(mViewFinder.createSurfaceProvider(camera.getCameraInfo()));

                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        }, ContextCompat.getMainExecutor(this));
    }

    public void takePhoto(View view){
        if(mImageCapture != null){
            File dir = new File(Constants.PATH);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            //创建文件
            File file = new File(Constants.PATH,"testx.jpg");
            if (file.exists()) {
                file.delete();
            }
            //创建包文件的数据，比如创建文件
            ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(file).build();

            //开始拍照
            mImageCapture.takePicture(outputFileOptions, ContextCompat.getMainExecutor(this),
                    new ImageCapture.OnImageSavedCallback() {
                        @Override
                        public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                            Toast.makeText(CameraXActivity.this, "保存成功: ", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(@NonNull ImageCaptureException exception) {
                            Toast.makeText(CameraXActivity.this, "保存失败", Toast.LENGTH_SHORT).show();
                        }
                    });

        }
    }

    public void switchCamera(View view){
        /**
         * 白屏的问题是 PreviewView 移除所有View，且没数据到 Surface，
         * 所以只留背景色，可以对次做处理
         */
        mFacing =  mFacing == CameraSelector.LENS_FACING_FRONT?
                CameraSelector.LENS_FACING_BACK:CameraSelector.LENS_FACING_FRONT;
        startCamera();
    }
}