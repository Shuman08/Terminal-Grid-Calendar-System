package edu.curtin.calplugins;
import java.util.Locale;
import java.util.Map;

public interface CalendarPlugin {
    void execute(Map<String, String> arguments,Locale locale);
}