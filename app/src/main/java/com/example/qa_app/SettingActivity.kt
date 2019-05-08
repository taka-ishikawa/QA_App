package com.example.qa_app

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.Snackbar
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_setting.*

class SettingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        title = "設定"

        // editTextUserName.setText(userName)
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val userName = sharedPreferences.getString(NameKEY, "")
        editTextUserName.setText(userName)

        buttonChange.setOnClickListener {
            // close keyboard
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(it.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)

            val User = FirebaseAuth.getInstance().currentUser
            if (User == null) {
                Snackbar.make(it, "ログインしていません", Snackbar.LENGTH_LONG).show()
            } else {
                // change userName on Firebase
                val newUserName = editTextUserName.text.toString()
                val dataBaseReference = FirebaseDatabase.getInstance().reference
                val userRef = dataBaseReference.child(UsersPATH).child(User.uid)
                val data = HashMap<String, String>()
                data[UserNameKEY] = newUserName
                userRef.setValue(data)

                // change userName on Preference
                val editor = sharedPreferences.edit()
                editor.putString(NameKEY, newUserName)
                editor.commit()

                Snackbar.make(it, "表示名を変更しました", Snackbar.LENGTH_LONG).show()
            }
        }

        buttonLogout.setOnClickListener {
            // call logout.　(FirebaseAuth.getInstance() = auth in LoginActivity.kt)
            FirebaseAuth.getInstance().signOut()
            val editor = sharedPreferences.edit()
            editor.putString(NameKEY, "")
            editor.commit()

            Toast.makeText(this, "ログアウトしました", Toast.LENGTH_SHORT).show()

            finish()
        }
    }
}