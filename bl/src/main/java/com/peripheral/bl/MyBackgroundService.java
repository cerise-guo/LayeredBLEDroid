package com.peripheral.bl;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;
import android.util.Log;


import com.peripheral.data.DataManagerImpl;
import com.peripheral.logger.SimpleLogger;

import java.util.concurrent.LinkedBlockingQueue;

public class MyBackgroundService extends JobIntentService {
    public static final int JOB_ID = 0x01;

    final static String TAG_NAME = "MyBackgroundService";

    LinkedBlockingQueue<String> logList = new LinkedBlockingQueue<String>();

    private static String logPath = null;
    private static boolean stopLoop = false;

    final private String TARGET_DEVICE_NAME = "Nordic_Blinky";

    public static int LOOP_INTERVAL = 2000; //millisecond

    //private static BLEScanner bleScanner;
    //private static BLEDeviceManager deviceManager;

    public static void enqueueWork(Context context, Intent intent) {

        //if( null == bleScanner ){
        //    bleScanner = BLEScanner.initBLEScanner( context );
        //}
        //if( null == deviceManager ){
        //    BLEDeviceManager.initiate(context);
        //    deviceManager = deviceManager.getInstance();
        //}

        if( !DataManagerImpl.getInstance().isInitialized()){
            DataManagerImpl.getInstance().initiate(context);
        }

        SimpleLogger.getInstance().log(TAG_NAME, "MyBackgroundService enqueueWork is called");
        enqueueWork(context, MyBackgroundService.class, JOB_ID, intent);
    }

    static public void setLogPath(Context context ){

        //Note: one other solution is to save path to User preference at the first time.
        //      Then all subsequence call just uses the saved path in preference.
        String externalFilePath = context.getExternalFilesDir(null).getPath() + "/";
        SimpleLogger.getInstance().setLogPath(externalFilePath);
        SimpleLogger.getInstance().log(TAG_NAME, "setLogPath : " + externalFilePath);
        logPath = externalFilePath;
    }
    static public String getLogPath(){
        return logPath;
    }

    static public void stopLoop(){
        stopLoop = true;
    }

    // do NOT override onCreate(), else onHandleWork won't be called.
    //@Override
    //public void onCreate(){
    //    Log.d(MainActivity.TAG_NAME, "MyBackgroundService onCreate()");
    //    toast("background service onCreate");
    //}

    static int onHandleWorkCount = 0;

    static boolean isExecuting = false;

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        final int hashCode = this.hashCode();
        onHandleWorkCount++;
        SimpleLogger.getInstance().log(TAG_NAME,"special 1 : onHandleWork entry : " + onHandleWorkCount + " , hash : " + hashCode );

        if( isExecuting ){
            SimpleLogger.getInstance().log(TAG_NAME,"special : onHandleWork re-entry : " + onHandleWorkCount + " , hash : " + hashCode);
            return;
        }
        isExecuting = true;

        final int value = intent.getIntExtra("myData", 100);

        SimpleLogger.getInstance().log(TAG_NAME, "onHandleWork : " + value );

        //if( null != bleScanner ){
        //    bleScanner.startScan();
        //}

        DataManagerImpl.getInstance().updateDeviceInfo(TARGET_DEVICE_NAME);

        //final SimpleDateFormat timeStampFormat = new SimpleDateFormat("dd HH:mm:ss:SSS");

        int count = 1;
        while (!stopLoop){

            //final String logMessage = ""//timeStampFormat.format( new Date()).toString()
            //        + "   execute work : intent ID " + value
            //        + ", loop ID : " + onHandleWorkCount
            //        + ", count : " + count++
            //        + ", thread ID : " + Thread.currentThread().getId()
             //       + ", hash : " + hashCode;
            //Log.d(MainActivity.TAG_NAME, logMessage);
            /*if( null != DataManager.getInstance().foundDevice(TARGET_DEVICE_NAME)){

                //stop scan, the scan operation will be really terminated after N seconds.
                //bleScanner.termianteScan();
            }*/
            /*if( null != DataManager.getInstance().foundDevice(TARGET_DEVICE_NAME) &&
            !bleScanner.isScanning()){
                SimpleLogger.getInstance().log(TAG_NAME,
                        "scan has been terminated, connect now.");

                final String macAddress = DataManager.getInstance().foundDevice(TARGET_DEVICE_NAME);

                if (deviceManager.isConnected(macAddress)) {

                } else {
                    if (deviceManager.connectWithAddress(macAddress)) {
                        SimpleLogger.getInstance().log(TAG_NAME, "start to connect");
                    } else {
                        SimpleLogger.getInstance().log(TAG_NAME, "failed to start connection");
                    }
                }

            }*/

            //SimpleLogger.getInstance().log(
            //        TAG_NAME, "Device ready : " + DataManager.getInstance().isDeviceRead());

            try {
                //SimpleLogger.getInstance().log(TAG_NAME, logMessage);


                Thread.sleep(LOOP_INTERVAL);
            }
            catch (InterruptedException e){

            }
        }

        isExecuting = false;
        SimpleLogger.getInstance().log(TAG_NAME, "special : completed onHandleWork : " + onHandleWorkCount + " , hash : " + hashCode + ", value : " + value);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        Log.d(TAG_NAME, "MyBackgroundService onDestroy() , hash : " + this.hashCode());
        SimpleLogger.getInstance().log(TAG_NAME,"special : MyBackgroundService onDestroy() , hash : " + this.hashCode() );
        SimpleLogger.getInstance().flushLogToDisk();
    }

    @Override
    public boolean onStopCurrentWork() {
        SimpleLogger.getInstance().log(TAG_NAME,"special : MyBackgroundService onStopCurrentWork() , hash " +  this.hashCode() );
        return true;
    }
}
