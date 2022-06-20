package com.example.androidrecorder.MediaCodec.decode.async;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import androidx.annotation.NonNull;

import com.example.androidrecorder.MediaCodec.decode.BaseCodec;

/**
 * 异步解码基类
 */
public class BaseAsyncDecode extends BaseCodec implements Handler.Callback {

    private static final String TAG = "BaseAsyncDecode";
    protected final static int MSG_AUDIO_OUTPUT = 0X011;
    protected final static int MSG_VIDEO_OUTPUT = 0X013;
    protected HandlerThread mHandlerThread;
    protected Handler mHandler;

    public BaseAsyncDecode(){
        super();

        mHandlerThread = new HandlerThread(decodeType() == VIDEO ? "videoThread" : "audioThread");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper(),this);
    }

    public void start(){
        if (mHandlerThread == null) {
            mHandlerThread = new HandlerThread(decodeType() == VIDEO ? "videoThread" : "audioThread");
            mHandlerThread.start();
            mHandler = new Handler(mHandlerThread.getLooper(), this);
        }
    }

    private void releaseMedia(){
        mediaCodec.release();

        //释放MediaExtractor
        extractor.release();
    }

    private void stopMedia(){
        mediaCodec.stop();
    }

    public void release(){
        try{
            stop();
            releaseMedia();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void stop(){

        if(mHandlerThread != null){
            mHandlerThread.quit();
            mHandlerThread = null;
        }
        if(mHandler != null){
            mHandler.removeMessages(MSG_VIDEO_OUTPUT);
            mHandler.removeMessages(MSG_AUDIO_OUTPUT);
            mHandler = null;
        }

        stopMedia();
    }

    @Override
    protected int decodeType() {
        return 0;
    }

    @Override
    public boolean handleMessage(@NonNull Message message) {
        return false;
    }
}
