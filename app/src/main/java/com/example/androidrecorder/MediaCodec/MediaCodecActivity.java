package com.example.androidrecorder.MediaCodec;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.androidrecorder.R;

public class MediaCodecActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_codec);
    }

    public void decoder(View view) {
        startActivity(new Intent(this,DecodeMediaActivity.class));
    }


    public void encodeAcc(View view) {
        //startActivity(new Intent(this, AacCodecActivity.class));
    }
}