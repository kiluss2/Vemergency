package com.kiluss.vemergency.utils

import androidx.recyclerview.widget.DiffUtil
import com.kiluss.vemergency.data.model.Shop

class ShopDiff(
    private var newList: MutableList<Shop>,
    private var oldList: MutableList<Shop>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return (oldList[oldItemPosition].id == newList[newItemPosition].id)
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}
