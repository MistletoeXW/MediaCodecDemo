package com.example.androidrecorder;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.androidrecorder.Camera.Camera2Activity;
import com.example.androidrecorder.Camera.CameraActivity;
import com.example.androidrecorder.Camera.CameraXActivity;
import com.example.androidrecorder.MediaCodec.MediaCodecActivity;
import com.example.androidrecorder.databinding.ActivityMainBinding;

import java.io.File;

public class MainActivity extends AppCompatActivity{

    private ActivityMainBinding binding;

    private Camera camera;//声明Camera类
    private SurfaceView surfaceView;//声明相机预览类
    private SurfaceHolder surfaceHolder;

    private int width = 1280;//视频图片宽
    private int height = 720;//视频图片高
    private int framerate = 30; //视频帧率

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        ActivityCompat.requestPermissions(this,
                new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.CAMERA
                },1);

        File file = new File(Constants.VIDEO_PATH);
        if (!file.exists()) {
            Toast.makeText(this, "找不到视频", Toast.LENGTH_SHORT).show();
        }
    }

    public void camera1(View view) {
        startActivity(new Intent(this, CameraActivity.class));
    }

    public void camera2(View view) {
        startActivity(new Intent(this, Camera2Activity.class));
        return;
    }

    public void camera3(View view) {
        startActivity(new Intent(this, CameraXActivity.class));
        return;
    }

    public void mediaCodec(View view) {
        startActivity(new Intent(this, MediaCodecActivity.class));
    }
}