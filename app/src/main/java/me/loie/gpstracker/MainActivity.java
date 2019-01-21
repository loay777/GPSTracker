package me.loie.gpstracker;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.content.Intent;
import android.location.LocationManager;
import android.Manifest;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.*;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;
import java.util.Locale;


public class MainActivity extends Activity implements LocationListener {

    private static final int PERMISSIONS_REQUEST = 100;
    private TextView coordinatesTxt, addressTxt,speedTxt;
    private Button updateLocationBttn;
    private LocationManager locationManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseApp.initializeApp(this);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        coordinatesTxt = (TextView) findViewById(R.id.coordinatesTxt);
        addressTxt = (TextView) findViewById(R.id.addresTxt);
        speedTxt = (TextView) findViewById(R.id.speedTxt);
        updateLocationBttn = (Button)findViewById(R.id.updateLocationBttn);
        updateLocationBttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Build.VERSION.SDK_INT >- Build.VERSION_CODES.M){
                    if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                        //getLocation();
                        requestLocationUpdates();

                    }else {
                      //API 23 and above
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if(shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)){

                             }
                        }
                        // requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION},PERMISSIONS_REQUEST_FINE_LOCATION_RESULT);

                    }
                }else{
                    requestLocationUpdates();
                }

            }
        });



//Check whether GPS tracking is enabled//

        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

        }

//Check whether this app has access to the location permission//

        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);

//If the location permission has been granted, then start the TrackerService//

        if (permission == PackageManager.PERMISSION_GRANTED) {
            startTrackerService();
        } else {

//If the app doesn’t currently have access to the user’s location, then request access//

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST);
        }


    }

//Handles wha happens after the user is prompted to enable location
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[]
            grantResults) {

//If the permission has been granted

        if (requestCode == PERMISSIONS_REQUEST && grantResults.length == 1
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

//then start the GPS tracking service

            startTrackerService();


        }

        else {

//If the user denies the permission request, then display a toast with some more information

            Toast.makeText(this, "Please enable location services to allow GPS tracking", Toast.LENGTH_SHORT).show();
        }

    }



//LocationListener Methods implementation
    @Override
    public void onLocationChanged(Location location) {
       // coordinatesTxt.setText("Latitude = "+location.getLatitude()+"\nLongitude =  "+location.getLongitude());
       // addressTxt.setText(getCompleteAddressString(location.getLatitude(),location.getLongitude()));

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(MainActivity.this,"Please Enable your GPS and INTERNET",Toast.LENGTH_SHORT).show();
    }



//Start the Firebase TrackerService
    private void startTrackerService() {
        startService(new Intent(this, TrackingService.class));

//Notify the user that tracking has been enabled//

        Toast.makeText(this, "GPS tracking enabled", Toast.LENGTH_SHORT).show();

    }

// getting the full address using the latitude and longitude
    private String getCompleteAddressString(double LATITUDE, double LONGITUDE) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");

                for (int i = 0; i <= returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                strAdd = strReturnedAddress.toString();
                Log.w("Current address", strReturnedAddress.toString());
            } else {
                Log.w("My Current address", "No Address returned!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.w("My Current  address", "Canont get Address!");
        }
        return strAdd;
    }

// getting location to be displayed on the MainActivity
    void getLocation(){
        try{
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 500,5,this);

        }catch(SecurityException e){
            e.printStackTrace();
        }
    }


    private void requestLocationUpdates() {
        LocationRequest request = new LocationRequest();

//Specify how often your app should request the device’s location//

        request.setInterval(3000); //1000 MilliSecond = 1 sec

//Get the most accurate location data available//

        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(this);
        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);

//If the app currently has access to the location permission...//

        if (permission == PackageManager.PERMISSION_GRANTED) {

//...then request location updates//

            client.requestLocationUpdates(request, new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {

//
                    Location location = locationResult.getLastLocation();
                    coordinatesTxt.setText("Latitude = "+location.getLatitude()+"\nLongitude =  "+location.getLongitude());
                    addressTxt.setText(getCompleteAddressString(location.getLatitude(),location.getLongitude()));

                    speedTxt.setText(Float.toString(location.getSpeed())+ " Meters/Second \n"+Float.toString(location.getSpeed()*60)+ " Meters/Minute \n"+Float.toString((location.getSpeed()*60*60)/1000)+ " KM/Hour");
                }
            }, null);
        }
    }
}