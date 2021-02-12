package com.example.calldev;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.calldev.other.GlobalStorage;

import java.util.ArrayList;

/**
 * Date: Nov 28-2020.
 * This is a class that extends activity and represents the subpage of the app in which the user can add/edit it's events.
 * It is spawned from the EventsActivity class.
 * @author CALDEV.
 */

public class AddEditEventActivity extends AppCompatActivity
{
    /**
     * String constant used to distinguish this activity (used in debugging and to show the user pop up messages).
     */
    private static final String TAG = "AddEditEventActivity";
    /**
     * String constant passed to the EventsActivity class to distinguish the add event from the edit event action.
     * It also passes the event's id.
     */
    public static final String EXTRA_NEW_EVENT_ID = "com.example.login.example.EXTRA_NEW_EVENT_ID";
    /**
     * String constant passed to the EventsActivity class with the inserted event's type.
     */
    public static final String EXTRA_NEW_TYPE = "com.example.login.example.EXTRA_NEW_TYPE";
    /**
     * String constant passed to the EventsActivity class with the inserted event's name.
     */
    public static final String EXTRA_NEW_NAME = "com.example.login.example.EXTRA_NEW_NAME";
    /**
     * String constant passed to the EventsActivity class with the inserted event's start time.
     */
    public static final String EXTRA_NEW_START_TIME = "com.example.login.example.EXTRA_NEW_TIME";
    /**
     * String constant passed to the EventsActivity class with the inserted event's start date.
     */
    public static final String EXTRA_NEW__START_DATE = "com.example.login.example.EXTRA_NEW_DATE";
    /**
     * String constant passed to the EventsActivity class with the inserted event's end time.
     */
    public static final String EXTRA_NEW_END_TIME = "com.example.login.example.EXTRA_NEW_END_TIME";
    /**
     * String constant passed to the EventsActivity class with the inserted event's end date.
     */
    public static final String EXTRA_NEW_END_DATE = "com.example.login.example.EXTRA_NEW_END_DATE";

    /**
     * AutoCompleteTextView XML object used to insert the event's type.
     */
    private AutoCompleteTextView NewTypeAutoText;
    /**
     * EditText XML object used to insert the event's username.
     */
    private EditText NewNameEditText;
    /**
     * TimePicker XML object used to select the event's start time.
     */
    private TimePicker StartTimeTimePicker;
    /**
     * TimePicker XML object used to select the event's end time.
     */
    private TimePicker EndTimeTimePicker;
    /**
     * TimePicker XML object used to select the event's start date.
     */
    private DatePicker StartDateDatePicker;
    /**
     * TimePicker XML object used to select the event's end date.
     */
    private DatePicker EndDateDatePicker;

    /**
     * String value of the inputted event's name.
     */
    private String sname;
    /**
     * String value of the inputted event's type.
     */
    private String editype;
    /**
     * String value of the selected event's id.
     */
    private String editid;
    /**
     * Integer value of the event's start hour.
     */
    private int istarthour;
    /**
     * Integer value of the event's start minutes.
     */
    private int istartminutes;
    /**
     * Integer value of the event's start day.
     */
    private int istartday;
    /**
     * Integer value of the event's start month.
     */
    private int istartmonth;
    /**
     * Integer value of the event's start year.
     */
    private int istartyear;
    /**
     * Integer value of the event's end hour.
     */
    private int iendhour;
    /**
     * Integer value of the event's end minutes.
     */
    private int iendminutes;
    /**
     * Integer value of the event's end day.
     */
    private int iendday;
    /**
     * Integer value of the event's end month.
     */
    private int iendmonth;
    /**
     * Integer value of the event's end year.
     */
    private int iendyear;
    /**
     * Integer value used to distinguish the add event from the edit event action.
     */
    private int operation;

