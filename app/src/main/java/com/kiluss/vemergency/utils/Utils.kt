package com.kiluss.vemergency.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.widget.Toast
import com.kiluss.vemergency.constant.ADMIN_COLLECTION
import com.kiluss.vemergency.constant.ROLE_ADMIN
import com.kiluss.vemergency.constant.ROLE_NAN
import com.kiluss.vemergency.constant.ROLE_SHOP
import com.kiluss.vemergency.constant.ROLE_USER
import com.kiluss.vemergency.constant.SHARE_PREF_ROLE
import com.kiluss.vemergency.constant.SHOP_COLLECTION
import com.kiluss.vemergency.constant.USER_COLLECTION
import java.io.ByteArrayOutputStream
import java.io.File

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

    internal fun getResizedBitmap(imgFile: File, maxWidth: Int): Bitmap {
        val image = getFileImageBitmap(imgFile)
        var width = image!!.width
        var height = image.height
        val bitmapRatio = width.toFloat() / height.toFloat()
        if (bitmapRatio > 0) {
            width = maxWidth
            height = (width / bitmapRatio).toInt()
        } else {
            height = maxWidth
            width = (height * bitmapRatio).toInt()
        }
        return Bitmap.createScaledBitmap(image, width, height, true)
    }

    internal fun getFileImageBitmap(imgFile: File): Bitmap {
        return BitmapFactory.decodeFile(imgFile.absolutePath)
    }
}
