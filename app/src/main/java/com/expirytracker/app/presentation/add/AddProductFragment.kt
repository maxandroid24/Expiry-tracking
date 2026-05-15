package com.expirytracker.app.presentation.add

import android.Manifest
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.expirytracker.app.R
import com.expirytracker.app.databinding.FragmentAddProductBinding
import com.expirytracker.app.domain.model.Product
import com.expirytracker.app.util.DateFormats
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate

@AndroidEntryPoint
class AddProductFragment : Fragment() {
    private var _binding: FragmentAddProductBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AddProductViewModel by viewModels()
    private var manufacturedDate: LocalDate? = null
    private var expiryDate: LocalDate? = null
    private var pendingCameraUri: Uri? = null

    private val cameraPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) openCamera()
    }
    private val takePicture = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) pendingCameraUri?.let(::openOcrPreview)
    }
    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri -> uri?.let(::openOcrPreview) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAddProductBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
        binding.manufacturedInput.setOnClickListener { showDatePicker("Manufacturer date") { setManufacturedDate(it) } }
        binding.manufacturedLayout.setEndIconOnClickListener { binding.manufacturedInput.performClick() }
        binding.expiryInput.setOnClickListener { showDatePicker("Expiry date") { setExpiryDate(it) } }
        binding.expiryLayout.setEndIconOnClickListener { binding.expiryInput.performClick() }
        binding.cameraButton.setOnClickListener { cameraPermission.launch(Manifest.permission.CAMERA) }
        binding.galleryButton.setOnClickListener { pickImage.launch("image/*") }
        binding.saveButton.setOnClickListener {
            viewModel.save(
                binding.nameInput.text?.toString().orEmpty(),
                binding.categoryInput.text?.toString().orEmpty(),
                manufacturedDate,
                expiryDate,
                binding.notesInput.text?.toString().orEmpty()
            )
        }
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect(::render)
            }
        }
    }

    private fun render(state: AddProductUiState) {
        state.product?.let(::fillForm)
        binding.saveButton.isEnabled = !state.isSaving
        state.error?.let { Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show() }
        if (state.saved) findNavController().navigateUp()
    }

    private fun fillForm(product: Product) {
        if (binding.nameInput.text?.isNotBlank() == true) return
        binding.nameInput.setText(product.name)
        binding.categoryInput.setText(product.category)
        setManufacturedDate(product.manufacturedDate)
        setExpiryDate(product.expiryDate)
        binding.notesInput.setText(product.notes)
    }

    private fun showDatePicker(title: String, onSelected: (LocalDate) -> Unit) {
        MaterialDatePicker.Builder.datePicker()
            .setTitleText(title)
            .build()
            .apply {
                addOnPositiveButtonClickListener { onSelected(DateFormats.fromEpochMillis(it)) }
            }
            .show(parentFragmentManager, title)
    }

    private fun setManufacturedDate(date: LocalDate) {
        manufacturedDate = date
        binding.manufacturedInput.setText(DateFormats.display.format(date))
    }

    private fun setExpiryDate(date: LocalDate) {
        expiryDate = date
        binding.expiryInput.setText(DateFormats.display.format(date))
    }

    private fun openCamera() {
        val directory = File(requireContext().cacheDir, "camera").apply { mkdirs() }
        val file = File(directory, "capture_${System.currentTimeMillis()}.jpg")
        pendingCameraUri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.fileprovider", file)
        takePicture.launch(pendingCameraUri)
    }

    private fun openOcrPreview(uri: Uri) {
        findNavController().navigate(R.id.ocrPreviewFragment, bundleOf("imageUri" to uri.toString()))
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
