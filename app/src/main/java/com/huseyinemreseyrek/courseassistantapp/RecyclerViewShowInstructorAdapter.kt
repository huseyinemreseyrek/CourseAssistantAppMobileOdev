package com.huseyinemreseyrek.courseassistantapp

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.huseyinemreseyrek.courseassistantapp.databinding.LayoutCoursesListItemBinding
import com.huseyinemreseyrek.courseassistantapp.databinding.LayoutInstructorsListItemBinding

class RecyclerViewShowInstructorAdapter(private val instructorList : ArrayList<ShowInstructorDataClass>) : RecyclerView.Adapter<RecyclerViewShowInstructorAdapter.InstructorHolder>(){

    class InstructorHolder(val binding : LayoutInstructorsListItemBinding) : RecyclerView.ViewHolder(binding.root){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InstructorHolder {
        val binding = LayoutInstructorsListItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return InstructorHolder(binding)
    }

    override fun getItemCount(): Int {
        return instructorList.size
    }

    override fun onBindViewHolder(holder: InstructorHolder, position: Int) {


        holder.binding.instructorEmail.text = instructorList[position].instructorEmail
        val groupNameText = "Group No: " + instructorList[position].groupNumber
        holder.binding.groupName.text = groupNameText
        holder.binding.cardView.setOnClickListener{

            Toast.makeText(holder.itemView.context,instructorList[position].instructorEmail, Toast.LENGTH_SHORT).show()
        }

    }


}