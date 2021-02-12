package com.example.calldev;

import android.accounts.AccountManager;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.calldev.other.GlobalStorage;
import com.example.calldev.server.ServerClientAsyncTask;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.CalendarScopes;

import java.util.Arrays;

/**
 * Date: Nov 28-2020.
 * This is a class that extends activity and represents the sign in page of the app.
 * In this page the user is shown a progress bar while we verify it's input in the previous page and ask him to select an account with the Google API.
 * It's one of the few time in which the mobile application handles with the Google API directly, alongside the ServerClientAsyncTask class.
 * @author CALDEV.
 */

public class SignInActivity extends AppCompatActivity {

    /**
     * String constant used to distinguish this activity (used in debugging and to show the user pop up messages).
     */
    private static final String TAG = "SignInActivity";

    /**
     * Integer constant used to distinguish the choose an account request.
     */
    public static final int REQUEST_ACCOUNT_PICKER = 1000;
    /**
     * Integer constant used to distinguish the authorization request.
     */
    public static final int REQUEST_AUTHORIZATION = 1001;
    /**
     * Integer constant used to distinguish the Google Play Services installation request.
     */
    public static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    /**
     * String constant used to distinguish the email address of the user.
     */
    private static final String PREF_ACCOUNT_NAME = "accountName";
    /**
     * Array of Strings constant containing the available OAuth 2.0 scopes for use with the Google Calendar API.
     * More specifically, it enables to see, edit, share, and permanently delete all the calendars.
     */
    private static final String[] SCOPES = {CalendarScopes.CALENDAR};

    /**
     * Google Calendar API service object used to access the API.
     * Manipulates events and other calendar data.
     */
    public com.google.api.services.calendar.Calendar service;
    /**
     * Google Calendar API object that manages authorization and account selection for Google accounts.
     */
    public GoogleAccountCredential credential;
    /**
     * HTTP transport object used to build the Google Calendar API service object.
     */
    private final HttpTransport transport = AndroidHttp.newCompatibleTransport();
    /**
     * JSON factory object used to build the Google Calendar API service object.
     */
    private final JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

    /**
     * String value of the user's email address.
     */
    private String email;

    /**
     * Boolean value that indicates the state of login of the user.
     */
    private boolean logged;

    /**
     * Called whenever this activity is created.
     * Passes the email first entered by user in the login page and then creates the credential and sets up the google api services.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        checkIncomingIntent();

        SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);



        credential = GoogleAccountCredential.usingOAuth2(getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff())
                .setSelectedAccountName(settings.getString(PREF_ACCOUNT_NAME, null));



        service = new com.google.api.services.calendar.Calendar.Builder(transport, jsonFactory, credential)
                .setApplicationName("Google Calendar API Android")
                .build();

        logged=false;
    }

    /**
     * Called whenever this activity is pushed to the foreground, in this case after a call to onCreate().
     */
    @Override
    protected void onResume()
    {
        super.onResume();
        if (isGooglePlayServicesAvailable()) { refreshResults(); }
        else { Toast.makeText(this, "Google Play Services required: " + "after installing, close and relaunch this app", Toast.LENGTH_LONG).show(); }
    }

