package com.example.medimanager.models;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Patient implements Serializable {
    private int id;
    private int doctorId;
    private Integer userId; // Links to user account (nullable for patients without accounts)
    private String firstName;
    private String lastName;
    private String dateOfBirth;
    private String gender;
    private String phone;
    private String email;
    private String address;
    private String bloodGroup;
    private String allergies;
    private String lastVisit;
    private String createdAt;

    // Constructors
    public Patient() {
    }

    public Patient(int id, int doctorId, Integer userId, String firstName, String lastName, String dateOfBirth,
                   String gender, String phone, String email, String address,
                   String bloodGroup, String allergies, String lastVisit, String createdAt) {
        this.id = id;
        this.doctorId = doctorId;
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.bloodGroup = bloodGroup;
        this.allergies = allergies;
        this.lastVisit = lastVisit;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(int doctorId) {
        this.doctorId = doctorId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public String getAllergies() {
        return allergies;
    }

    public void setAllergies(String allergies) {
        this.allergies = allergies;
    }

    public String getLastVisit() {
        return lastVisit;
    }

    public void setLastVisit(String lastVisit) {
        this.lastVisit = lastVisit;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    // Helper Methods
    public String getFullName() {
        return firstName + " " + lastName;
    }

    public String getInitials() {
        String initials = "";
        if (firstName != null && !firstName.isEmpty()) {
            initials += firstName.charAt(0);
        }
        if (lastName != null && !lastName.isEmpty()) {
            initials += lastName.charAt(0);
        }
        return initials.toUpperCase();
    }

    public int getAge() {
        if (dateOfBirth == null || dateOfBirth.isEmpty()) {
            return 0;
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date birthDate = sdf.parse(dateOfBirth);
            if (birthDate != null) {
                Calendar birth = Calendar.getInstance();
                birth.setTime(birthDate);
                Calendar today = Calendar.getInstance();

                int age = today.get(Calendar.YEAR) - birth.get(Calendar.YEAR);

                if (today.get(Calendar.DAY_OF_YEAR) < birth.get(Calendar.DAY_OF_YEAR)) {
                    age--;
                }

                return age;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return 0;
    }

    @Override
    public String toString() {
        return "Patient{" +
                "id=" + id +
                ", name='" + getFullName() + '\'' +
                ", age=" + getAge() +
                ", gender='" + gender + '\'' +
                ", bloodGroup='" + bloodGroup + '\'' +
                '}';
    }
}
