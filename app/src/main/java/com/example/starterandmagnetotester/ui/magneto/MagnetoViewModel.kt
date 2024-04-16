package com.example.starterandmagnetotester.ui.magneto

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MagnetoViewModel : ViewModel() {
    private val _text = MutableLiveData<String>().apply {
        value = "This is magneto fragment"
    }
    val text: LiveData<String> = _text
}