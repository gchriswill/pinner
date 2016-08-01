package me.gchriswill.pinner;

import java.io.Serializable;

/**
 * Created by gchriswill on 7/27/16.
 */
public class UserLocation implements Serializable {

    public Double latitude;
    public Double longitude;
    public String address;

    public UserLocation() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public UserLocation(Double latitude, Double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
