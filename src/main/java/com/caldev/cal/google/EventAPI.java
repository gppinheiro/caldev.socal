package com.caldev.cal.google;

import com.caldev.cal.db.dbConnect;
import com.caldev.cal.server.Club;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.*;
import com.google.api.services.calendar.model.Event;
import com.google.gson.Gson;

import java.io.IOException;
import java.sql.SQLException;
import java.time.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This is a class that deals with event related methods from the Google Calendar API.
 * Date: Dec 5-2020.
 * @author CALDEV.
 */
public class EventAPI {
    /**
     * Calendar to access.
     */
    private transient Calendar cal;
    /**
     * Array List with the class printEvent. Help us to send information to Android App.
     */
    private final ArrayList<printEvent> evList = new ArrayList<>();

    /**
     * Constructor that creates simple object with calendar only.
     * @param cal Calendar of person.
     */
    public EventAPI(Calendar cal)
    {
        this.cal = cal;
    }

    /**
     * Constructor that creates object for the listing of events.
     * @param evL - List of events from a certain category.
     * @param catN - Name of the category.
     * @param cat - ID of category (used by Google API).
     */
    public EventAPI(List<Event> evL, String catN, String cat) {
        if(evL == null) return;
        else if(evL.isEmpty()) return;

        for(Event event : evL) {
            if(event.getRecurrence() == null)
                this.evList.add(new printEvent(event, catN, cat));
            else {
                String hlpr = event.getRecurrence().get(0);
                ZonedDateTime zone= ZonedDateTime.now();
                ZonedDateTime zdt = ZonedDateTime.parse(event.getStart().getDateTime().toStringRfc3339());
                ZonedDateTime zdtE = ZonedDateTime.parse(event.getEnd().getDateTime().toStringRfc3339());
                if(hlpr.contains("WEEKLY")) {
                    while( zdt.isBefore(zone))
                    {
                        zdt = zdt.plus(Period.ofWeeks(1));
                        zdtE = zdtE.plus(Period.ofWeeks(1));
                    }
                    for(int i=0; i<52; i++)
                    {
                        String dInit = fromZDT(zdt);
                        String dEnd = fromZDT(zdtE);

                        this.evList.add(new printEvent(event, catN, cat, dInit, dEnd));

                        zdt = zdt.plus(Period.ofWeeks(1));
                        zdtE = zdtE.plus(Period.ofWeeks(1));
                    }
                }
                //TODO: explode string and add month abd year to the next events
                else if(hlpr.contains("MONTHLY")) {
                    while( zdt.isBefore(zone)) {
                        zdt = zdt.plus(Period.ofMonths(1));
                        zdtE = zdtE.plus(Period.ofMonths(1));
                    }
                    for(int i=0; i<12; i++) {
                        String dInit = fromZDT(zdt);
                        String dEnd = fromZDT(zdtE);

                        this.evList.add(new printEvent(event, catN, cat, dInit, dEnd));

                        zdt = zdt.plus(Period.ofMonths(1));
                        zdtE = zdtE.plus(Period.ofMonths(1));
                    }
                }
                else if(hlpr.contains("YEARLY")) {
                    while( zdt.isBefore(zone)) {
                        zdt = zdt.plus(Period.ofYears(1));
                        zdtE = zdtE.plus(Period.ofYears(1));
                    }
                    for(int i=0; i<12; i++) {
                        String dInit = fromZDT(zdt);
                        String dEnd = fromZDT(zdtE);

                        this.evList.add(new printEvent(event, catN, cat, dInit, dEnd));

                        zdt = zdt.plus(Period.ofYears(1));
                        zdtE = zdtE.plus(Period.ofYears(1));
                    }
                }
            }
        }

    }

    /**
     * Return the calendar on this instance.
     * @return Calendar.
     */
    public Calendar getCal() { return cal; }

    /**
     * Fetches all categories names.
     * @return ArrayList of Strings with all the categories.
     * @throws IOException if I/O operations failed or were interrupted.
     */
    public ArrayList<String> getCatList() throws IOException {
        ArrayList<String> catList = new ArrayList<>();
        List<CalendarListEntry> calItems = (this.getCal().calendarList().list().execute()).getItems();

        for(CalendarListEntry item : calItems) { catList.add(item.getId()); }

        return catList;
    }

    /**
     * Fetches all the events from a certain category.
     * If null is used in the ArrayList, it fetches all the events, from all the categories.
     * @param catNames - ArrayList of Strings of all categories to be used.
     * @return JSON of all event data.
     * @throws IOException if I/O operations failed or were interrupted.
     */
    public String getEventList(ArrayList<String> catNames) throws IOException {
        DateTime now = new DateTime(System.currentTimeMillis());
        EventAPI finalList = new EventAPI(this.getCal());
        ArrayList<String> catIDs = new ArrayList<>();

        if(catNames == null)
            catIDs = this.getCatList();
        else{

            for(String name: catNames)
            {
                catIDs.add(getCatID(name));
            }
        }

        for(String str : catIDs) {
            if(!this.getCal().events().list(str).isEmpty()) {
                String catN = this.getCal().events().list(str).execute().getSummary();
                Events evS = this.getCal().events().list(str).setTimeMin(now).execute();
                EventAPI evL = new EventAPI(evS.getItems(),catN,  str);
                finalList.appendList(evL);
            }
        }

        finalList.sort();
        return new Gson().toJson(finalList);
    }

