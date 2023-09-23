package org.example;// 导入相关的类
import org.bytedeco.javacv.*;
import org.bytedeco.javacpp.*;

import static org.bytedeco.ffmpeg.global.avcodec.AV_CODEC_ID_H264;

// 创建一个录屏器类
public class ScreenRecorder {

    // 定义一些属性
    private FFmpegFrameGrabber grabber; // 用于抓取屏幕画面
    private FFmpegFrameRecorder recorder; // 用于录制视频文件
    private CanvasFrame canvas; // 用于显示实时画面
    private boolean recording; // 用于标记是否正在录制

    // 构造方法，传入输出文件名和帧率
    public ScreenRecorder(String filename, int frameRate) {
        try {
            FFmpegLogCallback.set();
            // 创建屏幕抓取器，设置参数
            grabber = new FFmpegFrameGrabber("1");
            grabber.setFormat("avfoundation");
            grabber.setFrameRate(frameRate);

            // 创建视频录制器，设置参数
            recorder = new FFmpegFrameRecorder(filename, grabber.getImageWidth(), grabber.getImageHeight());
            recorder.setFormat("mp4");
            recorder.setVideoCodec(AV_CODEC_ID_H264);
            recorder.setFrameRate(frameRate);

            // 创建画布窗口，设置参数
            canvas = new CanvasFrame("Screen Recorder");
            canvas.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
            canvas.setCanvasSize(grabber.getImageWidth(), grabber.getImageHeight());

            // 初始化录制状态为false
            recording = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 开始录制的方法
    public void start() {
        try {
            // 启动抓取器和录制器
            grabber.start();
            recorder.start();

            // 设置录制状态为true
            recording = true;

            // 创建一个新的线程，循环抓取和录制画面
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (recording) {
                        try {
                            // 抓取一帧画面
                            Frame frame = grabber.grab();

                            // 如果画面不为空，进行录制和显示
                            if (frame != null) {
                                // 录制一帧画面
                                recorder.record(frame);

                                // 在画布上显示一帧画面
                                canvas.showImage(frame);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 停止录制的方法
    public void stop() {
        try {
            // 设置录制状态为false
            recording = false;

            // 停止抓取器和录制器
            grabber.stop();
            recorder.stop();

            // 关闭画布窗口
            canvas.dispose();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 测试方法，创建一个录屏器对象，开始和停止录制
    public static void main(String[] args) throws Exception {
        ScreenRecorder screenRecorder = new ScreenRecorder("output.mp4", 30); // 输出文件名为output.mp4，帧率为30
        screenRecorder.start(); // 开始录制

        Thread.sleep(10000); // 等待10秒

        screenRecorder.stop(); // 停止录制
    }
}
