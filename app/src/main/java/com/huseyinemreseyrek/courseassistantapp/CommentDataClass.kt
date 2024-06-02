package com.huseyinemreseyrek.courseassistantapp

class CommentDataClass {
    private var date : String = ""
    private var nameSurname : String = ""
    private var comment : String = ""

    constructor()

    constructor(date : String, nameSurname : String, comment : String){
        this.date = date
        this.nameSurname = nameSurname
        this.comment = comment
    }
    fun getDate() : String{
        return date
    }
    fun setdate(date : String){
        this.date = date
    }
    fun getnameSurname() : String{
        return nameSurname
    }
    fun setnameSurname(nameSurname : String){
        this.nameSurname = nameSurname
    }
    fun getcomment() : String{
        return comment
    }
    fun setcomment(comment : String){
        this.comment = comment
        }

}