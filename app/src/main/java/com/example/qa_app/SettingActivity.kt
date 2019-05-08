package com.example.qa_app

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_setting.*

class SettingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        title = "設定"

        buttonChange.setOnClickListener {
            val userName = editTextUserName.text.toString()

            // change userName on the Firebase
        }

        buttonLogout.setOnClickListener {
            // call logout.
        }
    }
}