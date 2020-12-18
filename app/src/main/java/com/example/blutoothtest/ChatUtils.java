package com.example.blutoothtest;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.util.Log;


import java.io.IOException;
import java.util.UUID;


public class ChatUtils {

    private final UUID uuid = UUID.fromString("amro017624405535");
    private Context context;
    private final Handler handler;


    public static final int STATE_NONE = 0;
    public static final int STATE_LISTEN = 1;
    public static final int STATE_CONNECTING = 2;
    public static final int STATE_CONNECTED = 3;


    private int state; //handel the grand state to our utiliti


    public ChatUtils(Context context, Handler handler) {
        this.context = context;
        this.handler = handler;


        state = STATE_NONE;
    }


    public int getState() {
        return state;
    }

    public synchronized void setState(int state) {//weil Bluetooth is a synco. device
        this.state = state;
        handler.obtainMessage(MainActivity.MESS_STATE_CHANGE, state, -1).sendToTarget();// we have to send the state back to the Handler , so we can reflect the state in our mainActivity .
    }

    public synchronized void start() {


    }

    public synchronized void stop() {


    }


    private class Connectivity extends Thread {//handle all of our connectivity

        private final BluetoothSocket socket;
        private final BluetoothDevice device;


        private Connectivity(BluetoothDevice device) {

            this.device = device;


            BluetoothSocket tmp = null;

            try {

                tmp = device.createRfcommSocketToServiceRecord(uuid);
            } catch (IOException e) {

                Log.e("Connectiv->constructor ", e.toString());

            }
            socket = tmp;
        }

        public void run() {

            try {

                socket.connect();

            } catch (IOException e) {
                Log.e("Connectiv->run ", e.toString());
                try {
                    socket.close();

                } catch (IOException e1) {
                    Log.e("Connectiv->colseSocket ", e1.toString());

                }


            }
            synchronized (ChatUtils.this) {




            }

        }


    }


}
