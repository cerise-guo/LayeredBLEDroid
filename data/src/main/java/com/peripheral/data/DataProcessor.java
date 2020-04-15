package com.peripheral.data;

//Note: this interface is NOT public and only accessible by data layer.
interface DataProcessor {

    byte[] encode( byte value );

    byte decode( byte value[] );
}
