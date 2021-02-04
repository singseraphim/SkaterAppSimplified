package com.example.skaterappsimplified.objects;

public class Event {
    //Start and end times are relative to the beginning of the session
    public long startTime;
    public long endTime;
    public String label;


    public Event(long startTime, long endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }
}
