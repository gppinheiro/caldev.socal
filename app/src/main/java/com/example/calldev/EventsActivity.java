package com.example.calldev;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.calldev.item.EventItem;
import com.example.calldev.other.AlertReceiver;
import com.example.calldev.other.GlobalStorage;
import com.example.calldev.server.ServerEventAsyncTask;
import com.example.calldev.view.EventAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static com.example.calldev.other.Notifications.CHANNEL_1_ID;

/**
 * Date: Nov 28-2020.
 * This is a class that extends activity and represents the events page of the app.
 * In this page the user can see which events he has.
 * In this page the user can create/edit/delete events associated with its email address and google account.
 * @author CALDEV.
 */

public class EventsActivity extends AppCompatActivity
{
    /**
     * String constant used to distinguish this activity (used in debugging and to show the user pop up messages).
     */
    private static final String TAG = "EventsActivity";
    /**
     * Integer constant used to distinguish the add event request.
     */
    private static final int ADD_EVENT_REQUEST = 1;
    /**
     * Integer constant used to distinguish the edit event request.
     */
    private static final int EDIT_EVENT_REQUEST = 2;
    /**
     * Array of Strings constant containing the default event's types.
     */
    public ArrayList<String> TYPES = new ArrayList<String>();
    /**
     * Array of Strings constant containing the default event's types.
     */
    public ArrayList<String> TYPESowner = new ArrayList<String>();
    /**
     * String constant passed to the LoginActivity class to signal that the user logout.
     */
    public static final String EXTRA_EVENTS_LOGOUT = "com.example.login.example.EXTRA_EVENTS_LOGOUT";

    /**
     * BottomNavigationView XML object used by the bottom menu of the app.
     */
    private BottomNavigationView EventsBottomNavigationView;
    /**
     * TextView XML object used to show the current type of events shown.
     */
    private TextView EventShapeTextView;
    /**
     * ImageView XML object used to display ads.
     */
    private ImageView EventAdImageView;
    /**
     * RecyclerView XML object used to show events.
     */
    private RecyclerView EventsRecyclerView;
    /**
     * ProgressBar XML object used before the events are loaded in.
     */
    public ProgressBar EventBuildProgressBar;
    /**
     * RelativeLayout XML object used for the EventsRecyclerView object.
     */
    public RelativeLayout EventRelativeLayout;
    /**
     * EventAdapter object used to display the events to the EventsRecyclerView object.
     */
    private EventAdapter AdapterRecyclerView;
    /**
     * LayoutManager object used to display the EventsRecyclerView object inside the EventsActivity layout.
     */
    private RecyclerView.LayoutManager LayoutManagerRecyclerView;

    /**
     * Integer value that is incremented upon the user touch on the EventShapeTextView.
     */
    private int counter = 0;

    /**
     * Integer value that is incremented upon the user touch on the EventShapeTextView for groups.
     */
    private int counterGroups = 0;

    /**
     * String value of the user's email address.
     */
    private String email;

    /**
     * String value of the required server operation.
     */
    private String operation;

    /**
     * Boolean value that indicates if the ArrayList is empty or not.
     */
    private boolean empty;

    /**
     * String value of the current date.
     */
    private final String date = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
    /**
     * String value of the current time.
     */
    private final String time = new SimpleDateFormat("HH:mm",Locale.getDefault()).format(new Date());
    /**
     * ArrayList object containing events in the form of EventItem objects.
     */
    private final ArrayList<EventItem> eventList = new ArrayList<>();
    /**
     * Object used to set up notifications.
     */
    private NotificationManagerCompat notificationManager;

