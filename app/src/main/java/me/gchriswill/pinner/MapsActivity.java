package me.gchriswill.pinner;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, ValueEventListener {

    public static final String TAG = "MapsActivity";
    public static final String LOCATED_USER = "LOCATED_USER";
    public static final String MAP_MODE_USER_LOCATION = "MAP_MODE_USER_LOCATION";
    public static final String MAP_MODE_USER_ADDRESS = "MAP_MODE_USER_ADDRESS";

    private GoogleMap mMap;
    private SupportMapFragment mapFragment;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;

    User locatedUser;
    String mapMode;
    private boolean firstCameraMove = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        Intent intent = getIntent();
        firebaseDatabase = FirebaseDatabase.getInstance();

        if (intent.hasExtra(LOCATED_USER)){
            locatedUser = (User) intent.getSerializableExtra(LOCATED_USER);
        }

        if(intent.hasExtra(MAP_MODE_USER_LOCATION)) {
            mapMode = MAP_MODE_USER_LOCATION;
            databaseReference = firebaseDatabase.getReference().child("userlocations");
        }

        if(intent.hasExtra(MAP_MODE_USER_ADDRESS)) {
            mapMode = MAP_MODE_USER_ADDRESS;
            databaseReference = firebaseDatabase.getReference().child("users");
        }

        databaseReference.keepSynced(true);

        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

        if (mMap == null){
            mMap = googleMap;
            mMap.setContentDescription("This is the content description...");

            boolean locationPermission = ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

            if (locationPermission) mMap.setMyLocationEnabled(true);
        }

        if (mapMode == MAP_MODE_USER_LOCATION)
            databaseReference.orderByKey().equalTo(locatedUser.userId).addValueEventListener(this);

        if (mapMode == MAP_MODE_USER_ADDRESS){

            LatLng userLocationLatLon = new LatLng((Double) locatedUser.location.get("latitude"),
                    (Double) locatedUser.location.get("longitude") );

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocationLatLon, 16.0f));

            mMap.clear();

            mMap.addMarker(new MarkerOptions()
                    .position(userLocationLatLon)
                    .title("User's Address")
                    .snippet("The latest known address of "+ locatedUser.displayName)).showInfoWindow();
        }
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {

        Iterable<DataSnapshot> iterableData = dataSnapshot.getChildren();
        UserLocation userLocation = new UserLocation();

        for (DataSnapshot d : iterableData) {
            userLocation = d.getValue(UserLocation.class);
        }

        LatLng userLocationLatLon = new LatLng(userLocation.latitude, userLocation.longitude);

        if (firstCameraMove){
            firstCameraMove = false;
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocationLatLon, 16.0f));
        }else{
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocationLatLon, 16.0f));
        }

        mMap.clear();
        mMap.addMarker(new MarkerOptions()
                .position(userLocationLatLon)
                .title("User's Location")
                .snippet("This is the last known location of "+ locatedUser.displayName)).showInfoWindow();
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {}
}
