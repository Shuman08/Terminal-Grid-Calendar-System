package edu.curtin.calplugins;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

import edu.curtin.Event;
import edu.curtin.CalendarAPI;

public class Repeat implements CalendarPlugin {
    private CalendarAPI calendarAPI;

    public Repeat(CalendarAPI calendarAPI) {
        this.calendarAPI = calendarAPI;
    }

     @Override
    public void execute(Map<String, String> arguments,Locale locale) {
          ResourceBundle messages = ResourceBundle.getBundle("Messages", locale);
        // String title = arguments.get("title").replaceAll("\"", "");;
        // String startDateStr = arguments.get("startDate");
        // String startTimeStr = arguments.get("startTime");
        String title = arguments.get(messages.getString("title")).replaceAll("\"", "");;
        String startDateStr = arguments.get(messages.getString("startDate"));
        String startTimeStr = arguments.get(messages.getString("startTime"));

        // Remove double quotes from startDate and startTime
        startDateStr = startDateStr.replaceAll("\"", "");
        startTimeStr = startTimeStr.replaceAll("\"", "");

        LocalDate startDate = LocalDate.parse(startDateStr);
        
        // Parse startTime as an Optional<LocalTime>
        Optional<LocalTime> optionalStartTime = parseOptionalLocalTime(startTimeStr);
        // int duration= Integer.parseInt(arguments.get("duration").replaceAll("\"", ""));
        // int repeatDuration= Integer.parseInt(arguments.get("repeatDuration").replaceAll("\"", ""));
        // int repeat = Integer.parseInt(arguments.get("repeat").replaceAll("\"", ""));
         int duration= Integer.parseInt(arguments.get(messages.getString("duration")).replaceAll("\"", ""));
        int repeatDuration= Integer.parseInt(arguments.get(messages.getString("repeatDuration")).replaceAll("\"", ""));
        int repeat = Integer.parseInt(arguments.get(messages.getString("repeat")).replaceAll("\"", ""));

        Event event = null;

        for (int i = 0, repeatCount = 0; repeatCount < repeat; i += repeatDuration, repeatCount++) {
            if (repeatCount == repeat) {
                break; // Stop when we've reached the specified number of repeats
            }
        
            LocalDate repeatedDate = startDate.plusDays(i);
        
            if (optionalStartTime.isPresent()) {
                // If startTime is present, use it; otherwise, use null
                event = new Event(title, repeatedDate, optionalStartTime.get(), duration, repeatDuration, repeat);
            } else {
                // Handle the case when startTime is missing or null
                event = new Event(title, repeatedDate, null, 0, repeatDuration, repeat);
            }
        
            calendarAPI.addEvent(event);
        }
        
    }

    private Optional<LocalTime> parseOptionalLocalTime(String timeStr) {
        if (timeStr.isEmpty()) {
            return Optional.empty(); // Return an empty Optional if timeStr is empty
        }
        return Optional.of(LocalTime.parse(timeStr));
    }

}
