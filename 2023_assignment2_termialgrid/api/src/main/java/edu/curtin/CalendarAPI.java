package edu.curtin;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.BlockingQueue;

public interface CalendarAPI {
    Event createEvent(String title, String date, String startTime, int duration,int repeatDuration ,int repeat);
    void addEvent(Event event);
    void notifyEvent(Event event,Locale locale);
    List<Event> getEvents();
    BlockingQueue<Event> getEventsQueue();
}