    /**
     * Get all next week events.
     * @return Event's list in a String.
     * @throws IOException if I/O operations failed or were interrupted.
     */
    public String getWeekList() throws IOException {
        DateTime now = new DateTime(System.currentTimeMillis());
        ZonedDateTime weekB = ZonedDateTime.parse(now.toStringRfc3339()).plus(Period.ofWeeks(1));
        long weekBL = weekB.toInstant().toEpochMilli();
        DateTime max = new DateTime(weekBL);

        EventAPI finalList = new EventAPI(this.getCal());

        ArrayList<String> catNames = this.getCatList();

        for(String str : catNames) {
            if (!this.getCal().events().list(str).isEmpty()) {
                String catN = this.getCal().events().list(str).execute().getSummary();
                Events evS = this.getCal().events().list(str).setTimeMin(now).setTimeMax(max).execute();
                EventAPI evL = new EventAPI(evS.getItems(), catN, str);
                finalList.appendList(evL);
            }
        }
        finalList.sort();
        return new Gson().toJson(finalList);
    }

    /**
     * Get all next month events.
     * @return Event's list in a String.
     * @throws IOException if I/O operations failed or were interrupted.
     */
    public String getMonthList() throws IOException {
        DateTime now = new DateTime(System.currentTimeMillis());
        ZonedDateTime monthB = ZonedDateTime.parse(now.toStringRfc3339()).plus(Period.ofMonths(1));
        long monthBL = monthB.toInstant().toEpochMilli();
        DateTime max = new DateTime(monthBL);

        EventAPI finalList = new EventAPI(this.getCal());

        ArrayList<String> catNames = this.getCatList();

        for(String str : catNames) {
            if (!this.getCal().events().list(str).isEmpty()) {
                String catN = this.getCal().events().list(str).execute().getSummary();
                Events evS = this.getCal().events().list(str).setTimeMin(now).setTimeMax(max).execute();
                EventAPI evL = new EventAPI(evS.getItems(), catN, str);
                finalList.appendList(evL);
            }
        }
        finalList.sort();
        return new Gson().toJson(finalList);
    }

    /**
     * Joins all the elements of the passed "list" to the current one.
     * @param adder - List of events.
     */
    public void appendList(EventAPI adder) { this.evList.addAll(adder.evList); }

    /**
     * Sorts the list of events on this object instance.
     */
    public void sort() {
        if(!this.evList.isEmpty())
            Collections.sort(this.evList);
    }

    /**
     * Transforms a data object of ZonedDateTIme into a string.
     * @param a - ZonedDateTime object.
     * @return a string version of the input.
     */
    public String fromZDT(ZonedDateTime a) {
        String aaa = a.toString();

        String str = aaa.substring(8, 10); //add day
        str += "-";
        str += aaa.substring(5, 7); //add month
        str += "-";
        str += aaa.substring(0, 4); //add year
        str += "T";
        str += aaa.substring(11, 16);

        return str;
    }

    /**
     * Not zoned DateTime object from the strings input.
     * not the timezone          dd-mm-yyyy    hh:mm.
     * @param date - date of format dd-mm-yyy.
     * @param time - time of format hh:mm.
     * @return EventDateTime Class.
     */
    public EventDateTime toEDT(String date, String time) {
        String fin = date.substring(6,10);
        fin += date.substring(2, 6);
        fin += date.substring(0, 2);
        fin += "T";

        fin += time;
        fin += ":00";

        return new EventDateTime().setDateTime(new DateTime(fin));
    }

    /**
     * Adds event to user calendar on specified category.
     * @param name - name of the event.
     * @param stTime - time the event starts hh:mm.
     * @param endTime - time the event ends hh:mm.
     * @param stDate - date the event starts dd-mm-yyyy.
     * @param endDate - date the event ends dd-mm-yyyy.
     * @param catName - Name of the category of the event.
     * @param email - User's Email.
     * @param db - DB Connection.
     * @throws IOException if I/O operations failed or were interrupted.
     * @throws SQLException if an error occurs in DB.
     */
    public void addEvent(String name, String stTime, String endTime, String stDate, String endDate, String catName, String email, dbConnect db) throws IOException, SQLException {
        EventDateTime zdt = toEDT(stDate, stTime);
        EventDateTime zdtE = toEDT(endDate, endTime);
        Event ev = new Event().setSummary(name).setStart(zdt).setEnd(zdtE);


        String catID = getCatID(catName);
        if (catName.equals("primary") || catName.equals("Primary")) catID = email;
        else if(catID.equals("primary"))
        {
            Club c = new Club(catName,email, this.getCal(), true, db);
            catID = c.getID();
            //Add user to a club in database
            db.addUserToClub(email,catName,catID);

        }

        this.getCal().events().insert(catID, ev).execute();
    }

