package com.ballofknives.bluetoothball.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.ballofknives.bluetoothball.R
import com.ballofknives.bluetoothball.bluetooth.BluetoothSharedViewModel
import com.ballofknives.bluetoothball.databinding.FragmentSelectABallBinding
import com.ballofknives.bluetoothball.utils.PersistentStorage
import java.nio.ByteBuffer
import kotlin.math.abs

class SelectAballFragment : Fragment(), SensorEventListener {
    private var _binding: FragmentSelectABallBinding? = null
    private val binding get() = _binding!!

    private var eventCount = 0

    private var xEvent: Float = 0.0f
    private var yEvent: Float = 0.0f

    private val sharedViewModel: BluetoothSharedViewModel by activityViewModels()

    private lateinit var persistentStorage: PersistentStorage
    private var mSensorManager: SensorManager? = null
    private var mAccelerometer: Sensor? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSelectABallBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mSensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mAccelerometer = mSensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        persistentStorage = PersistentStorage(requireContext())

        binding.btnGet.setOnClickListener {
            onUpdatePairedDeviceListClicked()
        }

        requestBluetoothPermission()

        sharedViewModel.connected.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { isConnected ->
                if (isConnected) {
                    val deviceName = sharedViewModel.connectedDeviceName.value ?: "Server"
                    val bundle = bundleOf(Pair("ball", deviceName))
                    this@SelectAballFragment.findNavController()
                        .navigate(R.id.action_selectAballFragment_to_driverConnectedFragment, bundle)
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private val bluetoothPermissionRequest =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { map ->
            val userAcknowledgement = map.values.all { it }
            if (userAcknowledgement) {
                persistentStorage.userHasAcknowledgedBluetoothPermissionRationale = false
            }
        }

    private fun isBluetoothPermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ((ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.BLUETOOTH
            ) == PackageManager.PERMISSION_GRANTED) &&
                    (ActivityCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED))
        }
    }

    private fun shouldShowBluetoothPermissionRationale(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            shouldShowRequestPermissionRationale(Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) || shouldShowRequestPermissionRationale(
                Manifest.permission.BLUETOOTH
            )
        }
    }

    private fun userHasPreviouslyAcknowledgedBluetoothPermissionRationale(): Boolean {
        return persistentStorage.userHasAcknowledgedBluetoothPermissionRationale
    }

    private fun requestBluetoothPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val permissions = arrayOf(
                Manifest.permission.BLUETOOTH_CONNECT
            )
            bluetoothPermissionRequest.launch(permissions)
        } else {
            val permissions = arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            bluetoothPermissionRequest.launch(permissions)
        }
    }

    private fun showRationaleDialog() {
        AlertDialog.Builder(requireContext())
            .setMessage(getString(R.string.bluetooth_rationale_1))
            .setPositiveButton(getString(android.R.string.ok)) { _, _ ->
                persistentStorage.userHasAcknowledgedBluetoothPermissionRationale = true
                requestBluetoothPermission()
            }
            .show()
    }

    private fun showPreviouslyAcknowledgedRationaleDialog() {
        AlertDialog.Builder(requireContext())
            .setMessage(getString(R.string.bluetooth_rationale_2))
            .setNegativeButton(getString(R.string.no_thanks), null)
            .setPositiveButton(getString(android.R.string.ok)) { _, _ -> showApplicationDetailsSettingsScreen() }
            .show()
    }

    private fun showApplicationDetailsSettingsScreen() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            .setData(Uri.fromParts("package", requireContext().packageName, null))

        startActivity(intent)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    override fun onSensorChanged(event: SensorEvent?) {
        eventCount++
        val cutoff = 2
        if ((event != null) && (eventCount == 1)) {
            eventCount = 0
            synchronized(this) {
                xEvent = if (abs(event.values[0]) > cutoff) {
                    -1.0f * event.values[0]
                } else {
                    0.0f
                }

                yEvent = if (abs(event.values[1]) > cutoff) {
                    event.values[1]
                } else {
                    0.0f
                }
                val shortX = java.lang.Float.floatToIntBits(xEvent)
                val shortY = java.lang.Float.floatToIntBits(yEvent)
                val allBytes =
                    ByteBuffer.allocate(2 * java.lang.Float.BYTES).putInt(shortX).putInt(shortY)
                if (abs(xEvent) > 0.5 || abs(yEvent) > cutoff) {
                    synchronized(this) {
                        sharedViewModel.write(allBytes.array())
                    }
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun updatePairedDeviceList() {
        val deviceNames = sharedViewModel._bondedDevices.value?.map { it.name } ?: listOf()
        val arrayAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, deviceNames)
        val listView = binding.deviceListView
        listView.adapter = arrayAdapter
        listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, index, _ ->
            val toConnect = sharedViewModel._bondedDevices.value?.elementAt(index)

            if (toConnect != null) {
                try {
                    Toast.makeText(requireContext(), "Trying to connect.", Toast.LENGTH_SHORT)
                        .show()
                    sharedViewModel.connect(toConnect)
                } catch (_: Exception) {
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mSensorManager!!.registerListener(
            this, mAccelerometer,
            SensorManager.SENSOR_DELAY_GAME
        )
        sharedViewModel.start()
    }

    private fun onUpdatePairedDeviceListClicked() {
        if (isBluetoothPermissionGranted()) {
            updatePairedDeviceList()
        } else {
            if (shouldShowBluetoothPermissionRationale()) {
                showRationaleDialog()
            } else if (userHasPreviouslyAcknowledgedBluetoothPermissionRationale()) {
                showPreviouslyAcknowledgedRationaleDialog()
            } else {
                requestBluetoothPermission()
            }
        }
    }
}





