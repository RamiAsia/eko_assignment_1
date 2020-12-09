package com.ramiasia.ekoapp1.ui.time

import android.app.Activity
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanResult
import android.companion.CompanionDeviceManager
import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.ramiasia.ekoapp1.R
import com.ramiasia.ekoapp1.communication.TimeDeviceManager

class TimeFragment() : Fragment() {

    companion object {
        fun newInstance() = TimeFragment()
        private const val SELECT_DEVICE_REQ_CODE = 314
    }

    private var companionDeviceManager: CompanionDeviceManager? = null

    private lateinit var viewModel: TimeViewModel
    private lateinit var timeTextView: TextView
    private lateinit var deviceTextView: TextView
    private lateinit var fab: FloatingActionButton
    private lateinit var timeDeviceManager: TimeDeviceManager
    private var bluetoothManager: BluetoothManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        companionDeviceManager = getSystemService(context!!, CompanionDeviceManager::class.java)
        bluetoothManager = getSystemService(context!!, BluetoothManager::class.java)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        timeDeviceManager = TimeDeviceManager(context, companionDeviceManager, bluetoothManager, object : CompanionDeviceManager.Callback() {
            override fun onDeviceFound(chooserLauncher: IntentSender?) {
                startIntentSenderForResult(chooserLauncher,
                        SELECT_DEVICE_REQ_CODE, null, 0, 0, 0, null)
            }

            override fun onFailure(error: CharSequence?) {
                Toast.makeText(context, getString(R.string.txt_connection_error), Toast.LENGTH_LONG).show()
            }

        })

        viewModel = ViewModelProvider(this).get(TimeViewModel::class.java)
        viewModel.timeDeviceManager = timeDeviceManager

        val view = inflater.inflate(R.layout.main_fragment, container, false)

        timeTextView = view.findViewById(R.id.timeTextView)
        deviceTextView = view.findViewById(R.id.deviceTextView)
        fab = view.findViewById(R.id.floatingActionButton)
        fab.setOnClickListener {
            timeDeviceManager.scan()
        }

        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            SELECT_DEVICE_REQ_CODE -> when(resultCode) {
                Activity.RESULT_OK -> {
                    // User has chosen to pair with the Bluetooth device.
                    val deviceToPair: ScanResult? =
                            data?.getParcelableExtra(CompanionDeviceManager.EXTRA_DEVICE)
                    deviceToPair?.let {
                        viewModel.connect(deviceToPair)
                    }
                }
            }
        }
    }

}