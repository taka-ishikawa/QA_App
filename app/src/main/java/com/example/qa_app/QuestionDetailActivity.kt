package com.example.qa_app

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_question_detail.*

class QuestionDetailActivity : AppCompatActivity() {

    private lateinit var question: Question
    private lateinit var questionDetailListAdapter: QuestionDetailListAdapter

    private val childEventListener = object : ChildEventListener {
        override fun onChildAdded(p0: DataSnapshot, p1: String?) { // question.answer.add(answer)
            val map = p0.value as Map<String, String>

            val answerUid = p0.key ?: ""

            for (answer in question.answers) {
                // 同じAnswerUidのものが存在しているときは何もしない ？？存在するん？？
                if (answerUid == answer.answerUid) {
                    return
                }
            }

            // ?: "" これで、安全になるし、bodyがStringと確定する
            val body = map["body"] ?: ""
            val userName = map["userName"] ?: ""
            val uid = map["uid"] ?: ""

            val answer = Answer(body, userName, uid, answerUid)
            question.answers.add(answer)
            questionDetailListAdapter.notifyDataSetChanged()
        }

        override fun onChildChanged(p0: DataSnapshot, p1: String?) {
        }

        override fun onCancelled(p0: DatabaseError) {
        }

        override fun onChildMoved(p0: DataSnapshot, p1: String?) {
        }

        override fun onChildRemoved(p0: DataSnapshot) {
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_question_detail)

        // extras idQuestion 的な
        question = intent.extras.get(QuestionIntentKEY) as Question

        title = question.title

        // setting of listView
        questionDetailListAdapter = QuestionDetailListAdapter(this, question)
        listView.adapter = questionDetailListAdapter
        questionDetailListAdapter.notifyDataSetChanged()

        fab.setOnClickListener {
            val user = FirebaseAuth.getInstance().currentUser

            if (user == null) { // ログインしてない
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            } else { // ログインしてる
                val intent = Intent(this, AnswerSendActivity::class.java)
                intent.putExtra(QuestionIntentKEY, question)
                startActivity(intent)
            }
        }

        val databaseReference = FirebaseDatabase.getInstance().reference
        val answerRef = databaseReference.child(ContentsPATH).child(question.questionUid).child(AnswersPATH)
        answerRef.addChildEventListener(childEventListener)
    }
}