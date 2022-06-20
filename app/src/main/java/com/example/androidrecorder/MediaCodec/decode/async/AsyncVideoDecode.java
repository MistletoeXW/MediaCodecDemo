package com.example.androidrecorder.MediaCodec.decode.async;

import android.graphics.SurfaceTexture;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Bundle;
import android.os.Message;
import android.view.Surface;

import androidx.annotation.NonNull;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 异步视频解码
 */
public class AsyncVideoDecode extends BaseAsyncDecode{

    private static final String TAG = "AsyncVideoDecode";
    private Surface mSurface;
    private long mTime = -1;
    private Map<Integer, MediaCodec.BufferInfo> map =
            new ConcurrentHashMap<>();

    public AsyncVideoDecode(SurfaceTexture surfaceTexture){
        super();
        mSurface = new Surface(surfaceTexture);
    }

    @Override
    public void start() {
        super.start();
        //设置mediaCodec异步解码回调
        mediaCodec.setCallback(new MediaCodec.Callback() {
            @Override
            public void onInputBufferAvailable(@NonNull MediaCodec mediaCodec, int index) {
                ByteBuffer inputBuffer = mediaCodec.getInputBuffer(index);
                int size = extractor.readBuffer(inputBuffer);
                if(size >= 0){
                    mediaCodec.queueInputBuffer(
                            index,
                            0,
                            size,
                            extractor.getSampleTime(),
                            extractor.getSampleFlags()
                    );
                }else{
                    //结束
                    mediaCodec.queueInputBuffer(
                            index,
                            0,
                            0,
                            0,
                            MediaCodec.BUFFER_FLAG_END_OF_STREAM
                    );
                }
            }

            @Override
            public void onOutputBufferAvailable(@NonNull MediaCodec codec, int index, @NonNull MediaCodec.BufferInfo info) {
                Message msg = new Message();
                msg.what = MSG_VIDEO_OUTPUT;
                Bundle bundle = new Bundle();
                bundle.putInt("index",index);
                bundle.putLong("time",info.presentationTimeUs);
                msg.setData(bundle);
                mHandler.sendMessage(msg);
            }

            @Override
            public void onError(@NonNull MediaCodec codec, @NonNull MediaCodec.CodecException e) {
                codec.stop();
            }

            @Override
            public void onOutputFormatChanged(@NonNull MediaCodec mediaCodec, @NonNull MediaFormat mediaFormat) {

            }
        });

        mediaCodec.configure(mediaFormat,mSurface,null,0);
        mediaCodec.start();
    }

    @Override
    protected int decodeType() {
        return VIDEO;
    }

    @Override
    public boolean handleMessage(@NonNull Message message) {
        switch (message.what){

            case MSG_VIDEO_OUTPUT:
                try {
                    if(mTime == -1){
                        mTime = System.currentTimeMillis();
                    }
                    Bundle bundle = message.getData();
                    int index = bundle.getInt("index");
                    long ptsTime = bundle.getLong("time");
                    sleepRender(ptsTime,mTime);
                    mediaCodec.releaseOutputBuffer(index, true);
                }catch (Exception e){
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }

        return super.handleMessage(message);
    }

    /**
     * 数据时间戳对齐：与系统时间进行对齐
     * @param ptsTimes
     * @param startMs
     * @return
     */
    private long sleepRender(long ptsTimes,long startMs){
        /**
         * 注意这里是以 0 为出事目标的，info.presenttationTimes 的单位为微秒
         * 这里用系统时间来模拟两帧的时间差
         */
        ptsTimes = ptsTimes / 1000;
        long systemTimes = System.currentTimeMillis() - startMs;
        long timeDifference = ptsTimes - systemTimes;
        // 如果当前帧比系统时间差快了，则延时以下
        if (timeDifference > 0) {
            try {
                //todo 受系统影响，建议还是用视频本身去告诉解码器 pts 时间
                Thread.sleep(timeDifference);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
        return timeDifference;
    }
}
