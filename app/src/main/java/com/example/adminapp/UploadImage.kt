package com.example.adminapp

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
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.example.adminapp.databinding.ActivityUploadImageBinding
import com.example.notice.UploadNotice
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import java.io.ByteArrayOutputStream
import java.io.InputStream

class UploadImage : AppCompatActivity() {
    private lateinit var binding : ActivityUploadImageBinding
    private lateinit var category : String
    companion object{
        const val IMAGE_REQUEST_CODE = 100
    }
    private  var bitmap: Bitmap? =null
    private lateinit var progressDialog: ProgressDialog
    private lateinit var databaseReference: DatabaseReference
    private lateinit var storageReference: StorageReference
    private  var downloadUrl : String = ""
    private lateinit var imageFileName : String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor=resources.getColor(R.color.statusBarColor_2)
        supportActionBar?.title = "Upload Image"
        supportActionBar?.setBackgroundDrawable(ColorDrawable(Color.parseColor("#F73E7C"))) // Replace with your desired color

        // Enable the Up button (back button)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding = DataBindingUtil.setContentView(this,R.layout.activity_upload_image)
        progressDialog = ProgressDialog(this)

        databaseReference = FirebaseDatabase.getInstance().getReference().child("gallery")
        storageReference = FirebaseStorage.getInstance().getReference().child("gallery")

//        supportActionBar.setBackgroundDrawable(ColorDrawable(Drawable(Resourceg)))
        val items : List<String> = listOf("Select Category","Convocation","Independence Day","Other Events")
        val adapter = ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,items)

//        if (supportActionBar != null) {
//            supportActionBar!!.hide()
//        }

        binding.imageCategory.adapter = adapter
        binding.imageCategory.onItemSelectedListener = object  : AdapterView.OnItemSelectedListener
        {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
               category = binding.imageCategory.selectedItem.toString()
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }

        }
        binding.selectGalleryImage.setOnClickListener {
            openGallery()
        }
        binding.uploadImageButton.setOnClickListener {
            if(bitmap==null)
            {
                Toast.makeText(this, "Please select image", Toast.LENGTH_SHORT).show()
            }
            else if(category.equals("Select Category"))
            {
                Toast.makeText(this, "Please select Image Category", Toast.LENGTH_SHORT).show()
            }
            else{
                progressDialog.setMessage("Uploading")
                    progressDialog.show()
                uploadImage()
            }
        }
    }

    private fun uploadImage() {
        val outputStream = ByteArrayOutputStream()
        bitmap?.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        val compressImage = outputStream.toByteArray()

        // Generate a unique filename, for example, using a timestamp
        //val timestamp = System.currentTimeMillis().toString()
        val filename = imageFileName
        val filePath: StorageReference = storageReference.child(filename)
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

    private fun uploadData() {
        databaseReference = databaseReference.child(category)
        val uniqueKey : String? = databaseReference.push().key
        if(uniqueKey!=null)
        {
            databaseReference.child(uniqueKey).setValue(downloadUrl).addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Image uploaded!", Toast.LENGTH_SHORT).show()
                binding.fileName.text=""
                databaseReference = FirebaseDatabase.getInstance().getReference().child("gallery")
                binding.imageCategory.setSelection(0)
                category="Select Category"
                bitmap=null
                binding.galleryImageView.setImageResource(android.R.color.transparent)
            }.addOnFailureListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Something went wrong!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_REQUEST_CODE)
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
        if (requestCode== UploadNotice.IMAGE_REQUEST_CODE && resultCode== RESULT_OK)
        {
            val imageUri : Uri? = data?.data
            imageFileName = getImageName(imageUri)
            binding.fileName.text = imageFileName
            val contentResolver: ContentResolver = this.contentResolver
            bitmap = imageUri?.let { uriToBitmap(contentResolver, it) }

            //binding.noticeImageView.setImageURI(data?.data)
            if (bitmap != null) {
                // Do something with the bitmap, e.g., set it to an ImageView
                binding.galleryImageView.setImageBitmap(bitmap)
                binding.galleryImageView.scaleType = ImageView.ScaleType.FIT_CENTER
            } else {
                // Handle the case where the conversion failed
                Toast.makeText(this, "Error pta nhi kyu aa rha hh", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getImageName(imageUri: Uri?): String {
        val contentResolver = applicationContext.contentResolver
        var displayName=""
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
                    if(it.moveToFirst()) {
                        val displayNameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (displayNameIndex != -1) {
                            displayName = it.getString(displayNameIndex)
                        }
                    }
                }
            }
        }catch (e:Exception)
        {
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