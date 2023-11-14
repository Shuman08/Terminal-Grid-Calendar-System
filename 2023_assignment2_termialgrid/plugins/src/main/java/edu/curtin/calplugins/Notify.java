package edu.curtin.calplugins;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import edu.curtin.Event;
import edu.curtin.CalendarAPI;

public class Notify implements CalendarPlugin {
    private CalendarAPI calendarAPI;

    public Notify(CalendarAPI calendarAPI) {
        this.calendarAPI = calendarAPI;

        
    }

    @Override
    public void execute(Map<String, String> arguments,Locale locale) {
            ResourceBundle messages = ResourceBundle.getBundle("Messages", locale);
        // if (arguments.containsKey("text")) {
        // String text = arguments.get("text").replaceAll("\"", "");;
        if (arguments.containsKey(messages.getString("text"))) {
        String text = arguments.get(messages.getString("text")).replaceAll("\"", "");;
        List<Event> events = calendarAPI.getEvents();

        for (Event event : events) {
           
            if (event.getTitle().toLowerCase().contains(text.toLowerCase())) {
                calendarAPI.notifyEvent(event,locale);
            }
        }
    }
    }

}
