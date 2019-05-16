package com.example.qa_app

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.design.widget.Snackbar
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.util.Base64
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.INVISIBLE
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_main.*

import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private var genre = 0

    private lateinit var databaseReference: DatabaseReference
    private lateinit var questionArrayList: ArrayList<Question>
    private lateinit var questionListAdapter: QuestionListAdapter

    private var genreRef: DatabaseReference ?= null

    private var currentUser: FirebaseUser? = null

    private val childEventLister = object : ChildEventListener {
        override fun onChildAdded(p0: DataSnapshot, p1: String?) { // when question added
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

            val question = Question(title, body, userName, uid, p0.key ?: "", genre, bytes, answerArrayList)
            questionArrayList.add(question)
            questionListAdapter.notifyDataSetChanged()
        }

        override fun onChildChanged(p0: DataSnapshot, p1: String?) { // when question changed (answer added)
            val map = p0.value as Map<String,String>

            // find question changed
            for (question in questionArrayList) {
                if (p0.key.equals(question.questionUid)) { // このアプリで変更がある可能性があるのは回答(Answer)のみ
                    question.answers.clear()
                    val answerMap = map["answers"] as Map<String, String>?
                    if (answerMap != null) {
                        for (key in answerMap.keys) {
                            val temp = answerMap[key] as Map<String, String>
                            val answerBody = temp["body"] ?: ""
                            val answerUserName = temp["userName"] ?: ""
                            val answerUid = temp["uid"] ?: ""
                            val answer = Answer(answerBody, answerUserName, answerUid, key)
                            question.answers.add(answer)
                        }
                        questionListAdapter.notifyDataSetChanged()
                    }
                }
            }
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
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        // setting of drawer
        val toggle = ActionBarDrawerToggle(this, drawer_layout, toolbar, R.string.app_name, R.string.app_name)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        // Firebase
        databaseReference = FirebaseDatabase.getInstance().reference

        // preparation for listView
        questionListAdapter = QuestionListAdapter(this)
        questionArrayList = ArrayList()
        questionListAdapter.notifyDataSetChanged()

        fab.setOnClickListener {
            if (genre == 0) {
                Snackbar.make(it, "ジャンルを選択してください", Snackbar.LENGTH_LONG).show()
            }

            if (currentUser == null) {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            } else {
                val intent = Intent(this, QuestionSendActivity::class.java)
                intent.putExtra(GenreIntentKEY, genre)
                startActivity(intent)
            }
        }

        nav_view.setNavigationItemSelectedListener (this)

        listView.setOnItemClickListener { _, _, i, _ ->
            val intent = Intent(this, QuestionDetailActivity::class.java)
            intent.putExtra(QuestionIntentKEY, questionArrayList[i])
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        val navigationView = findViewById<NavigationView>(R.id.nav_view)

        // 1:趣味を既定の選択とする
        if (genre == 0) {
            onNavigationItemSelected(navigationView.menu.getItem(0))
        }

        currentUser = FirebaseAuth.getInstance().currentUser
        val navItemFavorite = navigationView.menu.findItem(R.id.nav_favorite)
        navItemFavorite.isVisible = (currentUser != null)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        if (item.itemId == R.id.action_settings){
            val intent = Intent(this, SettingActivity::class.java)
            startActivity(intent)
            return true
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
//            R.id.nav_all -> {
//                toolbar.title = "すべて"
//                genre = 0
//            }
            R.id.nav_hobby -> {
                toolbar.title = "趣味"
                genre = 1
            }
            R.id.nav_life -> {
                toolbar.title = "生活"
                genre = 2
            }
            R.id.nav_health -> {
                toolbar.title = "健康"
                genre = 3
            }
            R.id.nav_computer -> {
                toolbar.title = "コンピューター"
                genre = 4
            }
            R.id.nav_favorite -> {
                val intent = Intent(this, FavoriteActivity::class.java)
                startActivity(intent)
            }
        }

        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        drawer.closeDrawer(GravityCompat.START)

        // 質問のリストをクリアしてから再度Adapterにセットし、AdapterをListViewにセットし直す
        questionArrayList.clear()
        questionListAdapter.setQuestionArrayList(questionArrayList) //questionArrayList: @MainActivity
        listView.adapter = questionListAdapter

        // 選択したジャンルにリスナーを登録する
        if (genreRef != null) { // 前に選択されてたジャンルを削除
            genreRef!!.removeEventListener(childEventLister)
        }
//        genreRef =
//            if (genre == 0) {
//                databaseReference.child(ContentsPATH)
//            } else {
//                databaseReference.child(ContentsPATH).child(genre.toString())
//            }
        genreRef = databaseReference.child(ContentsPATH).child(genre.toString())
        genreRef!!.addChildEventListener(childEventLister)

        return true
    }
}
