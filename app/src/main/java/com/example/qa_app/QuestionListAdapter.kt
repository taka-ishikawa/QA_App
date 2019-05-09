package com.example.qa_app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView

class QuestionListAdapter(context: Context): BaseAdapter() {
    val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private var questionArrayList = ArrayList<Question>()

    override fun getCount(): Int {
        return questionArrayList.size
    }

    override fun getItem(position: Int): Any {
        return questionArrayList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var convertView = convertView
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.list_question, parent, false)
        }

        val textViewTitle = convertView!!.findViewById<View>(R.id.textViewTitle) as TextView
        textViewTitle.text = questionArrayList[position].title

        val textViewUserName = convertView.findViewById<View>(R.id.textViewUserName) as TextView
        textViewUserName.text = questionArrayList[position].userName

        val textViewRes = convertView.findViewById<View>(R.id.textViewRes) as TextView
        textViewRes.text = questionArrayList[position].answers.size.toString()

        val bytes = questionArrayList[position].imageBytes
        if (bytes.isNotEmpty()) {
            val image = BitmapFactory.decodeByteArray(bytes, 0, bytes.size).copy(Bitmap.Config.ARGB_8888, true)
            val imageViewQuestion = convertView.findViewById<View>(R.id.imageViewQuestion) as ImageView
            imageViewQuestion.setImageBitmap(image)
        }

        return  convertView
    }

    fun setQuestionArrayList(questionArrayList: ArrayList<Question>) {
        this.questionArrayList = questionArrayList
    }
}