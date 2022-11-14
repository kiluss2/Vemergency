package com.kiluss.vemergency.ui.user.rescue

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.kiluss.vemergency.R
import com.kiluss.vemergency.constant.CURRENT_TRANSACTION_COLLECTION
import com.kiluss.vemergency.constant.HISTORY_TRANSACTION_COLLECTION
import com.kiluss.vemergency.constant.SHOP_ARRIVE_DISTANCE
import com.kiluss.vemergency.constant.SHOP_COLLECTION
import com.kiluss.vemergency.data.model.Review
import com.kiluss.vemergency.data.model.Shop
import com.kiluss.vemergency.data.model.Transaction
import com.kiluss.vemergency.ui.base.BaseViewModel

class UserRescueViewModel(application: Application) : BaseViewModel(application) {
    private var activeShopLists = mutableListOf<Shop>()
    private var shopCloneLists = mutableListOf<Shop>()
    private val db = Firebase.firestore
    internal var transaction = Transaction()
    internal var currentLocation = LatLng(0.0, 0.0)
    internal var shop: Shop? = null
    private val _update: MutableLiveData<Transaction> by lazy {
        MutableLiveData<Transaction>()
    }
    internal val update: LiveData<Transaction> = _update
    private val _arrived: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    internal val arrived: LiveData<String> = _arrived
    private val _finish: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    internal val finish: LiveData<String> = _finish
    private val _shopValue: MutableLiveData<Shop> by lazy {
        MutableLiveData<Shop>()
    }
    internal val shopValue: LiveData<Shop> = _shopValue
    private val _finishActivity: MutableLiveData<Unit> by lazy {
        MutableLiveData<Unit>()
    }
    internal val finishActivity: LiveData<Unit> = _finishActivity

    internal fun getShopCloneInfo(position: Int) = shopCloneLists[position]

    internal fun getActiveShopInfo(position: Int) = activeShopLists[position]

    internal fun getShopClone() = shopCloneLists

    internal fun getNearByShopNumber(): Int {
        println(shopCloneLists.size)
        println(activeShopLists.size)
        return shopCloneLists.size + activeShopLists.size
    }

    internal fun getUpdateShopLocationInfo() {
        transaction.id?.let {
            db.collection(CURRENT_TRANSACTION_COLLECTION).document(it).addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("user rescue listen shop realtime location", "Listen failed.", e)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    transaction = snapshot.toObject(Transaction::class.java)!!
                    transaction.distance?.let { distance ->
                        if (distance < SHOP_ARRIVE_DISTANCE) {
                            _arrived.value =
                                getApplication<Application>().getString(R.string.rescue_service_is_arrived)
                            return@addSnapshotListener
                        } else {
                            _update.value = transaction
                        }
                    }
                } else {
                    Log.d("user rescue listen shop realtime location", "Current data: null")
                }
            }
        }
    }

    internal fun finishTransaction(userRating: Double, comment: String) {
        transaction.id?.let {
            db.collection(CURRENT_TRANSACTION_COLLECTION).document(it).delete()
            db.collection(HISTORY_TRANSACTION_COLLECTION).document(it).set(transaction)
            transaction.shopId?.let { shopId ->
                if (shop != null) {
                    var rating = 0.0
                    rating = if (shop?.rating != null) {
                        (shop?.rating!! * transaction.review?.rating!!) / 2
                    } else {
                        userRating
                    }
                    db.collection(SHOP_COLLECTION).document(shopId).update(
                        "ready", true,
                        "review",
                        Review(rating, comment)
                    )
                } else {
                    db.collection(SHOP_COLLECTION).document(shopId).update("ready", true)
                }
                _finishActivity.value = null
            }
        }
    }

    internal fun getShopRescueInfo() {
        transaction.shopId?.let {
            db.collection(SHOP_COLLECTION)
                .document(it)
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        shop = documentSnapshot.toObject<Shop>()
                        _shopValue.value = shop
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("ShopInfoUserRescue", exception.message.toString())
                }
        }
    }
}
