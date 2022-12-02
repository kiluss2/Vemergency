package com.kiluss.vemergency.ui.user.navigation

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
import com.kiluss.vemergency.constant.GEO_HASH
import com.kiluss.vemergency.constant.LATITUDE
import com.kiluss.vemergency.constant.LONGITUDE
import com.kiluss.vemergency.constant.SHOP_CLONE_COLLECTION
import com.kiluss.vemergency.constant.SHOP_COLLECTION
import com.kiluss.vemergency.data.model.Shop
import com.kiluss.vemergency.ui.base.BaseViewModel

/**
 * Created by sonlv on 9/19/2022
 */
class NavigationViewModel(application: Application) : BaseViewModel(application) {
    private var activeShopLists = mutableListOf<Shop>()
    private var shopCloneLists = mutableListOf<Shop>()
    private val db = Firebase.firestore
    private var nearByShopNumber = 0
    private val _allShop: MutableLiveData<MutableList<Shop>> by lazy {
        MutableLiveData<MutableList<Shop>>()
    }
    internal val allShop: LiveData<MutableList<Shop>> = _allShop
    private val _cloneShop: MutableLiveData<MutableList<Shop>> by lazy {
        MutableLiveData<MutableList<Shop>>()
    }
    internal val cloneShop: LiveData<MutableList<Shop>> = _cloneShop

    internal fun getAllShopLocation() {
        db.collection(SHOP_COLLECTION)
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
                    activeShopLists.addAll(list)
                    _allShop.value = activeShopLists
                } else {
                    Log.d("Error getting documents: ", task.exception.toString())
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Navigation Activity", exception.message.toString())
            }
    }

    internal fun getAllCloneShopLocation() {
        db.collection(SHOP_CLONE_COLLECTION)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val list = mutableListOf<Shop>()
                    for (documentSnapshot in task.result) {
                        val item: Shop = documentSnapshot.toObject()
                        list.add(item)
                    }
                    shopCloneLists.addAll(list)
                    _cloneShop.value = shopCloneLists
                } else {
                    Log.d("Error getting documents: ", task.exception.toString())
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Navigation Activity", exception.message.toString())
            }
    }

    // get clone shop near by
    internal fun getNearByCloneShop(location: Location, radiusKmRange: Int) {
        if (radiusKmRange < 200) {
            val center = GeoLocation(location.latitude, location.longitude)
            // query $radiusKmRange km around the location
            val radiusInM = (radiusKmRange * 1000).toDouble()
            val bounds = GeoFireUtils.getGeoHashQueryBounds(center, radiusInM)
            val tasks: MutableList<Task<QuerySnapshot>> = ArrayList()
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
                    val matchingDocs: MutableList<DocumentSnapshot> = ArrayList()
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
                                    matchingDocs.add(doc)
                                }
                            }
                        }
                    }
                    if (matchingDocs.isEmpty()) {
                        getNearByCloneShop(location, radiusKmRange + radiusKmRange % 10 + 1)
                        println(radiusKmRange)
                    } else {
                        // matchingDocs contains the results
                        val list = mutableListOf<Shop>()
                        for (documentSnapshot in matchingDocs) {
                            val item = documentSnapshot.toObject<Shop>()
                            item?.let { it1 -> list.add(it1) }
                        }
                        shopCloneLists.addAll(list)
                        _cloneShop.value = shopCloneLists
                    }
                }
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
                .orderBy("location.$GEO_HASH")
                .startAt(b.startHash)
                .endAt(b.endHash)
            tasks.add(q.get())
        }
        // Collect all the query results together into a single list
        Tasks.whenAllComplete(tasks)
            .addOnCompleteListener {
                val matchingDocs: MutableList<DocumentSnapshot> = ArrayList()
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
                                matchingDocs.add(doc)
                            }
                        }
                    }
                }
                // matchingDocs contains the results
                val list = mutableListOf<Shop>()
                for (documentSnapshot in matchingDocs) {
                    val item = documentSnapshot.toObject<Shop>()
                    item?.let { it1 -> list.add(it1) }
                }
                activeShopLists.addAll(list)
                _allShop.value = activeShopLists
            }
    }

    internal fun getShopCloneInfo(position: Int) = shopCloneLists[position]

    internal fun getActiveShopInfo(position: Int) = activeShopLists[position]

    internal fun getShopClone() = shopCloneLists

    internal fun getNearByShopNumber(): Int {
        return shopCloneLists.size + activeShopLists.size
    }
}
