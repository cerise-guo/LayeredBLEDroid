package com.peripheral.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Handler;

//import com.peripheral.data.BLEDeviceInfo;
//import com.peripheral.data.DataManager;
import com.peripheral.logger.SimpleLogger;

import java.util.List;
import java.util.UUID;

import static android.bluetooth.BluetoothGatt.GATT_SUCCESS;

public class DeviceManagerImpl extends BluetoothGattCallback implements DeviceManager {

    final static String TAG_NAME = "DeviceManagerImpl";

    final BluetoothAdapter btAdapter;
    private Handler msgHandler;
    private int SCAN_PERIOD = 10000; //millisecond
    private int RESCAN_INTERVAL = 1000; //millisecond

    private boolean isScanning = false;
    private final DeviceFound deviceFound;
    private String targetDeviceName;
    private BluetoothGatt mGatt;
    private final Context context;

    private ReadWriteListener readWritelistener;

    private static DeviceManager deviceManager;

    //Device will be ready to use if it is connected and services are discovered.
    //ToDo: when if additional encryption layer is added, ready is set after encrypted communication
    //is established.
    private boolean isReadyToUse = false;

    enum CONNECTION_STATUS {
        DISCONNECTED,
        CONNECTED,
        CONNECTING,
        DISCONNECTING
    }

    private CONNECTION_STATUS connectionStatus = CONNECTION_STATUS.DISCONNECTED;

    public static boolean initiate(Context context, DeviceFound deviceFound ) {

        //if( null != BLEDeviceManager.mInstance ){
        //    Log.e(TAG_NAME, "device manager has been initialized");
        //    throw new RuntimeException("device manager has been initialized");
        //}

        if( null == context ){
            SimpleLogger.getInstance().log(TAG_NAME, "context should not be null");
            throw new RuntimeException("context should not be null");
        }

        final BluetoothAdapter adapter =
                ((BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();

        if (null == adapter || !adapter.isEnabled()) {
            SimpleLogger.getInstance().log(TAG_NAME, "failed to get bt adapter");
            return false;
        }

        deviceManager = new DeviceManagerImpl(context, adapter, deviceFound);
        return true;
    }

    private DeviceManagerImpl(Context context, BluetoothAdapter adapter, DeviceFound deviceFound) {
        this.btAdapter = adapter;
        this.msgHandler = new Handler();
        this.deviceFound = deviceFound;
        this.context = context;
    }

    public static DeviceManager getInstance(){
        return deviceManager;
    }

    public boolean isConnected(){
        //ToDo:  use mac address during check
        return connectionStatus == CONNECTION_STATUS.CONNECTED;
    }

    public boolean isReadyToUse(){
        return isReadyToUse;
    }

    private boolean connectWithAddress( String macAddress ){

        SimpleLogger.getInstance().log(TAG_NAME, "connectWithAddress : " + macAddress);

        if( connectionStatus != CONNECTION_STATUS.DISCONNECTED ){
            SimpleLogger.getInstance().log(TAG_NAME, "can not start connection: "
                    + connectionStatus);
            return false;
        }

        final BluetoothDevice device = btAdapter.getRemoteDevice(macAddress);

        if (device == null) {
            SimpleLogger.getInstance().log(TAG_NAME, "can not find bluetooth device : " + macAddress);
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mGatt = device.connectGatt(context, false, this);
        connectionStatus = CONNECTION_STATUS.CONNECTING;

        return true;
    }

    //public boolean isScanning() {
    //    return isScanning;
    //}

    //ToDo:add  boolean isDataReady(String deviceName){
    //}

    private boolean terminateScan = false;
    /*public void termianteScan(){
        terminateScan = true;
        //ToDo: there is a tiny window where the scheduled stopScan() is call here. This will
        //cause that the stopScan() is called twice including the following one.
        //Then 'terminateScan' will not work as expectation.
        //ToDo: to fix above issue,
        // A)check 'isScanning' before calling stop scan. (done)
        // B)add mutex to avoid overlapped call on 'stopScan' (ToDo)
        if( isScanning ) {
            stopScan();
        }
    }*/

    private void stopScan(){
        btAdapter.getBluetoothLeScanner().stopScan(this.scanCallback);
        SimpleLogger.getInstance().log(TAG_NAME, "stop scan");
        isScanning = false;

        if( !terminateScan ){
            //After stopping scan, a new scan will be scheduled with 1s delay.
            msgHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    startConnect( targetDeviceName );
                }
            }, RESCAN_INTERVAL);

        }else{
            //If terminateScan is true, no startScan() will be triggered automatically after
            //a stopScan().
            //Reset the flag to allow new scan next time.
            terminateScan = false;
            SimpleLogger.getInstance().log(TAG_NAME, "scan is terminated.");
        }
    }

