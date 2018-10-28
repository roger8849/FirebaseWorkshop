package javeriana.edu.co.firebaseworkshop;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import com.google.android.gms.location.LocationRequest;

public class HomeUserActivity extends AppCompatActivity {

  private LocationRequest locationRequest;
  private final static int LOCATION_PERMISSION = 0;

  protected static final int REQUEST_CHECK_SETTINGS = 0x1;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    locationRequest = createLocationRequest();
    requestPermission(this, Manifest.permission.ACCESS_FINE_LOCATION, "Location access needed.",
        LOCATION_PERMISSION);
  }

  public void launchLocalization(View view) {
    Intent localizationActivity = new Intent(this, LocalizationActivity.class);
    localizationActivity.putExtra("locationRequest", locationRequest);
    startActivity(localizationActivity);
  }

  public void launchMaps(View view) {
    Intent permissions = new Intent(this, MapsActivity.class);
    startActivity(permissions);
  }

  private LocationRequest createLocationRequest() {
    LocationRequest locationRequest = new LocationRequest();
    locationRequest.setInterval(10000);
    locationRequest.setFastestInterval(5000);
    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    return locationRequest;
  }

  private void requestPermission(Activity context, String permission, String explanation,
      int requestId) {
    if (ContextCompat.checkSelfPermission(context, permission)
        != PackageManager.PERMISSION_GRANTED) {
      // Should we show an explanation?
      if (ActivityCompat.shouldShowRequestPermissionRationale(context, permission)) {
        Toast.makeText(context, explanation, Toast.LENGTH_LONG).show();
      }
      ActivityCompat.requestPermissions(context, new String[]{permission}, requestId);
    } else {
      if (Manifest.permission.ACCESS_FINE_LOCATION.equalsIgnoreCase(permission)) {
        Toast.makeText(context, "Permission " + permission + " already granted.", Toast.LENGTH_LONG)
            .show();
      }
    }
  }



  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
      @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    switch (requestCode) {
      case LOCATION_PERMISSION: {
        Toast.makeText(this, "LOCATION PERMISSION granted.", Toast.LENGTH_LONG)
            .show();
        break;
      }
    }
  }



}
