package com.peripheral.bl;


import android.util.Log;

import com.peripheral.data.DeviceHelper;
import com.peripheral.data.LEDCharacteristic;
import com.peripheral.data.LEDStatusListener;

import java.lang.ref.WeakReference;

public class LEDController {

    private final String TAG_NAME = "LEDController";

    private final LEDCharacteristic ledCharactertistic;

    private WeakReference<LEDStatusCallback> ledStatusCallback;

    public LEDController(){
        this.ledCharactertistic = DeviceHelper.getLEDService().getLEDCharater();
        if( null == ledCharactertistic ){
            throw new NullPointerException("Null LEDCharacteristic in LEDController ctor");
        }

        this.ledCharactertistic.setListener(new LEDStatusListener() {
            @Override
            public void onLEDStatus(boolean isOn) {
                Log.d(TAG_NAME, "onLEDStatus : " + isOn );

                final LEDStatusCallback statusCallback = ledStatusCallback.get();
                if( null != statusCallback ){
                    statusCallback.ledStatus( isOn );
                }
            }

            @Override
            public void onLEDChange(boolean success) {

            }
        });

    }

    public void TurnOnLED(){
        ledCharactertistic.writeLEDValue( (byte)1);
    }

    public void TurnOffLED(){
        ledCharactertistic.writeLEDValue((byte)0);
    }

    public void getLEDStatus(LEDStatusCallback callback ){
        this.ledStatusCallback = new WeakReference<>(callback);

        ledCharactertistic.readLEDValue();
    }

    public interface LEDStatusCallback {
        void ledStatus( boolean isOn );
    }

}
