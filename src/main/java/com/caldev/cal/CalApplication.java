package com.caldev.cal;

import com.caldev.cal.db.dbConnect;
import com.caldev.cal.google.LoginAccount;
import com.caldev.cal.server.Club;
import com.caldev.cal.server.Profile;
import com.caldev.cal.server.User;
import com.google.gson.Gson;
import org.json.JSONObject;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Principal/Main class, which gives access to our REST API.
 * Date: Nov 20-2020.
 * @author CALDEV.
 */
@SpringBootApplication
@RestController
@RequestMapping("/api")
public class CalApplication {

    /**
     * Variable to store the connection to DataBase.
     * Public - It can be access by everyone.
     * Static - Never changes.
     */
    public static dbConnect db;

    /**
     * SpringBoot and DB connection init.
     * @param args - args from system.
     */
    public static void main(String[] args) {
        SpringApplication.run(CalApplication.class, args);
        db = new dbConnect();
    }

    /**
     * Constructor to init DB Connection, if necessary.
     */
    public CalApplication() {
        if(db==null) db = new dbConnect();
    }

    /**
     * Method to show a default String.
     * @return default String.
     */
    @GetMapping("")
    public String defaultroot() { return "CalDev - LPRO 2020/2021 - Gonçalo Santos, Guilherme Pinheiro, Jorge Natal, José Magalhães."; }

    /**
     * Method to create a new login account.
     * @param log - LoginAccount Class.
     * @throws IOException if I/O operations failed or were interrupted.
     * @throws GeneralSecurityException if occurs any type of insecurity.
     * @throws SQLException if occurs an error at DB.
     */
    @PostMapping(path ="/login",consumes = "application/json")
    public void login(@RequestBody LoginAccount log) throws IOException, GeneralSecurityException, SQLException {
        LoginAccount login = new LoginAccount(log);
        login.CreateCredential();
        if (db.newUser(log.getEmail(),0)) { db.addToken(login); }
        else {
            if (!db.verifyTokenIsOK(login.getEmail(),login.getToken())) { db.updateToken(login); }
        }
        if (log.getEmail().equals("caldev.app@gmail.com")) updateMainPublicEvents();
        else {
            ArrayList<String> al = getMyCatIDs(log.getEmail());
            ArrayList<String> al2 = getMyCats(log.getEmail());
            int i=0;
            for(String gID : al) {
                String gName = al2.get(i);
                if(!db.ClubExistsOrNot(gID) && !gName.equals("Primary")) {
                    User usr = new User(login);
                    //Add newClub
                    new Club(gName,log.getEmail(),usr.getCal(),true, db);
                    db.addUserToClub(log.getEmail(),gName,gID);
                }
                i++;
            }
        }
    }

    /**
     * Method to update Main Public Events if necessary.
     * @throws SQLException if occurs an error at DB.
     * @throws IOException if I/O operations failed or were interrupted.
     * @throws GeneralSecurityException if occurs any type of insecurity.
     */
    public void updateMainPublicEvents() throws SQLException, IOException, GeneralSecurityException {
        String appAccName = "caldev.app@gmail.com";
        ArrayList<String> catList = getMyCats(appAccName);

        for (String cat : catList) {
            if(!cat.equals(appAccName)) {
                //Login Fiction
                String tok = db.getToken(appAccName);
                LoginAccount login = new LoginAccount(appAccName, tok);
                //User Fiction
                User usr = new User(login);
                //Add newClub
                Club c = new Club(cat,appAccName,usr.getCal(),false,getDb());
                //Add user to a club in database
                db.addUserToClub(appAccName,cat,c.getID());
            }
        }
    }

    /**
     * Method to verify if user Exists or not. If not, we add into DB what his email is, put him not premium user and notifications options will be default (both false).
     * @param email - User's email.
     * @return a boolean if user exists or not (in a JSON).
     * @throws SQLException if occurs an error at DB.
     */
    @PostMapping(path="/UserExistsOrNot",consumes="application/json",produces="application/json")
    public String userExists(@RequestBody String email) throws SQLException {
        boolean b= !db.newUser(email,1);
        if(!b) {
            db.addEmail(email);
            db.addPremiumUser(email,false);
            db.addNotification(email,false,false);
        }
        return new Gson().toJson(b);
    }

