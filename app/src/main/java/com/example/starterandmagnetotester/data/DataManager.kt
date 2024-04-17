package com.example.starterandmagnetotester.data

import android.bluetooth.BluetoothSocket

object DataManager {
    private var connectedDevice: Device? = null
    private var bluetoothSocket: BluetoothSocket? = null

    fun setConnectedDevice(value: Device?) {
        connectedDevice = value
    }

    fun setBluetoothSocket(value: BluetoothSocket?) {
        bluetoothSocket = value
    }

    fun getConnectedDevice(): Device? {
        return connectedDevice
    }

    fun getBluetoothSocket(): BluetoothSocket? {
        return bluetoothSocket
    }
}