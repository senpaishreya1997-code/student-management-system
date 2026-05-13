package com.university.model;

public class Student extends Person {
    private int studentId;
    private String enrollmentNo, program;
    private int yearOfAdmission, currentYear;

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }
    public String getEnrollmentNo() { return enrollmentNo; }
    public void setEnrollmentNo(String enrollmentNo) { this.enrollmentNo = enrollmentNo; }
    public String getProgram() { return program; }
    public void setProgram(String program) { this.program = program; }
    public int getYearOfAdmission() { return yearOfAdmission; }
    public void setYearOfAdmission(int yearOfAdmission) { this.yearOfAdmission = yearOfAdmission; }
    public int getCurrentYear() { return currentYear; }
    public void setCurrentYear(int currentYear) { this.currentYear = currentYear; }
}
