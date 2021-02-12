package com.caldev.cal.server;

import com.caldev.cal.db.dbConnect;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.AclRule;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * Class CLub is used to create/manage a Club/Group.
 * For us, group and club have this same class.
 * Date: Dec 22-2020.
 * @author CALDEV.
 * */
public class Club {
    /**
     * Club's name.
     */
    public String name;
    /**
     * Club's id.
     */
    private String id;
    /**
     * Club's owner email.
     */
    private final String ownerEmail;
    /**
     * Club if it is private or public.
     */
    private boolean priv = false;
    /**
     * Calendar to access in Google Calendar API.
     */
    private transient Calendar cal;

    /**
     * Constructor that creates a club.
     * @param name - name of the club.
     * @param em - email of the club owner.
     * @param cal - Calendar from the owner and where the Club belongs to.
     * @param p - Boolean that tells if the club is private or not.
     * @param db - DB Connection.
     * @throws IOException if I/O operations failed or were interrupted.
     * @throws SQLException if occurs an error at DB.
     */
    public Club(String name, String em, Calendar cal, boolean p, dbConnect db) throws IOException, SQLException {
        this.name=name;
        this.ownerEmail = em;
        this.cal = cal;
        this.priv = p;

        this.id = this.getCatID(name);

        if(id.equals("primary"))
            this.id = this.createCatg(name);

        if(!db.ClubExistsOrNot(this.id)) {
            db.addClub(this);
        }
    }

    /**
     * Simplified constructor, to be used with care.
     * @param name - Name of the club.
     * @param id - ID of the calendar associated with the Club.
     * @param email - email of the owner of the club.
     */
    public Club(String name, String id, String email) {
        this.name = name;
        this.id = id;
        this.ownerEmail = email;
    }

    /**
     * Method to fetches Club ID from the calendar.
     * @param catName - Name of the Club/Category.
     * @return the string ID of the desired calendar.
     * @throws IOException if I/O operations failed or were interrupted.
     */
    public String getCatID(String catName) throws IOException {
        List<CalendarListEntry> calItems = (this.getCal().calendarList().list().execute()).getItems();

        for(CalendarListEntry item : calItems)
        {
            if(catName.compareTo(item.getSummary())==0)
                return item.getId();
        }

        return "primary";
    }

    /**
     * Method to create a category/club on the google api.
     * @param catName - Name of the club.
     * @return - the ID of the newly created Club.
     * @throws IOException if I/O operations failed or were interrupted.
     */
    public String createCatg(String catName) throws IOException {
        com.google.api.services.calendar.model.Calendar calendar = new com.google.api.services.calendar.model.Calendar();
        calendar.setSummary(catName);
        com.google.api.services.calendar.model.Calendar createdCalendar = this.getCal().calendars().insert(calendar).execute();

        AclRule rule = new AclRule();
        AclRule.Scope scope = new AclRule.Scope();
        scope.setType("default");
        rule.setScope(scope).setRole("reader");
        // Insert new access rule
        this.getCal().acl().insert(createdCalendar.getId(), rule).execute();

        return createdCalendar.getId();
    }

    /**
     * Method to add the user to a public group.
     * @param gID - Desired group to be joined.
     * @throws IOException if I/O operations failed or were interrupted.
     */
    public void addMe2Group(String gID) throws IOException {
        CalendarListEntry newEntry = new CalendarListEntry();
        newEntry.setId(gID);
        this.getCal().calendarList().insert(newEntry).execute();
    }

    /**
     * Method to remove the user from the group.
     * @param gID - Desired group ID.
     * @throws IOException if I/O operations failed or were interrupted.
     */
    public void removeMeFromGroup(String gID) throws IOException {
        this.getCal().calendarList().delete(gID).execute();
    }

    /**
     * Method to get the name of the club.
     * @return the name of te club.
     */
    public String getName() { return this.name; }

    /**
     * Method to get the Calendar object of the club.
     * @return the calendar object of te club.
     */
    public Calendar getCal() { return this.cal; }

    /**
     * Method to get the ID of the club.
     * @return the ID of te club.
     */
    public String getID() { return id; }

    /**
     * Method to get the email of the owner of the club.
     * @return the email of the owner of the club.
     */
    public String getOwnerEmail() { return ownerEmail; }

    /**
     * Method to get the privacy policy of the club.
     * @return the boolean of privacy of the club.
     */
    public boolean isPriv() { return priv; }

    /**
     * Method to set the calendar of the club.
     * @param cal - Calendar that we want to associate with this Club.
     */
    public void setCal(Calendar cal) { this.cal = cal; }

    /**
     * Method to set the privacy policy of the club.
     * @param priv - Set this Club if it is private or public.
     */
    public void setPriv(boolean priv) {
        this.priv = priv;
    }

}
