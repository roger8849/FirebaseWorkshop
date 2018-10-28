package javeriana.edu.co.firebaseworkshop.dto;

import java.text.SimpleDateFormat;
import java.util.Date;
import org.json.JSONException;
import org.json.JSONObject;

public class MyLocation {

  private String latitude;
  private String longitude;
  private String dateString;

  public MyLocation() {
    super();
  }

  public MyLocation(String latitude, String longitude) {
    this.latitude = latitude;
    this.longitude = longitude;
    this.setDateString();
  }

  public String getLongitude() {
    return longitude;
  }

  public void setLongitude(String longitude) {
    this.longitude = longitude;
  }

  public String getLatitude() {
    return latitude;
  }

  public void setLatitude(String latitude) {
    this.latitude = latitude;
  }

  private void setDateString() {

    Date date = new Date(System.currentTimeMillis());
    SimpleDateFormat format = new SimpleDateFormat("EEE LLL dd HH:mm:ss");
    //DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE LLL dd HH:mm:ss");
    this.dateString = format.format(date);
  }

  public String getDateString() {
    return this.dateString;
  }

  public JSONObject toJSON() {
    JSONObject obj = new JSONObject();
    try {
      obj.put("latitud", getLatitude());
      obj.put("longitud", getLongitude());
      obj.put("date", getDateString());
    } catch (JSONException e) {
      e.printStackTrace();
    }
    return obj;
  }

  @Override
  public String toString() {
    StringBuilder locationBuilder = new StringBuilder(System.getProperty("line.separator"));
    locationBuilder.append(" ").append(getLatitude());
    locationBuilder.append(" ").append(getLongitude()).append(" on ");
    locationBuilder.append(this.dateString);
    return locationBuilder.toString();
  }
}
