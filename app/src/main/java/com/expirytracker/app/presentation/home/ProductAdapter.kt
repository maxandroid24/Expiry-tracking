package com.expirytracker.app.presentation.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.expirytracker.app.databinding.ItemProductBinding
import com.expirytracker.app.presentation.common.bindStatus
import java.io.File

class ProductAdapter(
    private val onClick: (Long) -> Unit
) : ListAdapter<ProductListItem, ProductAdapter.ProductViewHolder>(Diff) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        return ProductViewHolder(ItemProductBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) = holder.bind(getItem(position))

    inner class ProductViewHolder(private val binding: ItemProductBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ProductListItem) {
            binding.productName.text = item.name
            binding.category.text = item.category
            binding.countdown.text = item.countdown
            binding.statusPill.bindStatus(item.status)
            binding.productImage.load(item.imagePath?.let(::File))
            binding.root.setOnClickListener { onClick(item.id) }
        }
    }

    object Diff : DiffUtil.ItemCallback<ProductListItem>() {
        override fun areItemsTheSame(oldItem: ProductListItem, newItem: ProductListItem): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: ProductListItem, newItem: ProductListItem): Boolean = oldItem == newItem
    }
}