    /**
     * Method to verify if user is premium or not.
     * @param email - User's email.
     * @return a boolean if user is premium or not (in a JSON).
     * @throws SQLException if occurs an error at DB.
     */
    @PostMapping(path="/PremiumUserOrNot",consumes="application/json",produces = "application/json")
    public String userPremium(@RequestBody String email) throws SQLException {
        boolean b= db.premiumUser(email);
        return new Gson().toJson(b);
    }

    /**
     * Method to add a premium user.
     * @param em - User's email.
     */
    @PostMapping(path ="/AddPremiumUser",consumes="application/json")
    public void addPremiumUser(@RequestBody String em) {
        try { db.addPremiumUser(em,true);
        } catch (SQLException e) {e.printStackTrace();}
    }

    /**
     * Method to remove a premium user.
     * @param em - User's email.
     */
    @PostMapping(path="/RemovePremiumUser",consumes="application/json")
    public void removePremiumUser(@RequestBody String em) {
        try { db.addPremiumUser(em,false);
        } catch (SQLException e) {e.printStackTrace();}
    }

    /**
     * Method to add a new profile.
     * @param prof - Profile Class.
     */
    @PostMapping(path ="/AddProfile",consumes="application/json")
    public void addProfile(@RequestBody Profile prof) {
        try {
            Profile profile = new Profile(prof);
            profile.addIntoDB(db);
        } catch (SQLException e) {e.printStackTrace(); }
    }

    /**
     * Method to delete a profile, with a specific user.
     * @param email - User's email.
     */
    @PostMapping(path="/DeleteProfile",consumes="application/json")
    public void deleteProfile(@RequestBody String email) {
        email = email.replace("\"", "");
        try { db.deleteProfile(email); }
        catch (SQLException e) { e.printStackTrace(); }
    }

    /**
     * Method to get a profile of a user.
     * @param email - User's email.
     * @return a json with profile of this specific user.
     */
    @PostMapping(path="/GetProfile",consumes="application/json",produces="application/json")
    public String getProfile(@RequestBody String email) { return db.getProfile(email); }

