/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.bluetoothlegatt;

import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * For a given BLE device, this Activity provides the user interface to connect, display data,
 * and display GATT services and characteristics supported by the device.  The Activity
 * communicates with {@code BluetoothLeService}, which in turn interacts with the
 * Bluetooth LE API.
 */
public class DeviceControlActivity extends Activity {
    private final static String TAG = DeviceControlActivity.class.getSimpleName();

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private TextView mConnectionState;
    private TextView mDataField;
    private TextView mColdTemperature;
    private TextView mHotTemperature;
    private Button mActivate;
    private ImageView mOverlay;
    private String mDeviceAddress;
    private BluetoothLeService mBluetoothLeService;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<>();
    private boolean mConnected = false;
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    private BluetoothGattCharacteristic mOnOffCharacteristic;
    private boolean mActive = false;

    private int delay = 1000;
    private int lastRead;

    final Handler handler = new Handler();

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                updateConnectionState(R.string.connected);
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                updateConnectionState(R.string.disconnected);
                invalidateOptionsMenu();
                clearUI();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                displayGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            }
        }
    };

    private void clearUI() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gatt_services_characteristics);

        final Intent intent = getIntent();
        String mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        // Sets up UI references.
        mConnectionState = (TextView) findViewById(R.id.connection_state);
        mColdTemperature = (TextView) findViewById(R.id.cold_temperature);
        mHotTemperature = (TextView) findViewById(R.id.hot_temperature);
        mDataField = (TextView) findViewById(R.id.data_value);
        mActivate = (Button) findViewById(R.id.acivate);
        mOverlay = (ImageView) findViewById(R.id.lunchbox_overlay);
        mOverlay.setImageAlpha(0);
        mActivate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBluetoothLeService != null) {
                    if (mOnOffCharacteristic != null) {
                        byte[] value = new byte[1];
                        if (mActive) {
                            value[0] = (byte) (0x00);
                        } else {
                            value[0] = (byte) (0x11);
                        }
                        mOnOffCharacteristic.setValue(value);
                        if (mBluetoothLeService.writeCharacteristic()) {
                            if (mActive) {
                                mActivate.setText(R.string.activate);
                                mDataField.setText(R.string.inactive);
                                mActive = false;
                            } else {
                                mActivate.setText(R.string.deactivate);
                                mDataField.setText(R.string.active);
                                mActive = true;
                            }
                        }
                    }
                }
            }
        });

        if (getActionBar() != null) {
            getActionBar().setTitle(R.string.reconnect);
        }
        getActionBar().setDisplayHomeAsUpEnabled(true);
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gatt_services, menu);
        if (mConnected) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
        } else {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_connect:
                mBluetoothLeService.connect(mDeviceAddress);
                return true;
            case R.id.menu_disconnect:
                mBluetoothLeService.disconnect();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mConnectionState.setText(resourceId);
            }
        });
    }

    private void displayData(String data) {
        if (data != null) {
            int currentRead = Integer.parseInt(data.split("\n")[0]);
            if (currentRead >= 0 && currentRead < 256) {
                int difference = Math.abs(currentRead - lastRead);
                if (lastRead > currentRead) {
                    mOverlay.setImageAlpha(difference);
                    mColdTemperature.setText(Integer.toString(currentRead));
                } else {
                    mHotTemperature.setText(Integer.toString(currentRead));
                }
                lastRead = currentRead;
            }
        }
    }

    // Demonstrates how to iterate through the supported GATT Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the ExpandableListView
    // on the UI.
    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid;
        String unknownServiceString = getResources().getString(R.string.unknown_service);
        String unknownCharaString = getResources().getString(R.string.unknown_characteristic);
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData = new ArrayList<>();
        mGattCharacteristics = new ArrayList<>();

        // Loops through available GATT Services.
        String LIST_NAME = "NAME";
        String LIST_UUID = "UUID";
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<>();
            uuid = gattService.getUuid().toString();
            if (uuid.equals(SampleGattAttributes.LUNCHBOX_SERVICE)) {
                currentServiceData.put(LIST_NAME, SampleGattAttributes.lookup(uuid, unknownServiceString));
                currentServiceData.put(LIST_UUID, uuid);
                gattServiceData.add(currentServiceData);

                ArrayList<HashMap<String, String>> gattCharacteristicGroupData = new ArrayList<>();
                List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
                ArrayList<BluetoothGattCharacteristic> charas = new ArrayList<>();

                // Loops through available Characteristics.
                for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                    charas.add(gattCharacteristic);
                    HashMap<String, String> currentCharaData = new HashMap<>();
                    uuid = gattCharacteristic.getUuid().toString();
                    if (uuid.equals(SampleGattAttributes.TEMPERATURE_SENSOR_1)) {
                        currentCharaData.put(LIST_NAME, SampleGattAttributes.lookup(uuid, unknownCharaString));
                        currentCharaData.put(LIST_UUID, uuid);
                        gattCharacteristicGroupData.add(currentCharaData);
                        updateData(gattCharacteristic);
                    }
                    if (uuid.equals(SampleGattAttributes.TEMPERATURE_SENSOR_2)) {
                        delay = 2000;
                        currentCharaData.put(LIST_NAME, SampleGattAttributes.lookup(uuid, unknownCharaString));
                        currentCharaData.put(LIST_UUID, uuid);
                        gattCharacteristicGroupData.add(currentCharaData);
                        updateData(gattCharacteristic);
                    }
                    if (uuid.equals(SampleGattAttributes.DEVICE_ON_OFF)) {
                        mOnOffCharacteristic = gattCharacteristic;
                    }
                }
                mGattCharacteristics.add(charas);
                gattCharacteristicData.add(gattCharacteristicGroupData);
            }
        }
    }

    private void updateData(final BluetoothGattCharacteristic gattCharacteristic) {
        final int charaProp = gattCharacteristic.getProperties();
        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
            // If there is an active notification on a characteristic, clear
            // it first so it doesn't update the data field on the user interface.
            if (mNotifyCharacteristic != null) {
                mBluetoothLeService.setCharacteristicNotification(mNotifyCharacteristic, false);
                mNotifyCharacteristic = null;
            }
            mBluetoothLeService.readCharacteristic(gattCharacteristic);
        }
        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
            mNotifyCharacteristic = gattCharacteristic;
            mBluetoothLeService.setCharacteristicNotification(gattCharacteristic, true);
        }
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateData(gattCharacteristic);
            }
        }, delay);
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }
}
