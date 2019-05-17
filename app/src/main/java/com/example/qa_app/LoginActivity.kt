package com.example.qa_app

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.Snackbar
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var createAccountListener: OnCompleteListener<AuthResult>
    private lateinit var loginListener: OnCompleteListener<AuthResult>
    private lateinit var dataBaseReference: DatabaseReference

    // アカウント作成時にフラグを立て、ログイン処理後に名前をFirebaseに保存する
    private var isCreateAccount = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        title = "ログイン"

        lateinit var email: String
        lateinit var password: String
        lateinit var userName: String

        dataBaseReference = FirebaseDatabase.getInstance().reference

        // FirebaseAuthのオブジェクトを取得する
        auth = FirebaseAuth.getInstance()

        createAccountListener = OnCompleteListener { task ->
            if (task.isSuccessful) { // login
                login(email, password)
            } else { //display "error"
                val view = findViewById<View>(android.R.id.content)
                Snackbar.make(view, "アカウント作成に失敗しました", Snackbar.LENGTH_LONG).show()
                progressBar.visibility = View.GONE
            }
        }

        loginListener = OnCompleteListener { task ->
            if (task.isSuccessful) { // login successful
                val userRef = dataBaseReference.child(UsersPATH).child(auth.currentUser!!.uid)

                if (isCreateAccount) { // when  create account
                    // save userName on Firebase
                    val data = HashMap<String, String>() //HashMap<Key,Value>()
                    data["userName"] = userName
                    userRef.setValue(data)

                    // save userName on Preference
                    saveName(userName)
                    Toast.makeText(this, "アカウントを登録しました", Toast.LENGTH_SHORT).show()
                } else { // when login
                    userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            // get data from Firebase
                            val data = snapshot.value as Map<*, *>
                            // save userName on Preference
                            saveName(data["userName"] as String)
                        }

                        override fun onCancelled(firebaseError: DatabaseError) {
                        }
                    })
                    Toast.makeText(this, "ログインしました", Toast.LENGTH_SHORT).show()
                }

                progressBar.visibility = View.GONE
                finish()

            } else { // login false
                val view = findViewById<View>(android.R.id.content)
                Snackbar.make(view, "ログインに失敗しました", Snackbar.LENGTH_LONG).show()
                progressBar.visibility = View.INVISIBLE
            }
        }

        buttonResister.setOnClickListener {
            // close keyboard
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(it.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)

            email = editTextEmail.text.toString()
            password = editTextPass.text.toString()
            userName = editTextUserName.text.toString()
            if (email.isNotEmpty() && password.length >= 6 && userName.isNotEmpty()) {
                // raise the flag to reserve the user name when login
                isCreateAccount = true
                createAccount(email, password)
            } else { // SnackBar: input error
                Snackbar.make(it, "正しく入力してください", Snackbar.LENGTH_LONG).show()
            }
        }

        buttonLogin.setOnClickListener {
            // close keyboard
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(it.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)

            email = editTextEmail.text.toString()
            password = editTextPass.text.toString()
            userName = editTextUserName.text.toString()
            if (email.isNotEmpty() && password.length >= 6) {
                // let down the flag
                isCreateAccount = false
                login(email, password)
            } else { // SnackBar: input error
                Snackbar.make(it, "正しく入力してください", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun createAccount(email: String, password: String) {
        progressBar.visibility = View.VISIBLE
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(createAccountListener)
    }

    private fun login(email: String, password: String) {
        progressBar.visibility = View.VISIBLE
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(loginListener)
    }

    private fun saveName(userName: String) { // save userName on Preference
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val editor = sharedPreferences.edit()
        editor.putString(UserNameKEY, userName)
        editor.apply()
    }
}
