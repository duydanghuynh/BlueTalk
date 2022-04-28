package com.bignerdranch.android.bluetalk02

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.annotation.NonNull
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.androidtut.qaifi.bluetoothchatapp.ChatUtils
import android.app.AlertDialog.Builder

class MainActivity : AppCompatActivity() {
    private lateinit var context: Context
    private lateinit var  bluetoothAdapter: BluetoothAdapter
    private lateinit var  chatUtils: ChatUtils
    private lateinit var  listMainChat: ListView
    private lateinit var  edCreateMessage: EditText
    private lateinit var  btnSendMessage: Button
    private lateinit var  adapterMainChat: ArrayAdapter<String>
    private val LOCATION_PERMISSION_REQUEST = 101
    private val SELECT_DEVICE = 102
    private var connectedDevice: String? = null

    private val handler = Handler { message ->
        when (message.what) {
            MESSAGE_STATE_CHANGED -> when (message.arg1) {
                ChatUtils.STATE_NONE -> setState("Not Connected")
                ChatUtils.STATE_LISTEN -> setState("Not Connected")
                ChatUtils.STATE_CONNECTING -> setState("Connecting...")
                ChatUtils.STATE_CONNECTED -> setState("Connected: $connectedDevice")
            }
            MESSAGE_WRITE -> {
                val buffer1 = message.obj as ByteArray
                val outputBuffer = String(buffer1)
                adapterMainChat!!.add("Me: $outputBuffer")
            }
            MESSAGE_READ -> {
                val buffer = message.obj as ByteArray
                val inputBuffer = String(buffer, 0, message.arg1)
                adapterMainChat!!.add("$connectedDevice: $inputBuffer")
            }
            MESSAGE_DEVICE_NAME -> {
                connectedDevice = message.data.getString(DEVICE_NAME)
                Toast.makeText(context, connectedDevice, Toast.LENGTH_SHORT).show()
            }
            MESSAGE_TOAST -> Toast.makeText(
                context, message.data.getString(
                    TOAST
                ), Toast.LENGTH_SHORT
            ).show()
        }
        false
    }

    private fun setState(subTitle: CharSequence) {
        supportActionBar!!.subtitle = subTitle
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        context = this
        init()
        initBluetooth()
        chatUtils = ChatUtils(this, handler)
    }

    private fun init() {
        listMainChat = findViewById(R.id.list_conversation)
        edCreateMessage = findViewById(R.id.ed_enter_message)
        btnSendMessage = findViewById(R.id.btn_send_msg)
        adapterMainChat = ArrayAdapter(context!!, R.layout.message_layout)
        listMainChat.adapter= adapterMainChat
        btnSendMessage.setOnClickListener(View.OnClickListener {
            val message = edCreateMessage.getText().toString()
            if (!message.isEmpty()) {
                edCreateMessage.setText("")
                chatUtils?.write(message.toByteArray())
            }
        })
    }

    private fun initBluetooth() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null) {
            Toast.makeText(context, "No bluetooth found", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main_activity, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_search_devices -> {
                checkPermissions()
                true
            }
            R.id.menu_enable_bluetooth -> {
                enableBluetooth()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) !== PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this@MainActivity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST
            )
        } else {
            val intent = Intent(context, DeviceListActivity::class.java)
            startActivityForResult(intent, SELECT_DEVICE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == SELECT_DEVICE && resultCode == RESULT_OK) {
            val address = data!!.getStringExtra("deviceAddress")
            chatUtils.connect(bluetoothAdapter!!.getRemoteDevice(address))
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        @NonNull permissions: Array<String>,
        @NonNull grantResults: IntArray
    ) {
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val intent = Intent(context, DeviceListActivity::class.java)
                startActivityForResult(intent, SELECT_DEVICE)
            } else {
                Builder(context)
                    .setCancelable(false)
                    .setMessage("Location permission is required.\n Please grant")
                    .setPositiveButton(
                        "Grant",
                        DialogInterface.OnClickListener { dialogInterface, i -> checkPermissions() })
                    .setNegativeButton(
                        "Deny",
                        DialogInterface.OnClickListener { dialogInterface, i -> finish() }).show()
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun enableBluetooth() {
        if (!this.bluetoothAdapter!!.isEnabled) {
            if (ActivityCompat.checkSelfPermission(
                    this,
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
            bluetoothAdapter!!.enable()
        }
        if (bluetoothAdapter!!.scanMode != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            val discoveryIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
            discoveryIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
            startActivity(discoveryIntent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (chatUtils != null) {
            chatUtils!!.stop()
        }
    }

    companion object {
        const val MESSAGE_STATE_CHANGED = 0
        const val MESSAGE_READ = 1
        const val MESSAGE_WRITE = 2
        const val MESSAGE_DEVICE_NAME = 3
        const val MESSAGE_TOAST = 4
        const val DEVICE_NAME = "deviceName"
        const val TOAST = "toast"
    }
}