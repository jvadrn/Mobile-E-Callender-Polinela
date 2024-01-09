package com.example.firebasecalendarapp;

public class EventModel {

    private String eventKey;
    private String date;
    private String eventName;

    public EventModel() {
        // Default constructor required for DataSnapshot.getValue(EventModel.class)
    }

    public EventModel(String eventKey, String date, String eventName) {
        this.eventKey = eventKey;
        this.date = date;
        this.eventName = eventName;
    }

    public String getEventKey() {
        return eventKey;
    }

    public String getDate() {
        return date;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }
}
