package edu.curtin.terminalgriddemo;

import edu.curtin.terminalgrid.TerminalGrid;

import java.lang.reflect.Constructor;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;
import java.util.concurrent.BlockingQueue;

import org.python.util.PythonInterpreter;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.python.core.*;
import org.python.util.*;

import edu.curtin.CalendarAPI;
import edu.curtin.CalendarAPIImpl;
import edu.curtin.Event;
import edu.curtin.calplugins.CalendarPlugin;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * This illustrates different ways to use TerminalGrid. You may not feel you
 * _need_ all the
 * different features shown here.
 */
public class TerminalGridDemo {
    static CalendarAPI calendarAPI = new CalendarAPIImpl();
    static Locale selectedLanguage = Locale.getDefault();
    static List<List<String>> table = new ArrayList<>();
    static List<String> colHeadings = new ArrayList<>();
    static List<String> allDayRow = new ArrayList<>();

    //Displaying calendar function
    public static void DisplayCalendar(Scanner scanner, String filename) {
        LocalDate currentDate = LocalDate.now();
        var terminalGrid = TerminalGrid.create();
        ResourceBundle messages = ResourceBundle.getBundle("Messages", selectedLanguage);

        while (true) {
            // Call creating the table
            List<List<String>> table = createCalendarTable(currentDate, filename);

            // Print the table
            terminalGrid.print(table);
            System.out.println();

            for (Event event : calendarAPI.getEvents()) {
                event.getStartTime().ifPresent(startTime -> {
                    if (startTime.equals(LocalTime.now())) {
                        System.out.println(event.getTitle());
                    }
                });
            }

            // Prompt the user for navigation
            System.out.println(messages.getString("navigation.message"));
            String input = scanner.nextLine().trim();

            //Navigation control
            if (input.toLowerCase().equals("q")) {
                // exit displaying and return to main menu
                break;
            } else if (input.toLowerCase().equals("+d")) {
                currentDate = currentDate.plusDays(1);
            } else if (input.toLowerCase().equals("+w")) {
                currentDate = currentDate.plusWeeks(1);
            } else if (input.toLowerCase().equals("+m")) {
                currentDate = currentDate.plusMonths(1);
            } else if (input.toLowerCase().equals("+y")) {
                currentDate = currentDate.plusYears(1);
            } else if (input.toLowerCase().equals("-d")) {
                currentDate = currentDate.minusDays(1);
            } else if (input.toLowerCase().equals("-w")) {
                currentDate = currentDate.minusWeeks(1);
            } else if (input.toLowerCase().equals("-m")) {
                currentDate = currentDate.minusMonths(1);
            } else if (input.toLowerCase().equals("-y")) {
                currentDate = currentDate.minusYears(1);
            } else if (input.toLowerCase().equals("t")) {
                currentDate = LocalDate.now(); // Return to today
            }
            else{
                System.out.println(messages.getString("wrong.navigation.message"));
            }

        }

    }

    //Creating table function 
    private static List<List<String>> createCalendarTable(LocalDate currentDate, String filename) {
        // Create a table with dates starting from currentDate
        List<List<String>> table = new ArrayList<>();
        List<String> colHeadings = new ArrayList<>();
        List<String> allDayRow = new ArrayList<>();
        ResourceBundle messages = ResourceBundle.getBundle("Messages", selectedLanguage);
        // Set the column headings for days starting from currentDate
        colHeadings.add(messages.getString("date.title"));

        LocalDate tempDate = currentDate; // Initialize tempDate for generating the column headings

        for (int i = 0; i < 7; i++) {
            colHeadings.add(tempDate
                    .format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(selectedLanguage)));
            // colHeadings.add(tempDate.format(DateTimeFormatter.ISO_DATE.localizedBy(selectedLanguage)));

            tempDate = tempDate.plusDays(1);
        }

        // Add the column headings as the first row
        table.add(colHeadings);

        allDayRow.add(messages.getString("alldays.title"));

        //set the column to empty first 
        for (int i = 0; i < 7; i++) {
            allDayRow.add(""); 
        }

        //adding row into table
        table.add(allDayRow);

        //setting the hour row 
        for (int hour = 0; hour < 24; hour++) {
            List<String> hourRow = new ArrayList<>();
            hourRow.add(LocalTime.of(hour, 0)
                    .format(DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM).withLocale(selectedLanguage))); // Format
                                                                                                                  // the
                                                                                                                  // hour
            // hourRow.add(LocalTime.of(hour,
            // 0).format(DateTimeFormatter.ISO_TIME.localizedBy(selectedLanguage))); //
            // Format the hour

