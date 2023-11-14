package edu.curtin;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class CalendarAPIImpl implements CalendarAPI {

    private List<Event> events = new ArrayList<>();
    private BlockingQueue<Event> queue = new LinkedBlockingQueue<>();

    @Override
    public Event createEvent(String title, String date, String startTime, int duration, int repeatDuration,int repeat) {
        Event event = new Event(title, LocalDate.parse(date), LocalTime.parse(startTime), duration, repeatDuration,repeat);
        addEvent(event);
        return event;
    }

    @Override
    public void addEvent(Event event) {
        events.add(event);
    }

    @Override
    public void notifyEvent(Event event,Locale locale) {
    ResourceBundle messages = ResourceBundle.getBundle("Messages", locale);
        LocalDate currentDate = LocalDate.now();
        LocalTime currentTime = LocalTime.now();
        currentTime = LocalTime.of(currentTime.getHour(), currentTime.getMinute(), 0);

        if (event.getStartDate().equals(currentDate)) {
            if (event.getStartTime().isEmpty()) {
                try {
                    queue.put(event);
                   
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    System.out.println(messages.getString("exception.message") + e.getMessage());
                }
            } else {
                LocalTime time = LocalTime.of(event.getStartTime().get().getHour(),
                        event.getStartTime().get().getMinute(), 0);
                        if (time.isAfter(currentTime) && time.isBefore(currentTime.plusMinutes(10))) {
                    try {
                        queue.put(event);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                         System.out.println(messages.getString("exception.message") + e.getMessage());
                    }
                }
            }
        }
    }

    @Override
    public List<Event> getEvents() {
        return events;
    }

    @Override
    public BlockingQueue<Event> getEventsQueue() {
        return queue;
    }

}
