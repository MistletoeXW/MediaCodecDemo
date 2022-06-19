package com.example.androidrecorder.MediaCodec.decode;

import android.media.MediaCodec;
import android.media.MediaFormat;

import com.example.androidrecorder.Constants;
import com.example.androidrecorder.utils.MyExtractor;

import java.io.IOException;

public abstract class BaseCodec {
    private static final String TAG = "BaseCodec";
    protected final static int VIDEO = 1;
    protected final static int AUDIO = 2;
    protected MediaFormat mediaFormat;//当前数据帧信息
    protected MediaCodec mediaCodec;//解码器
    protected MyExtractor extractor;//解析器

    public BaseCodec(){

        try {
            //获取MediaExtractor
            extractor = new MyExtractor(Constants.VIDEO_PATH);
            //判断是音频还是视频
            int type = decodeType();
            //拿到音频或视频的MediaFormat
            mediaFormat = (type == VIDEO ? extractor.getVideoFormat() : extractor.getAudioFormat());
            String mime = mediaFormat.getString(MediaFormat.KEY_MIME);
            //选择需要解析的轨道
            extractor.selectTrack(type == VIDEO ? extractor.getVideoTrackId() : extractor.getAudioTrackId());
            //创建MediaCodec
            mediaCodec = MediaCodec.createDecoderByType(mime);

        }catch (IOException e){
            e.printStackTrace();
        }

    }

    protected abstract int decodeType();

}
