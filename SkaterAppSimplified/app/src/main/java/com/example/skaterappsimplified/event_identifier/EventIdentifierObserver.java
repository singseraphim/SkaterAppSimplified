package com.example.skaterappsimplified.event_identifier;

import com.example.skaterappsimplified.objects.Event;

public interface EventIdentifierObserver {
    public void onNewEvent(Event event);

}
