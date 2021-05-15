//
// DemoPureCloud
//
// Copyright © 2021 VideoEngager. All rights reserved.
//
package com.videoengager.demoapp


import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.videoengager.sdk.VideoEngager


class WebChat : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_chat)
        val msgBox = findViewById<EditText>(R.id.msgBox)
        val chatBox = findViewById<TextView>(R.id.chatBox)
        chatBox.text=""
        findViewById<ImageButton>(R.id.sendButt).setOnClickListener {
            Globals.chat?.SendMessage(msgBox.text.toString())
            chatBox.append("ME->${msgBox.text.toString()}\n")
            msgBox.text.clear()
        }
        Globals.chat?.onEventListener=object : VideoEngager.EventListener(){

            override fun onDisconnected() {
                Toast.makeText(this@WebChat,"Chat Ended!",Toast.LENGTH_SHORT).show()
                finish()
            }

            override fun onMessageReceived(message: String) {
                chatBox.append("${Globals.chat?.settings?.AgentShortURL}->${message}\n")
            }


        }
    }

    override fun onBackPressed() {
        Globals.chat?.Disconnect()
        //super.onBackPressed()
    }
}