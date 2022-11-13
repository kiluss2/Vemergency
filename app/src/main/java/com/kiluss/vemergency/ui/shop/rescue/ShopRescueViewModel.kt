package com.kiluss.vemergency.ui.shop.rescue

import android.app.Application
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.kiluss.vemergency.data.model.Shop
import com.kiluss.vemergency.data.model.Transaction
import com.kiluss.vemergency.ui.base.BaseViewModel

class ShopRescueViewModel(application: Application) : BaseViewModel(application) {
    private var activeShopLists = mutableListOf<Shop>()
    private var shopCloneLists = mutableListOf<Shop>()
    private val db = Firebase.firestore
    internal var transaction = Transaction()
    internal var currentLocation = LatLng(0.0, 0.0)

    internal fun getShopCloneInfo(position: Int) = shopCloneLists[position]

    internal fun getActiveShopInfo(position: Int) = activeShopLists[position]

    internal fun getShopClone() = shopCloneLists

    internal fun getNearByShopNumber(): Int {
        println(shopCloneLists.size)
        println(activeShopLists.size)
        return shopCloneLists.size + activeShopLists.size
    }
}
