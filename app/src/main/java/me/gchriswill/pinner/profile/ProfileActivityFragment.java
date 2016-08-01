package me.gchriswill.pinner.profile;

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
import android.widget.ImageView;

import me.gchriswill.pinner.R;
import me.gchriswill.pinner.User;

/**
 * A placeholder fragment containing a simple view.
 */
public class ProfileActivityFragment extends Fragment {

    User userProfile;
    ImageView userImage;
    TextInputEditText username, email, phone, text, address;
    AppCompatCheckBox sharingPhone, sharingText, sharingAddress, sharingLocation;

    FloatingActionButton mailActionButton, phoneActionButton, textActionButton, addressActionButton,
            locationActionButton;

    ProfileActivityFragmentInterface profileActivityFragmentInterface;

    public ProfileActivityFragment() {}

    public interface ProfileActivityFragmentInterface {
        User getUserData();
        Bitmap setUserProfileImage();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ProfileActivityFragmentInterface)
            profileActivityFragmentInterface = (ProfileActivityFragmentInterface) context;
        else throw new IllegalStateException("ProfileActivityFragmentInterface not implemented");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ProfileActivity profileActivity = (ProfileActivity) getActivity();

        mailActionButton = (FloatingActionButton) profileActivity
                .findViewById(R.id.profile_viewer_email_field_action_button);

        phoneActionButton = (FloatingActionButton) profileActivity
                .findViewById(R.id.profile_viewer_phone_field_action_button);

        textActionButton = (FloatingActionButton) profileActivity
                .findViewById(R.id.profile_viewer_text_phone_field_action_button);

        addressActionButton = (FloatingActionButton) profileActivity
                .findViewById(R.id.profile_viewer_address_field_action_button);
        addressActionButton.setOnClickListener(profileActivity);

        locationActionButton = (FloatingActionButton) profileActivity
                .findViewById(R.id.profile_viewer_current_location_field_action_button);
        locationActionButton.setOnClickListener(profileActivity);

        userImage = (ImageView ) profileActivity.findViewById(R.id.profile_viewer_user_image);
        username = (TextInputEditText) profileActivity.findViewById(R.id.profile_viewer_username_field);
        email = (TextInputEditText) profileActivity.findViewById(R.id.profile_viewer_email_field);
        phone = (TextInputEditText) profileActivity.findViewById(R.id.profile_viewer_phone_field);
        text = (TextInputEditText) profileActivity.findViewById(R.id.profile_viewer_text_phone_field);
        address = (TextInputEditText) profileActivity.findViewById(R.id.profile_viewer_address_field);

        sharingPhone = (AppCompatCheckBox) profileActivity.findViewById(R.id.profile_viewer_checkbox_layout_phone);
        sharingText = (AppCompatCheckBox) profileActivity.findViewById(R.id.profile_viewer_checkbox_layout_text);
        sharingAddress = (AppCompatCheckBox) profileActivity.findViewById(R.id.profile_viewer_checkbox_layout_address);
        sharingLocation = (AppCompatCheckBox) profileActivity.findViewById(R.id.profile_viewer_checkbox_layout_location);
    }

    public void getUser(){
        userProfile = profileActivityFragmentInterface.getUserData();
        if(userProfile != null) {
            if (userProfile.displayName == null) username.setVisibility(View.INVISIBLE);
            else {
                username.setVisibility(View.VISIBLE);
                username.setText(userProfile.displayName);
            }

            if (userProfile.email == null) email.setVisibility(View.INVISIBLE);
            else {
                email.setVisibility(View.VISIBLE);
                email.setText(userProfile.email);
            }

            if (userProfile.phone == null) phone.setVisibility(View.INVISIBLE);
            else {
                phone.setVisibility(View.VISIBLE);
                phone.setText(userProfile.phone);
            }

            if (userProfile.text == null) text.setVisibility(View.INVISIBLE);
            else {
                text.setVisibility(View.VISIBLE);
                text.setText(userProfile.text);
            }

            if (userProfile.location.get("address") == null) address.setVisibility(View.INVISIBLE);
            else {
                address.setVisibility(View.VISIBLE);
                address.setText(String.valueOf(userProfile.location.get("address")));
            }

            sharingPhone.setChecked(userProfile.isSharingPhone);
            sharingText.setChecked(userProfile.isSharingText);
            sharingAddress.setChecked(userProfile.isSharingAddress);
            sharingLocation.setChecked(userProfile.isSharingLocation);
        }
    }

    public void getUserProfileImage(){
        Bitmap bitmap = profileActivityFragmentInterface.setUserProfileImage();
        this.userImage.setImageBitmap(bitmap);
    }
}
