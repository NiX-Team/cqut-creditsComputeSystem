package com.nix.model;

import java.util.List;

public class StudentModel {
    private Long id;
    private String studentId;
    private String name;
    private UniversityModel university;
    private List<CoursesModel> courses;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UniversityModel getUniversity() {
        return university;
    }

    public void setUniversity(UniversityModel university) {
        this.university = university;
    }

    public List<CoursesModel> getCourses() {
        return courses;
    }

    public void setCourses(List<CoursesModel> courses) {
        this.courses = courses;
    }
}
