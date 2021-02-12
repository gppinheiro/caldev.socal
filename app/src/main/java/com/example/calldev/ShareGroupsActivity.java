package com.example.calldev;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.calldev.item.GroupItem;
import com.example.calldev.other.GlobalStorage;
import com.example.calldev.server.ServerGroupAsyncTask;
import com.example.calldev.view.GroupAdapter;

import java.util.ArrayList;

/**
 * Date: Dec 9-2020.
 * This is a class that extends activity and represents the subpage of the app in which the user can see all the users that are part of a specific group.
 * It is spawned from the GroupsActivity class.
 * The user can also remove and add new users to this group if he has premium status and if he is the owner.
 * @author CALDEV.
 */

public class ShareGroupsActivity extends AppCompatActivity
{
    /**
     * String constant used to distinguish this activity (used in debugging and to show the user pop up messages).
     */
    private static final String TAG = "ShareGroupsActivity";
    /**
     * String constant passed to the GroupsActivity class with the selected group's id.
     */
    public static final String EXTRA_GROUP_ID = "com.example.login.example.EXTRA_GROUP_ID";
    /**
     * String constant passed to the GroupsActivity class with the selected group's name.
     */
    public static final String EXTRA_GROUP_NAME = "com.example.login.example.EXTRA_GROUP_NAME";
    /**
     * String constant passed to the GroupsActivity class with the selected group's owner.
     */
    public static final String EXTRA_GROUP_OWNER = "com.example.login.example.EXTRA_GROUP_OWNER";

    /**
     * EditText XML object used to insert the new user's name.
     */
    private EditText NewUserEditText;
    /**
     * RecyclerView XML object used to show the group's users.
     */
    public RecyclerView AllPeopleRecyclerView;
    /**
     * ProgressBar XML object used before the group's users are loaded in.
     */
    public ProgressBar AllPeopleBuildProgressBar;
    /**
     * GroupAdapter object used to display the groups to the AllPeopleRecyclerView object.
     */
    private GroupAdapter AdapterRecyclerView;
    /**
     * LayoutManager object used to display the AllPeopleRecyclerView object inside the ShareGroupsActivity layout.
     */
    private RecyclerView.LayoutManager LayoutManagerRecyclerView;

    /**
     * String value of the user's email address.
     */
    private String email;
    /**
     * Boolean value to distinguish if the user has sufficient permissions to add a new user to the selected group.
     */
    private boolean type = false;
    /**
     * ArrayList object containing the group's users in the form of GroupItem objects.
     */
    private final ArrayList<GroupItem> usersList = new ArrayList<>();
    /**
     * Object used to represent the group's users.
     */
    private GroupItem selectedUsersGroupItem;

