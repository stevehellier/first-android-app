package com.example.steve.myapplication;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.OnNmeaMessageListener;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;
import java.util.Locale;


public class MainActivity extends AppCompatActivity implements LocationListener, OnNmeaMessageListener {

    private LocationManager mLocationManager;
    private Double mLat, mLong, mAlt, mSpeedMph, mSpeedKph;
    private float mBearing;
    private Date mDateUtc;
    private Boolean bAltImp = true, bSpeedImp = true;
    private Double kph = 3.6, mph = 2.23694; //convert meters per second to mph and kph
    private ProgressDialog dialog;
    private TextView txtLat, txtHdg, txtSpeed, txtAlt, txtDate;
    private Locale locale = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        locale= Locale.getDefault();



//        SettingsFragment.instantiate(this, "");


        // display loading screen while waiting for the GPS to come online!!
        dialog = new ProgressDialog(this);
        dialog.setMessage("Waiting for GPS signal...");
        dialog.setCancelable(false);
        dialog.setInverseBackgroundForced(false);
        dialog.show();

        txtLat = findViewById(R.id.txt_mLat);
        txtHdg = findViewById(R.id.txt_mHdg);
        txtSpeed = findViewById(R.id.txt_mSpeed);
        txtAlt = findViewById(R.id.txt_mAlt);
        txtDate = findViewById(R.id.txt_mTime);


//        txtAlt.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                bAltImp = !bAltImp;
//            }
//        });
//
//        txtSpeed.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                bSpeedImp = !bSpeedImp;
//            }
//        });

        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        } else {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);
        }

        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);

        mLocationManager.addNmeaListener(this);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        mLat = location.getLatitude();
        mLong = location.getLongitude();
        mAlt = location.getAltitude();
        double speed = location.getSpeed();
        double fSpeedMph = (speed * mph);
        double fSpeedKph = (speed * kph);
        mSpeedMph = fSpeedMph;
        mSpeedKph = fSpeedKph;
        mDateUtc = new Date(location.getTime());
        mBearing = (location.getBearing());

        String strLat = Location.convert(mLat, Location.FORMAT_SECONDS);
        String strLon = Location.convert(mLong, Location.FORMAT_SECONDS);

        strLat = strLat.replaceFirst(":", "°");
        strLat = strLat.replaceFirst(":", "'");
        strLon = strLon.replaceFirst(":", "°");
        strLon = strLon.replaceFirst(":", "'");

        String latDir;
        if (mLat > 0) {
            latDir = " N";
        } else {
            latDir = " S";
        }
        String lonDir;
        if (mLong > 0) {
            lonDir = " W";
        } else {
            lonDir = " E";
        }

//        Log.d("Lat", strLat + latDir);
//        Log.d("Lon", strLon + lonDir);


        // set GPS Date and Time
        txtDate.setText(mDateUtc.toString());

        // Set position text
        String pos = String.format(locale,"Lat %.6f\nLon %.6f", mLat, mLong);
        txtLat.setText(pos);

        // Set Heading text

        String where = "NaNaS";

        if (mBearing >= 350 || mBearing <= 10)
            where = "N";
        if (mBearing < 350 && mBearing > 280)
            where = "NW";
        if (mBearing <= 280 && mBearing > 260)
            where = "W";
        if (mBearing <= 260 && mBearing > 190)
            where = "SW";
        if (mBearing <= 190 && mBearing > 170)
            where = "S";
        if (mBearing <= 170 && mBearing > 100)
            where = "SE";
        if (mBearing <= 100 && mBearing > 80)
            where = "E";
        if (mBearing <= 80 && mBearing > 10)
            where = "NE";

        txtHdg.setText(String.format(locale,"%.5s ° %s", mBearing, where));

        // Set Speed text and convert to MPH or KPH
        if (bSpeedImp) {
            txtSpeed.setText(String.format(locale,"%.1f mph", speed * mph));
        } else {
            txtSpeed.setText(String.format(locale,"%.1f kph", speed * kph));
        }

        // Set Altitude text and convert to Meters or Feet
        if (bAltImp) {
            txtAlt.setText(String.format(locale,"%.1f m", mAlt));
        } else {
            txtAlt.setText(String.format(locale,"%.1f ft", mAlt * 3.28084));
        }


        // Switch off the "loading" screen as we now have a GPS signal
        if (dialog.isShowing()) {
            dialog.hide();
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {
        Toast.makeText(getBaseContext(), "GPS ON", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onNmeaMessage(String message, long timestamp) {

    }
}

