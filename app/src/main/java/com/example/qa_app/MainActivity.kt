package com.example.qa_app

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity;
import android.view.Menu
import android.view.MenuItem
import com.google.firebase.auth.FirebaseAuth

import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener {
            val user = FirebaseAuth.getInstance().currentUser
            if (user == null) {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            } else {
                // need to change context: this
                val intent = Intent(this, QuestionSendActivity::class.java)
//                intent.putExtra()
                startActivity(intent)
            }
        }

        listView.setOnItemClickListener { _, _, _, _ ->
            // need to change context: this
            val intent = Intent(this, QuestionDetailActivity::class.java)
//            intent.putExtra()
            startActivity(intent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        if (item.itemId == R.id.action_settings){
            val intent = Intent(this, SettingActivity::class.java)
            startActivity(intent)
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
