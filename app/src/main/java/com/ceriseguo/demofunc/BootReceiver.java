package com.ceriseguo.demofunc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.peripheral.bl.MyBackgroundService;
import com.peripheral.logger.SimpleLogger;

import java.util.Random;

public class BootReceiver extends BroadcastReceiver {

    private static String TAG_NAME = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent ){
        SimpleLogger.getInstance().log(TAG_NAME,"started background services : " + intent.getAction());

        if( Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())){

            String message = "BootReceiver onReceive Boot_Completed.";
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();

            if( null == MyBackgroundService.getLogPath() ){
                SimpleLogger.getInstance().log(TAG_NAME,
                        "getExternalFilesDir : " + context.getExternalFilesDir(null).getPath());
                MyBackgroundService.setLogPath( context );
            }

            SimpleLogger.getInstance().log(TAG_NAME, "OnRecevie ACTION_BOOT_COMPLETED");
            startServiceDirectly(context);
        }
    }

    public void startServiceDirectly(Context context){

        String message = "BootReceiver onReceive Start Service directly.";

        Toast.makeText(context, message, Toast.LENGTH_LONG).show();

        Intent startServiceIntent = new Intent( context, MyBackgroundService.class);
        startServiceIntent.putExtra("myData", 654);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            Log.d(MainActivity.TAG_NAME, "startServiceDirectly Android O");

            final Intent myIntent = new Intent(context, MyBackgroundService.class);
            myIntent.putExtra("myData", new Random().nextInt(5000) + 10000);
            SimpleLogger.getInstance().log(TAG_NAME,"startServiceDirectly enqueueWork");
            MyBackgroundService.enqueueWork(context, myIntent);
        }else{
            SimpleLogger.getInstance().log(TAG_NAME,"startServiceDirectly old Android startService");
            context.startService( startServiceIntent);
        }
    }
}
