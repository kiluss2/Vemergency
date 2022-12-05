package com.kiluss.vemergency.ui.shop.rescue

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.kiluss.vemergency.constant.CURRENT_TRANSACTION_COLLECTION
import com.kiluss.vemergency.constant.SHOP_COLLECTION
import com.kiluss.vemergency.data.firebase.FirebaseManager
import com.kiluss.vemergency.data.model.Review
import com.kiluss.vemergency.data.model.Shop
import com.kiluss.vemergency.data.model.Transaction
import com.kiluss.vemergency.data.model.User
import com.kiluss.vemergency.ui.base.BaseViewModel

class ShopRescueViewModel(application: Application) : BaseViewModel(application) {
    private var activeShopLists = mutableListOf<Shop>()
    private var shopCloneLists = mutableListOf<Shop>()
    private val db = Firebase.firestore
    internal var transaction = Transaction()
    internal var currentLocation = LatLng(0.0, 0.0)
    internal var transactionFinished = false
    private val _userInfo: MutableLiveData<User> by lazy {
        MutableLiveData<User>()
    }
    internal val userInfo: LiveData<User> = _userInfo
    private val _finishDialog: MutableLiveData<Review> by lazy {
        MutableLiveData<Review>()
    }
    internal val finishDialog: LiveData<Review> = _finishDialog

    internal fun getShopCloneInfo(position: Int) = shopCloneLists[position]

    internal fun getActiveShopInfo(position: Int) = activeShopLists[position]

    internal fun getShopClone() = shopCloneLists

    internal fun getNearByShopNumber(): Int {
        println(shopCloneLists.size)
        println(activeShopLists.size)
        return shopCloneLists.size + activeShopLists.size
    }

    internal fun updateTransactionStatus() {
        transaction.id?.let {
            db.collection(CURRENT_TRANSACTION_COLLECTION)
                .document(it)
                .set(transaction)
        }
    }

    internal fun deleteCurrentTransaction() {
        transaction.id?.let {
            db.collection(CURRENT_TRANSACTION_COLLECTION)
                .document(it)
                .delete()
        }
    }

    internal fun setShopStatusReady() {
        FirebaseManager.getAuth()?.uid?.let {
            db.collection(SHOP_COLLECTION)
                .document(it)
                .update("ready", true)
        }
    }

    // listener for finish current transaction
    internal fun addCurrentTransactionListener() {
        transaction.id?.let {
            db.collection(CURRENT_TRANSACTION_COLLECTION).document(it).addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("user rescue listen shop realtime location", "Listen failed.", e)
                    return@addSnapshotListener
                }
                snapshot?.toObject<Transaction>()?.let { transaction ->
                    transaction.review?.let {
                        Log.e("user rescue listen shop realtime location", "Listen")
                        transactionFinished = true
                        _finishDialog.value = transaction.review
                        transaction.id?.let { id ->
                            db.collection(CURRENT_TRANSACTION_COLLECTION).document(id).delete()
                        }
                        return@addSnapshotListener
                    }
                }
            }
        }
    }
}
