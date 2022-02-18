//
// DemoPureCloud
//
// Copyright © 2022 VideoEngager. All rights reserved.
//
package com.videoengager.sterling.demoapp

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import com.google.gson.Gson
import com.videoengager.sdk.VideoEngager
import com.videoengager.sdk.model.Settings
import com.videoengager.sdk.tools.LangUtils
import java.util.*

class MainActivity : AppCompatActivity() {
    lateinit var settings:Settings
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<TextView>(R.id.versionView).text = "SDK Version: ${VideoEngager.SDK_VERSION}"

        findViewById<Button>(R.id.button_click_to_audio).setOnClickListener {
            settings.allowVisitorToSwitchAudioCallToVideoCall=false
            settings.AvatarImageUrl="https://mir-s3-cdn-cf.behance.net/project_modules/disp/96be2232163929.567197ac6fb64.png"
            val video = VideoEngager(this, settings, VideoEngager.Engine.genesys)
            if (video.Connect(VideoEngager.CallType.audio)) {
                video.onEventListener = listener
            } else Toast.makeText(this, "Error from connection", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.button_click_to_video).setOnClickListener {
            settings.startCallWithSpeakerPhone=true
            settings.AvatarImageUrl="https://mir-s3-cdn-cf.behance.net/project_modules/disp/96be2232163929.567197ac6fb64.png"
            val video = VideoEngager(this, settings, VideoEngager.Engine.genesys)
            if (video.Connect(VideoEngager.CallType.video)) {
                video.onEventListener = listener
            } else Toast.makeText(this, "Error from connection", Toast.LENGTH_SHORT).show()
        }

        if(Lang==null){
            startActivity(Intent(this,LangSelectorActivity::class.java))
            finish()
        }
        //load params.json from assets folder
        Globals.params=Gson().fromJson(assets.open("params.json").reader(Charsets.UTF_8),Params::class.java)
        Globals.params?.genesys_cloud_params_init?.let {
            settings = Settings(
                it.OrganizationId,
                it.DeploymentId,
                it.VideoengagerUrl,
                it.TennathId,
                it.Environment,
                it.Queue,
                it.AgentShortURL,
                it.MyNickname,
                it.MyFirstName, "",
                "myMail@aa.aa", "",
                Language = Lang ?: VideoEngager.Language.ENGLISH
            )
        }
    }

    val listener = object : VideoEngager.EventListener(){
        override fun onCallFinished() {
            Toast.makeText(this@MainActivity, "CallFinished", Toast.LENGTH_SHORT).show()
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