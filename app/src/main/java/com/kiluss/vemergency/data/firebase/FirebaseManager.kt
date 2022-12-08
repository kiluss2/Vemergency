package com.kiluss.vemergency.data.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.kiluss.vemergency.constant.USER_NODE

object FirebaseManager {
    private var databaseReference: DatabaseReference? = null
    private var storageReference: StorageReference? = null
    private var auth: FirebaseAuth? = null

    internal fun init() {
        if (auth == null || databaseReference == null) {
            setupFirebase()
        }
    }

    private fun setupFirebase() {
        auth = Firebase.auth
        databaseReference = Firebase.database.reference
    }

    internal fun logout() {
        auth = null
        databaseReference = null
    }

    internal fun getAuth() = auth
    internal fun getCurrentUser() = auth?.currentUser
    internal fun getUserInfoDatabaseReference() = databaseReference!!.child("$USER_NODE/${auth?.currentUser?.uid}")
//    internal fun getUserAvatarStorageReference() =
//        FirebaseStorage.getInstance().reference.child(auth?.currentUser?.uid + "/" + AVATAR_NODE + "/" + AVATAR)
    internal fun getDatabaseReference() = databaseReference
}
