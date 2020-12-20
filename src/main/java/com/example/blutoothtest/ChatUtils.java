package com.example.blutoothtest;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;


public class ChatUtils {

    private final UUID uuid = UUID.fromString("amro017624405535");
    private Context context;
    private final Handler handler;

    private BluetoothAdapter bluetoothAdapter;


    private ConnectThread connectThread;
    private ConnectedThread connectedThread;
    private AcceptThread acceptThread;
    private final String APP_NAME = "APP";

    public static final int STATE_NONE = 0;
    public static final int STATE_LISTEN = 1;
    public static final int STATE_CONNECTING = 2;
    public static final int STATE_CONNECTED = 3;
    private int state; //handel the grand state to our utiliti


    public ChatUtils(Context context, Handler handler) {
        this.context = context;
        this.handler = handler;

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        state = STATE_NONE;
    }

    public int getState() {
        return state;
    }

    public synchronized void setState(int state) {//weil Bluetooth is a synco. device
        this.state = state;
        handler.obtainMessage(MainActivity.MESS_STATE_CHANGE, state, -1).sendToTarget();// we have to send the state back to the Handler , so we can reflect the state in our mainActivity .
    }

    private synchronized void start() {
        if (connectThread != null) {
            connectThread.close();
            connectThread = null;

        }

        if (acceptThread == null) {

            acceptThread = new AcceptThread();
            acceptThread.start();
        }
        if (connectedThread != null) {
            connectedThread.cancel();
            connectedThread = null;

        }

        setState(STATE_LISTEN);
    }

    public synchronized void stop() {
        if (connectThread != null) {
            connectThread.close();
            connectThread = null;

        }

        if (acceptThread != null) {

            acceptThread.cancel();
            acceptThread = null;
        }

        setState(STATE_NONE);

    }

    public void connect(BluetoothDevice device) {


        if (state == STATE_CONNECTING) {


            connectThread.close();
            connectThread = null;

        }


        connectThread = new ConnectThread(device);
        connectThread.start();
        setState(STATE_CONNECTING);

    }

    public void write(byte[] buffer) throws IOException {

        ConnectedThread thread;
        synchronized (this) {


            if (state != STATE_CONNECTED) {

                return;

            }


            thread = connectedThread;

        }
        connectedThread.write(buffer);

    }

    private class AcceptThread extends Thread {

        private BluetoothServerSocket serverSocket;


        public AcceptThread() {
            BluetoothServerSocket tmp = null;

            try {

                bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(APP_NAME, uuid);

            } catch (IOException e) {

                Log.e("Accept->constructor ", e.toString());

            }
            serverSocket = tmp;

        }

        public void run() {//trying to connect this Socket !

            BluetoothSocket socket = null;

            try {

                socket = serverSocket.accept();
            } catch (IOException e) {

                Log.e("Accept->run ", e.toString());


                try {

                    serverSocket.close();


                } catch (IOException e1) {

                    Log.e("Accept->close ", e1.toString());
                }

            }

            if (socket != null) {


                switch (state) {

                    case STATE_LISTEN:
                    case STATE_CONNECTING:


                        connect(socket.getRemoteDevice());

                        break;


                    case STATE_CONNECTED:
                    case STATE_NONE:


                        try {

                            socket.close();

                        } catch (IOException e1) {

                            Log.e("Accept->close ", e1.toString());
                        }

                }


            }


        }

        public void cancel() {

            try {

                serverSocket.close();

            } catch (IOException e1) {

                Log.e("Accept->closeServerSock", e1.toString());
            }


        }

    }

    private class ConnectThread extends Thread {//handle all of our connectivity

        private final BluetoothSocket socket;
        private final BluetoothDevice device;


        private ConnectThread(BluetoothDevice device) {

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

                connectionFailed();
                return;
            }
            synchronized (ChatUtils.this) {
                connectThread = null;

            }


            connected(device, socket);

        }

        public void close() {

            try {

                socket.close();

            } catch (IOException e1) {
                Log.e("Connectiv->cancelMethod", e1.toString());

            }

        }


    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket bluetoothSocket;

        private final InputStream inputStream;

        private final OutputStream outputStream;

        public ConnectedThread(BluetoothSocket bluetoothSocket) {
            this.bluetoothSocket = bluetoothSocket;
            InputStream tmpin = null;
            OutputStream tmpou = null;

            try {
                tmpin = bluetoothSocket.getInputStream();
                tmpou = bluetoothSocket.getOutputStream();
            } catch (IOException e) {

                Log.e("Connectiv->constructor ", e.toString());

            }
            inputStream = tmpin;
            outputStream = tmpou;


        }

        public void run() {

            byte[] buffer = new byte[1024];
            int bytes;


            try {

                bytes = inputStream.read(buffer);


                handler.obtainMessage(MainActivity.MESS_READ, bytes, -1, buffer).sendToTarget();

            } catch (IOException e) {

                connectionLost();

            }


        }

        public void write(byte[] buffer) throws IOException {

            try {
                outputStream.write(buffer);

                handler.obtainMessage(MainActivity.MESS_WRITE, -1, -1, buffer).sendToTarget();

            } catch (IOException e0) {


            }


        }


        public void cancel() {
            try {

                bluetoothSocket.close();
            } catch (IOException e0) {


            }

        }
    }

    private void connectionLost() {
        Message message = handler.obtainMessage(MainActivity.MESS_TOAST);

        Bundle bundle = new Bundle();
        bundle.putString(MainActivity.TOAST, "Connection Lost !");

        message.setData(bundle);
        handler.sendMessage(message);

        ChatUtils.this.start();

    }

    private synchronized void connectionFailed() {
        Message message = handler.obtainMessage(MainActivity.MESS_TOAST);  //we gonna send it to the handler to show it in our mainactivity .
        Bundle bundle = new Bundle();
        bundle.putString(MainActivity.TOAST, " Cant connect to the Device");
        message.setData(bundle);
        handler.sendMessage(message);

        ChatUtils.this.start();//to start listining again
    }

    private synchronized void connected(BluetoothDevice device, BluetoothSocket socket) {

        //firstly we gonne close the connected Threads , because we not need it , we connected to a device !

        if (connectThread != null) {


            connectThread.close();
            connectThread = null;

        }
        if (connectedThread != null) {
            connectedThread.cancel();
            connectedThread = null;

        }
        connectedThread = new ConnectedThread(socket);
        connectedThread.start();


        Message message = handler.obtainMessage(MainActivity.MESS_DEV_NAME);  //we gonna send it to the handler to show it in our mainactivity .
        Bundle bundle = new Bundle();
        bundle.putString(MainActivity.DEV_NAME, device.getName());//dev_name in main activity definieren !
        message.setData(bundle);
        handler.sendMessage(message);
        setState(STATE_CONNECTED);//CHANGE THE STATE OF OUR CHATUTILIS

    }

}

