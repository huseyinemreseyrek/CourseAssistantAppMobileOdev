package com.huseyinemreseyrek.courseassistantapp

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.huseyinemreseyrek.courseassistantapp.databinding.LayoutPostListItemBinding

class PostAdapter(private val postList: List<Post> , private val userEmail: String, private val totalGroupNumber : String, private val courseID : String, private var notificationState : Boolean) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {
    class PostViewHolder(val binding : LayoutPostListItemBinding) : RecyclerView.ViewHolder(binding.root){

    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostAdapter.PostViewHolder {
        val binding = LayoutPostListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding)
    }
    fun updateNotificationStatus(newNotificationStatus: Boolean) {
        notificationState = newNotificationStatus
        notifyDataSetChanged()
    }
    override fun onBindViewHolder(holder: PostAdapter.PostViewHolder, position: Int) {
        val post = postList[position]

        holder.binding.datePost.text = post.getDate()
        holder.binding.userName.text = post.getNameSurname()
        holder.binding.textMainPost.text = post.getMainText()
        holder.binding.userEmail.text = post.getEmail()
        val commentNumber = post.getCommentNumber()
        if(commentNumber == "0"){
            holder.binding.comments.visibility = View.GONE
        }
        else{
            holder.binding.comments.visibility = View.VISIBLE
            val commentText = "View all $commentNumber comments"
            holder.binding.comments.text = commentText
        }
        holder.binding.commentPost.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, CommentsActivity::class.java).apply {
                putExtra("courseID", courseID)
                putExtra("postId", post.getDocumentId())
                putExtra( "userEmail", userEmail)
                putExtra("postDate",post.getDate())
                putExtra("postNameSurname",post.getNameSurname())
                putExtra("postMainText",post.getMainText())
                putExtra("postEmail",post.getEmail())
                putExtra("postCommentNumber",post.getCommentNumber())
                putExtra("notificationState",notificationState)
                putExtra("totalGroupNumber",totalGroupNumber)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return postList.size
    }

}