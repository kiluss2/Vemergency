package com.kiluss.vemergency.ui.user.navigation

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.kiluss.vemergency.data.model.Shop
import com.kiluss.vemergency.databinding.ItemListShopMapPreviewBinding
import com.kiluss.vemergency.utils.ShopDiff

class ShopPreviewAdapter(
    private var shops: MutableList<Shop>,
    private val context: Context,
    private val listener: OnClickListener
) : RecyclerView.Adapter<ShopPreviewAdapter.ViewHolder>() {

    interface OnClickListener {

        fun onOpen(shop: Shop)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(ItemListShopMapPreviewBinding.inflate(inflater, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(shops[position])
    }

    override fun getItemCount() = shops.size

    inner class ViewHolder(private val binding: ItemListShopMapPreviewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(shop: Shop) {
            itemView.setOnClickListener {
                listener.onOpen(shops[adapterPosition])
            }
            with(shop) {
                with(binding) {
                    tvShopTitle.text = name
                    tvShopAddress.text = address
                    val shopRating = rating
                    if (shopRating!= null) {
                        rbRating.visibility = View.VISIBLE
                        tvNoRating.visibility = View.GONE
                        rbRating.rating = shopRating.toFloat()
                    } else {
                        rbRating.visibility = View.GONE
                        tvNoRating.visibility = View.VISIBLE
                    }
                    shop.imageUrl?.let {
                        Glide.with(context)
                            .load(shop.imageUrl)
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
