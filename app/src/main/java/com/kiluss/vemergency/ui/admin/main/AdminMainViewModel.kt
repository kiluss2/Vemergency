package com.kiluss.vemergency.ui.admin.main

import android.app.Application
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.kiluss.vemergency.constant.ACTIVE_SHOP_ITEM_PER_PAGE
import com.kiluss.vemergency.constant.ADMIN_COLLECTION
import com.kiluss.vemergency.constant.CURRENT_TRANSACTION_COLLECTION
import com.kiluss.vemergency.constant.GEO_HASH
import com.kiluss.vemergency.constant.HISTORY_TRANSACTION_COLLECTION
import com.kiluss.vemergency.constant.LATITUDE
import com.kiluss.vemergency.constant.LONGITUDE
import com.kiluss.vemergency.constant.PENDING_TRANSACTION_COLLECTION
import com.kiluss.vemergency.constant.SHOP_CLONE_COLLECTION
import com.kiluss.vemergency.constant.SHOP_COLLECTION
import com.kiluss.vemergency.constant.SHOP_PENDING_COLLECTION
import com.kiluss.vemergency.constant.TRANSACTION_ITEM_PER_PAGE
import com.kiluss.vemergency.constant.USER_COLLECTION
import com.kiluss.vemergency.data.firebase.FirebaseManager
import com.kiluss.vemergency.data.model.Shop
import com.kiluss.vemergency.data.model.Transaction
import com.kiluss.vemergency.data.model.User
import com.kiluss.vemergency.extension.loadJSONFromAssets
import com.kiluss.vemergency.ui.base.BaseViewModel
import com.kiluss.vemergency.utils.Utils
import org.json.JSONArray
import java.time.Instant

