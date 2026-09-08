package com.hospital.hms.dto;

import java.time.LocalTime;

public class TimeSlotDTO {

    private LocalTime time;
    private String formattedTime;
    private boolean available;
    private boolean booked;

    public TimeSlotDTO() {
    }

    public TimeSlotDTO(LocalTime time, String formattedTime, boolean available, boolean booked) {
        this.time = time;
        this.formattedTime = formattedTime;
        this.available = available;
        this.booked = booked;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public String getFormattedTime() {
        return formattedTime;
    }

    public void setFormattedTime(String formattedTime) {
        this.formattedTime = formattedTime;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
        this.booked = !available;
    }

    public boolean isBooked() {
        return booked;
    }

    public void setBooked(boolean booked) {
        this.booked = booked;
        this.available = !booked;
    }
}
