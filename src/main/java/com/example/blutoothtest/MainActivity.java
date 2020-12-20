package com.example.blutoothtest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;


public class MainActivity extends AppCompatActivity {


    private Context context;

    private BluetoothAdapter bluetoothAdapter;

    private ChatUtils chatUtils;
    // unique for Activity
    private final int location_persmission_request = 101;

    private final int select_dev = 102;


    private ListView listMain;

    private EditText creatMess;

    private Button sendMess;

    private ArrayAdapter<String> chatAdapter;


    public static final int MESS_STATE_CHANGE = 0;
    public static final int MESS_READ = 1;
    public static final int MESS_WRITE = 2;
    public static final int MESS_DEV_NAME = 3;
    public static final int MESS_TOAST = 4;
    public static final String DEV_NAME = "device Name : ";//key for handler , case dev_name
    public static final String TOAST = "toast ";//key for handler , case mess_toast
    private String connenctedDev;

    //handelt alle Nachrichten , die zum MainActivity kommen !

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {

            switch (msg.what) {//tells the typ of Mess

                case MESS_STATE_CHANGE:

                    switch (msg.arg1) {//welches wir von chatutil gesendet haben .

                        case ChatUtils.STATE_NONE:

                            setState("Not Connected !");

                            break;

                        case ChatUtils.STATE_LISTEN:
                            setState("Not Connected !");

                            break;

                        case ChatUtils.STATE_CONNECTING:
                            setState("Connecting ...");

                            break;

                        case ChatUtils.STATE_CONNECTED:
                            setState("CONNECTED " + connenctedDev);

                            break;


                    }

                    break;
                case MESS_WRITE:

                    byte[] bufferWr = (byte[]) msg.obj;

                    String outputbuffer = new String(bufferWr);

                    chatAdapter.add(" Ich : " + outputbuffer);


                    break;
                case MESS_READ:
                    byte[] buffer = (byte[]) msg.obj;
                    String inputbuffer = new String(buffer, 0, msg.arg1);

                    chatAdapter.add(connenctedDev + ": " + inputbuffer);

                    break;

                case MESS_DEV_NAME:
                    connenctedDev = msg.getData().getString(DEV_NAME);
                    Toast.makeText(context, connenctedDev, Toast.LENGTH_SHORT).show();
                    break;

                case MESS_TOAST:

                    Toast.makeText(context, msg.getData().getString(TOAST), Toast.LENGTH_SHORT).show();
                    break;
            }

            return false;
        }
    });//بيبعت ويستقبل كل المعلومات الي جايه من الثريدز الي في الشات يوتليز

    private void setState(CharSequence subTitle) {//to reflect the states of the handler , which showing to chatuitils

        getSupportActionBar().setSubtitle(subTitle);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        init();
        initBluetooth();
        chatUtils = new ChatUtils(context, handler);
    }

    private void init() {
        listMain = findViewById(R.id.listMain);
        sendMess = findViewById(R.id.sendMess);
        creatMess = findViewById(R.id.txt1);
        chatAdapter = new ArrayAdapter<String>(context, R.layout.message_layout);
        listMain.setAdapter(chatAdapter);

        sendMess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String mess = creatMess.getText().toString();
                if (!mess.isEmpty()) {

                    try {
                        creatMess.setText("");
                        chatUtils.write(mess.getBytes());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }


            }
        });

    }

    private void initBluetooth() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();


        if (bluetoothAdapter == null) {

            Toast.makeText(context, "Sie haben Kein Bluetooth auf Ihr Geraet !!", Toast.LENGTH_SHORT).show();

        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {


        switch (item.getItemId()) {

            case R.id.menu_search_devices:

                checkPermission();
                return true;

            case R.id.menu_enable_bluetooth:
                enableBlutooth();
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }


    }

    private void checkPermission() {

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {


            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, location_persmission_request);


        } else {

            Intent intent = new Intent(context, DeviceListActivity.class);

            startActivityForResult(intent, select_dev);

        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {


        if (requestCode == select_dev && resultCode == RESULT_OK) {

            String addr = data.getStringExtra("deviceAddress");
            chatUtils.connect(bluetoothAdapter.getRemoteDevice(addr));


        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == location_persmission_request) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                Intent intent = new Intent(context, DeviceListActivity.class);

                startActivityForResult(intent, select_dev);


            } else {

                new AlertDialog.Builder(context).setCancelable(false).setMessage("Location permission is reqquaierd !").setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {


                        checkPermission();

                    }
                }).setNegativeButton("Deny", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MainActivity.this.finish();
                    }
                }).show();


            }

        } else {


            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void enableBlutooth() {

        if (!bluetoothAdapter.isEnabled()) {

            bluetoothAdapter.enable();

        }
        if (bluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {

            Intent discoveryIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);

            discoveryIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoveryIntent);


        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (chatUtils != null) {


            chatUtils.stop();


        }

    }
}

