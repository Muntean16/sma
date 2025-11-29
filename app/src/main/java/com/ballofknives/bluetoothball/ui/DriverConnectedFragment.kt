package com.ballofknives.bluetoothball.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.ballofknives.bluetoothball.R
import com.ballofknives.bluetoothball.bluetooth.BluetoothSharedViewModel
import com.ballofknives.bluetoothball.databinding.FragmentDriverConnectedBinding
import com.ballofknives.bluetoothball.utils.Event

class DriverConnectedFragment : Fragment() {
    private var _binding : FragmentDriverConnectedBinding? = null
    private val binding get() = _binding!!
    private lateinit var ball: String
    private val sharedViewModel: BluetoothSharedViewModel by activityViewModels()
    private var timerTextView: TextView? = null

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
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val callback = requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            sharedViewModel.stop()
            findNavController().popBackStack()
        }
        
        timerTextView = binding.root.findViewById(R.id.timerTextView)
        
        sharedViewModel.timerText.observe(viewLifecycleOwner, Observer { timeText ->
            timerTextView?.text = timeText
        })
        
        sharedViewModel.sharedScore.observe(viewLifecycleOwner, Observer { score ->
            android.util.Log.d("DriverConnectedFragment", "Score updated: $score")
        })
        
        // Observe game ended event - use lifecycle owner to ensure it's active
        sharedViewModel.gameEnded.observe(viewLifecycleOwner, Observer { event ->
            android.util.Log.d("DriverConnectedFragment", "Game ended event received, checking content... Event: $event")
            val sharedScore = event.getContentIfNotHandled()
            android.util.Log.d("DriverConnectedFragment", "getContentIfNotHandled returned: $sharedScore")
            if (sharedScore != null) {
                android.util.Log.d("DriverConnectedFragment", "Processing game end with score: $sharedScore")
                val finalScore = sharedViewModel.sharedScore.value ?: sharedScore
                val serverName = ball.ifEmpty { "Server" }
                android.util.Log.d("DriverConnectedFragment", "Opening GameResultsActivity with score: $finalScore, partner: $serverName")
                
                try {
                    val intent = android.content.Intent(requireContext(), GameResultsActivity::class.java).apply {
                        putExtra("sharedScore", finalScore)
                        putExtra("isServer", false)
                        putExtra("partnerName", serverName)
                    }
                    startActivity(intent)
                    requireActivity().finish()
                } catch (e: Exception) {
                    android.util.Log.e("DriverConnectedFragment", "Error opening GameResultsActivity", e)
                }
            } else {
                android.util.Log.d("DriverConnectedFragment", "Game ended event was already handled or is null")
            }
        })
        
        // Also check if game already ended (in case we missed the event)
        sharedViewModel.gameEnded.value?.getContentIfNotHandled()?.let { sharedScore ->
            android.util.Log.d("DriverConnectedFragment", "Game already ended when fragment created, score: $sharedScore")
            val finalScore = sharedViewModel.sharedScore.value ?: sharedScore
            val serverName = ball.ifEmpty { "Server" }
            val intent = android.content.Intent(requireContext(), GameResultsActivity::class.java).apply {
                putExtra("sharedScore", finalScore)
                putExtra("isServer", false)
                putExtra("partnerName", serverName)
            }
            startActivity(intent)
            requireActivity().finish()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        timerTextView = null
        _binding = null
    }
}

