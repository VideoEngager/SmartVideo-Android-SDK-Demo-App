//
// DemoPureCloud
//
// Copyright © 2021 VideoEngager. All rights reserved.
//
package com.videoengager.demoapp

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import com.videoengager.sdk.VideoEngager
import com.videoengager.sdk.model.Settings
import com.videoengager.sdk.tools.LangUtils
import java.util.*

class GC_Activity : AppCompatActivity() {
    lateinit var sett:Settings
    lateinit var preferences : SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_g_c)
        preferences = getSharedPreferences("genesys_cloud", MODE_PRIVATE)
       if(!preferences.contains("VideoengagerUrl")) {//load defaults from params.json
           Globals.params?.genesys_cloud_params_init?.let {
               findViewById<EditText>(R.id.orgid).setText(it.OrganizationId)
               findViewById<EditText>(R.id.depid).setText(it.DeploymentId)
               findViewById<EditText>(R.id.videourl).setText(it.VideoengagerUrl)
               findViewById<EditText>(R.id.tenid).setText(it.TennathId)
               findViewById<EditText>(R.id.env).setText(it.Environment)
               findViewById<EditText>(R.id.queue).setText(it.Queue)
               findViewById<EditText>(R.id.name).setText(it.MyNickname)
           }
       }else
       {
           //load modified
           findViewById<EditText>(R.id.orgid).setText(preferences.getString("OrganizationId",""))
           findViewById<EditText>(R.id.depid).setText(preferences.getString("DeploymentId",""))
           findViewById<EditText>(R.id.videourl).setText(preferences.getString("VideoengagerUrl",""))
           findViewById<EditText>(R.id.tenid).setText(preferences.getString("TennathId",""))
           findViewById<EditText>(R.id.env).setText(preferences.getString("Environment",""))
           findViewById<EditText>(R.id.queue).setText(preferences.getString("Queue",""))
           findViewById<EditText>(R.id.name).setText(preferences.getString("MyNickname",""))

       }

        findViewById<Button>(R.id.buttonaudio).setOnClickListener {
            // audio mode only
            readSettings()
            sett.allowVisitorToSwitchAudioCallToVideoCall=false
            sett.AvatarImageUrl="https://mir-s3-cdn-cf.behance.net/project_modules/disp/96be2232163929.567197ac6fb64.png"
            sett.informationLabelText= "Your security code is : <b>1234</b>"
            //sett.backgroundImageURL = "#FF1198"
            sett.backgroundImageURL="https://cdn.pixabay.com/photo/2015/04/23/22/00/tree-736885__480.jpg"
            sett.outgoingCallVC=Settings.OutgoingCallVC(hideAvatar = true,hideName = true)
            val video = VideoEngager(this, sett, VideoEngager.Engine.genesys)
            if (video.Connect(VideoEngager.CallType.audio)) {
                video.onEventListener = listener
            } else Toast.makeText(this, "Error from connection", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.buttonvideo).setOnClickListener {
            readSettings()
            // add custom fields
            val customFields = mutableMapOf<String,Any>()
            val urlClient = "https://my_url_client"
            val videocall_flag = true
            val audioonlycall_flag = false
            val chatonly_flag  = false
            customFields["urlclient"]=urlClient
            customFields["videocall"]=videocall_flag
            customFields["audioonlycall"]=audioonlycall_flag
            customFields["chatonly"]=chatonly_flag
            sett.CustomFields=customFields
            sett.AvatarImageUrl="https://mir-s3-cdn-cf.behance.net/project_modules/disp/96be2232163929.567197ac6fb64.png"
            sett.outgoingCallVC=Settings.OutgoingCallVC(hideAvatar = false,hideName = false)
            val video = VideoEngager(this, sett, VideoEngager.Engine.genesys)
            if (video.Connect(VideoEngager.CallType.video)) {
                video.onEventListener = listener
            } else Toast.makeText(this, "Error from connection", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.buttonchat).setOnClickListener {
            readSettings()
            Globals.chat = VideoEngager(this, sett, VideoEngager.Engine.genesys).apply {
                if (Connect(VideoEngager.CallType.chat)) {
                    onEventListener = object : VideoEngager.EventListener() {
                        override fun onChatAccepted() {
                            val intent = Intent(this@GC_Activity, WebChat::class.java)
                            startActivity(intent)
                            finish()
                        }

                        override fun onDisconnected() {
                            finish()
                        }
                    }
                } else Toast.makeText(
                    this@GC_Activity,
                    "Error from connection",
                    Toast.LENGTH_SHORT
                ).show()

            }
        }
    }

    fun readSettings(){
        Globals.params?.genesys_cloud_params_init?.let {
            sett = Settings(
                findViewById<EditText>(R.id.orgid).text.toString(),
                findViewById<EditText>(R.id.depid).text.toString(),
                findViewById<EditText>(R.id.videourl).text.toString(),
                findViewById<EditText>(R.id.tenid).text.toString(),
                findViewById<EditText>(R.id.env).text.toString(),
                findViewById<EditText>(R.id.queue).text.toString(),
                it.AgentShortURL,
                findViewById<EditText>(R.id.name).text.toString(),
                findViewById<EditText>(R.id.name).text.toString(), "",
                "myMail@aa.aa", "",
                Language = MainActivity.Lang?: VideoEngager.Language.ENGLISH
            )
            //save settings for later usage
            preferences.edit {
                putString("OrganizationId",findViewById<EditText>(R.id.orgid).text.toString())
                putString("DeploymentId",findViewById<EditText>(R.id.depid).text.toString())
                putString("VideoengagerUrl",findViewById<EditText>(R.id.videourl).text.toString())
                putString("TennathId",findViewById<EditText>(R.id.tenid).text.toString())
                putString("Environment",findViewById<EditText>(R.id.env).text.toString())
                putString("Queue",findViewById<EditText>(R.id.queue).text.toString())
                putString("MyNickname",findViewById<EditText>(R.id.name).text.toString())
                apply()
            }
        }
    }

    val listener = object : VideoEngager.EventListener(){
        override fun onDisconnected() {
            finish()
        }

        override fun onErrorMessage(type: String, message: String) {
            Toast.makeText(this@GC_Activity, "Error:$message", Toast.LENGTH_SHORT).show()
        }
    }

    override fun attachBaseContext(newBase: Context) {
        val localeUpdatedContext: ContextWrapper = LangUtils.updateLocale(newBase, Locale(
            MainActivity.Lang!!.value?:"")
        )
        super.attachBaseContext(localeUpdatedContext)
    }
}