package com.peripheral.ble;

public class BLEDeviceInfo {

    final public String macAddress;
    final public String name;

    public BLEDeviceInfo( String address,
                          String name){

        this.macAddress = address;
        this.name = name;
    }
}
