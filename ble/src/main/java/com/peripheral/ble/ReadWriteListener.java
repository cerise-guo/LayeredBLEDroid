package com.peripheral.ble;

import android.support.annotation.NonNull;

public interface ReadWriteListener {
    void onRead( boolean success, @NonNull final byte[] value );

    void onWrite( boolean success );
}
