package me.gchriswill.pinner;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.regex.Pattern;

/**
 * Created by gchriswill on 7/18/16.
 */
public class SetupDialog extends DialogFragment {

    public static final String TAG = "AccountDialogFragment";

    public static final String MODE_BUNDLE_KEY = "MODE_BUNDLE_KEY";
    public static final long DIALOG_MODE_CREATE = 0x0001001L;
    public static final long DIALOG_MODE_LOGIN = 0x0002002L;

    AccountDialogListener accountDialogListener;

    private TextInputEditText usField;
    private TextInputEditText eField;
    private TextInputEditText pwdField;

    private long dialogMode;

    public static SetupDialog newInstanceOf(long config){

        SetupDialog accountDialogFragment = new SetupDialog();

        Bundle bundle = new Bundle();
        bundle.putLong(MODE_BUNDLE_KEY, config);

        accountDialogFragment.setArguments(bundle);

        return accountDialogFragment;

    }

    public interface AccountDialogListener {

        void onPositiveButton(long dialogMode);
        void onCancelButton();
        void getCredentialsValues(@Nullable String username, @Nullable String email, String password);
        void showAlertWithLocalErrorFormat(String title, String message);

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof AccountDialogListener){

            accountDialogListener = (AccountDialogListener) context;

        }else throw new ClassCastException( "WARNING! ---> Dialog listener not implemented...");

    }

    @Override
    public void onDetach() {
        super.onDetach();

        accountDialogListener = null;

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Bundle bundle = getArguments();
        dialogMode = bundle.getLong(MODE_BUNDLE_KEY);

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder( getActivity() );
        LayoutInflater inflater = getActivity().getLayoutInflater();

        @SuppressLint("InflateParams")
        final View view = inflater.inflate(R.layout.account_dialog, null);

        alertBuilder.setView(view);

        TextView dTitle = (TextView) view.findViewById(R.id.main_dialog_title);
        usField = (TextInputEditText) view.findViewById(R.id.main_dialog_username_field);
        eField = (TextInputEditText) view.findViewById(R.id.main_dialog_email_field);
        pwdField = (TextInputEditText) view.findViewById(R.id.main_dialog_password_field);
        String bTitle;

        // Login mode
//        if(dialogMode == DIALOG_MODE_LOGIN){
//            bTitle = "Login";
//
//        }

        // Create mode
        if (dialogMode != DIALOG_MODE_CREATE){

            bTitle = "Login";
            usField.setVisibility(View.GONE);
            usField.setEnabled(false);

        }else {

            bTitle = "Create";

        }

        dTitle.setText(bTitle);

        alertBuilder.setPositiveButton(bTitle, null);

        alertBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int id) {

                Log.e(TAG, "onClick: Cancel Button Clicked...");
                accountDialogListener.onCancelButton();

            }

        });

        // --- Special Feature ---
        // Setting a listener for the Enter button on the keyboard that performs the same action as
        // the Positive Button...
        view.findViewById(R.id.main_dialog_password_field).setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER) ) {

                    Log.e(TAG, "onClick: Enter Key Pressed...");

                    performPositiveAction();

                    return true;

                }

                return false;

            }

        });

        return alertBuilder.create();

    }

    @Override
    public void onStart() {
        super.onStart();

        Button positiveButton = ( (AlertDialog) getDialog() ).getButton(Dialog.BUTTON_POSITIVE);

        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.e(TAG, "onClick: Positive Button Clicked...");

                performPositiveAction();

            }

        });

    }

    // --- Special Features Inside: Client-side Validation ---
    private void performPositiveAction() {

        Bundle bundle = getArguments();
        dialogMode = bundle.getLong(MODE_BUNDLE_KEY);

        boolean regexEmailMatcher;
        boolean regexPwdMatcher;
        boolean regexUserNameMatcher = true;

        String userNameValue = String.valueOf(usField.getText());
        String emailValue = String.valueOf(eField.getText().toString());
        String pwdValue = String.valueOf(pwdField.getText());

        // Checker for Validation in Create Mode
        if (dialogMode == DIALOG_MODE_CREATE) {

            // --- Username Validation ---
            // Username must contains alphabetic characters only
            // Username must be 4 to 12 characters long

            regexUserNameMatcher = Pattern.matches("^[A-Za-z].{3,12}$", userNameValue);

        }

        // --- Email Validation ---
        //Email must have a length of 2 characters or more before the @
        //Email must have a length of 2 characters or more before the period
        //Email must have a length of 2 to 6 characters or more after the period
        //Email must contain an @ symbol
        //EMail must contain a period

        regexEmailMatcher = Pattern.matches("^[\\w.]{2,}@[\\w.]{2,}\\.[A-Za-z]{2,6}$",
                emailValue);

        // --- Password Validation ---r
        // PWD must contains two uppercase letters.
        // PWD must contains one special case letter.
        // PWD must contains two digits.
        // PWD must contains three lowercase letters.
        // PWD must contains a length 8.

        regexPwdMatcher = Pattern.matches("^(?=.*[A-Z].*[A-Z])(?=.*[!@#$&*])(?=.*[0-9].*[0-9])(?=.*[a-z].*[a-z].*[a-z]).{8,}$",
                pwdValue);

        //accountDialogListener.getCredentialsValues(userNameValue, emailValue, pwdValue);

        if (dialogMode == DIALOG_MODE_LOGIN) {

            if (regexEmailMatcher && regexPwdMatcher) {

                Log.e(TAG, "performPositiveAction: Login values --> Pattern Matches");

                accountDialogListener.getCredentialsValues(null, emailValue, pwdValue);
                accountDialogListener.onPositiveButton(dialogMode);

            }

        } else if (dialogMode == DIALOG_MODE_CREATE) {

            if (regexEmailMatcher && regexPwdMatcher && regexUserNameMatcher) {

                Log.e(TAG, "performPositiveAction: Create Account Values --> Pattern Matches");

                accountDialogListener.getCredentialsValues(userNameValue, emailValue, pwdValue);
                accountDialogListener.onPositiveButton(dialogMode);

            }

        }

        // --- Validation Failed alerts ---
        // 1- Username Alert Validation Error
        if (!regexUserNameMatcher && dialogMode == DIALOG_MODE_CREATE) {

            Log.e(TAG, "performPositiveAction: Username --> Pattern Doesn't matches");

            accountDialogListener.showAlertWithLocalErrorFormat("Invalid Username Format",
                    "Please, enter a username that has a length with a minimum of 4 " +
                            "characters and a maximum of 12 characters. And contains only " +
                            "alphabetical characters only..." );

        } /* 2- Email Alert Validation Error*/ else if (!regexEmailMatcher) {

                //&& dialogMode != DIALOG_MODE_DELETE) {

            Log.e(TAG, "performPositiveAction: Email --> Pattern Doesn't matches");

            accountDialogListener.showAlertWithLocalErrorFormat("Invalid Email Format",
                    "Please, enter a valid email address..." );

        } /* 3- Password Alert Validation Error*/ else if (!regexPwdMatcher) {

            Log.e(TAG, "performPositiveAction: PWD --> Pattern Doesn't matches");

            accountDialogListener.showAlertWithLocalErrorFormat("Invalid Password Format",
                    "Please, enter a password that contains three lowercase letters, two " +
                            "uppercase letters, two digits, one special character, and has a " +
                            "length of at least 8 characters..." );

        }

    }

}
