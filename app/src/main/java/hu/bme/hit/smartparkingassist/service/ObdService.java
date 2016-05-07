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
        BluetoothDevice device = btAdapter.getRemoteDevice(deviceAddress);

        BluetoothSocket socket = null;
        try {
            //UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
            //socket = device.createInsecureRfcommSocketToServiceRecord(uuid);
            Method m = device.getClass().getMethod("createInsecureRfcommSocket", new Class[]{int.class});
            socket = (BluetoothSocket) m.invoke(device, Integer.valueOf(1));

            Log.d("[OBDService]", "Trying to connect to " + deviceAddress);
            socket.connect();
            if (socket.isConnected()) {
                Log.d("[OBDService]", "Bluetooth connection is established.");
            }

            //initialization
            new EchoOffCommand().run(socket.getInputStream(), socket.getOutputStream());
            new LineFeedOffCommand().run(socket.getInputStream(), socket.getOutputStream());
            new TimeoutCommand(125).run(socket.getInputStream(), socket.getOutputStream());
            new SelectProtocolCommand(ObdProtocols.AUTO).run(socket.getInputStream(), socket.getOutputStream());

            RPMCommand rpmCommand = new RPMCommand();
            rpmCommand.run(socket.getInputStream(), socket.getOutputStream());

            Log.d("[OBDService]", "RPM: " + rpmCommand.getFormattedResult());

            socket.close();
            Log.d("[OBDService]", "Bluetooth connection is closed.");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
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
        Log.d("[OBDService]", "Service was stopped.");
    }
}
