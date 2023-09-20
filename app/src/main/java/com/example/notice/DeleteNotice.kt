package com.example.notice

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.adminapp.R
import com.google.firebase.database.*

class DeleteNotice : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var list: ArrayList<NoticeData>
    private lateinit var adapter: NoticeAdapter

    private lateinit var dbref : DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_delete_notice)
        recyclerView = findViewById(R.id.deleteNoticeRV)
        progressBar = findViewById(R.id.progressBar)
        dbref = FirebaseDatabase.getInstance().getReference().child("Notice")
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.hasFixedSize()
        getNotice()
    }

    private fun getNotice() {
        dbref.addValueEventListener(
            object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    list = ArrayList()
                    for(it in snapshot.children)
                    {
                        val data: NoticeData? = it.getValue(NoticeData::class.java)
                        if (data != null) {
                            list.add(data)
                        }
                    }
                   adapter = NoticeAdapter(list, this@DeleteNotice)
                   progressBar.visibility = View.GONE
                   recyclerView.adapter = adapter
                }

                override fun onCancelled(error: DatabaseError) {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this@DeleteNotice, error.message, Toast.LENGTH_SHORT).show()
                }

            }
        )
    }
}