package com.example.skaterappsimplified.objects.reading;

import androidx.annotation.NonNull;

public class AccelerometerReading {
    public double x;
    public double y;
    public double z;

    public AccelerometerReading() {
        this.x = 0;
        this.y = 0;
        this.z = 0;
    }

    public AccelerometerReading(double x, double y, double z){
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @NonNull
    public String toString () {
        return ("accel: ("+x + ", "+ y + ", "+ z+")");
    }

}
