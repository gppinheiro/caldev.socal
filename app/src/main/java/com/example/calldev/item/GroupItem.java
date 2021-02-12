package com.example.calldev.item;

/**
 * Date: Dec 5-2020.
 * This is a class represents the groups and it's users.
 * @author CALDEV.
 */
public class GroupItem
{
    /**
     * Integer value of the group's colour.
     */
    private int colour;
    /**
     * String value of the groups's owner email address or the specific status of the user inside a group (owner or member).
     */
    private String ownerEmail;
    /**
     * String value of the group's name.
     */
    private String name;
    /**
     * String value of the group's id.
     */
    private String id;
    /**
     * Boolean value of the group's state (public or private).
     */
    private boolean state;

    /**
     * Constructor used for the groups added by the user on the app.
     * @param imageResource colour that changes according with the group's type.
     * @param ownerEmail the email address of the user that created this group.
     * @param groupname the name of the group.
     * @param state the state of the group (public or private).
     */
    public GroupItem(int imageResource, String ownerEmail, String groupname, boolean state)
    {
        this.colour = imageResource;
        this.ownerEmail = ownerEmail;
        this.name = groupname;
        this.state = state;
    }

    /**
     * Constructor used for the groups edited by the user on the app.
     * @param imageResource colour that changes according with the group's type.
     * @param ownerEmail the email address of the user that created this group.
     * @param groupname the name of the group.
     * @param id the id of the group (this id is retrieved from the server and is crucial for the editing and deleting operations on the API level)
     * @param state the state of the group (public or private).
     */
    public GroupItem(int imageResource, String ownerEmail, String groupname, String id, boolean state)
    {
        this.colour = imageResource;
        this.ownerEmail = ownerEmail;
        this.name = groupname;
        this.id = id;
        this.state = state;
    }

    /**
     * Constructor used for the users that are part of a specific group.
     * @param imageResource the photo of the user.
     * @param personname the name of the user.
     * @param membership the status of the user in the group (owner or member).
     */
    public GroupItem(int imageResource, String personname, String membership)
    {
        this.colour = imageResource;
        this.name = personname;
        this.ownerEmail = membership;
    }

    /**
     * Method used to access this class.
     */
    public GroupItem() {}

    /**
     * gets the colour of the group or photo of it's users.
     * @return the colour or photo.
     */
    public int getColour()
    {
        return colour;
    }

    /**
     * gets the email address of the owner of the group or of it's users.
     * @return the email address.
     */
    public String getOwnerEmail()
    {
        return ownerEmail;
    }

    /**
     * gets the name of the group or of it's users.
     * @return the name.
     */
    public String getName()
    {
        return name;
    }

    /**
     * gets the id of the group.
     * @return the id.
     */
    public String getId() { return id; }

    /**
     * gets the state of the group (public or private).
     * @return the state.
     */
    public boolean isState() {
        return state;
    }

    /**
     * sets the state of the group (public or private).
     * @param state that state.
     */
    public void setState(boolean state) {
        this.state = state;
    }

    /**
     * sets the name of the group or of it's users.
     * @param name the name.
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * set the email address of the owner of the group or of it's users.
     * @param ownerEmail the email address.
     */
    public void setOwnerEmail(String ownerEmail)
    {
        this.ownerEmail = ownerEmail;
    }

    /**
     * set the id of the group.
     * @param id the id.
     */
    public void setId(String id) { this.id = id; }

}
