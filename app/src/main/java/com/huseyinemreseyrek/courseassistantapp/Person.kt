package com.huseyinemreseyrek.courseassistantapp


sealed class Person(
    open val accountType : String,
    open var name: String,
    open var surName: String,
    open val email: String,
    open var downloadUrl: String,
    open var educationalInfo: String,
    open var phoneNumber: String,
    open var instagramAdress: String,
    open var twitterAdress: String,
    open var courses: MutableList<Map<String, Any>>
)

data class Student(
    override val accountType: String,
    val studentID: String,
    override var name: String,
    override var surName: String,
    override val email: String,
    override var downloadUrl: String,
    override var educationalInfo: String,
    override var phoneNumber: String,
    override var instagramAdress: String,
    override var twitterAdress: String,
    override var courses: MutableList<Map<String, Any>>
) : Person(accountType, name, surName, email, downloadUrl, educationalInfo, phoneNumber, instagramAdress, twitterAdress, courses)

data class Instructor(
    override val accountType: String,
    override var name: String,
    override var surName: String,
    override val email: String,
    override var downloadUrl: String,
    override var educationalInfo: String,
    override var phoneNumber: String,
    override var instagramAdress: String,
    override var twitterAdress: String,
    override var courses: MutableList<Map<String, Any>>
) : Person(accountType, name, surName, email, downloadUrl, educationalInfo, phoneNumber, instagramAdress, twitterAdress , courses)


object Admin {
    var admin: Instructor = Instructor("admin","admin","admin","admin","","",""
        ,"","" , mutableListOf()
    )
}



