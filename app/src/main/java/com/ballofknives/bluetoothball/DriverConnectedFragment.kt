package com.ballofknives.bluetoothball

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.ballofknives.bluetoothball.databinding.FragmentDriverConnectedBinding


class DriverConnectedFragment : Fragment() {
    private var _binding : FragmentDriverConnectedBinding? = null
    private val binding get() = _binding!!
    private lateinit var ball: String
    private val sharedViewModel: BluetoothSharedViewModel by activityViewModels()


    companion object {
        const val BALL = "ball"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            ball = it.getString(BALL).toString()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDriverConnectedBinding.inflate(inflater, container, false)
        val callback = requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            sharedViewModel.stop()
            findNavController().popBackStack()
        }
        return binding.root
    }


}