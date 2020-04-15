package com.ceriseguo.demofunc;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

//import com.peripheral.ble.BLEDeviceManager;
//import com.peripheral.data.DataManager;
import com.peripheral.bl.LEDController;
import com.peripheral.bl.MyBackgroundService;
import com.peripheral.logger.SimpleLogger;

import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements UIMessage {

    public final static String TAG_NAME = "DemoFuncBootComplete";

    TextView statusView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MyBackgroundService.setLogPath( this );
        SimpleLogger.setLogPath( MyBackgroundService.getLogPath());
        SimpleLogger.getInstance().log(TAG_NAME, "MainActivity.onCreate");

        checkBLEPermission();

        statusView = (TextView)findViewById(R.id.status_text);


        SimpleLogger.addUIListener(new SimpleLogger.UILogListener() {
            @Override
            public void onLogMessage(String msg) {
                updateUIMessage( msg );
            }
        });
    }

    public void updateUIMessage(final String message ){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if( !message.equals( statusView.getText() )){
                    statusView.setText( message );
                }
            }
        });
    }

    public void onClickA( View view)
    {
        Log.d(TAG_NAME, "Enter onClickA");

        //adb pull /sdcard/Android/data/com.ceriseguo.demofunc/files/
        final Intent myIntent = new Intent(this, MyBackgroundService.class);
        myIntent.putExtra("myData", new Random().nextInt(5000));
        MyBackgroundService.enqueueWork( this, myIntent);

    }

    public void onClickB( View view)
    {
        MyBackgroundService.stopLoop();
    }

    public void onClickC( View view)
    {
        Log.d(TAG_NAME, "will dump discovered devices.");
        //final List<String> devices = DataManager.getInstance().deviceList();

        //for(String deviceInfo : devices ){
        //    Log.d(TAG_NAME, deviceInfo);
        //}
    }

    //Don't instantiate the LEDController too early, else it won't be able to get gatt properly.
    LEDController ledController = null;
    public void onClickD( View view)
    {
        Log.d(TAG_NAME, "Turn LED Off.");

        if( null == ledController ){
            ledController = new LEDController();
        }
        ledController.TurnOffLED();
    }

    public void onClickE( View view)
    {
        Log.d(TAG_NAME, "Turn LED On.");

        if( null == ledController ){
            ledController = new LEDController();
        }
        ledController.TurnOnLED();
    }

    public void onClickF( View view)
    {
        Log.d(TAG_NAME, "OnClickF - read LED value");

        if( null == ledController ){
            ledController = new LEDController();
        }
        ledController.getLEDStatus(new LEDController.LEDStatusCallback() {
            @Override
            public void ledStatus(boolean isOn) {
                Log.d(TAG_NAME, "isLEDOn : " + (isOn?"YES":"No") );
            }
        });
    }

    public void onClickG( View view)
    {
        Log.d(TAG_NAME, "OnClickG");
    }

    private void checkBLEPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 123);
            }
        }

        BluetoothAdapter btAdapter = ((BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();
        if (btAdapter != null && !btAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent,100);
        }
    }
}
