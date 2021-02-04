package com.example.skaterappsimplified.objects.reading;

import androidx.annotation.NonNull;

public class GyroscopeReading {
    public double x;
    public double y;
    public double z;

    public GyroscopeReading() {
        this.x = 0;
        this.y = 0;
        this.z = 0;
    }

    public GyroscopeReading(double x, double y, double z){
        this.x = x;
        this.y = y;
        this.z = z;
    }
    public String toString() {
        return ("gryo: ("+this.x+", "+ this.y + ", "+ this.z +")");
    }
}
