package hu.bme.hit.smartparkingassist.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.github.pires.obd.commands.engine.RPMCommand;
import com.github.pires.obd.commands.protocol.EchoOffCommand;
import com.github.pires.obd.commands.protocol.LineFeedOffCommand;
import com.github.pires.obd.commands.protocol.SelectProtocolCommand;
import com.github.pires.obd.commands.protocol.TimeoutCommand;
import com.github.pires.obd.enums.ObdProtocols;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.UUID;

import hu.bme.hit.smartparkingassist.MainMenuActivity;

public class ObdService extends Service {

    public static final String  PARKING_STATUS_PREF_KEY = "PARKING_STATUS_PREF_KEY";
    public static final String BR_PARKING_STATUS = "BR_PARKIN_STATUS";
    public static final String PARKING_STATUS_KEY = "PARKING_STATUS_KEY";

    private BluetoothSocket socket;
    private int fallbackCounter = 0;
    private boolean isParking;
    private boolean isConnectionOk = false;
    private static final String OBD_NODATA_ERROR_MSG = "NODATA";
    private String deviceAddress;
    private static final int THIRTY_SECOND = 1000 * 30;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        deviceAddress = intent.getStringExtra(MainMenuActivity.SELECTED_BLUETOOTH_DEVICE_KEY);
        checkParkingStatus(deviceAddress);
        Log.e("[OBDService]", "Current parking status: " + isParking);

        if (isConnectionOk) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
            boolean parkingStatus = sp.getBoolean(PARKING_STATUS_PREF_KEY, false);

            if (isParking != parkingStatus) {
                sp.edit().putBoolean(PARKING_STATUS_PREF_KEY, isParking).commit();
                Intent brIntent = new Intent(BR_PARKING_STATUS);

                if (isParking) {
                    brIntent.putExtra(PARKING_STATUS_KEY, "reserved");
                    Log.e("[OBDService]", "Send reserved parking status.");
                } else {
                    brIntent.putExtra(PARKING_STATUS_KEY, "free");
                    Log.e("[OBDService]", "Send free parking status.");
                }

                LocalBroadcastManager.getInstance(this).sendBroadcast(brIntent);
            } else {
                Log.e("[OBDService]", "Parking status hasn't changed.");
            }
        } else {
            Log.e("[OBDService]", "Cannot determine parking status due to a connection error.");
        }

        stopSelf();

        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        SharedPreferences myPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        Boolean isObdEnabled = myPrefs.getBoolean("obd_switch", false);

        if (isObdEnabled) {
            AlarmManager alarm = (AlarmManager) getSystemService(ALARM_SERVICE);
            Intent intent = new Intent(this, ObdService.class);
            intent.putExtra(MainMenuActivity.SELECTED_BLUETOOTH_DEVICE_KEY, deviceAddress);
            alarm.set(alarm.RTC,
                    System.currentTimeMillis() + THIRTY_SECOND,
                    PendingIntent.getService(this, 0, intent, 0));
        }

        Log.e("[OBDService]", "Service was stopped.");
    }

    private void checkParkingStatus(String deviceAddress) {
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        btAdapter.cancelDiscovery();
        BluetoothDevice device = btAdapter.getRemoteDevice(deviceAddress);

        socket = null;
        BluetoothSocket sockFallback;
        UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
        try {
            socket = device.createRfcommSocketToServiceRecord(MY_UUID);
            socket.connect();
            isConnectionOk = true;
            Log.e("[OBDService]", "Bluetooth connection with OBD device is established.");
        } catch (Exception e1) {
            Log.e("[OBDService]", "There was an error while establishing Bluetooth connection. Falling back..", e1);
            Class<?> clazz = socket.getRemoteDevice().getClass();
            Class<?>[] paramTypes = new Class<?>[]{Integer.TYPE};
            try {
                Method m = clazz.getMethod("createInsecureRfcommSocket", paramTypes);
                Object[] params = new Object[]{Integer.valueOf(1)};
                sockFallback = (BluetoothSocket) m.invoke(socket.getRemoteDevice(), params);
                sockFallback.connect();
                socket = sockFallback;
                socket.connect();
                isConnectionOk = true;
                Log.e("[OBDService]", "Bluetooth connection with OBD device is established.");
            } catch (Exception e2) {
                Log.e("[OBDService]", "Couldn't fallback while establishing Bluetooth connection.", e2);
            }
        }

        if (isConnectionOk) {
            try {
                //initialization
                new EchoOffCommand().run(socket.getInputStream(), socket.getOutputStream());
                new LineFeedOffCommand().run(socket.getInputStream(), socket.getOutputStream());
                new TimeoutCommand(125).run(socket.getInputStream(), socket.getOutputStream());
                new SelectProtocolCommand(ObdProtocols.AUTO).run(socket.getInputStream(), socket.getOutputStream());
                checkRpmStatus();
            } catch (Exception e) {
                isConnectionOk = false;
                Log.e("[OBDService]", "Initialization was unsuccessful.", e);
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    Log.e("[OBDService]", "Couldn't close Bluetooth connection.", e);
                }
            }
        }
    }

    private void checkRpmStatus() {
        try {
            RPMCommand rpmCommand = new RPMCommand();
            rpmCommand.run(socket.getInputStream(), socket.getOutputStream());
            int rpm = rpmCommand.getRPM();
            Log.e("[OBDService]", "RPM rpm: " + rpm);
            if (rpm > 0) {
                isParking = false;
            } else if (rpm == 0) {
                isParking = true;
            } else {
                Log.e("[OBDService]", "Invalid RPM value.");
            }
        } catch (Exception e1) {
            Log.e("[OBDService]", "Couldn't receive RPM data from OBD device.", e1);
            String errorMsg = e1.getMessage();
            if (fallbackCounter++ < 3 && errorMsg.contains(OBD_NODATA_ERROR_MSG)) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e2) {
                    e2.printStackTrace();
                }
                checkRpmStatus();
            } else if (fallbackCounter >= 3  && errorMsg.contains(OBD_NODATA_ERROR_MSG)) {
                isParking = true;
            } else {
                isConnectionOk = false;
                Log.e("[OBDService]", "Unexpected error message: " + errorMsg);
            }
        } finally {
            try {
                socket.close();
            } catch (IOException e2) {
                Log.e("[OBDService]", "Couldn't close Bluetooth connection.", e2);
            }
        }
    }

}
