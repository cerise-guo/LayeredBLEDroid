package com.peripheral.data;

import android.content.Context;
import android.util.Log;

import com.peripheral.ble.BLEDeviceInfo;
import com.peripheral.ble.BLEScanResult;
import com.peripheral.ble.DeviceManager;
import com.peripheral.ble.DeviceManagerImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataManagerImpl implements DataManager {
    final static String TAG_NAME = "DataManager";

    static private DataManager instance;
    //private static BLEDeviceManager deviceManager;
    private static DeviceManager deviceManager;
    private final BLEScanResult scanResult;
    private String targetDeviceName;
    boolean initialized = false;

    Map<String, BLEDeviceInfo> deviceList = new HashMap<String, BLEDeviceInfo>();
    public static DataManager getInstance(){

        if( null == instance ){
            instance = new DataManagerImpl();
        }

        return instance;
    }

    private DataManagerImpl(){
        scanResult = new BLEScanResult() {
            @Override
            public void onScanResult(BLEDeviceInfo deviceInfo) {
                saveScanResult( deviceInfo );

            }
        };
    }

    public void saveScanResult( BLEDeviceInfo deviceInfo ){
        if( deviceList.containsKey( deviceInfo.macAddress)){
            //Log.d(TAG_NAME, "duplicated device : " + deviceInfo.name );
        }
        else{
            Log.d(TAG_NAME, "add device to list: " + deviceInfo.name + " , " + deviceInfo.macAddress );
            deviceList.put( deviceInfo.macAddress, deviceInfo );
        }
    }

    public List<String> deviceList(){
        List<String> savedDeviceList = new ArrayList<String>();

        for( Map.Entry<String,BLEDeviceInfo> entry : deviceList.entrySet() ){
            savedDeviceList.add( entry.getValue().name + ": " + entry.getValue().macAddress);
        }

        return savedDeviceList;
    }

    public String foundDevice(String deviceName){

        for( Map.Entry<String,BLEDeviceInfo> entry : deviceList.entrySet() ){

            if( null != entry.getValue().name ){
                if( entry.getValue().name.equals(deviceName)){
                    return entry.getValue().macAddress;
                }
            }
        }

        return null;
    }


    public void updateDeviceInfo( String targetDeviceName ){
        if( null != deviceManager ){
            deviceManager.startConnect( targetDeviceName );
        }
        this.targetDeviceName = targetDeviceName;
    }

    public boolean initiate( Context context ){

        if (null == deviceManager) {
            DeviceManagerImpl.initiate(context, this.scanResult);
            deviceManager = DeviceManagerImpl.getInstance();
        }
        initialized = true;
        return true;
    }

    public boolean isInitialized(){
        return initialized;
    }

    /*
    public boolean isDeviceRead(){
        if( null != deviceManager ){
            return deviceManager.isReadyToUse();
        }

        return false;
    }*/
}
