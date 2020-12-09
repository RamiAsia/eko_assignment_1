package com.ramiasia.ekoapp1.ui.time

import android.bluetooth.le.ScanResult
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ramiasia.ekoapp1.communication.TimeDeviceManager

class TimeViewModel : ViewModel() {


    //TODO: Make constructor arg through ViewModelProvider and set private
    var timeDeviceManager: TimeDeviceManager? = null

    private var _time: MutableLiveData<String> = MutableLiveData<String>("")
    var time: LiveData<String> = _time

    fun getTime() {
        _time.value = "Test time"
    }

    fun connect(scanResult: ScanResult) {
        timeDeviceManager?.connect(scanResult)
    }


}
