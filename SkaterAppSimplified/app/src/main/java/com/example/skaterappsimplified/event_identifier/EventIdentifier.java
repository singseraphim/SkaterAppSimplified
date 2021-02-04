package com.example.skaterappsimplified.event_identifier;

import android.util.Log;

import com.example.skaterappsimplified.controller.Controller;
import com.example.skaterappsimplified.objects.Event;
import com.example.skaterappsimplified.objects.reading.Reading;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

/*
CHANGE NOTES:
Made all access functions into public variable accessors.
Changed Event object creation and observer call.
 */

public class EventIdentifier {
    //Code that makes it so there is only ever one EventIdentifier object
    private static final EventIdentifier ourInstance = new EventIdentifier();
    public static synchronized EventIdentifier getInstance() {
        return ourInstance;
    }

    //Allows the EventIdentifier to let the Dispatcher know when there is a new event.
    private EventIdentifierObserver observer;
    public void attachEventObserver(EventIdentifierObserver o) { this.observer = o; }

    private ArrayList<Reading> accelerometerPeaks = new ArrayList<>();
    private ArrayList<Reading> gyroscopePeaks = new ArrayList<>();
    private ArrayList<Reading> movingWindow = new ArrayList<>();

    private ArrayList<Event> uniqueJumpEvents = new ArrayList<>();

    private static int WINDOW_SIZE = 21;
    private static long TIMEOUT_MILS = 2000;
    private static int NOISE_CHECK_MIN_INTERVAL = 150; // milliseconds / 10000 really a guess.
    // from section 2.3 on page 3
    // https://doi.org/10.1371/journal.pone.0206162
    private static double MIN_PEAK_ACCEL_X_VAL = 25; // m / s^2
    private static double MIN_ACCEL_PROMENANCE = 20;
    private static double MIN_PEAK_GYRO_X_VAL = 12;  // rad / sec (pretty sure it's rad/sec not deg/sec)
    private static double MIN_GYRO_PROMENANCE = 12;
    // from section 2.3 on page 3
    // https://doi.org/10.1371/journal.pone.0206162
    private static double MIN_JUMP_INTERVAL = 300; // milliseconds / 10000
    private static double MAX_JUMP_INTERVAL = 850;
    private static String logtag = "SkatingEventIdentifier";
    private long currentTimestamp;
    private int readingCount = 0;

