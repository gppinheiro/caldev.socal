package com.example.calldev.server;

import android.os.AsyncTask;
import android.widget.Toast;

import com.example.calldev.CalendarActivity;
import com.example.calldev.ProfileActivity;
import com.example.calldev.item.ProfileItem;
import com.example.calldev.other.GlobalStorage;
import com.example.calldev.view.ProfileAdapter;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

/**
 * Date: Nov 28-2020.
 * An asynchronous task that handles the communication with the server regarding any profile modification.
 * @author CALDEV.
 */

public class ServerProfileAsyncTask extends AsyncTask<Void, Void, String>
{
    /**
     * String constant used to distinguish this class (used in debugging).
     */
    private static final String TAG = "ServerProfileAsyncTask";

    /**
     * ProfileActivity object used to display pop up messages and update the profile data in the ProfileActivity class.
     */
    private ProfileActivity mActivity;
    /**
     * CalendarActivity object used to display pop up messages in the CalendarActivity class.
     */
    private CalendarActivity cActivity;
    /**
     * ProfileAdapter object used to display pop up messages in the ProfileAdapter class.
     */
    private ProfileAdapter adapter;
    /**
     * ArrayList object containing the profile data in the form of ProfileItem objects.
     */
    private ArrayList<ProfileItem> profileList = new ArrayList<>();

    /**
     * String value of the type of operation required and passed by the ProfileActivity/CalendarActivity/ProfileAdapter class.
     */
    private String flag;
    /**
     * String value of the user's email address.
     */
    private String email;
    /**
     * String value of the user's username retrieved from the server.
     */
    private String oldname;
    /**
     * String value of the user's age retrieved from the server.
     */
    private String oldage;
    /**
     * String value of the user's weight retrieved from the server.
     */
    private String oldweight;
    /**
     * String value of the user's height retrieved from the server.
     */
    private String oldheight;
    /**
     * String value of the user's bmi retrieved from the server.
     */
    private String oldbmi;
    /**
     * Boolean variable that tells if the user has a profile or not.
     */
    private boolean noProfile = false;

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
     * Constructor used when the user profile is added/edited/eliminated.
     * @param profileActivity ProfileActivity class that spawned this task.
     * @param flag a string that differentiates the operation that is required.
     * @param email the email address of the user.
     * @param profileList the contents of the profile that the user modified.
     */
    public ServerProfileAsyncTask(ProfileActivity profileActivity, String flag, String email, ArrayList<ProfileItem> profileList) {
        this.mActivity = profileActivity;
        this.flag = flag;
        this.email = email;
        this.profileList = profileList;
    }

    /**
     * Constructor used to retrieve the user's profile from the server.
     * @param profileActivity ProfileActivity class that spawned this task.
     * @param flag a string that differentiates the operation that is required.
     * @param email the email address of the user.
     */
    public ServerProfileAsyncTask(ProfileActivity profileActivity, String flag, String email) {
        this.mActivity = profileActivity;
        this.flag = flag;
        this.email = email;
    }

    /**
     * Constructor used to retrieve the user's settings from the server.
     * @param calendarActivity CalendarActivity class that spawned this task.
     * @param flag a string that differentiates the operation that is required.
     * @param email the email address of the user.
     */
    public ServerProfileAsyncTask(CalendarActivity calendarActivity, String flag, String email) {
        this.cActivity = calendarActivity;
        this.flag = flag;
        this.email = email;
    }

    /**
     * Constructor used to update the user's settings from the server.
     * @param profileAdapter ProfileAdapter class that spawned this task.
     * @param flag a string that differentiates the operation that is required.
     * @param email the email address of the user.
     */
    public ServerProfileAsyncTask(ProfileAdapter profileAdapter, String flag, String email) {
        this.adapter = profileAdapter;
        this.flag = flag;
        this.email = email;
    }

    /**
     * Background task to communicate with the server based on the type of operation required and passed by the ProfileActivity/CalendarActivity/ProfileAdapter class.
     * @param voids the parameters passed to the task, in this case there are none.
     * @return A result then passed to the onPostExecute() method.
     */
    @Override
    protected String doInBackground(Void... voids) {
        switch (flag) {
            case "add":
                String name = profileList.get(0).getText();
                String[] age = profileList.get(1).getData().split(" ");
                String[] weight = profileList.get(2).getData().split(" ");
                String[] height = profileList.get(3).getData().split(" ");
                String j = formatProfileAsJSON(name, age[0], weight[0], height[0]);
                AddProfileOnServer(j);
                break;
            case "delete":
            {
                DeleteProfileOnServer();
                break;
            }
            case "premium active":
            {
                flag = "active";
                addPremiumUser();
                break;
            }
            case "premium deactivated":
            {
                flag = "deactivated";
                removePremiumUser();
                break;
            }
            case "settings":
            {
                getNotifications();
                break;
            }
            case "notifications":
            {
                sendNotifications();
                break;
            }
            case "get":
            {
                getProfileFromServer();
                if(noProfile) {
                    flag = "noprofile";
                }
                else {
                    flag = "done";

                    int aux1 = Integer.parseInt(oldheight);
                    double aux2 = Double.parseDouble(oldweight);
                    double aux3 = aux2 / (aux1 * aux1 * 0.0001);

                    NumberFormat formater = new DecimalFormat("#0.00");
                    oldbmi = formater.format(aux3);
                }
                break;
            }
        }
        return null;
    }

