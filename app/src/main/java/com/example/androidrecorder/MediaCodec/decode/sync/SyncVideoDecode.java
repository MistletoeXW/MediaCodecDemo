package com.example.androidrecorder.MediaCodec.decode.sync;

import android.graphics.SurfaceTexture;
import android.media.MediaCodec;
import android.util.Log;

/**
 * 视频解码
 */
public class SyncVideoDecode extends BaseSyncDecode{

    private static final String TAG = "VideoDecodeSync";

    //用于对准视频的时间戳
    private long mStartMs = -1;

    public SyncVideoDecode(SurfaceTexture surfaceTexture){
        super(surfaceTexture);
    }

    @Override
    protected int decodeType() {
        return VIDEO;
    }

    @Override
    protected void configure(){
        //config对应的MediaCodec类型
        mediaCodec.configure(mediaFormat,mSurface,null,0);
    }

    @Override
    protected boolean handleOutputData(MediaCodec.BufferInfo info) {
        //等到拿到输出的buffer下标
        int outputId = mediaCodec.dequeueOutputBuffer(info,TIME_US);

        if(mStartMs == -1){
            //将时间戳对准当前时间
            mStartMs = System.currentTimeMillis();
        }

        while(outputId >= 0){
            //矫正pts
            sleepRender(info,mStartMs);

            //释放buffer,并渲染到Surface中
            mediaCodec.releaseOutputBuffer(outputId,true);

            //在所有解码后的帧都被渲染后，可以停止播放了
            if((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0){
                Log.e(TAG, "OutputBuffer BUFFER_FLAG_END_OF_STREAM");

                return true;
            }

            outputId = mediaCodec.dequeueOutputBuffer(info,TIME_US);
        }

        return false;
    }

    /**
     * 数据的时间戳对齐
     * @param info
     * @param startMs
     */
    private void sleepRender(MediaCodec.BufferInfo info,long startMs){

        /**
         * 这里使用系统时间来模拟两帧的时间差
         * info.presenttationTimes 的单位为微秒
         */
        long ptsTime = info.presentationTimeUs / 1000;
        long systemTimes = System.currentTimeMillis() - startMs;
        long timeDifference = ptsTime - systemTimes;
        //如果当前帧比系统时间快，则进行延时
        if(timeDifference > 0){
            try {
                Thread.sleep(timeDifference);
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }

    }
}
