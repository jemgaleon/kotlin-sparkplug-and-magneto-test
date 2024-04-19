package com.example.starterandmagnetotester.ui.magneto

import android.annotation.SuppressLint
import android.bluetooth.BluetoothSocket
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.starterandmagnetotester.data.DataManager
import com.example.starterandmagnetotester.databinding.FragmentMagnetoBinding
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class MagnetoFragment : Fragment() {
    private val logName: String = "MAGNETO_LOGS"

    private var _binding: FragmentMagnetoBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
//    private val binding get() = _binding!!

    private var bluetoothSocket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null
    private var inputStream: InputStream? = null
    private var isRunning: Boolean = false

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bluetoothSocket = DataManager.getBluetoothSocket()
        outputStream = bluetoothSocket?.outputStream
        inputStream = bluetoothSocket?.inputStream
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMagnetoBinding.inflate(inflater, container, false)
        val binding = _binding!!
        val root: View = binding.root

        binding.buttonToggle.setOnCheckedChangeListener { _, isChecked ->
            isRunning = isChecked

            binding.textResult.text = "0"
            binding.textResult.visibility = if (isChecked) View.VISIBLE else View.GONE

            if (isChecked) {
                Log.d(logName, "Start test")
                write("M_START")
            } else {
                Log.d(logName, "Stop test")
                write("M_STOP")
            }
        }
        binding.buttonToggle.isEnabled = bluetoothSocket != null


        read{
            binding.textResult.text = it
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun write(data: String) {
        if (outputStream != null) {
            try {
                val dataBytes = "${data}\n".toByteArray(Charsets.UTF_8)
                outputStream?.write(dataBytes)
                outputStream?.flush()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun read(callback: (String) -> Unit) {
        if (inputStream != null) {
            Thread(Runnable {
                try {
                    var receivedData = ByteArray(5 * 4) // Fixed char length 5 digits * 4
                    var bytesRead: Int

                    while (true) {
                        bytesRead = inputStream!!.read(receivedData)
                        Log.d(logName, "Raw: ${bytesRead}")

                        if (bytesRead > 0) {
                            val data = String(receivedData, 0, bytesRead, Charsets.UTF_8)

                            Log.d(logName, "Data ${data}")
                            callback(data)
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }).start()
        }
    }
}