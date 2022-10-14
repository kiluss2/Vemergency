package com.kiluss.vemergency.utils

import android.content.Context
import android.widget.Toast

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
}
