package util.timestamp;

/**
 * Timestamp.java
 * Timestamp utility that returns human readable timestamps suitable for filenames.
 * Created by DED8IRD on 11/10/2016.
 */

import android.text.format.Time;

public class Timestamp {

    // Returns current time in a human readable form.
    public static String now() {
        Time timeNow = new Time();
        timeNow.setToNow();
        String readableTime = timeNow.format("%Y_%m_%d %T");
        return readableTime;
    }

    // Returns readable Timestamp suitable for filenames (i.e. no spaces or special chars).
    public static String timestamp() {
        Time now = new Time();
        now.setToNow();
        String readableTime = now.format("%Y_%m_%d_%H_%M_%S");
        return readableTime;
    }

}
