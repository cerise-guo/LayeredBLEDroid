package com.peripheral.logger;

import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.LinkedBlockingQueue;

public class SimpleLogger {
    private String TAG_NAME = "SimpleLogger";

    private LinkedBlockingQueue<String> logQueue = new LinkedBlockingQueue<String>();
    final SimpleDateFormat timeStampFormat = new SimpleDateFormat("dd HH:mm:ss:SSS");
    static String logPath;
    static volatile private SimpleLogger instance;

    //Note: this can be a temporary solution to dump log message to UI layer
    //This log listener allows hooking one listener to receive all log message.
    static private UILogListener logListener = null;
    public interface UILogListener {
        void onLogMessage( String msg );
    }

    private SimpleLogger() {}
    public static SimpleLogger getInstance(){
        if (instance==null) {
            synchronized(SimpleLogger.class){
                if (instance==null) {
                    instance=new SimpleLogger();
                }
            }
        }
        return instance;
    }

    static public void addUIListener( UILogListener logListener ){
        if( null != SimpleLogger.logListener ){
            throw new RuntimeException("Already set UI log listener");
        }
        SimpleLogger.logListener = logListener;
    }

    static public void setLogPath(String path ){
        logPath = path;
    }
    static public String logPath(){ return logPath;}

    public void log(String tag, String message){

        final String logMessage = timeStampFormat.format( new Date()).toString()
                + " " + tag + " : " + message;

        logQueue.add(logMessage);
        Log.d(tag, logMessage);

        if( null != SimpleLogger.logListener){
            logListener.onLogMessage( logMessage );
        }

        if( 100 == logQueue.size()){
            flushLogToDisk();
        }
    }

    public void flushLogToDisk(){
        Log.d(TAG_NAME, "will save log to disk" );

        final LinkedBlockingQueue<String> tempList = new LinkedBlockingQueue<String>(logQueue);
        logQueue.clear();
        saveLogAsync( tempList );
    }

    //adb pull /storage/emulated/0/Android/data/com.ceriseguo.demofunc/files/
    void saveLogAsync( final LinkedBlockingQueue<String> stringList ){

        final SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-HH-mm");
        final String fileName = "Simple-" + dateFormat.format( Calendar.getInstance().getTime()).toString() + ".log";

        new Thread(new Runnable() {
            @Override
            public void run() {

                FileOutputStream outputStream = null;
                Log.d(TAG_NAME, "taget log file path: " + logPath + fileName );

                File logFile = new File(logPath , fileName  );

                try {
                    if (logFile.exists()) {
                        outputStream = new FileOutputStream(logFile, true);
                    } else {
                        outputStream = new FileOutputStream(logFile, false);
                    }

                    for (String message : stringList) {
                        outputStream.write( message.getBytes());
                        outputStream.write("\n".getBytes());
                    }

                    outputStream.flush();
                    outputStream.close();

                    Log.d(TAG_NAME, "log saved to disk : " + logPath + fileName );

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    log(TAG_NAME,"special : FileNotFoundException when saving log: " + e.toString() );
                } catch (IOException e) {
                    e.printStackTrace();
                    log(TAG_NAME,"special : IOException when saving log: " + e.toString() );
                }
            }
        }).start();
    }
}
