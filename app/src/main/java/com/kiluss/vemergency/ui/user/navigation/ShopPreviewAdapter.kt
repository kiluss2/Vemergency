package com.kiluss.vemergency.ui.user.navigation

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.kiluss.vemergency.R
import com.kiluss.vemergency.data.model.Shop
import com.kiluss.vemergency.databinding.ItemListShopMapPreviewBinding
import com.kiluss.vemergency.utils.ShopDiff
import java.text.MessageFormat

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
                    tvService.text = shop.service
                    tvPhone.text = shop.phone
                    val shopRating = rating
                    if (shopRating != null) {
                        rbRating.visibility = View.VISIBLE
                        tvReviewCount.text = MessageFormat.format(
                            context.resources.getText(R.string.reviews).toString(),
                            shop.reviewCount
                        )
                        rbRating.rating = shopRating.toFloat()
                    } else {
                        rbRating.visibility = View.GONE
                    }
                    shop.imageUrl?.let {
                        Glide.with(context)
                            .load(shop.imageUrl)
                            .centerCrop()
                            .placeholder(R.drawable.default_pic)
                            .centerCrop()
                            .into(ivShopImage)
                    }
                    tvPhone.setOnClickListener {
                        val alertDialog = AlertDialog.Builder(context)
                        alertDialog.apply {
                            setIcon(R.drawable.ic_call)
                            setTitle("Make a phone call?")
                            setMessage("Do you want to make a phone call?")
                            setPositiveButton("Yes") { _: DialogInterface?, _: Int ->
                                // make phone call
                                val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:${shop.phone}"))
                                context.startActivity(intent)
                            }
                            setNegativeButton("No") { _, _ ->
                            }
                        }.create().show()
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
