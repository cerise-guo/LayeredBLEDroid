package com.peripheral.data;

public interface LEDStatusListener {

    void onLEDStatus(boolean isOn);

    void onLEDChange(boolean success);
}
