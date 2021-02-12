package com.example.calldev;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.calldev.item.ProfileItem;
import com.example.calldev.other.GlobalStorage;
import com.example.calldev.server.ServerProfileAsyncTask;
import com.example.calldev.view.ProfileAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

/**
* Date: Nov 28-2020.
* This is a class that extends activity and represents the profile/settings page of the app.
* In this page the user can create/edit/delete a profile associated with its email address.
* @author CALDEV.
*/

public class ProfileActivity extends AppCompatActivity
{
    /**
     * String constant used to distinguish this activity (used in debugging and to show the user pop up messages).
     */
    private static final String TAG = "ProfileActivity";
    /**
     * Integer constant used to distinguish the add new profile request.
     */
    private static final int ADD_PROFILE_REQUEST = 1;
    /**
     * Integer constant used to distinguish the edit profile request.
     */
    private static final int EDIT_PROFILE_REQUEST = 2;
    /**
     * String constant passed to the LoginActivity class to signal that the user logout.
     */
    public static final String EXTRA_PROFILE_LOGOUT = "com.example.login.example.EXTRA_PROFILE_LOGOUT";

    /**
     * BottomNavigationView XML object used by the bottom menu of the app.
     */
    private BottomNavigationView ProfileBottomNavigationView;
    /**
     * ImageView XML object used to display ads.
     */
    private ImageView ProfileAdImageView;
    /**
     * RecyclerView XML object used to show the profile data.
     */
    private RecyclerView ProfileRecyclerView;
    /**
     * ProfileAdapter object used to display the profile data to the ProfileRecyclerView object.
     */
    private ProfileAdapter AdapterRecyclerView;
    /**
     * LayoutManager object used to display the ProfileRecyclerView object inside the ProfileActivity layout.
     */
    private RecyclerView.LayoutManager LayoutManagerRecyclerView;

    /**
     * String value of the user's email address.
     */
    private String email;
    /**
     * Integer value that is incremented upon the user touch on the profile page icon.
     */
    private int counter = 0;
    /**
     * Boolean value of the events notifications.
     */
    private boolean eventset = false;
    /**
     * Boolean value of the gym notifications.
     */
    private boolean gymset = false;
    /**
     * ArrayList object containing the profile data in the form of ProfileItem objects.
     */
    private final ArrayList<ProfileItem> profileList = new ArrayList<>();

    /**
     * Called whenever this activity is created.
     * Sets up the XML variables of the activity and the title of the page.
     * Checks for the email address of the user and also checks if he has an associated profile or not.
     * If there is already an existent profile, this profile is retrieved via the ServerProfileAsyncTask class.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        setTitle("Profile");

        ProfileBottomNavigationView = findViewById(R.id.ProfileBottomNavigationView);
        ProfileBottomNavigationView.setOnNavigationItemSelectedListener(ProfilenavListener);

        ProfileRecyclerView = findViewById(R.id.ProfileRecyclerView);
        ProfileRecyclerView.setHasFixedSize(true); //melhora a eficacia da app

        ProfileAdImageView = findViewById(R.id.ProfileAdImageView);

        checkUser();

        checkIncomingEmailIntent();

        checkSettings();

        if(checkIncomingNoProfileIntent())
        {
            onClickAddProfile();
        }
        else
        {
            new ServerProfileAsyncTask(this, "get", email).execute();
        }
    }

    /**
     * Method called when the AddEditProfileActivity class, that was launched here, is exited,
     * giving the requestCode started it with, the resultCode it returned, and the new event data inserted by the user.
     *
     * Calls the createProfileData() method to send the new/edited data to the server and the updateProfileData() to update the UI accordingly.
     * @param requestCode code indicating which activity result is incoming.
     * @param resultCode code indicating the result of the incoming activity result.
     * @param data Intent (containing result data) returned by the incoming activity result.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == ADD_PROFILE_REQUEST && resultCode == RESULT_OK)
        {
            String newusername = data.getStringExtra(AddEditProfileActivity.EXTRA_NEW_USERNAME);
            String newage = data.getStringExtra(AddEditProfileActivity.EXTRA_NEW_AGE);
            String newweight = data.getStringExtra(AddEditProfileActivity.EXTRA_NEW_WEIGHT);
            String newheight = data.getStringExtra(AddEditProfileActivity.EXTRA_NEW_HEIGHT);
            String newbmi = data.getStringExtra(AddEditProfileActivity.EXTRA_NEW_BMI);
            String newemail = GlobalStorage.getInstance().getEmail();
            createProfileData(newusername, newage, newweight, newheight, newbmi, false, newemail);

            Toast.makeText(this, "Profile Data Saved", Toast.LENGTH_SHORT).show();

            GlobalStorage.getInstance().setHasProfile(true);
        }
        else if(requestCode == EDIT_PROFILE_REQUEST && resultCode == RESULT_OK)
        {
            profileList.clear();

            new ServerProfileAsyncTask(this, "delete", email, profileList).execute();

            String newusername = data.getStringExtra(AddEditProfileActivity.EXTRA_NEW_USERNAME);
            String newage = data.getStringExtra(AddEditProfileActivity.EXTRA_NEW_AGE);
            String newweight = data.getStringExtra(AddEditProfileActivity.EXTRA_NEW_WEIGHT);
            String newheight = data.getStringExtra(AddEditProfileActivity.EXTRA_NEW_HEIGHT);
            String newbmi = data.getStringExtra(AddEditProfileActivity.EXTRA_NEW_BMI);
            String newemail = data.getStringExtra(AddEditProfileActivity.EXTRA_EMAIL);

            createProfileData(newusername, newage, newweight, newheight, newbmi, false, newemail);

            Toast.makeText(this, "Profile Updated", Toast.LENGTH_SHORT).show();
        }
        else
        {
            Toast.makeText(this, "Profile not Saved", Toast.LENGTH_SHORT).show();
        }

        updateProfileData();
    }

    /**
     * Method used to add the retrieved profile from the server and modify it on the app to an ArrayList so that it's data can be displayed on the UI.
     * It also sets up the settings regarding the notifications depending if the user has premium status or not.
     * @param name the username of the user.
     * @param age the age of the user.
     * @param weight the weight of the user.
     * @param height the height of the user.
     * @param bmi the bmi of the user.
     * @param old a boolean to distinguish a profile modified on the app from a profile retrieved from the server.
     * @param email the email address of the user.
     */
    public void createProfileData(String name, String age, String weight, String height, String bmi, boolean old, String email)
    {
        email = email.replace("\"", "");
        name = name.replace("\"", "");
        this.email = email;


        profileList.add(new ProfileItem(R.drawable.ic_user, name, email));
        profileList.add(new ProfileItem("Age:", age, 1));
        profileList.add(new ProfileItem("Weight:", weight + " kg", 1));
        profileList.add(new ProfileItem("Height:", height + " cm", 1));
        profileList.add(new ProfileItem("BMI:", bmi, 1));

        profileList.add(new ProfileItem("Event Notifications", eventset, 2));
        GlobalStorage.getInstance().setEventNots(eventset);

        if(GlobalStorage.getInstance().isPremium())
        {
            profileList.add(new ProfileItem("Drink Water Notification", gymset, 2));
            GlobalStorage.getInstance().setGymNots(gymset);
        }
        if(!old)
        {
            new ServerProfileAsyncTask(this, "add", email, profileList).execute();
        }
        else
        {
            updateProfileData();
        }

        LayoutManagerRecyclerView = new LinearLayoutManager(this);
        ProfileRecyclerView.setLayoutManager(LayoutManagerRecyclerView);
    }

