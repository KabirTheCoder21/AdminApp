package com.example.faculty

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.adminapp.R
import com.squareup.picasso.Picasso

class TeacherAdapter(
    private val list: List<FacultyData>,
    private val context: Context,
    private val cat: String
) : RecyclerView.Adapter<TeacherAdapter.TeacherViewHolder>() {

    class TeacherViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        val fName : TextView = itemView.findViewById(R.id.teacherNameTV)
        val fPost : TextView = itemView.findViewById(R.id.teacherPostTV)
        val fEmail : TextView = itemView.findViewById(R.id.teacherEmailTV)
        val updateBtn : Button = itemView.findViewById(R.id.updateInfoBtn)
        val fImage : ImageView = itemView.findViewById(R.id.teacherImage)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TeacherViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.faculty_item_layout,parent,false)
        return TeacherViewHolder(view)
    }

    override fun onBindViewHolder(holder: TeacherViewHolder, position: Int) {
       val items = list[position]
        holder.fName.text = items.name
        holder.fPost.text = items.post
        holder.fEmail.text = items.email
        try {
            Picasso.get().load(items.image)
                .placeholder(R.drawable.avtar_profile)
                .into(holder.fImage)
        }catch (_:Exception)
        {
            Toast.makeText(context, "issue", Toast.LENGTH_SHORT).show()
        }
        holder.updateBtn.setOnClickListener {
            Log.d("image", "onBindViewHolder: $items.image ")
            val intent = Intent(context,UpdateTeacher::class.java)
            intent.putExtra("name",holder.fName.text.toString())
            intent.putExtra("email",holder.fEmail.text.toString())
            intent.putExtra("post",holder.fPost.text.toString())
            intent.putExtra("image",items.image)
            intent.putExtra("key",items.key)
            intent.putExtra("category",cat)
            intent.putExtra("fname",items.fname)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }
}