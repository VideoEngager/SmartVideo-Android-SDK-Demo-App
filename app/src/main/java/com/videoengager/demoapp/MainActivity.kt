//
// DemoPureCloud
//
// Copyright © 2021 VideoEngager. All rights reserved.
//
package com.videoengager.demoapp

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.videoengager.sdk.SmartVideo
import com.videoengager.sdk.VideoEngager
import com.videoengager.sdk.tools.LangUtils
import org.acra.ACRA
import java.util.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val prefs = getSharedPreferences("additional", MODE_PRIVATE)
        findViewById<TextView>(R.id.versionView).text = "SDK Version: ${SmartVideo.SDK_VERSION}"

        //setup screenshare restriction policies
        SmartVideo.VeRegisterScreenSharePolicies(this, mutableListOf(R.id.avatarImageUrl), prefs.getBoolean("restrictPhone",false))

        findViewById<ImageButton>(R.id.button_ve).apply {
            prefs.getInt("modelTypeIndex",0).let {
                isVisible = (it==0 || it==1)
            }
        }.setOnClickListener {
            startActivity(Intent(this,VEActivity::class.java))
        }
        findViewById<ImageButton>(R.id.button_gc).apply {
            prefs.getInt("modelTypeIndex",0).let {
                isVisible = (it==0 || it==2)
            }
        }.setOnClickListener {
            startActivity(Intent(this,GCActivity::class.java))
        }

        findViewById<ImageButton>(R.id.button_ge).apply {
            prefs.getInt("modelTypeIndex",0).let {
                isVisible = (it==0 || it==4)
            }
        }.setOnClickListener {
            startActivity(Intent(this,GEActivity::class.java))
        }

        findViewById<ImageButton>(R.id.button_td).apply {
            prefs.getInt("modelTypeIndex",0).let {
                isVisible = (it==0 || it==3)
            }
        }.setOnClickListener {
            startActivity(Intent(this,TDActivity::class.java))
        }

        findViewById<Button>(R.id.button_settings).setOnClickListener {
            startActivity(Intent(this,AdditionalSettingsActivity::class.java))
        }

        if(Lang==null){
            startActivity(Intent(this,LangSelectorActivity::class.java))
            finish()
        }
        //load params.json from assets folder
        Globals.params=Gson().fromJson(assets.open("params.json").reader(Charsets.UTF_8),Params::class.java)

       findViewById<Button>(R.id.button_logging).apply {
           setOnClickListener {
               ACRA.errorReporter.handleException(null)
           }
           if(application.javaClass.simpleName!=ErrorReportingApplication::class.java.simpleName){
               visibility = View.GONE
           }
       }

        //check if user was click push notification and start SDK
        if(intent?.hasExtra("veurl")==true){
            //simulate deep link handle
            startActivity(Intent(this,VEActivity::class.java).apply {
                action = Intent.ACTION_VIEW
                data = Uri.parse(intent.getStringExtra("veurl"))
            })
        }

        //helper to copy device token
        findViewById<Button>(R.id.button_push_token).setOnClickListener {
           FirebaseMessaging.getInstance().token.addOnCompleteListener {
               AlertDialog.Builder(this@MainActivity)
                   .setTitle("Token")
                   .setMessage(it.result)
                   .setNegativeButton("Copy") { dialog, which ->
                       run {
                           (getSystemService(CLIPBOARD_SERVICE) as ClipboardManager).apply {
                                setPrimaryClip(ClipData.newPlainText("Token",it.result))
                           }
                       }
                   }
                   .setPositiveButton("OK",null)
                   .show()
           }
        }

        //We need permission to receive push messages
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions( arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 9999);
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