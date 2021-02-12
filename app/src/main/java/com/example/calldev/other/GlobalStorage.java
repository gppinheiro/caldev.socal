package com.example.calldev.other;

import java.util.ArrayList;

/**
 * Date: Dec 9-2020.
 * Class used to store and access globally used variables throughout the app.
 * To access this class from everywhere just call GlobalStorage.getInstance().(method or variable).
 * More global variables could be added if needed.
 * @author CALDEV.
 */
public class GlobalStorage
{
    /**
     * Object of the GlobalStorage class.
     */
    private static GlobalStorage mInstance= null;

    /**
     * String value of the user's email address.
     */
    private String email = null;
    /**
     * Boolean value if the user has an associated profile or not.
     */
    private boolean hasProfile = false;
    /**
     * Boolean value if the user has premium status or not.
     */
    private boolean premium = false;
    /**
     * Boolean value if the user has Event notifications on or off.
     */
    private boolean eventnots = false;
    /**
     * Boolean value if the user has Gym notifications on or off.
     */
    private boolean gymnots = false;

    /**
     * club list of the user.
     */
    private ArrayList<String> clubList = null;

    /**
     * owners of club list of the user.
     */
    private ArrayList<String> clubOwnerList = null;

    /**
     * ID's of the club list of the user
     */
    private ArrayList<String> clubIDList = null;

    /**
     * Method used to access this class.
     */
    protected GlobalStorage(){}

    /**
     * Method used to access the variables of this class.
     * @return the mInstance object.
     */
    public static synchronized GlobalStorage getInstance()
    { if(null == mInstance) { mInstance = new GlobalStorage(); } return mInstance; }

    /**
     * gets the email address of the user.
     * @return the email address.
     */
    public String getEmail() {
        return email;
    }

    /**
     * gets the indication if the user has a profile.
     * @return if there is profile or not.
     */
    public boolean hasProfile() {
        return hasProfile;
    }

    /**
     * gets the status of the user (premium or not).
     * @return the premium status.
     */
    public boolean isPremium() {
        return premium;
    }

    /**
     * gets the status of the event notifications.
     * @return the event notifications status.
     */
    public boolean hasEventnots() {
        return eventnots;
    }

    /**
     * gets the status of the gym notifications.
     * @return the gym notifications status.
     */
    public boolean hasGymnots() {
        return gymnots;
    }

    /**
     * gets the list of clubs that the user is part of.
     * @return returns the list of clubs.
     */
    public ArrayList<String> getclubList() { return this.clubList;}

    /**
     * gets the ip address of the server.
     * @return the ip address.
     */
    public String getIpAddr() {
        return "https://socal-caldev.herokuapp.com/api";
    }

    /**
     * sets the email address of the user.
     * @param email the email address.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * sets the indication if the user has a profile.
     * @param hasProfile the status of profile.
     */
    public void setHasProfile(boolean hasProfile) {
        this.hasProfile = hasProfile;
    }

    /**
     * sets the status of the user.
     * @param premium the status of the user.
     */
    public void setPremium(boolean premium) {
        this.premium = premium;
    }

    /**
     * sets the status of the event notifications.
     * @param eventNots the status of the event notifications.
     */
    public void setEventNots(boolean eventNots) {
        this.eventnots = eventNots;
    }

    /**
     * sets the status of the gym notifications.
     * @param gymNots the status of the gym notifications.
     */
    public void setGymNots(boolean gymNots) {
        this.gymnots = gymNots;
    }

    /**
     * sets the list of clubs that the user is part of.
     * @param cl - ArrayList of strings with clubs.
     */
    public void setclubList(ArrayList<String> cl) {this.clubList = cl;}

    /**
     * sets the list of clubs' owner.
     * @param clubOwnerList - ArrayList of strings with clubs' owners.
     */
    public void setClubOwnerList(ArrayList<String> clubOwnerList) { this.clubOwnerList = clubOwnerList; }

}