    public void onSessionStart() {
        Log.d(logtag,"starting session");
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (Controller.getInstance().session != null && accelerometerPeaks.size() > 1 && gyroscopePeaks.size() > 1) {
                    filterAccelerometerPeakNoise();
                    filterGyroscopePeakNoise();
                    findEvents();
                }
            }
        }, 0, 500);
    }
    public void onSessionEnd() {
        Log.d(logtag,"ending session");
        accelerometerPeaks = new ArrayList<>();
        gyroscopePeaks = new ArrayList<>();
        uniqueJumpEvents = new ArrayList<>();
    }

    //SCHEDULED ASYNCHRONOUS TASKS

    public void filterGyroscopePeakNoise() {
        ArrayList<Reading> newGyroscopePeaks = new ArrayList<>();
        ArrayList<Integer> removePeakIndices = new ArrayList<>();
        if(gyroscopePeaks.size() < 2) return;
        for (int i = 0; i < gyroscopePeaks.size(); ++i) {
            boolean examineNext = true;
            int indexToExamine = i;
            Reading current = gyroscopePeaks.get(i);
            while(examineNext) {
                if (indexToExamine == gyroscopePeaks.size() - 1) break;
                Reading next = gyroscopePeaks.get(++indexToExamine);
                double diff = next.timestamp - current.timestamp;
                if (next.timestamp - current.timestamp < NOISE_CHECK_MIN_INTERVAL){
                    if (current.accelerometerReading.x >
                            next.accelerometerReading.x) {
                        removePeakIndices.add(indexToExamine);
                    }
                    else {
                        removePeakIndices.add(i);
                        current = gyroscopePeaks.get(indexToExamine);
                    }
                }
                else examineNext = false;
            }
        }
        for (int i = 0; i < gyroscopePeaks.size(); ++i) {
            boolean insertPeak = true;
            for (int j = 0; j < removePeakIndices.size(); ++j){
                if (removePeakIndices.get(j) == i) insertPeak = false;
            }
            if (insertPeak) newGyroscopePeaks.add(gyroscopePeaks.get(i));
        }
    }

    public void filterAccelerometerPeakNoise() {
        ArrayList<Reading> newAccelerometerPeaks = new ArrayList<>();
        ArrayList<Integer> removePeakIndices = new ArrayList<>();
        if(accelerometerPeaks.size() < 2) return;
        for (int i = 0; i < accelerometerPeaks.size(); ++i) {
            boolean examineNext = true;
            int indexToExamine = i;
            Reading current = accelerometerPeaks.get(i);
            while(examineNext) {
                if (indexToExamine == accelerometerPeaks.size() - 1) break;
                Reading next = accelerometerPeaks.get(++indexToExamine);
                if (next.timestamp - current.timestamp < NOISE_CHECK_MIN_INTERVAL){
                    if (current.accelerometerReading.x >
                            next.accelerometerReading.x) {
                        removePeakIndices.add(indexToExamine);
                    }
                    else {
                        removePeakIndices.add(i);
                        current = accelerometerPeaks.get(indexToExamine);
                    }
                }
                else examineNext = false;
            }
        }
        for (int i = 0; i < accelerometerPeaks.size(); ++i) {
            boolean insertPeak = true;
            for (int j = 0; j < removePeakIndices.size(); ++j){
                if (removePeakIndices.get(j) == i) insertPeak = false;
            }
            if (insertPeak) newAccelerometerPeaks.add(accelerometerPeaks.get(i));
        }
        // use the newly filtered list from here on out.
        accelerometerPeaks = newAccelerometerPeaks;
    }

    public void findEvents() {
        // was...
        // Reading secondPeak = accelerometerPeaks.get(accelerometerPeaks.size() - 1);
        // Reading firstPeak = accelerometerPeaks.get(accelerometerPeaks.size() - 2);
        // todo compare all pairs, not just last 2.
        for (int i = 0; i < accelerometerPeaks.size(); i++) {
            Reading firstPeak = accelerometerPeaks.get(i);
            for (int j = i + 1; j < accelerometerPeaks.size(); j++) {
                Reading secondPeak = accelerometerPeaks.get(j);
                double secondPeakTime = secondPeak.timestamp;
                double firstPeakTime = firstPeak.timestamp;
                double timeDiff = secondPeak.timestamp - firstPeak.timestamp;
                //Log.d(logtag, "time diff " + timeDiff);
                if (timeDiff >= MIN_JUMP_INTERVAL && timeDiff <= MAX_JUMP_INTERVAL) {
                    //Log.d(logtag, "peaks in bounds with interval between peaks of " + timeDiff);
                    //NOTE: Changed Lists.reverse() to Collections.reverse().
                    ArrayList<Reading> localCopyGryoPeaks = new ArrayList<Reading>(gyroscopePeaks);
                    for (Reading r : Lists.reverse(localCopyGryoPeaks)) {
                        long gyroPeakTime = r.timestamp;
                        //Log.d(logtag, "first to gryo " + (gyroPeakTime - firstPeakTime) + " gryo to second " + (secondPeakTime - gyroPeakTime));
                        if (r.timestamp >= firstPeak.timestamp
                                && r.timestamp <= secondPeak.timestamp) {
                            //Log.d(logtag, "**** found a jump event");
                            //Log.d(logtag,"first peak " + firstPeakTime + " and second peak " + secondPeakTime);
                            //Log.d(logtag,"gryo above threshold at " + gyroPeakTime);
                            addNewEventIfUnique(firstPeak, secondPeak, r);
                        }
                    }
                }
            }
        }
    }
    public void clearOldData() {
        ListIterator<Reading> iter = accelerometerPeaks.listIterator();
        while(iter.hasNext()){
            if(iter.next().timestamp < currentTimestamp - TIMEOUT_MILS){
                iter.remove();
            }
        }
        iter = gyroscopePeaks.listIterator();
        while(iter.hasNext()){
            if(iter.next().timestamp < currentTimestamp - TIMEOUT_MILS){
                iter.remove();
            }
        }
    }

    //SYNCHRONOUS TASKS

    public void peakDetection(Reading r) {
        setMovingWindow(r);
        Reading examinedReading = movingWindow.get(movingWindow.size()/2);
        if (movingWindow.size() < WINDOW_SIZE) return;

        //calculate absolute value of accelerometer prominence
        double accelProminence = calculateAbsAccelProminence(movingWindow);

        //if accelerometer prominence is higher than the threshold, then add it to the peak list
        if (accelProminence > MIN_ACCEL_PROMENANCE) {
            //Log.d(logtag, "reading "+ readingCount + ": " + examinedReading.toString());
            //Log.d(logtag, "--> accel peak:" + accelProminence);
            double x = examinedReading.accelerometerReading.x;
            // we found a peak.  Was the actual value at that peak large enough?
            // and do all this in absolute values.
            if (Math.abs(examinedReading.accelerometerReading.x) >= MIN_PEAK_ACCEL_X_VAL) {
                //Log.d (logtag, "---> peak was over threshold at " + examinedReading.accelerometerReading.x);
                accelerometerPeaks.add(examinedReading);
            }
        }
        double x = examinedReading.gyroscopeReading.x;
        // todo why not?
        if (Math.abs(examinedReading.gyroscopeReading.x) >= MIN_PEAK_GYRO_X_VAL) {
            //Log.d(logtag, "==> gryo peak: " + examinedReading.timestamp + " " + examinedReading.gyroscopeReading.x);
            gyroscopePeaks.add(examinedReading);
        }
    }

    public double calculateAbsAccelProminence(ArrayList<Reading> readings) {
        int midpointIndex = readings.size()/2;
        double avgBefore = avgAccelX(readings.subList(0, midpointIndex));
        double avgAfter = avgAccelX(readings.subList(midpointIndex + 1, readings.size()));
        double prominence = Math.abs(Math.min(readings.get(midpointIndex).accelerometerReading.x - avgBefore,
                readings.get(midpointIndex).accelerometerReading.x - avgAfter));
        return (prominence);

    }

    public double avgAccelX(List<Reading> readings) {
        double total = 0;
        for (Reading r : new ArrayList<>(readings)) total += r.accelerometerReading.x;
        return total/readings.size();
    }

    public void setMovingWindow(Reading r) {
        movingWindow.add(r);
        if (movingWindow.size() > WINDOW_SIZE) movingWindow.remove(0);
    }

    //Called by the dispatcher whenever there is a new reading
    public void onNewReading(Reading r) {
        readingCount ++;
        currentTimestamp = r.timestamp;
        peakDetection(r);
    }

    public void addNewEventIfUnique(Reading firstAccelPeak, Reading secondAccelPeak, Reading gyroPeak) {

        Event newEvent = new Event(
                firstAccelPeak.timestamp,
                secondAccelPeak.timestamp
        );

        // note that I've overloaded .equals in Event so that we are making a meaningful comparison
        // here, otherwise we are comparing object refs.
        if (inUniqueJumpEvents(newEvent)) {
            // do nothing we already have this event.
            //Log.d (logtag,"already seen this event.  Pass");
        } else {
            uniqueJumpEvents.add(newEvent);
            observer.onNewEvent(newEvent);
            CalculateAndStoreJumpProperties(newEvent);
            Log.d(logtag, "added a unique event");
            Log.d(logtag, newEvent.toString());
        }
    }

    private boolean inUniqueJumpEvents(Event event) {
        for (Event e : uniqueJumpEvents) {
            if (event.startTime == e.startTime && event.endTime == e.endTime) return true;
        }
        return false;
    }

    private void CalculateAndStoreJumpProperties(Event newEvent) {
        // see if we can get a reading from when the event started.
        ArrayList<Reading> eventReadings = Controller.getInstance().session.getEventReadings(newEvent);
        if (eventReadings == null) return;
        /* Log.d (logtag, "------> first reading in this event: ");
        Log.d (logtag, eventReadings.get(0).toString());
        Log.d (logtag, "------> last reading in this event: ");
        Log.d (logtag, eventReadings.get(eventReadings.size()-1).toString());
         */

        // calculate total spin.  i = 1 because we are doing backwards samples.
        double totalSpinRadians = 0;
        Reading peakSpinReading = eventReadings.get(0);
        for (int i = 1 ; i < eventReadings.size(); i++) {
            Reading r1 = eventReadings.get(i-1);
            Reading r2 = eventReadings.get(i);
            double rotVelLongitudinalAxis = r2.gyroscopeReading.x;
            double deltaT = (r2.timestamp - r1.timestamp) / 1000000.0;  // in sec
            double radiansRotated = rotVelLongitudinalAxis * deltaT;
            totalSpinRadians += radiansRotated;
            if (Math.abs(rotVelLongitudinalAxis) > Math.abs(peakSpinReading.gyroscopeReading.x)) {
                peakSpinReading = r2;
            }
        }
        double totalDurationSeconds =
                ((eventReadings.get(eventReadings.size()-1)).timestamp -
                        eventReadings.get(0).timestamp)
                        / 1000000.0;
        double degreesRotated = Math.toDegrees(totalSpinRadians);
        double rotations = degreesRotated / 360.0;
        double peakSpinTimeSeconds = ((peakSpinReading.timestamp-eventReadings.get(0).timestamp)/1000000.0);
//        newEvent.addQuantitativeAttribte("TotalRotation",degreesRotated,"degrees");
//        newEvent.addQuantitativeAttribte("PeakRotationVelocity",Math.toDegrees(peakSpinReading.gyroscopeReading.x),"degrees per second");
//        newEvent.addQuantitativeAttribte("PeakRotationTime", peakSpinTimeSeconds, "seconds");
//        newEvent.addQuantitativeAttribte("Rotations",Math.abs(rotations),"complete rotations");
//        newEvent.addQuantitativeAttribte("Duration", totalDurationSeconds, "seconds");
        /*
        Log.d(logtag,"jump duration (s) " + totalDurationSeconds);
        Log.d(logtag,"total rotation (deg) " + degreesRotated + " (rotations: " + rotations+")");
        Log.d(logtag, "peak spin velocity was (deg/s) "+ (Math.toDegrees(peakSpinReading.gyroscopeReading.x))  +" at ");
        Log.d(logtag,peakSpinReading.toString());
        Log.d(logtag,"which is " + ((peakSpinReading.timestamp-eventReadings.get(0).timestamp)/1000000.0) +
                " seconds into the jump");
         */
    }
}

/*
data/raw/CNP/CNP_22_info.csv
data/raw/CNP/CNP_27_info.csv
data/raw/CNP/CNP_28_info.csv
data/raw/CNP/CNP_21_info.csv
data/raw/CNP/CNP_10_info.csv
data/raw/CNP/CNP_17_info.csv
 */