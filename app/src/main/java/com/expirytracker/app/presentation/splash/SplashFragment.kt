package com.expirytracker.app.presentation.splash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.expirytracker.app.R
import com.expirytracker.app.databinding.FragmentSplashBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashFragment : Fragment() {
    private var _binding: FragmentSplashBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSplashBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.logo.alpha = 0f
        binding.logo.animate().alpha(1f).scaleX(1.06f).scaleY(1.06f).setDuration(420).start()
        viewLifecycleOwner.lifecycleScope.launch {
            delay(650)
            findNavController().navigate(R.id.action_splash_to_home)
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
