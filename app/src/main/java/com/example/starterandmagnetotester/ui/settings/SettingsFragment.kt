package com.example.starterandmagnetotester.ui.settings

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.starterandmagnetotester.databinding.FragmentSettingsBinding
import com.example.starterandmagnetotester.services.BluetoothService
import com.example.starterandmagnetotester.adapter.DeviceListAdapter
import com.example.starterandmagnetotester.data.DataManager
import com.example.starterandmagnetotester.data.Device

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

                val bluetoothSocket = bluetoothService.connect(device)

                if (bluetoothSocket != null) {
                    binding.textDeviceName.text = device.name
                    Toast.makeText(this.requireContext(), "Connected to ${device.name}", Toast.LENGTH_SHORT).show()
                } else {
                    binding.textDeviceName.text = ""
                }

                // mimic global state; this is not the recommended approach
                DataManager.setConnectedDevice(device)
                DataManager.setBluetoothSocket(bluetoothService.bluetoothSocket)
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
        bluetoothService.onActivityResult(requestCode, resultCode, data)
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d("LOCAL_", "onRequestPermissionsResult")
        bluetoothService.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}