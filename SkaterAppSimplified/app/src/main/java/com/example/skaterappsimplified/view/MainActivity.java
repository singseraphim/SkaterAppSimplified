package com.example.skaterappsimplified.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.skaterappsimplified.R;
import com.example.skaterappsimplified.controller.Controller;
import com.example.skaterappsimplified.controller.ControllerObserver;
import com.example.skaterappsimplified.objects.Event;
import com.example.skaterappsimplified.objects.Session;
import com.example.skaterappsimplified.objects.reading.Reading;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements ControllerObserver {

    //Controller handles the main app logic, and passes data between components.
    Controller controller = Controller.getInstance();

    //Lists of events and readings to display
    ArrayList<Event> recordedEvents = new ArrayList<>();
    ArrayList<Reading> recordedReadings = new ArrayList<>();

    //UI objects displayed in activity
    ImageView spinner;
    ImageView checkIcon;
    RecyclerView eventsList;
    RecyclerView.Adapter eventsListAdapter;
    Button startButton;
    Button stopButton;
    Button startNewButton;
    TextView statusLabel;
    TextView noEvents;
    TextView readingsTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_recording);

        //MainActivity observes the controller.
        controller.attachObserver(this);
        controller.setActivity(this);

        //UI setup
        initializeLayoutObjects();
        setOnClickHandlers();
        recyclerViewSetup();
        UIDisplayNoActiveSession();
    }

    /**
     * Attaches layout objects to their XML components.
     */
    private void initializeLayoutObjects() {
        spinner = findViewById(R.id.recording_spinner);
        eventsList = findViewById(R.id.events_list);
        startButton = findViewById(R.id.btn_start);
        stopButton = findViewById(R.id.btn_stop);
        startNewButton = findViewById(R.id.btn_new_session);
        statusLabel = findViewById(R.id.status_label);
        checkIcon = findViewById(R.id.ic_check);
        readingsTextView = findViewById(R.id.readings_count_text);
        noEvents = findViewById(R.id.no_events_text);
    }

    /**
     * Sets onClickHandlers for all clickable UI components.
     */
    private void setOnClickHandlers() {
        startButton.setOnClickListener(v -> {
            UIDisplaySessionRecording();
            startSession();
        });
        stopButton.setOnClickListener(v -> {
            UIDisplaySessionComplete();
            endSession();
        });
        startNewButton.setOnClickListener(v -> UIDisplayNoActiveSession());
    }

    private void startSession() {
        controller.startSession();
    }

    private void endSession() {
        controller.endSession();
    }

    /**
     * Changes UI to "Recording Data" mode.
     */
    void UIDisplaySessionRecording() {
        clearUI();
        spinner.setVisibility(View.VISIBLE);
        statusLabel.setText(R.string.recording_data);
        statusLabel.setVisibility(View.VISIBLE);
        if (recordedEvents.size() == 0) noEvents.setVisibility(View.VISIBLE);
        stopButton.setVisibility(View.VISIBLE);
        eventsList.setVisibility(View.VISIBLE);
        readingsTextView.setVisibility(View.VISIBLE);
    }

    /**
     * Changes UI to the "Session Complete" mode.
     */
    void UIDisplaySessionComplete() {
        clearUI();
        statusLabel.setText(R.string.session_complete);
        statusLabel.setVisibility(View.VISIBLE);
        startNewButton.setVisibility(View.VISIBLE);
        checkIcon.setVisibility(View.VISIBLE);
        eventsList.setVisibility(View.VISIBLE);
        readingsTextView.setVisibility(View.VISIBLE);
    }

    /**
     * Changes UI to the "No Active Session" mode.
     */
    void UIDisplayNoActiveSession() {
        clearUI();
        recordedEvents.clear();
        refreshEventsList();
        recordedReadings.clear();
        refreshReadingsCount();
        statusLabel.setText(R.string.no_active_session);
        statusLabel.setVisibility(View.VISIBLE);
        startButton.setVisibility(View.VISIBLE);
    }

    /**
     * Sets all UI components to be invisible. Helper function for UI display functions.
     */
    void clearUI() {
        spinner.setVisibility(View.GONE);
        eventsList.setVisibility(View.GONE);
        startButton.setVisibility(View.GONE);
        stopButton.setVisibility(View.GONE);
        startNewButton.setVisibility(View.GONE);
        statusLabel.setVisibility(View.GONE);
        noEvents.setVisibility(View.GONE);
        checkIcon.setVisibility(View.GONE);
        readingsTextView.setVisibility(View.GONE);
    }


    /**
     * Initializes recyclerview that displays list of jumps.
     */
    private void recyclerViewSetup() {
        LinearLayoutManager eventManager = new LinearLayoutManager(this);
        eventsList.setLayoutManager(eventManager);
        eventsListAdapter = new EventsListAdapter(this, recordedEvents);
        eventsList.setAdapter(eventsListAdapter);
    }

    /**
     * Called by the controller when there is a new event.
     *
     * @param event New event to be displayed
     */
    @Override
    public void onNewEvent(Event event) {
        Context c = this;
        Activity a = this;
        runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        recordedEvents.add(event);
                        refreshEventsList();
                    }
                }
        );

    }

    /**
     * Called by the controller when there is a new reading.
     *
     * @param reading New reading to be displayed
     */
    @Override
    public void onNewReading(Reading reading) {
        Context c = this;
        Activity a = this;

        runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        recordedReadings.add(reading);
                        refreshReadingsCount();
                    }
                }
        );


    }

    /**
     * Called by the controller when the session is labeled.
     *
     * @param session The labeled session object.
     */
    @Override
    public void onSessionUpdate(Session session) {
        recordedEvents = session.events;
        refreshEventsList();
    }

    /**
     * Refreshes event list UI component.
     */
    void refreshEventsList() {
        eventsListAdapter = new EventsListAdapter(this, recordedEvents);
        eventsList.setAdapter(eventsListAdapter);
        eventsList.scrollToPosition(recordedEvents.size() - 1);
    }

    /**
     * Refreshes readings count UI component.
     */
    void refreshReadingsCount() {
        String readingsText = "Readings: " + recordedReadings.size();
        readingsTextView.setText(readingsText);
    }

    /**
     * RecyclerView adapter that displays list of jumps.
     * This is mostly Android shenanigans, and you don't have to care about almost all of it.
     * EventsHolder.bind() has all code for formatting and setting text.
     */
    static class EventsListAdapter extends RecyclerView.Adapter<EventsHolder> {
        private final ArrayList<Event> items;
        private final LayoutInflater inflater;

        public EventsListAdapter(Context context, ArrayList<Event> items) {
            this.items = items;
            inflater = LayoutInflater.from(context);
        }

        @NonNull
        @Override
        public EventsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = inflater.inflate(R.layout.event_list_entry, parent, false);
            return new EventsHolder(view);
        }

        @Override
        public void onBindViewHolder(EventsHolder holder, final int position) {
            final Event event = items.get(position);
            holder.bind(event, position);
            holder.itemLayout.setOnClickListener(v -> {
                //FIXME: add onClick for events here
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

    }

    static class EventsHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final ConstraintLayout itemLayout;
        private final TextView eventLabel;
        private final TextView eventTimestamp;

        private EventsHolder(View view) {
            super(view);
            eventLabel = view.findViewById(R.id.event_header);
            eventTimestamp = view.findViewById(R.id.event_timestamp);
            itemLayout = view.findViewById(R.id.events_list_entry_container);
        }

        void bind(Event event, int pos) {

            //Display label
            String labelText;
            if (event.label == null) {
                labelText = "Jump " + (pos + 1);
            } else {
                labelText = "Jump " + (pos + 1) + ": " + event.label;
            }
            eventLabel.setText(labelText);

            //Display time

            //Convert milliseconds to seconds, then divide and mod by 60
            long minutes = (event.endTime / 1000) / 60;
            long seconds = (event.endTime / 1000) % 60;

            String m = String.valueOf(minutes);
            String s = String.valueOf(seconds);

            if (seconds < 10) {
                s = "0" + seconds;
            }
            String time = m + ":" + s;
            eventTimestamp.setText(time);

        }

        @Override
        public void onClick(View view) {
        }

    }

}

/*
THINGS THIS APP CAN DO:
Read a CSV file
Peak detection
Start and end a session, and display data
Graph the data
Talk to AWS
Maybe work with an IMU
Have unit tests and documentation for everything

THINGS THIS APP CANNOT DO:
Persistent data
Multiple skaters
Multiple devices
Multiple sessions
Pause a session
More than one activity
Settings

BUGS TO FIX:
Stop and reset not working

 */