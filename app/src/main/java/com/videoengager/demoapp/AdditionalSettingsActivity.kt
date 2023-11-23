package com.videoengager.demoapp

import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import androidx.core.graphics.toColorInt
import com.videoengager.demoapp.databinding.ActivityAdditionalSettingsBinding
import yuku.ambilwarna.AmbilWarnaDialog

class AdditionalSettingsActivity : AppCompatActivity() {
    private lateinit var prefs : SharedPreferences
    private lateinit var ui : ActivityAdditionalSettingsBinding
    private var requiredRestart = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = ActivityAdditionalSettingsBinding.inflate(layoutInflater)
        setContentView(ui.root)
        prefs = getSharedPreferences("additional", MODE_PRIVATE)
        loadUI()
        ui.restrictPhone.setOnCheckedChangeListener { _, isChecked ->
            requiredRestart = true
        }
        ui.buttBackColor.setOnClickListener {
            AmbilWarnaDialog(
                this@AdditionalSettingsActivity,
                if (ui.buttBackColor.tag != null) ui.buttBackColor.tag.toString()
                    .toInt() else Color.parseColor("#42A5F5"),
                object : AmbilWarnaDialog.OnAmbilWarnaListener {
                    override fun onCancel(dialog: AmbilWarnaDialog?) {
                    }

                    override fun onOk(dialog: AmbilWarnaDialog?, color: Int) {
                        it.setBackgroundColor(color)
                        it.tag = color
                        ui.buttTextColor.setBackgroundColor(color)
                        window.statusBarColor = color
                    }
                }).show()
        }
        ui.buttTextColor.setOnClickListener {
            AmbilWarnaDialog(
                this@AdditionalSettingsActivity,
                if (ui.buttTextColor.tag != null) ui.buttTextColor.tag.toString()
                    .toInt() else Color.WHITE,
                object : AmbilWarnaDialog.OnAmbilWarnaListener {
                    override fun onCancel(dialog: AmbilWarnaDialog?) {
                    }

                    override fun onOk(dialog: AmbilWarnaDialog?, color: Int) {
                        ui.buttTextColor.setTextColor(color)
                        ui.buttBackColor.setTextColor(color)
                        it.tag = color
                    }
                }).show()
        }
        ui.moduleTypes.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (prefs.getInt("modelTypeIndex", 0) != position) {
                    requiredRestart = true
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

        }
    }

    private fun loadUI(){
        ui.avatarImageUrl.setText(prefs.getString("avatarImageUrl",""))
        ui.informationLabelText.setText(prefs.getString("informationLabelText",""))
        ui.backgroundImageURL.setText(prefs.getString("backgroundImageURL",""))
        ui.toolBarHideTimeout.setText(prefs.getString("toolBarHideTimeout","10"))
        ui.customerLabel.setText(prefs.getString("customerLabel",""))
        ui.agentWaitingTimeout.setText(prefs.getString("agentWaitingTimeout","120"))
        ui.allowVisitorToSwitchAudioCallToVideoCall.isChecked=prefs.getBoolean("allowVisitorToSwitchAudioCallToVideoCall",false)
        ui.startCallWithPictureInPictureMode.isChecked=prefs.getBoolean("startCallWithPictureInPictureMode",false)
        ui.startCallWithSpeakerPhone.isChecked=prefs.getBoolean("startCallWithSpeakerPhone",false)
        ui.hideAvatar.isChecked=prefs.getBoolean("hideAvatar",false)
        ui.hideName.isChecked=prefs.getBoolean("hideName",false)
        ui.showAgentBusyDialog.isChecked=prefs.getBoolean("showAgentBusyDialog",true)
        ui.restrictPhone.isChecked = prefs.getBoolean("restrictPhone",false)
        ui.moduleTypes.setSelection(prefs.getInt("modelTypeIndex",0))
        prefs.getInt("buttonBackColor",Int.MAX_VALUE).let {
            if(it!=Int.MAX_VALUE){
                ui.buttBackColor.setBackgroundColor(it)
                ui.buttTextColor.setBackgroundColor(it)
                ui.buttBackColor.tag = it
                window.statusBarColor = it
            }
        }
        prefs.getInt("buttonTextColor",Int.MAX_VALUE).let {
            if(it!=Int.MAX_VALUE){
                ui.buttBackColor.setTextColor(it)
                ui.buttTextColor.setTextColor(it)
                ui.buttTextColor.tag = it
            }
        }
        ui.logoImageUrl.setText(prefs.getString("logoImageUrl",""))
    }

    private fun save() {
        prefs.edit {
            putString("avatarImageUrl", ui.avatarImageUrl.text.toString())
            putString("informationLabelText", ui.informationLabelText.text.toString())
            putString("backgroundImageURL", ui.backgroundImageURL.text.toString())
            putString("toolBarHideTimeout", ui.toolBarHideTimeout.text.toString())
            putString("customerLabel", ui.customerLabel.text.toString())
            putString("agentWaitingTimeout", ui.agentWaitingTimeout.text.toString())
            putBoolean(
                "allowVisitorToSwitchAudioCallToVideoCall",
                ui.allowVisitorToSwitchAudioCallToVideoCall.isChecked
            )
            putBoolean(
                "startCallWithPictureInPictureMode",
                ui.startCallWithPictureInPictureMode.isChecked
            )
            putBoolean("startCallWithSpeakerPhone", ui.startCallWithSpeakerPhone.isChecked)
            putBoolean("hideAvatar", ui.hideAvatar.isChecked)
            putBoolean("hideName", ui.hideName.isChecked)
            putBoolean("showAgentBusyDialog", ui.showAgentBusyDialog.isChecked)
            putBoolean("restrictPhone", ui.restrictPhone.isChecked)
            putInt("modelTypeIndex",ui.moduleTypes.selectedItemPosition)
            putString("logoImageUrl",ui.logoImageUrl.text.toString())
            ui.buttBackColor.tag?.let { putInt("buttonBackColor",it.toString().toInt())}
            ui.buttTextColor.tag?.let { putInt("buttonTextColor",it.toString().toInt())}
            commit()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        save()
        if(requiredRestart){
            restartApp()
        }
    }

    fun restartApp(){
        val intent = Intent(this@AdditionalSettingsActivity, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        Runtime.getRuntime().exit(0)
    }
}