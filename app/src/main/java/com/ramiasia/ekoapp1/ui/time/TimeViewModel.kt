package com.ramiasia.ekoapp1.ui.time

import android.bluetooth.*
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ramiasia.ekoapp1.communication.TimeBleDeviceManager

class TimeViewModel : ViewModel() {

    private val LOGTAG = TimeViewModel::class.java.simpleName

    //TODO: Make constructor arg through ViewModelProvider and set private
    var timeBleDeviceManager: TimeBleDeviceManager? = null
    private val bleGattCallback: BluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            Log.i(LOGTAG, "GATT services discovered from server.")
            //TODO: Set notifications up for time characteristic from 1805 service

        }

        override fun onConnectionStateChange(
                gatt: BluetoothGatt,
                status: Int,
                newState: Int
        ) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    _connectionState.postValue(BluetoothProfile.STATE_CONNECTED)
                    _deviceName.postValue(gatt.device.name)
                    Log.i(LOGTAG, "Connected to GATT server.")
                    Log.i(LOGTAG, "Attempting to start service discovery: " +
                            gatt.discoverServices())
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    _connectionState.postValue(BluetoothProfile.STATE_DISCONNECTED)
                    Log.i(LOGTAG, "Disconnected from GATT server.")
                }
            }
        }

        override fun onCharacteristicRead(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic,
                status: Int
        ) {
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> {
                    Log.d(LOGTAG, "Received characteristic: $characteristic")
                }
            }
        }
    }

    //LiveData
    private var _time: MutableLiveData<String> = MutableLiveData<String>("")
    var time: LiveData<String> = _time

    private var _connectionState: MutableLiveData<Int> = MutableLiveData<Int>(BluetoothProfile.STATE_DISCONNECTED)
    var connectionState: LiveData<Int> = _connectionState

    private var _deviceName: MutableLiveData<String> = MutableLiveData<String>("")
    var deviceName: LiveData<String> = _deviceName


    fun connect(bluetoothDevice: BluetoothDevice) {
        timeBleDeviceManager?.connect(bluetoothDevice, bleGattCallback)
    }


}
