package com.expirytracker.app.presentation.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.expirytracker.app.R
import com.expirytracker.app.databinding.FragmentHomeBinding
import com.expirytracker.app.databinding.ViewStatTileBinding
import com.expirytracker.app.domain.model.ProductFilter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()
    private lateinit var adapter: ProductAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = ProductAdapter { id ->
            findNavController().navigate(R.id.action_home_to_details, bundleOf("productId" to id))
        }
        binding.productList.layoutManager = LinearLayoutManager(requireContext())
        binding.productList.adapter = adapter
        attachSwipeActions()

        ViewStatTileBinding.bind(binding.totalStat.root).statLabel.text = "Total"
        ViewStatTileBinding.bind(binding.expiredStat.root).statLabel.text = "Expired"
        ViewStatTileBinding.bind(binding.soonStat.root).statLabel.text = "Soon"

        binding.searchInput.doAfterTextChanged { viewModel.search(it?.toString().orEmpty()) }
        binding.addProductFab.setOnClickListener { findNavController().navigate(R.id.action_home_to_add) }
        binding.filterChips.setOnCheckedStateChangeListener { _, checked ->
            viewModel.setFilter(
                when (checked.firstOrNull()) {
                    R.id.filterFresh -> ProductFilter.FRESH
                    R.id.filterSoon -> ProductFilter.EXPIRING_SOON
                    R.id.filterExpired -> ProductFilter.EXPIRED
                    else -> ProductFilter.ALL
                }
            )
        }
        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_settings -> {
                    findNavController().navigate(R.id.action_home_to_settings)
                    true
                }
                R.id.action_sort -> {
                    viewModel.cycleSort()
                    true
                }
                R.id.action_scan -> {
                    findNavController().navigate(R.id.action_home_to_add)
                    true
                }
                else -> false
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect(::render)
            }
        }
    }

    private fun render(state: HomeUiState) {
        adapter.submitList(state.products)
        ViewStatTileBinding.bind(binding.totalStat.root).statValue.text = state.totalCount.toString()
        ViewStatTileBinding.bind(binding.expiredStat.root).statValue.text = state.expiredCount.toString()
        ViewStatTileBinding.bind(binding.soonStat.root).statValue.text = state.expiringSoonCount.toString()
    }

    private fun attachSwipeActions() {
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean = false
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val item = adapter.currentList[viewHolder.bindingAdapterPosition]
                if (direction == ItemTouchHelper.LEFT) {
                    viewModel.delete(item.id)
                } else {
                    findNavController().navigate(R.id.action_home_to_add, bundleOf("productId" to item.id))
                    adapter.notifyItemChanged(viewHolder.bindingAdapterPosition)
                }
            }
        }).attachToRecyclerView(binding.productList)
    }

    override fun onDestroyView() {
        binding.productList.adapter = null
        _binding = null
        super.onDestroyView()
    }
}