    /**
     * Called whenever this activity is created.
     * Sets up the XML variables of the activity and the title of the page.
     * All the users that are part of this group are retrieved via the ServerGroupAsyncTask class.
     * Also sets up the drag feature for removing a user from this group.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_groups);

        setTitle("Users");

        NewUserEditText = findViewById(R.id.NewUserEditText);

        AllPeopleRecyclerView = findViewById(R.id.AllPeopleRecyclerView);
        AllPeopleRecyclerView.setHasFixedSize(true);
        AllPeopleBuildProgressBar = findViewById(R.id.AllPeopleBuildProgressBar);
        AllPeopleBuildProgressBar.setVisibility(View.VISIBLE);
        AllPeopleRecyclerView.setVisibility(View.INVISIBLE);

        checkIncomingEmailIntent();

        type = selectedGroup();

        new ServerGroupAsyncTask(this, "get users", email, selectedUsersGroupItem).execute();

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT)
        {
            /**
             * Method that allows the manipulation of the RecyclerView object.
             * Despite not using this method, it still needs to be implemented.
             * @return a false boolean.
             */
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) { return false; }

            /**
             * Method that allows the manipulation of the RecyclerView object.
             * Specifically the dragging of the RecyclerView object to either the left or right side.
             * Doing this action results in removing a user from the group that the user clicked in the GroupsActivity class.
             * This action makes use of the ServerGroupAsyncTask class to signal the server to do this operation.
             * This feature is exclusive to premium users and the owner of the group.
             * @param viewHolder the recyclerview ViewHolder responsible for setting upon the specific XML objects.
             * @param direction the direction of the action.
             */
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction)
            {
                if(type)
                {
                    new ServerGroupAsyncTask(ShareGroupsActivity.this, "remove user", email, AdapterRecyclerView.getGroupItem(viewHolder.getAdapterPosition()).getName(), selectedUsersGroupItem).execute();
                    usersList.remove(AdapterRecyclerView.getGroupItem(viewHolder.getAdapterPosition()));
                    Toast.makeText(ShareGroupsActivity.this, "User Removed", Toast.LENGTH_SHORT).show();
                    updateAllUsersData();
                }
                else
                {

                    Toast.makeText(ShareGroupsActivity.this, "Insufficient Permissions", Toast.LENGTH_SHORT).show();
                    updateAllUsersData();
                }
            }
        }).attachToRecyclerView(AllPeopleRecyclerView);
    }

    /**
     * Method used to add the retrieved users of the group from the server and modify them on the app to an ArrayList so that they're data can be displayed on the UI.
     * Makes use of the GroupItem class to represent the users of this group.
     * @param groupItem the specific contents of the user retrieved.
     */
    public void createGroupData(GroupItem groupItem)
    {
        String username = groupItem.getName();

        String membership;

        if(username.equals(selectedUsersGroupItem.getOwnerEmail()))
        {
            membership = "Owner";
        }
        else
        {
            membership = "Member";
        }

        usersList.add(new GroupItem(R.drawable.ic_user, username, membership));

        LayoutManagerRecyclerView = new LinearLayoutManager(this);
        AllPeopleRecyclerView.setLayoutManager(LayoutManagerRecyclerView);

        updateAllUsersData();
    }

    /**
     * Method called in which the UI is updated, via the GroupAdapter class, with the updated contents of the users data stored in the ArrayList.
     */
    private void updateAllUsersData()
    {
        AdapterRecyclerView = new GroupAdapter(usersList);
        AllPeopleRecyclerView.setAdapter(AdapterRecyclerView);
    }

    /**
     * Method that deals with receiving the data specific to the group that the user chose in the GroupsActivity class.
     * If the user is the owner of this group and has premium status, he will be able to add other users to this group.
     * If not he will only be able to see the members of this group.
     * @return a boolean true or false to distinguish these two actions.
     */
    private boolean selectedGroup()
    {
        Intent intent = getIntent();
        String groupid = intent.getStringExtra(EXTRA_GROUP_ID);
        String groupname = intent.getStringExtra(EXTRA_GROUP_NAME);
        String groupowner = intent.getStringExtra(EXTRA_GROUP_OWNER);
        boolean state = intent.getBooleanExtra("GroupState", false);

        selectedUsersGroupItem = new GroupItem(R.drawable.ic_launcher_background, groupowner, groupname, groupid, state);

        if(email.equals(groupowner) && GlobalStorage.getInstance().isPremium())
        {
            NewUserEditText.setVisibility(View.VISIBLE);
            return true;
        }
        else
        {
            NewUserEditText.setVisibility(View.INVISIBLE);
            return false;
        }
    }

    /**
     * Method that is called when the user presses the save button on the top right corner of the page.
     * If he has premium status, it will save the email address inserted and signal the server to add this new user to this group via the ServerGroupAsyncTask class.
     * It also checks for a valid input of the email address.
     * It also exits this subpage.
     */
    private void saveGroup()
    {
        if(type)
        {
            String newUser = NewUserEditText.getText().toString();
            if(!newUser.trim().isEmpty())
            {
                new ServerGroupAsyncTask(this, "add other", email, newUser, selectedUsersGroupItem).execute();
                Intent groupData = new Intent();
                setResult(RESULT_OK, groupData);
                finish();
            }
            else
            {
                Toast.makeText(ShareGroupsActivity.this, "Please Enter a Valid Email", Toast.LENGTH_SHORT).show();
            }
        }
        else
        {
            Intent groupData = new Intent();
            setResult(RESULT_OK, groupData);
            finish();
        }
    }

    /**
     * Method responsible for checking the email address of the user stored in the GlobalStorage class.
     * This way every activity has access to this variable which they can use to contact the server if needed.
     */
    private void checkIncomingEmailIntent()
    {
        email = GlobalStorage.getInstance().getEmail();
    }

    /**
     * Method responsible for setting up a menu from a XML file, upon which the UI can draw upon in the top right corner of the page.
     * @return a boolean that is true for the menu to be displayed.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.save_menu, menu);
        return true;
    }

    /**
     * Method responsible for dealing with the onclick interactions of the menu defined earlier.
     * Upon these interactions, the respective methods will be called upon.
     * @return a boolean true to allow menu processing to proceed in this activity.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == R.id.SaveMenu)
        {
            saveGroup();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}