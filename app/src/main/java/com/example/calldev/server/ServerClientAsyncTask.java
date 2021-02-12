package com.example.calldev.server;

import android.os.AsyncTask;
import android.widget.Toast;

import com.example.calldev.SignInActivity;
import com.example.calldev.other.GlobalStorage;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Date: Nov 28-2020.
 * An asynchronous task that handles the Google Calendar API call and communicates with the server.
 * It's one of the few time in which the mobile application handles with the Google API directly, alongside the SignInActivity class.
 * @author CALDEV.
 */

public class ServerClientAsyncTask extends AsyncTask<Void, Void, String>
{
    /**
     * String constant used to distinguish this class (used in debugging).
     */
    private static final String TAG = "ServerClientAsyncTask";
    /**
     * SignInActivity object used to display pop up messages in the SignInActivity class.
     */
    private SignInActivity mActivity;
    /**
     * String value of the credential's token.
     */
    private String token;
    /**
     * String value of the credential's email address.
     */
    private String credemail;
    /**
     * String value of the user's email address.
     */
    private final String useremail;
    /**
     * Boolean value to check if the two email addresses are the same or not.
     */
    private boolean wrongemail = false;
    /**
     * Boolean value to check if the server is online or not.
     */
    private boolean dead_server = false;
    /**
     * Boolean value to check if the user already has a profile associated.
     */
    private boolean userExists = false;
    /**
     * Boolean value to start the user with default status (non premium).
     */
    private boolean premium = false;
    /**
     * Server Link.
     */
    private static final String API_STR = GlobalStorage.getInstance().getIpAddr();
    /**
     * Apache HttpClient.
     */
    private final DefaultHttpClient httpClient = new DefaultHttpClient();
    /**
     * HTTP to access POST MAPPING on server.
     */
    private HttpPost postRequest = null;
    /**
     * HTTP Response to receive information from CalDev server.
     */
    private HttpResponse httpResponse = null;
    /**
     * Represents an HTTP request or response entity, consisting of headers and body.
     */
    private HttpEntity httpResult = null;

    /**
     * Constructor used to access initiate this class.
     * @param activity SignInActivity class that spawned this task.
     * @param email email used in the creation of the credential.
     */
    public ServerClientAsyncTask(SignInActivity activity, String email) {
        this.mActivity = activity;
        this.useremail = email;
    }

