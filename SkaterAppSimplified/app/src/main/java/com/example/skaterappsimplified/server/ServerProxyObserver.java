package com.example.skaterappsimplified.server;

import com.example.skaterappsimplified.objects.Session;

public interface ServerProxyObserver {
    void onNewLabeledSession(Session session);
}
