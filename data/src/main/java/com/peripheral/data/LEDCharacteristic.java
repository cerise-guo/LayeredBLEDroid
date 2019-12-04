package com.peripheral.data;

import android.support.annotation.NonNull;
import android.util.Log;

import com.peripheral.ble.BLECharacteristic;
import com.peripheral.ble.DeviceManager;
import com.peripheral.ble.ReadWriteListener;

import java.util.UUID;

public class LEDCharacteristic{ //extends BLECharacteristic {

    final String TAG_NAME = "LEDCharacteristic";

    final private static UUID LED_CHAR_UUID = UUID.fromString("00001525-1212-efde-1523-785feabcd123");
    final private DataProcessor dataProcessor;

    //This listener is to return LED status to caller layer, e.g. business logic and/or app layer.
    private LEDStatusListener ledStatusListener;


    BLECharacteristic bleCharacteristic;
    public LEDCharacteristic(
            DeviceManager deviceManager,
            UUID serviceUUID,
            DataProcessor processor
    ){
        this.bleCharacteristic = DataHelper.buildCharacteristic(
                deviceManager, serviceUUID, LED_CHAR_UUID );
        this.dataProcessor = processor;
        this.bleCharacteristic.setListener( new LEDListener() );

    }

    /*public LEDCharacteristic(
            DeviceManager deviceManager,
            UUID serviceUUID,
            DataProcessor processor){
        super(deviceManager, serviceUUID, LED_CHAR_UUID);

        //this listener is to receive low level BLE result from BLE layer.
        this.setListener( new LEDListener() );

        this.dataProcessor = processor;
    }*/

    public void writeLEDValue( byte value ){
        bleCharacteristic.write( dataProcessor.encode(value));
    }

    public void readLEDValue(){
        bleCharacteristic.read();
    }

    public void setListener( LEDStatusListener listener ){
        if(null != this.ledStatusListener){
            throw new RuntimeException( "shouldn't set listener twice for LEDCharacteristic");
        }
        this.ledStatusListener = listener;
    }

    private class LEDListener implements ReadWriteListener {
        @Override
        public void onRead(boolean success, @NonNull byte[] value) {

            byte val = (null == value)?0:value[0];
            Log.d(TAG_NAME, "onRead : " + success + " : " + val );

            if( null != ledStatusListener ) {
                //ToDo: will this block further call ?
                ledStatusListener.onLEDStatus(0 != dataProcessor.decode(value));
            }
        }

        @Override
        public void onWrite(boolean success) {
            Log.d(TAG_NAME, "onWrite : " + success );
        }
    }
}
