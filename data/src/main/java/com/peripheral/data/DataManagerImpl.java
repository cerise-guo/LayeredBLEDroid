package com.peripheral.data;

import android.content.Context;

import com.peripheral.ble.BLEDeviceInfo;
import com.peripheral.ble.DeviceFound;
import com.peripheral.ble.DeviceManager;
import com.peripheral.ble.DeviceManagerImpl;

import com.peripheral.logger.SimpleLogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataManagerImpl implements DataManager {
    final static String TAG_NAME = "DataManager";

    static private DataManager instance;
    //private static BLEDeviceManager deviceManager;
    private static DeviceManager deviceManager;
    private final DeviceFound deviceFound;
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
        deviceFound = new DeviceFound() {
            @Override
            public void onDeviceFound(BLEDeviceInfo deviceInfo) {
                saveDevice( deviceInfo );

            }
        };
    }

    public void saveDevice( BLEDeviceInfo deviceInfo ){
        if( deviceList.containsKey( deviceInfo.macAddress)){
            //Log.d(TAG_NAME, "duplicated device : " + deviceInfo.name );
        }
        else{
            SimpleLogger.getInstance().log(TAG_NAME, "add device to list: " + deviceInfo.name + " , " + deviceInfo.macAddress );
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
        SimpleLogger.getInstance().log(TAG_NAME, "start to get device info for : " + targetDeviceName );
        if( null != deviceManager ){
            deviceManager.startConnect( targetDeviceName );
        }
        this.targetDeviceName = targetDeviceName;
    }

    public boolean initiate( Context context ){

        if (null == deviceManager) {
            DeviceManagerImpl.initiate(context, this.deviceFound);
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
