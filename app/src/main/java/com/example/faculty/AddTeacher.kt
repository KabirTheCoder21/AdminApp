package com.example.faculty

import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.example.adminapp.R
import com.example.adminapp.databinding.ActivityAddTeacherBinding
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.InputStream

class AddTeacher : AppCompatActivity() {
    private lateinit var binding : ActivityAddTeacherBinding
    companion object{
        const val IMAGE_REQUEST_CODE = 100
    }
    private val CLEAR_DATA_DELAY = 2*60*1000

    private lateinit var progressDialog : ProgressDialog
    private lateinit var category : String
    private  var bitmap: Bitmap? =null
    private lateinit var databaseReference: DatabaseReference
    private lateinit var dbRef : DatabaseReference
    private lateinit var storageReference: StorageReference
    private var downloadUrl : String = ""
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var timestamp : String
    private lateinit var filename : String
private val handler = Handler()
    val removeDataRunnable = Runnable{
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_add_teacher)
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Faculty")
        storageReference = FirebaseStorage.getInstance().getReference()
        progressDialog = ProgressDialog(this)

        sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)

        binding.addTeacherImage.setOnClickListener {
           // openGallery()
            ImagePicker.with(this)
                .crop()
                .compress(1024)
                .maxResultSize(1080, 1080)
                .start()

        }
        val items : List<String> = listOf("Select Category","Computer Science","Information Technology","Electronics Engineering","Electrical Engineering","Mechanical Engineering")
        val adapter = ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,items)

        binding.addTeacherCategory.adapter = adapter

        binding.addTeacherCategory.onItemSelectedListener = object  : AdapterView.OnItemSelectedListener
        {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                category = binding.addTeacherCategory.selectedItem.toString()
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }

        }

        binding.addTeacherName.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                lifecycleScope.launch {
                    saveTemporaryData("temporaryName", p0.toString())
                }
            }

        })
        runOnUiThread {
            val savedName = sharedPreferences.getString("temporaryName", "")
            binding.addTeacherName.setText(savedName)
        }
            binding.addTeacherBtn.setOnClickListener {
            checkValidation()
        }

    }
    override fun onResume() {
        super.onResume()
        // Remove the timer when the activity is paused
        handler.removeCallbacks(removeDataRunnable)
    }

    override fun onPause() {
        super.onPause()
        // Start the timer to clear data after a short span of time
        handler.postDelayed(removeDataRunnable, CLEAR_DATA_DELAY.toLong())
    }

    private fun saveTemporaryData(key: String, value: String) {
        val editor = sharedPreferences.edit()
        editor.putString(key, value)
        editor.apply()
    }

    private fun checkValidation() {
        if(bitmap==null)
        {
            Toast.makeText(this, "Select profile Image", Toast.LENGTH_SHORT).show()
        }
        else if(binding.addTeacherName.text.toString().isEmpty())
        {
            Toast.makeText(this, "Enter Name", Toast.LENGTH_SHORT).show()
            binding.addTeacherName.setError("Empty!")
            binding.addTeacherName.requestFocus()
        }
        else if (binding.addTeacherEmail.text.toString().isEmpty())
        {
            Toast.makeText(this, "Enter Email", Toast.LENGTH_SHORT).show()
            binding.addTeacherEmail.setError("Empty!")
            binding.addTeacherName.requestFocus()
        }
        else if(category.equals("Select Category"))
        {
            Toast.makeText(this, "Select Category", Toast.LENGTH_SHORT).show()

        }
        else uploadData()
    }

    private fun uploadData() {
        progressDialog.setMessage("Uploading...")
        progressDialog.show()
        val outputStream = ByteArrayOutputStream()
        bitmap?.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        val compressImage = outputStream.toByteArray()

        // Generate a unique filename, for example, using a timestamp
         timestamp = System.currentTimeMillis().toString()
        filename = "compressed_image_$timestamp.jpg"
        val filePath: StorageReference = storageReference.child("Faculty").child(filename)
        val uploadTask: UploadTask = filePath.putBytes(compressImage)
        uploadTask.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                uploadTask.addOnSuccessListener {
                    filePath.downloadUrl.addOnSuccessListener {
                        downloadUrl = it.toString()
                        uploadDataToDatabase()
                    }
                }
            }else{
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show()
                progressDialog.dismiss()
            }
        }

    }

    private fun uploadDataToDatabase() {
        dbRef = databaseReference.child(category)
        val uniqueKey : String? = dbRef.push().key
        val name = binding.addTeacherName.text.toString()
        val email = binding.addTeacherEmail.text.toString()
        val post = binding.addTeacherPost.text.toString()
        val facultyData = FacultyData(downloadUrl,name,email,post,uniqueKey,filename)
        if (uniqueKey != null) {
            dbRef.child(uniqueKey).setValue(facultyData).addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Faculty Data uploaded!", Toast.LENGTH_SHORT).show()
                binding.addTeacherImage.setImageResource(R.drawable.avtar_profile)
                binding.addTeacherName.setText("")
                binding.addTeacherEmail.setText("")
                binding.addTeacherPost.setText("")
                val itemText = "Select Category"
                val position = (binding.addTeacherCategory.adapter as ArrayAdapter<String>).getPosition(itemText)
                binding.addTeacherCategory.setSelection(position)
            }.addOnFailureListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Something went wrong!", Toast.LENGTH_SHORT).show()
            }
        }
    }

   // private fun openGallery() {
        //val intent = Intent(Intent.ACTION_PICK)
        //intent.type = "image/*"
        //startActivityForResult(intent, IMAGE_REQUEST_CODE)
    //}

   override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode== Activity.RESULT_OK)
        {
            val imageUri : Uri? = data?.data
            val contentResolver: ContentResolver = this.contentResolver
            bitmap = imageUri?.let { uriToBitmap(contentResolver, it) }

            //binding.noticeImageView.setImageURI(data?.data)
            if (bitmap != null) {
                // Do something with the bitmap, e.g., set it to an ImageView
                binding.addTeacherImage.setImageBitmap(bitmap)
            } else if (resultCode == ImagePicker.RESULT_ERROR) {
                Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    }
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

}