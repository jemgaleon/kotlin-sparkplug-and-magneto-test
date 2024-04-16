package com.example.starterandmagnetotester.adapter

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.starterandmagnetotester.data.Device
import com.example.starterandmagnetotester.databinding.AdapterDeviceListBinding

class DeviceListAdapter (context: Activity, items: ArrayList<Device>): BaseAdapter() {

    private lateinit var binding: AdapterDeviceListBinding
    private val context: Activity
    private val items: ArrayList<Device>

    override fun getCount(): Int {
        return items.size
    }

    override fun getItem(position: Int): Device {
        return items[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        binding = AdapterDeviceListBinding.inflate(context.layoutInflater)

        val view = binding.root
        val nameTextView: TextView = binding.name
        val macAddressTextView: TextView = binding.macAddress

        val currentItem = getItem(position) as Device
        nameTextView.text = currentItem.name
        macAddressTextView.text = currentItem.macAddress

        return view
    }

    init {
        this.context = context
        this.items = items
    }
}