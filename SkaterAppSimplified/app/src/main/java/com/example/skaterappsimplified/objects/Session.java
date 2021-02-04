package com.example.skaterappsimplified.objects;

import com.example.skaterappsimplified.objects.reading.Reading;

import java.util.ArrayList;
import java.util.Iterator;

public class Session {
    public ArrayList<Reading> readings = new ArrayList<>();
    public ArrayList<Event> events = new ArrayList<>();

    /**
     * Returns all readings between the startTime and endTime of the given event.
     * @param event Event to get all readings that fall within.
     * @return An ArrayList of all readings that fall within the given event.
     */
    public ArrayList<Reading> getEventReadings(Event event) {
        ArrayList<Reading> eventReadings = new ArrayList<>();
        for (Reading r : new ArrayList<>(readings)) {
            if (r.timestamp >= event.startTime && r.timestamp <= event.endTime) eventReadings.add(r);
        }
        return eventReadings;
    }

}
