package com.peripheral.ble;

import java.util.UUID;

public interface DeviceManager {

    //boolean initiate(Context context);

    void write(UUID serviceUUID, UUID characteristicUUID, byte[] value, ReadWriteListener callback );

    void read(UUID serviceUUID, UUID characteristicUUID, ReadWriteListener callback );

    //boolean isConnected();

    boolean isReadyToUse();

    void startConnect( String targetDeviceName ); //ToDo: needs return value to return immediate result
}
