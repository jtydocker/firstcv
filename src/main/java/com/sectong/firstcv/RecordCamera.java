package com.sectong.firstcv;

import javax.swing.JFrame;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.avcodec;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.presets.opencv_objdetect;
import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.FrameRecorder;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.javacv.OpenCVFrameGrabber;

/**
 * 开启摄像头捕捉，并转换成flv封装格式，推流
 * 
 * @author jiekechoo
 *
 */
public class RecordCamera {

	public static void recordCamera(String outputFile, double frameRate)
			throws Exception, InterruptedException, FrameRecorder.Exception {
		Loader.load(opencv_objdetect.class);
		FrameGrabber grabber = new OpenCVFrameGrabber(0);// 本机摄像头默认0，这里使用javacv的抓取器，至于使用的是ffmpeg还是opencv，请自行查看源码
		grabber.start();// 开启抓取器

		OpenCVFrameConverter.ToIplImage converter = new OpenCVFrameConverter.ToIplImage();// 转换器
		IplImage grabbedImage = converter.convert(grabber.grab());// 抓取一帧视频并将其转换为图像，至于用这个图像用来做什么？加水印，人脸识别等等自行添加
		int width = grabbedImage.width();
		int height = grabbedImage.height();

		FrameRecorder recorder = FrameRecorder.createDefault(outputFile, width, height);
		recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264); // avcodec.AV_CODEC_ID_H264，编码
		recorder.setFormat("flv");// 封装格式，如果是推送到rtmp就必须是flv封装格式
		recorder.setFrameRate(frameRate);

		recorder.start();// 开启录制器
		long startTime = 0;
		long videoTS = 0;
		CanvasFrame frame = new CanvasFrame("camera", CanvasFrame.getDefaultGamma() / grabber.getGamma());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setAlwaysOnTop(true);
		Frame rotatedFrame = converter.convert(grabbedImage);// 不知道为什么这里不做转换就不能推到rtmp
		while (frame.isVisible() && (grabbedImage = converter.convert(grabber.grab())) != null) {
			rotatedFrame = converter.convert(grabbedImage);
			frame.showImage(rotatedFrame);
			if (startTime == 0) {
				startTime = System.currentTimeMillis();
			}
			videoTS = 1000 * (System.currentTimeMillis() - startTime);
			recorder.setTimestamp(videoTS);
			recorder.record(rotatedFrame);
			Thread.sleep(40);
		}
		frame.dispose();
		recorder.stop();
		recorder.release();
		grabber.stop();

	}

	// 文件
	// public static void main(String[] args)
	// throws Exception, InterruptedException,
	// org.bytedeco.javacv.FrameRecorder.Exception {
	// recordCamera("video.mp4", 25);
	// }

//	// hls流
	public static void main(String[] args)
			throws Exception, InterruptedException, org.bytedeco.javacv.FrameRecorder.Exception {
		recordCamera("rtmp://list.sectong.com:1935/hls/osx", 25);
	}

//	rtmp 流

//	public static void main(String[] args)
//			throws Exception, InterruptedException, org.bytedeco.javacv.FrameRecorder.Exception {
//		recordCamera("rtmp://list.sectong.com:1935/myapp/osx", 25);
//	}
}
