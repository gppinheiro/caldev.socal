package com.example.calldev;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.calldev.item.GroupItem;
import com.example.calldev.other.GlobalStorage;
import com.example.calldev.server.ServerGroupAsyncTask;
import com.example.calldev.view.GroupAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

/**
* Date: Dec 5-2020.
* This is a class that extends activity and represents the groups page of the app.
* In this page the user can see in which groups he is a part of and view all the members of a group.
* If the user has a premium account he can create groups for himself, join public groups and, if he is the owner of that group, add/remove other users to that group.
* @author CALDEV.
*/

public class GroupsActivity extends AppCompatActivity
{
    /**
     * String constant used to distinguish this activity (used in debugging and to show the user pop up messages).
     */
    private static final String TAG = "GroupsActivity";
    /**
     * Integer constant used to distinguish the create new group request.
     */
    private static final int NEW_GROUP_REQUEST = 1;
    /**
     * Integer constant used to distinguish the join group request.
     */
    private static final int JOIN_GROUP_REQUEST = 2;
    /**
     * Integer constant used to distinguish the share group request.
     */
    private static final int SHARE_GROUP_REQUEST = 3;
    /**
     * String constant passed to the LoginActivity class to signal that the user logout.
     */
    public static final String EXTRA_GROUPS_LOGOUT = "com.example.login.example.EXTRA_GROUPS_LOGOUT";

    /**
     * BottomNavigationView XML object used by the bottom menu of the app.
     */
    private BottomNavigationView GroupsBottomNavigationView;
    /**
     * ImageView XML object used to display ads.
     */
    private ImageView GroupAdImageView;
    /**
     * RecyclerView XML object used to show groups.
     */
    private RecyclerView GroupRecyclerView;
    /**
     * ProgressBar XML object used before the groups are loaded in.
     */
    public ProgressBar GroupBuildProgressBar;
    /**
     * RelativeLayout XML object used for the GroupRecyclerView object.
     */
    public RelativeLayout GroupRelativeLayout;
    /**
     * GroupAdapter object used to display the groups to the GroupRecyclerView object.
     */
    private GroupAdapter AdapterRecyclerView;
    /**
     * LayoutManager object used to display the GroupRecyclerView object inside the GroupsActivity layout.
     */
    private RecyclerView.LayoutManager LayoutManagerRecyclerView;

    /**
     * String value of the user's email address.
     */
    private String email;
    /**
     * ArrayList object containing groups in the form of GroupItem objects.
     */
    private final ArrayList<GroupItem> groupList = new ArrayList<>();

