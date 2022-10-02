package com.kiluss.vemergency.ui.main

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.kiluss.vemergency.constant.AVATAR
import com.kiluss.vemergency.constant.TEMP_IMAGE
import com.kiluss.vemergency.data.firebase.FirebaseManager
import com.kiluss.vemergency.ui.base.BaseViewModel
import java.io.File

class MainViewModel(application: Application) : BaseViewModel(application) {

    private val _avatarBitmap: MutableLiveData<Bitmap> by lazy {
        MutableLiveData<Bitmap>()
    }
    val avatarBitmap: LiveData<Bitmap> = _avatarBitmap

    internal fun getUserInfo() {
        FirebaseManager.getCurrentUser()?.let {
            File("${getApplication<Application>().cacheDir}/$TEMP_IMAGE").mkdirs()
            val localFile = File("${getApplication<Application>().cacheDir}/$TEMP_IMAGE/$AVATAR.jpg")
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

    internal fun signOut() {
        FirebaseManager.getAuth()?.signOut() //End user session
    }
}
