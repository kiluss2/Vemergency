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
import com.kiluss.vemergency.constant.*
import com.kiluss.vemergency.data.firebase.FirebaseManager
import com.kiluss.vemergency.data.model.Shop
import com.kiluss.vemergency.data.model.User
import com.kiluss.vemergency.extension.loadJSONFromAssets
import com.kiluss.vemergency.ui.base.BaseViewModel
import com.kiluss.vemergency.utils.Utils
import org.json.JSONArray
import java.time.Instant

class AdminMainViewModel(application: Application) : BaseViewModel(application) {

    private lateinit var shopGoogleMaps: ArrayList<Shop>
    private var lastDocument: Double? = null
    private var user = User()
    private val db = Firebase.firestore
    private var questionCollectionQuery: Query? = null
    private val currentList = mutableListOf<Shop>()
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
        questionCollectionQuery = db.collection(SHOP_CLONE_COLLECTION)
            .orderBy("lastModifiedTime\$app_debug", Query.Direction.DESCENDING)
        questionCollectionQuery?.let {
            it.limit(ITEM_PER_PAGE)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val list = mutableListOf<Shop>()
                        for (documentSnapshot in task.result) {
                            val item: Shop = documentSnapshot.toObject()
//                        if (item.created == true) {
//                            list.add(item)
//                        }
                            list.add(item)
                        }
                        currentList.clear()
                        currentList.addAll(list)
                        _allShop.value = currentList
                        // Get the last visible document
                        lastDocument = if (task.result.size() > 0) {
                            currentList[currentList.size - 1].lastModifiedTime
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
        questionCollectionQuery?.let {
            it.startAfter(lastDocument)
                .limit(ITEM_PER_PAGE)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val list = mutableListOf<Shop>()
                        for (documentSnapshot in task.result) {
                            val item: Shop = documentSnapshot.toObject()
//                        if (item.created == true) {
//                            list.add(item)
//                        }
                            list.add(item)
                        }
                        currentList.addAll(list)
                        _allShop.value = currentList
                        // Get the last visible document
                        lastDocument = if (task.result.size() > 0) {
                            currentList[currentList.size - 1].lastModifiedTime
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
        FirebaseManager.getAuth()?.signOut() //End user session
        FirebaseManager.logout()
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
                shop.uid = docRef.id
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

