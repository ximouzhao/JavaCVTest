package test2;// 导入相关的类

import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.*;
import org.bytedeco.javacv.Frame;
import org.bytedeco.opencv.opencv_core.Mat;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicInteger;

import static org.bytedeco.ffmpeg.global.avcodec.AV_CODEC_ID_H264;
import static org.bytedeco.opencv.global.opencv_imgproc.*;

// 创建一个录屏器类
public class ScreenRecorder {

    private FFmpegFrameRecorder recorder; // 用于录制视频文件
    FFmpegFrameGrabber grabber;

    private boolean recording; // 用于标记是否正在录制

    // 构造方法，传入输出文件名和帧率
    public ScreenRecorder(String filename, int frameRate) throws AWTException {
        final Toolkit toolkit = Toolkit.getDefaultToolkit();
        final Rectangle screenbounds = new Rectangle(toolkit.getScreenSize());
        try {
            // 创建视频录制器，设置参数
            grabber = new FFmpegFrameGrabber("qqqg.mp3");
            // 一定要先start，然后才能获取SampleRate
            grabber.start();

            recorder = new FFmpegFrameRecorder(filename, screenbounds.width, screenbounds.height);
            recorder.setFormat("mp4");
            recorder.setVideoCodec(AV_CODEC_ID_H264);
            recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
            recorder.setSampleRate(grabber.getSampleRate());
            recorder.setFrameRate(frameRate);
            recorder.setAudioChannels(1);
            // 初始化录制状态为false
            recording = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 定义一个方法，用于将BufferedImage对象转换为Frame对象，并调整颜色模型
    public Frame convert(BufferedImage image) {
        // 创建一个Java2DFrameConverter对象，用于转换BufferedImage和Frame
        Java2DFrameConverter converter = new Java2DFrameConverter();

        // 将BufferedImage对象转换为Frame对象
        Frame frame = converter.convert(image);

        // 创建一个OpenCVFrameConverter对象，用于转换Frame和Mat
        OpenCVFrameConverter.ToMat matConverter = new OpenCVFrameConverter.ToMat();

        // 将Frame对象转换为Mat对象
        Mat mat = matConverter.convert(frame);

        // 创建一个新的Mat对象，用于存储转换后的图像
        Mat newMat = new Mat();

        // 调用cvtColor函数，将RGB颜色模型转换为BGR颜色模型
        cvtColor(mat, newMat, COLOR_RGB2BGR);

        // 将新的Mat对象转换为新的Frame对象

        // 返回新的Frame对象
        return matConverter.convert(newMat);
    }

    public static BufferedImage convertToType(BufferedImage sourceImage,
                                              int targetType)
    {
        BufferedImage image;

        // if the source image is already the target type, return the source image

        if (sourceImage.getType() == targetType)
            image = sourceImage;

            // otherwise create a new image of the target type and draw the new
            // image

        else
        {
            image = new BufferedImage(sourceImage.getWidth(),
                    sourceImage.getHeight(), targetType);
            image.getGraphics().drawImage(sourceImage, 0, 0, null);
        }
        return image;
    }

    // 开始录制的方法
    public void start() {
        final Robot robot;
        final Toolkit toolkit = Toolkit.getDefaultToolkit();
        final Rectangle screenbounds = new Rectangle(toolkit.getScreenSize());
        try {
            robot = new Robot();
        } catch (AWTException e) {
            throw new RuntimeException(e);
        }
        try {
            // 启动抓取器和录制器
            recorder.start();

            // 设置录制状态为true
            recording = true;
            AtomicInteger i= new AtomicInteger(1);
            // 创建一个新的线程，循环抓取和录制画面
            new Thread(() -> {
                while (recording) {
                    try (Java2DFrameConverter converter = new Java2DFrameConverter()) {
                        // 抓取一帧画面
                        BufferedImage bufferedImage = robot.createScreenCapture(screenbounds);

                        final BufferedImage screen = convertToType(bufferedImage, BufferedImage.TYPE_3BYTE_BGR);

                        Frame videoFrame = converter.convert(screen);

                        // 如果画面不为空，进行录制
                        if (videoFrame != null) {
                            // 录制一帧画面
                            recorder.record(videoFrame);
                        }
                        System.out.println(i.getAndIncrement() +"b");
                        Thread.sleep(85); // 等待0.1秒
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();

            new Thread(() -> {
                while (recording) {
                    try {
                        recorder.record(grabber.grabFrame());

                        Thread.sleep(85); // 等待0.1秒
                    } catch (Exception e) {
                        e.printStackTrace();
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

            //必须等待线程真正完成，否则就会提前关闭，导致报错
            Thread.sleep(1000); // 等待1秒
            // 停止抓取器和录制器
            recorder.stop();
            grabber.stop();
            grabber.release();

            // 释放资源
            recorder.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 测试方法，创建一个录屏器对象，开始和停止录制
    public static void main(String[] args) throws Exception {
        FFmpegLogCallback.set();
        ScreenRecorder screenRecorder = new ScreenRecorder("output.mp4", 5); // 输出文件名为output.mp4，帧率为30
        screenRecorder.start(); // 开始录制

        Thread.sleep(10000); // 等待10秒

        screenRecorder.stop(); // 停止录制
    }
}
