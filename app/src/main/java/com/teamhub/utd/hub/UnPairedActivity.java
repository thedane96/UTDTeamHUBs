package com.teamhub.utd.hub;


import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

/*

- repurpose bluetooth switch
- use a form field for device addition to access this activity so it can return address and set
the field
 */

public class UnPairedActivity extends AppCompatActivity {

    private final static String unpaired = "UNPAIRED_DEVICE";
    private final static int LOCATION_RESPONSE = 8;
    BluetoothDevice currentDevice;
    BluetoothAdapter bluetoothAdapter;
    private ArrayAdapter<String> mNewDevicesArrayAdapter;
    //for Bluetooth
    private ArrayList<BluetoothDevice> bluetoothDeviceArrayList = new ArrayList<BluetoothDevice>();
    private BluetoothAdapter bluetoothLeAdapter;

    /*Reference for action bar buttons. Not appropriate for scan since it's not
    * being initialized at startup */
    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.scanmenu, menu);
        return true;
    }
    */

    /*
    - when a device is clicked, gets the mac address and sends it in Intent and closes the activity
     */
    private AdapterView.OnItemClickListener mDeviceClickListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            bluetoothAdapter.cancelDiscovery();



            String info = ((TextView)view).getText().toString();

            /*
            check for no devices found
            for aesthetic purposes, no devices found is added as an list item. This check insures
            that nothing happens when it's clicked
             */
            if(info != (getResources().getText(R.string.no_devices).toString())) {
                String address = info.substring(info.length() - 17);
                String name = info.substring(0, info.length() - 18);


                for (int a = 0; a < bluetoothDeviceArrayList.size(); a++) {
                    //Log.d("TEST", name+name.length()+" gap "+bluetoothDeviceArrayList.get(a).getName()+bluetoothDeviceArrayList.get(a).getName().length());
                    if (address.equals(bluetoothDeviceArrayList.get(a).getAddress())) {
                        Log.d("Test", bluetoothDeviceArrayList.get(a).getName());
                        currentDevice = bluetoothDeviceArrayList.get(a);

                    } else {
                        Log.d("not found", "");
                    }

                }
                Intent intent = new Intent(UnPairedActivity.this, DeviceDetailActivity.class);

                Bundle b = new Bundle();

                b.putParcelable(unpaired, currentDevice);
                intent.putExtras(b);
                startActivity(intent);
                //intent.putExtra("MACADDRESS", address);

                //setResult(Activity.RESULT_OK, intent);
                finish();
            }
        }
    };

    // Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.

                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                int RSSI = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI,Short.MIN_VALUE);
                //skip if it's already paired
                if(device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    String deviceName = device.getName();
                    String deviceHardwareAddress = device.getAddress(); // MAC address

                    Log.i("Mac address:", deviceHardwareAddress + " " + deviceName);
                    //RSSI SIGNAL STRENGTH INFO//
                    if (RSSI >= -75 )
                    {mNewDevicesArrayAdapter.add(deviceName+"\n"+deviceHardwareAddress+"\n" + "SIGNAL: EXCELLENT");}
                    if (RSSI <= -70 && RSSI >= -85 )
                    {mNewDevicesArrayAdapter.add(deviceName+"\n"+deviceHardwareAddress+"\n" + "SIGNAL: GOOD");}
                    if (RSSI <=-86 && RSSI>= -100 )
                    {mNewDevicesArrayAdapter.add(deviceName+"\n"+deviceHardwareAddress+"\n" + "SIGNAL: FAIR");}
                    if (RSSI <= -100 && RSSI > -110 )
                    {mNewDevicesArrayAdapter.add(deviceName+"\n"+deviceHardwareAddress+"\n" + "SIGNAL: POOR");}
                    if (RSSI <= -110 )
                    {mNewDevicesArrayAdapter.add(deviceName+"\n"+deviceHardwareAddress+"\n" + "SIGNAL: NO SIGNAL");}

                    bluetoothDeviceArrayList.add(device);
                }
            }
            else{
                Log.i("NOT FOUND HERE", "This");
            }
        }
    };

    /*
    cancel discovery and unregister receiver
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();

        /*stop discovery*/
        if(bluetoothAdapter != null){
            bluetoothAdapter.cancelDiscovery();
        }

        // Don't forget to unregister the ACTION_FOUND receiver.
        unregisterReceiver(mReceiver);
    }

    /*start discovery*/
    public void doScanning(){
        Log.i("START","I DID");
        /*stop scanning if already scanning*/
        if(bluetoothAdapter.isDiscovering()){
            bluetoothAdapter.cancelDiscovery();
        }

        /*Request scanning*/
        bluetoothAdapter.startDiscovery();
    }

    /*
    on create
    initialize and create
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unpaired);
        // set up bluetooth adapter
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);

        bluetoothAdapter = bluetoothManager.getAdapter();


        bluetoothOnOff();

        /*
        Prompt for location permissions
         */
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            Log.w("UnpairedActivity", "Location access denied");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_RESPONSE);
        }
        /*scan button for starting discovery */
        FloatingActionButton scanButton = (FloatingActionButton) findViewById(R.id.searchDevicesButton);
        if(scanButton != null){
            scanButton.setOnClickListener(new View.OnClickListener(){
                public void onClick(View v){
                    doScanning();
                }
            });
        }


        /*
        check to see if BLE is supported on this device
         */
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.no_ble, Toast.LENGTH_SHORT).show();
            finish();
        }

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.

        bluetoothLeAdapter = bluetoothManager.getAdapter();


        /*
        - New device array to store paired devices

         */
        ArrayAdapter<String> pairedDevicesArrayAdapter =
                new ArrayAdapter<String>(this, R.layout.device_name);
        mNewDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);

        // Find and set up the ListView for paired devices
        ListView pairedListView = (ListView) findViewById(R.id.list_paired_bluetooth);
        pairedListView.setAdapter(pairedDevicesArrayAdapter);
        pairedListView.setOnItemClickListener(mDeviceClickListener);

        // Find and set up the ListView for newly discovered devices
        ListView newDevicesListView = (ListView) findViewById(R.id.list_unpaired_bluetooth);
        newDevicesListView.setAdapter(mNewDevicesArrayAdapter);
        newDevicesListView.setOnItemClickListener(mDeviceClickListener);

        // Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);

        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);

        // Get the local Bluetooth adapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // Get a set of currently paired devices
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        Log.i("Hello",pairedDevices.size()+"");

        // If there are paired devices, add each one to the ArrayAdapter
        if (pairedDevices.size() > 0) {
            findViewById(R.id.paired_devices).setVisibility(View.VISIBLE);
            for (BluetoothDevice device : pairedDevices) {
                if(device != null) {
                    bluetoothDeviceArrayList.add(device);
                    pairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                }


            }
        } else {
            String noDevices = getResources().getText(R.string.no_devices).toString();
            pairedDevicesArrayAdapter.add(noDevices);


        }

    }


    /*
     bluetooth on off switch.
    */
    private void bluetoothOnOff() {
        if (bluetoothAdapter == null) {
            Log.i("Error:", "Not working");
        }
        // if it is not enable then enable
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBtIntent);

            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mReceiver, BTIntent);
        }
        // if it is already enabled then disable it
        /* disabling the switch for a while
        if(bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.disable();

            IntentFilter BTIntent2 = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mReceiver, BTIntent2);
        }
        */
    }
}
