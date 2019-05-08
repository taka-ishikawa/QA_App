package com.example.qa_app

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_question_detail.*

class QuestionDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_question_detail)

//        val titleQuestion = ""
//        title = titleQuestion

        // extras idQuestion 的な
        fab.setOnClickListener {
            val intent = Intent(this, AnswerSendActivity::class.java)
//            intent.putExtra( idQuestionIntentKEY,/*idQuestion的な*/)
            startActivity(intent)
        }
    }
}
