package com.example.qa_app

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_favorite.*
import kotlinx.android.synthetic.main.content_favorite.*

class FavoriteActivity : AppCompatActivity() {

    private lateinit var databaseReference: DatabaseReference

    private lateinit var favoriteQuestionArrayList: ArrayList<Question>
    private lateinit var favoriteQuestionListAdapter: FavoriteQuestionListAdapter

    private lateinit var currentUser: FirebaseUser

    private lateinit var favoriteRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorite)
        setSupportActionBar(toolbar)

        title = "お気に入り"

        listViewFav.setOnItemClickListener { _, _, i, _ ->
            val intent = Intent(this, QuestionDetailActivity::class.java)
            intent.putExtra(QuestionIntentKEY, favoriteQuestionArrayList[i])
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()

        // Firebase
        databaseReference = FirebaseDatabase.getInstance().reference

        currentUser = FirebaseAuth.getInstance().currentUser!!
        favoriteRef = databaseReference.child(FavoritePATH).child(currentUser.uid)

        // setting of listView
        favoriteQuestionListAdapter = FavoriteQuestionListAdapter(this)
        favoriteQuestionArrayList = ArrayList()
        favoriteQuestionListAdapter.notifyDataSetChanged()

        favoriteRef.addChildEventListener(childEventListener)

        listViewFav.adapter = favoriteQuestionListAdapter
        favoriteQuestionListAdapter.setFavoriteQuestionArrayList(favoriteQuestionArrayList)
    }

    private val childEventListener = object : ChildEventListener {

        override fun onChildAdded(p0: DataSnapshot, p1: String?) {
            val map = p0.value as Map<String, String>
            val genre = map["genre"] ?: ""

            val questionRef = databaseReference.child(ContentsPATH).child(genre).child(p0.key.toString())
            questionRef.addValueEventListener(valueEventListener)
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

    private val valueEventListener = object : ValueEventListener {

        override fun onDataChange(p0: DataSnapshot) {
            if (p0.value != null) {
                val map = p0.value as Map<String, String>
                val title = map["title"] ?: ""
                val body = map["body"] ?: ""
                val userName = map["userName"] ?: ""
                val uid = map["uid"] ?: ""
                val genre = map["genre"] ?: ""
                val imageString = map["image"] ?: ""
                val bytes =
                    if (imageString.isNotEmpty()) {
                        Base64.decode(imageString, Base64.DEFAULT)
                    } else {
                        byteArrayOf()
                    }

                val answerArrayList = ArrayList<Answer>()
                val answerMap = map["answers"] as Map<String, String>?
                if (answerMap != null) {
                    for (key in answerMap.keys) { // keys: "answers". e.g. key = answer1: body1;name1;uid1
                        val temp = answerMap[key] as Map<String, String>
                        val answerBody = temp["body"] ?: ""
                        val answerUserName = temp["userName"] ?: ""
                        val answerUid = temp["uid"] ?: ""
                        val answer = Answer(answerBody, answerUserName, answerUid, key)
                        answerArrayList.add(answer)
                    }
                }

                val question = Question(title, body, userName, uid, p0.key ?: "", genre.toInt(), bytes, answerArrayList)
                favoriteQuestionArrayList.add(question)
                favoriteQuestionListAdapter.notifyDataSetChanged()
            }
        }

        override fun onCancelled(p0: DatabaseError) {
        }
    }
}