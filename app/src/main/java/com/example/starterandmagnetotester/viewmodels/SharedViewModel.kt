package com.example.starterandmagnetotester.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.starterandmagnetotester.data.Device
import com.example.starterandmagnetotester.services.BluetoothService

class SharedViewModel : ViewModel() {
    private  val _connectedDevice = MutableLiveData<Device>()

    val connectedDevice: LiveData<Device> = _connectedDevice

    fun setConnectedDevice(newValue: Device) {
        _connectedDevice.value = newValue
    }
}