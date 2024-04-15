package com.huseyinemreseyrek.courseassistantapp

import android.content.Context
import com.google.gson.Gson

object SharedPreferencesClass { //Person objelerini kaydetmemize yardimci olacak class, sharedpreferences ile kaydediyoruz.

    fun saveObject(context: Context, email : String, person : Person) {
        val sharedPreferences = context.getSharedPreferences("users", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val json = gson.toJson(person)
        editor.putString(email, json)
        editor.apply()
    }

    fun getObject(context: Context, email: String): Person {
        val sharedPreferences = context.getSharedPreferences("users", Context.MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPreferences.getString(email, null)
        return gson.fromJson(json, Person::class.java)


    }

}

