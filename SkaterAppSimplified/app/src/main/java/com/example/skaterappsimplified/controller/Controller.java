package com.example.skaterappsimplified.controller;

import android.app.Activity;

import com.example.skaterappsimplified.event_identifier.EventIdentifier;
import com.example.skaterappsimplified.event_identifier.EventIdentifierObserver;
import com.example.skaterappsimplified.imu.fake_imu.FakeIMU;
import com.example.skaterappsimplified.imu.IMUObserver;
import com.example.skaterappsimplified.objects.Event;
import com.example.skaterappsimplified.objects.Session;
import com.example.skaterappsimplified.objects.reading.Reading;
import com.example.skaterappsimplified.server.ServerProxy;
import com.example.skaterappsimplified.server.ServerProxyObserver;

public class Controller implements EventIdentifierObserver, IMUObserver, ServerProxyObserver {
    //Only one Controller object is used in app.
    private static final Controller ourInstance = new Controller();
    public static synchronized Controller getInstance() { return ourInstance; }

    //This app only keeps track of one session at a time.
    public Session session = new Session();

    //Used to keep track of app context
    Activity activity;

    //Attaches a controller observer. This observer is called when any new readings or events happen.
    ControllerObserver observer = null;
    public void attachObserver(ControllerObserver observer) { this.observer = observer; }

    FakeIMU imu = FakeIMU.getInstance();
    EventIdentifier eventIdentifier = EventIdentifier.getInstance();
    ServerProxy serverProxy = ServerProxy.getInstance();
    public Controller() {
        //Controller observes the IMU and event identifier.
        imu.attachObserver(this);
        eventIdentifier.attachEventObserver(this);
        serverProxy.attachObserver(this);
    }

    /**
     * Called by MainActivity when the user starts a session.
     */
    public void startSession() {
        session = new Session();
        imu.startSession();
        eventIdentifier.onSessionStart();
    }

    /**
     * Called by MainActivity when the user ends a session.
     */
    public void endSession() {
        session = null;
        imu.endSession();
        eventIdentifier.onSessionEnd();
    }

    /**
     * Called by EventIdentifier when a new event is found.
     * @param event The new event.
     */
    @Override
    public void onNewEvent(Event event) {
        session.events.add(event);
        observer.onNewEvent(event);
    }

    /**
     * Called by IMU when a new reading is found.
     * @param reading The new reading.
     */
    @Override
    public void onNewReading(Reading reading) {
        if (session == null) return;
        session.readings.add(reading);
        eventIdentifier.onNewReading(reading);
        observer.onNewReading(reading);
    }

    /**
     * Called by the ServerProxy when the server returns a labeled session.
     * @param session The new session object.
     */
    @Override
    public void onNewLabeledSession(Session session) {
        this.session = session;
        observer.onSessionUpdate(session);
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
        imu.setActivity(activity);
    }

}
