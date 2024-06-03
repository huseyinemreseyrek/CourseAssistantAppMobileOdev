package com.huseyinemreseyrek.courseassistantapp;

public class Poll {

    String name;
    String courseID;
    String status;




    public Poll(String name, String courseID, String status) {
        this.name = name;
        this.courseID = courseID;
        this.status = status;

    }

    public String getName() {
        return name;
    }

    public String getCourseID() {
        return courseID;
    }

    public String getStatus() {
        return status;
    }

    public void setName(String name) {
        this.name = name;
    }


    public void setCourseID(String courseID) {
        this.courseID = courseID;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
