package com.kiluss.vemergency.ui.shop.main

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.kiluss.vemergency.constant.HISTORY_TRANSACTION_COLLECTION
import com.kiluss.vemergency.constant.PENDING_TRANSACTION_COLLECTION
import com.kiluss.vemergency.constant.SHOP_COLLECTION
import com.kiluss.vemergency.constant.SHOP_PENDING_COLLECTION
import com.kiluss.vemergency.data.firebase.FirebaseManager
import com.kiluss.vemergency.data.model.LatLng
import com.kiluss.vemergency.data.model.Review
import com.kiluss.vemergency.data.model.Shop
import com.kiluss.vemergency.data.model.Transaction
import com.kiluss.vemergency.ui.base.BaseViewModel
import com.kiluss.vemergency.utils.Utils

/**
 * Created by sonlv on 10/17/2022
 */
class ShopMainViewModel(application: Application) : BaseViewModel(application) {
    internal var myShop: Shop? = null
    val db = Firebase.firestore
    private var pendingTransactions = mutableListOf<Transaction>()
    private var historyTransactions = mutableListOf<Transaction>()
    private val _progressBarStatus: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }
    internal val progressBarStatus: LiveData<Boolean> = _progressBarStatus
    private val _shop: MutableLiveData<Shop> by lazy {
        MutableLiveData<Shop>()
    }
    internal val shop: LiveData<Shop> = _shop
    private val _navigateToHome: MutableLiveData<Any> by lazy {
        MutableLiveData<Any>()
    }
    internal val navigateToHome: LiveData<Any> = _navigateToHome
    private val _pendingTransaction: MutableLiveData<MutableList<Transaction>> by lazy {
        MutableLiveData<MutableList<Transaction>>()
    }
    internal val pendingTransaction: LiveData<MutableList<Transaction>> = _pendingTransaction
    private val _historyTransaction: MutableLiveData<MutableList<Transaction>> by lazy {
        MutableLiveData<MutableList<Transaction>>()
    }
    internal val historyTransaction: LiveData<MutableList<Transaction>> = _historyTransaction
    private val _startRescue: MutableLiveData<Transaction> by lazy {
        MutableLiveData<Transaction>()
    }
    internal val startRescue: LiveData<Transaction> = _startRescue
    private val _dialogTransactionDone: MutableLiveData<Unit> by lazy {
        MutableLiveData<Unit>()
    }
    internal val dialogTransactionDone: LiveData<Unit> = _dialogTransactionDone

    internal fun getShopInfo() {
        FirebaseManager.getAuth()?.currentUser?.uid?.let {
            db.collection(SHOP_COLLECTION)
                .document(it)
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    val result = documentSnapshot.toObject<Shop>()
                    if (result != null) {
                        if (result.pendingApprove == true) {
                            getShopPendingInfo()
                        } else {
                            if (result.created == true) {
                                _shop.value = result
                                myShop = result
                            } else {
                                _shop.value = null
                                myShop = null
                            }
                            _progressBarStatus.value = false
                        }
                    } else {
                        _shop.value = null
                        myShop = null
                        _progressBarStatus.value = false
                    }
                }
                .addOnFailureListener { exception ->
                    hideProgressbar()
                    Log.e("Main Activity", exception.message.toString())
                    _shop.value = null
                    myShop = null
                }
        }
    }

    private fun getShopPendingInfo() {
        FirebaseManager.getAuth()?.currentUser?.uid?.let {
            db.collection(SHOP_PENDING_COLLECTION)
                .document(it)
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    val result = documentSnapshot.toObject<Shop>()
                    if (result != null) {
                        _shop.value = result
                        myShop = result
                    } else {
                        _shop.value = null
                        myShop = null
                    }
                    _progressBarStatus.value = false
                }
                .addOnFailureListener { exception ->
                    hideProgressbar()
                    Log.e("Main Activity", exception.message.toString())
                    _shop.value = null
                    myShop = null
                }
        }
    }

    internal fun getShopData() = myShop

    internal fun signOut() {
        removeFcmToken()
        FirebaseManager.getAuth()?.signOut() //End user session
        FirebaseManager.logout()
    }

    private fun removeFcmToken() {
        FirebaseManager.getAuth()?.currentUser?.uid?.let { uid ->
            db.collection(SHOP_COLLECTION).document(uid).update("fcmToken", "")
        }
    }

    private fun hideProgressbar() {
        _progressBarStatus.value = false
    }

    internal fun navigateToHome() {
        _navigateToHome.value = null
    }

    internal fun getPendingTransaction() {
        db.collection(PENDING_TRANSACTION_COLLECTION)
            .whereArrayContains("shops", FirebaseManager.getAuth()?.uid.toString())
            .orderBy("startTime")
            .get()
            .addOnSuccessListener { documents ->
                pendingTransactions.clear()
                for (document in documents) {
                    val transaction = Transaction().apply {
                        document.data["id"]?.let { id = it as String }
                        document.data["userId"]?.let { userId = it as String }
                        document.data["userImage"]?.let { userImage = it as String }
                        document.data["userFullName"]?.let { userFullName = it as String }
                        document.data["userPhone"]?.let { userPhone = it as String }
                        document.data["service"]?.let { service = it as String }
                        document.data["startTime"]?.let { startTime = it as Double }
                        document.data["content"]?.let { content = it as String }
                        document.data["userAddress"]?.let { userAddress = it as String }
                        document.data["userLocation"]?.let {
                            val location = it as HashMap<*, *>
                            userLocation = LatLng(location["latitude"] as Double, location["longitude"] as Double)
                        }
                        document.data["userFcmToken"]?.let { userFcmToken = it as String }
                    }
                    pendingTransactions.add(transaction)
                }
                _pendingTransaction.value = pendingTransactions
            }
            .addOnFailureListener { exception ->
                Log.e("pending transaction", exception.toString())
            }
    }

    internal fun startTransaction(transaction: Transaction, position: Int) {
        transaction.id?.let {
            db.collection(PENDING_TRANSACTION_COLLECTION).document(it).get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val document = task.result
                    if (document != null) {
                        if (document.exists()) {
                            // remove pending transaction
                            db.collection(PENDING_TRANSACTION_COLLECTION).document(it).delete()
                                .addOnSuccessListener {
                                    val newList = mutableListOf<Transaction>()
                                    for (index in pendingTransactions.indices) {
                                        if (index != position) {
                                            newList.add(pendingTransactions[index].copy())
                                        }
                                    }
                                    pendingTransactions = newList
                                    _pendingTransaction.value = pendingTransactions
                                    FirebaseManager.getAuth()?.uid?.let { id ->
                                        db.collection(SHOP_COLLECTION).document(id).update("ready", false)
                                    }
                                    _startRescue.value = transaction.apply {
                                        shopId = FirebaseManager.getAuth()?.uid
                                    }
                                }
                        } else {
                            _dialogTransactionDone.value = null
                        }
                    }
                } else {
                    Log.d("TAG", "Error: ", task.exception)
                }
            }
        }
    }

    internal fun getHistoryTransaction() {
        db.collection(HISTORY_TRANSACTION_COLLECTION)
            .whereEqualTo("shopId", FirebaseManager.getAuth()?.uid.toString())
            .orderBy("endTime", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                historyTransactions.clear()
                for (document in documents) {
                    val transaction = Transaction().apply {
                        document.data["id"]?.let { id = it as String }
                        document.data["userId"]?.let { userId = it as String }
                        document.data["shopId"]?.let { shopId = it as String }
                        document.data["userImage"]?.let { userImage = it as String }
                        document.data["userFullName"]?.let { userFullName = it as String }
                        document.data["userPhone"]?.let { userPhone = it as String }
                        document.data["service"]?.let { service = it as String }
                        document.data["endTime"]?.let { endTime = it as Double }
                        document.data["userAddress"]?.let { userAddress = it as String }
                        document.data["userLocation"]?.let {
                            val location = it as HashMap<*, *>
                            userLocation = LatLng(location["latitude"] as Double, location["longitude"] as Double)
                        }
                        document.data["review"]?.let {
                            val reviewData = it as HashMap<*, *>
                            review = Review(reviewData["rating"] as Double, reviewData["comment"] as String)
                        }
                    }
                    historyTransactions.add(transaction)
                }
                _historyTransaction.value = historyTransactions
            }
            .addOnFailureListener { exception ->
                Log.e("history transaction", exception.toString())
            }
    }
}
