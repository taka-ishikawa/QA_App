package com.example.qa_app

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.view.View
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var createAccountListener:OnCompleteListener<AuthResult>
    private lateinit var loginListener: OnCompleteListener<AuthResult>
    private lateinit var dataBaseReference: DatabaseReference

    // アカウント作成時にフラグを立て、ログイン処理後に名前をFirebaseに保存する
    private var isCreateAccount = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val email = editTextEmail.text.toString()
        val password = editTextPass.text.toString()
        val userName = editTextUserName.text.toString()

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

            } else { // login false

            }
        }

        title = "ログイン"

        buttonResister.setOnClickListener {
            // close keyboard
            if (email.length != 0 && password.length >= 6 && userName.length != 0) {
                // raise the flag to reserve the user name when login
            } else { // SnackBar: input error

            }
        }
        buttonLogin.setOnClickListener {
            // close keyboard
            if (email.length != 0 && password.length >= 6 && userName.length != 0) {
                // let down the flag
            } else { // SnackBar: input error

            }
        }
    }

    fun createAccount() {
        // create account, using .addOnCompleteListener(createAccountListener)
    }

    fun login(email: String, password: String) {
        // login, using .addOnCompleteListener(loginListener)
    }
}
