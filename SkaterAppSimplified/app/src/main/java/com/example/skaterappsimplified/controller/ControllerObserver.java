package com.example.skaterappsimplified.controller;

import com.example.skaterappsimplified.objects.Event;
import com.example.skaterappsimplified.objects.Session;
import com.example.skaterappsimplified.objects.reading.Reading;

public interface ControllerObserver {
    void onNewEvent(Event event);
    void onNewReading(Reading reading);
    void onSessionUpdate(Session session);
}
