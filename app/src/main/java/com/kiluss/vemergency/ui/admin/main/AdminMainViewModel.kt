package com.kiluss.vemergency.ui.admin.main

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.kiluss.vemergency.constant.ADMIN_COLLECTION
import com.kiluss.vemergency.constant.SHOP_PENDING_COLLECTION
import com.kiluss.vemergency.data.firebase.FirebaseManager
import com.kiluss.vemergency.data.model.Shop
import com.kiluss.vemergency.data.model.User
import com.kiluss.vemergency.ui.base.BaseViewModel

class AdminMainViewModel(application: Application) : BaseViewModel(application) {

    private var user = User()
    val db = Firebase.firestore
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
        FirebaseManager.getAuth()?.currentUser?.uid?.let {
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
