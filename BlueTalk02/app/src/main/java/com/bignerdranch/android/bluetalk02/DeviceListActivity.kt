package com.bignerdranch.android.bluetalk02

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.core.app.ActivityCompat

class DeviceListActivity : AppCompatActivity() {
    private lateinit var listPairedDevices: ListView
    private lateinit var listAvailableDevices: ListView
    private lateinit var progressScanDevices: ProgressBar
    private lateinit var adapterPairedDevices: ArrayAdapter<String>
    private lateinit var adapterAvailableDevices: ArrayAdapter<String>
    private lateinit var context: Context
    private lateinit var bluetoothAdapter: BluetoothAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_list)
        context = this
        init()
    }

    @SuppressLint("MissingPermission")
    private fun init() {
        listPairedDevices = findViewById(R.id.list_paired_devices)
        listAvailableDevices = findViewById(R.id.list_available_devices)
        progressScanDevices = findViewById(R.id.progress_scan_devices)
        adapterPairedDevices = ArrayAdapter<String>(context, R.layout.device_list_item)
        adapterAvailableDevices = ArrayAdapter<String>(context, R.layout.device_list_item)
        listPairedDevices!!.adapter = adapterPairedDevices
        listAvailableDevices!!.adapter = adapterAvailableDevices
        listAvailableDevices!!.setOnItemClickListener(object : AdapterView.OnItemClickListener {
            override fun onItemClick(adapterView: AdapterView<*>?, view: View, i: Int, l: Long) {
                val info: String = (view as TextView).getText().toString()
                val address = info.substring(info.length - 17)
                val intent = Intent()
                intent.putExtra("deviceAddress", address)
                setResult(RESULT_OK, intent)
                finish()
            }
        })
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        val pairedDevices: Set<BluetoothDevice> = bluetoothAdapter.getBondedDevices()
        if (pairedDevices != null && pairedDevices.size > 0) {
            for (device in pairedDevices) {
                adapterPairedDevices.add(device.getName() + "\n" + device.getAddress())
            }
        }
        val intentFilter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(bluetoothDeviceListener, intentFilter)
        val intentFilter1 = IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        registerReceiver(bluetoothDeviceListener, intentFilter1)
        listPairedDevices!!.setOnItemClickListener(object : AdapterView.OnItemClickListener {
            override fun onItemClick(adapterView: AdapterView<*>?, view: View, i: Int, l: Long) {
                bluetoothAdapter.cancelDiscovery()
                val info: String = (view as TextView).getText().toString()
                val address = info.substring(info.length - 17)
                Log.d("Address", address)
                val intent = Intent()
                intent.putExtra("deviceAddress", address)
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        })
    }

    private val bluetoothDeviceListener: BroadcastReceiver? = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action: String = intent.getAction().toString()
            if (BluetoothDevice.ACTION_FOUND == action) {
                val device: BluetoothDevice? =
                    intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return
                }
                if (device!!.getBondState() != BluetoothDevice.BOND_BONDED) {
                    adapterAvailableDevices.add(device.getName() + "\n" + device.getAddress())
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED == action) {
                progressScanDevices.setVisibility(View.GONE)
                if (adapterAvailableDevices.getCount() == 0) {
                    Toast.makeText(context, "No new devices found", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(
                        context,
                        "Click on the device to start the chat",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        getMenuInflater().inflate(R.menu.menu_device_list, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_scan_devices -> {
                scanDevices()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun scanDevices() {
        progressScanDevices.setVisibility(View.VISIBLE)
        adapterAvailableDevices.clear()
        Toast.makeText(context, "Scan started", Toast.LENGTH_SHORT).show()
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery()
        }
        bluetoothAdapter.startDiscovery()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (bluetoothDeviceListener != null) {
            unregisterReceiver(bluetoothDeviceListener)
        }
    }
}