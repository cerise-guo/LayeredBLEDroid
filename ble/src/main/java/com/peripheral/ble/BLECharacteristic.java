package com.peripheral.ble;

import android.support.annotation.NonNull;
import android.util.Log;

import java.util.UUID;

public class BLECharacteristic {

    final String TAG_NAME = "LEDCharactertistic";

    final private DeviceManager deviceManager;
    final private UUID serviceUUID;
    final private UUID charUUID;
    private ReadWriteListener listener;


    public BLECharacteristic(
            DeviceManager deviceManager,
            UUID serviceUUID,
            UUID characteristicUUID){
        this.deviceManager = deviceManager;
        this.serviceUUID = serviceUUID;
        this.charUUID = characteristicUUID;
    }

    private BLECharacteristic(){
        //should not be called.
        deviceManager = null;
        serviceUUID = null;
        listener = null;
        charUUID = null;
    }

    public void setListener( ReadWriteListener listener ){
        this.listener = listener;
    }

    public boolean write( byte[] value ){
        Log.d(TAG_NAME, "Turn off LED");

        deviceManager.write( serviceUUID, charUUID, value, this.listener);

        return true;
    }

    public boolean read(){
        deviceManager.read(serviceUUID, charUUID, this.listener);

        return true;
    }
}
