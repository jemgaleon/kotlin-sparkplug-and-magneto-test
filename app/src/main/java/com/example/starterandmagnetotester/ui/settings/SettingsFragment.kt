package com.example.starterandmagnetotester.ui.settings

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
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.starterandmagnetotester.R
import com.example.starterandmagnetotester.adapter.DeviceListAdapter
import com.example.starterandmagnetotester.data.DataManager
import com.example.starterandmagnetotester.data.Device
import com.example.starterandmagnetotester.databinding.FragmentSettingsBinding
import java.io.IOException
import java.util.UUID

class SettingsFragment : Fragment() {
    private val logName: String = "SETTINGS_LOGS"
    private val uuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    private var _binding: FragmentSettingsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var bluetoothManager: BluetoothManager? = null
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothSocket: BluetoothSocket? = null
    private var connectedDevice: BluetoothDevice? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bluetoothManager = this.requireActivity().getSystemService(ComponentActivity.BLUETOOTH_SERVICE) as BluetoothManager
        if (bluetoothManager != null) {
            bluetoothAdapter = bluetoothManager?.adapter
        } else {
            Log.d(logName, "Bluetooth is not supported")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.buttonEnableBluetooth.setOnClickListener {
            if (hasBluetoothPermission()) {
                enableBluetooth()
            } else {
                requestBluetoothPermission()
            }
        }

        binding.buttonGetPairedDevices.setOnClickListener {
            val pairedDevices: List<Device> = getPairedDevices()

            val deviceArrayList: ArrayList<Device> = pairedDevices.toCollection(ArrayList())
            val adapter = DeviceListAdapter(this.requireActivity(), deviceArrayList)

            binding.listPairedDevices.adapter = adapter
            binding.listPairedDevices.setOnItemClickListener { _, _, position, _ ->
                val device: Device = adapter.getItem(position)

                val bluetoothSocket = connect(device)

                if (bluetoothSocket != null) {
                    binding.textDeviceName.text = device.name
                    Toast.makeText(this.requireContext(), "Connected to ${device.name}", Toast.LENGTH_SHORT).show()
                } else {
                    binding.textDeviceName.text = ""
                }

                // mimic global state; this is not the recommended approach
                DataManager.setConnectedDevice(device)
                DataManager.setBluetoothSocket(bluetoothSocket)
            }
        }

        binding.textDeviceName.text = DataManager.getConnectedDevice()?.name

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("LOCAL_", "onActivityResult")

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

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d("LOCAL_", "onRequestPermissionsResult")

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

    private fun hasBluetoothPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return (ContextCompat.checkSelfPermission(
                this.requireActivity(),
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED)
        } else {
            Log.d(logName, "Bluetooth is not supported")
            return false;
        }
    }

    @SuppressLint("InlinedApi")
    fun requestBluetoothPermission() {
        ActivityCompat.requestPermissions(
            this.requireActivity(),
            arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
            R.integer.request_code_bt_permission
        )
    }

    @SuppressLint("MissingPermission")
    fun enableBluetooth() {
        if (bluetoothAdapter?.isEnabled == true) {
            Log.d(logName, "Bluetooth is already enabled")
            return;
        }

        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)

        this.requireActivity().startActivityForResult(enableBtIntent, R.integer.request_code_bt_enable)
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
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
    }
}