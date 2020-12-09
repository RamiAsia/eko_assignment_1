package com.ramiasia.ekoapp1.services

import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.companion.CompanionDeviceManager
import android.content.Intent
import android.os.IBinder
import androidx.core.content.ContextCompat
import com.ramiasia.ekoapp1.communication.TimeBleDeviceManager

class TimeForegroundService : Service() {

    //BLE objects
    private lateinit var timeBleDeviceManager: TimeBleDeviceManager
    private var bluetoothManager: BluetoothManager? = null
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var companionDeviceManager: CompanionDeviceManager? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        companionDeviceManager =
            ContextCompat.getSystemService(this, CompanionDeviceManager::class.java)
        bluetoothManager = ContextCompat.getSystemService(this, BluetoothManager::class.java)
        bluetoothAdapter = bluetoothManager?.adapter
        timeBleDeviceManager = TimeBleDeviceManager(this, companionDeviceManager)

        val bluetoothDevice = bluetoothAdapter?.getRemoteDevice(companionDeviceManager?.associations?.get(0))
        bluetoothDevice?.let {
            timeBleDeviceManager.connect(it)
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onDestroy() {
        timeBleDeviceManager.onDestroy()
        super.onDestroy()
    }
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}