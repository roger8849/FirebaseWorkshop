package javeriana.edu.co.firebaseworkshop;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

  private GoogleMap mMap;

  private EditText mAddress;

  private Geocoder mGeocoder;

  private FusedLocationProviderClient mFusedLocationClient;

  private static LatLng currentLocation;

  public static final double lowerLeftLatitude = 1.396967;
  public static final double lowerLeftLongitude = -78.903968;
  public static final double upperRightLatitude = 11.983639;
  public static final double upperRigthLongitude = -71.869905;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    getCurrentLocation();
    setContentView(R.layout.activity_maps);
    // Obtain the SupportMapFragment and get notified when the map is ready to be used.
    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
        .findFragmentById(R.id.map);
    mapFragment.getMapAsync(this);

    mAddress = findViewById(R.id.adrressEditText);
    mGeocoder = new Geocoder(this);

    setAddressListener();


  }

  @Override
  protected void onResume() {
    super.onResume();
    getCurrentLocation();
  }

  private void getCurrentLocation() {
    if (ContextCompat
        .checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_FINE_LOCATION)
        == PackageManager.PERMISSION_GRANTED) {
      mFusedLocationClient.getLastLocation().addOnSuccessListener(this,
          new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
              if (location != null) {
                currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                if (mMap != null) {
                  mMap.addMarker(
                      new MarkerOptions().position(currentLocation).title("Current location"));
                  mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 14));
                }
              }
            }
          });
    }
  }

  private void setAddressListener() {
    mAddress.setOnEditorActionListener(new TextView.OnEditorActionListener() {
      @Override
      public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
          String addressString = mAddress.getText().toString();
          if (!addressString.isEmpty()) {
            try {
              List<Address> addresses = mGeocoder
                  .getFromLocationName(addressString, 2, lowerLeftLatitude, lowerLeftLongitude,
                      upperRightLatitude, upperRigthLongitude);
              if (addresses != null && !addresses.isEmpty()) {
                Address addressResult = addresses.get(0);
                LatLng position = new LatLng(addressResult.getLatitude(),
                    addressResult.getLongitude());
                if (mMap != null) {
//                  MarkerOptions myMarkerOptions = new MarkerOptions();
//                  myMarkerOptions.position(position);
//                  myMarkerOptions.title("Address found.");
//                  myMarkerOptions.icon(
//                      BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
//                  mMap.addMarker(myMarkerOptions);
//                  mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 14));
                  findRouteBetweenPoints(position);
                }

              } else {
                Toast.makeText(MapsActivity.this, "Address not found", Toast.LENGTH_SHORT)
                    .show();
              }
            } catch (IOException e) {
              e.printStackTrace();
            }
          } else {
            Toast.makeText(MapsActivity.this, "Empty address field.", Toast.LENGTH_SHORT).show();
          }
        }

        return false;
      }
    });
  }

  /**
   * Manipulates the map once available. This callback is triggered when the map is ready to be
   * used. This is where we can add markers or lines, add listeners or move the camera. In this
   * case, we just add a marker near Sydney, Australia. If Google Play services is not installed on
   * the device, the user will be prompted to install it inside the SupportMapFragment. This method
   * will only be triggered once the user has installed Google Play services and returned to the
   * app.
   */
  @Override
  public void onMapReady(GoogleMap googleMap) {
    mMap = googleMap;

    // Add a marker in Sydney and move the camera
    LatLng sydney = new LatLng(-34, 151);

    mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
    mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

    Date now = new Date(System.currentTimeMillis());
    Calendar c = Calendar.getInstance();
    int timeOfDay = c.get(Calendar.HOUR_OF_DAY);

    if (timeOfDay >= 6 && timeOfDay < 18) {
      mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.default_map_style));
    } else {
      mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.night_map_style));
    }
  }

  public void findRouteBetweenPoints(LatLng destination){
    mMap.addMarker(new MarkerOptions().position(currentLocation).title("Current location"));

    mMap.addMarker(new MarkerOptions().position(destination).title("Destination"));

    //Define list to get all latlng for the route
    List<LatLng> path = new ArrayList();

    //Execute Directions API request
    GeoApiContext context = new GeoApiContext.Builder()
        .apiKey("YOUR_API_KEY")
        .build();
    String currentLocationString = new StringBuilder(String.valueOf(currentLocation.latitude)).append(",").append(String.valueOf(currentLocation.longitude)).toString();
    String destinationLocationString = new StringBuilder(String.valueOf(destination.latitude)).append(",").append(String.valueOf(destination.longitude)).toString();

    DirectionsApiRequest req = DirectionsApi
        .getDirections(context, currentLocationString, destinationLocationString);
    try {
      DirectionsResult res = req.await();

      //Loop through legs and steps to get encoded polylines of each step
      if (res.routes != null && res.routes.length > 0) {
        DirectionsRoute route = res.routes[0];

        if (route.legs != null) {
          for (int i = 0; i < route.legs.length; i++) {
            DirectionsLeg leg = route.legs[i];
            if (leg.steps != null) {
              for (int j = 0; j < leg.steps.length; j++) {
                DirectionsStep step = leg.steps[j];
                if (step.steps != null && step.steps.length > 0) {
                  for (int k = 0; k < step.steps.length; k++) {
                    DirectionsStep step1 = step.steps[k];
                    EncodedPolyline points1 = step1.polyline;
                    if (points1 != null) {
                      //Decode polyline and add points to list of route coordinates
                      List<com.google.maps.model.LatLng> coords1 = points1.decodePath();
                      for (com.google.maps.model.LatLng coord1 : coords1) {
                        path.add(new LatLng(coord1.lat, coord1.lng));
                      }
                    }
                  }
                } else {
                  EncodedPolyline points = step.polyline;
                  if (points != null) {
                    //Decode polyline and add points to list of route coordinates
                    List<com.google.maps.model.LatLng> coords = points.decodePath();
                    for (com.google.maps.model.LatLng coord : coords) {
                      path.add(new LatLng(coord.lat, coord.lng));
                    }
                  }
                }
              }
            }
          }
        }
      }
    } catch (Exception ex) {
      Log.e("LOCALIZATION TEST", ex.getLocalizedMessage());
    }

    //Draw the polyline
    if (path.size() > 0) {
      PolylineOptions opts = new PolylineOptions().addAll(path).color(Color.BLUE).width(5);
      mMap.addPolyline(opts);
    }

    mMap.getUiSettings().setZoomControlsEnabled(true);

    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 11));
  }
}
