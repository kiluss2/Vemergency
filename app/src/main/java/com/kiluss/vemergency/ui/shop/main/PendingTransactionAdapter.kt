package com.kiluss.vemergency.ui.shop.main

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.kiluss.vemergency.R
import com.kiluss.vemergency.constant.EXTRA_SHOP_LOCATION
import com.kiluss.vemergency.data.model.Shop
import com.kiluss.vemergency.data.model.Transaction
import com.kiluss.vemergency.databinding.ItemListPendingTransactionBinding
import com.kiluss.vemergency.ui.user.navigation.NavigationActivity
import com.kiluss.vemergency.utils.TransactionDiff
import java.util.Date

class PendingTransactionAdapter(
    private var transactions: MutableList<Transaction>,
    private val context: Context,
    private val listener: OnClickListener
) : RecyclerView.Adapter<PendingTransactionAdapter.ViewHolder>() {
    interface OnClickListener {
        fun onSelect(transaction: Transaction, position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(ItemListPendingTransactionBinding.inflate(inflater, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(transactions[position])
    }

    override fun getItemCount() = transactions.size

    inner class ViewHolder(private val binding: ItemListPendingTransactionBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(transaction: Transaction) {
            with(transaction) {
                with(binding) {
                    btnLocation.setOnClickListener {
                        context.startActivity(Intent(context, NavigationActivity::class.java).apply {
                            putExtra(EXTRA_SHOP_LOCATION, Shop().apply {
                                location = hashMapOf(
                                    "latitude" to userLocation?.latitude!!,
                                    "longitude" to userLocation?.longitude!!
                                )
                            })
                        })
                    }
                    tvPhone.text = userPhone
                    tvService.text = service
                    tvContent.text = content
                    tvFullName.text = userFullName
                    tvTime.text = startTime?.toLong()?.let { Date(it).toString() }
                    btnSelect.setOnClickListener {
                        listener.onSelect(transaction, adapterPosition)
                    }
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
                                    val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$userPhone"))
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
