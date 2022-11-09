package de.lmu.delusio.interfaces;

import org.bytedeco.javacpp.opencv_core.Mat;


public interface ICamera {

    public void start(int width, int height);
    public void stop();
    public Mat process(Mat mat);

}
