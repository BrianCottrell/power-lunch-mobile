package com.example.android.bluetoothlegatt;

import java.util.HashMap;

/**
 * This class includes a small subset of standard GATT attributes for demonstration purposes.
 */
public class SampleGattAttributes {
    private static HashMap<String, String> attributes = new HashMap();
    public static String LUNCHBOX_SERVICE = "19b10000-e8f2-537e-4f6c-d104768a1214";
    public static String DEVICE_ON_OFF = "19b10001-e8f2-537e-4f6c-d104768a1214";
    public static String TEMPERATURE_SENSOR_1 = "19b10000-e8f2-537e-4f6c-d104768a1217";
    public static String TEMPERATURE_SENSOR_2 = "19b10000-e8f2-537e-4f6c-d104768a1218";

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
}
