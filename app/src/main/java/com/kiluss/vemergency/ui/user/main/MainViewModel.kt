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
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.kiluss.vemergency.constant.*
import com.kiluss.vemergency.data.firebase.FirebaseManager
import com.kiluss.vemergency.data.model.Shop
import com.kiluss.vemergency.data.model.User
import com.kiluss.vemergency.ui.base.BaseViewModel

class MainViewModel(application: Application) : BaseViewModel(application) {

    private var user = User()
    private val db = Firebase.firestore
    private var nearByShopNumber = 0
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
        FirebaseManager.getAuth()?.signOut() //End user session
        FirebaseManager.logout()
        getUserInfo()
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
                .orderBy("location\$app_debug.$GEO_HASH")
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
                _nearByShopCount.value = nearByShopNumber
            }
    }

    // get active shop near by
    internal fun getNearByShop(location: Location, radiusKmRange: Int) {
        val center = GeoLocation(location.latitude, location.longitude)
        // query $radiusKmRange km around the location
        val radiusInM = (radiusKmRange * 1000).toDouble()
        val bounds = GeoFireUtils.getGeoHashQueryBounds(center, radiusInM)
        val tasks: MutableList<Task<QuerySnapshot>> = ArrayList()
        for (b in bounds) {
            val q = db.collection(SHOP_COLLECTION)
                .orderBy("location\$app_debug.$GEO_HASH")
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
                getNearByCloneShop(location, radiusKmRange)
            }
    }
}
