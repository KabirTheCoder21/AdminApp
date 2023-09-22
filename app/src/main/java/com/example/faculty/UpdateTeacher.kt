package com.example.faculty

import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.drawToBitmap
import androidx.databinding.DataBindingUtil
import com.example.adminapp.R
import com.example.adminapp.databinding.ActivityUpdateTeacherBinding
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.sql.Timestamp

class UpdateTeacher : AppCompatActivity() {
    private lateinit var binding : ActivityUpdateTeacherBinding
    private  var bitmap: Bitmap? =null
    private lateinit var dbreference : DatabaseReference
    private lateinit var storageReference: StorageReference

    private var downloadUrl = ""
    private lateinit var uniquekey: String
    private lateinit var category: String
    private lateinit var image : String
    private lateinit var fname : String
    private lateinit var filename : String
    private lateinit var progressDialog : ProgressDialog
    private lateinit var timestamp : String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (supportActionBar != null) {
            supportActionBar!!.hide()
        }
        window.statusBarColor = resources.getColor(R.color.statusBarColor_4)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_update_teacher)

        dbreference = FirebaseDatabase.getInstance().reference.child("Faculty")
        storageReference = FirebaseStorage.getInstance().getReference()

        progressDialog = ProgressDialog(this)



        val name = intent.getStringExtra("name")
        val email = intent.getStringExtra("email")
        val post = intent.getStringExtra("post")
         image = intent.getStringExtra("image")!!
        uniquekey = intent.getStringExtra("key")!!
        category = intent.getStringExtra("category")!!
        fname = intent.getStringExtra("fname")!!
        
        timestamp = System.currentTimeMillis().toString()
        filename = "compressed_image_$timestamp.jpg"
        try {
            Picasso.get().load(image)
                .placeholder(R.drawable.avtar_profile)
                .into(binding.avtarProfile)
        }catch (_:Exception)
        {
            Toast.makeText(this, "issue", Toast.LENGTH_SHORT).show()
        }
        binding.updateTeacherName.setText(name)
        binding.updateTeacherEmail.setText(email)
        binding.updateTeacherPost.setText(post)
binding.avtarProfile.setOnClickListener {
        // openGallery()
        ImagePicker.with(this)
            .crop()
            .compress(1024)
            .maxResultSize(1080, 1080)
            .start()
}
        binding.updateBtn.setOnClickListener {
            if(bitmap==null &&(binding.updateTeacherName.text.toString().equals("") || binding.updateTeacherEmail.text.toString().equals("")
                        || binding.updateTeacherPost.text.toString().equals("") || category=="Select Category"))
            {
                Toast.makeText(this, "Enter Required field", Toast.LENGTH_SHORT).show()
            }
            else {
                checkValidation()
            }
        }
        binding.deleteBtn.setOnClickListener {
             if(bitmap==null &&(binding.updateTeacherName.text.toString().equals("") || binding.updateTeacherEmail.text.toString().equals("")
                    || binding.updateTeacherPost.text.toString().equals("") || category=="Select Category"))
        {
            Toast.makeText(this, "Enter Required field", Toast.LENGTH_SHORT).show()
        }
            else deleteData()
        }
    }

    private fun deleteData() {
        dbreference.child(category).child(uniquekey).removeValue()
            .addOnSuccessListener {
                deletedOldPic()
                Toast.makeText(this, "Deleted successfully", Toast.LENGTH_SHORT).show()

                val intent = Intent(this,UpdateFaculty::class.java)
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)

            }.addOnFailureListener {
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show()
            }
    }

    private fun checkValidation() {
       if(binding.updateTeacherName.text.toString().isEmpty() || binding.updateTeacherName.text.toString().equals(""))
        {
            Toast.makeText(this, "Enter Name", Toast.LENGTH_SHORT).show()
            binding.updateTeacherName.setError("Empty!")
            binding.updateTeacherName.requestFocus()
          //  Toast.makeText(this, "${image}", Toast.LENGTH_SHORT).show()
        }
        else if (binding.updateTeacherEmail.text.toString().isEmpty() || binding.updateTeacherEmail.text.toString().equals(""))
        {
            Toast.makeText(this, "Enter Email", Toast.LENGTH_SHORT).show()
            binding.updateTeacherEmail.setError("Empty!")
            binding.updateTeacherEmail.requestFocus()
        }
        else if(binding.updateTeacherPost.text.toString().isEmpty() || binding.updateTeacherPost.text.toString().equals(""))
       {
           Toast.makeText(this, "Enter Post", Toast.LENGTH_SHORT).show()
           binding.updateTeacherPost.setError("Empty!")
           binding.updateTeacherPost.requestFocus()
       }
        else if(bitmap==null)
       {
           progressDialog.setMessage("Uploading...")
           progressDialog.show()
           uploadDataToDatabase(image)
       }
       else {
           uploadData()

       }
    }

    private fun deletedOldPic() {
        val filePath: StorageReference = storageReference.child("Faculty").child(fname)
        filePath.delete().addOnSuccessListener {
            Toast.makeText(this, "Deleted from storage", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(this, "something glt hai", Toast.LENGTH_SHORT).show()
        }
    }


    private fun uploadData() {
            //progressDialog.setMessage("Uploading...")
            //progressDialog.show()

            val outputStream = ByteArrayOutputStream()
            bitmap?.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            val compressImage = outputStream.toByteArray()

            // Generate a unique filename, for example, using a timestamp
            val filePath: StorageReference = storageReference.child("Faculty").child(fname)
            val uploadTask: UploadTask = filePath.putBytes(compressImage)
            uploadTask.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    uploadTask.addOnSuccessListener {
                        filePath.downloadUrl.addOnSuccessListener {
                            downloadUrl = it.toString()
                            uploadDataToDatabase(downloadUrl)
                        }
                    }
                }else{
                    Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show()
                    progressDialog.dismiss()
                }
            }

        }

    private fun uploadDataToDatabase(downloadUrl: String) {
        val hashMap = HashMap<String, Any>()
        hashMap.put("name",binding.updateTeacherName.text.toString())
        hashMap.put("email",binding.updateTeacherEmail.text.toString())
        hashMap.put("post",binding.updateTeacherPost.text.toString())
        hashMap.put("image",downloadUrl)
        hashMap.put("fname",fname)
        if (uniquekey != null && category!=null) {
            dbreference.child(category).child(uniquekey).updateChildren(hashMap).addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "updated SucessFully", Toast.LENGTH_SHORT).show()
                val intent = Intent(this,UpdateFaculty::class.java)
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
            }.addOnFailureListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show()
            }
        }
    }

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
                binding.avtarProfile.setImageBitmap(bitmap)
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
    override fun onPause(){
        super.onPause()
        val sharedPreferences = getSharedPreferences("MyData", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("keyName", category)
        editor.apply()
    }
}