    public void startConnect( String targetDeviceName ){
        SimpleLogger.getInstance().log(TAG_NAME, "startScan");

        if( isConnected() ){
            SimpleLogger.getInstance().log(TAG_NAME, "won't start scan since connected.");
            return;
        }

        this.targetDeviceName = targetDeviceName;

        //final WeakReference<ConnectManagerImpl> scanCallback = new WeakReference<>(this);

        msgHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if ( isScanning ) {
                    stopScan();
                }
            }
        }, SCAN_PERIOD);

        //1. Save scan result and data
        //2. schedule stop scan and repeat properly
        //3. Test scan after reboot
        //4. Output scan result to another manager for connection

        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                .setNumOfMatches(ScanSettings.MATCH_NUM_ONE_ADVERTISEMENT)
                //.setCallbackType(ScanSettings.CALLBACK_TYPE_FIRST_MATCH) //not supported by Samsung phones
                .build();
        btAdapter.getBluetoothLeScanner().startScan(null, settings, this.scanCallback);
        isScanning = true;
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
        SimpleLogger.getInstance().log(TAG_NAME, "onCharacteristicChanged");
    }

    @Override
    public void onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState) {
        SimpleLogger.getInstance().log(TAG_NAME, "onConnectionStateChange: " +
                status + " , " + newState);

        if( newState == BluetoothProfile.STATE_CONNECTED ){
            connectionStatus = CONNECTION_STATUS.CONNECTED;

        }else if( newState == BluetoothProfile.STATE_DISCONNECTED ){
            isReadyToUse = false;
            connectionStatus = CONNECTION_STATUS.DISCONNECTED;
            resetStatusAfterDisconnect();

        }else if( newState == BluetoothProfile.STATE_CONNECTING ){
            connectionStatus = CONNECTION_STATUS.CONNECTING;

        }else{
            SimpleLogger.getInstance().log(TAG_NAME, "onConnectionStateChange : Unknown");
            connectionStatus = CONNECTION_STATUS.DISCONNECTED;
        }

        if( status == GATT_SUCCESS){
            if( newState == BluetoothProfile.STATE_CONNECTED && !isReadyToUse ){
                SimpleLogger.getInstance().log(TAG_NAME, "connected successfully");

                gatt.discoverServices();
            }
        }

    }

    @Override
    public void onCharacteristicWrite (BluetoothGatt gatt,
                                       BluetoothGattCharacteristic characteristic,
                                       int status){
        SimpleLogger.getInstance().log(TAG_NAME, "onCharacteristicWrite : " + characteristic.getUuid() + " , " + status );
    }

    @Override
    public void onCharacteristicRead (BluetoothGatt gatt,
                                      BluetoothGattCharacteristic characteristic,
                                      int status){
        SimpleLogger.getInstance().log(TAG_NAME, "read char " + characteristic.getUuid() + " value : " + characteristic.getValue().toString());

        if( null != this.readWritelistener ){
            readWritelistener.onRead( status == GATT_SUCCESS , characteristic.getValue());
        }
        else{
            throw new NullPointerException( "readWriteListener is null");
        }
    }

    private void resetStatusAfterDisconnect(){
        //clean service and character list
    }

    @Override
    public void onServicesDiscovered(final BluetoothGatt gatt, final int status) {
        SimpleLogger.getInstance().log(TAG_NAME, "onServicesDiscovered. ");

        isReadyToUse = true;

        List<BluetoothGattService> serviceList = gatt.getServices();
        SimpleLogger.getInstance().log(TAG_NAME, "onServicesDiscovered : " + serviceList.size() );

        for( BluetoothGattService service: serviceList){
            //if( mServices.containsKey( service.getUuid() )){
            //    SimpleLogger.getInstance().log(TAG_NAME,
            //            "!!!!!!!!!!  duplicated service discovered why?");
            //}else{
            //mServices.put( service.getUuid(), service);

            //adaptCharacteristics( service );
            //}
        }

        //uiMessageHandler.updateUIMessage("Device is ready");
    }

