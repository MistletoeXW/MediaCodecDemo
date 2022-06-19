package com.example.androidrecorder.MediaCodec.decode.sync;

import android.graphics.SurfaceTexture;
import android.media.MediaCodec;
import android.view.Surface;

import com.example.androidrecorder.MediaCodec.decode.BaseCodec;

import java.nio.ByteBuffer;

/**
 * 解码基类，用于解码音视频
 */
public abstract class BaseSyncDecode extends BaseCodec implements Runnable{
    //等待时间
    protected final static int TIME_US = 10000;
    protected Surface mSurface;
    private boolean isDone;//是否完成
    private MediaCodec.BufferInfo mInfo = new MediaCodec.BufferInfo();

    public BaseSyncDecode(){
        super();
        //由子类进行配置
        configure();
        //开始工作，进入编解码状态
        mediaCodec.start();
    }

    public BaseSyncDecode(SurfaceTexture surfaceTexture) {
        super();
        mSurface = new Surface(surfaceTexture);
        //由子类去配置
        configure();
        //开始工作，进入编解码状态
        mediaCodec.start();
    }

    @Override
    public void run() {
        try {
            //编码
            while(!isDone){

                //延迟TIME_US等待拿到空的input buffer下标，单位为us
                //-1表示一直等待，直到拿到数据，0表示立即返回
                int inputBufferId = mediaCodec.dequeueInputBuffer(TIME_US);

                if(inputBufferId > 0){
                    //拿到可用的空的input buffer
                    ByteBuffer inputBuffer = mediaCodec.getInputBuffer(inputBufferId);
                    if(inputBuffer != null){
                        //通过 mediaExtractor.readSampleData(buffer, 0) 拿到视频的当前帧的buffer
                        //通过 mediaExtractor.advance() 拿到下一帧
                        int size = extractor.readBuffer(inputBuffer);

                        //解析数据
                        if(size >= 0){
                            mediaCodec.queueInputBuffer(
                                    inputBufferId,
                                    0,
                                    size,
                                    extractor.getSampleTime(),
                                    extractor.getSampleFlags()
                            );
                        }else {
                            //结束，传递end-of-stream标志
                            mediaCodec.queueInputBuffer(
                                    inputBufferId,
                                    0,
                                    0,
                                    0,
                                    MediaCodec.BUFFER_FLAG_END_OF_STREAM
                            );
                            isDone = true;
                        }
                    }
                }
                //解码输出交给子类
                boolean isFinish = handleOutputData(mInfo);
                if(isFinish){
                    break;
                }
            }

            done();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void done(){
        try {
            isDone = true;
            //释放mediacodec
            mediaCodec.stop();
            mediaCodec.release();
            //释放MediaExtractor
            extractor.release();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 处理输出缓存中的数据
     * @param info
     * @return
     */
    protected abstract boolean handleOutputData(MediaCodec.BufferInfo info);

    protected abstract void configure();
}
