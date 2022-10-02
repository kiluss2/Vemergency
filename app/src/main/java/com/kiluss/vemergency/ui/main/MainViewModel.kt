package com.kiluss.vemergency.ui.main

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.kiluss.vemergency.constant.TEMP_IMAGE
import com.kiluss.vemergency.constant.USER_NODE
import com.kiluss.vemergency.data.model.User
import com.kiluss.vemergency.ui.base.BaseViewModel
import java.io.File

class MainViewModel(application: Application) : BaseViewModel(application) {

    private lateinit var databaseReference: DatabaseReference
    private lateinit var storageReference: StorageReference
    private lateinit var auth: FirebaseAuth
    private var user: User? = null
    private var uid: String? = null
    private val _avatarBitmap: MutableLiveData<Bitmap> by lazy {
        MutableLiveData<Bitmap>()
    }
    val avatarBitmap: LiveData<Bitmap> = _avatarBitmap

    init {
        setupFirebase()
        getUserInfo()
    }

    private fun setupFirebase() {
        databaseReference = FirebaseDatabase.getInstance().getReference(USER_NODE)
        auth = FirebaseAuth.getInstance()
        uid = auth.currentUser?.uid
    }

    internal fun getUserInfo() {
        auth.currentUser?.let {
            storageReference = FirebaseStorage.getInstance().reference.child(USER_NODE + "/" + auth.currentUser?.uid)
            File("${getApplication<Application>().cacheDir}/$TEMP_IMAGE").mkdirs()
            val localFile = File("${getApplication<Application>().cacheDir}/$TEMP_IMAGE/${auth.currentUser?.uid}.jpg")
            localFile.createNewFile()
            storageReference
                .getFile(localFile)
                .addOnCompleteListener {
                    _avatarBitmap.value = BitmapFactory.decodeFile(localFile.absolutePath)
                }.addOnFailureListener {
                    it.printStackTrace()
                }
        }
    }

    internal fun signOut(){
        auth.signOut() //End user session
    }
}
