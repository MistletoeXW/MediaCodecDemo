package com.example.androidrecorder.MediaCodec;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.SurfaceTexture;
import android.media.MediaFormat;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;

import com.example.androidrecorder.Constants;
import com.example.androidrecorder.MediaCodec.decode.async.AsyncAudioDecode;
import com.example.androidrecorder.MediaCodec.decode.async.AsyncVideoDecode;
import com.example.androidrecorder.MediaCodec.decode.sync.SyncAudioDecode;
import com.example.androidrecorder.MediaCodec.decode.sync.SyncVideoDecode;
import com.example.androidrecorder.R;
import com.example.androidrecorder.utils.MyExtractor;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;

public class DecodeMediaActivity extends AppCompatActivity {

    private static final String TAG = "DecodeMediaActivity";
    private TextureView mTextureView;
    private SyncVideoDecode mVideoSync;
    private SyncAudioDecode mAudioDecodeSync;
    //创建线程池大小为2
    private ExecutorService mExecutorService = Executors.newFixedThreadPool(2);
    private Handler handler;
    private BlockingQueue<Integer> mBlockingQueue = new LinkedBlockingDeque<>();
    private BlockingQueue<Integer> mAudioBlockingQueue = new LinkedBlockingDeque<>();

    private AsyncVideoDecode mAsyncVideo;
    private AsyncAudioDecode mAsyncAudio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_decode_media);

        init();
    }

    /**
     * 配置TextureView
     */
    private void init(){
        mTextureView = findViewById(R.id.surface);
        mTextureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
                //使用Extractor来获取视频帧的宽高数据
                Log.i("xuwen","Constants.VIDEO_PATH="+Constants.VIDEO_PATH);
                MyExtractor myExtractor = new MyExtractor(Constants.VIDEO_PATH);
                MediaFormat videoFormat = myExtractor.getVideoFormat();
                int viewWidth = videoFormat.getInteger(MediaFormat.KEY_WIDTH);
                int viewHeight = videoFormat.getInteger(MediaFormat.KEY_HEIGHT);
                ViewGroup.LayoutParams params = mTextureView.getLayoutParams();
                params.width = viewWidth;
                params.height = viewHeight;
                mTextureView.setLayoutParams(params);
            }

            @Override
            public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {

            }
        });


    }

    public void sync(View view) {
        stopMedia();

        mVideoSync = new SyncVideoDecode(mTextureView.getSurfaceTexture());
        mAudioDecodeSync = new SyncAudioDecode();

        mExecutorService.execute(mVideoSync);
        mExecutorService.execute(mAudioDecodeSync);
    }

    public void async(View view) {
        stopMedia();
        // mExecutorService.shutdownNow();

        mAsyncVideo = new AsyncVideoDecode(mTextureView.getSurfaceTexture());

        mAsyncVideo.start();

        mAsyncAudio = new AsyncAudioDecode();
        mAsyncAudio.start();
    }

    private void stopMedia(){
        if(mExecutorService != null){
            mExecutorService.shutdown();
        }

        mExecutorService = Executors.newFixedThreadPool(2);

        if(mVideoSync != null){
            mVideoSync.done();
        }

        if(mAudioDecodeSync != null){
            mAudioDecodeSync.done();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mVideoSync != null) {
            mVideoSync.done();
        }
        if (mAudioDecodeSync != null) {
            mAudioDecodeSync.done();
        }
        mExecutorService.shutdown();
    }
}