    /**
     * Called whenever this activity is created.
     * Sets up the XML variables of the activity and the title of the page.
     * Checks for the email address of the user.
     * If there are any existent events in the user account, these are retrieved via the ServerEventAsyncTask class.
     *  Also sets up the drag feature for deleting an event.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events);

        setTitle("Events");

        EventsBottomNavigationView = findViewById(R.id.EventsBottomNavigationView);
        EventsBottomNavigationView.setOnNavigationItemSelectedListener(EventsnavListener);

        EventShapeTextView = findViewById(R.id.EventShapeTextView);
        EventBuildProgressBar = findViewById(R.id.EventBuildProgressBar);
        EventRelativeLayout = findViewById(R.id.EventRelativeLayout);
        EventsRecyclerView = findViewById(R.id.EventsRecyclerView);
        EventsRecyclerView.setHasFixedSize(true); //melhora a eficacia da app

        EventShapeTextView.setVisibility(View.VISIBLE);
        EventBuildProgressBar.setVisibility(View.VISIBLE);
        EventRelativeLayout.setVisibility(View.INVISIBLE);

        EventAdImageView = findViewById(R.id.EventAdImageView);

        checkUser();

        checkIncomingEmailIntent();

        alarm();

        operation = "get";

        new ServerEventAsyncTask(this, operation, email).execute();

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
             * Doing this action results in deleting the affected event.
             * This action makes use of the ServerEventAsyncTask class to signal the server to do this operation.
             * @param viewHolder the recyclerview ViewHolder responsible for setting upon the specific XML objects.
             * @param direction the direction of the action.
             */
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction)
            {


                if(!empty)
                {
                    ArrayList<String> clubs = GlobalStorage.getInstance().getclubList();
                    if(!clubs.contains(AdapterRecyclerView.getEventItem(viewHolder.getAdapterPosition()).getType())) {
                        Toast.makeText(EventsActivity.this, "Can't delete events that are not yours", Toast.LENGTH_SHORT).show();
                        updateEventData();
                    }
                    else {
                        EventBuildProgressBar.setVisibility(View.VISIBLE);
                        EventRelativeLayout.setVisibility(View.INVISIBLE);
                        new ServerEventAsyncTask(EventsActivity.this, "delete", email, AdapterRecyclerView.getEventItem(viewHolder.getAdapterPosition())).execute();
                        eventList.remove(AdapterRecyclerView.getEventItem(viewHolder.getAdapterPosition()));
                        eventList.clear();
                        new ServerEventAsyncTask(EventsActivity.this, operation, email).execute();
                    }
                }

            }
        }).attachToRecyclerView(EventsRecyclerView);
    }

    /**
     * Method that overrides and setups an onclick interaction using the setOnEventClickListener() method of the EventAdapter class.
     */
    private void enableEditOption()
    {
        AdapterRecyclerView.setOnEventClickListener(new EventAdapter.onEventClickListener()
        {
            /**
            * Method that handles the onclick interaction with the specific event that the user touches.
            * It starts the AddEditEventActivity class for changing the data of the selected event.
            * Handles the contents of the selected event to the AddEditEventActivity class.
            */
            @Override
            public void onEventClick(EventItem eventItem)
            {
                Intent intent = new Intent(EventsActivity.this, AddEditEventActivity.class);
                intent.putExtra(AddEditEventActivity.EXTRA_NEW_EVENT_ID, eventItem.getId());
                intent.putExtra(AddEditEventActivity.EXTRA_NEW_TYPE, eventItem.getType());
                intent.putExtra(AddEditEventActivity.EXTRA_NEW_NAME, eventItem.getName());
                intent.putExtra(AddEditEventActivity.EXTRA_NEW_START_TIME, eventItem.getStartTime());
                intent.putExtra(AddEditEventActivity.EXTRA_NEW__START_DATE, eventItem.getStartDate());
                intent.putExtra(AddEditEventActivity.EXTRA_NEW_END_TIME, eventItem.getEndTime());
                intent.putExtra(AddEditEventActivity.EXTRA_NEW_END_DATE, eventItem.getEndDate());
                startActivityForResult(intent, EDIT_EVENT_REQUEST);
            }
        });
    }

    /**
     * Method called when the AddEditEventActivity class, that was launched here, is exited,
     * giving the requestCode started it with, the resultCode it returned, and the new event data inserted by the user.
     * Handles the new/edited data to the server via the ServerEventAsyncTask class, clears the previous ArrayList and gets the updated data from the server.
     * @param requestCode code indicating which activity result is incoming.
     * @param resultCode code indicating the result of the incoming activity result.
     * @param data Intent (containing result data) returned by the incoming activity result.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        EventBuildProgressBar.setVisibility(View.VISIBLE);
        EventRelativeLayout.setVisibility(View.INVISIBLE);

        if(requestCode == ADD_EVENT_REQUEST && resultCode == RESULT_OK)
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

            sendOnEventChannel(newstartdate, newtype, newname, newstarttime, newstarttime + " " + newendtime);
        }
        else if(requestCode == EDIT_EVENT_REQUEST && resultCode == RESULT_OK)
        {

            String newtype = data.getStringExtra(AddEditEventActivity.EXTRA_NEW_TYPE);
            String newname = data.getStringExtra(AddEditEventActivity.EXTRA_NEW_NAME);
            String newstarttime = data.getStringExtra(AddEditEventActivity.EXTRA_NEW_START_TIME);
            String newstartdate = data.getStringExtra(AddEditEventActivity.EXTRA_NEW__START_DATE);
            String newendtime = data.getStringExtra(AddEditEventActivity.EXTRA_NEW_END_TIME);
            String newenddate = data.getStringExtra(AddEditEventActivity.EXTRA_NEW_END_DATE);
            String newid = data.getStringExtra(AddEditEventActivity.EXTRA_NEW_EVENT_ID);

            EventItem eventItem = new EventItem(R.drawable.ic_launcher_background, newtype, newname, newstarttime, newstartdate, newendtime, newenddate, newid);

            new ServerEventAsyncTask(this, "edit", email, eventItem).execute();

            Toast.makeText(this, "Event Updated", Toast.LENGTH_SHORT).show();

            sendOnEventChannel(newstartdate, newtype, newname, newstarttime, newstarttime + " " + newendtime);
        }

        eventList.clear();

        new ServerEventAsyncTask(this, operation, email).execute();
    }

    /**
     * Method used to add the retrieved events from the server and modify them on the app to an ArrayList so that they're data can be displayed on the UI.
     * It also checks if there is any null and duplicated content on the date and time of the event and if there are any "all day events".
     * These "all day events" are then chose to be displayed with only their date.
     * @param eventItem the specific contents of the group retrieved.
     */
    public void createEventData(EventItem eventItem)
    {

        String eventtype = eventItem.getType();
        String eventname = eventItem.getName();
        String eventstarttime = eventItem.getStartTime();
        String eventstartdate = eventItem.getStartDate();
        String eventendtime = eventItem.getEndTime();
        String eventenddate = eventItem.getEndDate();
        String eventid = eventItem.getId();

        int typecolor = setTypeColor(eventtype);
        boolean nulltime = false;

        if(eventstarttime.equals("00:00"))
        {
            if(eventendtime.equals("00:00"))
            {
                eventendtime = "";
                eventstarttime = "";
                nulltime = true;
            }
        }
        if(eventstartdate.equals(eventenddate))
        {
            eventenddate = "";
        }

        if(!nulltime)
        {
            sendOnEventChannel(eventstartdate, eventtype, eventname, eventstarttime, eventstarttime + " " + eventendtime);
        }
        eventList.add(new EventItem(typecolor, eventtype, eventname, eventstarttime, eventstartdate, eventendtime, eventenddate, eventid));

        LayoutManagerRecyclerView = new LinearLayoutManager(this);
        EventsRecyclerView.setLayoutManager(LayoutManagerRecyclerView);

        updateEventData();
    }

    /**
     * Method called in which the UI is updated, via the EventAdapter class, with the updated contents of the events stored in the ArrayList.
     * It also calls the enableEditOption() method to enable the onclick interactions of the events.
     */
    public void updateEventData()
    {
        if(eventList.isEmpty())
        {
            String sr = "No events on this group!";
            EventItem ei = new EventItem(0, "", sr, "", "", "", "");
            eventList.add(ei);
            empty = true;
        }
        AdapterRecyclerView = new EventAdapter(eventList);
        EventsRecyclerView.setAdapter(AdapterRecyclerView);

        if(!empty){ enableEditOption();}
    }

    /**
     * Method responsible for switching between the different events that the user has.
     * It will switch between all of the events, all the events from the current date to a month/week forward.
     * And also all the events associated with a specific group in which the user is in.
     * This action makes use of the ServerEventAsyncTask class to signal the server to get these specific events.
     */
    public void onClickSwitch(View v)
    {
        EventBuildProgressBar.setVisibility(View.VISIBLE);
        EventRelativeLayout.setVisibility(View.INVISIBLE);
        eventList.clear();

        operation = null;

        counter += 1;

        switch (counter)
        {
            case 1:
                operation = "_month_";
                EventShapeTextView.setText("Monthly Events");
                break;
            case 2:
                operation = "_week_" ;
                EventShapeTextView.setText("Weekly Events");
                break;
            case 3:
                if(counterGroups<TYPES.size())
                {
                    String gName = TYPES.get(counterGroups);
                    if(gName.equals("Primary"))
                    {
                        counterGroups++;
                        gName = TYPES.get(counterGroups);
                    }

                    EventShapeTextView.setText( gName+ " Events");
                    counter = counter - 1;
                    operation = "get groups:" + gName;
                    counterGroups += 1;
                }
                else
                {
                    counter = 0;
                    counterGroups = 0;
                    operation = "get";
                    EventShapeTextView.setText("All Events");
                }
                break;
        }
        Toast.makeText(this, "Event View Changed", Toast.LENGTH_SHORT).show();
        delay();
        new ServerEventAsyncTask(this, operation, email).execute();
    }

    /**
     * Method called upon the user clicking on the add event button on the top right corner of the page.
     * It starts the AddEditEventActivity class for the creation of a new event.
     */
    public void onClickAddEvent()
    {
        Intent intent = new Intent(EventsActivity.this, AddEditEventActivity.class);
        startActivityForResult(intent, ADD_EVENT_REQUEST);
    }

    /**
     * Method called upon the user clicking the logoff button on the top right corner of the page.
     * Heads the user to the login page first displayed when the user enters the app.
     */
    public void onClickLogOff()
    {
        Intent logoffevents = new Intent(this, LoginActivity.class);
        logoffevents.putExtra(EXTRA_EVENTS_LOGOUT, "LOGOUT");
        logoffevents.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(logoffevents);
        finish();
    }

    /**
     * Method responsible for preventing the overload of the UI based on the switch input between the different events that the user has.
     * It will create a delay of 5 seconds before the user can switch between events again, which is around the same time the communication with the server takes.
     */
    private void delay()
    {
        EventShapeTextView.setVisibility(View.INVISIBLE);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                EventShapeTextView.setVisibility(View.VISIBLE);
            }
        }, 3000);
    }

    /**
     * Method responsible for checking if the user has premium status or not using the GlobalStorage class.
     * Depending on the user's status, ads will be displayed or not.
     */
    public void checkUser()
    {
        if(GlobalStorage.getInstance().isPremium())
        {
            EventAdImageView.setVisibility(View.INVISIBLE);
        }
        else
        {
            EventAdImageView.setVisibility(View.VISIBLE);
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
     * Method that defines the color of the event based on it's type.
     * Currently there are 3 colors.
     * @param type the type of the event.
     * @return an integer that references to the the drawable that the specific event will have.
     */
    private int setTypeColor(String type)
    {
        int colorType;

        if (type.equals("Sports") || type.equals("sports"))
        {
            colorType = R.drawable.ic_event_color_sports;
        }
        else if(type.equals("Gym") || type.equals("gym"))
        {
            colorType = R.drawable.ic_event_color_gym;
        }
        else
        {
            colorType = R.drawable.ic_event_color_misc;
        }
        return colorType;
    }

    /**
     *  Method that allows the notification interaction,
     *  one hour before the event, five minutes before the event and at the time of the event, all this when the user is using the app.
     *  One of the channels created in the Notifications class is used and the contents of the notification are also set up.
     */
    private void sendOnEventChannel(String eventdate, String type, String name, String starthour, String eventtime)
    {
        if(GlobalStorage.getInstance().hasEventnots())
        {
            notificationManager = NotificationManagerCompat.from(this);

            String[] vec = starthour.split(":");
            int hour = Integer.parseInt(vec[0]);
            int minuts = Integer.parseInt(vec[1]);

            String[] vect = time.split(":");
            int actualhour = Integer.parseInt(vect[0]);
            int actualminuts = Integer.parseInt(vect[1]);

            if (eventdate.equals(date) && ((hour == actualhour + 1) || ((hour == actualhour) && ((minuts == actualminuts + 5) || minuts == actualminuts))))
            {
                Notification notification = new NotificationCompat.Builder(this, CHANNEL_1_ID)

                        .setSmallIcon(R.drawable.ic_events_list)
                        .setContentTitle(type)
                        .setContentText(name + " " + eventtime)
                        .setAutoCancel(true)//a notificação desaparece ao carregar
                        .build();

                notificationManager.notify(1, notification);
            }
        }
    }

    /**
     * Method that sets up an alarm after 1 minute of entering the page.
     * It signals the AlertReceiver class to receive this alarm and then send the notification.
     * The alarm will still be triggered, even if this page is exited or the app is closed before this time mark.
     * This operation must be done inside an if statement to make sure that the device has an API level > 19 Kitkat.
     */
    private void alarm()
    {
        if(GlobalStorage.getInstance().hasGymnots())
        {
            Calendar present = Calendar.getInstance();
            present.set(Calendar.MINUTE, present.get(Calendar.MINUTE) + 1);
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(this, AlertReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1, intent, 0);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, present.getTimeInMillis(), pendingIntent);
            }
        }
    }

    /**
     * Method responsible for setting up a menu from a XML file, upon which the UI can draw upon in the top right corner of the page.
     * @return a boolean that is true for the menu to be displayed.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.events_menu, menu);
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
            case R.id.AddEventMenu:
                onClickAddEvent();
                return true;
            case R.id.LogOffEventMenu:
                onClickLogOff();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Object responsible for handling with the navigation between the app's main 4 pages.
     */
    private final BottomNavigationView.OnNavigationItemSelectedListener EventsnavListener = new BottomNavigationView.OnNavigationItemSelectedListener()
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
                    Intent evetnstocalendar = new Intent(EventsActivity.this, CalendarActivity.class);
                    evetnstocalendar.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(evetnstocalendar);
                    finish();
                    break;
                case R.id.EventsMenu:
                    break;
                case R.id.GroupsMenu:
                    Intent eventstogroups = new Intent(EventsActivity.this, GroupsActivity.class);
                    eventstogroups.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(eventstogroups);
                    finish();
                    break;
                case R.id.SettingsMenu:
                    Intent eventstoprofile = new Intent(EventsActivity.this, ProfileActivity.class);
                    eventstoprofile.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(eventstoprofile);
                    finish();
            }
            return true;
        }
    };
}