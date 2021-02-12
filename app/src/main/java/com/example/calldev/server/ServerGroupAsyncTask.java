package com.example.calldev.server;

import android.os.AsyncTask;
import android.view.View;
import android.widget.Toast;

import com.example.calldev.BrowseGroupsActivity;
import com.example.calldev.GroupsActivity;
import com.example.calldev.ShareGroupsActivity;
import com.example.calldev.item.GroupItem;
import com.example.calldev.other.GlobalStorage;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
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
 * Date: Dec 5-2020.
 * An asynchronous task that handles the communication with the server regarding any group modification.
 * @author CALDEV.
 */

public class ServerGroupAsyncTask extends AsyncTask<Void, Void, String>
{
    /**
     * String constant used to distinguish this class (used in debugging).
     */
    private static final String TAG = "ServerGroupAsyncTask";
    /**
     * GroupsActivity object used to display pop up messages and update the groups in the GroupsActivity class.
     */
    private GroupsActivity viewActivity;
    /**
     * BrowseGroupsActivity object used to display pop up messages and update the public groups in the BrowseGroupsActivity class.
     */
    private BrowseGroupsActivity chooseActivity;
    /**
     * ShareGroupsActivity object used to display pop up messages and update the groups' users in the ShareGroupsActivity class.
     */
    private ShareGroupsActivity shareActivity;
    /**
     * Object used to represent the group's users and groups.
     */
    private GroupItem groupItem;
    /**
     * String value of the type of operation required and passed by the GroupsActivity/BrowseGroupsActivity/ShareGroupsActivity class.
     */
    private String flag;
    /**
     * String value of the user's email address.
     */
    private final String email;
    /**
     * String value of the new user's email address inserted in the ShareGroupsActivity class.
     */
    private String newUser;
    /**
     * ArrayList object containing public groups in the form of GroupItem objects.
     */
    private final ArrayList<GroupItem> publicClubsList = new ArrayList<>();
    /**
     * ArrayList object containing groups' users in the form of GroupItem objects.
     */
    private final ArrayList<GroupItem> userClubsList = new ArrayList<>();
    /**
     * ArrayList object containing groups in the form of GroupItem objects.
     */
    private final ArrayList<GroupItem> groupUsersList = new ArrayList<>();
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
     * Constructor used when a specific group that the user is part of is added/eliminated.
     * @param groupsActivity GroupsActivity class that spawned this task.
     * @param flag a string that differentiates the operation that is required.
     * @param email the email address of the user.
     * @param groupItem the contents of the group.
     */
    public ServerGroupAsyncTask(GroupsActivity groupsActivity, String flag, String email, GroupItem groupItem) {
        this.viewActivity = groupsActivity;
        this.flag = flag;
        this.email = email;
        this.groupItem = groupItem;
    }

    /**
     * Constructor used to retrieve all groups that the user is a part of.
     * @param groupsActivity GroupsActivity class that spawned this task.
     * @param flag a string that differentiates the operation that is required.
     * @param email the email address of the user.
     */
    public ServerGroupAsyncTask(GroupsActivity groupsActivity, String flag, String email) {
        this.viewActivity = groupsActivity;
        this.flag = flag;
        this.email = email;
    }

    /**
     * Constructor used when a specific public group is selected by the user.
     * @param browseGroupsActivity BrowseGroupsActivity class that spawned this task.
     * @param flag a string that differentiates the operation that is required.
     * @param email the email address of the user.
     * @param groupItem the contents of the selected group.
     */
    public ServerGroupAsyncTask(BrowseGroupsActivity browseGroupsActivity, String flag, String email, GroupItem groupItem) {
        this.chooseActivity = browseGroupsActivity;
        this.flag = flag;
        this.email = email;
        this.groupItem = groupItem;
    }

    /**
     * Constructor used to retrieve public groups.
     * @param browseGroupsActivity BrowseGroupsActivity class that spawned this task.
     * @param flag a string that differentiates the operation that is required.
     * @param email the email address of the user.
     */
    public ServerGroupAsyncTask(BrowseGroupsActivity browseGroupsActivity, String flag, String email) {
        this.chooseActivity = browseGroupsActivity;
        this.flag = flag;
        this.email = email;
    }

    /**
     * Constructor used to retrieve all the users of a group.
     * @param shareGroupsActivity ShareGroupsActivity class that spawned this task.
     * @param flag a string that differentiates the operation that is required.
     * @param email the email address of the user.
     */
    public ServerGroupAsyncTask(ShareGroupsActivity shareGroupsActivity, String flag, String email, GroupItem groupItem) {
        this.shareActivity = shareGroupsActivity;
        this.flag = flag;
        this.email = email;
        this.groupItem = groupItem;
    }

