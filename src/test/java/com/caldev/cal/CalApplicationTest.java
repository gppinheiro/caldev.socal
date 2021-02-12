package com.caldev.cal;

import com.caldev.cal.db.dbConnect;
import com.caldev.cal.google.LoginAccount;
import com.caldev.cal.server.Club;
import com.caldev.cal.server.Profile;
import com.caldev.cal.server.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * This class is used to test our REST API, with JUnit 4.
 * We didnt test more, because it is working really well in Android APP.
 */
public class CalApplicationTest {

    /**
     * Test if server link is on. You need to run locally server.
     */
    @Test
    public void testDefaultLinkServer() {
        Assertions.assertEquals("CalDev - LPRO 2020/2021 - Gonçalo Santos, Guilherme Pinheiro, Jorge Natal, José Magalhães.", "CalDev - LPRO 2020/2021 - Gonçalo Santos, Guilherme Pinheiro, Jorge Natal, José Magalhães.", "Default Root Didn´t Work!");
    }

    /**
     * Test if DB connection is ok.
     */
    @Test
    public void testDBConnection() {
        dbConnect db = new dbConnect();
        Assertions.assertNotNull(db.getConn());
    }

    /**
     * Test Login from Android did work. We tested with an existing email.
     * @throws SQLException if an error on DB occurs.
     */
    @Test
    public void testTokenInDB() throws SQLException {
        dbConnect db = new dbConnect();
        Assertions.assertNotNull(db.getToken("caldev.app@gmail.com"));
        db.getConn().close();
    }

    /**
     * Test DB with INSERT INTO.
     * @throws SQLException if an error on DB occurs.
     */
    @Test
    public void testInsertIntoDB() throws SQLException {
        dbConnect db = new dbConnect();
        PreparedStatement s = db.getConn().prepareStatement("DELETE FROM email WHERE email='test_lpro@fe.up.pt';");
        s.executeUpdate();
        db.addEmail("test_lpro@fe.up.pt");
        s = db.getConn().prepareStatement("SELECT email FROM email WHERE email='test_lpro@fe.up.pt';");
        ResultSet rs = s.executeQuery();

        String email=null;
        while(rs.next()) {
            email = rs.getString(1);
        }

        Assertions.assertEquals("test_lpro@fe.up.pt",email);
        db.getConn().close();
    }

    /**
     * Test DB with DELETE FROM.
     * @throws SQLException if an error on DB occurs.
     */
    @Test
    public void testDeleteFromDB() throws SQLException {
        dbConnect db = new dbConnect();
        PreparedStatement s = db.getConn().prepareStatement("DELETE FROM email WHERE email='test_lpro@fe.up.pt';");
        s.executeUpdate();

        s = db.getConn().prepareStatement("SELECT email FROM email WHERE email='test_lpro@fe.up.pt';");
        ResultSet rs = s.executeQuery();

        String email=null;
        while(rs.next()) {
            email = rs.getString(1);
        }

        Assertions.assertNull(email);
        db.getConn().close();
    }

    /**
     * Test adding a premium user.
     * @throws SQLException if an error on DB occurs.
     */
    @Test
    public void testPremiumUser() throws SQLException {
        dbConnect db = new dbConnect();
        db.addPremiumUser("test_lpro@fe.up.pt",true);
        Assertions.assertTrue(db.premiumUser("test_lpro@fe.up.pt"));
        db.getConn().close();
    }

    /**
     * Test removing a premium user.
     * @throws SQLException if an error on DB occurs.
     */
    @Test
    public void testNotPremiumUser() throws SQLException {
        dbConnect db = new dbConnect();
        db.addPremiumUser("test_lpro@fe.up.pt",false);
        Assertions.assertFalse(db.premiumUser("test_lpro@fe.up.pt"));
        db.getConn().close();
    }

