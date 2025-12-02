package com.sujith.pocket.ui.extensions

import android.content.Context
import android.widget.Toast

fun Context.showToast(msg: String) =
    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()


fun Context.showToast(msg: String , duration: Int) =
    Toast.makeText(this, msg, duration).show()
