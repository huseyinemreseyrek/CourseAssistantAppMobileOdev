package com.huseyinemreseyrek.courseassistantapp

import android.graphics.Bitmap
import java.io.Serializable

class Person(var name: String, var surName: String, val email : String, val accountType: String, val studentID: String) :
    Serializable { //Person classi instructor veya studentler icin, instructor ise studentID 0 olarak girilcek ve gosterilmicek.

    private var educationalInfo : String = ""
    private var phoneNumber : String = ""
    private var instagramAdress : String = ""
    private var twitterAdress : String = ""
    private var profilePicture: ByteArray? = null //profil fotografini byte array formunda kaydediyoruz.

    fun getEducationalInfo(): String {
        return educationalInfo
    }

    fun setEducationalInfo(info: String) {
        educationalInfo = info
    }

    fun getPhoneNumber(): String {
        return phoneNumber
    }

    fun setPhoneNumber(number: String) {
        phoneNumber = number
    }

    fun getInstagramAdress(): String {
        return instagramAdress
    }

    fun setInstagramAdress(adress: String) {
        instagramAdress = adress
    }

    fun getTwitterAdress(): String {
        return twitterAdress
    }

    fun setTwitterAdress(adress: String) {
        twitterAdress = adress
    }
    fun getProfilePicture(): ByteArray? {
        return profilePicture
    }

    fun setProfilePicture(bytearray: ByteArray) {
        profilePicture = bytearray
    }
    constructor(
        name: String,
        surName: String,
        email: String,
        accountType: String,
        studentID: String,
        educationalInfo: String,
        phoneNumber: String,

    ) : this(name, surName, email, accountType, studentID) {
        this.educationalInfo = educationalInfo
        this.phoneNumber = phoneNumber


    }

}

object Admin {
    var admin: Person = Person("admin","admin","admin","admin","0","admin","admin123456")
}
