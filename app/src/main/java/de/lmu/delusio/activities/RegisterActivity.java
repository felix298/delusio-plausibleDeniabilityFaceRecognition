package de.lmu.delusio.activities;

import static org.bytedeco.javacpp.opencv_imgproc.CV_BGR2GRAY;
import static org.bytedeco.javacpp.opencv_imgproc.cvtColor;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;


import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_objdetect;

import de.lmu.delusio.R;
import de.lmu.delusio.databinding.ActivityRegisterBinding;
import de.lmu.delusio.helper.CvCameraPreview;
import de.lmu.delusio.helper.TrainHelper;
import de.lmu.delusio.interfaces.ICamera;

public class RegisterActivity extends Activity implements CvCameraPreview.CvCameraViewListener {

    protected ActivityRegisterBinding binding;

    private opencv_objdetect.CascadeClassifier faceDetector;
    private int absoluteFaceSize = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        prepareFaceDetector();

        binding.cancel.setOnClickListener(v-> {
            onBackPressed();
        });

        binding.registerFace.setOnClickListener(v-> {
            startActivity(new Intent(this, LoginActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            finish();
        });
    }

    private void prepareFaceDetector() {

        if(TrainHelper.isTrained(getBaseContext())) {
            try {
                TrainHelper.reset(getBaseContext());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        binding.camera.setCvCameraViewListener(this);
        faceDetector = TrainHelper.loadClassifierCascade(RegisterActivity.this, R.raw.frontalface);

    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        absoluteFaceSize = (int) (width * 0.32f);
    }

    @Override
    public void onCameraViewStopped() {

    }


    @Override
    public opencv_core.Mat onCameraFrame(opencv_core.Mat rgbaMat) {

        if (faceDetector != null) {

            opencv_core.Mat greyMat = new opencv_core.Mat(rgbaMat.rows(), rgbaMat.cols());
            cvtColor(rgbaMat, greyMat, CV_BGR2GRAY);
            opencv_core.RectVector faces = new opencv_core.RectVector();
            faceDetector.detectMultiScale(greyMat, faces, 1.25f, 3, 1,
                    new opencv_core.Size(absoluteFaceSize, absoluteFaceSize),
                    new opencv_core.Size(4 * absoluteFaceSize, 4 * absoluteFaceSize));

            //Validate if Image has face
            if (faces.size() == 1) {
                //To train the model we need atleast 25 images
                if (isImageRequired()) train();
                else capturePhoto(rgbaMat);
            } else {
                updateStatus("Please align the face in center!");
            }
            greyMat.release();
        }

        return rgbaMat;
    }

    private void updateStatus(String text) {
        runOnUiThread(() -> {
            binding.faceStatus.setText(text);
        });
    }

    private boolean isImageRequired() {
        int remainingPhotos = TrainHelper.PHOTOS_TRAIN_QTY - TrainHelper.qtdPhotos(getBaseContext());
        return remainingPhotos == 0;
    }


    private void capturePhoto(opencv_core.Mat rgbaMat) {

        try {
            TrainHelper.takePhoto(getBaseContext(), 1, TrainHelper.qtdPhotos(getBaseContext()) + 1, rgbaMat.clone(), faceDetector);
            updateStatus(getProgress()+"%");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int getProgress() {
        return (TrainHelper.qtdPhotos(getBaseContext()) * 100)/(TrainHelper.PHOTOS_TRAIN_QTY+1);
    }


    private void train() {

        if(!TrainHelper.isTrained(getBaseContext())) {

            try {
                if (TrainHelper.train(getBaseContext())) {
                    runOnUiThread(() -> {
                        binding.faceStatus.setText("Done!");
                        binding.registerFace.setVisibility(View.VISIBLE);
                        binding.camera.setVisibility(View.GONE);
                        binding.doneLayout.setVisibility(View.VISIBLE);
                    });

                } else {
                    runOnUiThread(() -> {
                        binding.faceStatus.setText("Error! Face ID not set!, Try Again later!");
                        binding.camera.setVisibility(View.GONE);
                        binding.registerFace.setVisibility(View.GONE);
                    });
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void onBackPressed() {
        try {
            TrainHelper.reset(getBaseContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onBackPressed();
    }
}