    /**
     * Called whenever this activity is created.
     * Sets up the XML variables of the activity and the title of the page.
     * Checks which input from the user originated this page (add or edit).
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_event);

        NewTypeAutoText = findViewById(R.id.NewTypeAutoText);
        NewNameEditText = findViewById(R.id.NewNameEditText);
        StartTimeTimePicker = findViewById(R.id.StartTimeTimePicker);
        EndTimeTimePicker = findViewById(R.id.EndTimeTimePicker);
        StartDateDatePicker = findViewById(R.id.StartDateDatePicker);
        EndDateDatePicker = findViewById((R.id.EndDateDatePicker));

        ArrayList<String> clubs = GlobalStorage.getInstance().getclubList();

        /**
         * Array of Strings constant containing the default event's types.
         */
        String[] TYPES = clubs.toArray(new String[0]);

        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, TYPES);
        NewTypeAutoText.setAdapter(typeAdapter);

        StartTimeTimePicker.setIs24HourView(true);
        EndTimeTimePicker.setIs24HourView(true);

        Intent intent = getIntent();

        if(intent.hasExtra(EXTRA_NEW_EVENT_ID))
        {
            setTitle("Edit Event");

            operation = 1;

            editid = intent.getStringExtra(EXTRA_NEW_EVENT_ID);
            editype = intent.getStringExtra(EXTRA_NEW_TYPE);

            NewTypeAutoText.setVisibility(View.INVISIBLE);

            NewNameEditText.setText(intent.getStringExtra(EXTRA_NEW_NAME));
            String stime = intent.getStringExtra(EXTRA_NEW_START_TIME);
            if(!stime.equals(""))
            {

                String[] starttime = stime.split(":");
                int starthour = Integer.parseInt(starttime[0]);
                int startminutes = Integer.parseInt(starttime[1]);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                {
                    StartTimeTimePicker.setHour(starthour);
                    StartTimeTimePicker.setMinute(startminutes);
                }
            }
            String etime = intent.getStringExtra(EXTRA_NEW_END_TIME);
            if(!etime.equals(""))
            {
                String[] endtime = etime.split(":");
                int endhour = Integer.parseInt(endtime[0]);
                int endminutes = Integer.parseInt(endtime[1]);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                {
                    EndTimeTimePicker.setHour(endhour);
                    EndTimeTimePicker.setMinute(endminutes);
                }
            }
            String sdate = intent.getStringExtra(EXTRA_NEW__START_DATE);
            String[] startdate = sdate.split("-");
            int startday = Integer.parseInt(startdate[0]);
            int startmonth = Integer.parseInt(startdate[1]) -1 ;
            int startyear = Integer.parseInt(startdate[2]);
            StartDateDatePicker.updateDate(startyear, startmonth, startday);
            String edate = intent.getStringExtra(EXTRA_NEW_END_DATE);
            if(!edate.equals(""))
            {
                String[] enddate = edate.split("-");
                int endday = Integer.parseInt(enddate[0]);
                int endmonth = Integer.parseInt(enddate[1]) -1 ;
                int endyear = Integer.parseInt(enddate[2]);
                EndDateDatePicker.updateDate(endyear, endmonth, endday);
            }
            else EndDateDatePicker.updateDate(startyear, startmonth, startday);
        }
        else
        {
            String str;
            setTitle("Add Event");

            operation = 0;

            NewTypeAutoText.setVisibility(View.VISIBLE);

            if((str=intent.getStringExtra("EXTRA_DATA")) != null)
            {
                String[] startdate = str.split("-");
                int startday = Integer.parseInt(startdate[0]);
                int startmonth = Integer.parseInt(startdate[1]);
                int startyear = Integer.parseInt(startdate[2]);

                StartDateDatePicker.updateDate(startyear , startmonth-1, startday);
                EndDateDatePicker.updateDate(startyear , startmonth-1, startday);
            }
        }
    }

    /**
     * Method that is called when the users presses the save button on the top right corner of the page.
     * It saves the event data inserted by the user and exits this subpage.
     * If the user did not fill in all required information or inserted invalid data that the checkError() method checks, a message will appear.
     */
    private void saveEvent()
    {
        if(operation == 0)
        {
            editype = NewTypeAutoText.getText().toString();
        }
        sname = NewNameEditText.getText().toString();
        istarthour = StartTimeTimePicker.getCurrentHour();
        istartminutes = StartTimeTimePicker.getCurrentMinute();
        istartday = StartDateDatePicker.getDayOfMonth();
        istartmonth = StartDateDatePicker.getMonth() + 1;
        istartyear = StartDateDatePicker.getYear();
        iendhour = EndTimeTimePicker.getCurrentHour();
        iendminutes = EndTimeTimePicker.getCurrentMinute();
        iendday = EndDateDatePicker.getDayOfMonth();
        iendmonth = EndDateDatePicker.getMonth() + 1;
        iendyear = EndDateDatePicker.getYear();

        if(checkError())
        {
            Toast.makeText(this, "Please Try Again", Toast.LENGTH_SHORT).show();
        }
        else
        {
            String newstarttime = istarthour + ":" + istartminutes;
            String newstartdate = istartday + "-" + istartmonth + "-" + istartyear; //to send to server
            String newendtime = iendhour + ":" + iendminutes;
            String newenddate = iendday + "-" + iendmonth + "-" + iendyear; //to send to server


            if (newstarttime.charAt(1) == ':') newstarttime = "0" + newstarttime;
            if (newstarttime.length() < 5)
                newstarttime = newstarttime.substring(0, 3) + "0" + newstarttime.substring(3);

            if (newstartdate.charAt(1) == '-') newstartdate = "0" + newstartdate;
            if (newstartdate.charAt(4) == '-')
                newstartdate = newstartdate.substring(0, 3) + "0" + newstartdate.substring(3);

            if (newendtime.charAt(1) == ':') newendtime = "0" + newendtime;
            if (newendtime.length() < 5)
                newendtime = newendtime.substring(0, 3) + "0" + newendtime.substring(3);

            if (newenddate.charAt(1) == '-') newenddate = "0" + newenddate;
            if (newenddate.charAt(4) == '-')
                newenddate = newenddate.substring(0, 3) + "0" + newenddate.substring(3);

            Intent eventdata = new Intent();
            eventdata.putExtra(EXTRA_NEW_TYPE, editype);
            eventdata.putExtra(EXTRA_NEW_NAME, sname);
            eventdata.putExtra(EXTRA_NEW_START_TIME, newstarttime);
            eventdata.putExtra(EXTRA_NEW_END_TIME, newendtime);
            eventdata.putExtra(EXTRA_NEW__START_DATE, newstartdate);
            eventdata.putExtra(EXTRA_NEW_END_DATE, newenddate);
            if (operation == 1)
            {
                eventdata.putExtra(EXTRA_NEW_EVENT_ID, editid);
            }

            setResult(RESULT_OK, eventdata);
            finish();
        }
    }

    /**
     * Method responsible for checking the input data of the user.
     * @return a boolean that is true if an error was detected and false if not.
     */
    private boolean checkError()
    {
        if(operation == 0)
        {
            if(editype.trim().isEmpty())
            {
                Toast.makeText(this, "Please Enter A Type for the Event", Toast.LENGTH_SHORT).show();
                return true;
            }
        }
        if (sname.trim().isEmpty())
        {
            Toast.makeText(this, "Please Enter A Name for the Event", Toast.LENGTH_SHORT).show();
            return true;
        }
        else if((istartyear > iendyear))
        {
            Toast.makeText(this, "Invalid Date", Toast.LENGTH_SHORT).show();
            return true;
        }
        else if((istartmonth > iendmonth) && (istartyear == iendyear))
        {
            Toast.makeText(this, "Invalid Date", Toast.LENGTH_SHORT).show();
            return true;
        }
        else if((istartday > iendday) && ((istartmonth == iendmonth) && (istartyear == iendyear)))
        {
            Toast.makeText(this, "Invalid Date", Toast.LENGTH_SHORT).show();
            return true;
        }
        else if((istartday == iendday) && (istartmonth == iendmonth) && (istartyear == iendyear))
        {
           if((istarthour > iendhour) || ((istarthour == iendhour) && (istartminutes > iendminutes)))
           {
               Toast.makeText(this, "Invalid Date", Toast.LENGTH_SHORT).show();
               return true;
           }
        }
        return false;
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
            saveEvent();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}