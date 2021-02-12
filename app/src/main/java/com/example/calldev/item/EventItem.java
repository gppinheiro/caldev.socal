package com.example.calldev.item;

/**
 * Date: Nov 28-2020.
 * This is a class represents the events.
 * @author CALDEV.
 */
public class EventItem
{
    /**
     * String value of the event's id.
     */
    private String id;
    /**
     * Integer value of the event's colour.
     */
    private int colour;
    /**
     * String value of the event's type.
     */
    private String type;
    /**
     * String value of the event's name.
     */
    private String name;
    /**
     * String value of the event's start time.
     */
    private String startTime;
    /**
     * String value of the event's start date.
     */
    private String startDate;
    /**
     * String value of the event's end time.
     */
    private String endTime;
    /**
     * String value of the event's end date.
     */
    private String endDate;

    /**
     * Constructor used for the events added by the user on the app.
     * @param imageResource colour that changes according with the event's type.
     * @param eventtype the username of the user.
     * @param eventname the email address of the user.
     * @param eventstarttime the start time of the event.
     * @param eventstartdate the start date of the event.
     * @param eventendtime the end time of the event.
     * @param eventenddate the end date of the event.
     */
    public EventItem(int imageResource, String eventtype, String eventname, String eventstarttime, String eventstartdate, String eventendtime, String eventenddate)
    {
        this.colour = imageResource;
        this.type = eventtype;
        this.name = eventname;
        this.startTime = eventstarttime;
        this.startDate = eventstartdate;
        this.endTime = eventendtime;
        this.endDate = eventenddate;
    }

    /**
     * Constructor used for the events edited by the user on the app.
     * @param imageResource colour that changes according with the event's type.
     * @param eventtype the username of the user.
     * @param eventname the email address of the user.
     * @param eventstarttime the start time of the event.
     * @param eventstartdate the start date of the event.
     * @param eventendtime the end time of the event.
     * @param eventenddate the end date of the event.
     * @param eventid the id of the event (this id is retrieved from the server and is crucial for the editing and deleting operations on the API level)
     */
    public EventItem(int imageResource, String eventtype, String eventname, String eventstarttime, String eventstartdate, String eventendtime, String eventenddate, String eventid)
    {
        this.colour = imageResource;
        this.type = eventtype;
        this.name = eventname;
        this.startTime = eventstarttime;
        this.startDate = eventstartdate;
        this.endTime = eventendtime;
        this.endDate = eventenddate;
        this.id = eventid;
    }

    /**
     * Method used to access this class.
    */
    public EventItem() {}

    /**
     * gets the colour of the event.
     * @return the colour.
     */
    public int getColour()
    {
        return colour;
    }

    /**
     * gets the type of the event.
     * @return the type.
     */
    public String getType()
    {
        return type;
    }

    /**
     * gets the name of the event.
     * @return the name.
     */
    public String getName()
    {
        return name;
    }

    /**
     * gets the id of the event.
     * @return the id.
     */

    public String getId()
    {
        return id;
    }
    /**
     * gets the start time of the event.
     * @return the start time.
     */

    public String getStartTime()
    {
        return this.startTime;
    }
    /**
     * gets the end time of the event.
     * @return the end time.
     */

    public String getEndTime()
    {
        return this.endTime;
    }
    /**
     * gets the start date of the event.
     * @return the start date.
     */

    public String getStartDate()
    {
        return this.startDate;
    }

    /**
     * gets the end date of the event.
     * @return the end date.
     */
    public String getEndDate()
    {
        return this.endDate;
    }

    /**
     * sets the name of the event.
     * @param name the name.
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * sets the id of the event.
     * @param id the id.
     */
    public void setId(String id)
    {
        this.id = id;
    }

    /**
     * sets the type of the event.
     * @param type the name.
     */
    public void setType(String type)
    {
        this.type = type;
    }

    /**
     * sets the start time of the event.
     * @param initTime the start time.
     */
    public void setInitTime(String initTime)
    {
        this.startTime = initTime;
    }

    /**
     * sets the end time of the event.
     * @param endTime the end time.
     */
    public void setEndTime(String endTime)
    {
        this.endTime = endTime;
    }

    /**
     * sets the start date of the event.
     * @param initDate the start date.
     */
    public void setInitDate(String initDate)
    {
        this.startDate = initDate;
    }

    /**
     * sets the end date of the event.
     * @param endDate the end date.
     */
    public void setEndDate(String endDate)
    {
        this.endDate = endDate;
    }

}
