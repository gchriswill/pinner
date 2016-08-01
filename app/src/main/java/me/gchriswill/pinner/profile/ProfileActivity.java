package me.gchriswill.pinner.profile;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import me.gchriswill.pinner.MapsActivity;
import me.gchriswill.pinner.R;
import me.gchriswill.pinner.User;


public class ProfileActivity extends AppCompatActivity implements View.OnClickListener,
        ProfileActivityFragment.ProfileActivityFragmentInterface,
        ValueEventListener, OnSuccessListener<byte[]> {

    private static final String TAG = "ProfileActivity";

    private User userProfile;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private ProfileActivityFragment profileActivityFragment;

    private byte[] imageBytes;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Intent intent = getIntent();
        userProfile = (User) intent.getSerializableExtra("USER_OBJECT");

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference().child("users");
        databaseReference.keepSynced(true);

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReferenceFromUrl("gs://profile-widgets.appspot.com");
    }

    @Override
    protected void onResume() {
        super.onResume();
        profileActivityFragment = (ProfileActivityFragment) getSupportFragmentManager().findFragmentById(R.id.profile_fragment);

        databaseReference.orderByChild("userId").equalTo(userProfile.userId).addValueEventListener(this);

        String location = "/users/" + userProfile.userId + "/profileImage.jpg";
        storageReference.child(location).getBytes(Long.MAX_VALUE).addOnSuccessListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_delete) {
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            if (firebaseUser != null){
                userProfile.friends.remove( firebaseUser.getUid() );
                databaseReference.child(userProfile.userId).child("friends")
                        .setValue(userProfile.friends);
                ProfileActivity.this.finish();
            }else {
                Log.e(TAG, "onOptionsItemSelected: " + " FirebaseUser is null..." );
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public User getUserData() {
        return userProfile;
    }

    @Override
    public Bitmap setUserProfileImage() {

        Bitmap output = Bitmap.createBitmap(172, 172, Bitmap.Config.ARGB_8888);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 8;
        options.inScaled = true;

        Canvas canvas = new Canvas(output);

        int color = Color.RED;
        Paint paint = new Paint();
        Rect rect = new Rect(0, 0, 172 , 172);
        RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawOval(rectF, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

        canvas.drawBitmap( BitmapFactory.decodeByteArray(imageBytes,0, imageBytes.length, options), null, rect, paint);

        return output;
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        Iterable<DataSnapshot> iterableData = dataSnapshot.getChildren();
        for (DataSnapshot d : iterableData) {
            userProfile = d.getValue(User.class);
            Log.e(TAG, "onDataChange: USER ---> " + userProfile.displayName);
        }

        profileActivityFragment.getUser();
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {}

    @Override
    public void onSuccess(byte[] bytes) {
        this.imageBytes = bytes;
        profileActivityFragment.getUserProfileImage();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){

            case R.id.profile_viewer_address_field_action_button:

                Intent intent = new Intent(this, MapsActivity.class);
                intent.putExtra(MapsActivity.LOCATED_USER, userProfile);
                intent.putExtra(MapsActivity.MAP_MODE_USER_ADDRESS, "MAP_MODE_USER_ADDRESS");
                startActivity(intent);
                break;

            case R.id.profile_viewer_current_location_field_action_button:

                Intent intent2 = new Intent(this, MapsActivity.class);
                intent2.putExtra(MapsActivity.LOCATED_USER, userProfile);
                intent2.putExtra(MapsActivity.MAP_MODE_USER_LOCATION, "MAP_MODE_USER_LOCATION");
                startActivity(intent2);
                break;
        }
    }
}
