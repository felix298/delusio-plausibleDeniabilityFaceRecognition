package de.lmu.delusio.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;



import java.util.List;

import de.lmu.delusio.R;
import de.lmu.delusio.databinding.ActivityLockBinding;
import pub.devrel.easypermissions.EasyPermissions;

public class LockActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    protected ActivityLockBinding binding;

    private static final int CAMERA_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLockBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        requestPermission();

        binding.addFace.setOnClickListener( v -> {

            if (EasyPermissions.hasPermissions(this,
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE))
            {
                startActivity(new Intent(LockActivity.this, RegisterActivity.class));
            }
            else requestPermission();

        });
    }

    private void requestPermission() {
        EasyPermissions.requestPermissions(
                this,
                getResources().getString(R.string.camera_permission),
                CAMERA_CODE,
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        );
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        Toast.makeText(this, "Permissions Granted!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        Toast.makeText(this, "All Permissions Require!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

}