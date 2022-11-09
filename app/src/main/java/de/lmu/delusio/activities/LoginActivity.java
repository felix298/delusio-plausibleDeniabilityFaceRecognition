package de.lmu.delusio.activities;

import static de.lmu.delusio.helper.TrainHelper.ACCEPT_LEVEL;
import static org.bytedeco.javacpp.opencv_imgproc.CV_BGR2GRAY;
import static org.bytedeco.javacpp.opencv_imgproc.cvtColor;
import static org.bytedeco.javacpp.opencv_imgproc.resize;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.google.mlkit.vision.face.FaceLandmark;

import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_face;
import org.bytedeco.javacpp.opencv_objdetect;
import org.bytedeco.javacv.AndroidFrameConverter;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.OpenCVFrameConverter;

import java.io.File;
import java.util.Objects;

import de.lmu.delusio.R;
import de.lmu.delusio.helper.CvCameraPreview;
import de.lmu.delusio.helper.TrainHelper;
import de.lmu.delusio.interfaces.ICamera;
import de.lmu.delusio.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity implements CvCameraPreview.CvCameraViewListener {

    private ActivityLoginBinding binding;


    private opencv_objdetect.CascadeClassifier faceDetector;
    private int absoluteFaceSize = 0;
    private boolean startScan = false;
    opencv_face.FaceRecognizer faceRecognizer = opencv_face.EigenFaceRecognizer.create();
    private opencv_core.Mat bitMat;
    private boolean isDone = false;


    private FaceDetector eyeDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        prepareFaceDetector();

        binding.scanFace.setOnClickListener(v->{
            startScan = true;
            updateStatus("Scanning...!");
        });

    }

    private void prepareFaceDetector() {

        if (TrainHelper.isTrained(getBaseContext())) {
            binding.cameraView.setCvCameraViewListener(this);
            faceDetector = TrainHelper.loadClassifierCascade(LoginActivity.this, R.raw.frontalface);
            File folder = new File(getFilesDir(), TrainHelper.TRAIN_FOLDER);
            File f = new File(folder, TrainHelper.EIGEN_FACES_CLASSIFIER);
            faceRecognizer.read(f.getAbsolutePath());
        }

    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        absoluteFaceSize = (int) (width * 0.32f);
    }

    @Override
    public void onCameraViewStopped() {

    }

    //Get the Image from Camera
    @Override
    public opencv_core.Mat onCameraFrame(opencv_core.Mat rgbaMat) {

        if (faceDetector != null) {

            opencv_core.Mat greyMat = new opencv_core.Mat(rgbaMat.rows(), rgbaMat.cols());
            bitMat = greyMat;
            cvtColor(rgbaMat, greyMat, CV_BGR2GRAY);
            opencv_core.RectVector faces = new opencv_core.RectVector();
            faceDetector.detectMultiScale(greyMat, faces, 1.25f, 3, 1,
                    new opencv_core.Size(absoluteFaceSize, absoluteFaceSize),
                    new opencv_core.Size(4 * absoluteFaceSize, 4 * absoluteFaceSize));

            if (faces.size() == 1) {
                if(startScan) recognize(faces.get(0), greyMat, rgbaMat);
            } else {
                updateStatus("Please align the face in center!");
            }
            greyMat.release();
        }

        return rgbaMat;
    }


    private void recognize(opencv_core.Rect detFace, opencv_core.Mat grayMat, opencv_core.Mat rgbaMat) {
        opencv_core.Mat detectedFace = new opencv_core.Mat(grayMat, detFace);
        resize(detectedFace, detectedFace, new opencv_core.Size(TrainHelper.IMG_SIZE,TrainHelper.IMG_SIZE));

        IntPointer label = new IntPointer(1);
        DoublePointer reliability = new DoublePointer(1);
        faceRecognizer.predict(detectedFace, label, reliability);
        int prediction = label.get(0);
        double acceptanceLevel = reliability.get(0);
        if (prediction == -1 || acceptanceLevel >= ACCEPT_LEVEL) {
            updateStatus("Not matched! Scanning!");
        } else {
            checkFaceEyes(getBitmapFromMat(bitMat));
            updateStatus("Matched!");
        }
    }

    private void updateStatus(String text) {
        runOnUiThread(() -> {
            binding.faceStatus.setText(text);
        });
    }

    private Bitmap getBitmapFromMat(opencv_core.Mat bitMat) {
        OpenCVFrameConverter.ToMat converterToMat = new OpenCVFrameConverter.ToMat();
        AndroidFrameConverter converterToBitmap = new AndroidFrameConverter();

        Frame frame = converterToMat.convert(bitMat);
        return converterToBitmap.convert(frame);

    }

    private void checkFaceEyes(Bitmap bitmap) {

        FaceDetectorOptions highAccuracyOpts =
                new FaceDetectorOptions.Builder()
                        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                        .build();
        eyeDetector = FaceDetection.getClient(highAccuracyOpts);
        InputImage inputPhoto = InputImage.fromBitmap(bitmap, 0);

        eyeDetector.process(inputPhoto).addOnSuccessListener(
                faces -> {

                    Face face = faces.get(0);

                    Float leftEye, rightEye;

                    if (face.getLeftEyeOpenProbability() != null) {
                        leftEye = face.getLeftEyeOpenProbability();
                    }
                    else leftEye = 0.0f;

                    if (face.getRightEyeOpenProbability() != null) {
                        rightEye = face.getRightEyeOpenProbability();
                    }
                    else rightEye = 0.0f;

                    if(leftEye >= 0.7 && rightEye >= 0.7) {

                        if(!isDone) {
                            binding.cameraView.setVisibility(View.GONE);
                            startActivity(new Intent(this, HomeActivity.class)
                                    .putExtra("isEyeClosed", false)
                                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                            finish();
                            isDone = true;
                        }

                    }
                    else {
                        if (!isDone) {

                            startActivity(new Intent(this, HomeActivity.class)
                                    .putExtra("isEyeClosed", true)
                                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                            finish();

                            isDone = true;
                        }
                    }

                });

    }

}