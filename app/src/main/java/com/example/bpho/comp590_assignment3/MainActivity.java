package com.example.bpho.comp590_assignment3;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private GoogleApiClient c = null;
    protected Location lastLocation;
    public AddressResultReceiver resultReceiver = new AddressResultReceiver(null);
    private String addressOutput;
    private boolean addressRequested;
    private Handler mHandler;
    private double brooksLatitude = 35.909459;
    private double brooksLongitude = -79.053052;
    private double polkLatitude = 35.910643;
    private double polkLongitude = -79.050400;
    private double oldWellLatitude = 35.912054;
    private double oldWellLongitude = -79.051241;

    private MediaPlayer mp1;
    private MediaPlayer mp2;
    private MediaPlayer mp3;
//    private MediaPlayer mp;

    Location brooks;
    Location polk;
    Location oldWell;


    class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            // Display the address string
            // or an error message sent from the intent service.
            addressOutput = resultData.getString(Constants.RESULT_DATA_KEY);
            Log.v("UPDATE ", "onReceiveResult is called");
            Log.v("Add Output ", addressOutput);

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    displayAddressOutput();
                }
            });
        }
    }

    private void displayAddressOutput() {
        TextView address = (TextView) findViewById(R.id.address2);
        address.setText(addressOutput);
    }

    protected void startIntentService() {
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(Constants.RECEIVER, resultReceiver);
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, lastLocation);
        startService(intent);
    }

    public void retrieveAddress(View view) {
//        testDistance();
        if (c.isConnected() && lastLocation != null) {
            Log.v("LOG ", "Button pressed, intent started");
            startIntentService();
        }
        addressRequested = true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mHandler = new Handler();

        if (c == null) {
            c = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
        }

        brooks = new Location("Brooks Building");
        brooks.setLatitude(brooksLatitude);
        brooks.setLongitude(brooksLongitude);

        polk = new Location("Polk Place");
        polk.setLatitude(polkLatitude);
        polk.setLongitude(polkLongitude);

        oldWell = new Location("Old Well");
        oldWell.setLatitude(oldWellLatitude);
        oldWell.setLongitude(oldWellLongitude);

//        mp = new MediaPlayer();
//        mp1 = new MediaPlayer();
//        mp2 = new MediaPlayer();
//        mp3 = new MediaPlayer();

        mp1 = MediaPlayer.create(this.getApplicationContext(), R.raw.cake);
        mp2 = MediaPlayer.create(this.getApplicationContext(), R.raw.waves);
        mp3 = MediaPlayer.create(this.getApplicationContext(), R.raw.carolina);

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        lastLocation = LocationServices.FusedLocationApi.getLastLocation(c);

        if (lastLocation != null) {
            // Determine whether a Geocoder is available.
            if (!Geocoder.isPresent()) {
                Log.v("ERROR: ", "no geocoder available");
                return;
            }

            if (addressRequested) {
                Log.v("LOG ", "addressRequested Check, intent started");
                startIntentService();
            }

        }

        LocationRequest req = new LocationRequest();        // Receive location updates every second
        req.setInterval(5000);
        req.setFastestInterval(1000);
        req.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(c, req, this);
        } catch (SecurityException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    protected void onStart() {
        c.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        c.disconnect();
        super.onStop();
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        Log.v("LOC", "Lat = " +location.getLatitude() + ", Long = " +location.getLongitude());
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        TextView lat = (TextView) findViewById(R.id.latitude2);
        lat.setText("" +location.getLatitude());
        TextView lon = (TextView) findViewById(R.id.longitude2);
        lon.setText("" +location.getLongitude());

        testDistance(latitude, longitude);
    }

    public void testDistance(double lat, double lon){
        Location currentLocation = new Location("Current Coordinates");
        currentLocation.setLatitude(lat);
        currentLocation.setLongitude(lon);
        float distOldWell = currentLocation.distanceTo(oldWell);
        float distBrooks = currentLocation.distanceTo(brooks);
        float distPolk = currentLocation.distanceTo(polk);
        Log.v("From Old Well: ", String.valueOf(distOldWell));
        Log.v("From Brooks: ", String.valueOf(distBrooks));
        Log.v("From Polk: ", String.valueOf(distPolk));

        TextView places = (TextView) findViewById(R.id.places);
        places.setText("Brooks: " +String.valueOf(distBrooks) +" m");
        places.append(" Polk : " +String.valueOf(distPolk) + " m");
        places.append(" Old Well: " +String.valueOf(distOldWell) + " m");

        TextView song = (TextView) findViewById(R.id.song2);
        ImageView map = (ImageView)findViewById(R.id.map);
        if (distOldWell < 80) {
            song.setText("Carolina In My Mind - James Taylor");
            map.setImageResource(R.drawable.oldwell);
            if (mp3 == null) {
                mp3 = MediaPlayer.create(this.getApplicationContext(), R.raw.carolina);
            }
            if (!(mp3.isPlaying())) {
                stopPlayer();
                mp3 = MediaPlayer.create(this.getApplicationContext(), R.raw.carolina);
                mp3.setLooping(true);
                mp3.start();
            }

        } else if (distBrooks < 80) {
            song.setText("Cake by the Ocean - DNCE");
            map.setImageResource(R.drawable.brooks);
            if (mp1 == null) {
                mp1 = MediaPlayer.create(this.getApplicationContext(), R.raw.cake);
            }

            if (!(mp1.isPlaying())) {
                stopPlayer();
                mp1 = MediaPlayer.create(this.getApplicationContext(), R.raw.cake);
                mp1.setLooping(true);
                mp1.start();
            }

        } else if (distPolk < 80) {
            song.setText("Waves - Kanye West");
            map.setImageResource(R.drawable.polkplace);
            if (mp2 == null) {
                mp2 = MediaPlayer.create(this.getApplicationContext(), R.raw.waves);
            }
            if (!(mp2.isPlaying())) {
                stopPlayer();
                mp2 = MediaPlayer.create(this.getApplicationContext(), R.raw.waves);
                mp2.setLooping(true);
                mp2.start();
            }

        } else {
            song.setText(" ");
            map.setImageResource(R.drawable.map);
            stopPlayer();
        }

    }

    public void stopPlayer() {
        if (mp1 != null) {
            mp1.stop();
            mp1.release();
            mp1 = null;
        }

        if (mp2 != null) {
            mp2.stop();
            mp2.release();
            mp2 = null;
        }

        if (mp3 != null) {
            mp3.stop();
            mp3.release();
            mp3 = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopPlayer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mp1 = MediaPlayer.create(this.getApplicationContext(), R.raw.cake);
        mp2 = MediaPlayer.create(this.getApplicationContext(), R.raw.waves);
        mp3 = MediaPlayer.create(this.getApplicationContext(), R.raw.carolina);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopPlayer();
    }

}
