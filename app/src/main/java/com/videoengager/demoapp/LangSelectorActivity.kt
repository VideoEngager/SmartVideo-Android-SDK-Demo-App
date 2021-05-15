//
// DemoPureCloud
//
// Copyright © 2021 VideoEngager. All rights reserved.
//
package com.videoengager.demoapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.videoengager.sdk.VideoEngager

class LangSelectorActivity : AppCompatActivity(),View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lang_selector)
    }

    override fun onClick(b: View?) {
        when(b?.id){
            R.id.b_bg -> MainActivity.Lang=VideoEngager.Language.BULGARIAN
            R.id.b_en -> MainActivity.Lang=VideoEngager.Language.ENGLISH
            R.id.b_de -> MainActivity.Lang=VideoEngager.Language.GERMAN
            R.id.b_sp -> MainActivity.Lang=VideoEngager.Language.SPANISH
            R.id.b_po -> MainActivity.Lang=VideoEngager.Language.PORTUGAL
        }
        getSharedPreferences("settings",0).apply {
            edit().putString("language",MainActivity.Lang!!.name).apply()
        }
        startActivity(Intent(this,MainActivity::class.java))
        finish()
    }
}