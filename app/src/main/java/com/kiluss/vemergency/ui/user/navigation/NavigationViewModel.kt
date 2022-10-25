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
import com.kiluss.vemergency.constant.*
import com.kiluss.vemergency.data.model.Shop
import com.kiluss.vemergency.ui.base.BaseViewModel

/**
 * Created by sonlv on 9/19/2022
 */
class NavigationViewModel(application: Application) : BaseViewModel(application) {

    private var shopLists = mutableListOf<Shop>()
    private var shopCloneLists = mutableListOf<Shop>()
    private val db = Firebase.firestore
    private val _allShopLocation: MutableLiveData<MutableList<Shop>> by lazy {
        MutableLiveData<MutableList<Shop>>()
    }
    internal val allShopLocation: LiveData<MutableList<Shop>> = _allShopLocation
    private val _allCloneShopLocation: MutableLiveData<MutableList<Shop>> by lazy {
        MutableLiveData<MutableList<Shop>>()
    }
    internal val allCloneShopLocation: LiveData<MutableList<Shop>> = _allCloneShopLocation

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
                    shopLists.addAll(list)
                    _allShopLocation.value = shopLists
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
                    _allCloneShopLocation.value = shopCloneLists
                } else {
                    Log.d("Error getting documents: ", task.exception.toString())
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Navigation Activity", exception.message.toString())
            }
    }

    internal fun getNearByShop(location: Location) {
//        val center = GeoLocation(location.latitude, location.longitude)
//        // query 5km around the location
//        val radiusInM = (10 * 1000).toDouble()
//        val bounds = GeoFireUtils.getGeoHashQueryBounds(center, radiusInM)
//        val tasks: MutableList<Task<QuerySnapshot>> = ArrayList()
//        for (b in bounds) {
//            val q = db.collection(SHOP_CLONE_COLLECTION)
//                .orderBy(GEO_HASH)
//                .startAt(b.startHash)
//                .endAt(b.endHash)
//            tasks.add(q.get())
//        }
//        // Collect all the query results together into a single list
//        Tasks.whenAllComplete(tasks)
//            .addOnCompleteListener {
//                val matchingDocs: MutableList<DocumentSnapshot> = ArrayList()
//                for (task in tasks) {
//                    val snap: QuerySnapshot = task.result
//                    for (doc in snap.documents) {
//                        val shop = doc.toObject<Shop>()
//                        val lat = shop?.location?.getValue(LATITUDE)
//                        val lng = shop?.location?.getValue(LONGITUDE)
//                        // We have to filter out a few false positives due to GeoHash
//                        // accuracy, but most will match
//                        if (lat != null && lng != null) {
//                            val docLocation = GeoLocation(lat as Double, lng as Double)
//                            val distanceInM = GeoFireUtils.getDistanceBetween(docLocation, center)
//                            if (distanceInM <= radiusInM) {
//                                matchingDocs.add(doc)
//                            }
//                        }
//                    }
//                }
//                // matchingDocs contains the results
//                val list = mutableListOf<Shop>()
//                for (documentSnapshot in matchingDocs) {
//                    val item = documentSnapshot.toObject<Shop>()
//                    item?.let { it1 -> list.add(it1) }
//                }
//                shopCloneLists.addAll(list)
//                _allCloneShopLocation.value = shopCloneLists
//            }
        db.collection(SHOP_CLONE_COLLECTION)
            //.orderBy("lastModifiedTime")
//            .startAt("w6ugma")
//            .endAt("w6ugmz")
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val list = mutableListOf<Shop>()
                    for (documentSnapshot in task.result) {
                        val item: Shop = documentSnapshot.toObject()
                        list.add(item)
                    }
                    shopCloneLists.addAll(list)
                    _allCloneShopLocation.value = shopCloneLists
                } else {
                    Log.d("Error getting documents: ", task.exception.toString())
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Navigation Activity", exception.message.toString())
            }
    }
}
