package me.sjva.sosoplayer.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import me.sjva.sosoplayer.R;
import me.sjva.sosoplayer.util.Util;

public class SplashActivity  extends AppCompatActivity {
  private static final String TAG = SplashActivity.class.getSimpleName();
  private final static int REQUEST_PERMISSION = 0;
  private ProgressBar progressBar;

  protected void onCreate(Bundle savedInstanceState) {

    super.onCreate(savedInstanceState);
    setContentView(R.layout.splash_activity);

    progressBar = findViewById(R.id.player_progress);
    getSupportActionBar().hide();
    getSupportActionBar().setTitle(R.string.application_name);
    getSupportActionBar().setDisplayHomeAsUpEnabled(false);

    boolean needsToRequestPermission = mayShowPermissionRequest(new String[] { Manifest.permission.READ_EXTERNAL_STORAGE }, REQUEST_PERMISSION );
    if (!needsToRequestPermission) {
      startMainActivity();
    }
  }

  private void startMainActivity() {
    Intent intent = new Intent(this, MainActivity.class);
    startActivity(intent);
    finish();
  }

  public boolean mayShowPermissionRequest(String[] permissions, int requestCode) {
    boolean needsToRequestPermission = false;
    for (String permission : permissions) {
      int permissionResult = ContextCompat.checkSelfPermission(this, permission);
      if (permissionResult != PackageManager.PERMISSION_GRANTED) {
        needsToRequestPermission = true;
        break;
      }
    }

    if (needsToRequestPermission) {
      ActivityCompat.requestPermissions(this, permissions, requestCode);
      return needsToRequestPermission;
    }
    return needsToRequestPermission;
  }


  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
      @NonNull int[] grantResults) {
      if (requestCode == REQUEST_PERMISSION) {
        boolean permissionApplied = true;
        for (int i = 0; i < grantResults.length; i++) {
          permissionApplied |= (grantResults[i] == PackageManager.PERMISSION_GRANTED);
        }

        if (permissionApplied) {
          startMainActivity();
        } else {
          Util.permissionDeniedDialog(SplashActivity.this);
          Log.d(TAG, "PermissionDenied");
        }
      }
  }


}
