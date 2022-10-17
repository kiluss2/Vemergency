package com.kiluss.vemergency.ui.shop.main

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.kiluss.vemergency.constant.SHOP_COLLECTION
import com.kiluss.vemergency.constant.SHOP_COVER
import com.kiluss.vemergency.constant.TEMP_IMAGE
import com.kiluss.vemergency.data.firebase.FirebaseManager
import com.kiluss.vemergency.data.model.Shop
import com.kiluss.vemergency.ui.user.base.BaseViewModel
import java.io.File

/**
 * Created by sonlv on 10/17/2022
 */
class ShopMainViewModel(application: Application) : BaseViewModel(application) {

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
    private val _navigateToHome: MutableLiveData<Any> by lazy {
        MutableLiveData<Any>()
    }
    internal val navigateToHome: LiveData<Any> = _navigateToHome

    internal fun getShopInfo() {
        FirebaseManager.getAuth()?.currentUser?.uid?.let {
            db.collection(SHOP_COLLECTION)
                .document(it)
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    val result = documentSnapshot.toObject<Shop>()
                    if (result != null) {
                        _shop.value = result
                        myShop = result
                    } else {
                        _shop.value = null
                        myShop = null
                    }
                }
                .addOnFailureListener { exception ->
                    hideProgressbar()
                    Log.e("Main Activity", exception.message.toString())
                    _shop.value = null
                    myShop = null
                }
            getShopCover()
        }
    }

    internal fun getShopData() = myShop

    internal fun signOut() {
        FirebaseManager.getAuth()?.signOut() //End user session
        FirebaseManager.logout()
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

    internal fun navigateToHome() {
        _navigateToHome.value = null
    }
}
