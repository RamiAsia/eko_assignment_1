package com.ramiasia.ekoapp1.communication

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.le.ScanResult
import android.companion.AssociationRequest
import android.companion.BluetoothDeviceFilter
import android.companion.CompanionDeviceManager
import android.content.Context
import java.util.regex.Pattern

class TimeDeviceManager(
        private val context: Context?,
        private val deviceManager: CompanionDeviceManager?,
        private val bluetoothAdapter: BluetoothAdapter?,
        private val callBack: CompanionDeviceManager.Callback
) {

//    private var bleGatt: BluetoothGatt? = null
    private var bluetoothDevice: BluetoothDevice? = null



    fun scan() {
        val deviceFilter: BluetoothDeviceFilter = BluetoothDeviceFilter.Builder()
                //Name being used for now since the Time UUIDs don't seem to be working
                .setNamePattern(Pattern.compile("Time"))
                //TODO: Find why Time service UUIDs cause 0 scan results, despite confirming these are the UUIDs associated with each service from the virtual BLE device
//                .addServiceUuid(ParcelUuid.fromString("00001805-0000-1000-8000-00805F9B34FB"), null)
//                .addServiceUuid(ParcelUuid.fromString("00001806-0000-1000-8000-00805F9B34FB"), null)
//                .addServiceUuid(ParcelUuid.fromString("00001807-0000-1000-8000-00805F9B34FB"), null)
                .build()

        val pairingRequest: AssociationRequest = AssociationRequest.Builder()
                .addDeviceFilter(deviceFilter)
                .setSingleDevice(false)
                .build()

        deviceManager?.associate(pairingRequest, callBack, null)
    }

    fun connect(bluetoothDevice: BluetoothDevice, bleGattCallback: BluetoothGattCallback) {
//        bleGatt = scanResult.device.connectGatt(context, true, bleGattCallback)
        bluetoothDevice.createBond()
        bluetoothDevice.connectGatt(context, true, bleGattCallback)
    }
}