package com.example.medimanager.models;

import java.io.Serializable;

public class Appointment implements Serializable {
    private int id;
    private int patientId;
    private int doctorId;
    private String patientName;
    private String doctorName;
    private String appointmentDate;
    private String appointmentTime;
    private String reason;
    private String status; // scheduled, in_progress, completed, cancelled
    private String notes;
    private String createdAt;

    // Constructors
    public Appointment() {
    }

    public Appointment(int id, int patientId, int doctorId, String patientName, String doctorName, String appointmentDate,
                       String appointmentTime, String reason, String status,
                       String notes, String createdAt) {
        this.id = id;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.patientName = patientName;
        this.doctorName = doctorName;
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
        this.reason = reason;
        this.status = status;
        this.notes = notes;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public int getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(int doctorId) {
        this.doctorId = doctorId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(String appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public String getAppointmentTime() {
        return appointmentTime;
    }

    public void setAppointmentTime(String appointmentTime) {
        this.appointmentTime = appointmentTime;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    // Helper Methods
    public boolean isPending() {
        return "pending".equalsIgnoreCase(status);
    }

    public boolean isScheduled() {
        return "scheduled".equalsIgnoreCase(status);
    }

    public boolean isInProgress() {
        return "in_progress".equalsIgnoreCase(status);
    }

    public boolean isCompleted() {
        return "completed".equalsIgnoreCase(status);
    }

    public boolean isCancelled() {
        return "cancelled".equalsIgnoreCase(status);
    }

    public String getStatusDisplayName() {
        if (status == null) return "Scheduled";

        switch (status.toLowerCase()) {
            case "pending":
                return "Pending";
            case "in_progress":
                return "In Progress";
            case "completed":
                return "Completed";
            case "cancelled":
                return "Cancelled";
            default:
                return "Scheduled";
        }
    }

    @Override
    public String toString() {
        return "Appointment{" +
                "id=" + id +
                ", patient='" + patientName + '\'' +
                ", date='" + appointmentDate + '\'' +
                ", time='" + appointmentTime + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
