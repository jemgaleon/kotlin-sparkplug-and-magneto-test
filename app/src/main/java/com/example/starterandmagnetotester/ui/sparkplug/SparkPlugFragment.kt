package com.example.starterandmagnetotester.ui.sparkplug

import android.annotation.SuppressLint
import android.bluetooth.BluetoothSocket
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.starterandmagnetotester.data.DataManager
import com.example.starterandmagnetotester.databinding.FragmentSparkPlugBinding
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class SparkPlugFragment : Fragment() {
    private val logName: String = "SPARK_PLUG_LOGS"

    private var _binding: FragmentSparkPlugBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var bluetoothSocket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null
    private var inputStream: InputStream? = null

    private var isRunning: Boolean = false;

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bluetoothSocket = DataManager.getBluetoothSocket()

        if (bluetoothSocket?.isConnected == false) {
            bluetoothSocket?.connect()
        }

        outputStream = bluetoothSocket?.outputStream
        inputStream = bluetoothSocket?.inputStream
    }

    override fun onDestroy() {
        super.onDestroy()

        if (bluetoothSocket?.isConnected == true) {
            bluetoothSocket?.close()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSparkPlugBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.buttonToggle.setOnCheckedChangeListener { _, isChecked ->
            isRunning = isChecked

            binding.textResult.text = "-"

            if (isChecked) {
                Log.d(logName, "Start test")
                write("S_START")
            } else {
                Log.d(logName, "Stop test")
                write("S_STOP")
            }
        }
        binding.buttonToggle.isEnabled = bluetoothSocket != null

        read{
            if (it == "P") {
                binding.textResult.text = "PASS"
            }

            if (it == "F") {
                binding.textResult.text = "FAIL"
            }

            if (!isRunning) {
                binding.textResult.text = "-"
            }
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

    /**
     * Notes:
     * - Results from arduino is expected to be character 'P' (Pass) or 'F' (Fail)
     * - Data is received every 2s
     */
    private fun read(callback: (String) -> Unit) {
        if (inputStream != null) {
            Thread(Runnable {
                try {
                    var receivedData = ByteArray(1 * 4) // Fixed char length "P|F" * 4
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