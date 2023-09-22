package com.example.faculty

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.adminapp.R
import com.example.adminapp.databinding.ActivityUpdateFacultyBinding
import com.google.firebase.database.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

 class UpdateFaculty : AppCompatActivity() {
    private lateinit var binding : ActivityUpdateFacultyBinding

    private lateinit var csDepartment : RecyclerView
    private lateinit var itDepartment : RecyclerView
    private lateinit var meDepartment : RecyclerView
    private lateinit var eeDepartment : RecyclerView
    private lateinit var eceDepartment :RecyclerView

    private lateinit var csNoData : LinearLayout
    private lateinit var itNoData : LinearLayout
    private lateinit var meNoData : LinearLayout
    private lateinit var eeNoData : LinearLayout
    private lateinit var eceNoData : LinearLayout

    private lateinit var listCs : ArrayList<FacultyData>
    private lateinit var listIt : ArrayList<FacultyData>
    private lateinit var listMe : ArrayList<FacultyData>
    private lateinit var listEe : ArrayList<FacultyData>
    private lateinit var listEce : ArrayList<FacultyData>

    private lateinit var reference: DatabaseReference
    private lateinit var dbref: DatabaseReference

    private lateinit var adapter: TeacherAdapter

     private var isexpandedCs = false
     private var isexpandedIt = false
     private var isexpandedMe = false
     private var isexpandedEe = false
     private var isexpandedEce = false

     // private lateinit var animation : Animation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = resources.getColor(R.color.statusBarColor_4)

        binding = DataBindingUtil.setContentView(this,R.layout.activity_update_faculty)

        csDepartment = findViewById(R.id.csDepartment)
        itDepartment = findViewById(R.id.itDepartment)
        meDepartment = findViewById(R.id.meDepartment)
        eeDepartment = findViewById(R.id.eeDepartment)
        eceDepartment = findViewById(R.id.eceDepartment)

        csNoData = findViewById(R.id.csNoData)
        itNoData = findViewById(R.id.itNoData)
        meNoData = findViewById(R.id.meNoData)
        eeNoData = findViewById(R.id.eeNoData)
        eceNoData = findViewById(R.id.eceNoData)

        reference = FirebaseDatabase.getInstance().getReference().child("Faculty")

        if (supportActionBar != null) {
            supportActionBar!!.hide()
        }

        binding.fab.setOnClickListener {
            val intent = Intent(this,AddTeacher::class.java)
            startActivity(intent)
        }
        val csTv = findViewById<TextView>(R.id.csTv)

        csTv.setOnClickListener {
            if (isexpandedCs) {
                // Hide the RecyclerView
                csDepartment.visibility = View.GONE
                csNoData.visibility = View.GONE
            } else {
                //animation = AnimationUtils.loadAnimation(this,R.anim.fade_in)
                csDepartmentData()
            }
            isexpandedCs = !isexpandedCs // Toggle the flag
        }
        binding.itTv.setOnClickListener {
            if (isexpandedIt) {
                itDepartment.visibility = View.GONE
                itNoData.visibility = View.GONE
            }else{
                //animation = AnimationUtils.loadAnimation(this,R.anim.fade_in)
                itDepartmentData()
            }
            isexpandedIt = !isexpandedIt
        }
        binding.meTv.setOnClickListener {
            if(isexpandedMe)
            {
                meDepartment.visibility = View.GONE
                meNoData.visibility = View.GONE
            }
            else{
                //animation = AnimationUtils.loadAnimation(this,R.anim.fade_in)
                meDepartmentData()
            }
            isexpandedMe = !isexpandedMe
        }

        binding.eeTv.setOnClickListener {
            if(isexpandedEe)
            {
                eeDepartment.visibility = View.GONE
                eeNoData.visibility = View.GONE
            }else{
                //animation = AnimationUtils.loadAnimation(this,R.anim.fade_in)
                eeDepartmentData()
            }
            isexpandedEe=!isexpandedEe
        }
        binding.eceTv.setOnClickListener {
            if(isexpandedEce)
            {
                eceDepartment.visibility = View.GONE
                eceNoData.visibility = View.GONE
            }else{
                //animation = AnimationUtils.loadAnimation(this,R.anim.fade_in)
                eceDepartmentData()
            }
            isexpandedEce=!isexpandedEce
        }

       }

    private fun csDepartmentData() {
        dbref = reference.child("Computer Science")
        listCs = ArrayList()
       CoroutineScope(Dispatchers.IO).launch {
            try {
                val snapshot = dbref.get().await() // Fetch data in the background thread
                withContext(Dispatchers.Main) {
                    if (!snapshot.exists()) {
                        csNoData.visibility = View.VISIBLE
                        csDepartment.visibility = View.GONE
                    } else {
                        csNoData.visibility = View.GONE
                        csDepartment.visibility = View.VISIBLE
                        for (it in snapshot.children) {
                            val facultyData: FacultyData? = it.getValue(FacultyData::class.java)
                            if (facultyData != null) {
                                listCs.add(facultyData)
                            }
                        }

                        csDepartment.hasFixedSize()
                        csDepartment.layoutManager = LinearLayoutManager(this@UpdateFaculty, LinearLayoutManager.HORIZONTAL, false)
                        adapter = TeacherAdapter(listCs, this@UpdateFaculty,"Computer Science")
                        csDepartment.adapter = adapter
                        adapter.notifyDataSetChanged()
                       // csDepartment.startAnimation(animation)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    // Handle the error here, if needed
                    Toast.makeText(this@UpdateFaculty, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
       /* dbref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                listCs = ArrayList()
                if (!snapshot.exists()) {
                    csNoData.visibility = View.VISIBLE
                    csDepartment.visibility = View.GONE
                } else {
                    csNoData.visibility = View.GONE
                    csDepartment.visibility = View.VISIBLE
                    for (it in snapshot.children) {
                        val facultyData: FacultyData? = it.getValue(FacultyData::class.java)
                        if (facultyData != null) {
                            listCs.add(facultyData)
                        }
                    }

                    csDepartment.hasFixedSize()
                    csDepartment.layoutManager = LinearLayoutManager(this@UpdateFaculty, LinearLayoutManager.HORIZONTAL, false)
                    adapter = TeacherAdapter(listCs, this@UpdateFaculty,"Computer Science")
                    csDepartment.adapter = adapter
                    adapter.notifyDataSetChanged()
                    // csDepartment.startAnimation(animation)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })*/
    }
    private fun itDepartmentData() {
        dbref = reference.child("Information Technology")
        listIt = ArrayList()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val snapshot = dbref.get().await() // Fetch data in the background thread

                withContext(Dispatchers.Main) {
                    if (!snapshot.exists()) {
                        itNoData.visibility = View.VISIBLE
                        itDepartment.visibility = View.GONE
                    } else {
                        itNoData.visibility = View.GONE
                        itDepartment.visibility = View.VISIBLE

                        for (it in snapshot.children) {
                            val facultyData: FacultyData? = it.getValue(FacultyData::class.java)
                            if (facultyData != null) {
                                listIt.add(facultyData)
                            }
                        }

                        itDepartment.hasFixedSize()
                        itDepartment.layoutManager = LinearLayoutManager(this@UpdateFaculty, LinearLayoutManager.HORIZONTAL, false)
                        adapter = TeacherAdapter(listIt, this@UpdateFaculty, "Information Technology")
                        itDepartment.adapter = adapter
                        adapter.notifyDataSetChanged()
                       // itDepartment.startAnimation(animation)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    // Handle the error here, if needed
                    Toast.makeText(this@UpdateFaculty, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    private fun meDepartmentData() {
        dbref = reference.child("Mechanical Engineering")
        listMe = ArrayList()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val snapshot = dbref.get().await() // Fetch data in the background thread

                withContext(Dispatchers.Main) {
                    if (!snapshot.exists()) {
                        meNoData.visibility = View.VISIBLE
                        meDepartment.visibility = View.GONE
                    } else {
                        meNoData.visibility = View.GONE
                        meDepartment.visibility = View.VISIBLE

                        for (it in snapshot.children) {
                            val facultyData: FacultyData? = it.getValue(FacultyData::class.java)
                            if (facultyData != null) {
                                listMe.add(facultyData)
                            }
                        }

                        meDepartment.hasFixedSize()
                        meDepartment.layoutManager = LinearLayoutManager(this@UpdateFaculty, LinearLayoutManager.HORIZONTAL, false)
                        adapter = TeacherAdapter(listMe, this@UpdateFaculty, "Mechanical Engineering")
                        meDepartment.adapter = adapter
                        adapter.notifyDataSetChanged()
                       // meDepartment.startAnimation(animation)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    // Handle the error here, if needed
                    Toast.makeText(this@UpdateFaculty, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    private fun eeDepartmentData() {
        dbref = reference.child("Electrical Engineering")
        listEe = ArrayList()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val snapshot = dbref.get().await() // Fetch data in the background thread

                withContext(Dispatchers.Main) {
                    if (!snapshot.exists()) {
                        eeNoData.visibility = View.VISIBLE
                        eeDepartment.visibility = View.GONE
                    } else {
                        eeNoData.visibility = View.GONE
                        eeDepartment.visibility = View.VISIBLE

                        for (it in snapshot.children) {
                            val facultyData: FacultyData? = it.getValue(FacultyData::class.java)
                            if (facultyData != null) {
                                listEe.add(facultyData)
                            }
                        }

                        eeDepartment.hasFixedSize()
                        eeDepartment.layoutManager = LinearLayoutManager(this@UpdateFaculty, LinearLayoutManager.HORIZONTAL, false)
                        adapter = TeacherAdapter(listEe, this@UpdateFaculty, "Electrical Engineering")
                        eeDepartment.adapter = adapter
                        adapter.notifyDataSetChanged()
                     //   eeDepartment.startAnimation(animation)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    // Handle the error here, if needed
                    Toast.makeText(this@UpdateFaculty, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun eceDepartmentData() {
        dbref = reference.child("Electronics Engineering")
        listEce = ArrayList()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val snapshot = dbref.get().await() // Fetch data in the background thread

                withContext(Dispatchers.Main) {
                    if (!snapshot.exists()) {
                        eceNoData.visibility = View.VISIBLE
                        eceDepartment.visibility = View.GONE
                    } else {
                        eceNoData.visibility = View.GONE
                        eceDepartment.visibility = View.VISIBLE

                        for (it in snapshot.children) {
                            val facultyData: FacultyData? = it.getValue(FacultyData::class.java)
                            if (facultyData != null) {
                                listEce.add(facultyData)
                            }
                        }

                        eceDepartment.hasFixedSize()
                        eceDepartment.layoutManager = LinearLayoutManager(this@UpdateFaculty, LinearLayoutManager.HORIZONTAL, false)
                        adapter = TeacherAdapter(listEce, this@UpdateFaculty, "Electronics Engineering")
                        eceDepartment.adapter = adapter
                        adapter.notifyDataSetChanged()
                       // eceDepartment.startAnimation(animation)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    // Handle the error here, if needed
                    Toast.makeText(this@UpdateFaculty, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

     override fun onResume() {
         super.onResume()
         val sharedPreferences = getSharedPreferences("MyData", Context.MODE_PRIVATE)
         val receivedData = sharedPreferences.getString("keyName", "") // Default value can be set
         when(receivedData){
             "Computer Science" -> {
                 csDepartmentData()
             }
             "Mechanical Engineering"->{
                 meDepartmentData()
             }
             "Electrical Engineering"->{
                 eeDepartmentData()
             }
             "Electronics Engineering"->{
                 eceDepartmentData()
             }
             "Information Technology"->{
                 itDepartmentData()
             }
             else->{
                 Toast.makeText(this, "getting null", Toast.LENGTH_SHORT).show()
             }
         }
     }

     override fun onPause() {
         super.onPause()
         val sharedPreferences = getSharedPreferences("MyData", Context.MODE_PRIVATE)
         val editor = sharedPreferences.edit()
         editor.remove("keyName")
         editor.apply()
     }

 }
