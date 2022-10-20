package com.kiluss.vemergency.utils

import android.content.Context
import android.graphics.Bitmap
import android.util.Base64
import android.widget.Toast
import com.kiluss.vemergency.constant.*
import java.io.ByteArrayOutputStream

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

    internal fun encodeImageToBase64String(bm: Bitmap): String {
        val baos = ByteArrayOutputStream()
        bm.compress(Bitmap.CompressFormat.JPEG, 50, baos)
        val byteArray = baos.toByteArray()
        var base64Image = Base64.encodeToString(byteArray, Base64.DEFAULT).trim()
        base64Image = base64Image.replace(" ", "")
        base64Image = base64Image.lines().joinToString("")
        return base64Image
    }
}
