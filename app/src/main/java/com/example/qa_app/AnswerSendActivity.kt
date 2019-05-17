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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_answer_send.*

class AnswerSendActivity : AppCompatActivity(), DatabaseReference.CompletionListener, View.OnClickListener {

    private lateinit var question: Question

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_answer_send)

        title = "回答作成"

        // extras idQuestion 的な
        question = intent.extras!!.get(QuestionIntentKEY) as Question

        buttonSend.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        // キーボードが出てたら閉じる
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(v!!.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)

        val databaseReference = FirebaseDatabase.getInstance().reference
        val answerRef = databaseReference.child(ContentsPATH).child(question.genre.toString()).child(question.questionUid).child(AnswersPATH)

        val data = HashMap<String, String>()

        // push answerRef to
        val answer= editTextBody.text.toString()
        data["body"] = answer
        if (answer.isEmpty()) {
            Snackbar.make(v, "回答を入力してください", Snackbar.LENGTH_LONG).show()
            return
        }
        // Preferenceから名前を取る
        val sharedPreference = PreferenceManager.getDefaultSharedPreferences(this)
        data["userName"] = sharedPreference.getString(UserNameKEY, "")!!
        data["uid"] = FirebaseAuth.getInstance().currentUser!!.uid

        progressBar.visibility = View.VISIBLE
        answerRef.push().setValue(data, this)
    }

    override fun onComplete(p0: DatabaseError?, p1: DatabaseReference) {
        progressBar.visibility = View.GONE

        if (p0 == null) {
            Toast.makeText(this, "投稿しました", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Snackbar.make(findViewById(android.R.id.content), "投稿に失敗しました", Snackbar.LENGTH_LONG).show()
        }
    }
}
