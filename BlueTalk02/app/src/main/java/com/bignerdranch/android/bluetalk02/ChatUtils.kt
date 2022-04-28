package com.androidtut.qaifi.bluetoothchatapp

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.core.app.ActivityCompat
import com.bignerdranch.android.bluetalk02.MainActivity
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*

class ChatUtils(private val context: Context, private val handler: Handler) {
    private val bluetoothAdapter: BluetoothAdapter
    private var connectThread: ConnectThread? = null
    private var acceptThread: AcceptThread? = null
    private var connectedThread: ConnectedThread? = null
    private val APP_UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66")
    private val APP_NAME = "BluetoothChatApp"
    private var status: Int
        get() {
            return status
        }
        @Synchronized
        set(value) {
            this.status = value
            handler.obtainMessage(MainActivity.MESSAGE_STATE_CHANGED, value, -1).sendToTarget()
        }

    val STATE_NONE = 0
    val STATE_LISTEN = 1
    val STATE_CONNECTING = 2
    val STATE_CONNECTED = 3

    fun getState(): Int {
        return status
    }

//    @Synchronized
//    fun setState(state: Int) {
//        this.status = state
//        handler.obtainMessage(MainActivity.MESSAGE_STATE_CHANGED, state, -1).sendToTarget()
//    }

    @Synchronized
    private fun start() {
        if (connectThread != null) {
            connectThread!!.cancel()
            connectThread = null
        }
        if (acceptThread == null) {
            acceptThread = AcceptThread()
            acceptThread!!.start()
        }
        if (connectedThread != null) {
            connectedThread!!.cancel()
            connectedThread = null
        }
        status=STATE_LISTEN
    }

    @Synchronized
    fun stop() {
        if (connectThread != null) {
            connectThread!!.cancel()
            connectThread = null
        }
        if (acceptThread != null) {
            acceptThread!!.cancel()
            acceptThread = null
        }
        if (connectedThread != null) {
            connectedThread!!.cancel()
            connectedThread = null
        }
        status = STATE_NONE
    }

    fun connect(device: BluetoothDevice) {
        if (status == STATE_CONNECTING) {
            connectThread!!.cancel()
            connectThread = null
        }
        connectThread = ConnectThread(device)
        connectThread!!.start()
        if (connectedThread != null) {
            connectedThread!!.cancel()
            connectedThread = null
        }
        status = STATE_CONNECTING
    }

    fun write(buffer: ByteArray?) {
        var connThread: ConnectedThread?
        synchronized(this) {
            if (status != STATE_CONNECTED) {
                return
            }
            connThread = connectedThread
        }
        connThread!!.write(buffer)
    }

    private inner class AcceptThread : Thread() {
        private val serverSocket: BluetoothServerSocket?
        override fun run() {
            var socket: BluetoothSocket? = null
            try {
                socket = serverSocket!!.accept()
            } catch (e: IOException) {
                Log.e("Accept->Run", e.toString())
                try {
                    serverSocket!!.close()
                } catch (e1: IOException) {
                    Log.e("Accept->Close", e.toString())
                }
            }
            if (socket != null) {
                when (status) {
                    STATE_LISTEN, STATE_CONNECTING -> connected(socket, socket.remoteDevice)
                    STATE_NONE, STATE_CONNECTED -> try {
                        socket.close()
                    } catch (e: IOException) {
                        Log.e("Accept->CloseSocket", e.toString())
                    }
                }
            }
        }

        fun cancel() {
            try {
                serverSocket!!.close()
            } catch (e: IOException) {
                Log.e("Accept->CloseServer", e.toString())
            }
        }

        init {
            var tmp: BluetoothServerSocket? = null
            try {
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
                    null
                }
                tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord(APP_NAME, APP_UUID)
            } catch (e: IOException) {
                Log.e("Accept->Constructor", e.toString())
            }
            serverSocket = tmp
        }
    }

    private inner class ConnectThread(private val device: BluetoothDevice) : Thread() {
        private val socket: BluetoothSocket?
        override fun run() {
            try {
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
                socket!!.connect()
            } catch (e: IOException) {
                Log.e("Connect->Run", e.toString())
                try {
                    socket!!.close()
                } catch (e1: IOException) {
                    Log.e("Connect->CloseSocket", e.toString())
                }
                connectionFailed()
                return
            }
            synchronized(this@ChatUtils) { connectThread = null }
            connected(socket, device)
        }

        fun cancel() {
            try {
                socket!!.close()
            } catch (e: IOException) {
                Log.e("Connect->Cancel", e.toString())
            }
        }

        init {
            var tmp: BluetoothSocket? = null
            try {
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
                    null
                }
                tmp = device.createRfcommSocketToServiceRecord(APP_UUID)
            } catch (e: IOException) {
                Log.e("Connect->Constructor", e.toString())
            }
            socket = tmp
        }
    }

    private inner class ConnectedThread(private val socket: BluetoothSocket?) : Thread() {
        private val inputStream: InputStream?
        private val outputStream: OutputStream?
        override fun run() {
            val buffer = ByteArray(1024)
            val bytes: Int
            try {
                bytes = inputStream!!.read(buffer)
                handler.obtainMessage(MainActivity.MESSAGE_READ, bytes, -1, buffer).sendToTarget()
            } catch (e: IOException) {
                connectionLost()
            }
        }

        fun write(buffer: ByteArray?) {
            try {
                outputStream!!.write(buffer)
                handler.obtainMessage(MainActivity.MESSAGE_WRITE, -1, -1, buffer).sendToTarget()
            } catch (e: IOException) {
            }
        }

        fun cancel() {
            try {
                socket!!.close()
            } catch (e: IOException) {
            }
        }

        init {
            var tmpIn: InputStream? = null
            var tmpOut: OutputStream? = null
            try {
                tmpIn = socket!!.inputStream
                tmpOut = socket.outputStream
            } catch (e: IOException) {
            }
            inputStream = tmpIn
            outputStream = tmpOut
        }
    }

    private fun connectionLost() {
        val message = handler.obtainMessage(MainActivity.MESSAGE_TOAST)
        val bundle = Bundle()
        bundle.putString(MainActivity.TOAST, "Connection Lost")
        message.data = bundle
        handler.sendMessage(message)
        start()
    }

    @Synchronized
    private fun connectionFailed() {
        val message = handler.obtainMessage(MainActivity.MESSAGE_TOAST)
        val bundle = Bundle()
        bundle.putString(MainActivity.TOAST, "Cant connect to the device")
        message.data = bundle
        handler.sendMessage(message)
        start()
    }

    @Synchronized
    private fun connected(socket: BluetoothSocket?, device: BluetoothDevice) {
        if (connectThread != null) {
            connectThread!!.cancel()
            connectThread = null
        }
        if (connectedThread != null) {
            connectedThread!!.cancel()
            connectedThread = null
        }
        connectedThread = ConnectedThread(socket)
        connectedThread!!.start()
        val message = handler.obtainMessage(MainActivity.MESSAGE_DEVICE_NAME)
        val bundle = Bundle()
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
        bundle.putString(MainActivity.DEVICE_NAME, device.name)
        message.data = bundle
        handler.sendMessage(message)
        status = STATE_CONNECTED
    }

    companion object {
        const val STATE_NONE = 0
        const val STATE_LISTEN = 1
        const val STATE_CONNECTING = 2
        const val STATE_CONNECTED = 3
    }

    init {
        status =  STATE_NONE
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    }
}