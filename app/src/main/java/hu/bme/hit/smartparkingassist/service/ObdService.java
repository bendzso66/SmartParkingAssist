package hu.bme.hit.smartparkingassist.service;

import android.app.AlertDialog;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.github.pires.obd.commands.engine.RPMCommand;
import com.github.pires.obd.commands.engine.RuntimeCommand;
import com.github.pires.obd.commands.protocol.EchoOffCommand;
import com.github.pires.obd.commands.protocol.LineFeedOffCommand;
import com.github.pires.obd.commands.protocol.SelectProtocolCommand;
import com.github.pires.obd.commands.protocol.TimeoutCommand;
import com.github.pires.obd.commands.temperature.AmbientAirTemperatureCommand;
import com.github.pires.obd.enums.ObdProtocols;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import hu.bme.hit.smartparkingassist.MainMenuActivity;
import hu.bme.hit.smartparkingassist.communication.LogInTask;

public class ObdService extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String deviceAddress = intent.getStringExtra(MainMenuActivity.SELECTED_BLUETOOTH_DEVICE_KEY);
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        btAdapter.cancelDiscovery();
        BluetoothDevice device = btAdapter.getRemoteDevice(deviceAddress);

        BluetoothSocket socket = null;
        BluetoothSocket sockFallback;
        UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
        try {
            socket = device.createRfcommSocketToServiceRecord(MY_UUID);
            socket.connect();
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
                Log.e("[OBDService]", "Bluetooth connection with OBD device is established.");
            } catch (Exception e2) {
                Log.e("[OBDService]", "Couldn't fallback while establishing Bluetooth connection.", e2);
            }
        }

        try {
            //initialization
            new EchoOffCommand().run(socket.getInputStream(), socket.getOutputStream());
            new LineFeedOffCommand().run(socket.getInputStream(), socket.getOutputStream());
            new TimeoutCommand(125).run(socket.getInputStream(), socket.getOutputStream());
            new SelectProtocolCommand(ObdProtocols.AUTO).run(socket.getInputStream(), socket.getOutputStream());
        } catch (Exception e) {
            Log.e("[OBDService]", "Initialization was unsuccessful.", e);
        }

        try {
            RPMCommand rpmCommand = new RPMCommand();
            rpmCommand.run(socket.getInputStream(), socket.getOutputStream());
            Log.e("[OBDService]", "RPM rpm: " + rpmCommand.getRPM());
        } catch (Exception e1) {
            Log.e("[OBDService]", "Couldn't receive RPM data from OBD device.", e1);
        } finally {
            try {
                socket.close();
            } catch (IOException e2) {
                Log.e("[OBDService]", "Couldn't close Bluetooth connection.", e2);
            }
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
        Log.e("[OBDService]", "Service was stopped.");
    }
}