            // Initialize cells for 7 day
            for (int i = 0; i < 7; i++) {
                hourRow.add("");
            }

            table.add(hourRow);
        }

        // Read events from the calendar file and populate the table
        loadingAndParsingFile(table, colHeadings, allDayRow, filename);
        addEventToCalendar(calendarAPI.getEvents(), table, colHeadings, allDayRow);

        return table;
    }

    //Loading and parsing file
    private static void loadingAndParsingFile(List<List<String>> table, List<String> colHeadings,
            List<String> allDayRow, String filename) {
        String pluginConfig = null;
        String pluginName = null;
        boolean insideScript = false;
        boolean insideMultiLineString = false;
        String pythonScript = null;
        ResourceBundle messages = ResourceBundle.getBundle("Messages", selectedLanguage);

        try {
            FileInputStream fileInputStream = new FileInputStream(filename);
            InputStreamReader inputStreamReader;

            // Detect UTF-16 encoding
            if (hasUTF16BEBOM(fileInputStream)) {
                inputStreamReader = new InputStreamReader(fileInputStream, Charset.forName("UTF-16BE"));
            } else {
                inputStreamReader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
            }

            BufferedReader reader = new BufferedReader(inputStreamReader);

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(" ");
                if (parts.length > 3 && parts[0].equals(messages.getString("keyword.event"))) {
                    LocalDate eventDate = LocalDate.parse(parts[1]);
                    String eventTitle = "";
                    LocalTime eventStartTime = null;
                    int duration = 0;

                    // Find the position of "all-day" in the parts
                    int allDayIndex = -1;
                    for (int i = 2; i < parts.length; i++) {
                        if (parts[i].equals(messages.getString("allday"))) {
                            allDayIndex = i;
                            break;
                        }
                    }

                    // Combine parts for getting the full event title
                    boolean isAllDayEvent = allDayIndex != -1;

                    if (!isAllDayEvent) {
                        if (parts.length >= 6) {
                            eventStartTime = LocalTime.parse(parts[2]);
                            duration = Integer.parseInt(parts[3]);
                            for (int i = 4; i < parts.length; i++) {
                                eventTitle += parts[i];
                            }
                        }
                    } else {
                        for (int i = 3; i < parts.length; i++) {
                            eventTitle += parts[i] + " ";
                        }
                    }

                    // Create a new Event object
                    Event event = new Event(eventTitle, eventDate, eventStartTime, duration, 0, 0);

                    // Add the event to calendar.API

                    calendarAPI.addEvent(event);

                } else if (parts[0].equals(messages.getString("keyword.plugin"))) {
                    pluginName = parts[1]; //get the plugin id 
                    pluginConfig = ""; // Initialize the plugin configuration
                } else if (pluginConfig != null) {
                    // Append the line to the plugin configuration
                    pluginConfig += line.trim();
                    if (line.trim().endsWith("}")) {
                        // Place that ends the plugin configuration
                        if (pluginName != null && !pluginConfig.isEmpty()) {
                            pluginConfig = pluginConfig.substring(0, pluginConfig.length() - 1); // Remove '{' and '}' if there is 
                            //Split the line inside plugin {} with ,
                            String[] configLines = pluginConfig.split(",");
                            //Adding to map 
                            Map<String, String> arguments = new HashMap<>();

                            //for each line get the key and value using : delimeter
                            for (String configLine : configLines) {
                                int colonIndex = configLine.indexOf(':');
                                if (colonIndex != -1) {
                                    String key = configLine.substring(0, colonIndex).trim();
                                    String value = configLine.substring(colonIndex + 1).trim();
                                    arguments.put(key, value);
                                }
                            }

                            //For debugging purpose
                            // System.out.println("Plugin Name: " + pluginName);
                            // System.out.println("Arguments: " + arguments);

                            loadingPlugin(pluginName, arguments);
                        }
                        //reset to null for reading next 
                        pluginName = null;
                        pluginConfig = null;
                    }
                } else if (parts[0].equals(messages.getString("keyword.script"))) {
                    //set flag that script started
                    insideScript = true;
                    pythonScript = "";
                    continue;
                } else if (insideScript) {
                    if (line.trim().equals("\"")) {
                        insideScript = false;
                    } else {
                        pythonScript += line + "\n";
                    }
                }

                if (insideScript && !insideMultiLineString) {
                    if (line.trim().startsWith("\"\"\"")) {
                        insideMultiLineString = true;
                    }
                } else if (insideScript && insideMultiLineString) {
                    if (line.trim().endsWith("\"\"\"")) {
                        insideMultiLineString = false;
                    }
                }

                if (insideScript && !insideMultiLineString) {
                    continue; 
                }

            }

            reader.close();
        } catch (IOException e) {
            System.out.println(messages.getString("failtoreadfile.message"));
        }

        //execute the Python script when it is not null
        if (pythonScript != null && !pythonScript.isEmpty()) {
            executePythonScript(pythonScript);
        }
    }

    //for checking if file is UTF16 
     private static boolean hasUTF16BEBOM(FileInputStream stream) {
        ResourceBundle messages = ResourceBundle.getBundle("Messages", selectedLanguage);
        byte[] bom = new byte[2];
        try {
            stream.read(bom, 0, 2);
        } catch (IOException e) {
             System.out.println(messages.getString("failtoreadfile.message"));
        }
        return bom[0] == -2 && bom[1] == -1; // UTF-16 BE BOM
    }


    //Executing python script
    private static void executePythonScript(String scriptText) {
    ResourceBundle messages = ResourceBundle.getBundle("Messages", selectedLanguage);
        try (PythonInterpreter interpreter = new PythonInterpreter()) {
            //testing for hello world 
            interpreter.exec(scriptText);

            //exec creating event functionin the script with manually event data 
            interpreter.exec("event = create_event('New event', '2023-12-31', '18:00:00', '120')");

            // Get the result from the Python script
            String event = interpreter.get("event").toString();

            // Define a regular expression pattern to match key-value pairs
            String pattern = "'(\\w+)'\\s*:\\s*('(\\d+)'|'([^']*)')";
            Pattern r = Pattern.compile(pattern);

            // Create a Matcher and apply the pattern
            Matcher matcher = r.matcher(event);

            // Create a map to store the key-value pairs
            Map<String, String> keyValuePairs = new HashMap<>();

            // Find and store key-value pairs
            while (matcher.find()) {
                String key = matcher.group(1);
                String numericValue = matcher.group(3);
                String textValue = matcher.group(4);

                String value = numericValue != null ? numericValue : textValue;
                keyValuePairs.put(key, value);
            }

            // Extract values from the map
            String date = keyValuePairs.get("date");
            String title = keyValuePairs.get("title");
            String duration = keyValuePairs.get("duration");
            String time = keyValuePairs.get("time");

            //use calendar api create event to add new event 
            calendarAPI.createEvent(title, date, time, Integer.parseInt(duration), 0, 0);
        } catch (NumberFormatException e) {
            // TODO Auto-generated catch block
             System.out.println(messages.getString("exception.message") + e.getMessage());
        }
    }

   
    // Reflection for loading the plugins
    private static void loadingPlugin(String pluginName, Map<String, String> arguments) {
    ResourceBundle messages = ResourceBundle.getBundle("Messages", selectedLanguage);
        try {
            Class<?> pluginClass = Class.forName(pluginName);
            Constructor<?> constructor = pluginClass.getConstructor(CalendarAPI.class);
            CalendarPlugin plugin = (CalendarPlugin) constructor.newInstance(calendarAPI);
            plugin.execute(arguments,selectedLanguage);
        } catch (Exception e) {
            System.out.println(messages.getString("exception.message") + e.getMessage());
        }
    }

    // Adding event to calendar
    private static void addEventToCalendar(List<Event> events, List<List<String>> table, List<String> colHeadings,
            List<String> allDayRow) {

        for (Event event : events) {

            LocalDate eventDate = event.getStartDate();
            String localizedEventDate = eventDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(selectedLanguage));
            // String localizedEventDate =
            // eventDate.format(DateTimeFormatter.ISO_DATE.localizedBy(selectedLanguage));
            Optional<LocalTime> eventTime = event.getStartTime();
            String eventTitle = event.getTitle();

            for (int col = 1; col < colHeadings.size(); col++) {
                String colHeading = colHeadings.get(col);
                // int col = colHeadings.indexOf(eventDate.toString());
                if (localizedEventDate.equals(colHeading)) {
                    if (eventTime.isEmpty()) {
                        allDayRow.set(col, eventTitle);
                    } else {
                        int eventStartHour = eventTime.get().getHour();
                        int eventDuration = event.getDuration().orElse(0) / 60;
                        for (int i = 0; i < eventDuration; i++) {
                            List<String> hourRow = table.get(eventStartHour + 2 + i); // Offset by 2 for Date row
                                                                                      // and"All-Day Events" row
                            hourRow.set(col, eventTitle);
                        }
                    }
                }
            }

        }
    }

    //searching event and print a table (no navigation)
    public static void searchAndDisplayMatchingEvent(Scanner scanner, String filename) {
        LocalDate currentDate = LocalDate.now();
        var terminalGrid = TerminalGrid.create();
        ResourceBundle messages = ResourceBundle.getBundle("Messages", selectedLanguage);

        // Prompt the user for the search term
        System.out.print(messages.getString("searching.message"));
        String searchTerm = scanner.nextLine().trim().toLowerCase();

        boolean found = false;
        Event eventFound = null;

        for (Event event : calendarAPI.getEvents()) {
            if (event.getStartDate().isAfter(currentDate) || event.getStartDate().isEqual(currentDate)) {
                if (event.getTitle().toLowerCase().contains(searchTerm)) {
                    found = true;
                    eventFound = event;
                    currentDate = event.getStartDate();
                    break;
                }
            }
        }

        // Create a table to display dates
        List<List<String>> table = createCalendarTable(currentDate, filename);

        if (found) {
            displayMatchingEvent(eventFound);
        } else {
            System.out.println(messages.getString("nosearchfound.message"));
        }

        // Print the table
        terminalGrid.print(table);
    }

    

    //dispalying event details when event found 
    private static void displayMatchingEvent(Event event) {
        ResourceBundle messages = ResourceBundle.getBundle("Messages", selectedLanguage);

        System.out.println(messages.getString("searchfound.message"));
        System.out.println(event.toString(selectedLanguage));

    }


    public static void main(String[] args) throws InterruptedException {

        // setting the arguments
        String filename = null;
        if (args.length != 1) {
            System.out.println("Incorrect number of argument.Please provide 1 argument only.");

        } else {
            filename = args[0];

            int option = 0;
            Scanner sc = new Scanner(System.in);
            
            ResourceBundle messages = ResourceBundle.getBundle("Messages", Locale.getDefault());
            System.out.println("Enter the IETF language tag (e.g., en-AU, th-TH-u-nu-thai):");

            String langInput = sc.nextLine();
            selectedLanguage = Locale.forLanguageTag(langInput);

            messages = ResourceBundle.getBundle("Messages", selectedLanguage);

            String alldaymessage = messages.getString("notify.allday.message");
            String timemessage = messages.getString("notify.time.message");
            String exceptionMessage = messages.getString("exception.message") ;


            //thread for event notify
            Runnable eventChecker = new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        BlockingQueue<Event> events = calendarAPI.getEventsQueue();
                        
                        try {
                            Event eventNotify = events.take();
                            if (eventNotify.getStartTime().isEmpty()) {
                                System.out.println( alldaymessage + "\n "+ eventNotify.toString(selectedLanguage));
                            } else {
                                System.out.println( timemessage + "\n " + eventNotify.toString(selectedLanguage));
                            }

                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            System.out.println(exceptionMessage + e.getMessage());
                        }

                        try {
                            Thread.sleep(1000); // Sleep for 1 second before checking again
                        } catch (InterruptedException e) {
                             System.out.println(exceptionMessage + e.getMessage());
                        }
                    }
                }
            };

            Thread eventCheckerThread = new Thread(eventChecker);
            eventCheckerThread.start();

            // looping for menu 
            do {
                try {
                    System.out.println(messages.getString("welcome.message"));
                    System.out.println(messages.getString("select.option"));
                    System.out.println(messages.getString("option.calendar"));
                    System.out.println(messages.getString("option.search"));
                    System.out.println(messages.getString("option.exit"));

                    String input = sc.nextLine();

                    if (!input.matches("\\d+")) {
                        System.out.println(messages.getString("invalid.input"));
                        continue;
                    }

                    option = Integer.parseInt(input);

                    switch (option) {
                        case 1:
                            // Display calendar
                            System.out.println(messages.getString("display.calendar.message"));
                            Thread.sleep(5000);
                            DisplayCalendar(sc, filename);
                            break;
                        case 2:
                            // Search event
                            searchAndDisplayMatchingEvent(sc, filename);
                            break;
                        case 3:
                            System.out.println(messages.getString("exit.message"));
                            Thread.sleep(5000);
                            System.exit(0);
                            break;
                        default:
                            System.out.println(messages.getString("invalid.input"));
                    }
                } catch (InputMismatchException e) {
                    System.out.println(messages.getString("invalid.input"));
                    sc.nextLine(); // Clear the invalid input from the Scanner
                }

            } while (option != 3);

            sc.close(); // Close the scanner after everything is done

        }

    }

}
