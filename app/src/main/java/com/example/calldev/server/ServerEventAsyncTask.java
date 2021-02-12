package com.example.calldev.server;

import android.os.AsyncTask;
import android.os.Build;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.example.calldev.CalendarActivity;
import com.example.calldev.EventsActivity;
import com.example.calldev.item.EventItem;
import com.example.calldev.other.GlobalStorage;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

/**
 * Date: Nov 28-2020.
 * An asynchronous task that handles the communication with the server regarding any event modification.
 * @author CALDEV.
 */
public class ServerEventAsyncTask extends AsyncTask<Void, Void, String>
{
    /**
     * String constant used to distinguish this class (used in debugging).
     */
    private static final String TAG = "ServerEventAsyncTask";
    /**
     * EventsActivity object used to display pop up messages and update the events in the EventsActivity class.
     */
    private EventsActivity viewActivity;
    /**
     * CalendarActivity object used to display pop up messages in the CalendarActivity class.
     */
    private CalendarActivity calActivity = null;
    /**
     * ArrayList object containing events in the form of EventItem objects.
     */
    private ArrayList<EventItem> eventList = new ArrayList<EventItem>();
    /**
     * Object used to represent the events.
     */
    private EventItem eventItem;
    /**
     * String value of the type of operation required and passed by the EventsActivity/CalendarActivity class.
     */
    private String flag;
    /**
     * String value of the user's email address.
     */
    private final String email;

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
     * HTTP Response to receive information from our server.
     */
    private HttpResponse httpResponse = null;
    /**
     * Represents an HTTP request or response entity, consisting of headers and body.
     */
    private HttpEntity httpResult = null;

    /**
     * Constructor used when a specific event is added/edited/eliminated on the EventsActivity page.
     * @param eventsActivity EventActivity class that spawned this task.
     * @param flag a string that differentiates the operation that is required.
     * @param email the email address of the user.
     * @param eventItem the contents of the event.
     */
    public ServerEventAsyncTask(EventsActivity eventsActivity, String flag, String email, EventItem eventItem) {
        this.viewActivity = eventsActivity;
        this.flag = flag;
        this.email = email;
        this.eventItem = eventItem;
    }

    /**
     * Constructor used when a specific event is added/edited/eliminated on the CalendarActivity page.
     * @param calendarActivity CalendarActivity class that spawned this task.
     * @param flag a string that differentiates the operation that is required.
     * @param email the email address of the user.
     * @param eventItem the contents of the event.
     */
    public ServerEventAsyncTask(CalendarActivity calendarActivity, String flag, String email, EventItem eventItem) {
        this.calActivity = calendarActivity;
        this.flag = flag;
        this.email = email;
        this.eventItem = eventItem;
    }

    /**
     * Constructor used to retrieve the user's events from the server.
     * @param eventsActivity EventActivity class that spawned this task.
     * @param flag a string that differentiates the operation that is required.
     * @param email the email address of the user.
     */
    public ServerEventAsyncTask(EventsActivity eventsActivity, String flag, String email) {
        this.viewActivity = eventsActivity;
        this.flag = flag;
        this.email = email;
    }

