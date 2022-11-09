package de.lmu.delusio.activities;

import android.content.Intent;
import android.os.Bundle;

import de.lmu.delusio.databinding.ActivityInitBinding;
import de.lmu.delusio.helper.TrainHelper;

import androidx.appcompat.app.AppCompatActivity;

public class SplashScreenActivity extends AppCompatActivity {

    private ActivityInitBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityInitBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        if (TrainHelper.isTrained(getBaseContext())) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
        else {
            startActivity(new Intent(this, LockActivity.class));
            finish();
        }
    }
}
