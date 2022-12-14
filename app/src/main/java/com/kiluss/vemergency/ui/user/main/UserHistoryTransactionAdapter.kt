package com.kiluss.vemergency.ui.user.main

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.kiluss.vemergency.R
import com.kiluss.vemergency.data.model.Transaction
import com.kiluss.vemergency.databinding.ItemListUserHistoryTransactionBinding
import com.kiluss.vemergency.utils.TransactionDiff
import java.util.Date

class UserHistoryTransactionAdapter(
    private var transactions: MutableList<Transaction>,
    private val context: Context,
    private val listener: OnClickListener
) : RecyclerView.Adapter<UserHistoryTransactionAdapter.ViewHolder>() {
    interface OnClickListener {
        fun onSelect(transaction: Transaction, position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(ItemListUserHistoryTransactionBinding.inflate(inflater, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(transactions[position])
    }

    override fun getItemCount() = transactions.size

    inner class ViewHolder(private val binding: ItemListUserHistoryTransactionBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(transaction: Transaction) {
            with(transaction) {
                with(binding) {
                    tvUserFullName.text = shopName
                    review?.rating?.let { rbRate.rating = it.toFloat() }
                    tvPhone.text = shopPhone
                    tvService.text = service
                    tvAddress.text = shopAddress
                    review?.comment?.let {
                        if (it.isNotEmpty()) {
                            tvComment.visibility = View.VISIBLE
                            tvComment.text = it
                        }
                    }
                    tvTime.text = endTime?.toLong()?.let { Date(it).toString() }
                    binding.root.setOnClickListener {
                        listener.onSelect(transaction, adapterPosition)
                    }
                    Glide.with(context)
                        .load(shopImage.toString())
                        .centerCrop()
                        .placeholder(R.drawable.ic_account_avatar)
                        .into(ivUser)
                    tvPhone.setOnClickListener {
                        if (ContextCompat.checkSelfPermission(
                                context,
                                android.Manifest.permission.CALL_PHONE
                            )
                            != PackageManager.PERMISSION_GRANTED
                        ) {
                            ActivityCompat.requestPermissions(
                                context as Activity, arrayOf(android.Manifest.permission.CALL_PHONE),
                                0
                            )
                        } else {
                            val alertDialog = AlertDialog.Builder(context)
                            alertDialog.apply {
                                setIcon(R.drawable.ic_call)
                                setTitle("Make a phone call?")
                                setMessage("Do you want to make a phone call?")
                                setPositiveButton("Yes") { _: DialogInterface?, _: Int ->
                                    // make phone call
                                    val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$shopPhone"))
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
    }

    internal fun updateData(files: MutableList<Transaction>) {
        try {
            val diffResult = DiffUtil.calculateDiff(TransactionDiff(files, this.transactions))
            diffResult.dispatchUpdatesTo(this)
            this.transactions.clear()
            this.transactions.addAll(files)
        } catch (exception: RuntimeException) {
            exception.printStackTrace()
        }
    }

    internal fun updatePosition(position: Int) {
        notifyItemChanged(position)
    }
}
