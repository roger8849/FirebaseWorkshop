package javeriana.edu.co.firebaseworkshop;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.DirectionsStep;
import com.google.maps.model.EncodedPolyline;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import javeriana.edu.co.firebaseworkshop.util.DirectionsJSONParser;
import org.json.JSONObject;

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
                  mMap.clear();
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
    //List<LatLng> path = new ArrayList();

    LatLng origin = currentLocation;
    LatLng dest = destination;

    // Getting URL to the Google Directions API
    String url = getDirectionsUrl(origin, dest);

    DownloadTask downloadTask = new DownloadTask();

    // Start downloading json data from Google Directions API
    downloadTask.execute(url);


    mMap.getUiSettings().setZoomControlsEnabled(true);

    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 11));

    double distance = LocalizationActivity
        .distance(currentLocation.latitude, currentLocation.longitude, destination.latitude,
            destination.longitude);

    Toast.makeText(MapsActivity.this, "Distance between the points: " + distance + " KM ",
        Toast.LENGTH_LONG).show();

  }

  private class DownloadTask extends AsyncTask<String, Void, String> {

    @Override
    protected String doInBackground(String... url) {

      String data = "";

      try {
        data = downloadUrl(url[0]);
      } catch (Exception e) {
        Log.d("Background Task", e.toString());
      }
      return data;
    }

    @Override
    protected void onPostExecute(String result) {
      super.onPostExecute(result);
      ParserTask parserTask = new ParserTask();
      parserTask.execute(result);

    }
  }


  /**
   * A class to parse the Google Places in JSON format
   */
  private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

    @Override
    protected void onPreExecute() {
      super.onPreExecute();
    }

    // Parsing the data in non-ui thread
    @Override
    protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

      JSONObject jObject;
      List<List<HashMap<String, String>>> routes = null;

      try {
        jObject = new JSONObject(jsonData[0]);
        DirectionsJSONParser parser = new DirectionsJSONParser();

        routes = parser.parse(jObject);
      } catch (Exception e) {
        e.printStackTrace();
      }
      return routes;
    }

    @Override
    protected void onPostExecute(List<List<HashMap<String, String>>> result) {
      ArrayList points = null;
      PolylineOptions lineOptions = null;
      MarkerOptions markerOptions = new MarkerOptions();

      for (int i = 0; i < result.size(); i++) {
        points = new ArrayList();
        lineOptions = new PolylineOptions();

        List<HashMap<String, String>> path = result.get(i);

        for (int j = 0; j < path.size(); j++) {
          HashMap<String, String> point = path.get(j);

          double lat = Double.parseDouble(point.get("lat"));
          double lng = Double.parseDouble(point.get("lng"));
          LatLng position = new LatLng(lat, lng);

          points.add(position);
        }

        lineOptions.addAll(points);
        lineOptions.width(12);
        lineOptions.color(Color.BLUE);
        lineOptions.geodesic(true);

      }

// Drawing polyline in the Google Map for the i-th route
      mMap.addPolyline(lineOptions);
    }
  }

  private String getDirectionsUrl(LatLng origin, LatLng dest) {

    // Origin of route
    String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

    // Destination of route
    String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

    // Sensor enabled
    String sensor = "sensor=false";
    String mode = "mode=driving";
    // Building the parameters to the web service
    String parameters = new StringBuilder(str_origin).append("&").append(str_dest).append("&").append(sensor).append("&").append(mode).toString();

    // Output format
    String output = "json";

    String googleKey = getResources().getString(R.string.google_api_key);

    // Building the url to the web service
    StringBuilder url = new StringBuilder("https://maps.googleapis.com/maps/api/directions/").append(output).append("?").append(parameters).append("&key=").append(googleKey);

    return url.toString();
  }

  /**
   * A method to download json data from url
   */
  private String downloadUrl(String strUrl) throws IOException {
    String data = "";
    InputStream iStream = null;
    HttpURLConnection urlConnection = null;
    try {
      URL url = new URL(strUrl);

      urlConnection = (HttpURLConnection) url.openConnection();

      urlConnection.connect();

      iStream = urlConnection.getInputStream();

      BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

      StringBuffer sb = new StringBuffer();

      String line = "";
      while ((line = br.readLine()) != null) {
        sb.append(line);
      }

      data = sb.toString();

      br.close();

    } catch (Exception e) {
      Log.e("Exception", e.toString());
    } finally {
      iStream.close();
      urlConnection.disconnect();
    }
    return data;
  }

}
