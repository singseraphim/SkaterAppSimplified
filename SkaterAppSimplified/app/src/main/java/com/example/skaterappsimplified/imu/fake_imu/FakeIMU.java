package com.example.skaterappsimplified.imu.fake_imu;

import android.app.Activity;

import com.example.skaterappsimplified.imu.IMUObserver;
import com.example.skaterappsimplified.objects.reading.AccelerometerReading;
import com.example.skaterappsimplified.objects.reading.GyroscopeReading;
import com.example.skaterappsimplified.objects.reading.MagnetometerReading;
import com.example.skaterappsimplified.objects.reading.Reading;

import java.util.ArrayList;
import java.util.List;

public class FakeIMU {
    //Only one FakeIMU object is used in app.
    private static final FakeIMU ourInstance = new FakeIMU();
    public static synchronized FakeIMU getInstance() {
        return ourInstance;
    }

    //Attaches an observer. This observer is called when any new readings.
    IMUObserver observer = null;
    public void attachObserver(IMUObserver observer) {this.observer = observer;}

    private Thread dataThread;

    //Name of CSV file.
    private final String FILE_NAME = "cnp_12";

    private boolean sessionActive = false;

    private Activity activity;

    private ArrayList<Reading> csvReadings = new ArrayList<>();




    //Called by the controller when the user starts a session.
    public void startSession() {
        if (csvReadings.size() == 0) parseCSV();
        sessionActive = true;
        dataThread = new Thread(this::processReadings);
        dataThread.start();
    }

    /**
     * Parses readings from CSV file and populates csvReadings. Called once, in constructor.
     */
    public void parseCSV() {
        //Parse reading objects from CSV file
        CSVReader csvReader = new CSVReader(FILE_NAME, activity.getApplicationContext());
        List<Reading> readings = csvReader.parseFakeIMUData();

        //Set all timestamps to be relative to the first timestamp
        long first_timestamp = readings.get(0).timestamp;
        for (int i = 0; i < readings.size(); ++i) {
            readings.get(i).timestamp = (readings.get(i).timestamp - first_timestamp);
        }

        csvReadings = new ArrayList<>(readings);
    }

    /**
     * Parses readings from CSV file, retimes them, and notifies the observer for each reading.
     */
    private void processReadings() {

        //For each reading:
        for (int i = 0; i < csvReadings.size() - 1; ++i) {
            if (!sessionActive) return;

            //Grab the next two readings
            Reading readingBefore = csvReadings.get(i);
            Reading readingAfter = csvReadings.get(i + 1);

            //Use those two readings to interpolate a reading at 104 readings per second.
            //retime() does not always return a reading, because it is scaling down to a lower sampling rate.
            Reading interpolatedReading = retime(readingBefore, readingAfter);
            if (interpolatedReading == null) continue;

            //retime reading from microseconds to milliseconds
            interpolatedReading.timestamp = (interpolatedReading.timestamp / 1000);

            observer.onNewReading(interpolatedReading);

            //slow the data flow a bit, to simulate a real IMU.
            try {
                dataThread.sleep(2);
            } catch (InterruptedException e) {
                //throws InterruptedException if the session ends while the thread is sleeping.
                if (!sessionActive) return;
            }
        }
    }
    


    private double nextRetimedTimestamp = 0;
    private static double RETIMED_INTERVAL = 9615.3846153846;

    /**
     * Retimes readings from 120 readings per second (rps) to 104 rps.
     * At this point in execution, all timestamps are in microseconds.
     * There 1,000,000 microseconds in a second, and 104 rps.
     * 1,000,000 / 104 ~= 9615.3846153846, so that is our interval between retimed readings.
     * Every 9615 microseconds, we make a new reading. The reading values are interpolated
     * from the 120 rps readings that are directly before and after our new reading.
     * @param r1 Reading 1
     * @param r2 Reading 2
     * @return Interpolated reading between r1 and r2 if the next retimed timestamp falls between
     * those values, or null if it does not.
     */

    public Reading retime(Reading r1, Reading r2) {
        //if next timestamp falls between the two readings:
        if (nextRetimedTimestamp >= r1.timestamp && nextRetimedTimestamp <= r2.timestamp) {

            //create new Reading object with interpolated values
            Reading reading = new Reading();
            reading.timestamp = Math.round(nextRetimedTimestamp);
            reading.magnetometerReading = new MagnetometerReading(
                    lerp(nextRetimedTimestamp, r1.timestamp, r2.timestamp,
                            r1.magnetometerReading.x,
                            r2.magnetometerReading.x),
                    lerp(nextRetimedTimestamp, r1.timestamp, r2.timestamp,
                            r1.magnetometerReading.y,
                            r2.magnetometerReading.y),
                    lerp(nextRetimedTimestamp, r1.timestamp, r2.timestamp,
                            r1.magnetometerReading.z,
                            r2.magnetometerReading.z)
            );

            reading.accelerometerReading = new AccelerometerReading(
                    lerp(nextRetimedTimestamp, r1.timestamp, r2.timestamp,
                            r1.accelerometerReading.x,
                            r2.accelerometerReading.x),
                    lerp(nextRetimedTimestamp, r1.timestamp, r2.timestamp,
                            r1.accelerometerReading.y,
                            r2.accelerometerReading.y),
                    lerp(nextRetimedTimestamp, r1.timestamp, r2.timestamp,
                            r1.accelerometerReading.z,
                            r2.accelerometerReading.z)
            );

            reading.gyroscopeReading = new GyroscopeReading(
                    lerp(nextRetimedTimestamp, r1.timestamp, r2.timestamp,
                            r1.gyroscopeReading.x,
                            r2.gyroscopeReading.x),
                    lerp(nextRetimedTimestamp, r1.timestamp, r2.timestamp,
                            r1.gyroscopeReading.y,
                            r2.gyroscopeReading.y),
                    lerp(nextRetimedTimestamp, r1.timestamp, r2.timestamp,
                            r1.gyroscopeReading.z,
                            r2.gyroscopeReading.z)
            );

            nextRetimedTimestamp += RETIMED_INTERVAL;
            return reading;
        }
        else {

            //if the next timestamp does not fall between r1 and r2, do not return a new reading.
            return null;
        }

    }

    //performs linear interpolation function on reading values (equation came from google)
    public double lerp(double x, long x1, long x2, double y1, double y2) {
        return y1 + (x - x1) * ((y2 - y1) / (x2 - x1));
    }

    /**
     * Called by the controller when the user ends a session.
     */
    public void endSession() {
        sessionActive = false;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

}
