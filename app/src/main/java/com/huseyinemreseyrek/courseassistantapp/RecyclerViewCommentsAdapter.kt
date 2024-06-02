package com.huseyinemreseyrek.courseassistantapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.huseyinemreseyrek.courseassistantapp.databinding.LayoutCommentListItemBinding
import com.huseyinemreseyrek.courseassistantapp.databinding.LayoutPostListItemBinding

class RecyclerViewCommentsAdapter(private val comments: List<CommentDataClass>) : RecyclerView.Adapter<RecyclerViewCommentsAdapter.CommentsViewHolder>() {
    class CommentsViewHolder(val binding : LayoutCommentListItemBinding) : RecyclerView.ViewHolder(binding.root){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentsViewHolder {
        val binding = LayoutCommentListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CommentsViewHolder(binding)
    }



    override fun onBindViewHolder(holder: CommentsViewHolder, position: Int) {
        val comment = comments[position]
        holder.binding.userName.text = comment.getnameSurname()
        holder.binding.dateEditText.text = comment.getDate()
        holder.binding.userComment.text = comment.getcomment()
    }

    override fun getItemCount(): Int {
        return comments.size
    }

}