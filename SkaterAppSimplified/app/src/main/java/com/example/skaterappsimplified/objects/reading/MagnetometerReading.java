package com.example.skaterappsimplified.objects.reading;

public class MagnetometerReading {
    public double x = 0;
    public double y = 0;
    public double z = 0;

    public MagnetometerReading() {
        this.x = 0;
        this.y = 0;
        this.z = 0;
    }

    public MagnetometerReading(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    public String toString() {
        return ("magnet: ("+this.x+", "+ this.y + ", "+ this.z +")");
    }

}