    /**
     * Called when an activity launched here (specifically, AccountPicker and authorization) exits,
     * giving you the requestCode you started it with, the resultCode it returned, and any additional data from it.
     * @param requestCode code indicating which activity result is incoming.
     * @param resultCode code indicating the result of the incoming activity result.
     * @param data Intent (containing result data) returned by incoming activity result.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode)
        {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode == RESULT_OK) { refreshResults(); }
                else { isGooglePlayServicesAvailable(); }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null && data.getExtras() != null)
                {
                    String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null)
                    {
                        credential.setSelectedAccountName(accountName);
                        SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.commit();
                        refreshResults();
                    }
                }
                else if (resultCode == RESULT_CANCELED) { Toast.makeText(this, "Account unspecified", Toast.LENGTH_LONG).show();  returnActivity();}
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) { refreshResults(); }
                else { chooseAccount(); }
                break;
        }
    }

    /**
     * Method that attempts to get a set of data from the Google Calendar API and to communicate with the server.
     * If the email address isn't known yet, then the method chooseAccount() is called so the user can pick an account.
     */
    private void refreshResults()
    {
        if (credential.getSelectedAccountName() == null)
        {
            chooseAccount();
        }
        else
        {
            if (isDeviceOnline() && !logged)
            {

                logged=true;
                new ServerClientAsyncTask(this, email).execute();
            }
            else if (!logged)
            {
                Toast.makeText(this, "No network connection available", Toast.LENGTH_LONG).show();
                returnActivity();
            }
        }
    }

    /**
     * Starts an activity in Google Play Services so the user can pick an account.
     */
    private void chooseAccount()
    {
        startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
    }

    /**
     * Checks whether the device currently has a network connection.
     * @return true if the device has a network connection, false otherwise.
     */
    private boolean isDeviceOnline()
    {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        return (networkInfo != null && networkInfo.isConnected());
    }

    /**
     * Check that Google Play services APK is installed and up to date.
     * Will launch an error dialog for the user to update Google Play Services if possible.
     * @return true if Google Play Services is available and up to date on this device; false otherwise.
     */
    private boolean isGooglePlayServicesAvailable()
    {
        final int connectionStatusCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (GooglePlayServicesUtil.isUserRecoverableError(connectionStatusCode))
        {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
            return false;
        }
        else if (connectionStatusCode != ConnectionResult.SUCCESS)
        {
            return false;
        }
        return true;
    }

    /**
     * Display an error dialog showing that Google Play Services is missing or out of date.
     * @param connectionStatusCode code describing the presence (or lack of) Google Play Services on this device.
     */
    public void showGooglePlayServicesAvailabilityErrorDialog(final int connectionStatusCode)
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                Dialog dialog = GooglePlayServicesUtil.getErrorDialog(connectionStatusCode, SignInActivity.this, REQUEST_GOOGLE_PLAY_SERVICES);
                dialog.show();
            }
        });
    }

    /**
     * Method responsible for checking the reception of the logoff button interaction from other activities.
     * If it receives the constant signifying this interaction it will clear data stored in the shared preferences.
     * This action will prompt the user to go through the process of selecting a google account again.
     */
    public void checkIncomingIntent()
    {
        Intent intent = getIntent();
        if(intent.hasExtra(LoginActivity.EXTRA_LOGIN_LOGOUT))
        {
            SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
            settings.edit().clear().commit();
        }
        email = intent.getStringExtra(LoginActivity.EXTRA_EMAIL_LOGIN);
    }

    /**
     * Method responsible for the entry of the user into the "main" pages of the app.
     * There are 2 possible options of entry based on the data received from the server.
     * If the user has already a profile he will be promptly directed to the calendar page.
     * If not he will be promptly directed to the profile page where he will be able to create one.
     */
    public void newActivity(String femail, boolean flag)
    {
        email = femail;

        GlobalStorage.getInstance().setEmail(email);
        GlobalStorage.getInstance().setHasProfile(flag);

        if(flag)
        {
            Intent calendarsignin = new Intent(this, CalendarActivity.class);
            calendarsignin.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(calendarsignin);
        }
        else
        {
            Intent profilesignin = new Intent(this, ProfileActivity.class);
            profilesignin.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(profilesignin);
        }
        finish();
    }

    /**
     * Method responsible for returning the user to the login page first displayed when the user enters the app.
     * This method is used here when the user selects a different email from the email he first introduced in the login page.
     */
    public void returnActivity()
    {
        Intent failedsignin = new Intent(this, LoginActivity.class);
        failedsignin.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(failedsignin);
        finish();
    }
}