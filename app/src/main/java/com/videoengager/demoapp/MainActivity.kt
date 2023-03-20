//
// DemoPureCloud
//
// Copyright © 2021 VideoEngager. All rights reserved.
//
package com.videoengager.demoapp

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import com.google.gson.Gson
import com.videoengager.sdk.VideoEngager
import com.videoengager.sdk.tools.LangUtils
import java.util.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val prefs = getSharedPreferences("additional", MODE_PRIVATE)
        findViewById<TextView>(R.id.versionView).text = "SDK Version: ${VideoEngager.SDK_VERSION}"

        //setup screenshare restriction policies
        VideoEngager.VeRegisterScreenSharePolicies(this, mutableListOf(R.id.avatarImageUrl), prefs.getBoolean("restrictPhone",false))

        findViewById<ImageButton>(R.id.button_ve).setOnClickListener {
            startActivity(Intent(this,VEActivity::class.java))
        }
        findViewById<ImageButton>(R.id.button_gc).setOnClickListener {
            startActivity(Intent(this,GCActivity::class.java))
        }

        findViewById<ImageButton>(R.id.button_ge).setOnClickListener {
            startActivity(Intent(this,GEActivity::class.java))
        }

        if(Lang==null){
            startActivity(Intent(this,LangSelectorActivity::class.java))
            finish()
        }
        //load params.json from assets folder
        Globals.params=Gson().fromJson(assets.open("params.json").reader(Charsets.UTF_8),Params::class.java)

       findViewById<Button>(R.id.button_logging).apply {
           setOnClickListener {
               val v = findViewById<TextView>(R.id.text)
               v.setText("dummy error")
           }
           if(application.javaClass.simpleName!=ErrorReportingApplication::class.java.simpleName){
               visibility = View.GONE
           }
       }
    }

    override fun attachBaseContext(newBase: Context) {
        newBase.getSharedPreferences("settings",0).apply {
            getString("language",null)?.let {
                Lang= VideoEngager.Language.valueOf(it)
            }
        }
        val localeUpdatedContext: ContextWrapper = LangUtils.updateLocale(newBase, Locale(Lang?.value?:""))
        super.attachBaseContext(localeUpdatedContext)
    }

    companion object {
        var Lang:VideoEngager.Language?=null
    }
}