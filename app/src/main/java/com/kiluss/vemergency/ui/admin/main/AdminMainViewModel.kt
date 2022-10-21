package com.kiluss.vemergency.ui.admin.main

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.kiluss.vemergency.constant.ADMIN_COLLECTION
import com.kiluss.vemergency.constant.ITEM_PER_PAGE
import com.kiluss.vemergency.constant.SHOP_COLLECTION
import com.kiluss.vemergency.constant.SHOP_PENDING_COLLECTION
import com.kiluss.vemergency.data.firebase.FirebaseManager
import com.kiluss.vemergency.data.model.Shop
import com.kiluss.vemergency.data.model.User
import com.kiluss.vemergency.ui.base.BaseViewModel

class AdminMainViewModel(application: Application) : BaseViewModel(application) {

    private var lastDocument: DocumentSnapshot? = null
    private var user = User()
    private val db = Firebase.firestore
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
        db.collection(SHOP_COLLECTION)
            .orderBy("lastModifiedTime\$app_debug", Query.Direction.DESCENDING)
            .limit(ITEM_PER_PAGE)
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
                    currentList.addAll(list)
                    _allShop.value = currentList
                    // Get the last visible document
                    lastDocument = if (task.result.size() > 0) {
                        task.result.documents[task.result.size() - 1]
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

    internal fun getMoreActiveShop() {
        db.collection(SHOP_COLLECTION)
            .orderBy("lastModifiedTime\$app_debug", Query.Direction.DESCENDING)
            .startAfter(lastDocument)
            .limit(ITEM_PER_PAGE)
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
                    currentList.addAll(list)
                    _allShop.value = currentList
                    // Get the last visible document
                    lastDocument = if (task.result.size() > 0) {
                        task.result.documents[task.result.size() - 1]
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

    internal fun getUserData() = user

    internal fun signOut() {
        FirebaseManager.getAuth()?.signOut() //End user session
        FirebaseManager.logout()
    }

    private fun hideProgressbar() {
        _progressBarStatus.value = false
    }
}
