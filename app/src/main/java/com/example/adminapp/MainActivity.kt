package com.example.adminapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.example.adminapp.databinding.ActivityMainBinding
import com.example.faculty.UpdateFaculty
import com.example.notice.DeleteNotice
import com.example.notice.UploadNotice

class MainActivity : AppCompatActivity() {
 private lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_main)
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
}