    /**
     * Background task to communicate with the server based on the type of operation required and passed by the EventsActivity/CalendarActivity class.
     * @param voids the parameters passed to the task, in this case there are none.
     * @return A result then passed to the onPostExecute() method.
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected String doInBackground(Void... voids) {
        String[] temp = null;
        if(flag!=null) {
            temp = flag.split(":");
            flag = temp[0];
        }
        switch (flag)
        {
            case "add":
                AddEventOnServer();
                break;
            case "delete":
                DeleteEventOnServer();
                break;
            case "edit":
                EditEventOnServer();
                break;
            case "get":
                flag = "done";
                String s = getJSONFromServer();
                GetUserClubs();
                break;
            case "_month_":
                flag = "done";
                ViewEvent("_month_");
                break;
            case "_week_":
                flag = "done";
                ViewEvent("_week_");
                break;
            case "get groups":
                flag = "done";
                temp[0] = "";
                String str = String.join(":", temp);
                str = str.substring(1);
                ViewEvent(str);
                break;
        }
        return null;
    }

    /**
     * Method to inform server that we want to get all User's Event List.
     * @return String with HTTP Response or with an error message.
     */
    private String getJSONFromServer() {
        try {
            postRequest = new HttpPost(API_STR+"/GetAllEventList");

            StringEntity input = new StringEntity(email);
            input.setContentType("application/json");
            postRequest.setEntity(input);
            httpResponse = httpClient.execute(postRequest);

            httpResult = httpResponse.getEntity();
            String JSONString = EntityUtils.toString(httpResult, "UTF-8");

            DocJSON(JSONString);

            return httpResponse.toString();
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

        return "Unable to Contact Server";
    }

    /**
     * Method to format the JSON we received from server.
     */
    private void DocJSON(String JSONString) {
        try {
            JSONObject jsonObj = new JSONObject(JSONString);
            JSONArray events = jsonObj.getJSONArray("evList");

            for (int i = 0; i < events.length(); i++) {
                JSONObject c = events.getJSONObject(i);
                String name = c.getString("name");
                String initTime = c.getString("initTime");
                String endTime = c.getString("endTime");
                String id = c.getString("id");
                String catg = c.getString("catN");

                String[] FirstTime = initTime.split("T");
                String initDate = FirstTime[0];
                String initHour = FirstTime[1];

                String[] LastTime = endTime.split("T");
                String endDate = LastTime[0];
                String endHour = LastTime[1];

                // tmp hash map for single contact
                EventItem event = new EventItem();

                // adding each child node to HashMap key => value
                event.setName(name);
                event.setInitDate(initDate);
                event.setEndDate(endDate);
                event.setInitTime(initHour);
                event.setEndTime(endHour);
                event.setId(id);
                event.setType(catg);

                // adding event to event List
                eventList.add(event);
            }
        }
        catch(JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method to inform server that we want to add a new event on User's Calendar.
     * @return String with HTTP Response or with an error message.
     */
    private String AddEventOnServer() {
        try {
            postRequest = new HttpPost(API_STR+"/AddEvent");

            StringEntity input = new StringEntity(formatAddEventAsJSON());
            input.setContentType("application/json");
            postRequest.setEntity(input);
            httpResponse = httpClient.execute(postRequest);

            httpResult = httpResponse.getEntity();
            closeHTTP(httpResult);

            return httpResponse.toString();
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
        return "Unable to Contact Server";
    }

    /**
     * Method to format the JSON we want to send to server.
     * @return JSON with User's email and Event's Information.
     */
    private String formatAddEventAsJSON() {
        final JSONObject root = new JSONObject();
        try {
            root.put("email", email);
            root.put("name",eventItem.getName());
            root.put("stTime",eventItem.getStartTime());
            root.put("endTime",eventItem.getEndTime());
            root.put("stDate",eventItem.getStartDate());
            root.put("endDate",eventItem.getEndDate());
            root.put("catName",eventItem.getType());

            return root.toString();
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Method to inform server that we want to delete an event on User's Calendar.
     * @return String with HTTP Response or with an error message.
     */
    private String DeleteEventOnServer() {
        try {
            postRequest = new HttpPost(API_STR+"/DeleteEvent");

            StringEntity input = new StringEntity(formatDeleteEventAsJSON());
            input.setContentType("application/json");
            postRequest.setEntity(input);
            httpResponse = httpClient.execute(postRequest);

            httpResult = httpResponse.getEntity();
            closeHTTP(httpResult);

            return httpResponse.toString();
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
        return "Unable to Contact Server";
    }

    /**
     * Method to format the JSON we want to send to server.
     * @return JSON with User's email, Event's ID and Event's Category Name.
     */
    private String formatDeleteEventAsJSON() {
        final JSONObject root = new JSONObject();
        try {
            root.put("email", email);
            root.put("id", eventItem.getId());
            root.put("catName",eventItem.getType());

            return root.toString();
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Method to inform server that we want to edit an event on User's Calendar.
     * @return String with HTTP Response or with an error message.
     */
    private String EditEventOnServer() {
        try {
            postRequest = new HttpPost(API_STR+"/EditEvent");

            StringEntity input = new StringEntity(formatEditEventAsJSON());
            input.setContentType("application/json");
            postRequest.setEntity(input);
            httpResponse = httpClient.execute(postRequest);

            httpResult = httpResponse.getEntity();
            closeHTTP(httpResult);

            return httpResponse.toString();
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
        return "Unable to Contact Server";
    }

    /**
     * Method to format the JSON we want to send to server.
     * @return JSON with User's email and Event's Information.
     */
    private String formatEditEventAsJSON() {
        final JSONObject root = new JSONObject();
        try {
            root.put("email", email);
            root.put("name",eventItem.getName());
            root.put("stTime",eventItem.getStartTime());
            root.put("endTime",eventItem.getEndTime());
            root.put("stDate",eventItem.getStartDate());
            root.put("endDate",eventItem.getEndDate());
            root.put("catName",eventItem.getType());
            root.put("id", eventItem.getId());

            return root.toString();
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Method to inform server that we want to view events in a different way.
     * @param view - "_month_" if we want to see Monthly Events ; "_week_" if we want to see Weekly Events.
     * @return String with HTTP Response or with an error message.
     */
    private String ViewEvent(String view) {
        try {
            postRequest = new HttpPost(API_STR+"/GetEventListFromCatg");

            final JSONObject root = new JSONObject();

            root.put("email",email);
            root.put("catName",view);

            StringEntity input = new StringEntity(root.toString());
            input.setContentType("application/json");
            postRequest.setEntity(input);
            httpResponse = httpClient.execute(postRequest);

            httpResult = httpResponse.getEntity();
            String JSONString = EntityUtils.toString(httpResult, "UTF-8");

            eventList.clear();

            DocJSON(JSONString);

            return httpResponse.toString();
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
        catch (JSONException e) {
            e.printStackTrace();
        }

        return "Unable to Contact Server";
    }

    /**
     * Method to inform server that we want to get User's Club.
     * @return String with HTTP Response or with an error message.
     */
    private String GetUserClubs() {
        try {
            postRequest = new HttpPost(API_STR+"/GetMyClubs");

            StringEntity input = new StringEntity(email);
            input.setContentType("application/json");
            postRequest.setEntity(input);
            httpResponse = httpClient.execute(postRequest);

            httpResult = httpResponse.getEntity();
            String JSONString = EntityUtils.toString(httpResult, "UTF-8");

            JSONObject jsonObj = new JSONObject(JSONString);

            JSONArray groups = jsonObj.getJSONArray("userClubs");
            viewActivity.TYPES.clear();
            viewActivity.TYPESowner.clear();

            for (int i=0; i<groups.length(); i++) {
                JSONObject c = groups.getJSONObject(i);
                String name = c.getString("name");
                String owner = c.getString("ownerEmail");

                viewActivity.TYPES.add(name);
                viewActivity.TYPESowner.add(owner);
            }

            for(int i=0; i< viewActivity.TYPES.size(); i++)
            {
                if(! viewActivity.TYPESowner.get(i).equals(GlobalStorage.getInstance().getEmail())) {
                    viewActivity.TYPESowner.remove(i);
                    viewActivity.TYPES.remove(i);
                    i--;
                }
            }

            GlobalStorage.getInstance().setclubList(viewActivity.TYPES);
            GlobalStorage.getInstance().setClubOwnerList(viewActivity.TYPESowner);

            return httpResponse.toString();
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
        return "Unable to Contact Server";
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
     * Method called upon completion of the doInBackground() method displaying a message.
     * If the events are retrieved from the server,
     * the createEventData() method from the EventsActivity class is called passing the retrieved events.
     * It also updates the UI accordingly either on the CalendarActivity or the EventsActivity class.
     * @param result the result of the operation computed by the doInBackground() method.
     */
    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

         if(calActivity == null) {
             if(flag.equals("done")) {
                 viewActivity.EventBuildProgressBar.setVisibility(View.VISIBLE);
                 viewActivity.EventRelativeLayout.setVisibility(View.INVISIBLE);

                if(eventList.isEmpty()) {
                    viewActivity.updateEventData();
                }
                 for(int i = 0; i < eventList.size(); i++) {
                     viewActivity.createEventData(eventList.get(i));
                 }
                 viewActivity.EventBuildProgressBar.setVisibility(View.INVISIBLE);
                 viewActivity.EventRelativeLayout.setVisibility(View.VISIBLE);
                 Toast.makeText(viewActivity, "Events Lists Updated", Toast.LENGTH_SHORT).show();
             }
         }
         else {
             Toast.makeText(calActivity, "Event Created", Toast.LENGTH_SHORT).show();
         }
    }

}
