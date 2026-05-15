package com.expirytracker.app.presentation.details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import coil.load
import com.expirytracker.app.R
import com.expirytracker.app.databinding.FragmentProductDetailsBinding
import com.expirytracker.app.domain.model.Product
import com.expirytracker.app.presentation.common.bindStatus
import com.expirytracker.app.util.DateFormats
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File

@AndroidEntryPoint
class ProductDetailsFragment : Fragment() {
    private var _binding: FragmentProductDetailsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProductDetailsViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProductDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
        binding.editButton.setOnClickListener {
            findNavController().navigate(R.id.addProductFragment, bundleOf("productId" to viewModel.productId))
        }
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect(::render)
            }
        }
    }

    private fun render(state: ProductDetailsUiState) {
        state.product?.let { bindProduct(it, state) }
    }

    private fun bindProduct(product: Product, state: ProductDetailsUiState) {
        binding.productImage.load(product.imagePath?.let(::File))
        binding.name.text = product.name
        binding.category.text = product.category
        binding.status.bindStatus(product.freshnessStatus(state.today, state.thresholdDays.toLong()))
        binding.dates.text = "Manufactured: ${DateFormats.display.format(product.manufacturedDate)}\nExpires: ${DateFormats.display.format(product.expiryDate)}"
        binding.notes.text = product.notes.ifBlank { "No notes" }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
