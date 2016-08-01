package me.gchriswill.pinner.home;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import me.gchriswill.pinner.R;
import me.gchriswill.pinner.User;

//
// Created by gchriswill on 5/21/16.
//

/* --- FireAdapter ---
*
* Custom BaseAdapter subclass with Firebase integration
*
* */
public class FireAdapter extends BaseAdapter implements ValueEventListener {

    private static final String TAG = "FireAdapter";

    public Map<String, User> source = new HashMap<>();

    Context context;
    String currentId;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    public FireAdapter(Context context, DatabaseReference reference) {

        this.context = context;

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if( firebaseUser != null){

            databaseReference = reference;
            databaseReference.keepSynced(true);

            //noinspection ConstantConditions
            currentId = firebaseUser.getUid();

            addListeners();

        }else {

            Log.e(TAG, "FireAdapter: " +  "CAN'T ACCESS Home STAGE WITHOUT BEING LOGGED IN !!!");
            //throw new IllegalStateException("CAN'T ACCESS THIS STAGE WITHOUT BEING LOGGED IN !!!");

        }

    }

    public void removeListeners(){

        databaseReference.removeEventListener(this);

    }

    public void addListeners(){

        String reference = "friends/" + currentId;
        databaseReference.orderByChild(reference).equalTo(true).addValueEventListener(this);

    }

    @Override
    public int getCount() {

        return source.values().toArray().length;

    }

    @Override
    public Object getItem(int position) {

        return source.values().toArray()[position];

    }

    @Override
    public long getItemId(int position) {
        return position;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {

            convertView = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);

        }

        User userV = (User) source.values().toArray()[position];

        TextView usernameTv = (TextView) convertView.findViewById(R.id.list_item_username);
        usernameTv.setText(userV.displayName);

        convertView.setTag(userV);

        return convertView;

    }

    // Master Listeners
    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {

        source.clear();

        //Log.e(TAG, "onDataChange: " + dataSnapshot.toString() );

        Iterable<DataSnapshot> iterableData = dataSnapshot.getChildren();

        for (DataSnapshot d : iterableData ){

            User tempUser = d.getValue(User.class);

            source.put(tempUser.userId, tempUser);

        }

        this.notifyDataSetChanged();

    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

        //throw new IllegalStateException(databaseError.getMessage());

    }

}
