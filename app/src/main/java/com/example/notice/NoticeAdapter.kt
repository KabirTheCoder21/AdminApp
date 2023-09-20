package com.example.notice

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.adminapp.R
import com.example.faculty.FacultyData
import com.example.faculty.TeacherAdapter
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import org.w3c.dom.Text

class NoticeAdapter(private val list: List<NoticeData>,
                    private val context: Context,) : RecyclerView.Adapter<NoticeAdapter.NoteiceViewHolder>() {
    class NoteiceViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        val deleteBtn = itemView.findViewById<Button>(R.id.deleteBtn)
        val title = itemView.findViewById<TextView>(R.id.deleteNoticeTitle)
        val image = itemView.findViewById<ImageView>(R.id.deleteNoticeImage)
        val date = itemView.findViewById<TextView>(R.id.date)
        val time = itemView.findViewById<TextView>(R.id.time)
    }
    override fun getItemId(position: Int): Long {
        // Return a unique ID for the item at the given position
        return list[position].key.hashCode().toLong()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteiceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.newsfeed_item,parent,false)
        return NoteiceViewHolder(view)
    }

    override fun getItemCount(): Int {
      return list.size
    }

    override fun onBindViewHolder(holder: NoteiceViewHolder, position: Int) {
        val data : NoticeData = list[position]
        holder.title.text = data.title
        holder.date.text = data.date
        holder.time.text = data.time
        try {
            Picasso.get().load(data.image).into(holder.image)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        holder.deleteBtn.setOnClickListener {
            val dbref : DatabaseReference = FirebaseDatabase.getInstance().getReference().child("Notice")
            data.key?.let { it1 -> dbref.child(it1).removeValue().addOnCompleteListener {
                Toast.makeText(context, "Deleted Successfully", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                Toast.makeText(context, "Something Went Wrong", Toast.LENGTH_SHORT).show()
            }}
            notifyItemRangeChanged(position, list.size)
        }
    }
}