/*
    private void adaptCharacteristics( BluetoothGattService service ){
        Log.d( TAG_NAME, "service UUID : " + service.getUuid());

        //put characteristic in proxy class for BL module
        //Iterator<Map.Entry<UUID, BluetoothGattService>> iterator = services.entrySet().iterator();
        //while (iterator.hasNext()) {
        //    Map.Entry<UUID, BluetoothGattService> entry = iterator.next();
        //    Log.d( TAG_NAME, entry.getKey() + ":" + entry.getValue());
        //}

        final List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
        for( BluetoothGattCharacteristic character : characteristics ){
            Log.d( TAG_NAME, "    characteristic UUID : " + character.getUuid());
        }
    }*/

    public void write(UUID serviceUUID, UUID characteristicUUID, byte[] value, ReadWriteListener listener){
        final BluetoothGattCharacteristic ledChar =
                mGatt.getService(serviceUUID)
                        .getCharacteristic(characteristicUUID);

        ledChar.setValue(value);

        boolean result = mGatt.writeCharacteristic( ledChar );
        SimpleLogger.getInstance().log(TAG_NAME, "write LED result: " + result );
    }

    public void read(UUID serviceUUID, UUID characteristicUUID, ReadWriteListener listener ){

        //byte[] result = {0};
        this.readWritelistener = listener;

        final BluetoothGattCharacteristic ledChar =
                mGatt.getService(serviceUUID)
                        .getCharacteristic(characteristicUUID);
        mGatt.readCharacteristic( ledChar );

        SimpleLogger.getInstance().log(TAG_NAME, "read : " + characteristicUUID );
    }


    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);

            if( android.bluetooth.le.ScanSettings.CALLBACK_TYPE_ALL_MATCHES == callbackType )
            {
                final String resultString = "Scan Result: " +
                        result.getDevice().getName() + ", " +
                        result.getDevice().getAddress() + ", " +
                        result.getRssi() + ", " +
                        result.getScanRecord() + ", " +
                        "thread ID: " + Thread.currentThread().getId();

                //SimpleLogger.getInstance().log(TAG_NAME, resultString);

                final BLEDeviceInfo info = new
                        BLEDeviceInfo(result.getDevice().getAddress(), result.getDevice().getName());

                //DataManager.getInstance().saveScanResult(info);
                deviceFound.onDeviceFound( info );

                if( null != info.name && info.name.equals( targetDeviceName )){
                    if( isScanning ){
                        SimpleLogger.getInstance().log(
                                TAG_NAME, "found target device, will stop scan.");

                        //avoid scheduling a new startScan, set 'terminateScan' before
                        //calling stopScan()
                        terminateScan = true;
                        stopScan();
                    }

                    connectWithAddress( info.macAddress );
                }
            }
            else
            {
                throw new AssertionError( "Undefined behavior in current SDK.");
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);

            for( ScanResult result : results ){
                final String resultString = "Batch Scan Result: " +
                        result.getDevice() + ", " +
                        result.getRssi() + ", " +
                        result.getScanRecord();

                SimpleLogger.getInstance().log(TAG_NAME, resultString );
            }
            throw new AssertionError( "TBD - need set ScanSettings argument");
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);

            SimpleLogger.getInstance().log(TAG_NAME, "onScanFailed : " + errorCode );
        }
    };
}
