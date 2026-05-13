package com.university.model;

public class Person {
    private int personId;
    private String fname, mname, lname, email, phone, role, gender;

    public int getPersonId() { return personId; }
    public void setPersonId(int personId) { this.personId = personId; }
    public String getFname() { return fname; }
    public void setFname(String fname) { this.fname = fname; }
    public String getMname() { return mname; }
    public void setMname(String mname) { this.mname = mname; }
    public String getLname() { return lname; }
    public void setLname(String lname) { this.lname = lname; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getFullName() {
        return fname + (mname != null && !mname.isEmpty() ? " " + mname : "") + " " + lname;
    }
}
