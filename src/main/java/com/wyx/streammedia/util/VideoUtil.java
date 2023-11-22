package com.wyx.streammedia.util;

import com.wyx.streammedia.common.Resolution;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.FFmpegLogCallback;
import org.bytedeco.javacv.Frame;


/**
 * @BelongsProject: StreamMedia
 * @BelongsPackage: com.wyx.streammedia.util
 * @Author: Origami
 * @Date: 2023/11/20 22:55
 */
public class VideoUtil {
    private static final int M = 1000000;
    private static final String P1080 = "1080P";
    private static final String P720 = "720P";


    public static void main(String[] args) {
        String inputFileName = "D:\\university\\project\\StreamMedia\\src\\main\\resources\\static\\test.mp4";
        String outputFilePath = "D:\\university\\project\\StreamMedia\\src\\main\\resources\\static\\v1.mp4";
        int bitrate = getBitRate(inputFileName);
        System.out.println("bitRate = " + bitrate);
        selectByOriginalBitrate(inputFileName, outputFilePath, bitrate);

    }

    /**
     * 通过原画帧数来选择压缩的帧数
     */
    public static void selectByOriginalBitrate(String inputFileName, String outputFilePath, int originalBitrate) {
        long timestamp = System.currentTimeMillis();
        String outputFileName;
        // 原画
        outputFileName = outputFilePath + "\\" + timestamp + "_original_resolution.mp4";
        System.out.println("outputFileName = " + outputFileName);
        slice(inputFileName, "original");

        // 1080P
        Resolution resolution1080p = Resolution.RESOLUTION_1080P;
        int bitrate1080 = (int) (resolution1080p.getValue() * M);
        System.out.println("bitrate1080 = " + bitrate1080);
        if (originalBitrate > bitrate1080) {
            outputFileName = outputFilePath + "\\" + timestamp + "_1080p.mp4";
            encodeVideo(inputFileName, outputFileName, originalBitrate);
            slice(outputFileName, P1080);
        }

        // 720P
        Resolution resolution720p = Resolution.RESOLUTION_720P;
        int bitrate720 = (int) (resolution720p.getValue() * M);
        System.out.println("bitrate720 = " + bitrate720);
        if (originalBitrate > bitrate720) {
            outputFileName = outputFilePath + "\\" + timestamp + "_720p.mp4";
            encodeVideo(inputFileName, outputFileName, originalBitrate);
            slice(outputFileName, P720);
        }


    }

    public static int getBitRate(String filePath) {
        String[] cmd = {"ffprobe", "-i", filePath, "-show_entries", "format=bit_rate", "-v", "quiet", "-of", "csv=\"p=0\""};
        return Integer.parseInt(CommandUtil.exec(cmd));
    }

    public static void slice(String filePath, String resolution) {
        long timestamp = System.currentTimeMillis();
        String[] cmd = {"ffmpeg",
                "-i",
                filePath,
                "-c:v",
                "libx264",
                "-c:a",
                "aac",
                "-f",
                "dash",
                resolution + "_" + timestamp + ".mpd"};
        CommandUtil.execWithStream(cmd);
    }


    // 设置不同的码率来压缩视频
    public static void encodeVideo(String inputFileName, String outputFileName, int bitrate) {
        try {
            FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(inputFileName);
            grabber.setFormat("mp4");
            FFmpegLogCallback.set();
            grabber.start();
            FFmpegFrameRecorder
                    recorder = new FFmpegFrameRecorder(outputFileName, grabber.getImageWidth(), grabber.getImageHeight());
            recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
            recorder.setFormat("mp4");
            // FIX ME: set audio_channel=1 to avoid error, it may set other vals
            recorder.setAudioChannels(1);
            recorder.setVideoBitrate(bitrate);
            recorder.start();
            Frame frame;
            while ((frame = grabber.grabFrame()) != null) {
                recorder.record(frame);
            }
            recorder.stop();
            grabber.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void splitVideo(String inputFileName, String outputFileName) {
        // ffmpeg -i test.mp4 -vcodec copy -an v1.mp4
        String[] cmd = {
                "ffmpeg",
                "-i",
                inputFileName,
                "-vcodec",
                "copy",
                "-an",
                outputFileName
        };
        CommandUtil.exec(cmd);
    }


    public static void splitAudio(String inputFileName, String outputFileName) {
        // ffmpeg -i test.mp4 -acodec copy -vn a1.mp4
        String[] cmd = {
                "ffmpeg",
                "-i",
                inputFileName,
                "-acodec",
                "copy",
                "-vn",
                outputFileName
        };
        CommandUtil.exec(cmd);
    }

}
