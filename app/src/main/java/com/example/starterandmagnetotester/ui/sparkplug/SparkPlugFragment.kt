package com.example.starterandmagnetotester.ui.sparkplug

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import com.example.starterandmagnetotester.databinding.FragmentSparkPlugBinding

class SparkPlugFragment : Fragment() {

    private var _binding: FragmentSparkPlugBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val sparkPlugViewModel = ViewModelProvider(this).get(SparkPlugViewModel::class.java)

        _binding = FragmentSparkPlugBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textSparkPlug

        sparkPlugViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}