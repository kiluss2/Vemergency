package com.kiluss.vemergency.ui.user.emergency

import android.app.Application
import android.location.Location
import android.util.Log
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.kiluss.vemergency.constant.FCM_DEVICE_TOKEN
import com.kiluss.vemergency.constant.GEO_HASH
import com.kiluss.vemergency.constant.LATITUDE
import com.kiluss.vemergency.constant.LONGITUDE
import com.kiluss.vemergency.constant.SEND_NOTI_API_URL
import com.kiluss.vemergency.constant.SHOP_COLLECTION
import com.kiluss.vemergency.constant.USER_COLLECTION
import com.kiluss.vemergency.data.firebase.FirebaseManager
import com.kiluss.vemergency.data.model.Shop
import com.kiluss.vemergency.data.model.Transaction
import com.kiluss.vemergency.data.model.User
import com.kiluss.vemergency.network.api.ApiService
import com.kiluss.vemergency.network.api.RetrofitClient
import com.kiluss.vemergency.ui.base.BaseViewModel
import com.kiluss.vemergency.utils.SharedPrefManager
import com.kiluss.vemergency.utils.Utils
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Created by sonlv on 11/11/2022
 */
class CreateEmergencyViewModel(application: Application) : BaseViewModel(application) {
    internal var queryNearShop = true
    private val db = Firebase.firestore
    internal val shopLists = mutableListOf<Shop>()

    // get active shop near by
    internal fun getNearByShop(location: Location, radiusKmRange: Int, transaction: Transaction) {
        if (radiusKmRange >= 200) {
            Utils.showLongToast(getApplication(), "Can't find any service near by you")
        } else if (queryNearShop) {
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
                    if (matchingDocs.isEmpty()) {
                        getNearByShop(location, radiusKmRange + radiusKmRange % 10 + 1, transaction)
                        println(radiusKmRange + radiusKmRange % 10 + 1)
                    } else {
                        // matchingDocs contains the results
                        val list = mutableListOf<Shop>()
                        for (documentSnapshot in matchingDocs) {
                            val item = documentSnapshot.toObject<Shop>()
                            item?.let { shop ->
                                val token = shop.fcmToken
                                if (token != null && token.isNotEmpty() && shop.created == true) {
                                    list.add(shop)
                                }
                            }
                        }
                        if (list.isNotEmpty()) {
                            shopLists.addAll(list)
                        } else {
                            getNearByShop(location, radiusKmRange + radiusKmRange % 10 + 1, transaction)
                            println(radiusKmRange + radiusKmRange % 10 + 1)
                        }
                    }
                }
        }
    }

    internal fun sendEmergency(shopLists: MutableList<Shop>, transaction: Transaction) {
        val tokens = JsonArray()
        val shopIds = JsonArray()
        shopLists.forEach {
            val shopToken = it.fcmToken
            if (shopToken != null && shopToken.isNotEmpty() && it.created == true) {
                tokens.add(shopToken)
                shopIds.add(it.id)
            }
        }
        FirebaseManager.getAuth()?.uid?.let {
            db.collection(USER_COLLECTION)
                .document(it)
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    documentSnapshot.toObject<User>()?.let { userInfo ->
                        with(transaction) {
                            userFcmToken = SharedPrefManager.getString(FCM_DEVICE_TOKEN, "")
                            userFullName = userInfo.fullName
                            userPhone = userInfo.phone
                            address = userInfo.address
                        }
                        val request = JSONObject()
                        request.put("transaction", Json.encodeToString(transaction))
                        request.put("tokens", tokens)
                        request.put("shops", shopIds)
                        println(request)
                        RetrofitClient.getInstance(getApplication()).getClientUnAuthorize(SEND_NOTI_API_URL)
                            .create(ApiService::class.java)
                            .sendNoti(request.toString().toRequestBody())
                            .enqueue(object : Callback<JsonObject?> {
                                override fun onResponse(
                                    call: Call<JsonObject?>,
                                    response: Response<JsonObject?>
                                ) {
                                    when {
                                        response.isSuccessful -> {
                                            Log.e("createEmergency", response.body().toString())
                                        }
                                        else -> {
                                            Utils.showShortToast(getApplication(), "failCreateEmergency")
                                        }
                                    }
                                }

                                override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
                                    Log.e("failCreateEmergency", t.toString())
                                    Utils.showShortToast(getApplication(), "failCreateEmergency")
                                    t.printStackTrace()
                                }
                            })
                    }
                }
                .addOnFailureListener { exception ->
                    Utils.showShortToast(getApplication(), "Fail to get user information")
                    Log.e("UserProfileActivity", exception.message.toString())
                }
        }
    }
}