    /**
     * Background task to call Google Calendar API and communicate with the server.
     * Gets the Token of the credential and the email address of the credential.
     * @param voids the parameters passed to the task, in this case there are none.
     * @return A result then passed to the onPostExecute() method.
     */
    @Override
    protected String doInBackground(Void... voids)
    {
        try {
            if(!ServerOn()) {
                dead_server = true;
                return null;
            }
            List<String> events = getDataFromApi();
            token = mActivity.credential.getToken();
            credemail = mActivity.credential.getSelectedAccount().name;

            if(!credemail.equals(useremail.trim())) {
                wrongemail = true;
                return null;
            }

            String json = formatDataAsJSON();

            //if necessary
            postTokenOnServer(json);

            //if true, exists. If false, does not exist
            getIfUserExists();

            //Check if it is Premium or Not
            premium = getPremiumStatus();
        }
        catch (final GooglePlayServicesAvailabilityIOException availabilityException) {
            mActivity.showGooglePlayServicesAvailabilityErrorDialog(availabilityException.getConnectionStatusCode());
        }
        catch (UserRecoverableAuthIOException userRecoverableException) {
            mActivity.startActivityForResult(userRecoverableException.getIntent(), SignInActivity.REQUEST_AUTHORIZATION);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (GoogleAuthException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Method to see if server is on or off.
     * @return boolean = true if server is on.
     */
    private boolean ServerOn() {
        try {
            HttpGet getRequest = new HttpGet(API_STR + "");
            HttpParams p = httpClient.getParams();
            HttpParams oldP = p;
            HttpConnectionParams.setConnectionTimeout(p, 8000);
            HttpConnectionParams.setSoTimeout(p, 8000);
            httpClient.setParams(p);
            httpResponse = httpClient.execute(getRequest);
            httpResult = httpResponse.getEntity();
            String JSONString = EntityUtils.toString(httpResult, "UTF-8");
            httpClient.setParams(oldP);
            return JSONString != null;
        }
        catch (IOException e) {
            return false;
        }
    }

    /**
     * Method to format the JSON we want to send to server.
     * @return JSON with User's token and User's email.
     */
    private String formatDataAsJSON() {
        final JSONObject root = new JSONObject();
        try
        {
            root.put("token", token);
            root.put("email", credemail);

            return root.toString();
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Method to inform server that we have a new login.
     */
    private void postTokenOnServer(String json) {
        try {
            postRequest = new HttpPost(API_STR+"/login");

            StringEntity input = new StringEntity(json);
            input.setContentType("application/json");
            postRequest.setEntity(input);
            httpResponse = httpClient.execute(postRequest);

            httpResult = httpResponse.getEntity();
            closeHTTP(httpResult);
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
        catch (ClientProtocolException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Method to inform server that we want to know if an User exists or not in DB.
     * @throws IOException if I/O operations failed or were interrupted.
     */
    private void getIfUserExists() throws IOException {
        postRequest = new HttpPost(API_STR+"/UserExistsOrNot");

        StringEntity input = new StringEntity(credemail);
        input.setContentType("application/json");
        postRequest.setEntity(input);
        httpResponse = httpClient.execute(postRequest);

        httpResult = httpResponse.getEntity();
        String JSONString = EntityUtils.toString(httpResult, "UTF-8");
        userExists= JSONString.equals("true");
    }

    /**
     * Method to inform server that we want to know if an User is Premium or not.
     * @return true if user is premium and false if not.
     * @throws IOException if I/O operations failed or were interrupted.
     */
    private boolean getPremiumStatus() throws IOException {
        postRequest = new HttpPost(API_STR+"/PremiumUserOrNot");

        StringEntity input = new StringEntity(credemail);
        input.setContentType("application/json");
        postRequest.setEntity(input);
        httpResponse = httpClient.execute(postRequest);

        httpResult = httpResponse.getEntity();
        String JSONString = EntityUtils.toString(httpResult, "UTF-8");

        return JSONString.equals("true");
    }

    /**
     * Method to close HTTP connection.
     * @param result - HTTP Result or Response Entity.
     */
    private void closeHTTP (HttpEntity result) {
        try {
            InputStream inputstream = result.getContent();
            inputstream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method called upon completion of the doInBackground() method.
     * Check if the email that the user inserted on the login page does not match with the email selected on the following page.
     * It also verifies if the server is up and online.
     * If any of these conditions are met the returnActivity() method from the SignInActivity class is called.
     * If not the newActivity() method is called instead and the email address and a boolean are passed.
     * @param result the result of the operation computed by the doInBackground() method.
     */
    @Override
    protected void onPostExecute(String result)
    {
        super.onPostExecute(result);

        if(wrongemail || dead_server)
        {
            if(wrongemail) Toast.makeText(mActivity, "Please enter valid email", Toast.LENGTH_SHORT).show();
            else if(dead_server) Toast.makeText(mActivity, "Unable to reach server", Toast.LENGTH_SHORT).show();
            mActivity.returnActivity();
        }
        else
        {
            mActivity.newActivity(credemail, userExists);
        }

        GlobalStorage.getInstance().setPremium(premium);
    }

    /**
     * Method called to get the next 10 events from the calendar of the app's user.
     * Despite not using this information directly in the mobile app,
     * it is recommended to implement this method in order to get permission with the API and to make certain that the Google Calendar API is handled and synced properly.
     */
    private List<String> getDataFromApi() throws IOException {
        DateTime now = new DateTime(System.currentTimeMillis());
        List<String> eventStrings = new ArrayList<>();
        Events events = mActivity.service.events().list("primary")
                .setMaxResults(10)
                .setTimeMin(now)
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute();
        List<Event> items = events.getItems();

        for (Event event : items) {
            DateTime start = event.getStart().getDateTime();
            if (start == null) {
                start = event.getStart().getDate();
            }
            eventStrings.add(String.format("%s (%s)", event.getSummary(), start));
        }
        return eventStrings;
    }
}