package com.peripheral.data;

import com.peripheral.ble.DeviceManager;

import java.util.UUID;

public class LEDService {

    final String TAG_NAME = "LEDService";

    //private final BluetoothGatt mGatt;

    final private LEDCharacteristic ledCharactertistic;

    final String LED_SERVICE_UUID = "00001567-1212-efde-1523-785feabcd123";

    public LEDService(
            DeviceManager deviceManager,
            DataHelper dataHelper){

        if( null == deviceManager ){
            throw new NullPointerException("Null DeviceManager in LEDService ctor");
        }

        /*ledCharactertistic = new LEDCharacteristic(
                deviceManager,
                UUID.fromString(LED_SERVICE_UUID),
                dataHelper.getProcessor(LEDCharacteristic.class));*/

        ledCharactertistic = new LEDCharacteristic(
                deviceManager, UUID.fromString(LED_SERVICE_UUID), dataHelper.getProcessor(LEDCharacteristic.class));
    }

    public LEDCharacteristic getLEDCharater(){
        return ledCharactertistic;
    }
}
