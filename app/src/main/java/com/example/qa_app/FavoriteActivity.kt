package com.example.qa_app

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.util.Base64
import android.util.Log
import android.view.MenuItem
import android.widget.BaseAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_favorite.*
import kotlinx.android.synthetic.main.content_favorite.*
import kotlinx.android.synthetic.main.content_main.*

class FavoriteActivity : AppCompatActivity() {

    private lateinit var databaseReference: DatabaseReference

    private lateinit var favoriteQuestionArrayList: ArrayList<Question>
    private lateinit var favoriteQuestionListAdapter: FavoriteQuestionListAdapter

    private lateinit var currentUser: FirebaseUser

    private lateinit var favoriteRef: DatabaseReference

    private var genre = "0"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorite)
        setSupportActionBar(toolbar)

        title = "お気に入り"

//        // setting of drawer
//        val toggle = ActionBarDrawerToggle(this, drawer_layout_fav, toolbar, R.string.app_name, R.string.app_name)
//        drawer_layout_fav.addDrawerListener(toggle)
//        toggle.syncState()

        // Firebase
        databaseReference = FirebaseDatabase.getInstance().reference

        currentUser = FirebaseAuth.getInstance().currentUser!!
        favoriteRef = databaseReference.child(FavoritePATH).child(currentUser.uid)
//        favoriteRef.addValueEventListener(valueEventListener)

        listViewFav.setOnItemClickListener { _, _, i, _ ->
            val intent = Intent(this, QuestionDetailActivity::class.java)
            intent.putExtra(QuestionIntentKEY, favoriteQuestionArrayList[i])
            startActivity(intent)
        }
//        nav_view_fav.setNavigationItemSelectedListener(this)
    }

    override fun onResume() {
        super.onResume()

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
            Log.d("dataSnapshot.key", "FavoriteActivity; Child: ${p0.key}")
            val map = p0.value as Map<String, String>
            Log.d("dataSnapshot.value", "FavoriteActivity; Child: ${map.values}")
            genre = map["genre"] ?: ""
            Log.d("dataSnapshot.genre", "FavoriteActivity; Child $genre")

            val questionRef = databaseReference.child(ContentsPATH).child(genre).child(p0.key.toString())
//            questionRef.addChildEventListener(childEventListenerToGetQuestion)
            questionRef.addValueEventListener(valueEventListener)

//
//            val questionRef = databaseReference.child(ContentsPATH).child(genre).child(p0.key.toString())
//            val mapContents = questionRef.key!!.map { } as Map<String, String>
//            Log.d("map", "mapContents: $mapContents")
//
//            val title = mapContents["title"] ?: ""
//            val body = mapContents["body"] ?: ""
//            val userName = mapContents["userName"] ?: ""
//            val uid = mapContents["uid"] ?: ""
//            val imageString = mapContents["image"] ?: ""
//            val bytes =
//                if (imageString.isNotEmpty()) {
//                    Base64.decode(imageString, Base64.DEFAULT)
//                } else {
//                    byteArrayOf()
//                }
//
//            val answerArrayList = ArrayList<Answer>()
//            val answerMap = mapContents["answers"] as Map<String, String>?
//            if (answerMap != null) {
//                for (key in answerMap.keys) { // keys: "answers". e.g. key = answer1: body1;name1;uid1
//                    val temp = answerMap[key] as Map<String, String>
//                    val answerBody = temp["body"] ?: ""
//                    val answerUserName = temp["userName"] ?: ""
//                    val answerUid = temp["uid"] ?: ""
//                    val answer = Answer(answerBody, answerUserName, answerUid, key)
//                    answerArrayList.add(answer)
//                }
//            }
//
//            val favRef = FirebaseDatabase.getInstance().reference.child(FavoritePATH).child(currentUser.uid).child(p0.key.toString())
//            val question = Question(title, body, userName, uid, p0.key ?: "",  p0.key.toString().toInt(), bytes, answerArrayList)
//            favoriteQuestionArrayList.add(question)
//            favoriteQuestionListAdapter.notifyDataSetChanged()
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
            Log.d("dataSnapshot.key", "FavoriteActivity; Value: ${p0.value}")
//            val map = p0.value as Map<String, String>?
//            Log.d("dataSnapshot.key", "FavoriteActivity; Value: $map")
            val map = p0.value as Map<String, String>
            val title = map["title"] ?: ""
            val body = map["body"] ?: ""
            val userName = map["userName"] ?: ""
            val uid = map["uid"] ?: ""
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

//
//            if (map != null) {
////                val favoriteQuestionUidList = map.keys as ArrayList<String>
//                Log.d("map", favoriteQuestionUidArrayList.toString())
//                for (i in 1..map.keys.size) {
//                    val questionUid = map.keys.toList()[i]
//                    favoriteQuestionUidArrayList.add(questionUid)
//                }
//            }


//            if (map != null) {
//                for (i in 0..map.keys.size) {
//                    val questionRef = databaseReference.child(ContentsPATH).child(genre).child(p0.key!![i].toString())
//                    Log.d("map", "questionRef: $questionRef")
//                    val mapContents = questionRef.key!![i] as Map<String, String>?
//                    Log.d("map", "mapContents: $mapContents")
//                    val title = mapContents!!["title"] ?: ""
//                    val body = mapContents["body"] ?: ""
//                    val userName = mapContents["userName"] ?: ""
//                    val uid = mapContents["uid"] ?: ""
//                    val imageString = mapContents["image"] ?: ""
//                    val bytes =
//                        if (imageString.isNotEmpty()) {
//                            Base64.decode(imageString, Base64.DEFAULT)
//                        } else {
//                            byteArrayOf()
//                        }
//
//                    val answerArrayList = ArrayList<Answer>()
//                    val answerMap = mapContents["answers"] as Map<String, String>?
//                    if (answerMap != null) {
//                        for (key in answerMap.keys) { // keys: "answers". e.g. key = answer1: body1;name1;uid1
//                            val temp = answerMap[key] as Map<String, String>
//                            val answerBody = temp["body"] ?: ""
//                            val answerUserName = temp["userName"] ?: ""
//                            val answerUid = temp["uid"] ?: ""
//                            val answer = Answer(answerBody, answerUserName, answerUid, key)
//                            answerArrayList.add(answer)
//                        }
//                    }
//
////            val favRef = FirebaseDatabase.getInstance().reference.child(FavoritePATH).child(currentUser.uid).child(p0.key.toString())
//                    val question = Question(title, body, userName, uid, p0.key ?: "",  p0.key.toString().toInt(), bytes, answerArrayList)
//                    favoriteQuestionArrayList.add(question)
//                    favoriteQuestionListAdapter.notifyDataSetChanged()
//                }
//            }
        }

        override fun onCancelled(p0: DatabaseError) {
        }
    }

    private val childEventListenerToGetQuestion = object : ChildEventListener {
        override fun onChildAdded(p0: DataSnapshot, p1: String?) {
            Log.d("dataSnapshot", "${p0.key}")
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
}