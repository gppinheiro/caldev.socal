package com.example.calldev.item;

/**
 * Date: Nov 28-2020.
 * This is a class represents the profile and settings items.
 * @author CALDEV.
 */
public class ProfileItem
{
    /**
     * Integer value of the user's photo.
     */
    private int photo;
    /**
     * String value of the specific profile item name.
     */
    private final String text;
    /**
     * String value of the user's email address.
     */
    private String email;
    /**
     * String value of specific profile item data.
     */
    private String data;
    /**
     * Boolean value of the setting's state (on or off).
     */
    private boolean state;
    /**
     * Integer value to signal that a specific profile item is part of the profile or settings (1 or 2).
     */
    private int flag;

    /**
     * Constructor used for the profile picture and name.
     * @param imageResource ImageResource that symbolizes the user's photo.
     * @param username the username of the user.
     * @param email the email address of the user.
     */
    public ProfileItem(int imageResource, String username, String email)
    {
        this.photo = imageResource;
        this.text = username;
        this.email = email;
    }

    /**
     * Constructor used for the remaining profile item's.
     * @param item the specific item in the profile page (age, height,...).
     * @param data the data that is added to this item by the user.
     * @param flag an integer to signal that this item is part of the profile.
     */
    public ProfileItem(String item, String data, int flag)
    {
        this.text = item;
        this.data = data;
        this.flag = flag;
    }

    /**
     * Constructor used for the settings item's in the profile.
     * @param item the specific settings item in the profile page.
     * @param state the state of the setting.
     * @param flag an integer to signal that this item is part of the settings.
     */
    public ProfileItem(String item, boolean state, int flag)
    {
        this.text = item;
        this.state = state;
        this.flag = flag;
    }

    /**
     * gets the photo of the user.
     * @return the photo.
     */
    public int getPhoto()
    {
        return photo;
    }

    /**
     * gets the username of the user.
     * @return the username.
     */
    public String getText()
    {
        return text;
    }

    /**
     * gets the email address of the user.
     * @return the email address.
     */
    public String getSubText()
    {
        return email;
    }

    /**
     * gets the data of the profile item.
     * @return the data.
     */
    public String getData()
    {
        return data;
    }

    /**
     * get the state of the settings item.
     * @return the state.
     */
    public boolean getState()
    {
        return state;
    }

    /**
     * gets the flag of the item, differentiating it between the profile's and settings' item.
     * @return the flag (1 or 2).
     */
    public int getFlag()
    {
        return flag;
    }

    /**
     * sets the data of the profile item.
     * @param data the data.
     */
    public void setData(String data)
    {
        this.data = data;
    }

    /**
     * sets the state of the settings item.
     * @param state the boolean of the item.
     */
    public void setState(boolean state)
    {
        this.state = state;
    }
}