class AdminMainViewModel(application: Application) : BaseViewModel(application) {
    private lateinit var shopGoogleMaps: ArrayList<Shop>
    private var lastActiveShopModifiedTime: Double? = null
    private var lastUserModifiedTime: Double? = null
    private var lastHistoryTransactionModifiedTime: Double? = null
    private var lastPendingTransactionModifiedTime: Double? = null
    private var lastCurrentTransactionModifiedTime: Double? = null
    private var user = User()
    private val db = Firebase.firestore
    private var activeShopCollectionQuery: Query? = null
    private var allUserCollectionQuery: Query? = null
    private var historyTransactionCollectionQuery: Query? = null
    private var pendingTransactionCollectionQuery: Query? = null
    private var currentTransactionCollectionQuery: Query? = null
    private val currentShopList = mutableListOf<Shop>()
    private val currentUserList = mutableListOf<User>()
    private val currentHistoryTransactionList = mutableListOf<Transaction>()
    private val currentPendingTransactionList = mutableListOf<Transaction>()
    private val currentCurrentTransactionList = mutableListOf<Transaction>()
    private val _progressBarStatus: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }
    internal val progressBarStatus: LiveData<Boolean> = _progressBarStatus
    private val _shop: MutableLiveData<Shop> by lazy {
        MutableLiveData<Shop>()
    }
    internal val shop: LiveData<Shop> = _shop
    private val _userInfo: MutableLiveData<User> by lazy {
        MutableLiveData<User>()
    }
    internal val userInfo: LiveData<User> = _userInfo
    private val _shopPending: MutableLiveData<MutableList<Shop>> by lazy {
        MutableLiveData<MutableList<Shop>>()
    }
    internal val shopPending: LiveData<MutableList<Shop>> = _shopPending
    private val _allShop: MutableLiveData<MutableList<Shop>> by lazy {
        MutableLiveData<MutableList<Shop>>()
    }
    internal val allShop: LiveData<MutableList<Shop>> = _allShop
    private val _allUser: MutableLiveData<MutableList<User>> by lazy {
        MutableLiveData<MutableList<User>>()
    }
    internal val allUser: LiveData<MutableList<User>> = _allUser
    private val _historyTransaction: MutableLiveData<MutableList<Transaction>> by lazy {
        MutableLiveData<MutableList<Transaction>>()
    }
    internal val historyTransaction: LiveData<MutableList<Transaction>> = _historyTransaction
    private val _pendingTransaction: MutableLiveData<MutableList<Transaction>> by lazy {
        MutableLiveData<MutableList<Transaction>>()
    }
    internal val pendingTransaction: LiveData<MutableList<Transaction>> = _pendingTransaction
    private val _currentTransaction: MutableLiveData<MutableList<Transaction>> by lazy {
        MutableLiveData<MutableList<Transaction>>()
    }
    internal val currentTransaction: LiveData<MutableList<Transaction>> = _currentTransaction

    internal fun getUserInfo() {
        FirebaseManager.getAuth()?.currentUser?.uid?.let {
            db.collection(ADMIN_COLLECTION)
                .document(it)
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    documentSnapshot.toObject<User>()?.let { result ->
                        user = result
                        _userInfo.value = user
                    }
                }
                .addOnFailureListener { exception ->
                    hideProgressbar()
                    Log.e("Main Activity", exception.message.toString())
                }
        }
        if (FirebaseManager.getAuth()?.currentUser == null) {
            user = User()
            _userInfo.value = user
        }
    }

    internal fun getShopPendingInfo() {
        db.collection(SHOP_PENDING_COLLECTION)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val list = mutableListOf<Shop>()
                    for (documentSnapshot in task.result) {
                        list.add(documentSnapshot.toObject())
                    }
                    _shopPending.value = list
                } else {
                    Log.d("Error getting documents: ", task.exception.toString())
                }
            }
            .addOnFailureListener { exception ->
                hideProgressbar()
                Log.e("Main Activity", exception.message.toString())
            }
    }

    internal fun getActiveShop() {
        activeShopCollectionQuery = db.collection(SHOP_COLLECTION)
            .orderBy("lastModifiedTime", Query.Direction.DESCENDING)
        activeShopCollectionQuery?.let {
            it.limit(ACTIVE_SHOP_ITEM_PER_PAGE)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val list = mutableListOf<Shop>()
                        for (documentSnapshot in task.result) {
                            val item: Shop = documentSnapshot.toObject()
                            if (item.created == true) {
                                list.add(item)
                            }
                        }
                        currentShopList.clear()
                        currentShopList.addAll(list)
                        _allShop.value = currentShopList
                        // Get the last visible document
                        lastActiveShopModifiedTime = if (task.result.size() > 0) {
                            currentShopList[currentShopList.size - 1].lastModifiedTime
                        } else {
                            null
                        }
                    } else {
                        Log.d("Error getting documents: ", task.exception.toString())
                    }
                }
                .addOnFailureListener { exception ->
                    hideProgressbar()
                    Log.e("Main Activity", exception.message.toString())
                }
        }
    }

    internal fun getMoreActiveShop() {
        activeShopCollectionQuery?.let {
            it.startAfter(lastActiveShopModifiedTime)
                .limit(ACTIVE_SHOP_ITEM_PER_PAGE)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val list = mutableListOf<Shop>()
                        for (documentSnapshot in task.result) {
                            val item: Shop = documentSnapshot.toObject()
                            if (item.created == true) {
                                list.add(item)
                            }
                        }
                        currentShopList.addAll(list)
                        _allShop.value = currentShopList
                        // Get the last visible document
                        lastActiveShopModifiedTime = if (task.result.size() > 0) {
                            currentShopList[currentShopList.size - 1].lastModifiedTime
                        } else {
                            null
                        }
                    } else {
                        Log.d("Error getting documents: ", task.exception.toString())
                    }
                }
                .addOnFailureListener { exception ->
                    hideProgressbar()
                    Log.e("Main Activity", exception.message.toString())
                }
        }
    }

    internal fun getAllUser() {
        allUserCollectionQuery = db.collection(USER_COLLECTION)
            .orderBy("lastModifiedTime", Query.Direction.DESCENDING)
        allUserCollectionQuery?.let {
            it.limit(ACTIVE_SHOP_ITEM_PER_PAGE)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val list = mutableListOf<User>()
                        for (documentSnapshot in task.result) {
                            val item: User = documentSnapshot.toObject()
                            list.add(item)
                        }
                        currentUserList.clear()
                        currentUserList.addAll(list)
                        _allUser.value = currentUserList
                        // Get the last visible document
                        lastUserModifiedTime = if (task.result.size() > 0) {
                            currentUserList[currentUserList.size - 1].lastModifiedTime
                        } else {
                            null
                        }
                    } else {
                        Log.d("Error getting documents: ", task.exception.toString())
                    }
                }
                .addOnFailureListener { exception ->
                    hideProgressbar()
                    Log.e("Main Activity", exception.message.toString())
                }
        }
    }

    internal fun getMoreAllUser() {
        allUserCollectionQuery?.let {
            it.startAfter(lastUserModifiedTime)
                .limit(ACTIVE_SHOP_ITEM_PER_PAGE)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val list = mutableListOf<User>()
                        for (documentSnapshot in task.result) {
                            val item: User = documentSnapshot.toObject()
                            list.add(item)
                        }
                        currentUserList.addAll(list)
                        _allUser.value = currentUserList
                        // Get the last visible document
                        lastUserModifiedTime = if (task.result.size() > 0) {
                            currentUserList[currentUserList.size - 1].lastModifiedTime
                        } else {
                            null
                        }
                    } else {
                        Log.d("Error getting documents: ", task.exception.toString())
                    }
                }
                .addOnFailureListener { exception ->
                    hideProgressbar()
                    Log.e("Main Activity", exception.message.toString())
                }
        }
    }

    internal fun getHistoryTransaction() {
        historyTransactionCollectionQuery = db.collection(HISTORY_TRANSACTION_COLLECTION)
            .orderBy("endTime", Query.Direction.DESCENDING)
        historyTransactionCollectionQuery?.let {
            it.limit(TRANSACTION_ITEM_PER_PAGE)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val list = mutableListOf<Transaction>()
                        for (documentSnapshot in task.result) {
                            val item: Transaction = documentSnapshot.toObject()
                            list.add(item)
                        }
                        currentHistoryTransactionList.clear()
                        currentHistoryTransactionList.addAll(list)
                        _historyTransaction.value = currentHistoryTransactionList
                        // Get the last visible document
                        lastHistoryTransactionModifiedTime = if (task.result.size() > 0) {
                            currentHistoryTransactionList[currentHistoryTransactionList.size - 1].endTime
                        } else {
                            null
                        }
                    } else {
                        Log.d("Error getting documents: ", task.exception.toString())
                    }
                }
                .addOnFailureListener { exception ->
                    hideProgressbar()
                    Log.e("Main Activity", exception.message.toString())
                }
        }
    }

    internal fun getMoreHistoryTransaction() {
        historyTransactionCollectionQuery?.let {
            it.startAfter(lastHistoryTransactionModifiedTime)
                .limit(TRANSACTION_ITEM_PER_PAGE)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val list = mutableListOf<Transaction>()
                        for (documentSnapshot in task.result) {
                            val item: Transaction = documentSnapshot.toObject()
                            list.add(item)
                        }
                        currentHistoryTransactionList.addAll(list)
                        _historyTransaction.value = currentHistoryTransactionList
                        // Get the last visible document
                        lastHistoryTransactionModifiedTime = if (task.result.size() > 0) {
                            currentHistoryTransactionList[currentHistoryTransactionList.size - 1].endTime
                        } else {
                            null
                        }
                    } else {
                        Log.d("Error getting documents: ", task.exception.toString())
                    }
                }
                .addOnFailureListener { exception ->
                    hideProgressbar()
                    Log.e("Main Activity", exception.message.toString())
                }
        }
    }

    internal fun getPendingTransaction() {
        pendingTransactionCollectionQuery = db.collection(PENDING_TRANSACTION_COLLECTION)
            .orderBy("endTime", Query.Direction.DESCENDING)
        pendingTransactionCollectionQuery?.let {
            it.limit(TRANSACTION_ITEM_PER_PAGE)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val list = mutableListOf<Transaction>()
                        for (documentSnapshot in task.result) {
                            val item: Transaction = documentSnapshot.toObject()
                            list.add(item)
                        }
                        currentPendingTransactionList.clear()
                        currentPendingTransactionList.addAll(list)
                        _pendingTransaction.value = currentPendingTransactionList
                        // Get the last visible document
                        lastPendingTransactionModifiedTime = if (task.result.size() > 0) {
                            currentPendingTransactionList[currentPendingTransactionList.size - 1].endTime
                        } else {
                            null
                        }
                    } else {
                        Log.d("Error getting documents: ", task.exception.toString())
                    }
                }
                .addOnFailureListener { exception ->
                    hideProgressbar()
                    Log.e("Main Activity", exception.message.toString())
                }
        }
    }

    internal fun getMorePendingTransaction() {
        pendingTransactionCollectionQuery?.let {
            it.startAfter(lastPendingTransactionModifiedTime)
                .limit(TRANSACTION_ITEM_PER_PAGE)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val list = mutableListOf<Transaction>()
                        for (documentSnapshot in task.result) {
                            val item: Transaction = documentSnapshot.toObject()
                            list.add(item)
                        }
                        currentPendingTransactionList.addAll(list)
                        _pendingTransaction.value = currentPendingTransactionList
                        // Get the last visible document
                        lastPendingTransactionModifiedTime = if (task.result.size() > 0) {
                            currentPendingTransactionList[currentUserList.size - 1].endTime
                        } else {
                            null
                        }
                    } else {
                        Log.d("Error getting documents: ", task.exception.toString())
                    }
                }
                .addOnFailureListener { exception ->
                    hideProgressbar()
                    Log.e("Main Activity", exception.message.toString())
                }
        }
    }

    internal fun getCurrentTransaction() {
        currentTransactionCollectionQuery = db.collection(CURRENT_TRANSACTION_COLLECTION)
            .orderBy("endTime", Query.Direction.DESCENDING)
        currentTransactionCollectionQuery?.let {
            it.limit(TRANSACTION_ITEM_PER_PAGE)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val list = mutableListOf<Transaction>()
                        for (documentSnapshot in task.result) {
                            val item: Transaction = documentSnapshot.toObject()
                            list.add(item)
                        }
                        currentCurrentTransactionList.clear()
                        currentCurrentTransactionList.addAll(list)
                        _currentTransaction.value = currentCurrentTransactionList
                        // Get the last visible document
                        lastCurrentTransactionModifiedTime = if (task.result.size() > 0) {
                            currentCurrentTransactionList[currentCurrentTransactionList.size - 1].endTime
                        } else {
                            null
                        }
                    } else {
                        Log.d("Error getting documents: ", task.exception.toString())
                    }
                }
                .addOnFailureListener { exception ->
                    hideProgressbar()
                    Log.e("Main Activity", exception.message.toString())
                }
        }
    }

    internal fun getMoreCurrentTransaction() {
        currentTransactionCollectionQuery?.let {
            it.startAfter(lastCurrentTransactionModifiedTime)
                .limit(TRANSACTION_ITEM_PER_PAGE)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val list = mutableListOf<Transaction>()
                        for (documentSnapshot in task.result) {
                            val item: Transaction = documentSnapshot.toObject()
                            list.add(item)
                        }
                        currentCurrentTransactionList.addAll(list)
                        _currentTransaction.value = currentCurrentTransactionList
                        // Get the last visible document
                        lastCurrentTransactionModifiedTime = if (task.result.size() > 0) {
                            currentCurrentTransactionList[currentUserList.size - 1].endTime
                        } else {
                            null
                        }
                    } else {
                        Log.d("Error getting documents: ", task.exception.toString())
                    }
                }
                .addOnFailureListener { exception ->
                    hideProgressbar()
                    Log.e("Main Activity", exception.message.toString())
                }
        }
    }

    internal fun getUserData() = user

    internal fun signOut() {
        removeFcmToken()
        FirebaseManager.getAuth()?.signOut() //End user session
        FirebaseManager.logout()
    }

    private fun removeFcmToken() {
        FirebaseManager.getAuth()?.currentUser?.uid?.let { uid ->
            db.collection(Utils.getCollectionRole()).document(uid).update("fcmToken", "")
        }
    }

    private fun hideProgressbar() {
        _progressBarStatus.value = false
    }

    internal fun bindJSONDataInShopList(context: Context) {
        shopGoogleMaps = ArrayList()
        val shopJsonArray = JSONArray(context.loadJSONFromAssets("result.json")) // Extension Function call here
        for (i in 0 until shopJsonArray.length()) {
            val shop = Shop()
            val shopJSONObject = shopJsonArray.getJSONObject(i)
            if (shopJSONObject.has(LATITUDE) && shopJSONObject.has(LONGITUDE)) {
                shop.location = HashMap<String, Any>().apply {
                    put(
                        GEO_HASH,
                        GeoFireUtils.getGeoHashForLocation(
                            GeoLocation(
                                shopJSONObject.getDouble(LATITUDE),
                                shopJSONObject.getDouble(LONGITUDE)
                            )
                        )
                    )
                    put(LATITUDE, shopJSONObject.getDouble(LATITUDE))
                    put(LONGITUDE, shopJSONObject.getDouble(LONGITUDE))
                }
                shop.service = shopJSONObject.getString("category")
                shop.name = shopJSONObject.getString("title")
                shop.rating = if (shopJSONObject.getString("rating").isNotEmpty()) {
                    shopJSONObject.getString("rating").toDouble()
                } else {
                    null
                }
                shop.reviewCount = if (shopJSONObject.getString("reviewCount").isNotEmpty()) {
                    shopJSONObject.getLong("reviewCount")
                } else {
                    null
                }
                shop.address = shopJSONObject.getString("address")
                shop.website = shopJSONObject.getString("website")
                shop.phone = shopJSONObject.getString("phoneNumber")
                if (shopJSONObject.has("monday")) {
                    shop.openTime = shopJSONObject.getString("monday")
                }
                shop.imageUrl = shopJSONObject.getString("imgUrl")
                shop.created = false
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    shop.lastModifiedTime = Instant.parse(shopJSONObject.getString("timestamp"))
                        .toEpochMilli().toDouble()
                }
                shopGoogleMaps.add(shop)
            }
        }
        pushGoogleMapData(shopGoogleMaps)
    }

    private fun pushGoogleMapData(shops: ArrayList<Shop>) {
        val collectionRef = db.collection(SHOP_CLONE_COLLECTION)
        db.runBatch { batch ->
            for (shop in shops) {
                val docRef = collectionRef.document()
                shop.id = docRef.id
                batch.set(docRef, shop)
            }
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Utils.showShortToast(getApplication(), "upload success")
            } else {
                Utils.showShortToast(getApplication(), "upload error")
                Log.d("Error getting documents: ", task.exception.toString())
            }
        }.addOnFailureListener { exception ->
            hideProgressbar()
            Log.e("Main Activity", exception.message.toString())
        }
    }

    internal fun getShopCloneSize() {
        db.collection(SHOP_CLONE_COLLECTION)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    println("clone " + task.result.size())
                } else {
                    Log.d("Error getting documents: ", task.exception.toString())
                }
            }
            .addOnFailureListener { exception ->
                hideProgressbar()
                Log.e("Main Activity", exception.message.toString())
            }
    }
}

