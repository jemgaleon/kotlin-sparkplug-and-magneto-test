package com.example.starterandmagnetotester.ui.settings

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.starterandmagnetotester.databinding.FragmentSettingsBinding
import com.example.starterandmagnetotester.services.BluetoothService
import com.example.starterandmagnetotester.R
import com.example.starterandmagnetotester.adapter.DeviceListAdapter
import com.example.starterandmagnetotester.data.Device
import com.example.starterandmagnetotester.viewmodels.SharedViewModel
import java.util.UUID

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val bluetoothService: BluetoothService by lazy {
        BluetoothService(this.requireActivity())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val settingsViewModel = ViewModelProvider(this).get(SettingsViewModel::class.java)
        val sharedViewModel = ViewModelProvider(this).get(SharedViewModel::class.java)

        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.buttonEnableBluetooth.setOnClickListener {
            if (bluetoothService.hasBluetoothPermission()) {
                bluetoothService.enableBluetooth()
            } else {
                bluetoothService.requestBluetoothPermission()
            }
        }

        binding.buttonGetPairedDevices.setOnClickListener {
            val pairedDevices: List<Device> = bluetoothService.getPairedDevices()

            val deviceArrayList: ArrayList<Device> = pairedDevices.toCollection(ArrayList())
            val adapter = DeviceListAdapter(this.requireActivity(), deviceArrayList)

            binding.listPairedDevices.adapter = adapter
            binding.listPairedDevices.setOnItemClickListener { _, _, position, _ ->
                val device: Device = adapter.getItem(position)
                val success = bluetoothService.connect(device)
                if (success) {
                    binding.textDeviceName.text = device.name
                    Toast.makeText(this.requireContext(), "Connected to ${device.name}", Toast.LENGTH_SHORT).show()
                } else {
                    binding.textDeviceName.text = ""
                }

                sharedViewModel.setConnectedDevice(device)
            }
        }

        settingsViewModel.text.observe(viewLifecycleOwner) {
            // TODO remove
        }

//        sharedViewModel.bluetoothService.observe(viewLifecycleOwner) {
//            bluetoothService = it
//        }

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
        bluetoothService.onActivityResult(requestCode, resultCode, data)
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d("LOCAL_", "onRequestPermissionsResult")
        bluetoothService.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}