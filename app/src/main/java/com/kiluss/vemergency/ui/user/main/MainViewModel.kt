package com.kiluss.vemergency.ui.user.main

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.kiluss.vemergency.constant.AVATAR
import com.kiluss.vemergency.constant.SHOP_COVER
import com.kiluss.vemergency.constant.SHOP_NODE
import com.kiluss.vemergency.constant.TEMP_IMAGE
import com.kiluss.vemergency.constant.USER_COLLECTION
import com.kiluss.vemergency.data.firebase.FirebaseManager
import com.kiluss.vemergency.data.model.Shop
import com.kiluss.vemergency.data.model.User
import com.kiluss.vemergency.ui.user.base.BaseViewModel
import com.kiluss.vemergency.utils.Utils
import java.io.File

class MainViewModel(application: Application) : BaseViewModel(application) {

    private var user = User()
    private var myShop: Shop? = null
    val db = Firebase.firestore
    private val _avatarBitmap: MutableLiveData<Bitmap> by lazy {
        MutableLiveData<Bitmap>()
    }
    internal val avatarBitmap: LiveData<Bitmap> = _avatarBitmap
    private val _progressBarStatus: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }
    internal val progressBarStatus: LiveData<Boolean> = _progressBarStatus
    private val _shop: MutableLiveData<Shop> by lazy {
        MutableLiveData<Shop>()
    }
    internal val shop: LiveData<Shop> = _shop
    private val _shopImage: MutableLiveData<Bitmap> by lazy {
        MutableLiveData<Bitmap>()
    }
    internal val shopImage: LiveData<Bitmap> = _shopImage
    private val _userInfo: MutableLiveData<User> by lazy {
        MutableLiveData<User>()
    }
    internal val userInfo: LiveData<User> = _userInfo

    internal fun getUserInfo() {
        FirebaseManager.getAuth()?.uid?.let {
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
        FirebaseManager.getCurrentUser()?.let {
            File("${getApplication<Application>().cacheDir}/$TEMP_IMAGE").mkdirs()
            val localFile = File("${getApplication<Application>().cacheDir}/$TEMP_IMAGE/$AVATAR.jpg")
            if (localFile.exists()) {
                localFile.delete()
            }
            localFile.createNewFile()
            FirebaseManager.getUserAvatarStorageReference()
                .getFile(localFile)
                .addOnCompleteListener {
                    _avatarBitmap.value = BitmapFactory.decodeFile(localFile.absolutePath)
                }.addOnFailureListener {
                    it.printStackTrace()
                }
        }
    }

    internal fun getUserData() = user

    internal fun signOut() {
        FirebaseManager.getAuth()?.signOut() //End user session
        FirebaseManager.logout()
    }

    internal fun getShopData() {
        FirebaseManager.getAuth()?.uid?.let {
            FirebaseManager.getUserInfoDatabaseReference().child(SHOP_NODE)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        _shop.value = snapshot.getValue(Shop::class.java)
                        myShop = snapshot.getValue(Shop::class.java)
                        hideProgressbar()
                        getShopCover()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        hideProgressbar()
                        Utils.showShortToast(getApplication(), "Fail to get user information")
                        Log.e("Main Activity", error.message)
                    }
                })
        }
    }

    private fun hideProgressbar() {
        _progressBarStatus.value = false
    }

    private fun getShopCover() {
        File("${getApplication<Application>().cacheDir}/$TEMP_IMAGE").mkdirs()
        val localFile = File("${getApplication<Application>().cacheDir}/$TEMP_IMAGE/$SHOP_COVER.jpg")
        if (localFile.exists()) {
            localFile.delete()
        }
        localFile.createNewFile()
        FirebaseManager.getShopImageStorageReference()
            .getFile(localFile)
            .addOnCompleteListener {
                val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
                bitmap?.let {
                    _shopImage.value = it
                }
            }.addOnFailureListener {
                it.printStackTrace()
            }
    }
}