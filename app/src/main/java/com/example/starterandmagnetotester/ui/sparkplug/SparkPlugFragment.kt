package com.example.starterandmagnetotester.ui.sparkplug

import android.annotation.SuppressLint
import android.bluetooth.BluetoothSocket
import android.os.Bundle
import android.os.CountDownTimer
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
//    private val binding get() = _binding!!

    private var bluetoothSocket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null
    private var inputStream: InputStream? = null
    private var isRunning: Boolean = false;

    private var totalTimeInMillis = 120000L;
    private var isTimerRunning = false

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bluetoothSocket = DataManager.getBluetoothSocket()
        outputStream = bluetoothSocket?.outputStream
        inputStream = bluetoothSocket?.inputStream
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSparkPlugBinding.inflate(inflater, container, false)
        val binding = _binding!!
        val root: View = binding.root

        // init countdown
        val countDownTimer = object : CountDownTimer(totalTimeInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                // Update the text view with the remaining time
                if (isTimerRunning) {
                    binding.textTimer.text = convertMillisToTime(millisUntilFinished)
                }
            }

            override fun onFinish() {
                isTimerRunning = false
                binding.textTimer.text = convertMillisToTime(totalTimeInMillis)
                this.cancel()
            }
        }

        binding.buttonToggle.setOnCheckedChangeListener { _, isChecked ->
            isRunning = isChecked

            binding.textResult.text = ""
            binding.textResult.visibility = if (isChecked) View.VISIBLE else View.GONE

            if (isChecked) {
                Log.d(logName, "Start test")
                write("S_START")

                // start countdown
                isTimerRunning = true
                countDownTimer.onTick(totalTimeInMillis)
                countDownTimer.start()
            } else {
                Log.d(logName, "Stop test")
                write("S_STOP")

                // stop countdown
                isTimerRunning = false
                binding.textTimer.text = convertMillisToTime(totalTimeInMillis)
                countDownTimer.cancel()
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


    private fun convertMillisToTime(millis: Long): String {
        val seconds = (millis / 1000) % 60
        val minutes = (millis / (1000 * 60)) % 60
        val hours = millis / (1000 * 60 * 60)

        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }
}