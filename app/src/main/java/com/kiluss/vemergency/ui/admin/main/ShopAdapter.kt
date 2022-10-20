package com.kiluss.vemergency.ui.admin.main

import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.storage.FirebaseStorage
import com.kiluss.vemergency.constant.SHOP_COVER
import com.kiluss.vemergency.constant.SHOP_NODE
import com.kiluss.vemergency.constant.TEMP_IMAGE
import com.kiluss.vemergency.data.firebase.FirebaseManager
import com.kiluss.vemergency.data.model.Shop
import com.kiluss.vemergency.databinding.ItemListShopViewBinding
import com.kiluss.vemergency.utils.ShopDiff
import java.io.File

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
                    getShopCover(uid, ivShopImage, adapterPosition)
                }
            }
        }
    }

    private fun getShopCover(uid: String?, ivShopImage: ImageView, adapterPosition: Int) {
        File("${context.cacheDir}/$TEMP_IMAGE").mkdirs()
        val localFile = File("${context.cacheDir}/$TEMP_IMAGE/$uid.jpg")
        if (localFile.exists()) {
            localFile.delete()
        }
        localFile.createNewFile()
        FirebaseStorage.getInstance().reference.child(uid.toString() + "/" + SHOP_NODE + "/" + SHOP_COVER)
            .getFile(localFile)
            .addOnCompleteListener {
                val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
                bitmap?.let {
                    ivShopImage.setImageBitmap(it)
                    // notifyItemChanged(adapterPosition)
                }
            }.addOnFailureListener {
                it.printStackTrace()
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
