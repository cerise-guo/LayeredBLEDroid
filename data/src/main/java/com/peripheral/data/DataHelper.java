package com.peripheral.data;


import com.peripheral.ble.BLECharacteristic;
import com.peripheral.ble.DeviceManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DataHelper {

    private static final Map<Class<?>, DataProcessor> processorMap = new HashMap<>();

    static {
        processorMap.put(LEDCharacteristic.class, new LEDProcessor());
    }

    public static DataProcessor getProcessor( final Class<?> charactertistic ){
        final DataProcessor processor = processorMap.get( charactertistic );

        if( null == processor ){
            throw new NullPointerException("can not find processor :" + charactertistic.getSimpleName());
        }

        return processor;
    }

    public static BLECharacteristic buildCharacteristic(
            DeviceManager deviceManager, UUID serviceUUID, UUID characteristicUUID){

        return new BLECharacteristic( deviceManager, serviceUUID, characteristicUUID);
    }
}
