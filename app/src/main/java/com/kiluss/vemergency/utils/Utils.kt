package com.kiluss.vemergency.utils

import android.content.Context
import android.widget.Toast

object Utils {
    internal fun showShortToast(context: Context, string: String) {
        Toast.makeText(context, string, Toast.LENGTH_SHORT).show()
    }

    internal fun showLongToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
}
