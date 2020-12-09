package com.ramiasia.ekoapp1.ui.time

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.companion.CompanionDeviceManager
import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.ramiasia.ekoapp1.R
import com.ramiasia.ekoapp1.communication.TimeBleDeviceManager

class TimeFragment : Fragment() {

    companion object {
        fun newInstance() = TimeFragment()
        private const val SELECT_DEVICE_REQ_CODE = 314
        private const val ENABLE_BT_REQ_CODE = 315
    }

    private var companionDeviceManager: CompanionDeviceManager? = null

    private lateinit var viewModel: TimeViewModel
    private lateinit var timeTextView: TextView
    private lateinit var deviceTextView: TextView
    private lateinit var fab: FloatingActionButton
    private lateinit var timeBleDeviceManager: TimeBleDeviceManager
    private var bluetoothManager: BluetoothManager? = null
    private var bluetoothAdapter: BluetoothAdapter? = null


    // Overridden Fragment methods

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        companionDeviceManager = getSystemService(context!!, CompanionDeviceManager::class.java)
        bluetoothManager = getSystemService(context!!, BluetoothManager::class.java)
        bluetoothAdapter = bluetoothManager?.adapter
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        //Initialize our ViewModel
        initViewModel()
        val view = inflater.inflate(R.layout.main_fragment, container, false)
        //Initialize the UI elements
        initUi(view)
        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            //Handle device selected through CompanionDeviceManager
            SELECT_DEVICE_REQ_CODE -> when (resultCode) {
                Activity.RESULT_OK -> {
                    val deviceToPair: BluetoothDevice? =
                        data?.getParcelableExtra(CompanionDeviceManager.EXTRA_DEVICE)
                    deviceToPair?.let {
                        // Call on viewModel to handle logic of connecting to the device and minimize
                        // the logic on the Fragment's end.
                        viewModel.connect(deviceToPair)
                    }
                }
            }
            //Handle result of trying to enable Bluetooth
            ENABLE_BT_REQ_CODE -> when (resultCode) {
                Activity.RESULT_OK -> {
                    Toast.makeText(
                        context,
                        "Hooray! Bluetooth is now enabled and ready to scan.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else -> {
                    Toast.makeText(
                        context,
                        "Please enable bluetooth. Bluetooth is needed for connection.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    // Utility methods

    /**
     * All logic used for initializing our [TimeViewModel] is neatly separated to this method.
     * A custom [ViewModelProvider.Factory] is needed for [TimeViewModel], as a
     * [TimeBleDeviceManager] is expected as an arg.
     */
    private fun initViewModel() {
        timeBleDeviceManager = TimeBleDeviceManager(
            context,
            companionDeviceManager,
            object : CompanionDeviceManager.Callback() {
                override fun onDeviceFound(chooserLauncher: IntentSender?) {
                    startIntentSenderForResult(
                        chooserLauncher,
                        SELECT_DEVICE_REQ_CODE, null, 0, 0, 0, null
                    )
                }

                override fun onFailure(error: CharSequence?) {
                    Toast.makeText(
                        context,
                        getString(R.string.txt_connection_error),
                        Toast.LENGTH_SHORT
                    ).show()
                }

            })
        val factory = TimeViewModelFactory(timeBleDeviceManager)
        viewModel = ViewModelProvider(this, factory).get(TimeViewModel::class.java)
    }

    /**
     * Initializes the individual UI elements of the [Fragment], given an inflated [View]
     * hosting the elements.
     *
     * @param view The View hosting our UI elements
     */
    private fun initUi(view: View) {
        timeTextView = view.findViewById(R.id.timeTextView)
        deviceTextView = view.findViewById(R.id.deviceTextView)
        fab = view.findViewById(R.id.floatingActionButton)
        fab.setOnClickListener {
            bluetoothAdapter?.let {
                if (!it.isEnabled) {
                    val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    startActivityForResult(enableBtIntent, ENABLE_BT_REQ_CODE)
                } else {
                    timeBleDeviceManager.scan()
                }
            }
        }

        viewModel.deviceName?.observe(this) {
            deviceTextView.text = it
        }
    }

}