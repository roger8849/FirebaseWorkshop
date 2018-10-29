package javeriana.edu.co.firebaseworkshop;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import javeriana.edu.co.firebaseworkshop.dto.MyLocation;

public class LocalizationActivity extends AppCompatActivity {

  private static final double RADIUS_OF_EARTH_KM = 6371;
  private static final double AIRPORT_LATITUDE = 4.6889695;
  private static final double AIRPORT_LONGITUDE = -74.1398821;

  protected static final int REQUEST_CHECK_SETTINGS = 0x1;
  private final static int LOCATION_PERMISSION = 0;


  private TextView latitude, longitude, altitude, distanceToAirport;

  private ListView locationList;

  //Location
  private FusedLocationProviderClient fusedLocationClient;
  private LocationRequest locationRequest;

  private LocationCallback locationCallback;

  private ArrayAdapter<String> arrayAdapter;

  List<String> locationListValues;

  @Override
  protected void onResume() {
    super.onResume();
    startLocationUpdates();
  }

  @Override
  protected void onPause() {
    super.onPause();
    stopLocationUpdates();
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_localization);

    latitude = findViewById(R.id.latitudeTextView);
    longitude = findViewById(R.id.longitudeTextView);
    altitude = findViewById(R.id.altitudeTextView);
    distanceToAirport = findViewById(R.id.airportDistanceView);

    locationListValues = new ArrayList<>();
    locationList = findViewById(R.id.locationListView);
    arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
        locationListValues);
    locationList.setAdapter(arrayAdapter);
    Intent mainActivity = getIntent();
    locationRequest = mainActivity.getParcelableExtra("locationRequest");
    turnOnLocation();

    fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

    initLocationCallback();

  }

  private void initLocationCallback() {
    locationCallback = new LocationCallback() {
      @Override
      public void onLocationResult(LocationResult locationResult) {
        Location location = locationResult.getLastLocation();
        Log.i("Location", "location callback update: " + location);
        if (location != null) {
          latitude.setText("Latitude: " + String.valueOf(location.getLatitude()));
          longitude.setText("Longitude: " + String.valueOf(location.getLongitude()));
          altitude.setText("Altitude: " + String.valueOf(location.getAltitude()));

          double distance = LocalizationActivity.distance(AIRPORT_LATITUDE, AIRPORT_LONGITUDE, location.getLatitude(),
              location.getLongitude());

          distanceToAirport.setText("Distance to airport: " + String.valueOf(distance));

        }
      }
    };
  }

  public void turnOnLocation() {
    LocationSettingsRequest.Builder builder = new
        LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
    SettingsClient client = LocationServices.getSettingsClient(this);
    Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

    task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
      @Override
      public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
        startLocationUpdates(); //Todas las condiciones para recibir localizaciones
      }
    });

    task.addOnFailureListener(this, new OnFailureListener() {
      @Override
      public void onFailure(@NonNull Exception e) {
        int statusCode = ((ApiException) e).getStatusCode();
        switch (statusCode) {
          case CommonStatusCodes.RESOLUTION_REQUIRED:
            // Location settings are not satisfied, but this can be fixed by showing the user a dialog.
            try {// Show the dialog by calling startResolutionForResult(), and check the result in onActivityResult().
              ResolvableApiException resolvable = (ResolvableApiException) e;
              resolvable.startResolutionForResult(LocalizationActivity.this,
                  REQUEST_CHECK_SETTINGS);
            } catch (IntentSender.SendIntentException sendEx) {
              // Ignore the error.
            }
            break;
          case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
            // Location settings are not satisfied. No way to fix the settings so we won't show the dialog.
            break;
        }
      }
    });
  }

  public void addLocationsToLocationsList(View view) {
    MyLocation myLocation = new MyLocation(latitude.getText().toString(),
        longitude.getText().toString());
    locationListValues.add(myLocation.toString());
    arrayAdapter.notifyDataSetChanged();
    writeJSONObject(myLocation);

  }

  private void writeJSONObject(MyLocation myLocation) {
    Writer output = null;
    String filename = "locations.json";
    try {
      File file = new File(getBaseContext().getFilesDir(), filename);
      Log.i("LOCATION", "File location: " + file);
      output = new BufferedWriter(new FileWriter(file));
      output.write(myLocation.toJSON().toString());
      output.close();
      Toast.makeText(getApplicationContext(), "Location saved",
          Toast.LENGTH_LONG).show();
    } catch (Exception e) {
      Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_LONG).show();
    }
  }

  private void startLocationUpdates() {
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        == PackageManager.PERMISSION_GRANTED) {
      fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }
  }

  private void stopLocationUpdates() {
    fusedLocationClient.removeLocationUpdates(locationCallback);
  }

  public static double distance(double lat1, double long1, double lat2, double long2) {
    double latDistance = Math.toRadians(lat1 - lat2);
    double lngDistance = Math.toRadians(long1 - long2);
    double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
        + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
        * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    double result = RADIUS_OF_EARTH_KM * c;
    return Math.round(result * 100.0) / 100.0;
  }
}
