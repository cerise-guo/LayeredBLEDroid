package com.peripheral.data;

import android.content.Context;

public interface DataManager {

    boolean isInitialized();

    boolean initiate( Context context );

    void updateDeviceInfo( String targetDeviceName );
}
