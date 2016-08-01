package me.gchriswill.pinner;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.TwitterAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import io.fabric.sdk.android.Fabric;

public class Chooser extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener, SetupDialog.AccountDialogListener, OnCompleteListener<AuthResult> {

    //
    private static final String TAG = "Chooser";

    // TODO: Consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_KEY = "Xw42ob9CmP0HcsKVUPCdEfmVY";
    private static final String TWITTER_SECRET = "cHhOoKlG7QFmU89UGm0jciGM1zNBUqaNGAk3xRpBthpX6Rq9n0";
    private static final int RC_SIGN_IN = 10001;

    // Firebase properties
    private FirebaseAuth mAuth;
    private FirebaseDatabase mFDB;
    private FirebaseUser mFuser;

    // Twitter Properties
    private TwitterLoginButton twitterLoginButton;

    // Google Properties
    public static GoogleApiClient mGoogleApiClient;
    SignInButton signInButton;

    // Facebook properties
    CallbackManager mCallbackManager;
    LoginButton faceBookLoginButton;

    // Android UI reference properties
    private Button loginButton;
    private Button createButton;
    private ProgressDialog progressDialog;
    private SetupDialog setUpDialogFragment;

    // User Record
    private User user;

    // Modes
    private long dialogMode;
    private AlertDialog alertation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // Init SDK's ==============================================================================

        // Initializing Facebook SKD (Needs to be initialize right after the super call)
        FacebookSdk.sdkInitialize(getApplicationContext());

        // Initializing Twitter's Fabric SKD (Needs to be initialize right after the super call)
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);

        Fabric.with(this, new Twitter(authConfig));

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .requestProfile()
                .build();

        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        //==========================================================================================


        // DEFAULT CODE
        setContentView(R.layout.activity_chooser);


        // Initializing Firebase properties ========================================================
        mAuth = FirebaseAuth.getInstance();
        mFDB = FirebaseDatabase.getInstance();

        // =========================================================================================

        // Twitter Logging setup ===================================================================

        // Initializing Twitter login button reference property
        twitterLoginButton = (TwitterLoginButton) findViewById(R.id.twitter_login_button);

        // Registering call back manager with Twitter's call back
        twitterLoginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                // TwitterSession is also available through:
                // Twitter.getInstance().core.getSessionManager().getActiveSession()
                handleTwitterSession(result.data);
