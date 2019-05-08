package com.example.qa_app

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_answer_send.*

class AnswerSendActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_answer_send)

        title = "回答作成"

        // extras idQuestion 的な
        buttonSend.setOnClickListener {
            val answerBody = editTextAnswer.text.toString()
//            val userName =

            // post answer to list_question_detail 的な
            finish()
        }
    }
}