    /**
     * Constructor used to add a new user to a group.
     * @param shareGroupsActivity ShareGroupsActivity class that spawned this task.
     * @param flag a string that differentiates the operation that is required.
     * @param email the email address of the user.
     * @param newUser the email address of the new user added.
     * @param groupItem the contents of the group that the new user was added into.
     */
    public ServerGroupAsyncTask(ShareGroupsActivity shareGroupsActivity, String flag, String email, String newUser, GroupItem groupItem) {
        this.shareActivity = shareGroupsActivity;
        this.flag = flag;
        this.email = email;
        this.newUser = newUser;
        this.groupItem = groupItem;
    }

    /**
     * Background task to communicate with the server based on the type of operation required and passed by the GroupActivity/AddGroupsActivity/ShareGroupsActivity class.
     * @param voids the parameters passed to the task, in this case there are none.
     * @return A result then passed to the onPostExecute() method.
     */
    @Override
    protected String doInBackground(Void... voids) {
        switch (flag) {
            case "join":
                flag = "joined";
                AddUserToGroupOnServer();
                break;
            case "delete":
                RemoveUserFromGroupOnServer(0);
                break;
            case "new club":
                NewGroupOnServer();
                break;
            case "add other":
                flag = "added";
                AddUserToOtherGroupOnServer();
                break;
            case "get inserted":
                flag = "done";
                GetUserClubs();
                break;
            case "get all":
                flag = "retrieved";
                GetAllPublicClubs();
                break;
            case "get users":
                flag = "retrieved users";
                GetAllUsers();
                break;
            case "remove user":
                flag = "user removed";
                RemoveUserFromGroupOnServer(1);
                break;
        }
        return null;
    }

