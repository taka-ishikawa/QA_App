package com.example.qa_app

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_question_detail.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.content_main.listView
import kotlinx.android.synthetic.main.content_questiondetail.*

class QuestionDetailActivity : AppCompatActivity() {

    private lateinit var question: Question
    private lateinit var questionDetailListAdapter: QuestionDetailListAdapter

    private var currentUser: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_question_detail)

        // extras idQuestion 的な
        question = intent.extras!!.get(QuestionIntentKEY) as Question

        title = question.title

        fab.setOnClickListener {

            if (currentUser == null) { // ログインしてない
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            } else { // ログインしてる
                val intent = Intent(this, AnswerSendActivity::class.java)
                intent.putExtra(QuestionIntentKEY, question)
                startActivity(intent)
            }
        }
    }

    override fun onResume() {
        super.onResume()

        val databaseReference = FirebaseDatabase.getInstance().reference
        val answerRef = databaseReference.child(ContentsPATH).child(question.questionUid).child(AnswersPATH)
        answerRef.addChildEventListener(childEventListener)

        currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val favoriteRef = databaseReference.child(FavoritePATH).child(currentUser!!.uid)
            favoriteRef.addValueEventListener(valueEventListener)
        }

        // setting of listView
        questionDetailListAdapter = QuestionDetailListAdapter(this, question)
        listView.adapter = questionDetailListAdapter
        questionDetailListAdapter.notifyDataSetChanged()

        listView.isEnabled = false
    }

    private val childEventListener = object : ChildEventListener {
        override fun onChildAdded(p0: DataSnapshot, p1: String?) { // question.answer.add(answer)
            val map = p0.value as Map<String, String>

            val answerUid = p0.key ?: ""

            for (answer in question.answers) {
                // 同じAnswerUidのものが存在しているときは何もしない
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

    private val valueEventListener = (object : ValueEventListener {

        override fun onDataChange(p0: DataSnapshot) {
            val favoriteMap = p0.value as Map<*, *>?

//            Log.d("value", "favoriteMap: $favoriteMap")
//            Log.d("value", "favoriteMap?.keys: ${favoriteMap?.keys}")
//            Log.d("value", "questionUid: ${question.questionUid}")

            toggleButtonFav.visibility = View.VISIBLE
            val favoriteRef = FirebaseDatabase.getInstance().reference
                .child(FavoritePATH).child(currentUser!!.uid).child(question.questionUid)

            if (favoriteMap == null || !favoriteMap.containsKey(question.questionUid)) {
                toggleButtonFav.setBackgroundResource(android.R.drawable.btn_star_big_off)
            } else {
                toggleButtonFav.setBackgroundResource(android.R.drawable.btn_star_big_on)
            }
//            if (favoriteMap?.keys?.contains(question.questionUid)) { // favorite
//                toggleButtonFav.setBackgroundResource(android.R.drawable.btn_star_big_on)
//            } else { // NOT favorite
//                toggleButtonFav.setBackgroundResource(android.R.drawable.btn_star_big_off)
//            }

            val data = HashMap<String, String>()
            data["genre"] = question.genre.toString()

            toggleButtonFav.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    favoriteRef.setValue(data)
                } else {
                    favoriteRef.removeValue()
                }
//                +α オフライン機能
//                favoriteRef.onDisconnect()
            }
            questionDetailListAdapter.notifyDataSetChanged()
        }

        override fun onCancelled(p0: DatabaseError) {

        }
    })
}