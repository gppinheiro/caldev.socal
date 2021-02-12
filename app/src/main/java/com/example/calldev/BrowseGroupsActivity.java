package com.example.calldev;

import android.os.Bundle;
import android.view.View;
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
 * Date: Dec 8-2020.
 * This is a class that extends activity and represents the subpage of the app in which the user can see all public groups that exists and can choose to be part of.
 * It is spawned from the GroupsActivity class.
 * The user must have premium status to access this class.
 * @author CALDEV.
 */
public class BrowseGroupsActivity extends AppCompatActivity
{
    /**
     * String constant used to distinguish this activity (used in debugging and to show the user pop up messages).
     */
    private static final String TAG = "BrowseGroupsActivity";

    /**
     * RecyclerView XML object used to show public groups.
     */
    public RecyclerView AllGroupRecyclerView;
    /**
     * ProgressBar XML object used before the public groups are loaded in.
     */
    public ProgressBar AllGroupBuildProgressBar;
    /**
     * GroupAdapter object used to display the groups to the AllGroupRecyclerView object.
     */
    private GroupAdapter AdapterRecyclerView;
    /**
     * LayoutManager object used to display the AllGroupRecyclerView object inside the BrowseGroupsActivity layout.
     */
    private RecyclerView.LayoutManager LayoutManagerRecyclerView;

    /**
     * String value of the user's email address.
     */
    private String email;
    /**
     * ArrayList object containing public groups in the form of GroupItem objects.
     */
    private final ArrayList<GroupItem> groupList = new ArrayList<>();

    /**
     * Called whenever this activity is created.
     * Sets up the XML variables of the activity and the title of the page.
     * All the public groups are retrieved via the ServerGroupAsyncTask class.
     * Also sets up the drag feature for joining a group.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_group);

        checkIncomingEmailIntent();

        setTitle("Available Groups");

        AllGroupBuildProgressBar = findViewById(R.id.AllGroupBuildProgressBar);
        AllGroupRecyclerView = findViewById(R.id.AllGroupRecyclerView);
        AllGroupRecyclerView.setHasFixedSize(true);

        AllGroupBuildProgressBar.setVisibility(View.VISIBLE);
        AllGroupRecyclerView.setVisibility(View.INVISIBLE);

        new ServerGroupAsyncTask(this, "get all", email).execute();

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT)
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
             * Specifically the dragging of the RecyclerView object to either the left side.
             * Doing this action results in signaling to the server to include the user in this group.
             * This action makes use of the ServerGroupAsyncTask class to signal the server to do this operation.
             * @param viewHolder the recyclerview ViewHolder responsible for setting upon the specific XML objects.
             * @param direction the direction of the action.
             */
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction)
            {
                new ServerGroupAsyncTask(BrowseGroupsActivity.this, "join", email, AdapterRecyclerView.getGroupItem(viewHolder.getAdapterPosition())).execute();
                Toast.makeText(BrowseGroupsActivity.this, "Group Selected", Toast.LENGTH_SHORT).show();
            }
        }).attachToRecyclerView(AllGroupRecyclerView);
    }

    /**
     * Method used to add the retrieved groups from the server and modify them on the app to an ArrayList so that they're data can be displayed on the UI.
     * Makes use of the GroupItem class to represent the groups.
     * @param groupItem the specific contents of the group retrieved.
     */
    public void createGroupData(GroupItem groupItem)
    {
        String grouponwer = groupItem.getOwnerEmail();
        String groupname = groupItem.getName();
        String groupid = groupItem.getId();
        boolean grouppriv = groupItem.isState();

        groupList.add(new GroupItem(R.drawable.ic_drag_arrow, grouponwer, groupname, groupid, grouppriv));

        LayoutManagerRecyclerView = new LinearLayoutManager(this);
        AllGroupRecyclerView.setLayoutManager(LayoutManagerRecyclerView);

        updateGroupData();
    }

    /**
     * Method called in which the UI is updated, via the GroupAdapter class, with the updated contents of the groups stored in the ArrayList.
     */
    private void updateGroupData()
    {
        AdapterRecyclerView = new GroupAdapter(groupList);
        AllGroupRecyclerView.setAdapter(AdapterRecyclerView);
    }

    /**
     * Method responsible for checking the email address of the user stored in the GlobalStorage class.
     * This way every activity has access to this variable which they can use to contact the server if needed.
     */
    private void checkIncomingEmailIntent()
    {
        email = GlobalStorage.getInstance().getEmail();
    }
}