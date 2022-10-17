package com.kiluss.vemergency.utils

import android.content.Context
import android.widget.Toast
import com.kiluss.vemergency.constant.*

object Utils {

    private var toast: Toast? = null

    internal fun showShortToast(context: Context, message: String) {
        toast?.cancel()
        toast = Toast.makeText(context, message, Toast.LENGTH_SHORT)
        toast?.show()
    }

    internal fun showLongToast(context: Context, message: String) {
        toast?.cancel()
        toast = Toast.makeText(context, message, Toast.LENGTH_LONG)
        toast?.show()
    }

    internal fun getCollectionRole(): String {
        return when (SharedPrefManager.getString(SHARE_PREF_ROLE, ROLE_NAN)) {
            ROLE_USER -> USER_COLLECTION
            ROLE_SHOP -> SHOP_COLLECTION
            ROLE_ADMIN -> ADMIN_COLLECTION
            else -> ROLE_NAN
        }
    }
}