    /**
     * Method to get all event list of a user.
     * @param email - user's email.
     * @return a json with all events.
     */
    @PostMapping(path="/GetAllEventList",consumes="application/json", produces="application/json")
    public String getAllEventList(@RequestBody String email) {
        try {
            email = email.replace("\"", "");
            String tok = db.getToken(email);
            LoginAccount login = new LoginAccount(email,tok);
            User usr = new User(login);

            return usr.getEventList();
        } catch (SQLException | IOException | GeneralSecurityException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }

    /**
     * Method to get events from a specific group of a user.
     * @param json - JSON with all information needed (own email, group name)
     * @return a json with all events.
     */
    @PostMapping(path="/GetEventListFromCatg",consumes="application/json", produces="application/json")
    public String getEventsFromCatg(@RequestBody String json) {
        try {
            String email = null;
            String catName = null;
            json = json.replace("{", "");
            json = json.replace("}", "");
            String[] parts = json.split(",");

            for (String line : parts) {
                String[] words = line.split(":");

                words[0] = words[0].replace("\"", "");
                words[1] = words[1].replace("\"", "");

                switch (words[0]) {
                    case "email":
                        email = words[1];
                        break;
                    case "catName":
                        catName = words[1];
                        break;
                }
            }

            String tok = db.getToken(email);
            LoginAccount login = new LoginAccount(email,tok);
            User usr = new User(login);

            assert catName != null;
            return usr.getEventListFromCatg(catName);
        } catch (SQLException | IOException | GeneralSecurityException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }

    /**
     * Method to add a new event on User's Calendar.
     * @param json - JSON with all information about event (email,name,startTime,endTime,startDate,endDate and category name).
     */
    @PostMapping(path="/AddEvent",consumes="application/json")
    public void addEvent (@RequestBody String json) {
        try {
            //Format JSON
            String email = null;
            String name = null;
            String stTime = null;
            String endTime = null;
            String stDate = null;
            String endDate = null;
            String catName = null;


            json = json.replace("{", "");
            json = json.replace("}", "");
            String[] parts = json.split(",");

            for (String line : parts) {
                String[] words = line.split(":");

                words[0] = words[0].replace("\"","");
                words[1] = words[1].replace("\"","");

                switch (words[0]) {
                    case "email":
                        email = words[1];
                        break;
                    case "name":
                        name = words[1];
                        break;
                    case "stTime":
                        stTime = line.substring(9);
                        stTime = stTime.replace("\"", "");
                        break;
                    case "endTime":
                        endTime = line.substring(10);
                        endTime = endTime.replace("\"", "");
                        break;
                    case "stDate":
                        stDate = words[1];
                        break;
                    case "endDate":
                        endDate = words[1];
                        break;
                    case "catName":
                        catName = words[1];
                        break;
                }
            }


            //Login Fiction
            String tok = db.getToken(email);
            LoginAccount login = new LoginAccount(email, tok);
            //User Fiction
            User usr = new User(login);
            //Add Event
            usr.addEvent(name, stTime, endTime, stDate, endDate, catName, db);
        } catch (SQLException | IOException | GeneralSecurityException throwables) {
            throwables.printStackTrace();
        }
    }

    /**
     * Method to delete a specific event with email, eventID and catName informations that comes from Android App.
     * @param json - JSON with event's informations.
     */
    @PostMapping(path="/DeleteEvent",consumes="application/json")
    public void deleteEvent (@RequestBody String json) {
        try {
            //Get message from user
            String email = "";
            String eventID = "";
            String catName = "";
            json = json.replace("{", "");
            json = json.replace("}", "");
            String[] parts = json.split(",");

            for (String line : parts) {
                String[] words = line.split(":");

                words[0] = words[0].replace("\"","");
                words[1] = words[1].replace("\"","");

                switch (words[0]) {
                    case "email":
                        email = words[1];
                        break;
                    case "id":
                        eventID = words[1];
                        break;
                    case "catName":
                        catName = words[1];
                        break;
                }
            }

            //Get user
            String tok = db.getToken(email);
            LoginAccount login = new LoginAccount(email, tok);

            User usr = new User(login);
            usr.deleteEvent(eventID, catName);

        } catch (SQLException | IOException | GeneralSecurityException throwables) {
            throwables.printStackTrace();
        }
    }

    /**
     * Method to edit a specific event.
     * @param json - JSON with all informations about an event.
     */
    @PostMapping(path="/EditEvent",consumes="application/json")
    public void editEvent (@RequestBody String json) {
        try {
            //Format JSON
            String email = null;
            String name = null;
            String stTime = null;
            String endTime = null;
            String stDate = null;
            String endDate = null;
            String catName = null;
            String eventID = null;

            json = json.replace("{", "");
            json = json.replace("}", "");
            String[] parts = json.split(",");

            for (String line : parts) {
                String[] words = line.split(":");

                words[0] = words[0].replace("\"","");
                words[1] = words[1].replace("\"","");

                switch (words[0]) {
                    case "email":
                        email = words[1];
                        break;
                    case "name":
                        name = words[1];
                        break;
                    case "stTime":
                        stTime = line.substring(9);
                        stTime = stTime.replace("\"", "");
                        break;
                    case "endTime":
                        endTime = line.substring(10);
                        endTime = endTime.replace("\"", "");
                        break;
                    case "stDate":
                        stDate = words[1];
                        break;
                    case "endDate":
                        endDate = words[1];
                        break;
                    case "catName":
                        catName = words[1];
                        break;
                    case "id":
                        eventID = words[1];
                        break;
                }
            }

            //Login Fiction
            String tok = db.getToken(email);
            LoginAccount login = new LoginAccount(email, tok);
            //User Fiction
            User usr = new User(login);
            //Add Event
            usr.editEvent(name, stTime, endTime, stDate, endDate, catName, eventID);
        } catch (SQLException | IOException | GeneralSecurityException throwables) {
            throwables.printStackTrace();
        }
    }

    /**
     * Method to create a new club.
     * @param json - JSON with email, name, ownerEmail and if private or not.
     * @return clubID in a json.
     */
    @PostMapping(path="/NewClub",consumes="application/json",produces="application/json")
    public String newClub (@RequestBody String json) {
        try {
            //Format JSON
            String email = null;
            String name = null;
            String ownerEmail = null;
            boolean priv = false;

            json = json.replace("{", "");
            json = json.replace("}", "");
            String[] parts = json.split(",");

            for (String line : parts) {
                String[] words = line.split(":");

                words[0] = words[0].replace("\"","");
                words[1] = words[1].replace("\"","");

                switch (words[0]) {
                    case "email":
                        email = words[1];
                        break;
                    case "name":
                        name = words[1];
                        break;
                    case "ownerEmail":
                        ownerEmail = words[1];
                        break;
                    case "private":
                        priv = words[1].equals("true");
                        break;
                }
            }

            //Login Fiction
            String tok = db.getToken(email);
            LoginAccount login = new LoginAccount(email, tok);
            //User Fiction
            User usr = new User(login);
            //Add newClub
            Club c = new Club(name,ownerEmail,usr.getCal(),priv,getDb());
            //Add user to a club in database
            db.addUserToClub(ownerEmail,name,c.getID());
            return new Gson().toJson(c.getID());
        } catch (SQLException | GeneralSecurityException | IOException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }

    /**
     * Method to add a user to a public group/club.
     * @param json - JSON with user's email, club's name and club's id.
     */
    @PostMapping(path="/Add2NewGroup",consumes="application/json")
    public void addToTheGroup (@RequestBody String json) {
        try {
            String[] data = formatJSONGroup(json);
            //Login Fiction
            data[0] = data[0].replace("\"","");
            String tok = db.getToken(data[0]);
            LoginAccount login = new LoginAccount(data[0], tok);
            //User Fiction
            User usr = new User(login);
            //Add newClub
            Club c = new Club(data[1],data[2],"@google.com");
            c.setCal(usr.getCal());

            boolean b = data[3].equals("true");
            c.setPriv(b);

            c.addMe2Group(data[2]);
            usr.addNewGroup(data[2]);

            db.addUserToClub(data[0],data[1],data[2]);
        } catch (SQLException | GeneralSecurityException | IOException throwables) {
            throwables.printStackTrace();
        }
    }

    /**
     * Method to remove a user from a public group/club.
     * @param json - JSON with user's email, club's name and club's id.
     */
    @PostMapping(path="/RemoveMeFromGroup",consumes="application/json")
    public void removeMeFromGroup (@RequestBody String json) {
        try {
            String[] data = formatJSONGroup(json);
            //Login Fiction
            //data[0] = data[0].substring(1,data[0].length()-1);
            String tok = db.getToken(data[0]);
            LoginAccount login = new LoginAccount(data[0], tok);
            //User Fiction
            User usr = new User(login);
            //Add newClub
            Club c = new Club(data[1],data[2],"@google.com");
            c.setCal(usr.getCal());
            if(db.isOwner(data[0],data[1])) db.removeOwnerInClub(data[0],data[1], data[2]);
            else
            {
                db.removeUserInClub(data[0],data[1]);
                c.removeMeFromGroup(data[2]);
            }
        } catch (SQLException | GeneralSecurityException | IOException throwables) {
            throwables.printStackTrace();
        }
    }

    /**
     * Method to help formatting a specific JSON.
     * @param json - JSON that comes from Android App.
     * @return a String array with different values like email, name, id and private.
     */
    public String[] formatJSONGroup(String json) {
        String[] s = new String[5];

        json = json.replace("{", "");
        json = json.replace("}", "");
        String[] parts = json.split(",");

        for (String line : parts) {
            String[] words = line.split(":");

            words[0] = words[0].replace("\"","");
            words[1] = words[1].replace("\"","");

            switch (words[0]) {
                case "email":
                    s[0] = words[1];
                    break;
                case "name":
                    s[1] = words[1];
                    break;
                case "id":
                    s[2] = words[1];
                    break;
                case "private":
                    s[3] = words[1];
                    break;
            }
        }

        return s;
    }

    /**
     * Method to add other user (inviting him) to a specific club.
     * @param json - JSON with user's email, email that we want to add and club's id.
     */
    @PostMapping(path="/AddOtherClub",consumes="application/json")
    public void addOtherClub(@RequestBody String json) {
        try {
            //Format JSON
            String otherEmail = null;
            String clubID = null;

            json = json.replace("{", "");
            json = json.replace("}", "");
            String[] parts = json.split(",");

            for (String line : parts) {
                String[] words = line.split(":");

                words[0] = words[0].replace("\"","");
                words[1] = words[1].replace("\"","");

                switch (words[0]) {
                    case "otherEmail":
                        otherEmail = words[1];
                        break;
                    case "clubID":
                        clubID = words[1];
                        break;
                }
            }

            //Login Fiction
            String tok = db.getToken(otherEmail);
            LoginAccount login = new LoginAccount(otherEmail, tok);
            //User Fiction
            User usr = new User(login);
            usr.addNewGroup(clubID);
            db.addUserToClub(otherEmail, db.clubName(clubID),clubID);

        } catch (SQLException | IOException | GeneralSecurityException throwables) {
            throwables.printStackTrace();
        }
    }

    /**
     * Method to get all users from a group with a Club's id.
     * @param clubid - Club's id.
     * @return All users from a group.
     * @throws SQLException if occurs an error at DB.
     */
    @PostMapping(path="/GetAllUsersFromGroup",consumes = "application/json", produces="application/json")
    public String getAllUsersFromGroup(@RequestBody String clubid) throws SQLException { return new Gson().toJson(db.getUsersFromGroup(clubid)); }

    /**
     * Method to get all public groups from DB.
     * @return json with all public groups.
     */
    @GetMapping("/GetAllPublicGroups")
    public String getAllPublicGroups() {
        try {
            return new Gson().toJson(db.ClubsPublic());
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }

    /**
     * Method to get all user's clubs.
     * @param email - User's email.
     * @return json with all user's clubs.
     */
    @PostMapping(path= "/GetMyClubs",consumes="application/json",produces="application/json")
    public String getMyClubs(@RequestBody String email) {
        try {
            email = email.replace("\"", "");
            String tok = db.getToken(email);
            LoginAccount login = new LoginAccount(email, tok);
            User usr = new User(login);

            return new Gson().toJson(usr.getMyClubs(getDb()));
        } catch (SQLException | GeneralSecurityException | IOException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }

    /**
     * Method to auxiliar in order to get all category names in user's calendar.
     * @param email - User's email.
     * @return ArrayList with all category names.
     */
    public ArrayList<String> getMyCats(String email) {
        try {
            //Login Fiction
            String tok = db.getToken(email);
            LoginAccount login = new LoginAccount(email, tok);
            //User Fiction
            User usr = new User(login);
            //Add Event
            return usr.getMyCats();
        } catch (SQLException | IOException | GeneralSecurityException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }

    /**
     * Method to auxiliar in order to get all category id's in user's calendar.
     * @param email - User's email.
     * @return ArrayList with all category names.
     */
    public ArrayList<String> getMyCatIDs(String email) {
        try {
            //Login Fiction
            String tok = db.getToken(email);
            LoginAccount login = new LoginAccount(email, tok);
            //User Fiction
            User usr = new User(login);
            //Add Event
            return usr.getMyCatIDs();
        } catch (SQLException | IOException | GeneralSecurityException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }

    /**
     * Method to put into DB what state is events and gym notifications (if true or not).
     * @param json - json that comes with user's email, boolean event nots and other boolean gym nots.
     * @throws SQLException if occurs an error at DB.
     */
    @PostMapping(path="/SendNotifications",consumes="application/json")
    public void sendNotification(@RequestBody String json) throws SQLException {
        String email = null;

        boolean en = false, gn = false;
        json = json.replace("{", "");
        json = json.replace("}", "");
        String[] parts = json.split(",");

        for (String line : parts) {
            String[] words = line.split(":");

            words[0] = words[0].replace("\"","");
            words[1] = words[1].replace("\"","");

            switch (words[0]) {
                case "email":
                    email = words[1];
                    break;
                case "hasEventnots":
                    en = words[1].equals("true");
                    break;
                case "hasGymnots":
                    gn = words[1].equals("true");
                    break;
            }
        }

        db.addNotification(email,en,gn);
    }

    /**
     * Method to send to Android APP what state is events and gym notifications (if true or not). This values are in DB.
     * @param email - User's email.
     * @return a json with if user's is premium, notification event is true or false and notification gym is true or false.
     * @throws SQLException if occurs an error at DB.
     */
    @PostMapping(path= "/GetNotifications",consumes="application/json",produces="application/json")
    public String getNotification(@RequestBody String email) throws SQLException {
        JSONObject obj = new JSONObject();

        String[] str = db.getNotification(email);

        obj.put("Premium", str[0]);
        obj.put("Event", str[1]);
        obj.put("Gym",str[2]);

        return obj.toString();
    }

    /**
     * Method to get DB connection.
     * @return dbConnect.
     */
    public static dbConnect getDb() {
        return db;
    }

}