    /**
     * Method called in which the UI is updated, via the ProfileAdapter class, with the updated contents of the profile stored in the ArrayList.
     */
    private void updateProfileData()
    {
        AdapterRecyclerView = new ProfileAdapter(profileList);
        ProfileRecyclerView.setAdapter(AdapterRecyclerView);
    }

    /**
     * Method called upon the user clicking on the add profile button on the top right corner of the page.
     * It starts the AddEditProfileActivity class for the creation of a new profile.
     */
    public void onClickAddProfile()
    {
        Intent intent = new Intent(ProfileActivity.this, AddEditProfileActivity.class);
        intent.putExtra(AddEditProfileActivity.EXTRA_EMAIL, email);
        startActivityForResult(intent, ADD_PROFILE_REQUEST);
    }

    /**
     * Method called upon the user clicking on the edit profile button on the top right corner of the page.
     * It starts the AddEditProfileActivity class for the edit of the existent profile.
     * Its handles the email, the parameters of the existent profile to the AddEditProfileActivity class.
     */
    private void onClickEditProfile()
    {
        String oldusername = profileList.get(0).getText();
        String oldemail = profileList.get(0).getSubText();
        String[] olddage = profileList.get(1).getData().split(" ");
        String[] oldweight = profileList.get(2).getData().split(" ");
        String[] oldheight = profileList.get(3).getData().split(" ");

        Intent intent = new Intent(ProfileActivity.this, AddEditProfileActivity.class);
        intent.putExtra(AddEditProfileActivity.EXTRA_NEW_PROFILE_ID, 1);
        intent.putExtra(AddEditProfileActivity.EXTRA_EMAIL, oldemail);
        intent.putExtra(AddEditProfileActivity.EXTRA_NEW_USERNAME, oldusername);
        intent.putExtra(AddEditProfileActivity.EXTRA_NEW_AGE, olddage[0]);
        intent.putExtra(AddEditProfileActivity.EXTRA_NEW_WEIGHT, oldweight[0]);
        intent.putExtra(AddEditProfileActivity.EXTRA_NEW_HEIGHT, oldheight[0]);
        startActivityForResult(intent, EDIT_PROFILE_REQUEST);
    }

