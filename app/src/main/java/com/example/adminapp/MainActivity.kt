package com.example.adminapp

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import androidx.databinding.DataBindingUtil
import com.example.adminapp.databinding.ActivityMainBinding
import com.example.faculty.UpdateFaculty
import com.example.notice.DeleteNotice
import com.example.notice.UploadNotice

class MainActivity : AppCompatActivity() {
 private lateinit var binding : ActivityMainBinding
 private val INTERNET_PERMISSION_REQUEST_CODE = 101
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_main)

        if (!isInternetAvailable()) {
            showInternetDialog()
        }
        binding.addNotice.setOnClickListener {
            val intent = Intent(this, UploadNotice::class.java)
            startActivity(intent)
        }
        binding.addGalleryImage.setOnClickListener {
            val intent = Intent(this, UploadImage::class.java)
            startActivity(intent)
        }
        binding.addEbook.setOnClickListener {
            val intent = Intent(this,UploadPdfActivity::class.java)
            startActivity(intent)
        }
        binding.addFaculty.setOnClickListener {
            val intent = Intent(this,UpdateFaculty::class.java)
            startActivity(intent)
        }
        binding.delete.setOnClickListener {
            val intent = Intent(this,DeleteNotice::class.java)
            startActivity(intent)
        }
    }




    private fun isInternetAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
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
}