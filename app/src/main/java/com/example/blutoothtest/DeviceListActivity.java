package com.example.blutoothtest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;

public class DeviceListActivity extends AppCompatActivity {

    private ListView listPairedDev, listAvaDev;

    private ProgressBar progressBar;

    private ArrayAdapter<String> adapterPariredDev, adapterAvaliableDev;

    private Context context;

    private BluetoothAdapter bluetoothAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);

        context = this;

        init();
    }


    private void init() {

        listPairedDev.findViewById(R.id.list_paried_dev);
        listAvaDev.findViewById(R.id.list_avaliable_dev);
        progressBar.findViewById(R.id.progress_scan_dev);
        adapterAvaliableDev = new ArrayAdapter<String>(context, R.layout.device_list_item);
        adapterPariredDev = new ArrayAdapter<String>(context, R.layout.device_list_item);


        listAvaDev.setAdapter(adapterAvaliableDev);
        listPairedDev.setAdapter(adapterPariredDev);


        listAvaDev.setOnItemClickListener((parent, view, position, id) -> {
            String info = ((TextView) view).getText().toString();
            String addr = info.substring(info.length() - 17);

            Intent intent = new Intent();

            intent.putExtra("deviceAddress", addr);
            setResult(RESULT_OK, intent);
            finish();
        });

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        Set<BluetoothDevice> parieddevices = bluetoothAdapter.getBondedDevices();

        if (parieddevices != null && parieddevices.size() > 0) {
            for (BluetoothDevice e : parieddevices) {


                adapterPariredDev.add(e.getName() + "\n" + e.getAddress());
            }

        }

        IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(bluetoothDevListner, intentFilter);
        IntentFilter intentFilter2 = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(bluetoothDevListner, intentFilter2);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_device_list, menu);

        return super.onCreateOptionsMenu(menu);
    }

    private BroadcastReceiver bluetoothDevListner = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {


            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {


                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {


                    adapterAvaliableDev.add(device.getName() + "\n" + device.getAddress());
                }


            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {

                progressBar.setVisibility(View.GONE);

                if (adapterAvaliableDev.getCount() == 0) {
                    Toast.makeText(context, " No new Device Found ! ", Toast.LENGTH_SHORT).show();

                } else {


                    Toast.makeText(context, " Click on Device to start the Chat ! ", Toast.LENGTH_SHORT).show();

                }


            }

        }
    };


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {


        switch (item.getItemId()) {

            case R.id.scan_devices:

                scanDevices();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void scanDevices() {

        progressBar.setVisibility(View.VISIBLE);
        adapterAvaliableDev.clear();
        Toast.makeText(context, "Scan started", Toast.LENGTH_SHORT).show();


        if (bluetoothAdapter.isDiscovering()) {

            bluetoothAdapter.cancelDiscovery();
        }


        bluetoothAdapter.startDiscovery();


    }
}