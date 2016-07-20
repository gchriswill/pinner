package me.gchriswill.pinner;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by gchriswill on 7/18/16.
 */
public class User implements Serializable {

    public String username;
    public String email;
    public String password;
    public String userId;
    public String imageUrl;

    public String phone;
    public String address;
    public boolean location;

    public boolean isSahringPhone;
    public boolean isSahringAddress;
    public boolean isSharingLocation;

    public Map<String, Object> profiles = new HashMap<>();

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }

}
