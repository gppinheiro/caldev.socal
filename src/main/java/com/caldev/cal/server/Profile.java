package com.caldev.cal.server;

import com.caldev.cal.db.dbConnect;

import java.sql.SQLException;

/**
 * Class is used to create a User's Profile.
 * Date: Dec 22-2020.
 * @author CALDEV.
 * */
public class Profile {
    /**
     * User's email.
     */
    private final String email;
    /**
     * User's name.
     */
    private final String name;
    /**
     * User's weight.
     */
    private final Double weight;
    /**
     * User's height.
     */
    private final int height;
    /**
     * User's age.
     */
    private final int age;

    /**
     * Constructor that creates a profile.
     * @param email - User's email.
     * @param name - User's email.
     * @param weight - User's weight (kg).
     * @param height - User's height (cm).
     * @param age - User's age.
     */
    public Profile(String email, String name, Double weight, int height, int age) {
        this.email=email;
        this.name=name;
        this.weight=weight;
        this.height=height;
        this.age=age;
    }

    /**
     * Constructor that creates a profile.
     * @param p - Class Profile.
     */
    public Profile (Profile p) {
        this.email=p.email.replace("\"", "");
        this.name=p.name.replace("\"", "");
        this.weight=p.weight;
        this.height=p.height;
        this.age=p.age;
    }

    /**
     * Method to add into DataBase the profile.
     * @param db - dbConnect Class.
     * @throws SQLException if occurs an error in DB.
     */
    public void addIntoDB(dbConnect db) throws SQLException { db.addProfile(this); }

    /**
     * Method to override and Create an General String with Profile values.
     * @return profile string.
     */
    @Override
    public String toString() {
        return "Profile{" +
                "email='" + email + "\"" +
                ", name='" + name + "\"" +
                ", weight=" + weight +
                ", height=" + height +
                ", age=" + age +
                '}';
    }

    /**
     * Method to know BMI value based on kg and cm.
     * @param w - User's Weight.
     * @param h - User's Height.
     * @return BMI value.
     */
    public Double getBMI(Double w, int h) {
        String sValue = String.format("%.4f", w/(h*h*0.0001)).replace(",",".");
        return Double.parseDouble(sValue);
    }

    /**
     * Method to get User's email.
     * @return email associate with this profile.
     */
    public String getEmail() { return email; }

    /**
     * Method to get User's name.
     * @return name associate with this profile.
     */
    public String getName() { return name; }

    /**
     * Method to get User's weight.
     * @return weight associate with this profile.
     */
    public Double getWeight() { return weight; }

    /**
     * Method to get User's height.
     * @return height associate with this profile.
     */
    public int getHeight() { return height; }

    /**
     * Method to get User's age.
     * @return age associate with this profile.
     */
    public int getAge() { return age; }

}
