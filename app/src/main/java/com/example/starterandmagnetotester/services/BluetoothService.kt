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
import android.widget.Toast
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
    private val uuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") // default well-known SSP UUID

    private var bluetoothManager: BluetoothManager? = null
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var connectedDevice: BluetoothDevice? = null
    private var bluetoothSocket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null
    private var inputStream: InputStream? = null

    init {
        bluetoothManager = activity.getSystemService(ComponentActivity.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager?.adapter
    }

    fun hasBluetoothPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return (ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED)
        }

        return false;
    }

    fun requestBluetoothPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestPermissions(activity, arrayOf(Manifest.permission.BLUETOOTH_CONNECT), R.integer.request_code_bt_permission)
        }
    }

    @SuppressLint("MissingPermission")
    fun enableBluetooth() {
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
    fun connect(device: Device): Boolean {
        connectedDevice = bluetoothAdapter?.getRemoteDevice(device.macAddress)
        if (connectedDevice == null) {
            return false
        }

        try {
            bluetoothSocket = connectedDevice!!.createInsecureRfcommSocketToServiceRecord(uuid)
            bluetoothSocket?.connect()
            outputStream = bluetoothSocket?.outputStream
            inputStream = bluetoothSocket?.inputStream
            return true
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        }
    }

    fun disconnect() {
        if (outputStream != null) {
            try {
                outputStream!!.close()
                outputStream = null
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        if (inputStream != null) {
            try {
                inputStream!!.close()
                inputStream = null
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        connectedDevice = null
    }

    fun write(data: String) {
        if (outputStream != null) {
            try {
                outputStream!!.write(data.toByteArray())
                outputStream!!.flush()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun startListeningThread(callback: (String) -> Unit) {
        if (inputStream != null) {
            Thread(Runnable {
                val receivedData = ByteArray(1024)
                var bytesRead: Int
                while (true) {
                    try {
                        bytesRead = inputStream!!.read(receivedData)
                        if (bytesRead > 0) {
                            val data = String(receivedData, 0, bytesRead)
                            callback(data)
                        }
                    } catch (e: IOException) {
                        break
                    }
                }
            }).start()
        }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            R.integer.request_code_bt_enable -> {
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(activity, "Bluetooth is enabled", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(activity, "Bluetooth is required", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            R.integer.request_code_bt_enable -> {
                if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    Toast.makeText(activity, "Bluetooth permission granted", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(activity, "Bluetooth is required", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}