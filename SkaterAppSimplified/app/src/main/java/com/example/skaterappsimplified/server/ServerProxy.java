package com.example.skaterappsimplified.server;

import com.example.skaterappsimplified.objects.Session;

public class ServerProxy {

    //Only one ServerProxy object is used in app.
    private static final ServerProxy ourInstance = new ServerProxy();
    public static synchronized ServerProxy getInstance() {
        return ourInstance;
    }

    //Attaches a ServerProxy observer. This observer is called when the server returns the labeled session.
    ServerProxyObserver observer = null;
    public void attachObserver(ServerProxyObserver observer) { this.observer = observer; }

    //URL of AWS lambda
    private String URL =  "https://0cefs6gq8b.execute-api.us-east-1.amazonaws.com/beta";

    /**
     * Sends session to classifier on AWS server.
     * @param session Session object to be sent to the server.
     */
    public void sendSession(Session session) {

    }

    /**
     * Called when server returns a labeled session object.
     * @param session Labeled session object.
     */
    public void onSessionReturn(Session session) {
        observer.onNewLabeledSession(session);
    }

}
