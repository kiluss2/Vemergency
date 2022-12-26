package com.kiluss.vemergency.ui.user.main

import android.app.Application
import android.location.Location
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.kiluss.vemergency.constant.GEO_HASH
import com.kiluss.vemergency.constant.HISTORY_TRANSACTION_COLLECTION
import com.kiluss.vemergency.constant.LATITUDE
import com.kiluss.vemergency.constant.LONGITUDE
import com.kiluss.vemergency.constant.SHOP_CLONE_COLLECTION
import com.kiluss.vemergency.constant.SHOP_COLLECTION
import com.kiluss.vemergency.constant.USER_COLLECTION
import com.kiluss.vemergency.data.firebase.FirebaseManager
import com.kiluss.vemergency.data.model.LatLng
import com.kiluss.vemergency.data.model.Review
import com.kiluss.vemergency.data.model.Shop
import com.kiluss.vemergency.data.model.Transaction
import com.kiluss.vemergency.data.model.User
import com.kiluss.vemergency.ui.base.BaseViewModel
import com.kiluss.vemergency.utils.Utils

class MainViewModel(application: Application) : BaseViewModel(application) {
    private var user = User()
    private val db = Firebase.firestore
    private var nearByShopNumber = 0
    private var historyTransactions = mutableListOf<Transaction>()
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
    private val _nearByShopCount: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }
    internal val nearByShopCount: LiveData<Int> = _nearByShopCount
    private val _historyTransaction: MutableLiveData<MutableList<Transaction>> by lazy {
        MutableLiveData<MutableList<Transaction>>()
    }
    internal val historyTransaction: LiveData<MutableList<Transaction>> = _historyTransaction

    internal fun getUserInfo() {
        FirebaseManager.getAuth()?.currentUser?.uid?.let {
            db.collection(USER_COLLECTION)
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

    internal fun getUserData() = user

    internal fun signOut() {
        removeFcmToken()
        FirebaseManager.getAuth()?.signOut() //End user session
        FirebaseManager.logout()
        getUserInfo()
    }

    private fun removeFcmToken() {
        FirebaseManager.getAuth()?.currentUser?.uid?.let { uid ->
            db.collection(Utils.getCollectionRole()).document(uid).update("fcmToken", "")
        }
    }

    private fun hideProgressbar() {
        _progressBarStatus.value = false
    }

    internal fun getNearByCloneShop(location: Location, radiusKmRange: Int) {
        val center = GeoLocation(location.latitude, location.longitude)
        // query $radiusKmRange km around the location
        val radiusInM = (radiusKmRange * 1000).toDouble()
        val bounds = GeoFireUtils.getGeoHashQueryBounds(center, radiusInM)
        val tasks = mutableListOf<Task<QuerySnapshot>>()
        for (b in bounds) {
            val q = db.collection(SHOP_CLONE_COLLECTION)
                .orderBy("location.$GEO_HASH")
                .startAt(b.startHash)
                .endAt(b.endHash)
            tasks.add(q.get())
        }
        // Collect all the query results together into a single list
        Tasks.whenAllComplete(tasks)
            .addOnCompleteListener {
                for (task in tasks) {
                    val snap: QuerySnapshot = task.result
                    for (doc in snap.documents) {
                        val shop = doc.toObject<Shop>()
                        val lat = shop?.location?.getValue(LATITUDE)
                        val lng = shop?.location?.getValue(LONGITUDE)
                        // We have to filter out a few false positives due to GeoHash
                        // accuracy, but most will match
                        if (lat != null && lng != null) {
                            val docLocation = GeoLocation(lat as Double, lng as Double)
                            val distanceInM = GeoFireUtils.getDistanceBetween(docLocation, center)
                            if (distanceInM <= radiusInM) {
                                nearByShopNumber++
                            }
                        }
                    }
                }
                if (nearByShopNumber == 0) {
                    getNearByCloneShop(location, radiusKmRange + radiusKmRange % 10 + 1)
                } else {
                    _nearByShopCount.value = nearByShopNumber
                }
            }
    }

    // get active shop near by
    internal fun getNearByShop(location: Location, radiusKmRange: Int) {
        val center = GeoLocation(location.latitude, location.longitude)
        // query $radiusKmRange km around the location
        val radiusInM = (radiusKmRange * 5000).toDouble()
        val bounds = GeoFireUtils.getGeoHashQueryBounds(center, radiusInM)
        val tasks: MutableList<Task<QuerySnapshot>> = ArrayList()
        for (b in bounds) {
            val q = db.collection(SHOP_COLLECTION)
                .orderBy("location.$GEO_HASH")
                .startAt(b.startHash)
                .endAt(b.endHash)
            tasks.add(q.get())
        }
        // Collect all the query results together into a single list
        Tasks.whenAllComplete(tasks)
            .addOnCompleteListener {
                nearByShopNumber = 0
                for (task in tasks) {
                    val snap: QuerySnapshot = task.result
                    for (doc in snap.documents) {
                        val shop = doc.toObject<Shop>()
                        val lat = shop?.location?.getValue(LATITUDE)
                        val lng = shop?.location?.getValue(LONGITUDE)
                        // We have to filter out a few false positives due to GeoHash accuracy, but most will match
                        if (lat != null && lng != null) {
                            val docLocation = GeoLocation(lat as Double, lng as Double)
                            val distanceInM = GeoFireUtils.getDistanceBetween(docLocation, center)
                            if (distanceInM <= radiusInM) {
                                nearByShopNumber++
                            }
                        }
                    }
                }
                getNearByCloneShop(location, radiusKmRange)
            }
    }

    internal fun getHistoryTransaction() {
        db.collection(HISTORY_TRANSACTION_COLLECTION)
            .whereEqualTo("userId", FirebaseManager.getAuth()?.uid.toString())
            .orderBy("endTime", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                historyTransactions.clear()
                for (document in documents) {
                    val transaction = Transaction().apply {
                        document.data["id"]?.let { id = it as String }
                        document.data["userId"]?.let { userId = it as String }
                        document.data["shopId"]?.let { shopId = it as String }
                        document.data["shopImage"]?.let { shopImage = it as String }
                        document.data["shopName"]?.let { shopName = it as String }
                        document.data["shopPhone"]?.let { shopPhone = it as String }
                        document.data["service"]?.let { service = it as String }
                        document.data["endTime"]?.let { endTime = it as Double }
                        document.data["shopAddress"]?.let { shopAddress = it as String }
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