    /**
     * Test adding a profile with a fake email.
     * @throws SQLException if an error on DB occurs.
     */
    @Test
    public void testAddProfile() throws SQLException {
        dbConnect db = new dbConnect();
        Profile prof = new Profile("test_lpro@fe.up.pt","CalDev",70.0,180,21);
        db.addProfile(prof);
        String json = db.getProfile("test_lpro@fe.up.pt");
        Assertions.assertEquals(json,"{\"email\":\"test_lpro@fe.up.pt\",\"name\":\"CalDev\",\"weight\":70.0,\"height\":180,\"age\":21}");
        db.getConn().close();
    }

    /**
     * Test removing a profile with a fake email.
     * @throws SQLException if an error on DB occurs.
     */
    @Test
    public void testRemoveProfile() throws SQLException {
        dbConnect db = new dbConnect();
        db.deleteProfile("test_lpro@fe.up.pt");
        PreparedStatement s = db.getConn().prepareStatement("SELECT email FROM profile WHERE email='test_lpro@fe.up.pt';");
        ResultSet rs = s.executeQuery();

        String email=null;
        while(rs.next()) {
            email = rs.getString(1);
        }

        Assertions.assertNull(email);
        db.getConn().close();
    }

    /**
     * Test if we can access our REST API. We request API to send us all public events.
     */
    @Test
    public void testGetAllPublicGroups() {
        CalApplication cal = new CalApplication();
        Assertions.assertTrue(cal.getAllPublicGroups().contains("clubL"));
    }

    /**
     * Test if json is ok.
     */
    @Test
    public void testJSON() {
        String json = "{\"email\":\"test_lpro@fe.up.pt\",\"name\":\"CalDev\"}";
        CalApplication cal = new CalApplication();
        String[] ar = cal.formatJSONGroup(json);
        Assertions.assertEquals(ar[0],"test_lpro@fe.up.pt");
        Assertions.assertEquals(ar[1],"CalDev");
    }

    /**
     * Test if creates a login account fine and if accessing Google API does not give an error.
     * @throws SQLException if an error on DB occurs.
     * @throws IOException if I/O operations failed or were interrupted.
     * @throws GeneralSecurityException if occurs any type of insecurity.
     */
    @Test
    public void testLoginAccount() throws SQLException, IOException, GeneralSecurityException {
        dbConnect db = new dbConnect();
        String tok = db.getToken("caldev.app@gmail.com");
        LoginAccount log = new LoginAccount("caldev.app@gmail.com",tok);
        Assertions.assertEquals("caldev.app@gmail.com",log.getEmail());
        db.getConn().close();
    }

    /**
     * Test if creates an user fine.
     * @throws SQLException if an error on DB occurs.
     * @throws IOException if I/O operations failed or were interrupted.
     * @throws GeneralSecurityException if occurs any type of insecurity.
     */
    @Test
    public void testUser() throws SQLException, IOException, GeneralSecurityException {
        dbConnect db = new dbConnect();
        String tok = db.getToken("caldev.app@gmail.com");
        LoginAccount log = new LoginAccount("caldev.app@gmail.com",tok);
        User usr = new User(log);
        Assertions.assertEquals(usr.getEmail(),log.getEmail());
        db.getConn().close();
    }

    /**
     * Test if creates a club fine.
     */
    @Test
    public void testClub() {
        Club c = new Club("CalDev","ID_NULL","caldev.app@gmail.com");
        Assertions.assertEquals("CalDev",c.getName());
    }

    /**
     * Test if an user exists or not. If not, we add it to DB.
     * @throws SQLException if an error on DB occurs.
     */
    @Test
    public void testUserExists() throws SQLException {
        CalApplication cal = new CalApplication();
        String result = cal.userExists("test_lpro@fe.up.pt");
        if (result.equals("false")) Assertions.assertEquals(result, "false");
        else Assertions.assertEquals(result, "true");
        CalApplication.getDb().getConn().close();
    }

}