package com.example.calldev;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CalendarView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.calldev.item.EventItem;
import com.example.calldev.other.GlobalStorage;
import com.example.calldev.server.ServerEventAsyncTask;
import com.example.calldev.server.ServerProfileAsyncTask;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Date: Nov 28-2020.
 * This is a class that extends activity and represents the calendar page of the app.
 * In this page the user can access a calendar, create events in it and access the other pages of the app.
 * @author CALDEV.
 */

public class CalendarActivity extends AppCompatActivity
{
    /**
     * String constant used to distinguish this activity (used in debugging and to show the user pop up messages).
     */
    private static final String TAG = "CalendarActivity";
    /**
     * Integer constant used to distinguish the add event request.
     */
    private static final int ADD_EVENT_FROM_CAL_REQUEST = 1;
    /**
     * String constant passed to the LoginActivity class to signal that the user logout.
     */
    public static final String EXTRA_CALENDAR_LOGOUT = "com.example.login.example.EXTRA_CALENDAR_LOGOUT";

    /**
     * BottomNavigationView XML object used by the bottom menu of the app.
     */
    private BottomNavigationView CalendarBotNavigationView;
    /**
     * CalendarView XML object used to represent the calendar.
     */
    private CalendarView CalendarCalendarView;
    /**
     * TextView XML object used to show the current selected date.
     */
    private TextView DateTextView;
    /**
     * ImageView XML object used to display ads.
     */
    private ImageView CalendarAdImageView;

    /**
     * String value of the user's email address.
     */
    private String email;
    /**
     * String value of the current date.
     */
    private String date = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
    /**
     * String value of the previous date selected in the calendar.
     */
    private String oldDate = "";

    /**
     * Called whenever this activity is created.
     * Sets up the XML variables of the activity and the title of the page.
     * Checks for the email address of the user.
     * Also sets up the click feature on the calendar.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        setTitle("Calendar");

        CalendarBotNavigationView = findViewById(R.id.CalendarBottomNavigationView);
        CalendarBotNavigationView.setOnNavigationItemSelectedListener(CalendarnavListener);

        CalendarCalendarView = findViewById(R.id.CalendarCalendarView);
        DateTextView = findViewById(R.id.DateTextView);
        CalendarAdImageView = findViewById(R.id.CalendarAdImageView);

        DateTextView.setText(date);

        checkIncomingEmailIntent();

        checkUser();

        CalendarCalendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener()
        {
            /**
             * Method that displays the date in text when the user selects that specific date in the calendar.
             * If the users selects the same date more than once, he will be promptly asked to add a new event on that specific date.
             * @param view the CalendarView object in which the user selected the date.
             * @param year year of the date that the user selected.
             * @param month month of the date that the user selected.
             * @param dayOfMonth day of the date that the user selected.
             */
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth)
            {
                oldDate = date;
                date = dayOfMonth + "-" + (month+1) + "-" + year; //month + 1 (Jan = 0, Fev = 1, etc...)
                DateTextView.setText(date);

                if(oldDate.equals(date)){
                    Intent intent = new Intent(getBaseContext(), AddEditEventActivity.class);
                    intent.putExtra("EXTRA_DATA", date);
                    startActivityForResult(intent, ADD_EVENT_FROM_CAL_REQUEST);
                }
            }
        });
    }

    /**
     * Method called when the AddEditEventActivity, that was launched here, is exited,
     * giving the requestCode started it with, the resultCode it returned, and the new event data inserted by the user.
     * Handles the new/edited data to the server via the ServerEventAsyncTask class.
     * @param requestCode code indicating which activity result is incoming.
     * @param resultCode code indicating the result of the incoming activity result.
     * @param data Intent (containing result data) returned by the the incoming activity result.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == ADD_EVENT_FROM_CAL_REQUEST && resultCode == RESULT_OK)
        {
            String newtype = data.getStringExtra(AddEditEventActivity.EXTRA_NEW_TYPE);
            String newname = data.getStringExtra(AddEditEventActivity.EXTRA_NEW_NAME);
            String newstarttime = data.getStringExtra(AddEditEventActivity.EXTRA_NEW_START_TIME);
            String newstartdate = data.getStringExtra(AddEditEventActivity.EXTRA_NEW__START_DATE);
            String newendtime = data.getStringExtra(AddEditEventActivity.EXTRA_NEW_END_TIME);
            String newenddate = data.getStringExtra(AddEditEventActivity.EXTRA_NEW_END_DATE);

            EventItem neweventItem = new EventItem(R.drawable.ic_launcher_background, newtype, newname, newstarttime, newstartdate, newendtime, newenddate);

            new ServerEventAsyncTask(this, "add", email, neweventItem).execute();

            Toast.makeText(this, "Event Saved", Toast.LENGTH_SHORT).show();
        }
        else
        {
            Toast.makeText(this, "Event not Saved", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Method called upon the user clicking the logoff button on the top right corner of the page.
     * Heads the user to the login page first displayed when the user enters the app.
     */
    private void onClickLogOff()
    {
        Intent logoffcalendar = new Intent(this, LoginActivity.class);
        logoffcalendar.putExtra(EXTRA_CALENDAR_LOGOUT, "LOGOUT");
        logoffcalendar.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(logoffcalendar);
        finish();//acaba esta activity
    }

    /**
     * Method responsible for checking if the user has premium status or not using the ServerProfileAsyncTask class to retrieve the stored values of this and the settings.
     * It then analyses the values stored in the GlobalStorage class.
     * Depending on the user's status, ads will be displayed or not.
     */
    private void checkUser()
    {
        new ServerProfileAsyncTask(CalendarActivity.this, "settings", email).execute();
        if(GlobalStorage.getInstance().isPremium())
        {
            CalendarAdImageView.setVisibility(View.INVISIBLE);
        }
        else
        {
            CalendarAdImageView.setVisibility(View.VISIBLE);
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
        menuInflater.inflate(R.menu.logoff_menu, menu);
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
        if (item.getItemId() == R.id.LogOffMenu)
        {
            onClickLogOff();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Object responsible for handling with the navigation between the app's main 4 pages.
     */
    private final BottomNavigationView.OnNavigationItemSelectedListener CalendarnavListener = new BottomNavigationView.OnNavigationItemSelectedListener()
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
                    break;
                case R.id.EventsMenu:
                    Intent calendartoevents = new Intent(CalendarActivity.this, EventsActivity.class);
                    calendartoevents.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(calendartoevents);
                    finish();
                    break;
                case R.id.GroupsMenu:
                    Intent calendartogroups = new Intent(CalendarActivity.this, GroupsActivity.class);
                    calendartogroups.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(calendartogroups);
                    finish();
                    break;
                case R.id.SettingsMenu:
                    Intent calendartoprofile = new Intent(CalendarActivity.this, ProfileActivity.class);
                    calendartoprofile.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(calendartoprofile);
                    finish();
                    break;
            }
            return true;
        }
    };
}