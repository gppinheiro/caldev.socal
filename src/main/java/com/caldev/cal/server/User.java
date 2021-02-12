package com.caldev.cal.server;

import com.caldev.cal.db.dbConnect;
import com.caldev.cal.google.EventAPI;
import com.caldev.cal.google.LoginAccount;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * This is a class that deals with user related methods.
 * It uses several methods from the Google API.
 * Date: Dec 28-2020.
 * @author CALDEV
 */
public class User {
    /**
     * User's Google Credential.
     */
    private final Credential cred;
    /**
     * User's Google email.
     */
    private final String email;
    /**
     * User's Google Calendar.
     */
    private final com.google.api.services.calendar.Calendar cal;

    /**
     * Constructor of new user without profile.
     * @param log Login account that contains the credentials of the user.
     * @throws SQLException if occurs an error at DB.
     */
    public User(LoginAccount log) throws SQLException {
        this.cred = log.getCredential();
        this.email = log.getEmail();
        this.cal = newCalendar(LoginAccount.getHttpTransport(), LoginAccount.getJsonFactory());
    }

    /**
     * Associates a calendar to the user.
     * @param HTTP_TRANSPORT - HttpTransport to establish connection with Google.
     * @param JSON_FACTORY - JsonFactory to establish connection with Google.
     * @return Calendar (class from Google API) from the user.
     */
    public Calendar newCalendar(HttpTransport HTTP_TRANSPORT, JsonFactory JSON_FACTORY) {
        return new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, this.cred)
                .setApplicationName("SOCal")
                .build();
    }

    /**
     * List of entries on the calendar.
     * @return List of CalendarListEntry of the calendar.
     * @throws IOException if I/O operations failed or were interrupted.
     */
    public List<CalendarListEntry> getCalendarList() throws IOException {
        return (this.cal.calendarList().list().execute()).getItems();
    }

    /**
     * List of all events from the user.
     * @return list of all events from the user.
     * @throws IOException if I/O operations failed or were interrupted.
     */
    public String getEventList() throws IOException {
        EventAPI a = new EventAPI(this.getCal());
        return a.getEventList(null);
    }

    /**
     * List to get all events with a category name.
     * @param catName - Category Name.
     * @return All Events in a String.
     * @throws IOException - if I/O operations failed or were interrupted.
     */
    public String getEventListFromCatg(String catName) throws IOException {
        EventAPI a = new EventAPI(this.getCal());
        if(catName.equals("_week_")) return a.getWeekList();
        else if(catName.equals("_month_")) return a.getMonthList();
        else {
            ArrayList<String> al = new ArrayList<>();
            al.add(catName);
            return a.getEventList(al);
        }
    }

    /**
     * Adds event to te user calendar.
     * @param name - Name of the event to be added.
     * @param stTime - Time the event starts.
     * @param endTime - Time the event ends.
     * @param stDate - Date the event starts.
     * @param endDate - Date the event ends.
     * @param catName - Category of the event to be added.
     * @param db - DB Connection.
     */
    public void addEvent(String name, String stTime, String endTime, String stDate, String endDate, String catName, dbConnect db) {
        try {
            EventAPI a = new EventAPI(this.getCal());
            a.addEvent(name,stTime,endTime,stDate,endDate,catName, this.getEmail(), db);
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Edits event parameters.
     * @param name - New name of the event.
     * @param stTime - New time the event starts.
     * @param endTime - New time the event ends.
     * @param stDate - New date the event starts.
     * @param endDate - New date the event ends.
     * @param catName - Category of the event to be edited.
     * @param eventID - ID of the event.
     */
    public void editEvent(String name, String stTime, String endTime, String stDate, String endDate, String catName, String eventID) {
        try {
            EventAPI a = new EventAPI(this.getCal());
            a.editEvent(name,stTime,endTime,stDate,endDate,catName, eventID);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Deletes event from the user calendar.
     * @param eventID - ID of the event to be deleted.
     * @param catName - Category of the event.
     */
    public void deleteEvent(String eventID, String catName) {
        try {
            EventAPI a = new EventAPI(this.getCal());
            a.deleteEvent(eventID, catName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns an arraylist of strings with all user categories.
     * @return ArrayList of String with all categories.
     * @throws IOException if I/O operations failed or were interrupted.
     */
    public ArrayList<String> getMyCats() throws IOException {
        ArrayList<String> alCat = new ArrayList<>();
        List<CalendarListEntry> objL = getCalendarList();

        for(CalendarListEntry cEntry : objL) {
            alCat.add(cEntry.getSummary());
        }

        return alCat;
    }

    /**
     * Gets the email address of the user.
     * @return email address of the user.
     */
    public String getEmail()
    {
        return this.email;
    }

    /**
     * Gets the calendar of the user.
     * @return calendar of the user.
     */
    public Calendar getCal() { return cal; }

    /**
     * Adds new group to the user account (where he is not the owner).
     * @param clubID - ID of the club to be added.
     * @throws IOException if I/O operations failed or were interrupted.
     */
    public void addNewGroup(String clubID) throws IOException {
       com.google.api.services.calendar.model.CalendarListEntry cle = new CalendarListEntry();
       cle.setId(clubID);
       this.getCal().calendarList().insert(cle).execute();
    }

    /**
     * Returns all clubs present in the user (inc "Primary").
     * @param db - DB Connection.
     * @return myClubList of all clubs from the user.
     * @throws IOException if I/O operations failed or were interrupted.
     * @throws SQLException if occurs an error at DB.
     */
    public myClubsList getMyClubs(dbConnect db) throws IOException, SQLException {
        myClubsList mcl = new myClubsList();
        List<CalendarListEntry> objL = getCalendarList();

        for (CalendarListEntry cat : objL) {
            String catName = cat.getSummary();
            String id = cat.getId();
            String owner = db.ownerEmail(id);
            if (owner == null) owner = "Group";

            if(catName.equals(this.getEmail())) catName = "Primary";
            else if(catName.equals("caldev.app@gmail.com")) catName = "SOCal";
            Club temp = new Club(catName, id, owner);
            mcl.userClubs.add(temp);
        }

        return mcl;
    }

    /**
     * Get User's Category IDs.
     * @return ArrayList String with User's Category IDs,
     * @throws IOException if I/O operations failed or were interrupted.
     */
    public ArrayList<String> getMyCatIDs() throws IOException {
        ArrayList<String> alCat = new ArrayList<>();
        List<CalendarListEntry> objL = getCalendarList();

        for(CalendarListEntry cEntry : objL) {
            alCat.add(cEntry.getId());
        }

        return alCat;
    }

    /**
     * Class that aids in the json creation, it has an arraylist of Clubs.
     */
    public static class myClubsList {
        ArrayList<Club> userClubs = new ArrayList<>();
    }

}
