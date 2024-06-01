package com.huseyinemreseyrek.courseassistantapp

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.huseyinemreseyrek.courseassistantapp.databinding.LayoutCoursesListItemBinding

class RecyclerViewCoursesAdapter(private val coursesList : ArrayList<CourseNames>, private val userEmail: String) : RecyclerView.Adapter<RecyclerViewCoursesAdapter.CoursesHolder>(){

    class CoursesHolder(val binding : LayoutCoursesListItemBinding) : RecyclerView.ViewHolder(binding.root){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CoursesHolder {
        val binding = LayoutCoursesListItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return CoursesHolder(binding)
    }

    override fun getItemCount(): Int {
        return coursesList.size
    }

    override fun onBindViewHolder(holder: CoursesHolder, position: Int) {
        val courseIDText = "Course ID: " + coursesList[position].courseID
        holder.binding.courseID.text = courseIDText
        holder.binding.courseName.text = coursesList[position].courseName
        val mainInstructorText = "Main Instructor: " + coursesList[position].mainInstructor
        holder.binding.mainInstructor.text = mainInstructorText
        holder.binding.status.text = coursesList[position].status

        holder.binding.cardView.setOnClickListener{

            val intent = Intent(holder.itemView.context, CourseInformations::class.java)
            intent.putExtra("courseID", coursesList[position].courseID)
            intent.putExtra("userEmail", userEmail)
            holder.itemView.context.startActivity(intent)
            Toast.makeText(holder.itemView.context,coursesList[position].courseName,Toast.LENGTH_SHORT).show()

        }

    }


}