package com.kiluss.vemergency.ui.admin.main

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.kiluss.vemergency.R
import com.kiluss.vemergency.data.model.Shop
import com.kiluss.vemergency.databinding.ItemListShopViewBinding
import com.kiluss.vemergency.utils.ShopDiff

class ShopAdapter(
    private var shops: MutableList<Shop>,
    private val context: Context,
    private val listener: OnClickListener
) : RecyclerView.Adapter<ShopAdapter.ViewHolder>() {

    interface OnClickListener {

        fun onOpen(shop: Shop)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(ItemListShopViewBinding.inflate(inflater, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(shops[position])
    }

    override fun getItemCount() = shops.size

    inner class ViewHolder(private val binding: ItemListShopViewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(shop: Shop) {
            itemView.setOnClickListener {
                listener.onOpen(shops[adapterPosition])
            }
            with(shop) {
                with(binding) {
                    tvShopTitle.text = name
                    tvShopAddress.text = address
                    shop.imageUrl?.let {
                        Glide.with(context)
                            .load(shop.imageUrl)
                            .centerCrop()
                            .placeholder(R.drawable.default_pic)
                            .into(ivShopImage)
                    }
                }
            }
        }
    }

    internal fun updateData(files: MutableList<Shop>) {
        try {
            val diffResult = DiffUtil.calculateDiff(ShopDiff(files, this.shops))
            diffResult.dispatchUpdatesTo(this)
            this.shops.clear()
            this.shops.addAll(files)
        } catch (exception: RuntimeException) {
            exception.printStackTrace()
        }
    }

    internal fun updatePosition(position: Int) {
        notifyItemChanged(position)
    }
}
