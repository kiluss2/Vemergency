package com.kiluss.vemergency.ui.user.navigation

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.kiluss.vemergency.constant.ITEM_PER_PAGE
import com.kiluss.vemergency.constant.SHOP_COLLECTION
import com.kiluss.vemergency.constant.USER_NODE
import com.kiluss.vemergency.data.firebase.FirebaseManager
import com.kiluss.vemergency.data.model.Shop
import com.kiluss.vemergency.data.model.User
import com.kiluss.vemergency.ui.base.BaseViewModel

/**
 * Created by sonlv on 9/19/2022
 */
class NavigationViewModel(application: Application) : BaseViewModel(application) {

    private var shopLists = mutableListOf<Shop>()
    private val db = Firebase.firestore
    private val _allShopLocation: MutableLiveData<MutableList<Shop>> by lazy {
        MutableLiveData<MutableList<Shop>>()
    }
    internal val allShopLocation: LiveData<MutableList<Shop>> = _allShopLocation

    internal fun getAllShopLocation() {
        // Most viewed posts
        FirebaseManager.getDatabaseReference()?.child(USER_NODE)?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                shopLists.clear()
                for (data in snapshot.children) {
                    data.getValue(User::class.java)?.let {
                        //it.shop?.let { it1 -> shopLists.add(it1) }
                    }
                }
                _allShopLocation.value = shopLists
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Navigation Activity", error.message)
            }
        })
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
}
