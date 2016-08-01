package me.gchriswill.pinner.home;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import me.gchriswill.pinner.R;
import me.gchriswill.pinner.User;

/**
 * Created by gchriswill on 5/28/16.
 */
public class ProfileSearchController implements ValueEventListener {

    private static final String TAG = "ProfileSearchController";

    Context context;
    DatabaseReference databaseReference;
    User resultingUser;

    public ProfileSearchController(){}

    public ProfileSearchController(Context context, DatabaseReference databaseReference){

        this.context = context;
        this.databaseReference = databaseReference;

    }

    public void onSearchForProfileWithUsername(String username){

        databaseReference.orderByChild("email").equalTo(username).addListenerForSingleValueEvent(this);

    }

    public void removeValueEventListener(){

        databaseReference.removeEventListener(this);

    }

    private void alertUserWithTitleAndMessage(String title, String message, int alertType) {


        AlertDialog.Builder alertBuilder = new AlertDialog.Builder( context )
                .setTitle(title)
                .setMessage(message);

        if (alertType == 0){

            alertBuilder.setIcon(R.drawable.ic_error_outline)
                    .setPositiveButton("Ok", null)
                    .create()
                    .show();

        }

        if (alertType == 1){

            alertBuilder.setIcon(R.drawable.ic_person_add)
                    .setPositiveButton("Add Friend", new DialogInterface.OnClickListener() {


                        @Override

                        public void onClick(DialogInterface dialog, int which) {

                            Map<String, Object> mapper = new HashMap<>();
                            //noinspection ConstantConditions
                            mapper.put(FirebaseAuth.getInstance().getCurrentUser().getUid(), true);

                            String reference = resultingUser.userId + "/friends";
                            databaseReference.child(reference).updateChildren(mapper);
                            HomeActivity.searchDialogFragment.dismiss();
                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            dialog.dismiss();

                        }

                    });

            alertBuilder.create().show();

        }

    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {

        // Retrieving an object with data that we can iterate through
        Iterable<DataSnapshot> iterableData = dataSnapshot.getChildren();

        if (dataSnapshot.getValue() == null) {

            alertUserWithTitleAndMessage("Profile NOT Found!", "The profile you've searching for " +
                    "was not found. Please try another username...", 0);

        } else {

            for (DataSnapshot d : iterableData) {

                resultingUser = d.getValue(User.class);
                Log.e(TAG, "onDataChange: USER ---> " + resultingUser.displayName);

            }

            alertUserWithTitleAndMessage(resultingUser.displayName + "'s profile was found!",
                    "Do you want to add this profile to your profile list?", 1);

        }

        HomeActivity.progressDialog.dismiss();

    }

    @Override
    public void onCancelled(DatabaseError databaseError) {}

}
