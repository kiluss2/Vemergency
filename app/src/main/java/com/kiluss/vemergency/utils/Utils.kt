package com.kiluss.vemergency.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.widget.Toast
import com.kiluss.vemergency.constant.*
import okhttp3.MultipartBody
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
        var width = image.width
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

    internal fun createRequestBodyForImage(imageBase64: String) = run {
        MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("key", API_KEY)
            .addFormDataPart("source", imageBase64)
            .build()
    }

    internal fun distanceFormat(distance: Double): Int {
        return if (distance >= 1000.0) {
            (distance / 1000).toInt()
        } else {
            distance.toInt()
        }
    }

    internal fun convertSeconds(seconds: Int): String {
        val h = seconds / 3600
        val m = seconds % 3600 / 60
        val s = seconds % 60
        val sh = if (h > 0) "$h h" else ""
        val sm =
            (if (m in 1..9 && h > 0) "0" else "") + if (m > 0) if (h > 0 && s == 0) m.toString() else "$m min" else ""
        val ss =
            if (s == 0 && (h > 0 || m > 0)) "" else (if (s < 10 && (h > 0 || m > 0)) "0" else "") + s.toString() + " " + "sec"
        return sh + (if (h > 0) " " else "") + sm + (if (m > 0) " " else "") + ss
    }
}
