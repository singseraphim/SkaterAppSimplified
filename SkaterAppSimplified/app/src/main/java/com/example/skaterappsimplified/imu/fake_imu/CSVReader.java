package com.example.skaterappsimplified.imu.fake_imu;


import android.content.Context;
import android.os.health.SystemHealthManager;

import com.example.skaterappsimplified.objects.reading.AccelerometerReading;
import com.example.skaterappsimplified.objects.reading.GyroscopeReading;
import com.example.skaterappsimplified.objects.reading.MagnetometerReading;
import com.example.skaterappsimplified.objects.reading.Reading;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.util.LinkedList;
import java.util.List;

public class CSVReader {
    private final InputStream fileStream;
    private final int TIME = 0;
    private final int ACCELEROMETER_X = 1;
    private final int ACCELEROMETER_Y = 2;
    private final int ACCELEROMETER_Z = 3;
    private final int GYROSCOPE_X = 4;
    private final int GYROSCOPE_Y = 5;
    private final int GYROSCOPE_Z = 6;
    private final int MAGNETOMETER_X = 7;
    private final int MAGNETOMETER_Y = 8;
    private final int MAGNETOMETER_Z = 9;

    private final int LINE_START = 4;

    public CSVReader(String fileName, Context c) {
        int fakeIMUFileResourceID = c.getResources().getIdentifier(fileName, "raw",
                c.getPackageName());
        this.fileStream = c.getResources().openRawResource(fakeIMUFileResourceID);
    }

    public List<Reading> parseFakeIMUData() {
        try {
            org.apache.commons.csv.CSVParser csvParser = CSVFormat.DEFAULT.parse(new InputStreamReader(fileStream));
            List<CSVRecord> records = csvParser.getRecords();
            List<Reading> readingList = new LinkedList<>();

            for (int i = LINE_START; i < records.size(); ++i) {
                readingList.add(getReading(records.get(i)));
            }
            return readingList;
        }
        catch (IOException e) {
            System.out.println(e.toString());
        }
        return null;
    }

    private Reading getReading(CSVRecord record) {
        String time = record.get(TIME);
        long convertedLong = Long.parseLong(time);
        return new Reading(
                parseMagnetometerReading(record),
                parseGyroscopeReading(record),
                parseAccelerometerReading(record),
                convertedLong
        );

    }

    private MagnetometerReading parseMagnetometerReading(CSVRecord record) {
        String magnet_x = record.get(MAGNETOMETER_X);
        String magnet_y = record.get(MAGNETOMETER_Y);
        String magnet_z = record.get(MAGNETOMETER_Z);
        return new MagnetometerReading(
                Double.parseDouble(magnet_x),
                Double.parseDouble(magnet_y),
                Double.parseDouble(magnet_z)
        );
    }

    private AccelerometerReading parseAccelerometerReading(CSVRecord record) {
        String acc_x = record.get(ACCELEROMETER_X);
        String acc_y = record.get(ACCELEROMETER_Y);
        String acc_z = record.get(ACCELEROMETER_Z);
        return new AccelerometerReading(
                Double.parseDouble(acc_x),
                Double.parseDouble(acc_y),
                Double.parseDouble(acc_z));
    }

    private GyroscopeReading parseGyroscopeReading(CSVRecord record) {
        String gyro_x = record.get(GYROSCOPE_X);
        String gyro_y = record.get(GYROSCOPE_Y);
        String gyro_z = record.get(GYROSCOPE_Z);
        return new GyroscopeReading(
                Double.parseDouble(gyro_x),
                Double.parseDouble(gyro_y),
                Double.parseDouble(gyro_z)
        );
    }









}
