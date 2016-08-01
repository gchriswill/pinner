package me.gchriswill.pinner.editor;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
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
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import me.gchriswill.pinner.R;
import me.gchriswill.pinner.User;
import me.gchriswill.pinner.home.HomeActivity;

public class AccountEditorActivity extends AppCompatActivity implements
        AccountEditorActivityFragment.AccountEditorActivityFragmentInterface,
        View.OnClickListener, ValueEventListener,
        OnSuccessListener<byte[]>,
        CompoundButton.OnCheckedChangeListener {

    private static final String TAG = "AccountEditorActivity";
    private static final int REQUEST_CAMERA = 1001;
    public static final int PLACE_PICKER_REQUEST = 1012;

    private Menu menu;
    private Uri imageUri;
    private byte[] imageBytes;
    private User resultingUser;
    private FirebaseStorage storage;
    private FirebaseUser firebaseUser;
    private FirebaseDatabase firebaseDatabase;
    private StorageReference storageReference;
    private DatabaseReference databaseReference;
    private AccountEditorActivityFragment accountEditorActivityFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_editor);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser != null){

            storage = FirebaseStorage.getInstance();
            storageReference = storage.getReferenceFromUrl("gs://profile-widgets.appspot.com");

            firebaseDatabase = FirebaseDatabase.getInstance();
            databaseReference = firebaseDatabase.getReference().child("users");

        }else {
            throw new IllegalStateException("User must be looged in to access account management stage");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        accountEditorActivityFragment = (AccountEditorActivityFragment) getSupportFragmentManager()
                .findFragmentById(R.id.account_editor_fragment);

        databaseReference.orderByChild("userId").equalTo(firebaseUser.getUid()).keepSynced(true);
        databaseReference.orderByChild("userId").equalTo(firebaseUser
                .getUid()).addValueEventListener(this);

        String location = "/users/" + firebaseUser.getUid() + "/profileImage.jpg";
        storageReference.child(location).getBytes(Long.MAX_VALUE).addOnSuccessListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        databaseReference.orderByChild("userId").equalTo(firebaseUser
                .getUid()).removeEventListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_account_editor, menu);

        this.menu = menu;

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        item.setVisible(false);

        switch (id){
            case R.id.action_edit:
                menu.findItem(R.id.action_save).setVisible(true);
                menu.findItem(R.id.action_cancel).setVisible(true);
                menu.findItem(R.id.action_delete).setVisible(false);

                accountEditorActivityFragment.setFormEnabled(true);

                break;
            case R.id.action_save:
                menu.findItem(R.id.action_cancel).setVisible(false);
                menu.findItem(R.id.action_edit).setVisible(true);
                menu.findItem(R.id.action_delete).setVisible(true);

                accountEditorActivityFragment.setFormEnabled(false);

                accountEditorActivityFragment.getFormValues();

                Log.e(TAG, "onOptionsItemSelected: " + resultingUser.friends );
                databaseReference.child(resultingUser.userId).setValue(resultingUser)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.e(TAG, "onSuccess: User account changes has been saved successfully" );
                            }
                });

                break;
            case R.id.action_cancel            :
                menu.findItem(R.id.action_save).setVisible(false);
                menu.findItem(R.id.action_edit).setVisible(true);
                menu.findItem(R.id.action_delete).setVisible(true);

                accountEditorActivityFragment.setFormEnabled(false);

                //accountEditorActivityFragment.setValues();

                break;
            case R.id.action_delete:
                // Delete root user object in Firebase database
                // Return to Home
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public static Uri createImageFile() {
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMddyyyy_HHmmss");
        String imgName = simpleDateFormat.format( new Date( System.currentTimeMillis() ) );

        File directory = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);

        File applicationDirectory = new File(directory, "pinner");

        if ( !applicationDirectory.mkdirs() ) {
            Log.i(TAG, "createFileWithURI: --> FILE AND URI WERE NOT CREATED OR ALREADY EXISTS...");
        }else{
            Log.i(TAG, "createFileWithURI: --> FOLDER \"pinner\" WAS CREATED in public DIRECTORY_PICTURES" );
        }

        File imagePath = new File(applicationDirectory, imgName + ".jpg");

        try {
            if ( imagePath.createNewFile() ){
                Log.i(TAG, "createFileWithURI: --> FILE WAS CREATED" );
            }
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }

        return Uri.fromFile(imagePath);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CAMERA) {
            if (resultCode == RESULT_OK) {
                accountEditorActivityFragment.getUserProfileImage();

                String location = "/users/" + firebaseUser.getUid() + "/profileImage.jpg";
                storageReference.child(location).putFile(imageUri)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {

                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                                Log.e(TAG, "onSuccess: " + downloadUrl );

                            }

                });
            } else if (resultCode == RESULT_CANCELED) {
                // User cancelled the image capture
                Log.e(TAG, "onActivityResult: User cancelled the image capture activity..." );
            } else {
                // Image capture failed, advise user
                Log.e(TAG, "onActivityResult: Image capture activity failed to capture image..." );
            }
        }

        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(this, data);
                String address = String.valueOf(place.getAddress());

                resultingUser.location.put("address", address);
                resultingUser.location.put("latitude", place.getLatLng().latitude);
                resultingUser.location.put("longitude", place.getLatLng().longitude);

                accountEditorActivityFragment.setFormValues();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PermissionInfo.PROTECTION_DANGEROUS){
            if (grantResults.length > 0) {
                Log.e(TAG, "onRequestPermissionsResult: " + permissions[0] + " " + (grantResults[0] == PackageManager.PERMISSION_GRANTED) );
                Log.e(TAG, "onRequestPermissionsResult: " + permissions[1] + " " + (grantResults[1] == PackageManager.PERMISSION_GRANTED) );
                // Camera Features with external Storage Permissions Checkers
                boolean checkTypeCamera = permissions[0].equals(Manifest.permission.CAMERA),
                        grantedCamera = grantResults[0] == PackageManager.PERMISSION_GRANTED,
                        checkCameraPermission = checkTypeCamera && grantedCamera;

                boolean checkTypeWES = permissions[1].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        grantedWES = grantResults[1] == PackageManager.PERMISSION_GRANTED,
                        checkWESPermission = checkTypeWES && grantedWES;

                boolean allowCameraAccess = checkCameraPermission && checkWESPermission;

                if (allowCameraAccess) {
                    Log.e(TAG, "onRequestPermissionsResult: Camera ACCESS GRANTED");
                    imageUri = createImageFile();
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                    startActivityForResult(intent, REQUEST_CAMERA);
                }else{
                    Log.e(TAG, "onRequestPermissionsResult: Camera ACCESS DENIED");
                }
            }
        }
    }

    @Override
    public void setUserValues(User user) {
        this.resultingUser = user;
    }

    @Override
    public User getUserValues() {
        return resultingUser;
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

        if (imageUri != null){
            canvas.drawBitmap(BitmapFactory.decodeFile(imageUri.getEncodedPath(),
                    options), null, rect, paint);
        }else {
            canvas.drawBitmap( BitmapFactory.decodeByteArray(imageBytes,0, imageBytes.length,
                    options), null, rect, paint);
        }

        return output;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.account_editor_address_picker_button){
            PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
            try {
                startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
            } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
                e.printStackTrace();
            }
        }

        if (v.getId() == R.id.account_editor_camera_button){

            boolean permissionCheck0 = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED;

            boolean permissionCheck1 = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED;

            if (permissionCheck0 && permissionCheck1) {

                String[] permissions = new String[]{
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                };

                ActivityCompat.requestPermissions(this, permissions,
                        PermissionInfo.PROTECTION_DANGEROUS);

            }else {
                Log.e(TAG, "requestCameraPermission: PERMISSION ALREADY GRANTED" );
                Log.e(TAG, "requestCameraPermission: PROCEEDING TO CAMERA ACTIVITY" );
                imageUri = createImageFile();
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(intent, REQUEST_CAMERA);
            }
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        Log.e("AEA", "onCheckedChanged: " + "CHECK EVENT" );
        switch (compoundButton.getId() ){
            case R.id.account_editor_checkbox_layout_phone:
                Log.e("AEA", "onCheckedChanged: " + "PHONE checkbox" );
                break;
            case R.id.account_editor_checkbox_layout_text:
                Log.e("AEA", "onCheckedChanged: " + "TEXT checkbox" );

                break;
            case R.id.account_editor_checkbox_layout_address:
                Log.e("AEA", "onCheckedChanged: " + "ADDRESS checkbox" );
                break;
            case R.id.account_editor_checkbox_layout_location:

                Log.e("AEA", "onCheckedChanged: " + "LOCATION checkbox" );
                Intent newIntent = new Intent(HomeActivity.LOCATION_UPDATES);

                if (compoundButton.isChecked()){
                    Log.e("AEA", "onCheckedChanged: " + "LOCATION checkbox starting" );
                    newIntent.putExtra("START", "start_location_updates");
                }else{
                    Log.e("AEA", "onCheckedChanged: " + "LOCATION checkbox stopping" );
                    newIntent.putExtra("STOP", "stop_location_updates");
                }

                sendBroadcast(newIntent);

                break;
        }
    }

    // Firebase Methods
    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        Iterable<DataSnapshot> iterableData = dataSnapshot.getChildren();
        for (DataSnapshot d : iterableData) {
            resultingUser = d.getValue(User.class);
            Log.e(TAG, "onDataChange: ON USER ---> " + resultingUser.displayName);
        }

        accountEditorActivityFragment.setFormValues();
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
        Log.e(TAG, "onCancelled: Listener value event listener was cancelled");
        AccountEditorActivity.this.finish();
    }

    @Override
    public void onSuccess(byte[] bytes) {
        Log.e(TAG, "onSuccess: Storage Accessed" );
        this.imageBytes = bytes;
        accountEditorActivityFragment.getUserProfileImage();
    }
    // End of Firebase Methods
}
