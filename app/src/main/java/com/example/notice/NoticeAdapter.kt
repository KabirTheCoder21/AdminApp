package com.example.notice

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.adminapp.R
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso

class NoticeAdapter(
    private val list: List<NoticeData>,
    private val context: Context,
    private val recyclerView: RecyclerView,) : RecyclerView.Adapter<NoticeAdapter.NoteiceViewHolder>() {
    //private var layoutManagerState: Parcelable? = null

    class NoteiceViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        val deleteBtn = itemView.findViewById<Button>(R.id.deleteBtn)
        val title = itemView.findViewById<TextView>(R.id.deleteNoticeTitle)
        val image = itemView.findViewById<ImageView>(R.id.deleteNoticeImage)
        val date = itemView.findViewById<TextView>(R.id.date)
        val time = itemView.findViewById<TextView>(R.id.time)
        val progressBar = itemView.findViewById<ProgressBar>(R.id.progressBar)
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
       // val pos = holder.absoluteAdapterPosition
        try {
         //   Picasso.get().load(data.image).into(holder.image)

            Picasso.get()
                .load(data.image)
                .into(holder.image, object : Callback {
                    override fun onSuccess() {
                        // Image loaded successfully, hide the progress dialog
                        holder.progressBar.visibility = View.GONE
                    }

                    override fun onError(e: Exception?) {
                        // Handle error (e.g., show an error message)
                        if (e != null) {
                            e.printStackTrace()
                        }
                        holder.progressBar.visibility = View.GONE
                    }
                })
        } catch (e: Exception) {
            e.printStackTrace()
            holder.progressBar.visibility = View.GONE
        }
        val layoutManager = recyclerView.layoutManager as LinearLayoutManager
        holder.deleteBtn.setOnClickListener {
         //   layoutManagerState = recyclerView.layoutManager?.onSaveInstanceState()
            val builder = AlertDialog.Builder(context)
            builder.setMessage("Are you sure want to delete this Notice ?")
            builder.setCancelable(true)
                .setPositiveButton("OK") { dialog: DialogInterface, _: Int ->
                    // Handling the "OK" button click event
                    //notifyItemRangeChanged(position, list.size - position)
                    // Notify the adapter about the removal
                    val firstVisibleItemPositionAfterDeletion = layoutManager.findFirstVisibleItemPosition()
                    val firstVisibleItemAfterDeletion = layoutManager.findViewByPosition(firstVisibleItemPositionAfterDeletion)
                    val offsetAfterDeletion = firstVisibleItemAfterDeletion?.top ?: 0

                    notifyItemRemoved(holder.bindingAdapterPosition)
                    notifyItemRangeChanged(holder.bindingAdapterPosition, list.size - holder.bindingAdapterPosition)

                    recyclerView.post {
                        layoutManager.scrollToPositionWithOffset(firstVisibleItemPositionAfterDeletion, offsetAfterDeletion)
                    }

                    deleteNotice(data,holder.bindingAdapterPosition)
                }
                .setNegativeButton("Cancel") { dialog: DialogInterface, _: Int ->
                    // Handling the "Cancel" button click event
                    dialog.cancel()
                }
            // Initialize alert as nullable
            var alert: AlertDialog? = null
            try {
                alert = builder.create()
            } catch (e: Exception) {
                e.printStackTrace()
            }
           alert!!.show()
           /* val dbref : DatabaseReference = FirebaseDatabase.getInstance().getReference().child("Notice")
            data.key?.let { it1 -> dbref.child(it1).removeValue().addOnCompleteListener {
                Toast.makeText(context, "Deleted Successfully", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                Toast.makeText(context, "Something Went Wrong", Toast.LENGTH_SHORT).show()
            }}
            notifyItemRangeChanged(position, list.size)*/
        }
    }

    private fun deleteNotice(data: NoticeData, position: Int) {
        val dbref : DatabaseReference = FirebaseDatabase.getInstance().getReference().child("Notice")
        data.key?.let { it1 -> dbref.child(it1).removeValue().addOnCompleteListener {
            Toast.makeText(context, "Deleted Successfully", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(context, "Something Went Wrong", Toast.LENGTH_SHORT).show()
        }}
    }

}