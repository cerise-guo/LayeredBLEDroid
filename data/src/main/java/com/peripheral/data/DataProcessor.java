package com.peripheral.data;

public interface DataProcessor {

    byte[] encode( byte value );

    byte decode( byte value[] );
}
