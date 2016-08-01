package me.gchriswill.pinner.home;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import java.util.regex.Pattern;

import me.gchriswill.pinner.R;

//import me.gchriswill.apd2project.R;

/**
 * Created by gchriswill on 5/23/16.
 */
public class SearchDialogFragment extends DialogFragment {

    public static final String TAG = "SearchDialogFragment";

    TextInputEditText usField;
    SearchDialogFragmentListener searchDialogFragmentListener;

    public static SearchDialogFragment newInstanceOf(){
        return new SearchDialogFragment();
    }

    public interface SearchDialogFragmentListener{
        void onCancelButtonClicked();
        void onPositiveButtonClicked(String username);
        void showAlertWithLocalErrorFormat(String title, String message);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof SearchDialogFragmentListener)
            searchDialogFragmentListener = (SearchDialogFragmentListener) context;
        else throw new IllegalStateException(TAG + ": onAttach: ATTENTION!!!! :---> \n" +
                "SearchDialogFragmentListener is not being implemented!!!!" );
    }

    @Override
    public void onDetach() {
        super.onDetach();
        searchDialogFragmentListener = null;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder( getActivity() );
        LayoutInflater inflater = getActivity().getLayoutInflater();

        @SuppressLint("InflateParams")
        View view = inflater.inflate(R.layout.search_dialog, null);

        alertBuilder.setView(view);

        usField = (TextInputEditText) view.findViewById(R.id.search_dialog_username_field);

        alertBuilder.setPositiveButton("Search", null);
        alertBuilder.setNegativeButton("Cancel", null);

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

        Button negativeButton = ( (AlertDialog) getDialog() ).getButton(Dialog.BUTTON_NEGATIVE);
        negativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, "onClick: Cancel Button Clicked...");
                searchDialogFragmentListener.onCancelButtonClicked();
            }
        });
    }

    private void performPositiveAction() {
        String emailValue = String.valueOf(usField.getText());
        if (Pattern.matches("^[\\w.]{2,}@[\\w.]{2,}\\.[A-Za-z]{2,6}$", emailValue))
            searchDialogFragmentListener.onPositiveButtonClicked(emailValue);
        else {
            Log.e(TAG, "performPositiveAction: Username --> Pattern Doesn't matches");
            searchDialogFragmentListener.showAlertWithLocalErrorFormat("Invalid Email",
                    "PLease enter a valid email...");
        }
    }
}
