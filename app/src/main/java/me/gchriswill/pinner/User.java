package me.gchriswill.pinner;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by gchriswill on 7/18/16.
 */
public class User implements Serializable {

    public String displayName;
    public String email;
    public String password;
    public String userId;
    public String imageUrl;
    public String provider;

    public String phone;
    public String text;

    public boolean isSharingPhone = false;
    public boolean isSharingText = false;
    public boolean isSharingAddress = false;
    public boolean isSharingLocation = false;

    public Map<String, Object> location = new HashMap<>();
    public Map<String, Object> friends = new HashMap<>();

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String username, String email, String password) {
        this.displayName = username;
        this.email = email;
        this.password = password;
    }

}
