package me.gchriswill.pinner.widget;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import me.gchriswill.pinner.Chooser;
import me.gchriswill.pinner.R;
import me.gchriswill.pinner.User;

public class ConfigureAcitivity extends AppCompatActivity implements ValueEventListener,
        ConfigureActivityFragment.ConfigureActivityFragmentInterface {

    private static final String TAG = "Configure";

    private int mWidgetID;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference firebaseRootRef;
    private ArrayList<User> profiles = new ArrayList<>();
    private ConfigureActivityFragment configureActivityFragment;
    private ArrayList<CharSequence> ids;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.configuration_layout);

        Intent intent = getIntent();
        mWidgetID = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);

        if (mWidgetID == AppWidgetManager.INVALID_APPWIDGET_ID){
            finish();
            return;
        }

        configureActivityFragment = new ConfigureActivityFragment();

        getFragmentManager().beginTransaction().replace(R.id.configuration_container,
                configureActivityFragment,
                ConfigureActivityFragment.TAG).commit();

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        if(firebaseUser == null){
            startActivity(new Intent(ConfigureAcitivity.this, Chooser.class) );
        }else{
            FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
            firebaseRootRef = firebaseDatabase.getReference().child("users");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        String reference = "friends/" + firebaseUser.getUid();
        firebaseRootRef.orderByChild(reference).equalTo(true).addValueEventListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        String reference = "friends/" + firebaseUser.getUid();
        firebaseRootRef.orderByChild(reference).equalTo(true).removeEventListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.configuration_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_done) {
            WidgetHelper widgetHelper = new WidgetHelper(this);
            widgetHelper.updateWidget( mWidgetID);

            Intent intent = new Intent();
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mWidgetID);
            setResult(RESULT_OK, intent);

            finish();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        Iterable<DataSnapshot> iterableData = dataSnapshot.getChildren();

        for (DataSnapshot d : iterableData ){
            User tempUser = d.getValue(User.class);
            Log.e(TAG, "onDataChange: " + tempUser.email );
            profiles.add(tempUser);
        }

        configureActivityFragment.getProfiles();
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {}

    @Override
    public ArrayList<User> getProfileList() {
        return profiles;
    }

    @Override
    public void setWidgetType(String type) {SettingsManager.setShortcutType(this, type );}

    @Override
    public void setWidgetProfile(String id) {SettingsManager.setSelectedProfile(this, id );}
}
