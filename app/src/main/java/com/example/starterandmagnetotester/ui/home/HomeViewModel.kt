package com.example.starterandmagnetotester.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.starterandmagnetotester.R.string

class HomeViewModel : ViewModel() {

    private val _textAppName = MutableLiveData<String>().apply {
        value = "Spark Plug and Magneto Tester"
    }

    private val _textAppVersion = MutableLiveData<String>().apply {
        value = "Version 1.0"
    }

    val textAppName: LiveData<String> = _textAppName
    val textAppVersion: LiveData<String> = _textAppVersion
}