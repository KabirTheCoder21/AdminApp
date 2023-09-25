package com.example.notice

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.ContentResolver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.OpenableColumns
import android.provider.Settings
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.example.adminapp.R
import com.example.adminapp.databinding.ActivitySignInBinding
import com.example.adminapp.databinding.ActivityUploadNoticeBinding
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Calendar

class UploadNotice : AppCompatActivity() {
    private lateinit var binding : ActivityUploadNoticeBinding
    private  var bitmap: Bitmap? =null
    private lateinit var databaseReference: DatabaseReference
    private lateinit var dbRef : DatabaseReference
    private lateinit var storageReference: StorageReference
    private  var downloadUrl : String = ""
    private lateinit var progressDialog : ProgressDialog
    companion object{
        val IMAGE_REQUEST_CODE = 100
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityUploadNoticeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor= resources.getColor(R.color.statusBarColor_1)
        supportActionBar?.setBackgroundDrawable(ColorDrawable(Color.parseColor("#424F93"))) // Replace with your desired color
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        databaseReference = FirebaseDatabase.getInstance().getReference()
        storageReference = FirebaseStorage.getInstance().getReference()
        progressDialog = ProgressDialog(this)
        supportActionBar?.title = "Upload Notice"

//        if (supportActionBar != null) {
//            supportActionBar!!.hide()
//        }

        binding.selectImage.setOnClickListener {
            //pickImageGallery()
            ImagePicker.with(this)
                .crop()
                .compress(1024)
                .maxResultSize(1080, 1080)
                .start(IMAGE_REQUEST_CODE)
        }
        binding.uploadNoticeButton.setOnClickListener{
            if(bitmap==null)
            {
                Toast.makeText(this, "select image !", Toast.LENGTH_SHORT).show()
            }
            else if(binding.noticeTitle.text.toString().isEmpty())
            {
                binding.noticeTitle.setError("Empty")
                binding.noticeTitle.requestFocus()
            }
            else
            {
                uploadImage()
            }
        }
    }


    private fun uploadImage() {
        progressDialog.setMessage("Uploading...")
        progressDialog.show()
        val outputStream = ByteArrayOutputStream()
        bitmap?.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        val compressImage = outputStream.toByteArray()

        // Generate a unique filename, for example, using a timestamp
        val timestamp = System.currentTimeMillis().toString()
        val filename = "compressed_image_$timestamp.jpg"
        val filePath: StorageReference = storageReference.child("Notice").child(filename)
        val uploadTask: UploadTask = filePath.putBytes(compressImage)
        uploadTask.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                uploadTask.addOnSuccessListener {
                    filePath.downloadUrl.addOnSuccessListener {
                        downloadUrl = it.toString()
                        uploadData()
                    }
                }
            }else{
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show()
                progressDialog.dismiss()
            }
        }

    }

    @SuppressLint("SimpleDateFormat")
    private fun uploadData() {
        dbRef = databaseReference.child("Notice")
        val uniqueKey : String? = dbRef.push().key
        val noticeTitle = binding.noticeTitle.text.toString()


        val calForDate : Calendar = Calendar.getInstance()
        val currDate = SimpleDateFormat("dd-MM-yy")
        val date : String = currDate.format(calForDate.time)

        val calForTime : Calendar = Calendar.getInstance()
        val currTime = SimpleDateFormat("hh:mm a")
        val time = currTime.format(calForTime.time)

        val noticeData : NoticeData = NoticeData(noticeTitle,downloadUrl,date,time, uniqueKey)
        if (uniqueKey != null) {
            dbRef.child(uniqueKey).setValue(noticeData).addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Notice uploaded!", Toast.LENGTH_SHORT).show()
                binding.fileName.text = ""
                binding.noticeTitle.text?.clear()
                bitmap=null
                binding.noticeImageView.visibility = View.GONE
            }.addOnFailureListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Something went wrong!", Toast.LENGTH_SHORT).show()
            }
        }
    }

   /* private fun pickImageGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_REQUEST_CODE)
    }*/

    */
    private fun uriToBitmap(contentResolver: ContentResolver, imageUri: Uri): Bitmap? {
        try {
            // Open an input stream from the URI
            val inputStream: InputStream? = contentResolver.openInputStream(imageUri)

            // Decode the input stream into a Bitmap
            bitmap = inputStream?.let { BitmapFactory.decodeStream(it) }

            // Close the input stream
            inputStream?.close()

            return bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }


    private fun getImageName(imageUri: Uri?): String {
        val contentResolver = applicationContext.contentResolver
        var displayName = ""
        try {
            val cursor = imageUri?.let {
                contentResolver.query(
                    it,
                    null,
                    null,
                    null,
                    null
                )
            }
            cursor.use {
                if (it != null) {
                    if (it.moveToFirst()) {
                        val displayNameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (displayNameIndex != -1) {
                            displayName = it.getString(displayNameIndex)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (displayName.isEmpty()) {
            // If the DISPLAY_NAME column is not available, extract the name from the URI itself
            val pathSegments = imageUri?.pathSegments
            if (pathSegments != null) {
                if (pathSegments.isNotEmpty()) {
                    displayName = pathSegments.last()
                }
            }
        }
        return displayName
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                // Handle the Up button click event (back button)
                onBackPressed()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode== IMAGE_REQUEST_CODE&&resultCode== RESULT_OK)
        {
            val imageUri : Uri? = data?.data
            val contentResolver: ContentResolver = this.contentResolver
            bitmap = imageUri?.let { uriToBitmap(contentResolver, it) }

            //binding.noticeImageView.setImageURI(data?.data)
            if (bitmap != null) {
                // Do something with the bitmap, e.g., set it to an ImageView
                binding.noticeImageView.visibility = View.VISIBLE
                binding.noticeImageView.setImageBitmap(bitmap)
                binding.noticeImageView.scaleType = ImageView.ScaleType.FIT_XY
               val fname = getImageName(imageUri)
                binding.fileName.text = fname
            } else {
                // Handle the case where the conversion failed
                Toast.makeText(this, "Error pta nhi kyu aa rha hh", Toast.LENGTH_SHORT).show()
            }
        }
    }
}