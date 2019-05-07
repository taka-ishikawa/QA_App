package com.example.qa_app

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_question_detail.*

class QuestionDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_question_detail)

        fab.setOnClickListener {
            // need to change context: this
            val intent = Intent(this, AnswerSendActivity::class.java)
//            intent.putExtra()
            startActivity(intent)
        }
    }
}