    /**
     * Deletes event from the user calendar.
     * @param eventID - ID of the event to be deleted.
     * @param catName - category of the event.
     * @throws IOException if I/O operations failed or were interrupted.
     */
    public void deleteEvent(String eventID, String catName) throws IOException {
        String calID = this.getCatID(catName);
        this.getCal().events().delete(calID, eventID).execute();
    }

    /**
     * Edit events parameters.
     * @param name - name of the event.
     * @param stTime - start time of the event.
     * @param endTime - time the event ends hh:mm.
     * @param stDate - date the event starts dd-mm-yyyy.
     * @param endDate - date the event ends dd-mm-yyyy.
     * @param catName - name of the category.
     * @param eventID - ID of the event.
     * @throws IOException if I/O operations failed or were interrupted.
     */
    public void editEvent(String name, String stTime, String endTime, String stDate, String endDate, String catName, String eventID) throws IOException {
        String catID = this.getCatID(catName);
        EventDateTime zdt = toEDT(stDate, stTime);
        EventDateTime zdtE = toEDT(endDate, endTime);

        Event ev = this.getCal().events().get(catID, eventID).execute();

        ev.setStart(zdt).setEnd(zdtE);

        if(ev.getSummary().compareTo(name) != 0)
            ev.setSummary(name);

        this.getCal().events().update(catID, ev.getId(), ev).execute();
        ev.getUpdated();
    }

    /**
     * Fetches category ID.
     * @param catName - category name to search.
     * @return ID of the searched category.
     * @throws IOException if I/O operations failed or were interrupted.
     */
    public String getCatID(String catName) throws IOException {
        List<CalendarListEntry> calItems = (this.getCal().calendarList().list().execute()).getItems();

        for(CalendarListEntry item : calItems) {
            if(catName.compareTo(item.getSummary())==0)
                return item.getId();
        }

        return "primary";
    }

    /**
     * Small class to contain event details to be passed to the app.
     */
    public static class printEvent implements Comparable<printEvent> {
        String name;
        String initTime;
        String endTime;
        String id;
        transient String catg;
        String catN;

        /**
         * Constructor of printEvent.
         * @param evn - Event from google API.
         * @param catN - category name.
         * @param cat - category id.
         */
        public printEvent(com.google.api.services.calendar.model.Event evn, String catN, String cat) {
            this.name = evn.getSummary();

            if (evn.getStart().getDateTime() == null) {
                evn.getStart().getDate().getValue();
                this.initTime = fromZDT(evn.getStart().getDate().toStringRfc3339(), 0);
                this.endTime = fromZDT(evn.getStart().getDate().toStringRfc3339(), 0);
            }
            else {
                this.initTime = fromZDT(evn.getStart().getDateTime().toStringRfc3339(), evn.getStart().getDateTime().getTimeZoneShift());
                this.endTime = fromZDT(evn.getEnd().getDateTime().toStringRfc3339(), evn.getStart().getDateTime().getTimeZoneShift());
            }

            this.id = evn.getId();
            this.catg = cat;
            this.catN = catN;
        }

        /**
         * Constructor of printEvent that overrides the event DateTime.
         * @param evn - Event from google API.
         * @param catN - category name.
         * @param cat - category id.
         * @param st - init time (time+date).
         * @param end - end time (time + date).
         */
        public printEvent(Event evn, String catN, String cat, String st, String end) {
            this.name = evn.getSummary();
            this.initTime = st;
            this.endTime = end;
            this.id = evn.getId();
            this.catg = cat;
            this.catN = catN;
        }

        /**
         * Transforms ZDT string to Android format.
         * @param aaa - String of time from ZDT.
         * @param timeZ - Timezone offset in minutes.
         * @return a string with time format.
         */
        public String fromZDT(String aaa, int timeZ) {
            timeZ *= -1;
            int hA = timeZ / 60;
            int mA = timeZ % 60;

            if(aaa.length() < 16) aaa+= "T00:00";

            String str = aaa.substring(8, 10); //add day
            str += "-";
            str += aaa.substring(5, 7); //add month
            str += "-";
            str += aaa.substring(0, 4); //add year

            str += "T";

            int h = Integer.parseInt(aaa.substring(11, 13));
            int m = Integer.parseInt(aaa.substring(14, 16));

            h += hA;
            m += mA;

            if(h < 10)  str += "0";
            str += Integer.toString(h);

            str += ":";

            if(m < 10)  str += "0";
            str += Integer.toString(m);

            return str;
        }

        /**
         * Needed to order events by their starting date.
         * @param o - object to be compares.
         * @return the normal compare format.
         */
        @Override
        public int compareTo(printEvent o) {
            int x = this.initTime.substring(6, 10).compareTo(o.initTime.substring(6, 10)); //compare year
            if( x != 0) return x;

            x = this.initTime.substring(3, 5).compareTo(o.initTime.substring(3, 5)); //compare month
            if( x != 0) return x;

            x = this.initTime.substring(0, 2).compareTo(o.initTime.substring(0, 2)); //compare day
            if( x != 0) return x;


            x = this.initTime.substring(11, 16).compareTo(o.initTime.substring(11, 16)); //compare hour+min
            return x;
        }
    }

}
