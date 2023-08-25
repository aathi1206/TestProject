package com.testproject;

import android.location.Location;
import android.Manifest;
import android.content.pm.PackageManager;
import android.provider.Settings;
import androidx.core.app.ActivityCompat;
import android.widget.Toast;
import android.content.DialogInterface;
import android.content.Intent;
import android.app.AlertDialog;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import android.net.Uri;

public class LocationModule extends ReactContextBaseJavaModule {

    private FusedLocationProviderClient fusedLocationClient;
    private ReactApplicationContext reactContext;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 120; // Choose any unique integer value


    public LocationModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(reactContext);
    }

    private void resetLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    @Override
    public String getName() {
        return "LocationModule";
    }

    @ReactMethod
    public void startLocationUpdates() {
        checkLocationPermission();
    }

  
  @ReactMethod
    public void fetchLocation(){
        resetLocationUpdates(); // Remove any ongoing location requests
        // Permission granted and location enabled, start location updates
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        showToast("Check permission");

    }

    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(reactContext, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                getCurrentActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION)){
                showPermissionAlert("Location permission is required to use this feature. Please grant the permission in the app settings.");
                } else{
                     ActivityCompat.requestPermissions(
                    getCurrentActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE
            );
                }
            return;
        }

        if (!isLocationEnabled()) {
            showPermissionAlert("Location permission is required to use this feature. Please enable location services in the app settings.");
            return;
        }
    }

    private boolean isLocationEnabled() {
        int locationMode = Settings.Secure.getInt(
                reactContext.getContentResolver(),
                Settings.Secure.LOCATION_MODE,
                Settings.Secure.LOCATION_MODE_OFF
        );
        return locationMode != Settings.Secure.LOCATION_MODE_OFF;
    }

    private void showPermissionAlert(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getCurrentActivity());
        builder.setMessage(message)
                .setPositiveButton("Go to Settings", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        openAppSettings();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void openAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", reactContext.getPackageName(), null);
        intent.setData(uri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        reactContext.startActivity(intent);
    }

    private LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            showToast("Location callback");
            if (locationResult != null) {
                // Handle location updates here
                Location location = locationResult.getLastLocation();
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                float accuracy = location.getAccuracy();

                String message = "Latitude: " + latitude + ", Longitude: " + longitude;
                showToast(message);
                sendLocationToReact(latitude , longitude , accuracy);
            }
        }
    };

    private void showToast(String message) {
        Toast.makeText(reactContext, message, Toast.LENGTH_SHORT).show();
    }

private void sendLocationToReact(double latitude, double longitude, float accuracy) {
    WritableMap locationMap = Arguments.createMap();
    locationMap.putDouble("latitude", latitude);
    locationMap.putDouble("longitude", longitude);
    locationMap.putDouble("accuracy", accuracy);

    getReactApplicationContext()
        .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
        .emit("onLocationUpdate", locationMap);
    // showToast(accuracy);
}
}