//                String msg = "@" + session.getUserName() + " logged in! (#" + session.getUserId() + ")";
//                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
            }
            @Override
            public void failure(TwitterException exception) {
                Log.d("TwitterKit", "Login with Twitter failure", exception);
            }
        });
        // =========================================================================================


        // Google Sign in setup ====================================================================
        signInButton = (SignInButton) findViewById(R.id.google_sign_in_button);

        // Setting the size of the Google Button
        signInButton.setSize(SignInButton.SIZE_STANDARD);

        // Setting scopes for the request (permissions)
        signInButton.setScopes(gso.getScopeArray() );

        // Registering call back manager with Google's call back
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }

        });
        // =========================================================================================


        // Facebook login setup ====================================================================
        // Initializing Facebook call back manager
        mCallbackManager = CallbackManager.Factory.create();

        // Initializing Facebook login button reference property
        faceBookLoginButton = (LoginButton) findViewById(R.id.login_button);

        // Setting permissions
        faceBookLoginButton.setReadPermissions("email", "public_profile");

        // Registering call back manager with Facebook's call back
        faceBookLoginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.e(TAG, "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.e(TAG, "facebook:onCancel");
            }

            @Override
            public void onError(FacebookException error) {
                Log.e(TAG, "facebook:onError", error );
            }
        });
        // =========================================================================================

        // Initializing Android UI
        loginButton = (Button) findViewById(R.id.main_login_button);
        createButton = (Button) findViewById(R.id.main_create_button);
        progressDialog = new ProgressDialog(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        loginButton.setOnClickListener(this);
        createButton.setOnClickListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (alertation != null){
            alertation.dismiss();
        }

        progressDialog.dismiss();

        loginButton.setOnClickListener(null);
        createButton.setOnClickListener(null);
    }

    // Twitter call back handler for getting the Twitter session from the call back manager ========
    private void handleTwitterSession(TwitterSession session) {
        progressDialog.setButton(ProgressDialog.BUTTON_POSITIVE, "Continue",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        progressDialog.dismiss();

                    }

                });

        progressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {

                finish();

            }

        });

        progressDialog.show();
        progressDialog.getButton(ProgressDialog.BUTTON_POSITIVE).setVisibility(View.INVISIBLE);

        AuthCredential credential = TwitterAuthProvider.getCredential(
                session.getAuthToken().token,
                session.getAuthToken().secret);

        mAuth.signInWithCredential(credential).addOnCompleteListener(this,
                new OnCompleteListener<AuthResult>() {

                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {

                            Log.e(TAG, "signInWithCredential", task.getException());
                            progressDialog.dismiss();
                            alertUserWithTitleAndMessage("Authentication Failed!",
                                    "Twitter authentication has failed with your Twitter account!" +
                                            "Please retry or use a different method of authentication");
                        }else{
                            mFuser = task.getResult().getUser();
                            String displayName = mFuser.getDisplayName();

                            user = new User();
                            user.userId = mFuser.getUid();
                            user.displayName = displayName;
                            user.email = mFuser.getEmail();
                            //user.provider = mFuser.getProviderId();

                            mFDB.getReference().child("users").child(mFuser.getUid())
                                    .addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.getValue() == null){
                                                // New User
                                                mFDB.getReference().child("users")
                                                        .child(mFuser.getUid()).setValue(user);
                                                mFDB.getReference().child("users")
                                                        .child(mFuser.getUid())
                                                        .removeEventListener(this);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {
                                            Log.e(TAG, "onCancelled: " + databaseError.getMessage());
                                        }
                                    });

                            progressDialog.setMessage(displayName + " had successfully logged in using your " +
                                    "Twitter account!");
                            progressDialog.getButton(ProgressDialog.BUTTON_POSITIVE)
                                    .setVisibility(View.VISIBLE);
                        }
                    }

                });
    }
    // =============================================================================================


    // Google call back handler for getting the Google account from the call back manager ==========
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        progressDialog.setButton(ProgressDialog.BUTTON_POSITIVE, "Continue",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        progressDialog.dismiss();
                    }

                });

        progressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                finish();
            }

        });

        progressDialog.show();
        progressDialog.getButton(ProgressDialog.BUTTON_POSITIVE).setVisibility(View.INVISIBLE);

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);

        mAuth.signInWithCredential(credential).addOnCompleteListener(this,
                new OnCompleteListener<AuthResult>() {

                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            progressDialog.dismiss();

                            alertUserWithTitleAndMessage("Authentication Failed!",
                                    "Google authentication has failed with your Google account!" +
                                    "Please retry or use a different method of authentication");
                        }else{
                            mFuser = task.getResult().getUser();
                            String displayName = mFuser.getDisplayName();

                            user = new User();
                            user.userId = mFuser.getUid();
                            user.displayName = displayName;
                            user.email = mFuser.getEmail();
                            //user.provider = mFuser.getProviderId();

                            // Checking if the user not exists in databased then ad user object to
                            // database...
                            mFDB.getReference().child("users").child(mFuser.getUid())
                                    .addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.getValue() == null){
                                                // New User
                                                mFDB.getReference().child("users")
                                                        .child(mFuser.getUid()).setValue(user);
                                                mFDB.getReference().child("users")
                                                        .child(mFuser.getUid())
                                                        .removeEventListener(this);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {
                                            Log.e(TAG, "onCancelled: " + databaseError.getMessage());
                                        }
                            });

                            progressDialog.setMessage(displayName +
                                    " has successfully logged in using a " + "Google account!");
                            progressDialog.getButton(ProgressDialog.BUTTON_POSITIVE)
                                    .setVisibility(View.VISIBLE);
                        }
                    }
                });
    }
    // =============================================================================================


    // Facebook call back handler for getting the access token from the call back manager ==========
    private void handleFacebookAccessToken(AccessToken token) {
        progressDialog.setButton(ProgressDialog.BUTTON_POSITIVE, "Continue",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        progressDialog.dismiss();
                    }

                });

        progressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                finish();
            }
        });

        progressDialog.show();
        progressDialog.getButton(ProgressDialog.BUTTON_POSITIVE).setVisibility(View.INVISIBLE);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());

        mAuth.signInWithCredential(credential).addOnCompleteListener(this,
                new OnCompleteListener<AuthResult>() {

                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            Log.e(TAG, "signInWithCredential", task.getException());
                            progressDialog.dismiss();

                            alertation = alertUserWithTitleAndMessage("Authentication Failed!",
                                    "Facebook authentication has failed with your Facebook account!" +
                                            "Please retry or use a different method of authentication");
                        }else{
                            mFuser = task.getResult().getUser();
                            String displayName = mFuser.getDisplayName();

                            user = new User();
                            user.userId = mFuser.getUid();
                            user.displayName = displayName;
                            user.email = mFuser.getEmail();
                            user.provider = mFuser.getProviderId();

                            mFDB.getReference().child("users").child(mFuser.getUid())
                                    .addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.getValue() == null){
                                                // New User
                                                mFDB.getReference().child("users")
                                                        .child(mFuser.getUid()).setValue(user);
                                                mFDB.getReference().child("users")
                                                        .child(mFuser.getUid())
                                                        .removeEventListener(this);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {
                                            Log.e(TAG, "onCancelled: " + databaseError.getMessage());
                                        }
                                    });

                            progressDialog.setMessage(displayName + " had successfully logged in using your " +
                                    "Facebook account!");
                            progressDialog.getButton(ProgressDialog.BUTTON_POSITIVE)
                                    .setVisibility(View.VISIBLE);
                        }
                    }

                });
    }
    // =============================================================================================

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Log.e(TAG, "onActivityResult: " + requestCode + resultCode + data.toString() );
        // Redirecting the results to Facebook's callback manager.
        mCallbackManager.onActivityResult(requestCode, resultCode, data);

        // Make sure that the twitterLoginButton hears the result from any
        // Activity that it triggered.
        twitterLoginButton.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);

            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            }
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {}

    // From Past
    // Login and Create accounts Buttons Click listener ============================================
    @Override
    public void onClick(View v) {
        int buttonId = v.getId();

        if (buttonId == R.id.main_login_button){
            Log.e(TAG, "onClick: Login Button has being activated...");
            showAccountDialog(SetupDialog.DIALOG_MODE_LOGIN);
        }

        if (buttonId == R.id.main_create_button){
            Log.e(TAG, "onClick: Create Button has being activated...");
            showAccountDialog(SetupDialog.DIALOG_MODE_CREATE);
        }
    }
    // =============================================================================================

    // SetupDialog Interface Methods
    @Override
    public void onPositiveButton(long dialogMode) {
        this.dialogMode = dialogMode;
        progressDialog.show();
        if(dialogMode == SetupDialog.DIALOG_MODE_CREATE) {
            // Create account with Email and Password
            mAuth.createUserWithEmailAndPassword(user.email,
                    user.password).addOnCompleteListener(this);
        }

        if (dialogMode == SetupDialog.DIALOG_MODE_LOGIN){
            // Logging in with Email And Password
            mAuth.signInWithEmailAndPassword(user.email,
                    user.password).addOnCompleteListener(this);
        }
    }

    @Override
    public void onCancelButton() {
        // Saved for extra features
    }

    @Override
    public void getCredentialsValues(@Nullable String username,
                                     @Nullable String email,
                                     String password) {

        user = new User(username, email, password);
    }

    @Override
    public void showAlertWithLocalErrorFormat(String title, String message) {
        alertUserWithTitleAndMessage(title, message);
    }

    //Complete Listener for signing in or creating account
    @Override
    public void onComplete(@NonNull Task<AuthResult> task) {
        if(dialogMode == SetupDialog.DIALOG_MODE_CREATE) {
            createAccountAction(task);
        }

        if (dialogMode == SetupDialog.DIALOG_MODE_LOGIN){
            loginAccountAction(task);
        }

        progressDialog.dismiss();
    }

    private AlertDialog alertUserWithTitleAndMessage(String title, String message) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder( this );
        alertBuilder.setTitle(title);
        alertBuilder.setIcon(R.drawable.ic_error_outline);
        alertBuilder.setMessage(message);
        alertBuilder.setPositiveButton("Ok", null);

        AlertDialog alertDialog = alertBuilder.create();
        alertDialog.show();

        return alertDialog;
    }

    private void showAccountDialog(long mode) {
        setUpDialogFragment = SetupDialog.newInstanceOf(mode);
        setUpDialogFragment.show(getSupportFragmentManager(), SetupDialog.TAG);
    }

    private void createAccountAction(Task<AuthResult> task){
        if (!task.isSuccessful()) {
            //noinspection ConstantConditions,ThrowableResultOfMethodCallIgnored
            alertUserWithTitleAndMessage("Creating account Error",
                    task.getException().getLocalizedMessage() );
        }else {
            UserProfileChangeRequest userProfileChangeRequest = new UserProfileChangeRequest.Builder()
                    .setDisplayName(user.displayName)
                    .build();

            mFuser = mAuth.getCurrentUser();

            //noinspection ConstantConditions
            mFuser.updateProfile(userProfileChangeRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    if (!task.isSuccessful()){

                        Log.e(TAG, "onComplete: updateProfile: Profile Not updated...");
                    }else{
                        user.userId = mFuser.getUid();

                        Log.e(TAG, "\n" + "createAccountAction: FIREBASE DISPLAY NAME :---> " + mFuser.getDisplayName() + "\n");
                        Log.e(TAG, "createUserWithEmailAndPassword:onComplete: With ID ---> "
                                + user.userId + "\n"+"And Email ---> "
                                + user.email + "\n"+ "And password --->"
                                + user.password);

                        mFDB.getReference().child("users").child(user.userId).setValue(user);

                        Intent intent = new Intent();
                        intent.putExtra("CURRENT_USER_PROFILE", user);
                        setResult(RESULT_OK, intent);

                        finish();
                    }
                }

            });
        }
    }

    private void loginAccountAction(Task<AuthResult> task){
        if (!task.isSuccessful()) {
            //noinspection ConstantConditions,ThrowableResultOfMethodCallIgnored
            alertUserWithTitleAndMessage("Login Error",
                    task.getException().getLocalizedMessage() );

            Log.e(TAG, "signInWithEmailAndPassword: EXCEPTION --> ",
                    task.getException());
        }else {
            setUpDialogFragment.dismiss();

            mFuser = mAuth.getCurrentUser();

            //noinspection ConstantConditions
            user.userId = mFuser.getUid();
            user.email = mFuser.getEmail();
            //user.provider = mFuser.getProviderData().toString();
            user.displayName = mFuser.getDisplayName();

            Log.e(TAG, "signInWithEmailAndPassword:onComplete: \n" + "With ID :---> "
                    + user.userId + "\n" +"And Email :---> "
                    + user.email);

            finish();
        }
    }
}
