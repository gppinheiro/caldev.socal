package com.caldev.cal.db;

import com.caldev.cal.google.LoginAccount;
import com.caldev.cal.server.Club;
import com.caldev.cal.server.Profile;
import com.caldev.cal.server.User;
import com.google.gson.Gson;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.sql.*;
import java.util.ArrayList;

/**
 * Class dbConnect is used to: communicate with Heroku DataBase and create a Prepared Statements to send to the DB.
 * Date: Dec-7-2020.
 * @author CALDEV.
 */
public class dbConnect {
    /**
     * Store Heroku URI: User + Password + Host + Port + Database.
     * Private - Nobody needs to know what URI we are using.
     * Static and Final - Never changes.
     */
    private static final String heroku_url="postgres://cfrlqgvonyyach:2a16a2e6fc2049341d245f9560d02b18580050eda77c4b52db78291ed691aa31@ec2-54-160-133-106.compute-1.amazonaws.com:5432/dfjiem8rs6m7gk";
    /**
     * Store DB Connection.
     * Private - Nobody needs to know the db connection.
     */
    private Connection conn = null;

    /**
     * Constructor that creates connection with DataBase.
     */
    public dbConnect() {
        try {
            this.conn = getConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Method to make first connection.
     * @return DB Connection.
     * @throws URISyntaxException if occurs an URI error in syntax.
     * @throws SQLException if occurs an error in DB.
     */
    private static Connection getConnection() throws URISyntaxException, SQLException {
        URI dbUri = new URI(heroku_url);

        String username = dbUri.getUserInfo().split(":")[0];
        String password = dbUri.getUserInfo().split(":")[1];
        String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ':' + dbUri.getPort() + dbUri.getPath() + "?sslmode=require";

        return DriverManager.getConnection(dbUrl, username, password);
    }

    /**
     * Method to add a token into SQL Database.
     * @param log - class LoginAccount.
     * @throws SQLException if occurs an error in DB.
     */
    public void addToken(LoginAccount log) throws SQLException {
        PreparedStatement s = this.conn.prepareStatement("INSERT INTO login VALUES (?,?);");
        s.setString(1,log.getEmail());
        s.setString(2,log.getToken());
        s.executeUpdate();
    }

    /**
     * Method to get a specific token from SQL Database.
     * @param email - String with email on it.
     * @return an String that references the request token.
     * @throws SQLException if occurs an error in DB.
     */
    public String getToken(String email) throws SQLException {
        PreparedStatement s = this.conn.prepareStatement("SELECT * FROM login WHERE email='"+email+"';");
        ResultSet rs = s.executeQuery();
        while(rs.next()) {
            if(rs.getString(1).equals(email)) {
                return rs.getString(2);
            }
        }
        return null;
    }

    /**
     * Method to verify if a token exists in SQL Database.
     * Dependency with method getToken().
     * @param email - String with user's email.
     * @param token - String with user's token.
     * @return true if Token didn't change and false if needs to update.
     * @throws SQLException if occurs an error in DB.
     */
    public boolean verifyTokenIsOK(String email, String token) throws SQLException {
        String tok = getToken(email);
        return tok.equals(token);
    }

    /**
     * Method to update a token in SQL Database.
     * @param log - Class LoginAccount.
     * @throws SQLException if occurs an error in DB.
     */
    public void updateToken(LoginAccount log) throws SQLException {
        PreparedStatement s = this.conn.prepareStatement("UPDATE login SET token=(?) WHERE email='"+log.getEmail()+"';");
        s.setString(1,log.getToken());
        s.executeUpdate();
    }

    /**
     * Method to add an email into SQL Database.
     * @param e - String with email.
     * @throws SQLException if occurs an error in DB.
     */
    public void addEmail(String e) throws SQLException {
        PreparedStatement s = this.conn.prepareStatement("INSERT INTO email VALUES (?);");
        s.setString(1,e);
        s.executeUpdate();
    }

    /**
     * Method to verify if it's a new user or not.
     * @param e - String with user's email.
     * @param i - String with what table server wants (0 - login; 1 - email; 2 - profile).
     * @return true if it is a new user and false if not.
     * @throws SQLException if occurs an error in DB.
     */
    public boolean newUser(String e, int i) throws SQLException {
        PreparedStatement s = null;
        if (i==0) { s = this.conn.prepareStatement("SELECT email FROM login WHERE email='"+e+"';"); }
        if (i==1) { s = this.conn.prepareStatement("SELECT email FROM email WHERE email='"+e+"';"); }
        if (i==2) { s = this.conn.prepareStatement("SELECT email FROM profile WHERE email='"+e+"';"); }
        ResultSet rs = null;
        if (s != null) {
            rs = s.executeQuery();
        }

        if(rs!=null) {
            while(rs.next())
                if(rs.getString(1).equals(e)) return false;
        }

        return true;
    }

    /**
     * Method to add a new premium user (Or not). INSERT if does not exist. UPDATE if exist on database.
     * @param email - String with user's email.
     * @param bool - Premium User = true. if not = false.
     * @throws SQLException if occurs an error in DB.
     */
    public void addPremiumUser(String email, Boolean bool) throws SQLException {
        PreparedStatement s = this.conn.prepareStatement("SELECT email, premium FROM premium WHERE email='"+email+"';");
        ResultSet rs = s.executeQuery();

        if(!rs.next()) {
            s = this.conn.prepareStatement("INSERT INTO premium VALUES (?,?);");
            s.setString(1,email);
            s.setBoolean(2,bool);
        }
        else {
            s = this.conn.prepareStatement("UPDATE premium SET premium="+bool+" WHERE email='"+email+"';");
        }

        s.executeUpdate();
    }

    /**
     * Method to verify if it's a premium user or not.
     * @param e - String with user's email.
     * @return true if it is a premium user and false if not.
     * @throws SQLException if occurs an error in DB.
     */
    public boolean premiumUser(String e) throws SQLException {
        PreparedStatement s = this.conn.prepareStatement("SELECT email, premium FROM premium WHERE email='" + e + "';");
        ResultSet rs = s.executeQuery();

        while (rs.next()) {
            if (rs.getString(1).equals(e)) {
                return rs.getBoolean(2);
            }
        }

        return false;
    }

    /**
     * Method to add a profile into SQL Database.
     * @param prof - Class profile.
     * @throws SQLException if occurs an error in DB.
     */
    public void addProfile(Profile prof) throws SQLException {
        PreparedStatement s = this.conn.prepareStatement("INSERT INTO profile VALUES (?,?,?,?,?,?);");
        s.setString(1,prof.getEmail());
        s.setString(2,prof.getName());
        s.setInt(3,prof.getAge());
        s.setDouble(4,prof.getHeight());
        s.setDouble(5,prof.getWeight());
        s.setDouble(6,prof.getBMI(prof.getWeight(), prof.getHeight()));
        s.executeUpdate();
    }

    /**
     * Method to delete an profile in SQL Database.
     * @param email - String with email.
     * @throws SQLException if occurs an error in DB.
     */
    public void deleteProfile(String email) throws SQLException {
        PreparedStatement s = this.conn.prepareStatement("DELETE FROM profile WHERE email='"+email+"';");
        s.executeUpdate();
    }

    /**
     * Method to get a specific profile from SQL Database.
     * @param email - String with email on it.
     * @return an GSON that references the request profile.
     */
    public String getProfile(String email) {
        try {
            Gson gson = new Gson();
            Profile prof = null;

            PreparedStatement s = this.conn.prepareStatement("SELECT * FROM profile WHERE email='"+email+"';");
            ResultSet rs = s.executeQuery();

            while(rs.next()) {
                prof = new Profile(rs.getString(1), rs.getString(2), rs.getDouble(5), rs.getInt(4), rs.getInt(3));
            }

            return gson.toJson(prof);
        }
        catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }

    /**
     * Method to verify if this club exists or not.
     * @param id - String with club's ID.
     * @return true if club exists and false if not.
     * @throws SQLException if occurs an error in DB.
     */
    public boolean ClubExistsOrNot(String id) throws SQLException {
        PreparedStatement s = this.conn.prepareStatement("SELECT id FROM club WHERE id='"+id+"';");
        ResultSet rs = s.executeQuery();

        while(rs.next()) {
            String sr = rs.getString(1);
            if(sr.equals(id)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Method to add a Club into SQL Database.
     * @param club - Class Club.
     * @throws SQLException if occurs an error in DB.
     */
    public void addClub(Club club) throws SQLException {
        PreparedStatement s = this.conn.prepareStatement("INSERT INTO club VALUES (?,?,?,?);");
        s.setString(1,club.getName());
        s.setString(2, club.getID());
        s.setString(3,club.getOwnerEmail());
        s.setBoolean(4,club.isPriv());
        s.executeUpdate();
    }

    /**
     * Method to get all Clubs that are public from SQL Database.
     * @return all public clubs in an Class clubList that is an ArrayList.
     * @throws SQLException if occurs an error in DB.
     */
    public clubList ClubsPublic() throws SQLException {
        clubList clubs = new clubList();
        Club c;

        PreparedStatement s = this.conn.prepareStatement("SELECT * FROM club WHERE private=false;");
        ResultSet rs = s.executeQuery();

        while(rs.next()) {
            c = new Club(rs.getString(1),rs.getString(2),rs.getString(3));
            clubs.clubL.add(c);
        }

        return clubs;
    }

    /**
     * Method to add a new user to the club into SQL Database.
     * @param email - user's email String.
     * @param name - club's name String.
     * @param id - clubs id String.
     * @throws SQLException if occurs an error in DB.
     */
    public void addUserToClub(String email, String name, String id) throws SQLException {
        PreparedStatement s = this.conn.prepareStatement("INSERT INTO association VALUES (?,?,?);");
        s.setString(1,email);
        s.setString(2,name);
        s.setString(3,id);
        s.executeUpdate();
    }

    /**
     * Method to remove an user in a club in SQL Database.
     * @param email - String with email.
     * @param name - Clubs name.
     * @throws SQLException if occurs an error in DB.
     */
    public void removeUserInClub(String email, String name) throws SQLException {
        PreparedStatement s = this.conn.prepareStatement("DELETE FROM association WHERE member='"+email+"'AND groupname='"+name+"';");
        s.executeUpdate();
    }

    /**
     * Method to remove an user in a club in SQL Database.
     * @param email - String with owner email.
     * @param name - Club's name.
     * @param clubID - Club's ID.
     * @throws SQLException if occurs an error in DB.
     * @throws IOException if I/O operations failed or were interrupted.
     * @throws GeneralSecurityException if occurs any type of insecurity.
     */
    public void removeOwnerInClub(String email, String name, String clubID) throws SQLException, IOException, GeneralSecurityException {
        for(String user : getUsersFromGroup(clubID)) {
            String tok = this.getToken(user);
            LoginAccount login = new LoginAccount(user, tok);
            User usr = new User(login);
            Club c = new Club(name,clubID,"@google.com");
            c.setCal(usr.getCal());
            c.removeMeFromGroup(clubID);
            removeUserInClub(user, name);
        }

        PreparedStatement s = this.conn.prepareStatement("DELETE FROM club WHERE owner='"+email+"'AND name='"+name+"';");
        s.executeUpdate();
    }

    /**
     * Method to get owner email of a club from a SQL DATABASE.
     * @param id - Club's Id String.
     * @return owner email String.
     * @throws SQLException if occurs an error in DB.
     */
    public String ownerEmail(String id) throws SQLException {
        PreparedStatement s = this.conn.prepareStatement("SELECT owner FROM club WHERE id='"+id+"';");
        ResultSet rs = s.executeQuery();
        while(rs.next()) {
            return rs.getString(1);
        }
        return null;
    }

    /**
     * Method to get club name of a specific owner from a SQL DATABASE.
     * @param clubID - Club id String.
     * @return club name String.
     * @throws SQLException if occurs an error in DB.
     */
    public String clubName(String clubID) throws SQLException {
        PreparedStatement s = this.conn.prepareStatement("SELECT name FROM club WHERE id='"+clubID+"';");
        ResultSet rs = s.executeQuery();
        while(rs.next()) {
            return rs.getString(1);
        }
        return null;
    }

    /**
     * Method to verify if given email is this the club owner or not.
     * @param email - String with user's email.
     * @param name - Club name.
     * @return true if that is the owner, false if not.
     * @throws SQLException if occurs an error in DB.
     */
    public boolean isOwner (String email, String name) throws SQLException {
        PreparedStatement s = this.conn.prepareStatement("SELECT * FROM club WHERE owner='"+email+"' AND name='"+name+"';");
        ResultSet rs = s.executeQuery();

        while(rs.next()) {
            if(rs.getString(1).equals(name) && rs.getString(3).equals(email))
                return true;
        }

        return false;
    }

    /**
     * Method to get all users that are in a specific group/club from SQL Database.
     * @param id - Group id.
     * @return all users in an Class userList that is an ArrayList.
     * @throws SQLException if occurs an error in DB.
     */
    public ArrayList<String> getUsersFromGroup(String id) throws SQLException {
        ArrayList<String> usrL = new ArrayList<>();
        PreparedStatement s = this.conn.prepareStatement("SELECT member FROM association WHERE groupid='"+id+"';");
        ResultSet rs = s.executeQuery();
        while(rs.next()) {
            if(!usrL.contains(rs.getString(1)))
                usrL.add(rs.getString(1));
        }
        return usrL;
    }

    /**
     * Method to put into DB one string and two booleans.
     * @param email - User's email.
     * @param en - Event Notification if is on or off.
     * @param gn - Gym Notification if is on or off.
     * @throws SQLException if occurs an error in DB.
     */
    public void addNotification(String email, Boolean en, Boolean gn) throws SQLException {
        PreparedStatement s = this.conn.prepareStatement("SELECT * FROM notification WHERE email='"+email+"';");
        ResultSet rs = s.executeQuery();

        if(!rs.next()) {
            s = this.conn.prepareStatement("INSERT INTO notification VALUES (?,?,?);");
            s.setString(1,email);
            s.setBoolean(2,en);
            s.setBoolean(3,gn);
        }
        else {
            s = this.conn.prepareStatement("UPDATE notification SET eventnots="+en+",gymnots="+gn+" WHERE email='"+email+"';");
        }
        s.executeUpdate();
    }

    /**
     * Method to get notifications state from DB.
     * @param email - User's email.
     * @return a string with the values that we need: if user is premium or not, and if notification is on or not.
     * @throws SQLException if occurs an error in DB.
     */
    public String[] getNotification(String email) throws SQLException {
        PreparedStatement s = this.conn.prepareStatement("SELECT * FROM notification WHERE email='"+email+"';");
        ResultSet rs = s.executeQuery();

        String[] str = new String[5];
        while(rs.next()) {
            if(rs.getString(1).equals(email)) {
                assert str != null;
                str[0] = Boolean.toString(premiumUser(email));
                str[1] = rs.getString(2);
                str[2] = rs.getString(3);
            }
            else str=null;
        }
        return str;
    }

    /**
     * Method to get DB Connection.
     * @return DB Connection.
     */
    public Connection getConn() {
        return this.conn;
    }

    /**
     * Class to send all public clubs list to Android App. Help us to create a json.
     */
    public static class clubList {
        ArrayList<Club> clubL = new ArrayList<>();
    }

}