    /**
     * Called whenever this activity is created.
     * Sets up the XML variables of the activity and the title of the page.
     * Checks for the email address of the user.
     * If there is already any existent groups in which the user is included, these are retrieved via the ServerGroupAsyncTask class.
     * Also sets up the drag feature for removing the user from a group.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups);

        setTitle("Groups");

        GroupsBottomNavigationView = findViewById(R.id.GroupsBottomNavigationView);
        GroupsBottomNavigationView.setOnNavigationItemSelectedListener(GroupsNavListener);

        GroupBuildProgressBar = findViewById(R.id.GroupBuildProgressBar);
        GroupRelativeLayout = findViewById(R.id.GroupRelativeLayout);
        GroupRecyclerView = findViewById(R.id.GroupRecyclerView);
        GroupRecyclerView.setHasFixedSize(true); //melhora a eficacia da app

        GroupAdImageView = findViewById(R.id.GroupAdImageView);

        GroupBuildProgressBar.setVisibility(View.VISIBLE);
        GroupRelativeLayout.setVisibility(View.INVISIBLE);

        checkUser();

        checkIncomingEmailIntent();

        new ServerGroupAsyncTask(this, "get inserted", email).execute();

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
             * Doing this action results in abandoning the affected group.
             * This action makes use of the ServerGroupAsyncTask class to signal the server to do this operation.
             * This feature is exclusive to premium users or if the user is the owner of the group.
             * @param viewHolder the recyclerview ViewHolder responsible for setting upon the specific XML objects.
             * @param direction  the direction of the action.
             */
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction)
            {
                if(GlobalStorage.getInstance().isPremium() || email.equals(AdapterRecyclerView.getGroupItem(viewHolder.getAdapterPosition()).getOwnerEmail()))
                {
                    new ServerGroupAsyncTask(GroupsActivity.this, "delete", email, AdapterRecyclerView.getGroupItem(viewHolder.getAdapterPosition())).execute();
                    groupList.remove(AdapterRecyclerView.getGroupItem(viewHolder.getAdapterPosition()));
                    Toast.makeText(GroupsActivity.this, "Group Removed", Toast.LENGTH_SHORT).show();
                    updateGroupData();
                }
                else
                {
                    updateGroupData();
                    Toast.makeText(GroupsActivity.this, "Insufficient Permissions", Toast.LENGTH_SHORT).show();
                }
            }
        }).attachToRecyclerView(GroupRecyclerView);
    }

    /**
     *  Method that overrides and setups an onclick interaction using the setOnGroupClickListener() method of the GroupAdapter class.
     */
    private void enableShareOption()
    {
        AdapterRecyclerView.setOnGroupClickListener(new GroupAdapter.onGroupClickListener()
        {
            /**
             * Method that handles the onclick interaction with the specific group that the user touches.
             * It starts the ShareGroupsActivity class for the viewing of the users of the selected group and for adding new members.
             * Handles the contents of the selected group to the ShareGroupsActivity class.
             */
            @Override
            public void onGroupClick(GroupItem groupItem)
            {
                Intent intent = new Intent(GroupsActivity.this, ShareGroupsActivity.class);
                intent.putExtra(ShareGroupsActivity.EXTRA_GROUP_ID, groupItem.getId());
                intent.putExtra(ShareGroupsActivity.EXTRA_GROUP_NAME, groupItem.getName());
                intent.putExtra(ShareGroupsActivity.EXTRA_GROUP_OWNER, groupItem.getOwnerEmail());
                intent.putExtra("GroupState", groupItem.isState());
                startActivityForResult(intent, SHARE_GROUP_REQUEST);
            }
        });
    }
    
    /**
     * Method called when the AddGroupsActivity class, BrowseGroupsActivity or ShareGroupsActivity class, that was launched here, is exited,
     * giving the requestCode started it with, the resultCode it returned, and the new group data inserted by the user.
     * Handles the new group created by the user to the server via the ServerGroupAsyncTask class and gets the updated data from the server.
     * @param requestCode code indicating which activity result is incoming.
     * @param resultCode code indicating the result of the incoming activity result.
     * @param data Intent (containing result data) returned by the incoming activity result.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        GroupBuildProgressBar.setVisibility(View.VISIBLE);
        GroupRelativeLayout.setVisibility(View.INVISIBLE);

        if(requestCode == NEW_GROUP_REQUEST && resultCode == RESULT_OK)
        {
            String groupName = data.getStringExtra(AddGroupActivity.EXTRA_GROUP_NAME);
            String groupOption = data.getStringExtra(AddGroupActivity.EXTRA_GROUP_STATE);

            boolean priv = false;

            if(!groupOption.equals("Public"))
            {
                priv = true;
            }

            String ownerEmail = email;

            GroupItem newGroupItem = new GroupItem(R.drawable.ic_launcher_background, ownerEmail, groupName, priv);

            new ServerGroupAsyncTask(this, "new club", email, newGroupItem).execute();

            Toast.makeText(this, "Groups Created", Toast.LENGTH_SHORT).show();
        }
        else if(requestCode == JOIN_GROUP_REQUEST && resultCode == RESULT_OK)
        {
            Toast.makeText(this, "Groups Selected", Toast.LENGTH_SHORT).show();
        }
        else if(requestCode == SHARE_GROUP_REQUEST && resultCode == RESULT_OK)
        {
            Toast.makeText(this, "User Added", Toast.LENGTH_SHORT).show();
        }


        groupList.clear();

        new ServerGroupAsyncTask(this, "get inserted", email).execute();
    }

    /**
     * Method used to add the retrieved groups from the server and modify them on the app to an ArrayList so that they're data can be displayed on the UI.
     * Makes use of the GroupItem class to represent the groups.
     * @param groupItem the specific contents of the group retrieved.
     */
    public void createGroupData(GroupItem groupItem)
    {
        String groupOwner = groupItem.getOwnerEmail();
        String groupName = groupItem.getName();
        String groupId = groupItem.getId();
        boolean groupPriv = groupItem.isState();

        int catColor = setTypeColor(groupName);

        groupList.add(new GroupItem(catColor, groupOwner, groupName, groupId, groupPriv));

        LayoutManagerRecyclerView = new LinearLayoutManager(this);
        GroupRecyclerView.setLayoutManager(LayoutManagerRecyclerView);

        updateGroupData();
    }

    /**
     * Method called in which the UI is updated, via the GroupAdapter class, with the updated contents of the groups stored in the ArrayList.
     * It also calls the enableShareOption() method to enable the onclick interactions of the groups.
     */
    private void updateGroupData()
    {
        AdapterRecyclerView = new GroupAdapter(groupList);
        GroupRecyclerView.setAdapter(AdapterRecyclerView);

        enableShareOption();
    }

    /**
     * Method called upon the user clicking on the add group button on the top right corner of the page.
     * It starts the AddGroupActivity class for the creation of a new group.
     * This feature is exclusive to premium users.
     */
    private void onClickAddGroup()
    {
        if(GlobalStorage.getInstance().isPremium())
        {
            Intent newGroup = new Intent(GroupsActivity.this, AddGroupActivity.class);
            startActivityForResult(newGroup, NEW_GROUP_REQUEST);
        }
        else
        {
            Toast.makeText(this, "Can't Access this Feature", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Method called upon the user clicking on the browse groups button on the top right corner of the page.
     * It starts the BrowseGroupsActivity class for the viewing and selection of a group.
     * This feature is exclusive to premium users.
     */
    private void onClickBrowseGroup()
    {
        if(GlobalStorage.getInstance().isPremium())
        {
        Intent joinGroup = new Intent(GroupsActivity.this, BrowseGroupsActivity.class);
        startActivityForResult(joinGroup, JOIN_GROUP_REQUEST);
        }
        else
        {
            Toast.makeText(this, "Can't Access this Feature", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Method called upon the user clicking the logoff button on the top right corner of the page.
     * Heads the user to the login page first displayed when the user enters the app.
     */
    public void onClickLogOff()
    {
        Intent logoffEvents = new Intent(this, LoginActivity.class);
        logoffEvents.putExtra(EXTRA_GROUPS_LOGOUT, "LOGOUT");
        logoffEvents.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(logoffEvents);
        finish();
    }

    /**
     * Method responsible for checking if the user has premium status or not using the GlobalStorage class.
     * Depending on the user's status, ads will be displayed or not and certain feature will be available/unavailable.
     */
    private void checkUser()
    {
        if(GlobalStorage.getInstance().isPremium())
        {
            GroupAdImageView.setVisibility(View.INVISIBLE);
        }
        else
        {
            GroupAdImageView.setVisibility(View.VISIBLE);
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
     * Method that defines the color of the group based on it's category.
     * Currently there are 3 colors.
     * @param category the category of the group.
     * @return an integer that references to the the drawable XML file that the specific group will have.
     */
    private int setTypeColor(String category)
    {
        int colorCategory;

        if (category.equals("Sports") || category.equals("sports"))
        {
            colorCategory = R.drawable.ic_event_color_sports;
        }
        else if(category.equals("Gym") || category.equals("gym"))
        {
            colorCategory = R.drawable.ic_event_color_gym;
        }
        else
        {
            colorCategory = R.drawable.ic_event_color_misc;
        }
        return colorCategory;
    }

    /**
     * Method responsible for setting up a menu from a XML file, upon which the UI can draw upon in the top right corner of the page.
     * @return a boolean that is true for the menu to be displayed.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.groups_menu, menu);
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
        switch (item.getItemId())
        {
            case R.id.AddGroupMenu:
                onClickAddGroup();
                return true;
            case R.id.BrowseGroupMenu:
                onClickBrowseGroup();
                return true;
            case R.id.LogOffGroupMenu:
                onClickLogOff();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Object responsible for handling with the navigation between the app's main 4 pages.
     */
    private final BottomNavigationView.OnNavigationItemSelectedListener GroupsNavListener = new BottomNavigationView.OnNavigationItemSelectedListener()
    {
        /**
         * Method that deals with the onclick interactions of the bottom menu of the app.
         * Upon clicking each button the user will be promptly redirected to the respective page.
         * @param item the different items inside the bottom menu.
         * @return a boolean that is true to display the item as the selected item.
         */
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item)
        {
            switch (item.getItemId())
            {
                case R.id.CalendarMenu:
                    Intent groupstocalendar = new Intent(GroupsActivity.this, CalendarActivity.class);
                    groupstocalendar.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(groupstocalendar);
                    finish();
                    break;
                case R.id.EventsMenu:
                    Intent groupstoevents = new Intent(GroupsActivity.this, EventsActivity.class);
                    groupstoevents.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(groupstoevents);
                    finish();
                    break;
                case R.id.GroupsMenu:
                    break;
                case R.id.SettingsMenu:
                    Intent groupstoprofile = new Intent(GroupsActivity.this, ProfileActivity.class);
                    groupstoprofile.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(groupstoprofile);
                    finish();
                    break;
            }
            return true;
        }
    };
}