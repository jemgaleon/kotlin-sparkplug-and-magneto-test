package com.example.starterandmagnetotester.services

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID
import com.example.starterandmagnetotester.R
import com.example.starterandmagnetotester.data.Device

class BluetoothService(
    private val activity: FragmentActivity,
) {
    /**
     * The default well-known SSP UUID
     */
    private val uuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private val logName: String = "BT_LOGS"

    private var bluetoothManager: BluetoothManager? = null
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var connectedDevice: BluetoothDevice? = null
    /**
     * Not: Exposed this for now. It will be later saved to DataManager as global state
     * TODO find a way to handle global states as this is not a good practice as per docs
     */
    var bluetoothSocket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null
    private var inputStream: InputStream? = null

    init {
        bluetoothManager = activity.getSystemService(ComponentActivity.BLUETOOTH_SERVICE) as BluetoothManager
        if (bluetoothManager != null) {
            bluetoothAdapter = bluetoothManager?.adapter
        } else {
            Log.d(logName, "Bluetooth is not supported")
        }
    }

    fun hasBluetoothPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return (ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED)
        } else {
            Log.d(logName, "Bluetooth is not supported")
            return false;
        }
    }

    @SuppressLint("InlinedApi")
    fun requestBluetoothPermission() {
        requestPermissions(activity, arrayOf(Manifest.permission.BLUETOOTH_CONNECT), R.integer.request_code_bt_permission)
    }

    @SuppressLint("MissingPermission")
    fun enableBluetooth() {
        if (bluetoothAdapter?.isEnabled == true) {
            Log.d(logName, "Bluetooth is already enabled")
            return;
        }

        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)

        activity.startActivityForResult(enableBtIntent, R.integer.request_code_bt_enable)
    }


    @SuppressLint("MissingPermission")
    fun getPairedDevices(): List<Device> {
        val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices

        return pairedDevices
//            ?.filter { pairedDevice ->
//                pairedDevice.name.indexOf("HC-") > -1
//            }
            ?.map { pairedDevice ->
                Device(pairedDevice.name, pairedDevice.address)
            } ?: emptyList()
    }

    @SuppressLint("MissingPermission")
    fun connect(device: Device): BluetoothSocket? {
        try {
            // get device
            connectedDevice = bluetoothAdapter?.getRemoteDevice(device.macAddress)

            if (connectedDevice == null) {
                Log.d(logName, "Bluetooth device is not found")
                return null
            }

            if (bluetoothSocket?.isConnected == true) {
                bluetoothSocket?.close()
                Log.d(logName, "Close existing connection")
            }

            bluetoothSocket = connectedDevice!!.createInsecureRfcommSocketToServiceRecord(uuid)
            Log.d(logName, "Create new socket")

            bluetoothSocket?.connect()
            Log.d(logName, "Bluetooth device is now connected")

            return bluetoothSocket
//            outputStream = _bluetoothSocket?.outputStream
//            inputStream = _bluetoothSocket?.inputStream
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
    }

//    fun disconnect() {
//        if (outputStream != null) {
//            try {
//                outputStream!!.close()
//                outputStream = null
//            } catch (e: IOException) {
//                e.printStackTrace()
//            }
//        }
//        if (inputStream != null) {
//            try {
//                inputStream!!.close()
//                inputStream = null
//            } catch (e: IOException) {
//                e.printStackTrace()
//            }
//        }
//        connectedDevice = null
//    }

//    fun write(data: String) {
//        if (outputStream != null) {
//            try {
//                outputStream!!.write(data.toByteArray())
//                outputStream!!.flush()
//            } catch (e: IOException) {
//                e.printStackTrace()
//            }
//        }
//    }

//    fun read(callback: (String) -> Unit) {
//        if (inputStream != null) {
//            Thread(Runnable {
//                val receivedData = ByteArray(1024)
//                var bytesRead: Int
//                while (true) {
//                    try {
//                        bytesRead = inputStream!!.read(receivedData)
//                        if (bytesRead > 0) {
//                            val data = String(receivedData, 0, bytesRead)
//                            callback(data)
//                        }
//                    } catch (e: IOException) {
//                        break
//                    }
//                }
//            }).start()
//        }
//    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            R.integer.request_code_bt_enable -> {
                if (resultCode == Activity.RESULT_OK) {
                    Log.d(logName, "Bluetooth is now enabled")
                } else {
                    Log.d(logName, "Bluetooth is required")
                }
            }
        }
    }

    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            R.integer.request_code_bt_enable -> {
                if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    Log.d(logName, "Bluetooth permission granted")
                } else {
                    Log.d(logName, "Bluetooth is required")
                }
            }
        }
    }
}