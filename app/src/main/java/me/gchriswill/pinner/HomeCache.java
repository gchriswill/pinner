package me.gchriswill.pinner;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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
import com.twitter.sdk.android.Twitter;

import io.fabric.sdk.android.Fabric;

public class HomeCache extends AppCompatActivity {

    private static final String TAG = "HomeCache";

    FloatingActionButton addUser;
    FirebaseAuth mAuth;

    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);

        mAuth = FirebaseAuth.getInstance();

        //addUser = (FloatingActionButton) findViewById(R.id.fab);

        // Initializing Firebase authentication listener ===========================================
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user != null) {

                    Log.e(TAG, "onAuthStateChanged:signed_in:" + user.getUid() );
                    Log.e(TAG, "onAuthStateChanged:signed_in:" + user.getDisplayName() );
                    Log.e(TAG, "onAuthStateChanged:signed_in:" + user.getEmail() );

                    Snackbar.make(HomeCache.this.getWindow().getDecorView(),
                            "You had been logged in", Snackbar.LENGTH_LONG).show();

                } else {

                    Log.e(TAG, "onAuthStateChanged:signed_out");

                    startActivity(new Intent(HomeCache.this, Chooser.class) );

                }

            }

        };
        // =========================================================================================

    }

    @Override
    protected void onStart() {
        super.onStart();

        addUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Snackbar.make(HomeCache.this.getWindow().getDecorView(), "Add user button triggered",
                        Snackbar.LENGTH_LONG).show();

            }

        });

        // Setting Firebase's authentication listener to Firebase's authentication object
        mAuth.addAuthStateListener(mAuthListener);

    }

    @Override
    protected void onStop() {
        super.onStop();

        // Removing Firebase's authentication listener
        if (mAuthListener != null) {

            mAuth.removeAuthStateListener(mAuthListener);

        }

        addUser.setOnClickListener(null);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the HomeCache/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.home_action_logout) {

            mAuth.signOut();

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

            //startActivity(new Intent(this, Chooser.class));

            return true;
        }

        return super.onOptionsItemSelected(item);

    }

}
