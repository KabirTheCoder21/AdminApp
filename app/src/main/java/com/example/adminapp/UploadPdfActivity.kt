package com.example.adminapp

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.ContentResolver
import android.content.ContentValues.TAG
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.OpenableColumns
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.example.adminapp.databinding.ActivityUploadNoticeBinding
import com.example.adminapp.databinding.ActivityUploadPdfBinding
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File
import java.io.InputStream
import kotlin.math.log

class UploadPdfActivity : AppCompatActivity() {
    private lateinit var binding : ActivityUploadPdfBinding
    private  var pdfData: Uri?=null
    private lateinit var databaseReference: DatabaseReference
    private lateinit var storageReference: StorageReference
    private  var downloadUrl : String = ""
    private lateinit var pdfName : String
    private lateinit var progressDialog : ProgressDialog
    companion object{
        private val PICK_PDF_REQUEST = 1 // Any unique request code
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = resources.getColor(R.color.statusBarColor_3)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_upload_pdf)
        databaseReference = FirebaseDatabase.getInstance().getReference()
        storageReference = FirebaseStorage.getInstance().getReference()

        progressDialog = ProgressDialog(this)
        binding.selectPdf.setOnClickListener {
            pickImageGallery()
        }
        if (supportActionBar != null) {
            supportActionBar!!.hide()
        }
        binding.pdfReview.setOnClickListener {
            val pdfUri = Uri.parse(pdfData.toString())
            binding.pdfView.visibility = View.VISIBLE
            binding.pdfView.fromUri(pdfUri)
                .onLoad(object : OnLoadCompleteListener {
                    override fun loadComplete(nbPages: Int) {
                        // PDF has been loaded, you can perform actions here
                        Toast.makeText(applicationContext, "okkkkkkk", Toast.LENGTH_SHORT).show()
                    }
                })
                .load()
        }
        binding.uploadPdfButton.setOnClickListener {
            if(binding.pdfTitle.text.toString().isEmpty())
            {
                binding.pdfTitle.setError("Empty")
                binding.pdfTitle.requestFocus()
            }else if(pdfData==null)
            {
                Toast.makeText(this, "Please Upload Pdf", Toast.LENGTH_SHORT).show()
            }
            else if (!isInternetAvailable()) {
                showInternetDialog()
            }
            else uploadPdf()
        }

    }

    private fun showInternetDialog() {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setTitle("No Internet Connection")
        dialogBuilder.setMessage("Please enable internet connectivity to use this app.")
        dialogBuilder.setPositiveButton("Settings") { dialog: DialogInterface, _: Int ->
            val settingsIntent = Intent(Settings.ACTION_DATA_ROAMING_SETTINGS)
            startActivity(settingsIntent)
            dialog.dismiss()

        }
        dialogBuilder.setNegativeButton("Exit") { dialog: DialogInterface, _: Int ->
            finish()
        }
        val alertDialog = dialogBuilder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()

    }

    private fun isInternetAvailable():Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected

    }

    private fun uploadPdf() {
        progressDialog.setTitle("Please wait !")
        progressDialog.setMessage("Uploading Pdf")
        progressDialog.show()
        storageReference = storageReference.child("pdf/"+pdfName+" : "+System.currentTimeMillis()+".pdf")
        pdfData?.let {
            storageReference.putFile(it).addOnSuccessListener {
                val uriTask = it.storage.downloadUrl
                while (!uriTask.isComplete){if(uriTask.isComplete) break}
                val uri = uriTask.getResult()
                uploadData(uri.toString())
            }.addOnFailureListener{
                progressDialog.dismiss()
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uploadData(downloadUrl: String) {
        val uniqueKey : String? = databaseReference.child("pdf").push().key
        val data = mutableMapOf<String, String>()
        data.put("pdfTitle",binding.pdfTitle.text.toString())
        data.put("pdfUrl",downloadUrl)
        if (uniqueKey != null) {
            databaseReference.child("pdf").child(uniqueKey).setValue(data).addOnCompleteListener{
               progressDialog.dismiss()
                Toast.makeText(this, "pdf uploaded successfully", Toast.LENGTH_SHORT).show()
                binding.pdfTv.text=""
                binding.pdfTitle.text?.clear()
                binding.pdfView.visibility = View.GONE
//                binding.pdfView.finis
            }.addOnFailureListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Failed to upload pdf", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun pickImageGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "application/pdf"
        startActivityForResult(Intent.createChooser(intent, "Select PDF File"), PICK_PDF_REQUEST)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode== PICK_PDF_REQUEST && resultCode== RESULT_OK)
        {
            if (data != null) {
                pdfData = data.data!!
               if(pdfData.toString().startsWith("content://"))
               {
                   try {
                       val cursor = contentResolver.query(
                           pdfData!!,
                          null,
                           null,
                           null,
                           null
                       )
                       if(cursor!=null && cursor.moveToFirst())
                       {
                           val pdfNameColumnIndex = cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME)
                           pdfName = cursor.getString(pdfNameColumnIndex)
                           cursor.close()
                       }
                   } catch (e: Exception) {
                       e.printStackTrace()
                   }
               }
                else if(pdfData.toString().startsWith("file://"))
               {
                    pdfName = File(pdfData.toString()).name
               }
               binding.pdfTv.text = pdfName
            }
        }
    }
}