package me.gchriswill.pinner.editor;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatCheckBox;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import me.gchriswill.pinner.R;
import me.gchriswill.pinner.User;

/**
 * A placeholder fragment containing a simple view.
 */
public class AccountEditorActivityFragment extends Fragment {

    public static final String TAG = "AccountEditorActivityFragment";

    AccountEditorActivityFragmentInterface accountEditorActivityFragmentInterface;

    public ImageView userImage;
    public User userAccountProfile;
    public FloatingActionButton cameraButton;
    public Button addressButton;
    public TextInputEditText usernameField, emailField, phoneField, textField, addressField;
    private AppCompatCheckBox  sharingPhone, sharingText, sharingAddress, sharingLocation;

    public AccountEditorActivityFragment() {}

    public interface AccountEditorActivityFragmentInterface {
        User getUserValues();
        void setUserValues(User user);

        Bitmap setUserProfileImage();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_account_editor, container, false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof AccountEditorActivityFragmentInterface){
            accountEditorActivityFragmentInterface = (AccountEditorActivityFragmentInterface) context;
        }else {
            throw new IllegalStateException("AccountEditorActivityFragmentInterface not implemented");
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        AccountEditorActivity accountEditorActivity = (AccountEditorActivity) getActivity();

        userImage = (ImageView) accountEditorActivity.findViewById(R.id.account_editor_user_image);
        cameraButton = (FloatingActionButton) accountEditorActivity.findViewById(R.id.account_editor_camera_button);
        usernameField = (TextInputEditText) accountEditorActivity.findViewById(R.id.account_editor_username_field);

        emailField = (TextInputEditText) accountEditorActivity.findViewById(R.id.account_editor_email_field);
        phoneField = (TextInputEditText) accountEditorActivity.findViewById(R.id.account_editor_phone_field);
        textField = (TextInputEditText) accountEditorActivity.findViewById(R.id.account_editor_text_phone_field);
        addressField = (TextInputEditText) accountEditorActivity.findViewById(R.id.account_editor_address_field);
        addressButton = (Button) accountEditorActivity.findViewById(R.id.account_editor_address_picker_button);

        sharingPhone = (AppCompatCheckBox) accountEditorActivity.findViewById(R.id.account_editor_checkbox_layout_phone);
        sharingText = (AppCompatCheckBox) accountEditorActivity.findViewById(R.id.account_editor_checkbox_layout_text);
        sharingAddress = (AppCompatCheckBox) accountEditorActivity.findViewById(R.id.account_editor_checkbox_layout_address);
        sharingLocation = (AppCompatCheckBox) accountEditorActivity.findViewById(R.id.account_editor_checkbox_layout_location);

        sharingPhone.setOnCheckedChangeListener(accountEditorActivity);
        sharingText.setOnCheckedChangeListener(accountEditorActivity);
        sharingAddress.setOnCheckedChangeListener(accountEditorActivity);
        sharingLocation.setOnCheckedChangeListener(accountEditorActivity);

        cameraButton.setOnClickListener(accountEditorActivity);
        addressButton.setOnClickListener(accountEditorActivity);
    }

    @Override
    public void onResume() {
        super.onResume();
        cameraButton.hide();
    }

    public void setFormEnabled(boolean bool){
        phoneField.setEnabled(bool);
        textField.setEnabled(bool);
        addressField.setEnabled(bool);
        addressButton.setEnabled(bool);

        sharingPhone.setEnabled(bool);
        sharingText.setEnabled(bool);
        sharingAddress.setEnabled(bool);
        sharingLocation.setEnabled(bool);

        if (bool) cameraButton.show();
        else cameraButton.hide();
    }

    public void getFormValues(){

        String phone = String.valueOf(phoneField.getText());
        String text = String.valueOf(textField.getText());
        String address = String.valueOf(addressField.getText());

        boolean isSharingPhone = sharingPhone.isChecked();
        boolean isSharingText = sharingText.isChecked();
        boolean isSharingAddress = sharingAddress.isChecked();
        boolean isSharingLocation = sharingLocation.isChecked();

        userAccountProfile = accountEditorActivityFragmentInterface.getUserValues();

        if(!phone.isEmpty())
            userAccountProfile.phone = phone;
        if(!text.isEmpty())
            userAccountProfile.text = text;
        if (!address.isEmpty())
            userAccountProfile.location.put("address", address);

        userAccountProfile.isSharingPhone = isSharingPhone;
        userAccountProfile.isSharingText = isSharingText;
        userAccountProfile.isSharingAddress = isSharingAddress;
        userAccountProfile.isSharingLocation = isSharingLocation;

        accountEditorActivityFragmentInterface.setUserValues(userAccountProfile);
    }

    public void setFormValues(){
        userAccountProfile = accountEditorActivityFragmentInterface.getUserValues();

        boolean checkPhone = userAccountProfile.phone != null;
        boolean checkText = userAccountProfile.text != null;
        boolean checkAddress = userAccountProfile.location.get("address") != null;

        boolean checkLat = userAccountProfile.location.get("latitude") != null;
        boolean checkLon = userAccountProfile.location.get("longitude") != null;
        boolean checkLatLon = checkLat && checkLon;

        usernameField.setText(userAccountProfile.displayName);
        emailField.setText(userAccountProfile.email);

        if (checkPhone)
            phoneField.setText(userAccountProfile.phone);
        if (checkText)
            textField.setText(userAccountProfile.text);
        if (checkAddress)
            addressField.setText( String.valueOf(userAccountProfile.location.get("address") ) );
        else if (checkLatLon){
            String latString = String.valueOf(userAccountProfile.location.get("latitude"));
            String lonString = String.valueOf(userAccountProfile.location.get("longitude"));
            String latLonString = latString + "," + lonString;
            addressField.setText(latLonString);
        }

        sharingPhone.setChecked(userAccountProfile.isSharingPhone);
        sharingText.setChecked(userAccountProfile.isSharingText);
        sharingAddress.setChecked(userAccountProfile.isSharingAddress);
        sharingLocation.setChecked(userAccountProfile.isSharingLocation);
    }

    public void getUserProfileImage(){
        Bitmap bitmap = accountEditorActivityFragmentInterface.setUserProfileImage();
        this.userImage.setImageBitmap(bitmap);
    }
}
