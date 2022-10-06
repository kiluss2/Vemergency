package com.kiluss.vemergency.data.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.kiluss.vemergency.constant.AVATAR
import com.kiluss.vemergency.constant.AVATAR_NODE
import com.kiluss.vemergency.constant.SHOP_COVER
import com.kiluss.vemergency.constant.SHOP_NODE
import com.kiluss.vemergency.constant.USER_NODE
import com.kiluss.vemergency.data.model.User

object FirebaseManager {

    private var databaseReference: DatabaseReference? = null
    private var storageReference: StorageReference? = null
    private var auth: FirebaseAuth? = null
    private var user: User? = null
    private var uid: String? = null

    internal fun init() {
        if (auth == null) {
            setupFirebase()
        }
    }

    private fun setupFirebase() {
        auth = FirebaseAuth.getInstance()
        uid = auth?.currentUser?.uid
        databaseReference = Firebase.database.reference
    }

    internal fun logout() {
        auth = null
        uid = null
        databaseReference = null
    }

    internal fun getAuth() = auth
    internal fun getCurrentUser() = auth?.currentUser
    internal fun getUid() = uid
    internal fun getUserInfoDatabaseReference() = FirebaseDatabase.getInstance().getReference("$USER_NODE/$uid")
    internal fun getUserAvatarStorageReference() =
        FirebaseStorage.getInstance().reference.child( auth?.currentUser?.uid + "/" + AVATAR_NODE + "/" + AVATAR)
    internal fun getShopImageStorageReference() =
        FirebaseStorage.getInstance().reference.child( auth?.currentUser?.uid + "/" + SHOP_NODE + "/" + SHOP_COVER)
    internal fun getDatabaseReference() = databaseReference
}