    /**
     * Method to inform server that we want to create a new club.
     */
    private void NewGroupOnServer() {
        try {
            postRequest = new HttpPost(API_STR+"/NewClub");

            StringEntity input = new StringEntity(formatNewClubAsJSON());
            input.setContentType("application/json");
            postRequest.setEntity(input);
            httpResponse = httpClient.execute(postRequest);

            httpResult = httpResponse.getEntity();
            groupItem.setId(EntityUtils.toString(httpResult, "UTF-8"));
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
     * Method to format the JSON we want to send to server.
     * @return JSON with User's email, Club's name, Club's Owner Email, Club's State.
     */
    private String formatNewClubAsJSON() {
        final JSONObject root = new JSONObject();
        try {
            root.put("email", email);
            root.put("name",groupItem.getName());
            root.put("ownerEmail",groupItem.getOwnerEmail());
            root.put("private",groupItem.isState());

            return root.toString();
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Method to inform server that we want to join a new group.
     */
    private void AddUserToGroupOnServer() {
        try {
            postRequest = new HttpPost(API_STR+"/Add2NewGroup");

            StringEntity input = new StringEntity(formatUserAsJSON(0));
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
     * Method to inform server that we want to add another User to a group.
     */
    private void AddUserToOtherGroupOnServer() {
        try {
            postRequest = new HttpPost(API_STR+"/AddOtherClub");

            StringEntity input = new StringEntity(formatOtherClubAsJSON());
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
     * Method to format the JSON we want to send to server.
     * @return JSON with User's email, User's email that we want to add, Club's ID.
     */
    private String formatOtherClubAsJSON() {
        final JSONObject root = new JSONObject();
        try {
            root.put("myEmail", email);
            root.put("otherEmail", newUser);
            root.put("clubID",groupItem.getId());

            return root.toString();
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Method to remove user from group. Club's owner can remove him or User can remove himself.
     * @param i - 0 if user removes himself or 1 if club's owner remove User.
     */
    private void RemoveUserFromGroupOnServer(int i) {
        try {
            postRequest = new HttpPost(API_STR+"/RemoveMeFromGroup");

            StringEntity input = new StringEntity(formatUserAsJSON(i));
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
     * Method to format the JSON we want to send to server.
     * @param i - 0 if user removes himself or 1 if club's owner remove User.
     * @return JSON with User's email, Clubs's name, Clubs's id, Club's state.
     */
    private String formatUserAsJSON(int i) {
        final JSONObject root = new JSONObject();
        try {
            if (i==0) root.put("email", email);
            else if (i==1) root.put("email", this.newUser);
            else root.put("email","google.com");
            root.put("name",groupItem.getName());
            root.put("id",groupItem.getId());
            root.put("private",groupItem.isState());

            return root.toString();
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Method to inform server that we want to get all public groups/clubs.
     */
    private void GetAllPublicClubs() {
        try {
            HttpGet getRequest = new HttpGet(API_STR + "/GetAllPublicGroups");
            httpResponse = httpClient.execute(getRequest);

            httpResult = httpResponse.getEntity();
            String JSONString = EntityUtils.toString(httpResult, "UTF-8");
            JSONObject jsonObj = new JSONObject(JSONString);

            JSONArray groups = jsonObj.getJSONArray("clubL");

            for (int i=0; i<groups.length(); i++) {
                JSONObject c = groups.getJSONObject(i);
                String name = c.getString("name");
                String id = c.getString("id");
                String owner = c.getString("ownerEmail");
                boolean priv = c.getBoolean("priv");

                // tmp hash map for single contact
                GroupItem g = new GroupItem();

                g.setName(name);
                g.setId(id);
                g.setOwnerEmail(owner);
                g.setState(priv);

                // adding group to group List
                publicClubsList.add(g);
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
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method to inform server that we want to get User's Club.
     */
    private void GetUserClubs() {
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

            for (int i=0; i<groups.length(); i++) {
                JSONObject c = groups.getJSONObject(i);
                String name = c.getString("name");
                String id = c.getString("id");
                String owner = c.getString("ownerEmail");
                boolean priv = c.getBoolean("priv");

                // tmp hash map for single contact
                GroupItem g = new GroupItem();

                g.setName(name);
                g.setId(id);
                g.setOwnerEmail(owner);
                g.setState(priv);

                // adding group to group List
                userClubsList.add(g);
            }

            httpResponse.toString();
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
     * Method to inform server that we want to get all Users from a group/club.
     */
    private void GetAllUsers() {
        try {
            postRequest = new HttpPost(API_STR+"/GetAllUsersFromGroup");

            StringEntity input = new StringEntity(groupItem.getId());
            input.setContentType("application/json");
            postRequest.setEntity(input);
            httpResponse = httpClient.execute(postRequest);

            httpResult = httpResponse.getEntity();
            String JSONString = EntityUtils.toString(httpResult, "UTF-8");

            JSONString = JSONString.replace("[","");
            JSONString = JSONString.replace("]","");
            JSONString = JSONString.replace("{","");
            JSONString = JSONString.replace("}","");

            String[] emails = JSONString.split(",");

            for (String email:emails) {
                GroupItem gi = new GroupItem();
                email = email.replace("\"","");
                gi.setName(email);
                groupUsersList.add(gi);
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
     * Method called upon completion of the doInBackground method displaying a message.
     * When the groups are retrieved from the server,
     * the createGroupData() method from the GroupActivity/AddGroupsActivity/ShareGroupsActivity class is called passing the retrieved groups.
     * It also updates the UI accordingly either on the GroupsActivity/AddGroupsActivity/ShareGroupsActivity page.
     * @param result the result of the operation computed by the doInBackground() method.
     */
    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        switch (flag) {
            case "done":
                for (int i = 0; i < userClubsList.size(); i++) {
                    viewActivity.createGroupData(userClubsList.get(i));
                }
                viewActivity.GroupBuildProgressBar.setVisibility(View.INVISIBLE);
                viewActivity.GroupRelativeLayout.setVisibility(View.VISIBLE);

                Toast.makeText(viewActivity, "Retrieved from Server", Toast.LENGTH_SHORT).show();
                break;
            case "retrieved":
                for (int i = 0; i < publicClubsList.size(); i++) {
                    chooseActivity.createGroupData(publicClubsList.get(i));
                }
                chooseActivity.AllGroupBuildProgressBar.setVisibility(View.INVISIBLE);
                chooseActivity.AllGroupRecyclerView.setVisibility(View.VISIBLE);

                Toast.makeText(chooseActivity, "Retrieved from Server", Toast.LENGTH_SHORT).show();
                break;
            case "retrieved users":
                for (int i = 0; i < groupUsersList.size(); i++) {
                    shareActivity.createGroupData(groupUsersList.get(i));
                }
                shareActivity.AllPeopleBuildProgressBar.setVisibility(View.INVISIBLE);
                shareActivity.AllPeopleRecyclerView.setVisibility(View.VISIBLE);

                Toast.makeText(shareActivity, "Retrieved from Server", Toast.LENGTH_SHORT).show();
                break;
            case "joined":
                Toast.makeText(chooseActivity, "Joined to Group", Toast.LENGTH_SHORT).show();
                break;
            case "added":
                Toast.makeText(shareActivity, "User Added", Toast.LENGTH_SHORT).show();
                break;
            case "user removed":
                Toast.makeText(shareActivity, "User Removed", Toast.LENGTH_SHORT).show();
                break;
            default:
                Toast.makeText(viewActivity, "Server Updated", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
