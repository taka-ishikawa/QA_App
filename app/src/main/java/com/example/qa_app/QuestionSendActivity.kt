package com.example.qa_app

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_question_send.*

class QuestionSendActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_question_send)

        val genre = intent.extras["GenreIntentKEY"]!!.toString().toInt()

        buttonSend.setOnClickListener {
            val newTitle = editTextTitle.text.toString()
            val newBody = editTextBody.text.toString()
            val idNewImage = imageView.id

            // send question: register genre/ newTitle/ newBody/ idNewImage with Firebase

        }
    }
}
