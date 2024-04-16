package com.example.starterandmagnetotester.ui.sparkplug

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SparkPlugViewModel : ViewModel() {
    private val _text = MutableLiveData<String>().apply {
        value = "This is spark plug fragment"
    }
    val text: LiveData<String> = _text
}