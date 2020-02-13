package com.example.nicolaswong.androidassignment;

import android.Manifest;
import android.content.*;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.location.*;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.*;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.loopj.android.http.*;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    String[] showSearch;
    String[] searchL;
    private Button bookmark;
    private Button search;
    LocationManager locationManager;
    LocationListener locationListener;
    private GoogleMap mMap;
    double longitude;
    double latitude;
    String apiKey = "25906a2aa1477e44a11abe1481f3698e";
    String apiUrl = "http://api.openweathermap.org/data/2.5/";
    String weatherUrl = apiUrl + "weather?q=London" + "&appid=" + apiKey + "&units=metric";
    String locatUrl = apiUrl + "weather?lat=" + latitude + "lon=" + longitude + "&appid=" + apiKey + "&units=metric";
    ListView list;
    private DB database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        bookmark = (Button) findViewById(R.id.bookmarkBtn);
        bookmark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBookmark();
            }
        });

        search = (Button) findViewById(R.id.searchBtn);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchCity();
            }
        });

        list  = (ListView) findViewById(R.id.list);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                saveToDB();
            }
        });

    }

    public void saveToDB(){
        database = new DB(this);
        SQLiteDatabase writeDB = database.getWritableDatabase();
        ContentValues cv=new ContentValues();
        cv.put("name", searchL[0]);
        writeDB.insert("weather", null, cv);

        Log.v("nameeee",searchL[0]);
        database.close();

        Toast.makeText(this,"Bookmarked",Toast.LENGTH_SHORT).show();
    }

    public void findedCity(){
        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, showSearch );
        list.setAdapter(adapter);
    }

    public void searchCity(){
        AsyncHttpClient client = new AsyncHttpClient();
        EditText text = (EditText) findViewById(R.id.editText);
        final String city = text.getText().toString();
        String Url = apiUrl + "weather?q="+ city + "&appid=" + apiKey + "&units=metric";
        client.get(Url, null ,new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    String name = response.getString("name");

                    searchL = new String[]{name};
                    showSearch = new String[]{name};
                    findedCity();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void openBookmark(){
        Intent intent = new Intent(this, BookmarkActivity.class);
        startActivity(intent);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Add a marker in Sydney and move the camera

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                longitude = location.getLongitude();
                latitude = location.getLatitude();
                Log.d("location", latitude + " , " + longitude);

                LatLng place = new LatLng(latitude, longitude);
                mMap.addMarker(new MarkerOptions().position(place).title("Your location"));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(place));
                getWeather((int)latitude, (int)longitude);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.INTERNET
            }, 10 );
            return;
        }else{
            locationManager.requestLocationUpdates("gps", 5000, 10, locationListener);
        }
    }

    public void getWeather(int lat, int lon){
        AsyncHttpClient client = new AsyncHttpClient();
        String Url = apiUrl + "weather?lat=" + lat + "&lon=" + lon + "&appid=" + apiKey + "&units=metric";
        client.get(Url, null ,new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    String name = response.getString("name");
                    String weather = response.getJSONArray("weather").getString(0);
                    JSONObject Jweather = new JSONObject(weather);
                    String mintemp = response.getJSONObject("main").getString("temp_min");
                    String maxtemp = response.getJSONObject("main").getString("temp_max");
                    String country = response.getJSONObject("sys").getString("country");
                    String hum = response.getJSONObject("main").getString("humidity");
                    String pres = response.getJSONObject("main").getString("pressure");

                    TextView Cname = findViewById(R.id.textView3);
                    Cname.setText("Place: "+name);
                    TextView desc = findViewById(R.id.textView4);
                    desc.setText("Weather: "+Jweather.getString("description"));
                    TextView Tcountry = findViewById(R.id.textView5);
                    Tcountry.setText("Country: "+country);
                    TextView Ttemp = findViewById(R.id.textView6);
                    Ttemp.setText("Temp: "+mintemp+"-"+maxtemp+"°C");
                    TextView humidity = findViewById(R.id.textView7);
                    humidity.setText("humidity: "+hum+"%");
                    TextView pressure = findViewById(R.id.textView8);
                    pressure.setText("Pressure: "+pres);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
