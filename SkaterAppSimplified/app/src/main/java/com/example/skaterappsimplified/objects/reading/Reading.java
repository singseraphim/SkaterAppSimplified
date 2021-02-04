package com.example.skaterappsimplified.objects.reading;

public class Reading {
    public MagnetometerReading magnetometerReading;
    public GyroscopeReading gyroscopeReading;
    public AccelerometerReading accelerometerReading;
    public long timestamp;

    public Reading() {
        magnetometerReading = new MagnetometerReading();
        gyroscopeReading = new GyroscopeReading();
        accelerometerReading = new AccelerometerReading();
        timestamp = 0;
    }

    public Reading(MagnetometerReading magnetometerReading, GyroscopeReading gyroscopeReading, AccelerometerReading accelerometerReading, long timestamp) {
        this.magnetometerReading = magnetometerReading;
        this.gyroscopeReading = gyroscopeReading;
        this.accelerometerReading = accelerometerReading;
        this.timestamp = timestamp;
    }
}
