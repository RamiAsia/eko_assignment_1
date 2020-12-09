package com.ramiasia.ekoapp1.ui

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ramiasia.ekoapp1.communication.TimeBleDeviceManager

/**
 * ViewModel for Time related data. Middle layer between our data model and
 */
class TimeViewModel(private val timeBleDeviceManager: TimeBleDeviceManager? = null) : ViewModel() {

    //LiveData
    var time: LiveData<String>? = timeBleDeviceManager?.time
    var connectionState: LiveData<Int>? = timeBleDeviceManager?.connectionState
    var deviceName: LiveData<String>? = timeBleDeviceManager?.deviceName


    /**
     * Connect to a [BluetoothDevice].
     *
     * @param bluetoothDevice Device to connect to.
     */
    fun connect(bluetoothDevice: BluetoothDevice) {
        timeBleDeviceManager?.connect(bluetoothDevice)
    }

    /**
     * Requests time from [TimeBleDeviceManager].
     */
    fun requestTime() {
        timeBleDeviceManager?.requestTime()
    }

    /**
     * Overridden method to ensure we properly release resources when done with the [TimeViewModel]
     */
    override fun onCleared() {
        timeBleDeviceManager?.onDestroy()
        super.onCleared()
    }
}

/**
 * Factory for [TimeViewModel], providing the required [TimeBleDeviceManager].
 */
class TimeViewModelFactory(private val timeBleDeviceManager: TimeBleDeviceManager) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(TimeBleDeviceManager::class.java)
            .newInstance(timeBleDeviceManager)
    }
}
