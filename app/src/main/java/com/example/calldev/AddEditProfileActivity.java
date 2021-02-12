package com.example.calldev;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Toast;

import com.example.calldev.other.GlobalStorage;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * Date: Nov 28-2020.
 * This is a class that extends activity and represents the subpage of the app in which the user can add/edit it's profile.
 * It is spawned from the ProfileActivity class.
 * @author CALDEV.
 */
public class AddEditProfileActivity extends AppCompatActivity
{
    /**
     * String constant used to distinguish this activity (used in debugging and to show the user pop up messages).
     */
    private static final String TAG = "AddEditProfileActivity";
    /**
     * String constant passed to the ProfileActivity class to distinguish the add profile from the edit profile action.
     */
    public static final String EXTRA_NEW_PROFILE_ID = "com.example.login.example.EXTRA_NEW_PROFILE_ID";
    /**
     * String constant passed to the ProfileActivity class with the inserted username.
     */
    public static final String EXTRA_NEW_USERNAME = "com.example.login.example.EXTRA_NEW_USERNAME";
    /**
     * String constant passed to the ProfileActivity class with the inserted age.
     */
    public static final String EXTRA_NEW_AGE = "com.example.login.example.EXTRA_NEW_AGE";
    /**
     * String constant passed to the ProfileActivity class with the inserted weight.
     */
    public static final String EXTRA_NEW_WEIGHT = "com.example.login.example.EXTRA_NEW_WEIGHT";
    /**
     * String constant passed to the ProfileActivity class with the inserted height.
     */
    public static final String EXTRA_NEW_HEIGHT = "com.example.login.example.EXTRA_NEW_HEIGHT";
    /**
     * String constant passed to the ProfileActivity class with the calculated bmi.
     */
    public static final String EXTRA_NEW_BMI = "com.example.login.example.EXTRA_NEW_BMI";
    /**
     * String constant passed to the ProfileActivity class with the user's email.
     */
    public static final String EXTRA_EMAIL = "com.example.login.example.EXTRA_EMAIL";

    /**
     * EditText XML object used to insert the user's username.
     */
    private EditText NewProfileNameEditText;
    /**
     * EditText XML object used to insert the user's weight.
     */
    private EditText WeightEditText;
    /**
     * NumberPicker XML object used to select the user's age.
     */
    private NumberPicker AgeNumberPicker;
    /**
     * NumberPicker XML object used to select the user's height.
     */
    private NumberPicker HeightNumberPicker;
    /**
     * Button XML object used to skip the add profile action.
     */
    private Button SkippButton;

    /**
     * String value of the user's email address.
     */
    private String email;

    /**
     * Called whenever this activity is created.
     * Sets up the XML variables of the activity and the title of the page.
     * Checks which input from the user originated this page (add or edit).
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_profile);

        NewProfileNameEditText = findViewById(R.id.NewProfileNameEditText);
        AgeNumberPicker = findViewById(R.id.AgeNumberPicker);
        WeightEditText = findViewById(R.id.WeightEditText);
        HeightNumberPicker = findViewById(R.id.HeightNumberPicker);
        SkippButton = findViewById(R.id.SkippButton);

        AgeNumberPicker.setMinValue(15);
        AgeNumberPicker.setMaxValue(70);

        HeightNumberPicker.setMinValue(140);
        HeightNumberPicker.setMaxValue(220);

        Intent intent = getIntent();

        email = GlobalStorage.getInstance().getEmail();

        if(intent.hasExtra(EXTRA_NEW_PROFILE_ID))
        {
            setTitle("Edit Profile");
            SkippButton.setVisibility(View.INVISIBLE);
            NewProfileNameEditText.setText(intent.getStringExtra(EXTRA_NEW_USERNAME));

            String sage = intent.getStringExtra(EXTRA_NEW_AGE);
            int age = Integer.parseInt(sage);
            AgeNumberPicker.setValue(age);

            WeightEditText.setText(intent.getStringExtra(EXTRA_NEW_WEIGHT));

            String sheight = intent.getStringExtra(EXTRA_NEW_HEIGHT);
            int height = Integer.parseInt(sheight);
            HeightNumberPicker.setValue(height);
        }
        else
        {
            setTitle("Add Profile");
            SkippButton.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Method that is called when the users presses the save button on the top right corner of the page.
     * It saves the profile data inserted by the user and exits this subpage.
     * If the user did not fill in all required information a message will appear.
     */
    private void saveProfile()
    {
        String newusername = NewProfileNameEditText.getText().toString();
        int newage = AgeNumberPicker.getValue();
        String newweight = WeightEditText.getText().toString();
        int newheight = HeightNumberPicker.getValue();

        double dweight = Double.parseDouble(newweight);
        double dheight = newheight * 0.01;

        double newbmi = (dweight / (dheight * dheight));

        NumberFormat formater = new DecimalFormat("#0.00");

        if(newusername.trim().isEmpty())
        {
            Toast.makeText(this, "Please Enter An Username", Toast.LENGTH_SHORT).show();
        }
        else if(newweight.trim().isEmpty())
        {
            Toast.makeText(this, "Please Enter An Weight Value", Toast.LENGTH_SHORT).show();
        }
        else
        {
            String age = String.valueOf(newage);
            String height = String.valueOf(newheight);
            String bmi = formater.format(newbmi);

            Intent profiledata = new Intent();
            profiledata.putExtra(EXTRA_NEW_USERNAME, newusername);
            profiledata.putExtra(EXTRA_NEW_AGE, age);
            profiledata.putExtra(EXTRA_NEW_WEIGHT, newweight);
            profiledata.putExtra(EXTRA_NEW_HEIGHT, height);
            profiledata.putExtra(EXTRA_NEW_BMI, bmi);
            profiledata.putExtra(EXTRA_EMAIL, email);

            int id = profiledata.getIntExtra(EXTRA_NEW_PROFILE_ID, -1);
            if (id != -1) {
                profiledata.putExtra(EXTRA_NEW_PROFILE_ID, id);
            }

            setResult(RESULT_OK, profiledata);
            finish();
        }
    }

    /**
     * Method that is called when the users chooses to skip the creation of a profile, redirecting him to the Calendar page.
     */
    public void onClickSkippButton(View view)
    {
        Intent profiletocalendar = new Intent(this, CalendarActivity.class);
        profiletocalendar.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(profiletocalendar);
        finish();
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
            saveProfile();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}