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
import com.kiluss.vemergency.constant.*
import com.kiluss.vemergency.data.model.Shop
import com.kiluss.vemergency.network.api.ApiService
import com.kiluss.vemergency.network.api.RetrofitClient
import com.kiluss.vemergency.ui.base.BaseViewModel
import com.kiluss.vemergency.utils.Utils
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Created by sonlv on 11/11/2022
 */
class CreateEmergencyViewModel(application: Application) : BaseViewModel(application) {

    internal var isStarting = false
    internal var queryNearShop = true
    private val db = Firebase.firestore
    internal val shopLists = mutableListOf<Shop>()

    // get active shop near by
    internal fun getNearByShop(location: Location, radiusKmRange: Int) {
        if (radiusKmRange >= 200) {
            Utils.showLongToast(getApplication(), "Can't find any service near by you")
        } else if (isStarting) {
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
                        getNearByShop(location, radiusKmRange + radiusKmRange % 10 + 1)
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
                            if (isStarting) {
                                sendEmergency(shopLists)
                            }
                        } else {
                            getNearByShop(location, radiusKmRange + radiusKmRange % 10 + 1)
                            println(radiusKmRange + radiusKmRange % 10 + 1)
                        }
                    }
                }
        }
    }

    internal fun sendEmergency(shopLists: MutableList<Shop>) {
        val tokens = JsonArray()
        shopLists.forEach {
            val token = it.fcmToken
            if (token != null && token.isNotEmpty()) {
                tokens.add(token)
                println(it)
            }
        }
        RetrofitClient.getInstance(getApplication()).getClientUnAuthorize(SEND_NOTI_API_URL)
            .create(ApiService::class.java)
            .sendNoti(tokens.toString().toRequestBody())
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