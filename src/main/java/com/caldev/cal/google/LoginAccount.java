package com.caldev.cal.google;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;

import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * Class LoginAccount is used to create: a Credential, an Google Calendar, a HTTP Transport and a JSON Factory.
 * Date: Dec-7-2020.
 * @author CALDEV.
 * */
public class LoginAccount {
    /**
     * User's private Google token.
     */
    private String token;
    /**
     * User's private Google email.
     */
    private String email;
    /**
     * User's private Google Credential.
     */
    private static Credential cred = null;
    /**
     * Private JsonFactory to access Google Calendar API.
     */
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    /**
     * Private HttpTransport to access Google Calendar API.
     */
    private static HttpTransport HTTP_TRANSPORT = null;

    /**
     * Constructor that creates a Google Login Account.
     * With token, this depends on method CreateCredential().
     * @param email - User's email.
     * @param token - User's token, that comes from Android App.
     * @throws IOException if I/O operations failed or were interrupted.
     * @throws GeneralSecurityException if occurs any type of insecurity.
     */
    public LoginAccount(String email, String token) throws IOException, GeneralSecurityException {
        this.email = email;
        this.token = token;
        this.CreateCredential();
    }

    /**
     * Constructor that creates a Google Login Account.
     * With token, this depends on method CreateCredential().
     * @param log - Class LoginAccount.
     */
    public LoginAccount(LoginAccount log) {
        try {
            this.email = log.email;
            this.token = log.token;
            this.CreateCredential();
        } catch (IOException | GeneralSecurityException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method that creates a google credential with a token response.
     * @param tokenResponse - TokenResponse Class.
     * @return a class Credential, that is user's credential.
     */
    public static Credential createCredentialWithAccessTokenOnly(TokenResponse tokenResponse) {
        return new Credential(BearerToken.authorizationHeaderAccessMethod()).setFromTokenResponse(tokenResponse);
    }

    /**
     * Method that creates a google credential.
     * It depends on TokenResponse Class, HTTP_TRANSPORT and method createCredentialWithAccessTokenOnly().
     * @throws IOException if I/O operations failed or were interrupted.
     * @throws GeneralSecurityException if occurs any type of insecurity.
     */
    public void CreateCredential() throws IOException, GeneralSecurityException {
        HTTP_TRANSPORT = com.google.api.client.googleapis.apache.v2.GoogleApacheHttpTransport.newTrustedTransport();

        TokenResponse new_token = new TokenResponse().setAccessToken(this.token).setExpiresInSeconds(93600L);
        cred = createCredentialWithAccessTokenOnly(new_token);
    }

    /**
     * Method to get User's Token.
     * @return user's token.
     */
    public String getToken() { return token; }

    /**
     * Method to get User's Email.
     * @return user's email.
     */
    public String getEmail() { return email; }

    /**
     * Method to get User's Credential.
     * @return user's credential.
     */
    public Credential getCredential() { return cred; }

    /**
     * Method to get JsonFactory.
     * @return JSON_FACTORY.
     */
    public static JsonFactory getJsonFactory() { return JSON_FACTORY; }

    /**
     * Method to get HttpTransport.
     * @return HTTP_TRANSPORT.
     */
    public static HttpTransport getHttpTransport() { return HTTP_TRANSPORT; }

}