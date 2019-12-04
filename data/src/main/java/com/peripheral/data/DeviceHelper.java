package com.peripheral.data;

import com.peripheral.ble.DeviceManager;
import com.peripheral.ble.DeviceManagerImpl;

public class DeviceHelper {

    private static DeviceManager getDeviceManager(){

        DeviceManager deviceManager = DeviceManagerImpl.getInstance();
        return deviceManager;
    }

    //ToDo: singleton or inject
    final static private DataHelper dataHelper = new DataHelper();

    private static class LEDServiceSingleton{
        private static final LEDService ledService = new LEDService( getDeviceManager(), dataHelper );
    }
    public static LEDService getLEDService(){
        return LEDServiceSingleton.ledService;
    }

}
