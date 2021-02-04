package com.example.skaterappsimplified.imu;

import com.example.skaterappsimplified.objects.reading.Reading;

public interface IMUObserver {
    void onNewReading(Reading reading);
}
