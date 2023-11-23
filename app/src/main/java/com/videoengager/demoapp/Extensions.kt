//
// DemoPureCloud
//
// Copyright © 2023 VideoEngager. All rights reserved.
//
package com.videoengager.demoapp

import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Handler
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.view.children
import java.net.URL
import java.util.concurrent.Executors


fun Window.applyCustomColors(prefs: SharedPreferences) {
    val backColor = prefs.getInt("buttonBackColor", Int.MAX_VALUE)
    val textColor = prefs.getInt("buttonTextColor", Int.MAX_VALUE)
    fun findAndSet(view: ViewGroup?) {
        view?.children?.forEach { child ->
            if (child is ViewGroup) {
                findAndSet(child)
            }
            if (child is Button) {
                if (backColor != Int.MAX_VALUE) {
                    child.setBackgroundColor(backColor)
                }
                if (textColor != Int.MAX_VALUE) {
                    child.setTextColor(textColor)
                }
            }

        }
    }
    if (backColor != Int.MAX_VALUE){
        this.statusBarColor = backColor
    }

    findAndSet(decorView.findViewById(android.R.id.content))
    findViewById<ImageView>(R.id.logoimage)?.let {
        var res = prefs.getString("logoImageUrl", "")?.runCatching {
            if (isNotBlank() && startsWith("http")) {
                Executors.newSingleThreadExecutor().execute {
                    BitmapFactory.decodeStream(URL(this).openConnection().getInputStream())
                        ?.let { bmp ->
                            ContextCompat.getMainExecutor(context).execute {
                                it.setImageBitmap(bmp)
                            }
                        }
                }
            }
        }
    }

}