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
import com.smarteist.autoimageslider.SliderView

class MainActivity : AppCompatActivity() {
 private lateinit var binding : ActivityMainBinding
 private val INTERNET_PERMISSION_REQUEST_CODE = 101
    var url1 =
        "https://images.indianexpress.com/2022/07/lucknow-university-1200.jpg?w=640"
    var url2 =
        "https://static.toiimg.com/thumb/msid-103522839,imgsize-91318,width-400,resizemode-4/103522839.jpg"
    var url3 =
        "https://images.collegedunia.com/public/college_data/images/campusimage/1412852400lu4.jpg"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_main)

        if (!isInternetAvailable()) {
            showInternetDialog()
        }

        var sliderView = binding.slider
        // on below line creating variable for array list.
        val sliderDataArrayList: ArrayList<String> = ArrayList()
        // on below line adding urls in slider list.
        sliderDataArrayList.add(url1)
        sliderDataArrayList.add(url2)
        sliderDataArrayList.add(url3)
        // on below line initializing our adapter class by passing our list to it.
        val adapter = SliderAdapter(sliderDataArrayList)
        // on below line setting auto cycle direction for slider view from left to right.
        sliderView.setAutoCycleDirection(SliderView.LAYOUT_DIRECTION_LTR);
        // on below line setting adapter for slider view.
        sliderView.setSliderAdapter(adapter);
        // on below line setting scroll time for slider view
        sliderView.setScrollTimeInSec(3);
        // on below line setting auto cycle for slider view.
        sliderView.setAutoCycle(true);
        // on below line setting start cycle for slider view.
        sliderView.startAutoCycle();


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