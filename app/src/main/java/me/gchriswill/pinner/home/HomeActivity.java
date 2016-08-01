package me.gchriswill.pinner.home;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.Auth;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.twitter.sdk.android.Twitter;

import io.fabric.sdk.android.Fabric;
import me.gchriswill.pinner.Chooser;
import me.gchriswill.pinner.R;
import me.gchriswill.pinner.User;
import me.gchriswill.pinner.UserLocation;
import me.gchriswill.pinner.editor.AccountEditorActivity;
import me.gchriswill.pinner.profile.ProfileActivity;


public class HomeActivity extends AppCompatActivity implements
        FirebaseAuth.AuthStateListener,
        ValueEventListener,
        SearchDialogFragment.SearchDialogFragmentListener,
        HomeActivityFragment.HomeActivityFragmentInterface, View.OnClickListener, LocationListener {

    private static final String TAG = "HomeActivity";
    public static final String LOCATION_UPDATES = "LOCATION_UPDATES";

    // Firebase Properties
    private FirebaseAuth firebaseAuth;
    private DatabaseReference firebaseRootRef;
    private DatabaseReference firebaseRootRefLatLon;

    // Fragments
    public static FireAdapter fireAdapter;
    public static SearchDialogFragment searchDialogFragment;

    public static final int REQUEST_ACCOUNT = 1001;

    // Location properties
    private static LocationManager mgr;
    private Location location;
    public boolean isLocationAccessible = false;

    // Current Local User
    User currentUser;

    // Warning Dialog
    public static ProgressDialog progressDialog;
    private boolean locationFirstSync = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mgr = (LocationManager) getSystemService(LOCATION_SERVICE);

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

        firebaseAuth = FirebaseAuth.getInstance();

        firebaseRootRef = firebaseDatabase.getReference().child("users");
        firebaseRootRef.keepSynced(true);

        firebaseRootRefLatLon = firebaseDatabase.getReference().child("userlocations");
        firebaseRootRefLatLon.keepSynced(true);

        FloatingActionButton searchAddButton = (FloatingActionButton) findViewById(R.id.home_search_add);
        //noinspection ConstantConditions
        searchAddButton.setOnClickListener(this);

        progressDialog = new ProgressDialog(this);

        boolean locationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED;

        if (locationPermission) {
            String[] permissions = new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            };

            ActivityCompat.requestPermissions(this, permissions,
                    PermissionInfo.PROTECTION_DANGEROUS);

        }else {
            isLocationAccessible = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case PermissionInfo.PROTECTION_DANGEROUS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED ) {
                    isLocationAccessible = true;
                    Log.i(TAG, "onRequestPermissionsResult: Location ACCESS GRANTED");
                    locationUpdates();
                } else {
                    isLocationAccessible = false;
                    Log.i(TAG, "onRequestPermissionsResult: Location ACCESS DENIED");
                    alertUserWithTitleAndMessage("Location Features",
                            "Please be aware that location features will not be available until you enable them...");
                }

                break;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        try {
            this.unregisterReceiver(forecastReceiver);
        }catch (RuntimeException e){
            e.printStackTrace();
        }

        this.registerReceiver(forecastReceiver, new IntentFilter(LOCATION_UPDATES) );
    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(this);
        fireAdapter = new FireAdapter(HomeActivity.this, firebaseRootRef);

        HomeActivityFragment homeActivityFragment = (HomeActivityFragment) getSupportFragmentManager()
                .findFragmentById(R.id.list_fragment);
        homeActivityFragment.setListAdapter(fireAdapter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        firebaseAuth.removeAuthStateListener(this);
        firebaseRootRef.removeEventListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.home_action_logout) {
            firebaseAuth.signOut();

            if (FacebookSdk.isInitialized() ){
                LoginManager.getInstance().logOut();
            }

            if (Fabric.isInitialized() ){
                Twitter.logOut();
            }

            if (Chooser.mGoogleApiClient != null){
                if (Chooser.mGoogleApiClient.isConnected() ){
                    Auth.GoogleSignInApi.signOut(Chooser.mGoogleApiClient);
                    Log.e(TAG, "onOptionsItemSelected: Logged out from Google" );
                }
            }

            removeLocationUpdates();

            return true;
        }

        if (id == R.id.action_account){
            Intent intent = new Intent(HomeActivity.this, AccountEditorActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPositiveButtonClicked(String username) {
        progressDialog.show();
        ProfileSearchController profileSearchController = new ProfileSearchController(this,
                firebaseRootRef);
        profileSearchController.onSearchForProfileWithUsername(username);
    }

    @Override
    public void onCancelButtonClicked() {
        searchDialogFragment.dismiss();
    }

    @Override
    public void showAlertWithLocalErrorFormat(String title, String message) {
        alertUserWithTitleAndMessage(title, message);
    }

    @Override
    public void onItemClicked(User user) {
        Intent intent = new Intent(this, ProfileActivity.class);
        intent.putExtra("USER_OBJECT", user);
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        searchDialogFragment = SearchDialogFragment.newInstanceOf();
        searchDialogFragment.show(getSupportFragmentManager(), SearchDialogFragment.TAG);
    }

    private void alertUserWithTitleAndMessage(String title, String message) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder( this );
        alertBuilder.setTitle(title);
        alertBuilder.setIcon(R.drawable.ic_error_outline);
        alertBuilder.setMessage(message);
        alertBuilder.setPositiveButton("Ok", null).create().show();
    }

    // Firebase Methods
    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            firebaseRootRef.orderByChild("userId")
                    .equalTo(firebaseUser.getUid()).removeEventListener(this);
            firebaseRootRef.orderByChild("userId")
                    .equalTo(firebaseUser.getUid()).keepSynced(true);
            firebaseRootRef.orderByChild("userId")
                    .equalTo(firebaseUser.getUid()).addValueEventListener(this);

            Log.e(TAG, "onAuthStateChanged: \n"
                    + "Signed in User :---> " + firebaseUser.getEmail() + "\n"
                    + "With uid: " + firebaseUser.getUid() );
        } else {
            Intent intent = new Intent(HomeActivity.this, Chooser.class);
            startActivityForResult(intent, REQUEST_ACCOUNT);
        }
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        Iterable<DataSnapshot> iterableData = dataSnapshot.getChildren();
        for (DataSnapshot d : iterableData) {
            currentUser = d.getValue(User.class);
            Log.e(TAG, "onDataChange: ON USER ---> " + currentUser.displayName);
        }

        if (currentUser != null){
            if (!locationFirstSync && isLocationAccessible && currentUser.isSharingLocation){
                locationUpdates();
                locationFirstSync = true;
            }
        }
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
        Log.e(TAG, "onCancelled: Listener value event listener was cancelled");
    }
    // End of Firebase Methods

    // Location Updates Methods
    @SuppressWarnings("ResourceType")
    public void locationUpdates() {
        if (isLocationAccessible){
            mgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, this);
            location = mgr.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }
    }

    @SuppressWarnings("ResourceType")
    private void removeLocationUpdates(){
        if (isLocationAccessible) {
            if (mgr != null){
                mgr.removeUpdates(this);
            }
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}

    @Override
    public void onLocationChanged(Location location) {
        Log.i(TAG, "onLocationChanged: " + location.toString() + " Location changed");
        this.location = location;

        UserLocation userLocation = new UserLocation(location.getLatitude(), location.getLongitude());
        firebaseRootRefLatLon.child(firebaseAuth.getCurrentUser().getUid()).setValue(userLocation);
    }

    @Override @SuppressWarnings("ResourceType")
    public void onProviderEnabled(String provider) {
        if(provider.equals(LocationManager.GPS_PROVIDER) ) {
            Log.i(TAG, "onProviderEnabled: " + provider + " Location Updates Enabled");

            mgr.requestLocationUpdates(provider, 5000, 10, this);
            location = mgr.getLastKnownLocation(provider);
        }
    }

    @Override @SuppressWarnings("ResourceType")
    public void onProviderDisabled(String provider) {
        Log.i(TAG, "onProviderDisabled: " + provider +" Location Update disabled");
        if(provider.equals(LocationManager.GPS_PROVIDER) ) mgr.removeUpdates(this);
    }

    // Receiver For Location updates
    private BroadcastReceiver forecastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(TAG, "onReceive: " + "Broadcast Received" );
            if(intent.getAction().equals(LOCATION_UPDATES) ) {
                Log.e(TAG, "onReceive: " + LOCATION_UPDATES );
                if (intent.hasExtra("START")){
                    Log.e(TAG, "onReceive: " + "start_location_updates" );
                    HomeActivity.this.locationUpdates();
                }else if (intent.hasExtra("STOP") ){
                    Log.e(TAG, "onReceive: " + "stop_location_updates" );
                    HomeActivity.this.removeLocationUpdates();
                }
            }
        }

    };
}
