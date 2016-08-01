package me.gchriswill.pinner.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
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
import android.util.Log;
import android.widget.RemoteViews;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

import me.gchriswill.pinner.MapsActivity;
import me.gchriswill.pinner.R;
import me.gchriswill.pinner.User;
import me.gchriswill.pinner.UserLocation;

public class WidgetHelper implements ValueEventListener, OnSuccessListener<byte[]> {

    private StorageReference storageReference;
    private FirebaseStorage storage;
    private String TAG = "WidgetHelper";
    private Context context;
    private User userProfile;
    private UserLocation userLocation;
    private int widgetId;
    private DatabaseReference firebaseRootRef;
    private RemoteViews remoteViews;

    public WidgetHelper(Context context) {
        this.context = context;

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseRootRef = firebaseDatabase.getReference().child("users");
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReferenceFromUrl("gs://profile-widgets.appspot.com/");
    }

    public void updateWidget( int widgetId) {
        String selectedProfileId = SettingsManager.getSelectedProfile(context);

        this.widgetId = widgetId;

        if(!selectedProfileId.equals("INVALID_PROFILE") ){

            Log.e(TAG, "setUpUserProfile: " + selectedProfileId);

            firebaseRootRef.orderByChild("userId").equalTo(selectedProfileId).addValueEventListener(this);
        }
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        Iterable<DataSnapshot> iterableData = dataSnapshot.getChildren();

        for (DataSnapshot d : iterableData) {
            userProfile = d.getValue(User.class);
            Log.e(TAG, "onDataChange: " + userProfile.displayName);
        }

        setTargetShortcut();
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {}

    public void setTargetShortcut() {
        String shortcutType = SettingsManager.getShortcutType(context);

        final Intent intent = new Intent();
        int typeId = 0;

        switch (shortcutType) {
            case "Mail":
                // Setting the laytout of the widthget
                typeId = R.layout.widget_email;

                // Setting up the Implicit intent for calling the default email activity
                intent.setAction(android.content.Intent.ACTION_SEND);
                intent.setData(Uri.parse("mailto:") );
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{userProfile.email} );
                intent.setType("text/plain");

                // Evaluating the activities available to handle the specified action
                PackageManager packageManager = context.getPackageManager();
                ArrayList<ResolveInfo> activities = (ArrayList<ResolveInfo>) packageManager
                        .queryIntentActivities(intent, 0);

                for (final ResolveInfo i : activities){
                    boolean packageNameCheck = i.activityInfo.packageName.endsWith(".gm");
                    boolean nameCheck = i.activityInfo.name.toLowerCase().contains("gmail");

                    if (packageNameCheck || nameCheck ) {
                        intent.setClassName(i.activityInfo.packageName, i.activityInfo.name);
                        break;
                    }
                }

                break;
            case "Phone":
                // Setting the laytout of the widthget
                typeId = R.layout.widget_phone;

                // Setting up the Implicit intent for calling the default phone activity
                intent.setAction(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + userProfile.phone) );

                break;
            case "Text":
                // Setting the laytout of the widthget
                typeId = R.layout.widget_text;
                // Setting up the Implicit intent for calling the default sms activity
                intent.setAction(Intent.ACTION_SEND);
                intent.setData(Uri.parse("sms:") );
                intent.putExtra("address"  ,  userProfile.phone);
                intent.setType("text/plain");

                break;
            case "Address":
                // Setting the laytout of the widthget
                typeId = R.layout.widget_address;
                // Setting up the Implicit intent for calling the custom maps activity
                intent.putExtra(MapsActivity.LOCATED_USER, userProfile);
                intent.putExtra(MapsActivity.MAP_MODE_USER_ADDRESS, "MAP_MODE_USER_ADDRESS");
                intent.setClass(context, MapsActivity.class);

                break;
            case "Location":
                // Setting the laytout of the widthget
                typeId = R.layout.widget_location;
                // Setting up the Implicit intent for calling the custom maps activity
                intent.putExtra(MapsActivity.LOCATED_USER, userProfile);
                intent.putExtra(MapsActivity.MAP_MODE_USER_LOCATION, "MAP_MODE_USER_LOCATION");
                intent.setClass(context, MapsActivity.class);

                break;
        }

        remoteViews = new RemoteViews(context.getPackageName(), typeId);

        storageReference.child("/users/"+userProfile.userId+"/profileImage.jpg")
                .getBytes(Long.MAX_VALUE).addOnSuccessListener(this);

        PendingIntent configPendingIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        remoteViews.setOnClickPendingIntent(R.id.circular_image_icon_button, configPendingIntent);
        remoteViews.setOnClickPendingIntent(R.id.circular_image_button, configPendingIntent);

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        appWidgetManager.updateAppWidget(widgetId, remoteViews);
    }

    @Override
    public void onSuccess(byte[] bytes) {
        Log.e(TAG, "onSuccess: " + bytes.length );

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 8;
        options.inScaled = true;

        Bitmap output = Bitmap.createBitmap(172, 172, Bitmap.Config.ARGB_8888);
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
        canvas.drawBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options), null, rect, paint);

        remoteViews.setImageViewBitmap(R.id.circular_image_button, output);

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        appWidgetManager.updateAppWidget(widgetId, remoteViews);
    }
}