    /**
     * Method called upon the user clicking on the delete profile button on the top right corner of the page.
     * It deletes the profile and communicates this action to the server via the ServerProfileAsyncTask class.
     * It also updates the UI accordingly using the updateProfileData() method.
     */
    private void onClickDelete()
    {
        profileList.clear();
        new ServerProfileAsyncTask(this, "delete", email, profileList).execute();
        GlobalStorage.getInstance().setHasProfile(false);
        updateProfileData();
    }

    /**
     * Method called upon the user clicking the logoff button on the top right corner of the page.
     * Heads the user to the login page first displayed when the user enters the app.
     */
    private void onClickLogOff()
    {
        Intent logoffprofile = new Intent(this, LoginActivity.class);
        logoffprofile.putExtra(EXTRA_PROFILE_LOGOUT, "LOGOUT");
        logoffprofile.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(logoffprofile);
        finish();
    }

    /**
     * Method responsible for checking the state of the settings based on their values stored in the GlobalStorage class.
     * It also signals the server to store these values using the ServerProfileAsyncTask class.
     */
    private void checkSettings()
    {
        eventset = GlobalStorage.getInstance().hasEventnots();
        gymset = GlobalStorage.getInstance().hasGymnots();
    }

    /**
     * Method responsible for checking if the user has premium status or not using the GlobalStorage class.
     * Depending on the user's status, ads will be displayed or not.
     */
    private void checkUser()
    {
        if(GlobalStorage.getInstance().isPremium())
        {
            ProfileAdImageView.setVisibility(View.INVISIBLE);
            counter = 5;
        }
        else
        {
            ProfileAdImageView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Method responsible for checking if the user doesn't have a profile associated using the GlobalStorage class.
     * @return a boolean either true or false.
     */
    private boolean checkIncomingNoProfileIntent()
    {
        return !GlobalStorage.getInstance().hasProfile();
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
        menuInflater.inflate(R.menu.profile_menu, menu);
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
            case R.id.AddProfileMenu:
                if(profileList.isEmpty())
                {
                    onClickAddProfile();
                }
                else
                {
                    Toast.makeText(this, "There is already a profile", Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.EditProfileMenu:
                if(!profileList.isEmpty())
                {
                    onClickEditProfile();
                }
                else
                {
                    Toast.makeText(this, "Can't edit a nonexistent profile", Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.DeleteProfileMenu:
                if(!profileList.isEmpty())
                {
                    onClickDelete();
                }
                else
                {
                    Toast.makeText(this, "Can't delete a nonexistent profile", Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.LogOffProfileMenu:
                onClickLogOff();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Object responsible for handling with the navigation between the app's main 4 pages.
     */
    private final BottomNavigationView.OnNavigationItemSelectedListener ProfilenavListener = new BottomNavigationView.OnNavigationItemSelectedListener()
    {
        /**
         * Method that deals with the onclick interactions of the bottom menu of the app.
         * Upon clicking each button the user will be promptly redirected to the respective page.
         * The user will be granted premium status if he clicks the profile symbol 5 times and if he already has an associated profile.
         * To deactivate the premium status, the user needs to repeat the same process again.
         * This latest action will update the UI accordingly and also signals the server to store these values using the ServerProfileAsyncTask class.
         * @param item the different items inside the bottom menu.
         * @return a boolean that is true to display the item as the selected item.
         */
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item)
        {
            switch (item.getItemId())
            {
                case R.id.CalendarMenu:
                    Intent profiletocalendar = new Intent(ProfileActivity.this, CalendarActivity.class);
                    profiletocalendar.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(profiletocalendar);
                    finish();
                    break;
                case R.id.EventsMenu:
                    Intent profiletoevents = new Intent(ProfileActivity.this, EventsActivity.class);
                    profiletoevents.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(profiletoevents);
                    finish();
                    break;
                case R.id.GroupsMenu:
                    Intent profiletogroups = new Intent(ProfileActivity.this, GroupsActivity.class);
                    profiletogroups.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(profiletogroups);
                    finish();
                    break;
                case R.id.SettingsMenu:
                    counter += 1;
                    String operation;
                    if(counter == 5 && !profileList.isEmpty())
                    {
                        GlobalStorage.getInstance().setPremium(true);
                        operation = "premium active";
                        new ServerProfileAsyncTask(ProfileActivity.this, operation, email).execute();
                        profileList.add(new ProfileItem("Drink Water Notification", gymset, 2));
                        GlobalStorage.getInstance().setGymNots(gymset);
                        updateProfileData();
                        ProfileAdImageView.setVisibility(View.INVISIBLE);
                    }
                    else if(counter == 10 && !profileList.isEmpty())
                    {
                        GlobalStorage.getInstance().setPremium(false);
                        operation = "premium deactivated";
                        new ServerProfileAsyncTask(ProfileActivity.this, operation, email).execute();
                        profileList.remove(6);
                        GlobalStorage.getInstance().setGymNots(gymset);
                        updateProfileData();
                        ProfileAdImageView.setVisibility(View.VISIBLE);
                        counter = 0;
                    }
                    else if(profileList.isEmpty())
                    {
                        Toast.makeText(ProfileActivity.this, "You need to Create a Profile First", Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
            return true;
        }
    };
}