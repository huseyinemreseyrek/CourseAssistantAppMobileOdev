package com.huseyinemreseyrek.courseassistantapp

class Post {

    private var date : String = ""
    private var email : String = ""
    private var commentNumber : String = ""
    private var mainText : String = ""
    private var nameSurname : String = ""

    constructor()

    constructor(date : String, email : String, commentNumber : String, mainText : String, nameSurname : String){
        this.date = date
        this.email = email
        this.commentNumber = commentNumber
        this.mainText = mainText
        this.nameSurname = nameSurname
    }
    fun getDate() : String{
        return this.date
    }
    fun getEmail() : String{
        return this.email
    }
    fun getCommentNumber() : String{
        return this.commentNumber
    }
    fun getMainText() : String{
        return this.mainText
        }
    fun getNameSurname() : String{
        return this.nameSurname
    }

    fun setDate(date : String){
        this.date = date
    }
    fun setEmail(email : String){
        this.email = email
    }
    fun setCommentNumber(commentNumber : String){
        this.commentNumber = commentNumber
        }
    fun setMainText(mainText : String){
        this.mainText = mainText
    }
    fun setNameSurname(nameSurname : String){
        this.nameSurname = nameSurname
    }



}