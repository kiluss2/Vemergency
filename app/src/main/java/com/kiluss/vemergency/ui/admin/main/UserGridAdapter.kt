package com.kiluss.vemergency.ui.admin.main

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.kiluss.vemergency.R
import com.kiluss.vemergency.data.model.User
import com.kiluss.vemergency.databinding.ItemListShopGridViewBinding
import com.kiluss.vemergency.utils.UserDiff

class UserGridAdapter(
    private var users: MutableList<User>,
    private val context: Context,
    private val listener: OnClickListener
) : RecyclerView.Adapter<UserGridAdapter.ViewHolder>() {
    interface OnClickListener {
        fun onOpenUserDetail(shop: User)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserGridAdapter.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(ItemListShopGridViewBinding.inflate(inflater, parent, false))
    }

    override fun onBindViewHolder(holder: UserGridAdapter.ViewHolder, position: Int) {
        holder.bind(users[position])
    }

    override fun getItemCount() = users.size

    inner class ViewHolder(private val binding: ItemListShopGridViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(user: User) {
            itemView.setOnClickListener {
                listener.onOpenUserDetail(users[adapterPosition])
            }
            with(user) {
                with(binding) {
                    tvShopTitle.text = fullName
                    tvShopAddress.text = address
                    user.imageUrl?.let {
                        Glide.with(context)
                            .load(user.imageUrl)
                            .centerCrop()
                            .placeholder(R.drawable.default_pic_small)
                            .into(ivShopImage)
                    }
                }
            }
        }
    }

    internal fun updateData(lists: MutableList<User>) {
        try {
            val diffResult = DiffUtil.calculateDiff(UserDiff(lists, this.users))
            diffResult.dispatchUpdatesTo(this)
            this.users.clear()
            this.users.addAll(lists)
        } catch (exception: RuntimeException) {
            exception.printStackTrace()
        }
    }

    internal fun updatePosition(position: Int) {
        notifyItemChanged(position)
    }
}
