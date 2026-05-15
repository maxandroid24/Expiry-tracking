package com.expirytracker.app.presentation.ocr

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import coil.load
import com.expirytracker.app.databinding.FragmentOcrPreviewBinding
import com.expirytracker.app.domain.model.ExtractedProduct
import com.expirytracker.app.util.DateFormats
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.LocalDate

@AndroidEntryPoint
class OcrPreviewFragment : Fragment() {
    private var _binding: FragmentOcrPreviewBinding? = null
    private val binding get() = _binding!!
    private val viewModel: OcrPreviewViewModel by viewModels()
    private var manufacturedDate: LocalDate? = null
    private var expiryDate: LocalDate? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentOcrPreviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
        binding.previewImage.load(viewModel.imageUri)
        binding.form.manufacturedInput.setOnClickListener { showDatePicker("Manufacturer date") { setManufacturedDate(it) } }
        binding.form.expiryInput.setOnClickListener { showDatePicker("Expiry date") { setExpiryDate(it) } }
        binding.saveButton.setOnClickListener {
            viewModel.save(
                binding.form.nameInput.text?.toString().orEmpty(),
                binding.form.categoryInput.text?.toString().orEmpty(),
                manufacturedDate,
                expiryDate,
                binding.form.notesInput.text?.toString().orEmpty()
            )
        }
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect(::render)
            }
        }
    }

    private fun render(state: OcrPreviewUiState) {
        binding.saveButton.isEnabled = !state.isLoading
        state.extractedProduct?.let(::fillForm)
        state.error?.let { Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show() }
        if (state.saved) findNavController().popBackStack()
    }

    private fun fillForm(product: ExtractedProduct) {
        if (binding.form.nameInput.text?.isNotBlank() == true) return
        binding.form.nameInput.setText(product.name.orEmpty())
        binding.form.categoryInput.setText(product.category.orEmpty())
        product.manufacturedDate?.let(::setManufacturedDate)
        product.expiryDate?.let(::setExpiryDate)
    }

    private fun showDatePicker(title: String, onSelected: (LocalDate) -> Unit) {
        MaterialDatePicker.Builder.datePicker()
            .setTitleText(title)
            .build()
            .apply { addOnPositiveButtonClickListener { onSelected(DateFormats.fromEpochMillis(it)) } }
            .show(parentFragmentManager, title)
    }

    private fun setManufacturedDate(date: LocalDate) {
        manufacturedDate = date
        binding.form.manufacturedInput.setText(DateFormats.display.format(date))
    }

    private fun setExpiryDate(date: LocalDate) {
        expiryDate = date
        binding.form.expiryInput.setText(DateFormats.display.format(date))
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
