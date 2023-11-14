package edu.curtin;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import java.util.*;

public class Event {
   private String title;
    private LocalDate startDate;
    private Optional<LocalTime> startTime; // Use Optional to allow null
    private Optional<Integer> repeatDuration; // Use Optional to allow null
    private Optional<Integer> duration;// Use Optional to allow null
    private Optional<Integer> repeat;// Use Optional to allow null

    // Constructor
    public Event(String title, LocalDate startDate,LocalTime startTime,int duration, int repeatDuration, int repeat) {
        this.title = title;
        this.startDate = startDate;
        this.startTime = Optional.ofNullable(startTime);
        this.duration = Optional.ofNullable(duration);
        this.repeatDuration = Optional.ofNullable(repeatDuration);
        this.repeat =  Optional.ofNullable(repeat);;
    }

    // Getters and setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public Optional<LocalTime> getStartTime() {
        return startTime;
    }

    public void setStartTime(Optional<LocalTime> startTime) {
        this.startTime = startTime;
    }

    public Optional<Integer> getDuration() {
        return duration;
    }

    public void setDuration(Optional<Integer> duration) {
        this.duration = duration;
    }

     public Optional<Integer> getRepeatDuration() {
        return repeatDuration;
    }

    public void setRepeatDuration(Optional<Integer> repeatDuration) {
        this.repeatDuration = repeatDuration;
    }

    public  Optional<Integer> getRepeat() {
        return repeat;
    }

    public void setRepeat( Optional<Integer> repeat) {
        this.repeat = repeat;
    }

    // toString method to display event details with different language
    public String toString(Locale locale) {
        ResourceBundle messages = ResourceBundle.getBundle("Messages", locale);
        String startTimeStr = startTime.map(LocalTime::toString).orElse(messages.getString("allday"));
        int durationValue = duration.orElse(0);
        int repeatDurationValue = repeatDuration.orElse(0);
        int repeatValue = repeat.orElse(0);
    
        return messages.getString("event.details")  + "\n" +
                "------------------------------------------" + "\n" +
                messages.getString("event.title") + title + '\'' + "\n" +
                messages.getString("event.date") + startDate + "\n" +
                messages.getString("event.time") + startTimeStr + "\n" +
                messages.getString("event.duration") + durationValue + (duration.isPresent() ? "" : " (defaulted to 0)") + "\n" +
                messages.getString("event.repDuration") + repeatDurationValue + (repeatDuration.isPresent() ? "" : " (defaulted to 0)") + "\n" +
                messages.getString("event.repeat") + + repeatValue + (repeat.isPresent() ? "" : " (defaulted to 0)") + "\n" ;
    }

   
}