    /**
     * Method to format the JSON we received from server.
     * @return JSON with User's profile (email, name, weight, height, age).
     */
    private String formatProfileAsJSON(String name, String age, String weight, String height) {
        final JSONObject root = new JSONObject();
        try {
            root.put("email", email);
            root.put("name", name);
            root.put("weight", weight);
            root.put("height", height);
            root.put("age", age);

            return root.toString();
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Method to inform server that we want to add a new User's Profile on server and DB.
     */
    private void AddProfileOnServer(String json) {
        try {
            postRequest = new HttpPost(API_STR+"/AddProfile");

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
     * Method to inform server that we want delete an User's Profile on server and DB.
     */
    private void DeleteProfileOnServer() {
        try {
            postRequest = new HttpPost(API_STR+"/DeleteProfile");

            StringEntity input = new StringEntity(email);
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
     * Method to inform server that we want to get an User's Profile from server and DB.
     */
    private void getProfileFromServer() {
        try {
            postRequest = new HttpPost(API_STR+"/GetProfile");

            StringEntity input = new StringEntity(email);
            input.setContentType("application/json");
            postRequest.setEntity(input);

            httpResponse = httpClient.execute(postRequest);

            httpResult = httpResponse.getEntity();
            String JSONString = EntityUtils.toString(httpResult, "UTF-8");

            if(JSONString.length()<5) noProfile = true;

            formatProfileString(JSONString);
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
     * Method to inform server that we want to add a new User's Premium on server and DB.
     */
    private void addPremiumUser() {
        try {
            postRequest = new HttpPost(API_STR+"/AddPremiumUser");

            StringEntity input = new StringEntity(this.email);
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
     * Method to inform server that we want to remove an User's Premium on server and DB.
     */
    private void removePremiumUser() {
        try {
            postRequest = new HttpPost(API_STR+"/RemovePremiumUser");

            StringEntity input = new StringEntity(this.email);
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
     * Method to inform server that we want to store notifications on DB.
     */
    private void sendNotifications() {
        try {
            postRequest = new HttpPost(API_STR+"/SendNotifications");

            JSONObject obj = new JSONObject();

            obj.put("email",this.email);
            obj.put("hasEventnots",GlobalStorage.getInstance().hasEventnots());
            obj.put("hasGymnots",GlobalStorage.getInstance().hasGymnots());

            StringEntity input = new StringEntity(obj.toString());
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
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method to inform server that we want to get notification's states from DB.
     */
    private void getNotifications() {
        try {
            postRequest = new HttpPost(API_STR+"/GetNotifications");

            StringEntity input = new StringEntity(this.email);
            input.setContentType("application/json");
            postRequest.setEntity(input);
            httpResponse = httpClient.execute(postRequest);

            httpResult = httpResponse.getEntity();
            String JSONString = EntityUtils.toString(httpResult, "UTF-8");
            //Remove brackets
            JSONString = JSONString.replace("{","");
            JSONString = JSONString.replace("}","");
            //Split ,
            String [] parts = JSONString.split(",");

            for (String words:parts) {
                words = words.replace("\"","");
                String[] word = words.split(":");
                if(word[0].equals("Premium"))
                    GlobalStorage.getInstance().setPremium(word[1].equals("true"));
                else if (word[0].equals("Event"))
                    GlobalStorage.getInstance().setEventNots(word[1].equals("t"));
                else if (word[0].equals("Gym"))
                    GlobalStorage.getInstance().setGymNots(word[1].equals("t"));
            }
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
     * Method to treat JSON the way we want.
     * @param s - JSON to treat.
     */
    private void formatProfileString(String s) {
        //Remove brackets
        s = s.replace("{","");
        s = s.replace("}","");
        //Split ,
        String [] parts = s.split(",");

        for (String words:parts) {
            String [] part = words.split(":");
            part[0] = part[0].substring(1,part[0].length()-1);
            if (part[0].equals("email")) { this.email=part[1]; }
            else if (part[0].equals("name")) { this.oldname=part[1]; }
            else if (part[0].equals("weight")) { this.oldweight=part[1]; }
            else if (part[0].equals("height")) { this.oldheight=part[1]; }
            else if (part[0].equals("age")) { this.oldage=part[1]; }
        }

    }

    /**
     * Method called upon completion of the doInBackground() method displaying a message.
     * If a profile is retrieved from the server,
     * the createProfileData() method from the ProfileActivity class is called passing the contents of the retrieved profile.
     * @param result the result of the operation computed by the doInBackground() method.
     */
    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        switch (flag) {
            case "noprofile":
                mActivity.onClickAddProfile();
                break;
            case "done":
                mActivity.createProfileData(oldname, oldage, oldweight, oldheight, oldbmi, true, email);
                break;
            case "active":
                Toast.makeText(mActivity, "Premium User Activated", Toast.LENGTH_SHORT).show();
                break;
            case "deactivated":
                Toast.makeText(mActivity, "Premium User Deactivated", Toast.LENGTH_SHORT).show();
                break;
        }

    }
}
