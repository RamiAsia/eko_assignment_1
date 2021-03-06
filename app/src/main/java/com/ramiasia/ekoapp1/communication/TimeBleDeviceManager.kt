package com.ramiasia.ekoapp1.communication

import android.bluetooth.*
import android.companion.AssociationRequest
import android.companion.BluetoothDeviceFilter
import android.companion.CompanionDeviceManager
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ramiasia.ekoapp1.utils.ByteConversionUtils.Companion.fromLittleEndian
import java.time.LocalDateTime
import java.util.*
import java.util.regex.Pattern

/**
 * Manager for the Time BLE devices. Logic enabling the scanning and connecting to the BLE device will
 * be isolated to this class.
 */
class TimeBleDeviceManager(
    private val context: Context?,
    private val deviceManager: CompanionDeviceManager?,
    private val callBack: CompanionDeviceManager.Callback? = null
) {

    private val LOGTAG = TimeBleDeviceManager::class.java.simpleName
    private var bluetoothGatt: BluetoothGatt? = null
    private var currentTimeCharacteristic: BluetoothGattCharacteristic =
        BluetoothGattCharacteristic(CURRENT_TIME_CHAR_UUID, 0, 0)
    private var characteristics: List<BluetoothGattCharacteristic>? = null

    //LiveData
    private var _time: MutableLiveData<String> = MutableLiveData<String>("")
    var time: LiveData<String> = _time

    private var _connectionState: MutableLiveData<Int> =
        MutableLiveData<Int>(BluetoothProfile.STATE_DISCONNECTED)
    var connectionState: LiveData<Int> = _connectionState

    private var _deviceName: MutableLiveData<String> = MutableLiveData<String>("")
    var deviceName: LiveData<String> = _deviceName

    //BluetoothGattCallback (Opting for composition here as opposed to inheritance)
    private val bleGattCallback: BluetoothGattCallback = object : BluetoothGattCallback() {

        /**
         * When finding the services, enable notifications for the current time characteristic
         */
        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            Log.i(LOGTAG, "GATT services discovered from server.")
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> {
                    gatt?.services?.let { services ->
                        for (service in services) {
                            Log.d(LOGTAG, "Service found: ${service.uuid} of $service")
                        }
                        val service = gatt.getService(TIME_SERVICE_UUID)
                        if (service.uuid == TIME_SERVICE_UUID) {
                            Log.d(LOGTAG, "Gotcha!")
                            characteristics = service?.characteristics
                            characteristics?.let {
                                currentTimeCharacteristic =
                                    service.getCharacteristic(CURRENT_TIME_CHAR_UUID)
                                gatt.setCharacteristicNotification(currentTimeCharacteristic, true)
                                val descriptor =
                                    currentTimeCharacteristic.getDescriptor(CURRENT_TIME_CLIENT_CHAR_CONFIG_UUID)
                                        ?.apply {
                                            value =
                                                BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                                        }
                                gatt.writeDescriptor(descriptor)
                                Log.d(LOGTAG, "Wrote notification enable for $currentTimeCharacteristic")
                            }
                        }
                    }
                }
                else -> {
                    Log.d(LOGTAG, "onServicesDiscovered status: $status")
                }
            }


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
                    Log.i(
                        LOGTAG, "Attempting to start service discovery: " +
                                gatt.discoverServices()
                    )
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
                    Log.d(LOGTAG, "Received characteristic: ${characteristic.uuid}")
                    if (characteristic.uuid == CURRENT_TIME_CHAR_UUID) {
                        _time.postValue(parseTimeFrom(characteristic))
                    }
                }
            }
        }

        private fun parseTimeFrom(characteristic: BluetoothGattCharacteristic): String {
            //TODO: Format value to an actual time lol
            val value = characteristic.value
            val dateTime = LocalDateTime.of(
                fromLittleEndian(
                    value.copyOfRange(0, 2)
                ),
//                ByteBuffer.wrap(
//                    value.copyOfRange(0, 2)
//                ).short.toInt(), //Year
                value[2].toInt(), //Month
                value[3].toInt(), //Day
                value[4].toInt(), //Hour
                value[5].toInt(), //Minute
                value[6].toInt() //Second
            )
            return dateTime.toString()
        }
    }


    /**
     * Initializes a scan for Bluetooth devices through [CompanionDeviceManager].
     */
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
        callBack?.let {
            deviceManager?.associate(pairingRequest, callBack, null)
        }
    }

    /**
     * Connects to a [BluetoothDevice], sending a [BluetoothGattCallback] object to handle responses
     * from the Gatt device.
     *
     * @param bluetoothDevice The Bluetooth device to connect to.
     */
    fun connect(bluetoothDevice: BluetoothDevice) {
        bluetoothDevice.createBond()
        bluetoothGatt = bluetoothDevice.connectGatt(context, true, bleGattCallback)
    }

    /**
     * Requests time from connected BLE device with Time service, if available.
     */
    fun requestTime() {
        bluetoothGatt?.readCharacteristic(currentTimeCharacteristic)
    }

    /**
     * onDestroy method to release resources. Must be called when done with [BluetoothGatt] object.
     * Preferably called in the onCleared method if used in a [ViewModel].
     */
    fun onDestroy() {
        bluetoothGatt?.close()
        bluetoothGatt = null
    }

    companion object {
        private val TIME_SERVICE_UUID = UUID.fromString("00001805-0000-1000-8000-00805f9b34fb")
        private val CURRENT_TIME_CHAR_UUID = UUID.fromString("00002a2b-0000-1000-8000-00805f9b34fb")
        private val CURRENT_TIME_CLIENT_CHAR_CONFIG_UUID =
            UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
    }
}