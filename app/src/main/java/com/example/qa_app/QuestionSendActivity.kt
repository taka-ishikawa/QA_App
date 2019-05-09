package com.example.qa_app

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.MediaStore
import android.support.design.widget.Snackbar
import android.util.Base64
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_question_send.*
import java.io.ByteArrayOutputStream

class QuestionSendActivity : AppCompatActivity(), View.OnClickListener, DatabaseReference.CompletionListener {

    companion object {
        private val PERMISSIONS_REQUEST_CODE = 100
        private val CHOOSER_REQUEST_CODE = 100
    }

    private var genre: Int = 0
    private var pictureUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_question_send)

        title = "質問作成"

        genre = intent.extras!!.getInt(GenreIntentKEY)

        buttonSend.isEnabled = true

        buttonSend.setOnClickListener(this)
        imageView.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        if (v == imageView) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED
                ) { // 許可されている
                    showChooser()
                } else { // 許可されていないので許可ダイアログを表示する
                    requestPermissions(
                        arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        PERMISSIONS_REQUEST_CODE
                    )
                    return
                }
            } else
                showChooser()

        } else if (v == buttonSend) {
            progressBar.visibility = View.VISIBLE
            buttonSend.isEnabled = false
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(v!!.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)

            // get data reference　from Firebase
            val dataBaseReference = FirebaseDatabase.getInstance().reference
            val genreRef = dataBaseReference.child(ContentsPATH).child(genre.toString())

            val data = HashMap<String, String>()

            data["uid"] = FirebaseAuth.getInstance().currentUser!!.uid

            val titleQuestion = editTextTitle.text.toString()
            val body = editTextBody.text.toString()

            if (titleQuestion.isEmpty()) {
                Snackbar.make(v, "タイトルを入力してください", Snackbar.LENGTH_LONG).show()
                buttonSend.isEnabled = true
                progressBar.visibility = View.INVISIBLE
                return
            }

            if (body.isEmpty()) {
                Snackbar.make(v, "質問内容を入力してください", Snackbar.LENGTH_LONG).show()
                buttonSend.isEnabled = true
                progressBar.visibility = View.INVISIBLE
                return
            }

            data["title"] = titleQuestion
            data["body"] = body

            // get name from Preference
            val sharedPreference = PreferenceManager.getDefaultSharedPreferences(this)
            val userName = sharedPreference.getString(UserNameKEY, "")!!.toString()
            data["userName"] = userName

            // as? ?: for safety. if imageView is not set, return null
            val drawable = imageView.drawable as? BitmapDrawable
            if (drawable != null) { // get image/ encode BASE64
                val bitmap = drawable.bitmap
                val baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos)
                val bitmapString = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT)
                data["image"] = bitmapString

            } else { // dialog "画像が添付されていません。このまま投稿しますか？"　的な y->break, n->return

            }

            // send question: register genre/ newTitle/ newBody/ idNewImage with Firebase
            genreRef.push().setValue(data, this)
//                data:
//                data["uid"] = FirebaseAuth.getInstance().currentUser!!.uid
//                data["title"] = titleQuestion
//                data["body"] = body
//                data["userName"] = userName
//                data["image"] = bitmapString
            progressBar.visibility = View.INVISIBLE
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // ユーザーが許可したとき
                    showChooser()
                }
                return
            }
        }
    }

    private fun showChooser() { // choose image from external storage
        // Intent from gallery
        val galleryIntent = Intent(Intent.ACTION_GET_CONTENT)
        galleryIntent.type = "image/*"
        galleryIntent.addCategory(Intent.CATEGORY_OPENABLE)

        // Intent from camera
        val filename = System.currentTimeMillis().toString() + ".jpg"
        val contentValues = ContentValues()
        contentValues.put(MediaStore.Images.Media.TITLE, filename)
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        pictureUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, pictureUri)

        // call createChooser()
        val chooserIntent = Intent.createChooser(galleryIntent, "getImage")
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(cameraIntent))

        startActivityForResult(chooserIntent, CHOOSER_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CHOOSER_REQUEST_CODE) {
            if (resultCode != Activity.RESULT_OK) {
                if (pictureUri != null) {
                    contentResolver.delete(pictureUri!!, null, null)
                    pictureUri = null
                }
                return
            }
        }
        // get picture uri
        val uri = if (data == null || data.data == null) pictureUri else data.data

        //URI -> Bitmap
        val image: Bitmap
        try {
            val inputStream = contentResolver.openInputStream(uri!!)
            image = BitmapFactory.decodeStream(inputStream)
            inputStream!!.close()
        } catch (e: Exception) {
            return
        }

        // the longer side of Bitmap -> 500 pixel
        val imageWidth = image.width
        val imageHeight = image.height
        val scale = Math.min(500.toFloat() / imageWidth, 500.toFloat() / imageHeight) // (1)

        Matrix().postScale(scale, scale)

        val resizedImage = Bitmap.createBitmap(image, 0, 0, imageWidth, imageHeight, Matrix(), true)

        // BitmapをImageViewに設定する
        imageView.setImageBitmap(resizedImage)

        pictureUri = null
    }

    override fun onComplete(databaseError: DatabaseError?, databaseReference: DatabaseReference) {
        progressBar.visibility = View.GONE

        if (databaseError == null) {
            Toast.makeText(this, "投稿しました", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Snackbar.make(findViewById(android.R.id.content), "投稿に失敗しました", Snackbar.LENGTH_LONG).show()
        }
    }
}