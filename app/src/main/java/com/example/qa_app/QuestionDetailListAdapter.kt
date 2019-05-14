package com.example.qa_app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import android.widget.ToggleButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class QuestionDetailListAdapter(context: Context, private val question: Question) : BaseAdapter() {
    companion object {
        private val TYPE_QUESTION = 0
        private val TYPE_ANSWER = 1
    }

    var layoutInflater: LayoutInflater ?= null

    init {
        layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            TYPE_QUESTION
        } else {
            TYPE_ANSWER
        }
    }

    override fun getViewTypeCount(): Int {
        return 2 // ?
    }

    override fun getCount(): Int {
        return 1 + question.answers.size
    }

    override fun getItem(position: Int): Any {
        return question
    }

    override fun getItemId(position: Int): Long {
        return 0 // ?
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var convertView = convertView

        if (getItemViewType(position) == TYPE_QUESTION) {
            if (convertView == null) {
                convertView = layoutInflater!!.inflate(R.layout.list_question_detail, parent, false)!!
            }

            val textViewBody = convertView.findViewById<View>(R.id.textViewBody) as TextView
            textViewBody.text = question.body

            val textViewUserName = convertView.findViewById<View>(R.id.textViewUserName) as TextView
            textViewUserName.text = question.userName

            val bytes = question.imageBytes
            if (bytes.isNotEmpty()) {
                val image = BitmapFactory.decodeByteArray(bytes, 0, bytes.size).copy(Bitmap.Config.ARGB_8888, true)
                val imageViewQuestionDetail = convertView.findViewById<View>(R.id.imageViewQuestionDetail) as ImageView
                imageViewQuestionDetail.setImageBitmap(image)
            }

            val toggleButtonFav = convertView.findViewById<View>(R.id.toggleButtonFav) as ToggleButton
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser == null) {
                toggleButtonFav.visibility = View.INVISIBLE
            } else { //TODO(if favorite -> star, not favorite -> star_border)
                //　わからんこと：　favoriteRefに登録されているかを確認する方法
                // こう言うValueを持つparent を探す方法
                toggleButtonFav.visibility = View.VISIBLE
                val favoriteRef = FirebaseDatabase.getInstance().reference.child(FavoritePATH).child(currentUser.uid).child(question.questionUid)
                val data = HashMap<String, String>()
                data["genre"] = question.genre.toString()

                toggleButtonFav.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        toggleButtonFav.setBackgroundResource(R.drawable.ic_star_24dp)
                        favoriteRef.setValue(data)
                    } else {
                        toggleButtonFav.setBackgroundResource(R.drawable.ic_star_border_24dp)
                        favoriteRef.removeValue()
                    }
                }
            }

        } else if (getItemViewType(position) == TYPE_ANSWER) {
            if (convertView == null) {
                convertView = layoutInflater!!.inflate(R.layout.list_answer, parent, false)!!
            }

            val textViewBody = convertView.findViewById<View>(R.id.textViewBody) as TextView
            textViewBody.text = question.answers[position - 1].body // ? position - 1

            val textViewUserName = convertView.findViewById<View>(R.id.textViewUserName) as TextView
            textViewUserName.text = question.userName
        }
        return convertView!!
    }
}