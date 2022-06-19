package com.example.androidrecorder.utils;

import android.media.MediaExtractor;
import android.media.MediaFormat;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * 解析 MediaExtractor
 */
public class MyExtractor {
    MediaExtractor mediaExtractor;//解析器
    int videoTrackId;//视频轨道ID
    int audioTrackId;//音频轨道ID
    MediaFormat videoFormat;//视频数据
    MediaFormat audioFormat;//音频数据
    long curSampleTime;
    int curSampleFlags;

    /**
     * 解析路径文件的视频和音频信息
     * @param path：数据文件路径
     */
    public MyExtractor(String path){
        try {
            mediaExtractor = new MediaExtractor();
            //设置数据源
            mediaExtractor.setDataSource(path);
        }catch (IOException e) {
            e.printStackTrace();
        }

        //拿到所有的轨道
        int count = mediaExtractor.getTrackCount();
        for(int i=0;i<count;i++){
            //根据下标拿到MediaFormat
            MediaFormat format = mediaExtractor.getTrackFormat(i);
            //拿到mime类型
            String mime = format.getString(MediaFormat.KEY_MIME);
            //拿到视频轨
            if (mime.startsWith("video")) {
                videoTrackId = i;
                videoFormat = format;
            } else if (mime.startsWith("audio")) {
                //拿到音频轨
                audioTrackId = i;
                audioFormat = format;
            }
        }
    }

    /**
     * 选择对应的轨道
     * @param trackId
     */
    public void selectTrack(int trackId){
        mediaExtractor.selectTrack(trackId);
    }

    /**
     * 读取一帧数据
     * @param buffer:帧数据buffer
     * @return
     */
    public int readBuffer(ByteBuffer buffer){
        //首先清空buffer
        buffer.clear();
        //选择需要解析的轨道
        //mediaExtractor.selectTrack(video ? videoTrackId : audioTrackId);
        //读取当前帧的数据,拿到视频的当前帧的buffer
        int buffercount = mediaExtractor.readSampleData(buffer,0);
        if(buffercount < 0){
            return -1;
        }
        //记录当前时间戳
        curSampleTime = mediaExtractor.getSampleTime();
        //记录当前帧的标志位
        curSampleFlags = mediaExtractor.getSampleFlags();
        //进入下一帧
        mediaExtractor.advance();
        return buffercount;
    }

    /**
     * 获取视频轨道Id
     * @return
     */
    public int getVideoTrackId() {
        return videoTrackId;
    }

    /**
     * 获取音频轨道Id
     * @return
     */
    public int getAudioTrackId() {
        return audioTrackId;
    }

    /**
     * 获取视频帧 MediaFormat
     * @return
     */
    public MediaFormat getVideoFormat(){
        return videoFormat;
    }

    /**
     * 获取音频 MediaFormat
     *
     * @return
     */
    public MediaFormat getAudioFormat() {
        return audioFormat;
    }

    /**
     * 获取当前帧的标志位
     *
     * @return
     */
    public int getSampleFlags() {
        return curSampleFlags;
    }

    /**
     * 获取当前帧的时间戳
     *
     * @return
     */
    public long getSampleTime() {
        return curSampleTime;
    }

    /**
     * 释放资源
     */
    public void release() {
        mediaExtractor.release();
    }
}
