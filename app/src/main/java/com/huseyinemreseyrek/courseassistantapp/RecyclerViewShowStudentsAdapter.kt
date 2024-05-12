package com.huseyinemreseyrek.courseassistantapp

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.huseyinemreseyrek.courseassistantapp.databinding.LayoutInstructorsListItemBinding
import com.huseyinemreseyrek.courseassistantapp.databinding.LayoutStudentsListItemBinding

class RecyclerViewShowStudentsAdapter (private val studentList : ArrayList<ShowStudentsDataClass>) : RecyclerView.Adapter<RecyclerViewShowStudentsAdapter.StudentsHolder>(){
    class StudentsHolder(val binding : LayoutStudentsListItemBinding) : RecyclerView.ViewHolder(binding.root){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentsHolder {
        val binding = LayoutStudentsListItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return RecyclerViewShowStudentsAdapter.StudentsHolder(binding)
    }

    override fun getItemCount(): Int {
        return studentList.size
    }

    override fun onBindViewHolder(holder: StudentsHolder, position: Int) {
        holder.binding.studentEmail.text = studentList[position].studentEmail
        holder.binding.cardView.setOnClickListener{
            Toast.makeText(holder.itemView.context,studentList[position].studentEmail, Toast.LENGTH_SHORT).show()
        }
    }


}